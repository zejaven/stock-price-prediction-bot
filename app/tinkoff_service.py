import configparser
import os
from datetime import datetime, timedelta

import numpy as np
import pandas as pd
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
from tensorflow.keras.optimizers import Adam
from tinkoff.invest import Client, CandleInterval

# Read the API token and Bot url from the configuration files
config = configparser.ConfigParser()
config.read(['config.ini', 'hidden.ini'])

API_TOKEN = config['DEFAULT']['API_TOKEN']
BOT_URL = os.getenv('BOT_URL', config['DEFAULT']['BOT_URL'])
RESOURCE_PATH = os.getenv('RESOURCE_PATH', config['DEFAULT']['RESOURCE_PATH'])


def make_prediction(ticker, chatId):
    # Define the start and end dates for historical data retrieval
    start_date = datetime.now() - timedelta(days=365 * 5)
    end_date = datetime.now()
    iso_start_date = start_date.date().isoformat()
    iso_end_date = end_date.date().isoformat()

    file_folder, filename, figi = get_data(ticker, start_date, end_date, iso_start_date, iso_end_date, chatId)
    df_stock, scaler, scaled_data, window_size, x, y = data_preprocessing(ticker, file_folder, filename, chatId)
    model, x_test, y_test, history, evaluated = training(ticker, iso_start_date, iso_end_date, file_folder, x, y, chatId)
    if not evaluated:
        evaluate(ticker, iso_start_date, iso_end_date, file_folder, model, df_stock, x_test, y_test, history, scaler, scaled_data, chatId)
    predict(ticker, figi, model, df_stock, scaler, chatId)


