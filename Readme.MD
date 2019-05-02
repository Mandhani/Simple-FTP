This Project has been implemented in Java and setup.sh file has been provided in the package. 
Run this script using the command: ./setup.sh
This script will create the class files. This has been done to avoid any Java version issues.
This project has been implemented and tested on Java JDK 1.8 in the VCL instance for CSC573+jdk image. This is available on VCL and we strongly recommend using this image as it has been tested on that environment.
We recommend using two vcl VMs to test this, however you can also test on a single instance.
Steps to follow:
1. Extract the folder and run the setup.sh file: ./setup.sh
2. Start Server: (On server VM)
	- cd src/
	- java server/Server <port> <file name> <loss_probability>
3. Start Client: (On client VM)
	- cd src/
	- java client/Client <host> <port> <fine name> <Window N> <MSS>

The above steps will start the file transfer of the specified file from client to server using the Go-Back-N ARQ scheme implemented as part of this project.
If you find any issues with java versioning during any part of the process, you can generate the Java classes simply by using this command from src directory: javac <class directory>/<class name>.java 
If you see this error: "Error: Could not find or load main class server.Server", that means you're probably not in the src directory during execution. It is required to be executed from this directory.
In case of any issues, please contact mkushal@ncsu.edu.
