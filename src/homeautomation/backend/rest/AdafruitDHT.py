#!/usr/bin/python
import sys

import Adafruit_DHT

# Parse command line parameters.
if len(sys.argv) == 2 and (sys.argv[1] == 't' or sys.argv[1] == 'h'):
    method = sys.argv[1]
else:
    print('Usage: sudo ./Adafruit_DHT.py [t|h]')
    print('Example: sudo ./Adafruit_DHT.py t - Read readTemperature')
    sys.exit(1)

# Try to grab a sensor reading.  Use the read_retry method which will retry up
# to 15 times to get a sensor reading (waiting 2 seconds between each retry).
humidity, temperature = Adafruit_DHT.read_retry(Adafruit_DHT.DHT22, 4)

if humidity is not None and sys.argv[1] == 'h':
     print('{0:0.1f}'.format(humidity))
elif temperature is not None and sys.argv[1] == 't':
     print('{0:0.1f}'.format(temperature))
else:
    print('Failed to get reading. Try again!')
    sys.exit(1)
