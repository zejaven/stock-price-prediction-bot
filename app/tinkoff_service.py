import configparser
import os
from datetime import datetime, timedelta

import numpy as np
import pandas as pd
import pytz
import requests
from matplotlib import pyplot as plt
from scipy.stats import pearsonr
from sklearn.metrics import mean_squared_error, mean_absolute_error, r2_score, mean_absolute_percentage_error
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import MinMaxScaler
from tensorflow.keras.callbacks import EarlyStopping, ReduceLROnPlateau
from tensorflow.keras.layers import Dense, Dropout, LSTM, Bidirectional
from tensorflow.keras.models import Sequential
from tensorflow.keras.models import load_model
from tinkoff.invest import Client, CandleInterval

from knowledge_base_service import check_r2_score, generate_recommendation

# Read the API token and Bot url from the configuration files
config = configparser.ConfigParser()
config.read(['config.ini', 'hidden.ini'])

API_TOKEN = config['DEFAULT']['API_TOKEN']
BOT_URL = os.getenv('BOT_URL', config['DEFAULT']['BOT_URL'])
RESOURCE_PATH = os.getenv('RESOURCE_PATH', config['DEFAULT']['RESOURCE_PATH'])
ADMIN_CHAT_ID = config['DEFAULT']['ADMIN_CHAT_ID']


def make_prediction(ticker, intervalChoice, chatId):
    # Define the start and end dates for historical data retrieval
    end_date = datetime.now()
    if intervalChoice == 'hour':
        start_date = end_date - timedelta(hours=1825)
        interval = CandleInterval.CANDLE_INTERVAL_HOUR
    elif intervalChoice == 'day':
        start_date = end_date - timedelta(days=365 * 5)
        interval = CandleInterval.CANDLE_INTERVAL_DAY
    else:
        requests.post(BOT_URL, json={
            "chatId": chatId,
            "text": "Invalid choice. Please enter 'hour' or 'day'."
        })
        raise ValueError("Invalid choice. Please enter 'hour' or 'day'.")

    iso_start_date = start_date.date().isoformat()
    iso_end_date = end_date.date().isoformat()

    file_folder, filename, figi = get_data(ticker, start_date, end_date, iso_start_date, iso_end_date, interval, intervalChoice, chatId)
    df_stock, scaler, scaled_data, window_size, x, y = data_preprocessing(ticker, file_folder, filename, intervalChoice, start_date, chatId)
    model, x_test, y_test, history, evaluated = training(ticker, iso_start_date, iso_end_date, file_folder, x, y, intervalChoice, chatId)
    if not evaluated:
        evaluate(ticker, iso_start_date, iso_end_date, file_folder, model, df_stock, x_test, y_test, history, scaler, scaled_data, intervalChoice, chatId)
    predict(ticker, model, df_stock, scaler, intervalChoice, chatId)


def get_data(ticker, start_date, end_date, iso_start_date, iso_end_date, interval, intervalChoice, chatId):
    # Get available tickers
    available_tickers = get_available_tickers()

    # Check if the ticker is available
    if ticker not in available_tickers:
        requests.post(BOT_URL, json={
            "chatId": chatId,
            "text": "Ticker not found. Please check the ticker symbol and try again."
        })
        raise ValueError("Ticker not found. Please check the ticker symbol and try again.")

    # Get FIGI for the selected ticker
    figi = get_figi(ticker)

    file_label = 'Dataframe' + '_' + intervalChoice
    filename = file_label + '_' + iso_start_date + '_' + iso_end_date + '.csv'
    file_folder = RESOURCE_PATH + '/' + ticker
    file_path = file_folder + '/' + filename
    if os.path.exists(file_path):
        requests.post(BOT_URL, json={
            "chatId": chatId,
            "text": 'Taking historical data from the storage for ' + ticker
        })
        return file_folder, filename, figi
    else:
        remove_old_file(file_folder, file_label)

        # Retrieve historical price data for the selected ticker
        df_stock = get_historical_data(figi, start_date, end_date, interval)
        if df_stock.empty:
            requests.post(BOT_URL, json={
                "chatId": chatId,
                "text": f"Data for '{intervalChoice}' interval not found"
            })
            raise ValueError(f"Data for '{intervalChoice}' interval not found")

        os.makedirs(os.path.dirname(file_path), exist_ok=True)
        df_stock.to_csv(file_path)

        requests.post(BOT_URL, json={
            "chatId": chatId,
            "text": 'Saved historical data for ' + ticker
        })

        return file_folder, filename, figi


