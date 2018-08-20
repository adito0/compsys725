/**
 * author: Ira Sukimin (isuk218@aucklanduni.ac.nz)
 * COMPSYS725 - Computer Networks and Distributed Applications
 **/

import java.io.*; 
import java.net.*;
import java.nio.file.*;
import java.nio.file.attribute.*;

class clientTCP { 
   
	public static String command = "";
	public static String errorMessage; 	
	
	public static BufferedReader inFromUser;
	public static Socket clientSocket;
	public static DataOutputStream outToServer;
	public static BufferedReader inFromServer;
	public static DataOutputStream fileOutToServer; 
	private static final File defaultDirectory = FileSystems.getDefault().getPath("").toFile().getAbsoluteFile();
	private File currentDirectory = defaultDirectory;
	
	public void attemptConnection() throws Exception {
		inFromUser = new BufferedReader(new InputStreamReader(System.in)); 
		clientSocket = new Socket("localhost", 1500); 
		outToServer = new DataOutputStream(clientSocket.getOutputStream()); 
		inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		fileOutToServer = new DataOutputStream(clientSocket.getOutputStream());
		fetchServerResponse();
		
		if (errorMessage.charAt(0) == '+') {
			System.out.println("connection to server is established");
		}
		else {
			System.out.println("could not connect to server");
			clientSocket.close();
		}
	}

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
	
	public void checkValidCommand() throws Exception {

		System.out.println("enter command: ");
		command = inFromUser.readLine();
		
		if (command != null) {
			outToServer.writeBytes(command + "\0");
		}
		else {
			System.out.println("invalid command entered. pls try again"); 
			checkValidCommand();
		}
	}			

	public void fetchServerResponse() throws Exception {
		errorMessage = readServerResponse();
		System.out.println("from server: " + errorMessage); 
		if (errorMessage.charAt(0) == 'f') {
			System.out.println("enter filename to be sent to the server: ");
			command = inFromUser.readLine();
			File file = new File(currentDirectory.toString() + "/" + command);
			sendFile(file);
			fetchServerResponse();
		}
	}

	public boolean sendFile(File file) {
		System.out.println("sendFile() called");
		byte[] bytes = new byte[(int) file.length()];

		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bufferedInStream = new BufferedInputStream(new FileInputStream(file));
			
			System.out.println("Total file size to read (in bytes) : " + fis.available());

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
	
    public static void main(String argv[]) throws Exception 
    { 
		//create new instance of TCPServer
		clientTCP server = new clientTCP();		
		server.attemptConnection();
		
		while(true) {
			server.checkValidCommand();
			server.fetchServerResponse();
		}
    } 
} 
