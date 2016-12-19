CNT5106C Chat Server and Client

Project Members:
Ashutosh Bondre
Ninad Mundalik

This project covers the following functionalities

  (1) Broadcast: Any client is able to send a text to the server, which will relay it to
      all other clients for display

  (2) Broadcast: Any client is able to send a file of any type to the group via the
      server.

  (3) Unicast: Any client is able to send a private message to a specific other client
      via the server.

  (4) Unicast: Any client is able to send a private file of any type to a specific other
      client via the server.

  (5) Blockcast: Any client is able to send a text to all other clients except for one
      via the sever.

  (6) Blockcast: Any client is able to send a file to all other clients except for one
      via the sever.


Usage:
  Start the server
  java server

  Start the Client
  java client

  After starting the client the client will be asked to enter his name. 
  All clients will be notified of the newly joined client.
  The clients can then start connecting with each other using the following commands.
  A new client can be added at any time.

Sample message formats:

Broadcast:
  broadcast message "sample string"
  broadcast file "path_to_file"

Unicast:
  unicast message client_name "sample string"
  unicast file client_name "path_to_file"
  The message or file will be sent to the client whose name is client_name

Blockcast:
  blockcast message client_name "sample string"
  blockcast file client_name "path_to_file"
  The message or file will be sent to all the clients except to the one whose name is client_name

Implementation details:
  On the server we start a new thread for each client, we assign clientName to each thread after the user enters it.
  This is used as a user_name by all clients who wish to communicate with each other.
  Multiple clients accessing the chat application simultaneously is handled using thread synchronization.
  
  Broadcast:
    Incoming command (message or file) is sent to all the threads.
  Unicast:
    Incoming command (message or file) is sent to only the matching client_name in the command.
  Blockcast:
    Incoming command (message or file) is sent to all threads except the matching client_name in the command.

Quiting
  /quit
  Enter the above command to quit the system
  All clients are notified of the client that is leaving
  We can also check on the server if a client has connected or left