def data_preprocessing(ticker, file_folder, filename, intervalChoice, start_date, chatId):
    requests.post(BOT_URL, json={
        "chatId": chatId,
        "text": 'Preprocessing data for ' + ticker
    })
    df_stock = pd.read_csv(file_folder + '/' + filename)
    df_stock['DateTime'] = pd.to_datetime(df_stock['DateTime'])
    df_stock.set_index('DateTime', inplace=True)

    if intervalChoice == 'day':
        td = timedelta(days=8)
        warning = '5 years'
    elif intervalChoice == 'hour':
        td = timedelta(hours=40)
        warning = '2.5 months'
    else:
        requests.post(BOT_URL, json={
            "chatId": chatId,
            "text": f"Such interval is not configured: '{intervalChoice}'"
        })
        raise ValueError(f"Such interval is not configured: '{intervalChoice}'")

    # Check if the data is less than configured interval units
    if df_stock.index[0] <= pytz.utc.localize(start_date) + td:
        requests.post(BOT_URL, json={
            "chatId": chatId,
            "text": "Data is sufficient for prediction"
        })
    else:
        requests.post(BOT_URL, json={
            "chatId": chatId,
            "text": f"The historical data is less than {warning}. Prediction may not be accurate"
        })

    # Exclude the latest data point
    df_stock = df_stock.iloc[:-1]

    # Calculate moving averages
    df_stock['ma_5'] = df_stock['Close'].rolling(window=5).mean()
    df_stock['ma_10'] = df_stock['Close'].rolling(window=10).mean()
    df_stock['ma_20'] = df_stock['Close'].rolling(window=20).mean()
    df_stock.dropna(inplace=True)

    # Scale the data
    scaler = MinMaxScaler()
    scaled_data = scaler.fit_transform(df_stock)

    # Prepare the data for the LSTM model
    x = []
    y = []
    window_size = 5
    for i in range(window_size, len(scaled_data)):
        x.append(scaled_data[i - window_size:i])
        y.append(scaled_data[i, 2])

    return df_stock, scaler, scaled_data, window_size, np.array(x), np.array(y)


def training(ticker, iso_start_date, iso_end_date, file_folder, x, y, intervalChoice, chatId):
    file_label = 'LSTM_Model' + '_' + intervalChoice
    filename = file_label + '_' + iso_start_date + '_' + iso_end_date + '.h5'
    file_path = file_folder + '/' + filename

    if os.path.exists(file_path):
        requests.post(BOT_URL, json={
            "chatId": chatId,
            "text": 'Taking trained model from the storage for ' + ticker
        })
        model = load_model(file_path)
        return model, None, None, None, True
    else:
        remove_old_file(file_folder, file_label)
        requests.post(BOT_URL, json={
            "chatId": chatId,
            "text": 'Training model for ' + ticker
        })

        # Split the data into training and testing sets
        x_train, x_test, y_train, y_test = train_test_split(x, y, test_size=0.2, shuffle=False)

        # Build the LSTM model
        model = Sequential()
        model.add(Bidirectional(LSTM(64, return_sequences=True), input_shape=(x_train.shape[1], x_train.shape[2])))
        model.add(Dropout(0.3))
        model.add(Bidirectional(LSTM(32)))
        model.add(Dropout(0.3))
        model.add(Dense(32, activation='relu'))
        model.add(Dense(1, activation='linear'))

        # Compile the model
        model.compile(optimizer='adam', loss='mse')

        # Define callbacks for early stopping and learning rate reduction
        early_stopping = EarlyStopping(monitor='val_loss', patience=10, restore_best_weights=True)
        reduce_lr = ReduceLROnPlateau(monitor='val_loss', factor=0.5, patience=5, min_lr=1e-6)

        # Train the model
        history = model.fit(
            x_train, y_train,
            epochs=20,
            batch_size=32,
            validation_data=(x_test, y_test),
            callbacks=[early_stopping, reduce_lr],
            verbose=1
        )
        model.save(file_folder + '/' + filename)
        requests.post(BOT_URL, json={
            "chatId": chatId,
            "text": 'Trained model saved for ' + ticker
        })
        return model, x_test, y_test, history, False


