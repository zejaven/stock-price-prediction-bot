from concurrent.futures import ThreadPoolExecutor
from flask import Flask, request, jsonify
from tinkoff_service import make_prediction

app = Flask(__name__)
executor = ThreadPoolExecutor(2)


@app.route('/predict', methods=['POST'])
def predict():
    data = request.get_json(force=True)
    executor.submit(make_prediction, data['ticker'], data['intervalChoice'], data['chatId'])
    return jsonify({"message": "Starting prediction process for ticker " + data['ticker'] + "..."})


if __name__ == '__main__':
    app.run(host='0.0.0.0')
