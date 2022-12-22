import sys,os
from Adafruit_IO import MQTTClient
from dotenv import load_dotenv
from simple_ai import *
import time
from physical import *

load_dotenv()

# Set Adafruit IO information
AIO_FEED_ID = ['actuator1', 'actuator2', 'sensor1', 'sensor2', 'sensor3', 'visiondetection']
AIO_USERNAME = "3Bros"
AIO_KEY = os.getenv('key')
print("🚀 ~ AIO_KEY", AIO_KEY)

# Define callback functions which will be called when connecting.
def connected(client):
    for topic in AIO_FEED_ID:
        client.subscribe(topic)
        print('ket noi')

# Subscribe to all feeds.
def subscribe(client, userdata, mid, granted_qos):
    print("Subscribe thanh cong ...")

# Define callback functions which will be called when disconnecting.
def disconnected(client):
    print("Ngat ket noi ...")
    sys.exit(1)

# Receive messages.
def message(client, feed_id, payload):
    print("Nhan du lieu: " + payload + " tu feed: " + feed_id)
    setDevice1(payload)

# Create an MQTT client instance.
client = MQTTClient(AIO_USERNAME, AIO_KEY)
client.on_connect = connected
client.on_disconnect = disconnected
client.on_message = message
client.on_subscribe = subscribe
client.connect()
client.loop_background()

# Publish a new value every 5 seconds.
while True:
    # publish image detection onto Adafruit IO
    image_capture()
    ai_result = image_detector()
    client.publish('visiondetection', ai_result)

    # publish sensor data onto Adafruit IO
    temperature = readTemperature() / 10
    client.publish('sensor1', temperature)
    moisture = readMoisture() / 10
    client.publish('sensor2', moisture)
    time.sleep(5)
    pass
