from flask import Flask, request, jsonify

app = Flask(__name__)


@app.route('/predict', methods=['POST'])
def predict():
    data = request.get_json(force=True)
    response = data['ticker']
    return jsonify({
        "response": response
    })


if __name__ == '__main__':
    app.run(host='0.0.0.0')
