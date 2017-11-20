import socket
import sys
import json

# Create a TCP/IP socket
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Bind the socket to the port
server_address = ('localhost', 10000)
#print(sys.stderr, 'starting up on %s port %s' %server_address)
print('starting up on %s port %s' %server_address)
sock.bind(server_address)

# Listen for incoming connections
sock.listen(1)

while True:
    # Wait for a connection
    #print(sys.stderr, 'waiting for a connection')
    print('waiting for a connection')
    connection, client_address = sock.accept()

    try:
        #print(sys.stderr, 'connection from', client_address)
        print('connection from', client_address)

        # Receive the data in small chunks and retransmit it
        data = json.dumps(json.loads(connection.recv(10000).decode()), indent=4)
        #print(sys.stderr, 'received "%s"' %data.decode())
        #print('received "%s"' % data)
        print(data)
            
    finally:
        # Clean up the connection
        connection.close()