def get_data(ticker, start_date, end_date, iso_start_date, iso_end_date, chatId):
    # Get available tickers
    available_tickers = get_available_tickers()

    # Check if the ticker is available
    if ticker not in available_tickers:
        raise ValueError("Ticker not found. Please check the ticker symbol and try again.")

    # Get FIGI for the selected ticker
    figi = get_figi(ticker)

    file_label = 'Dataframe'
    filename = file_label + '_' + iso_start_date + '_' + iso_end_date + '.csv'
    file_folder = RESOURCE_PATH + '/' + ticker
    file_path = file_folder + '/' + filename
    if os.path.exists(file_path):
        requests.post(BOT_URL, json={
            "chatId": chatId,
            "text": 'Taking historical data from the storage for ' + ticker + ': ' + filename
        })
        return file_folder, filename, figi
    else:
        if os.path.exists(file_folder):
            for fname in os.listdir(file_folder):
                if fname.startswith(file_label):
                    fpath = os.path.join(file_folder, fname)
                    os.remove(fpath)
                    break

        # Retrieve historical price data for the selected ticker
        data = []
        with Client(API_TOKEN) as client:
            candles = list(client.get_all_candles(
                figi=figi,
                from_=start_date,
                to=end_date,
                interval=CandleInterval.CANDLE_INTERVAL_DAY
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

        # Create a DataFrame from the retrieved data
        df_stock = pd.DataFrame(data)
        os.makedirs(os.path.dirname(file_path), exist_ok=True)
        df_stock.to_csv(file_path)

        requests.post(BOT_URL, json={
            "chatId": chatId,
            "text": 'Saved historical data for ' + ticker + ': ' + filename
        })

        return file_folder, filename, figi


def data_preprocessing(ticker, file_folder, filename, chatId):
    requests.post(BOT_URL, json={
        "chatId": chatId,
        "text": 'Preprocessing data for ' + ticker
    })
    df_stock = pd.read_csv(file_folder + '/' + filename)
    df_stock['DateTime'] = pd.to_datetime(df_stock['DateTime'])
    df_stock.set_index('DateTime', inplace=True)

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


def training(ticker, iso_start_date, iso_end_date, file_folder, x, y, chatId):
    file_label = 'LSTM_Model'
    filename = file_label + '_' + iso_start_date + '_' + iso_end_date + '.h5'
    file_path = file_folder + '/' + filename

    if os.path.exists(file_path):
        requests.post(BOT_URL, json={
            "chatId": chatId,
            "text": 'Taking trained model from the storage for ' + ticker + ': ' + filename
        })
        model = load_model(file_path)
        return model, None, None, None, True
    else:
        if os.path.exists(file_folder):
            for fname in os.listdir(file_folder):
                if fname.startswith(file_label):
                    fpath = os.path.join(file_folder, fname)
                    os.remove(fpath)
                    break
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


def evaluate(ticker, iso_start_date, iso_end_date, file_folder, model, df_stock, x_test, y_test, history, scaler, scaled_data, chatId):
    # Evaluate the model
    mse, mae, r2, mape, y_pred = evaluate_model(model, x_test, y_test)

    # Ensure y_test and y_pred are 1-dimensional arrays and of the same dtype
    y_test = np.array(y_test).flatten()
    y_pred = np.array(y_pred).flatten()

    # Calculate correlation
    correlation, _ = pearsonr(y_test, y_pred)

    file_label = 'Metrics'
    filename = file_label + '_' + iso_start_date + '_' + iso_end_date + '.txt'
    file_path = file_folder + '/' + filename
    if os.path.exists(file_folder):
        for fname in os.listdir(file_folder):
            if fname.startswith(file_label):
                fpath = os.path.join(file_folder, fname)
                os.remove(fpath)
                break

    # Open the file and write the metrics
    with open(file_path, 'w') as file:
        file.write(f'Mean Squared Error: {mse:.2f}\n')
        file.write(f'Mean Absolute Error: {mae:.2f}\n')
        file.write(f'R^2 Score: {r2:.2f}\n')
        file.write(f'Mean Absolute Percentage Error: {mape * 100:.2f}%\n')
        file.write(f'Correlation between actual and predicted prices: {correlation:.2f}\n')

    loss_file_label = 'Loss'
    loss_filename = loss_file_label + '_' + iso_start_date + '_' + iso_end_date + '.png'
    # Plot the training and validation loss
    plt.figure(figsize=(16, 4))
    plt.plot(history.history['loss'], label='Training Loss')
    plt.plot(history.history['val_loss'], label='Validation Loss')
    plt.xlabel('Epochs')
    plt.ylabel('Loss')
    plt.legend()
    plt.savefig(file_folder + '/' + loss_filename)

    prices_file_label = 'Prices'
    prices_filename = prices_file_label + '_' + iso_start_date + '_' + iso_end_date + '.png'
    # Plot the actual vs predicted prices for the test set
    plt.figure(figsize=(16, 4))
    plt.plot(df_stock.index[-len(y_test):], scaler.inverse_transform(np.concatenate((np.zeros((y_test.shape[0], scaled_data.shape[1]-1)), y_test.reshape(-1, 1)), axis=1))[:, -1], color='blue', label='Actual Prices')
    plt.plot(df_stock.index[-len(y_test):], scaler.inverse_transform(np.concatenate((np.zeros((y_pred.shape[0], scaled_data.shape[1]-1)), y_pred.reshape(-1, 1)), axis=1))[:, -1], color='red', linestyle='--', label='Predicted Prices')
    plt.xlabel('Date')
    plt.ylabel('Price')
    plt.legend()
    plt.savefig(file_folder + '/' + prices_filename)

    requests.post(BOT_URL, json={
        "chatId": chatId,
        "text": 'Evaluation metrics for ' + ticker + ' saved to folder: ' + file_folder
    })


def predict(ticker, figi, model, df_stock, scaler, chatId):
    # Predict the closing price for the current day
    current_day_prediction = round(predict_current_day(df_stock, model, df_stock.values, scaler, window_size=5), 2)

    # Get the actual closing price for the current day
    actual_price = get_current_day_price(figi)

    response = f'Here\'s the prediction for ticker {ticker}:\n'
    response += f'Predicted closing price for the current day: {current_day_prediction}\n'

    # Calculate and print prediction accuracy for the current day
    if actual_price:
        actual_price = round(actual_price, 2)
        response += f'Actual closing price for the current day: {actual_price}\n'
        accuracy_current_day = round(100 - abs((actual_price - current_day_prediction) / actual_price) * 100, 2)
        response += f'Prediction accuracy for the current day: {accuracy_current_day}%\n'
    else:
        response += 'Failed to retrieve the actual closing price for the current day.\n'

    # Predict the closing price for the next day
    next_day_prediction = round(predict_next_day(df_stock, current_day_prediction, model, df_stock.values, scaler), 2)
    response += f'Predicted closing price for the next day: {next_day_prediction}\n'

    requests.post(BOT_URL, json={
        "chatId": chatId,
        "text": response
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


# Function to evaluate the model
def evaluate_model(model, x_test, y_test):
    y_pred = model.predict(x_test)
    mse = mean_squared_error(y_test, y_pred)
    mae = mean_absolute_error(y_test, y_pred)
    r2 = r2_score(y_test, y_pred)
    mape = mean_absolute_percentage_error(y_test, y_pred)
    return mse, mae, r2, mape, y_pred


# Function to predict the closing price for the current day
def predict_current_day(df_stock, model, data, scaler, window_size=5):
    last_window = data[-window_size:]
    last_window_df = pd.DataFrame(last_window, columns=df_stock.columns)
    last_window_scaled = scaler.transform(last_window_df)
    last_window_scaled = np.expand_dims(last_window_scaled, axis=0)
    predicted_scaled = model.predict(last_window_scaled)
    predicted = scaler.inverse_transform(np.concatenate((np.zeros((predicted_scaled.shape[0], data.shape[1]-1)), predicted_scaled), axis=1))[:, -1]
    return predicted[0]


# Function to predict the closing price for the next day
def predict_next_day(df_stock, current_day_prediction, model, data, scaler, window_size=5):
    # Add the current day's predicted price to the data
    last_window = data[-window_size:].copy()
    last_window[-1][-1] = current_day_prediction
    last_window_df = pd.DataFrame(last_window, columns=df_stock.columns)
    last_window_scaled = scaler.transform(last_window_df)
    last_window_scaled = np.expand_dims(last_window_scaled, axis=0)
    predicted_scaled = model.predict(last_window_scaled)
    predicted = scaler.inverse_transform(np.concatenate((np.zeros((predicted_scaled.shape[0], data.shape[1]-1)), predicted_scaled), axis=1))[:, -1]
    return predicted[0]


# Function to get the actual closing price for the current day
def get_current_day_price(figi):
    today = datetime.now().date()
    tomorrow = today + timedelta(days=1)
    with Client(API_TOKEN) as client:
        candles = list(client.get_all_candles(
            figi=figi,
            from_=datetime.combine(today, datetime.min.time()),  # start of the day
            to=datetime.combine(tomorrow, datetime.min.time()),  # start of the next day
            interval=CandleInterval.CANDLE_INTERVAL_DAY
        ))
        if candles:
            return candles[-1].close.units + candles[-1].close.nano / 1e9
    return None
