import socket
import sys
import demjson

# Create a TCP/IP socket
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Connect the socket to the port where the server is listening
server_address = ('localhost', 9090)
print(sys.stderr, 'connecting to %s port %s' % server_address)
sock.connect(server_address)

try:
    data = {"barcode": "GI31509009755000","license": "BG 0805 NV","date": "2017-11-13 16:40:38"}
    json = demjson.encode(data)
    print(json)
    
    # Send data
    #message = 'Hello World'
    print(sys.stderr, 'sending "%s"' % json)
    sock.sendall(json.encode())

    # Look for the response
##    amount_received = 0
##    amount_expected = len(message)
##    
##    while amount_received < amount_expected:
##        data = sock.recv(16)
##        amount_received += len(data)
##        print(sys.stderr, 'received "%s"' % data)

finally:
    print(sys.stderr, 'closing socket')
    sock.close()
