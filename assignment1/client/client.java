/**
 * author: Ira Sukimin (isuk218@aucklanduni.ac.nz)
 * COMPSYS725 - Computer Networks and Distributed Applications
 **/

import java.io.*; 
import java.net.*;
import java.nio.file.*;
import java.nio.file.attribute.*;

class clientTCP { 
   
	private static String command = "";
	private static String cmd = "";
	private static String errorMessage;
	private static String filename = "";
	private static long fileSize = 0;
	
	private static BufferedReader inFromUser;
	private static Socket clientSocket;
	private static DataOutputStream outToServer;
	private static BufferedReader inFromServer;
	private static DataOutputStream fileOutToServer; 
	private static BufferedInputStream fileInFromClient;
	private static final File defaultDirectory = FileSystems.getDefault().getPath("").toFile().getAbsoluteFile();
	private File currentDirectory = defaultDirectory;
	public static File file;

	/** 
	*	descripton	: 	
	*					
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	public void attemptConnection() throws Exception {
		inFromUser = new BufferedReader(new InputStreamReader(System.in)); 
		clientSocket = new Socket("localhost", 1500); 
		outToServer = new DataOutputStream(clientSocket.getOutputStream()); 
		inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		fileOutToServer = new DataOutputStream(clientSocket.getOutputStream());
		fileInFromClient = new BufferedInputStream(clientSocket.getInputStream());
		processServerResponse();
		
		if (errorMessage.charAt(0) == '+') {
			System.out.println("connection to server is established");
		}
		else {
			System.out.println("could not connect to server");
			clientSocket.close();
		}
	}
	
	/** 
	*	descripton	: 	
	*					
	*	args		: 	NONE
	*	returns		:	NONE
	**/	
	public String readServerResponse() throws Exception {
		String line = "";
		int character = 0;

		while (true) {
			try {
				character = inFromServer.read();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (character == 0) {
				break;
			}

			line = line.concat(Character.toString((char)character));
		}
		return line;
	}

	/** 
	*	descripton	: 	
	*					
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	public void checkValidCommand() throws Exception {

		System.out.println("enter command: ");
		command = inFromUser.readLine();
		
		if (command != null) {
			try {
				cmd = command.substring(0,4);
			}
			catch (StringIndexOutOfBoundsException e) {
				System.out.println("invalid command entered. pls try again"); 
				checkValidCommand();
			}
			if (cmd.equalsIgnoreCase("send")) {
				outToServer.writeBytes(command + "\0");				
				System.out.println("save file as: ");
				filename = inFromUser.readLine();
				File file = new File(currentDirectory.toString() + "/" + filename);
				receiveFile(file,fileSize,true);
				checkValidCommand();
			}
			else if (cmd.equalsIgnoreCase("stor")) {
				System.out.println("enter filename to be sent to the server: ");
				cmd = inFromUser.readLine();
				file = new File(currentDirectory.toString() + "/" + cmd);
				try {
					FileInputStream fis = new FileInputStream(file);
					BufferedInputStream bufferedInStream = new BufferedInputStream(new FileInputStream(file));
					System.out.println("filesize : " + fis.available());
					fis.close();
				}
				catch (FileNotFoundException e) {
					System.out.println("size of file could not be retrieved bcs there is no such file");
				}
				System.out.println("please check for size of file manually");
				outToServer.writeBytes(command + "\0");
			}			
			else {
				outToServer.writeBytes(command + "\0");
			}
			System.out.println(command);
		}
		else {
			System.out.println("invalid command entered. pls try again"); 
			checkValidCommand();
		}			
	}			

	/** 
	*	descripton	: 	
	*					
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	public void processServerResponse() throws Exception {
		errorMessage = readServerResponse();
		System.out.println("from server: " + errorMessage); 
		
		if (errorMessage.equalsIgnoreCase("+ok, waiting for file")) {
			sendFile(file);
			processServerResponse();
		}
		else if ((errorMessage.charAt(0) == '!') || (errorMessage.charAt(0) == '+') || (errorMessage.charAt(0) == '-')) {
							
		}
		else {
			fileSize = Long.parseLong(errorMessage.replaceAll("\\s",""));	
		}
	}

	/** 
	*	descripton	: 	
	*					
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	public boolean sendFile(File file) throws Exception {
		System.out.println("sendFile() called");
		byte[] bytes = new byte[(int) file.length()];

		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bufferedInStream = new BufferedInputStream(new FileInputStream(file));

			int content = 0;
			
			// Read and send file until the whole file has been sent
			while ((content = bufferedInStream.read(bytes)) >= 0) {
				fileOutToServer.write(bytes, 0, content);
			}
			
			bufferedInStream.close();
			fis.close();
			fileOutToServer.flush();
	
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		return true;
	}		

	/** 
	*	descripton	: 	
	*					
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	public void receiveFile(File file, long fileSize, boolean overwrite) throws Exception  {
		System.out.println("receiveFile() called");
		FileOutputStream fileOutStream = new FileOutputStream(file, overwrite);
		BufferedOutputStream bufferedOutStream = new BufferedOutputStream(fileOutStream);

		// Read and write for all bytes
		for (int i = 0; i < fileSize; i++) {
			bufferedOutStream.write(fileInFromClient.read());
		}

		bufferedOutStream.close();
		fileOutStream.close();
		System.out.println(filename + " has been saved");
	}

    public static void main(String argv[]) throws Exception 
    { 
		//create new instance of TCPServer
		clientTCP server = new clientTCP();		
		server.attemptConnection();
		
		while(true) {
			server.checkValidCommand();
			server.processServerResponse();
		}
    } 
} 