def evaluate(ticker, iso_start_date, iso_end_date, file_folder, model, df_stock, x_test, y_test, history, scaler, scaled_data, intervalChoice, chatId):
    y_pred = model.predict(x_test)

    y_test_reshaped = np.zeros((y_test.shape[0], 9))
    y_test_reshaped[:, 2] = y_test
    y_test_original = scaler.inverse_transform(y_test_reshaped)[:, 2]

    y_pred_reshaped = np.zeros((y_pred.shape[0], 9))
    y_pred_reshaped[:, 2] = y_pred.flatten()
    y_pred_original = scaler.inverse_transform(y_pred_reshaped)[:, 2]

    # Evaluate the model
    mse, mae, r2, mape, correlation = evaluate_model(y_test_original, y_pred_original)

    file_label = 'Metrics' + '_' + intervalChoice
    filename = file_label + '_' + iso_start_date + '_' + iso_end_date + '.txt'
    file_path = file_folder + '/' + filename
    remove_old_file(file_folder, file_label)

    # Open the file and write the metrics
    with open(file_path, 'w') as file:
        file.write(f'Mean Squared Error: {mse:.2f}\n')
        file.write(f'Mean Absolute Error: {mae:.2f}\n')
        file.write(f'R^2 Score: {r2:.2f}\n')
        file.write(f'Mean Absolute Percentage Error: {mape * 100:.2f}%\n')
        file.write(f'Correlation between actual and predicted prices: {correlation:.2f}\n')

    check_r2_score(r2, BOT_URL, RESOURCE_PATH, ADMIN_CHAT_ID, ticker, intervalChoice)

    loss_file_label = 'Loss' + '_' + intervalChoice
    loss_filename = loss_file_label + '_' + iso_start_date + '_' + iso_end_date + '.png'
    remove_old_file(file_folder, loss_file_label)

    # Plot the training and validation loss
    plt.figure(figsize=(16, 4))
    plt.plot(history.history['loss'], label='Training Loss')
    plt.plot(history.history['val_loss'], label='Validation Loss')
    plt.xlabel('Epochs')
    plt.ylabel('Loss')
    plt.legend()
    plt.savefig(file_folder + '/' + loss_filename)

    prices_file_label = 'Prices' + '_' + intervalChoice
    prices_filename = prices_file_label + '_' + iso_start_date + '_' + iso_end_date + '.png'
    remove_old_file(file_folder, prices_file_label)

    # Plot the actual vs predicted prices for the test set
    plt.figure(figsize=(16, 4))
    plt.plot(df_stock.index[-len(y_test):], y_test_original, color='blue', label='Actual Prices')
    plt.plot(df_stock.index[-len(y_test):], y_pred_original, color='red', linestyle='--', label='Predicted Prices')
    plt.xlabel('Date')
    plt.ylabel('Price')
    plt.legend()
    plt.savefig(file_folder + '/' + prices_filename)

    requests.post(BOT_URL, json={
        "chatId": chatId,
        "text": 'Evaluation metrics for ' + ticker + ' saved to storage'
    })


def predict(ticker, model, df_stock, scaler, intervalChoice, chatId):
    # Predict the closing price for the current day
    current_interval_prediction = round(predict_current_interval(df_stock, model, df_stock.values, scaler, window_size=5), 2)

    # Get the actual closing price for the current day
    actual_price = get_current_interval_price_from_df(df_stock, intervalChoice)

    response = f'Here\'s the prediction for ticker {ticker}:\n'

    # Predict the closing price for the next day
    next_interval_prediction = round(predict_next_interval(df_stock, current_interval_prediction, model, df_stock.values, scaler), 2)
    response += f'Predicted closing price for the next {intervalChoice}: {next_interval_prediction}\n'

    requests.post(BOT_URL, json={
        "chatId": chatId,
        "text": response
    })

    generate_recommendation(df_stock, actual_price, next_interval_prediction, BOT_URL, RESOURCE_PATH, chatId)

    disclaimer = ('Disclaimer: This information does not constitute individual investment advice.\n'
                  'PJSC Nostradamus is not responsible for possible losses of the Investor in the event that the Investor decides to carry out a trading operation (transaction) based on recommendations from the chatbot.')
    requests.post(BOT_URL, json={
        "chatId": chatId,
        "text": disclaimer
    })


