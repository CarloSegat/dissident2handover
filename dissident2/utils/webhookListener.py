"""
Webhook Listener for ACA-Py Controller

Author: haidinhtuan@gmail.com

This script sets up a simple Flask server to listen to webhook events from an ACA-Py agent.
It prints the topic and body of incoming webhook events to the console.

How to Run:
Run the script with a command-line argument specifying the port number.
Example: python webhookListener.py 5555

Usage:
- The Flask server will start and listen on the specified port.
- When a webhook event is received at the endpoint '/webhook/topic/<topic>/',
  it will print the topic and the body of the event to the console.
"""

from flask import Flask, request, jsonify
import sys

app = Flask(__name__)

@app.route('/webhook/topic/<topic>/', methods=['POST'])
def webhook(topic):
    data = request.json
    print("========================================")
    print(f"Topic: {topic}")
    print(f"Body: {data}")
    print("========================================")
    return jsonify({"message": "Received"}), 200

if __name__ == '__main__':
    if len(sys.argv) != 2:
        print("Usage: python script.py <port>")
        sys.exit(1)

    port = int(sys.argv[1])
    app.run(debug=True, host='0.0.0.0', port=port)


