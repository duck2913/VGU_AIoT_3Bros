import sys,os
from Adafruit_IO import MQTTClient
from dotenv import load_dotenv
from simple_ai import *
import time
from physical import *

load_dotenv()

AIO_FEED_ID = ['actuator1', 'actuator2', 'sensor1', 'sensor2', 'sensor3', 'visiondetection']
AIO_USERNAME = "3Bros"
AIO_KEY = os.getenv('key')
print("🚀 ~ AIO_KEY", AIO_KEY)


def connected(client):
    for topic in AIO_FEED_ID:
        client.subscribe(topic)
        print('ket noi')


def subscribe(client, userdata, mid, granted_qos):
    print("Subscribe thanh cong ...")


def disconnected(client):
    print("Ngat ket noi ...")
    sys.exit(1)


def message(client, feed_id, payload):
    print("Nhan du lieu: " + payload + " tu feed: " + feed_id)
    setDevice1(payload)

client = MQTTClient(AIO_USERNAME, AIO_KEY)
client.on_connect = connected
client.on_disconnect = disconnected
client.on_message = message
client.on_subscribe = subscribe
client.connect()
client.loop_background()

while True:
    # image_capture()
    # ai_result = image_detector()
    # client.publish('visiondetection', ai_result)
    # setDevice1(True)
    # setDevice2(True)
    # print("on")
    # time.sleep(10)
    # setDevice1(False)
    # setDevice2(False)
    # print('off')

    # temperature = readTemperature() / 100
    # print(temperature)
    # client.publish('sensor1', temperature)
    # moisture = readMoisture() / 100
    # print(moisture)
    # client.publish('sensor2', moisture)
    # time.sleep(5)
    
    pass
