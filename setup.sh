#!/bin/bash
cd src
echo "Creating java classes..."
javac headers/AckHeader.java
javac headers/DataHeader.java
javac server/Server.java
javac client/Buffer.java
javac client/Client.java
echo "Java classes should be created"
echo "Please follow these steps next:"
echo -e "1. Start the server first. Follow these steps to start the server:\n
Commands:\n
\t> cd src/\n
\t> java server/Server <port> <file name> <loss_probability>\n
\tThis will start the server. Now start another terminal on another host and procees to step 2.\n
2. Start the client after server is running. Follow the below steps:\n
Commands:\n
\t> cd src/\n
\t> java client/Client <host> <port> <fine name> <Window N> <MSS>\n
\tThis will start the client and Go-Back-N ARQ implementation.\n"
