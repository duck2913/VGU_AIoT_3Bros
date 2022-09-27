import sys,os
from Adafruit_IO import MQTTClient
from dotenv import load_dotenv
load_dotenv()

AIO_FEED_ID = ['actuator1', 'actuator2']
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


client = MQTTClient(AIO_USERNAME, AIO_KEY)
client.on_connect = connected
client.on_disconnect = disconnected
client.on_message = message
client.on_subscribe = subscribe
client.connect()
client.loop_background()

while True:
    pass