# Function to get available tickers
def get_available_tickers():
    with Client(API_TOKEN) as client:
        instruments = client.instruments.shares()
        tickers = [instrument.ticker for instrument in instruments.instruments]
    return tickers


# Function to get FIGI (Financial Instrument Global Identifier) for a given ticker
def get_figi(ticker):
    with Client(API_TOKEN) as client:
        instruments = client.instruments.shares()
        for instrument in instruments.instruments:
            if instrument.ticker == ticker:
                return instrument.figi
    raise ValueError("Ticker not found")


# Function to retrieve historical price data
def get_historical_data(figi, start_date, end_date, interval):
    data = []
    with Client(API_TOKEN) as client:
        candles = list(client.get_all_candles(
            figi=figi,
            from_=start_date,
            to=end_date,
            interval=interval
        ))
        for candle in candles:
            data.append({
                'DateTime': candle.time,
                'Open': candle.open.units + candle.open.nano / 1e9,
                'Close': candle.close.units + candle.close.nano / 1e9,
                'High': candle.high.units + candle.high.nano / 1e9,
                'Low': candle.low.units + candle.low.nano / 1e9,
                'Volume': candle.volume,
            })
    return pd.DataFrame(data)


# Function to evaluate the model
def evaluate_model(true, predicted):
    mse = mean_squared_error(true, predicted)
    mae = mean_absolute_error(true, predicted)
    r2 = r2_score(true, predicted)
    mape = mean_absolute_percentage_error(true, predicted)
    correlation = pearsonr(np.array(true).flatten(), np.array(predicted).flatten())[0]
    return mse, mae, r2, mape, correlation


# Function to predict the closing price for the current interval
def predict_current_interval(df_stock, model, data, scaler, window_size=5):
    last_window = data[-window_size:]
    last_window_df = pd.DataFrame(last_window, columns=df_stock.columns)
    last_window_scaled = scaler.transform(last_window_df)
    last_window_scaled = np.expand_dims(last_window_scaled, axis=0)
    predicted_scaled = model.predict(last_window_scaled)
    predicted = scaler.inverse_transform(np.concatenate((np.zeros((predicted_scaled.shape[0], data.shape[1]-1)), predicted_scaled), axis=1))[:, -1]
    return predicted[0]


# Function to predict the closing price for the next interval
def predict_next_interval(df_stock, current_interval_prediction, model, data, scaler, window_size=5):
    # Add the current interval's predicted price to the data
    last_window = data[-window_size:].copy()
    last_window[-1][-1] = current_interval_prediction
    last_window_df = pd.DataFrame(last_window, columns=df_stock.columns)
    last_window_scaled = scaler.transform(last_window_df)
    last_window_scaled = np.expand_dims(last_window_scaled, axis=0)
    predicted_scaled = model.predict(last_window_scaled)
    predicted = scaler.inverse_transform(np.concatenate((np.zeros((predicted_scaled.shape[0], data.shape[1]-1)), predicted_scaled), axis=1))[:, -1]
    return predicted[0]


# Function to get the actual closing price for the current interval from the dataframe
def get_current_interval_price_from_df(df, interval_choice):
    if interval_choice == 'hour':
        current_time = df.index[-1].floor('H')
    elif interval_choice == 'day':
        current_time = df.index[-1].floor('D')
    else:
        raise ValueError("Invalid interval choice")

    current_price = df.loc[current_time]['Close']
    return current_price


def remove_old_file(file_folder, file_label):
    if os.path.exists(file_folder):
        for fname in os.listdir(file_folder):
            if fname.startswith(file_label):
                fpath = os.path.join(file_folder, fname)
                os.remove(fpath)
                break
