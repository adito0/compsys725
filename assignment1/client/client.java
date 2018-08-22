/**
 * author: Ira Sukimin (isuk218@aucklanduni.ac.nz)
 * COMPSYS725 - Computer Networks and Distributed Applications
 **/

import java.io.*; 
import java.net.*;
import java.nio.file.*;
import java.nio.file.attribute.*;

class clientTCP { 
   
	//variable declaration
	private static String command = "";
	private static String currentUser = "";
	private static String cmd = "";
	private static String errorMessage;
	private static String filename = "";
	private static String host = "localhost";
	private static int port = 1500;
	private static long fileSize = 0;
	//data and connection related variable declaration
	private static BufferedReader inFromUser;
	private static Socket clientSocket;
	private static DataOutputStream outToServer;
	private static BufferedReader inFromServer;
	private static DataOutputStream fileOutToServer; 
	private static BufferedInputStream fileInFromClient;
	//file related variable declaration
	private static final File defaultDirectory = FileSystems.getDefault().getPath("").toFile().getAbsoluteFile();
	private static File userFolder;
	private File currentDirectory = defaultDirectory;
	public static File file;

	/** 
	*	descripton	: 	attemptConnection() sets up the buffers needed for communication with the server
	*					and initiates connection through specified host and port
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	public boolean attemptConnection() throws Exception {

		inFromUser = new BufferedReader(new InputStreamReader(System.in)); 
		try {
			clientSocket = new Socket(host, port); 
			outToServer = new DataOutputStream(clientSocket.getOutputStream()); 
			inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			fileOutToServer = new DataOutputStream(clientSocket.getOutputStream());
			fileInFromClient = new BufferedInputStream(clientSocket.getInputStream());
			processServerResponse();
			if (errorMessage.charAt(0) == '+') {
				System.out.println("connection to server is established");
				return true;
			}			
		}
		catch (SocketException e) {
			System.out.println("could not connect to server");
			System.exit(0);
			clientSocket.close();
			return false;
		}

		return false;
	}
	
	/** 
	*	descripton	: 	readServerResponse() reads and concatenates the errorMessage sent by the server character 
	*					by character until a <null> character is found.
	*	args		: 	NONE
	*	returns		:	line - the errorMessage sent by the server without the null character
	**/	
	public String readServerResponse() throws Exception {
		String line = "";
		int character = 0;
		//loop through char by char until a terminating character is found
		while (true) {
			try {
				character = inFromServer.read();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
			if (character == 0) {
				break;
			}
			//convert and concatenate the characters into a string
			line = line.concat(Character.toString((char)character));
		}
		return line;
	}

	/** 
	*	descripton	: 	checkValidCommand() reads the command typed in by the user and checks if its of valid
	*					command format and sends them to the server. Preprocessing of files are also done
	8					for the SEND and STOR commands to prepare the client for file sending/retrieving
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	public void checkValidCommand() throws Exception {
		try {
			//prompts user for a commmand
			System.out.println("enter command: ");		
			command = inFromUser.readLine();
			//checks if the user has typed in anything
			if (command != null) {
				try {
					cmd = command.substring(0,4);
				}
				catch (StringIndexOutOfBoundsException e) {
					System.out.println("invalid command entered. pls try again"); 
					checkValidCommand();
				}
				//if user sends a SEND command, prompt user to enter the filename for the incoming file from server
				if (cmd.equalsIgnoreCase("send")) {
					outToServer.writeBytes(command + "\0");				
					System.out.println("save file as: ");
					filename = inFromUser.readLine();
					File file = new File(userFolder.toString() + "/" + filename);
					receiveFile(file,fileSize,true);
					checkValidCommand();
				}
				/**	
				*	if user sends a STOR command, prompt user to enter the filename of the file intended for the server
				*	as the file size will be fetched and printed to the console in preparation for the SIZE command
				*	and the file will be prepared for sendFile()
				**/
				else if (cmd.equalsIgnoreCase("stor")) {
					outToServer.writeBytes(command + "\0");
					System.out.println("enter filename to be sent to the server: ");
					cmd = inFromUser.readLine();
					file = new File(currentDirectory.toString() + "/" + cmd);
					try {
						FileInputStream fis = new FileInputStream(file);
						BufferedInputStream bufferedInStream = new BufferedInputStream(new FileInputStream(file));
						System.out.println("SIZE : " + fis.available());
						fis.close();
					}
					catch (FileNotFoundException e) {
						System.out.println("size of file could not be retrieved bcs there is no such file");
					}
				}
				/**	
				*	the username entered will be stored when the user sends a USER command to make sure the files
				*	retrieved from the server via the RETR command is stored in the correct userFolder within the
				*	client directory
				**/				
				else if (cmd.equalsIgnoreCase("user")) {
					outToServer.writeBytes(command + "\0");
					String[] parts = command.split("\\ ",2);
					currentUser = parts[1];				
					userFolder = FileSystems.getDefault().getPath(currentUser).toFile().getAbsoluteFile();
				}
				else {
					outToServer.writeBytes(command + "\0");
				}
			}
			else {
				System.out.println("invalid command entered. pls try again"); 
				checkValidCommand();
			}	
		}
		catch (ConnectException e) {
			System.out.println("server is offline");
			System.exit(0);
		}
		catch (SocketException e) {
			System.out.println("server went offline");	
			System.exit(0);
		}			
	}			

	/** 
	*	descripton	: 	processServerResponse() reads the errorMessage sent by the server and processes
	*					the file size sent over by the server in return for the RETR command sent
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	public void processServerResponse() throws Exception {
		try {
			errorMessage = readServerResponse();
			System.out.println("from server: " + errorMessage); 
			//initiate file sharing when server sends the appropriate errorMessage after a STOR command
			if (errorMessage.equalsIgnoreCase("+ok, waiting for file")) {
				sendFile(file);
				processServerResponse();
			}
			else if ((errorMessage.charAt(0) == '!') || (errorMessage.charAt(0) == '+') || (errorMessage.charAt(0) == '-')) {
				//do nothing
			}
			else {
				//if server sends over the file size in return for the RETR command sent, it is saved to fileSize
				fileSize = Long.parseLong(errorMessage.replaceAll("\\s",""));	
			}
		}
		catch (ConnectException e) {
			System.out.println("server is offline");
			System.exit(0);
		}
		catch (SocketException e) {
			System.out.println("server went offline");	
			System.exit(0);
		}		
	}

	/** 
	*	descripton	: 	sendFile() sends file from the current directory to the server byte by byte when the STOR
	*					command is sent to the server
	*	args		: 	NONE
	*	returns		:	boolean true if the file was sent succesfully
	**/		
	public void sendFile(File file) throws Exception {
		byte[] bytes = new byte[(int) file.length()];
		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bufferedInStream = new BufferedInputStream(new FileInputStream(file));
			int content = 0;
			//read the file and write to buffer byte by byte
			while ((content = bufferedInStream.read(bytes)) >= 0) {
				fileOutToServer.write(bytes, 0, content);
			}
			bufferedInStream.close();
			fis.close();
			fileOutToServer.flush();
			System.out.println("file has been sent to server");
		} 
		catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException when trying to sendFile");
		} 
		catch (IOException e) {
			System.out.println("IOException when trying to sendFile");
		}	
	}		

	/** 
	*	descripton	: 	receiveFile() receives file from the server byte by byte and saves it to the currentUser's folder
	*					when the RETR command is sent
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	public void receiveFile(File file, long fileSize, boolean overwrite) throws Exception  {
		try {
			FileOutputStream fileOutStream = new FileOutputStream(file, overwrite);
			BufferedOutputStream bufferedOutStream = new BufferedOutputStream(fileOutStream);
			//read the file and write to buffer byte by byte
			for (int i = 0; i < fileSize; i++) {
				bufferedOutStream.write(fileInFromClient.read());
			}
			bufferedOutStream.close();
			fileOutStream.close();
			System.out.println(filename + " has been saved to /" + currentUser);
		}
		catch (ConnectException e) {
			System.out.println("server is offline");
			System.exit(0);
		}
		catch (SocketException e) {
			System.out.println("server went offline");
			System.exit(0);
		}		
	}
	
    public static void main(String argv[]) throws Exception 
    { 
		//create new instance of TCPServer
		clientTCP server = new clientTCP();	
		try {
			if (server.attemptConnection()) {	
				while(true) {
					server.checkValidCommand();
					server.processServerResponse();
				}
			}
		}
		catch (ConnectException e) {
			System.out.println("server is offline");
			System.exit(0);
		}
		catch (SocketException e) {
			System.out.println("server went offline");	
			System.exit(0);
		}			
    } 
} 
