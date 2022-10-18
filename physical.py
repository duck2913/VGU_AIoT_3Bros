from pickle import TRUE
import serial.tools.list_ports
import time

def getPort():
    ports = serial.tools.list_ports.comports()
    N = len(ports)
    commPort = "None"
    for i in range(0, N):
        port = ports[i]
        strPort = str(port)
        if "USB Serial" in strPort: # or "FT232R" in strPort: for MacOS
            splitPort = strPort.split(" ")
            commPort = (splitPort[0])
    return commPort

portName = getPort()
if portName != "None":
    ser = serial.Serial(port=portName, baudrate=9600)

print(ser)

relay1_ON = [0, 6, 0, 0, 0, 255, 200, 91]
relay1_OFF = [0, 6, 0, 0, 0, 0, 136, 27]

relay2_ON = [15, 6, 0, 0, 0, 255, 200, 164]
relay2_OFF = [15, 6, 0, 0, 0, 0, 136, 228]

def setDevice1(state):
    if state == True or state == '1':
        ser.write(relay1_ON)
    else:
        ser.write(relay1_OFF)

def setDevice2(state):
    if state == True or state == '1':
        ser.write(relay2_ON)
    else:
        ser.write(relay2_OFF)

def serial_read_data(ser):    
    bytesToRead = ser.inWaiting()    
    if bytesToRead > 0:        
        out = ser.read(bytesToRead)        
        data_array = [b for b in out]        
        print(data_array)        
        if len(data_array) >= 7:            
            array_size = len(data_array)            
            value = data_array[array_size - 4] * 256 + data_array[array_size - 3]            
            return value        
        else:            
            return -1    
    return 0
       
soil_temperature = [1, 3, 0, 6, 0, 1, 100, 11]
def readTemperature():
    serial_read_data(ser)
    ser.write(soil_temperature)
    time.sleep(1)
    return serial_read_data(ser)

soil_moisture = [1, 3, 0, 7, 0, 1, 53, 203]
def readMoisture():
    serial_read_data(ser)
    ser.write(soil_moisture)
    time.sleep(1)
    return serial_read_data(ser)

while True:
    setDevice1(True)
    setDevice2(True)
    print("on")
    time.sleep(10)
    setDevice1(False)
    setDevice2(False)
    print('off')
    time.sleep(10)
