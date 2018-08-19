/**
 * author: Ira Sukimin (isuk218@aucklanduni.ac.nz)
 * COMPSYS725 - Computer Networks and Distributed Applications
 **/

import java.io.*; 
import java.net.*;

class clientTCP { 
   
	public static String sentence; 
	public static String errorMessage; 	
	public static String listingStandard;
	public static String directoryPath;
	
	public static boolean skipPassword = false;
	
	public static BufferedReader inFromUser;
	public static Socket clientSocket;
	public static DataOutputStream outToServer;
	public static BufferedReader inFromServer;
	
	public void attemptConnection() throws Exception {
		inFromUser = new BufferedReader(new InputStreamReader(System.in)); 
		clientSocket = new Socket("localhost", 1024); 
		outToServer = new DataOutputStream(clientSocket.getOutputStream()); 
		inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		errorMessage = inFromServer.readLine();
		if (errorMessage.charAt(0) == '+') {
			USER();
		}
		else {
			System.out.println("could not connect to server");
			clientSocket.close();
		}
	}
	
	public void USER() throws Exception {
		System.out.println("username: ");
		sentence = "USER[ " + inFromUser.readLine() + "]"; 
		outToServer.writeBytes(sentence + "\n");  												
		errorMessage = inFromServer.readLine();
		System.out.println("from server: " + errorMessage);

		if (errorMessage.charAt(0) == '+') {
			ACCT();
		}
		else if (errorMessage.charAt(0) == '!') {
			TYPE();
		}
		else if (errorMessage.charAt(0) == '-') {
			USER();
		}
	}

	public void ACCT() throws Exception {
		
		System.out.println("account: ");
		sentence = "ACCT[ " + inFromUser.readLine() + "]"; 
		outToServer.writeBytes(sentence + "\n"); 
		errorMessage = inFromServer.readLine();
		System.out.println("from server: " + errorMessage); 
		
		if (errorMessage.charAt(0) == '+') {
			if (skipPassword) {
				skipPassword = false;
				TYPE();
			} 
			else {
				PASS();
			}
		}
		else if (errorMessage.charAt(0) == '!') {
			TYPE();	
		}
		else if (errorMessage.charAt(0) == '-') {
			ACCT();
		}		
	}

	public void PASS() throws Exception {
		
		System.out.println("password: ");
		sentence = "PASS[ " + inFromUser.readLine() + "]"; 
		outToServer.writeBytes(sentence + "\n"); 
		errorMessage = inFromServer.readLine();
		System.out.println("from server: " + errorMessage); 
		
		if (errorMessage.charAt(0) == '+') {
			skipPassword = true;
			ACCT();
		}
		else if (errorMessage.charAt(0) == '!') {
			TYPE();
		}
		else if (errorMessage.charAt(0) == '-') {
			PASS();
		}		
	}

	public void TYPE() throws Exception {
		
		System.out.println("file type: ");
		sentence = "TYPE[ " + inFromUser.readLine() + "]"; 
		outToServer.writeBytes(sentence + "\n"); 
		errorMessage = inFromServer.readLine();
		System.out.println("from server: " + errorMessage); 

		
		if (errorMessage.charAt(0) == '+') {
			LIST();
		}
		else if (errorMessage.charAt(0) == '-') {
			TYPE();
		}		
	}	

	public void processContinuousInputFromServer(String errorMessage) {
		System.out.println("from server: " + errorMessage);
	}	
	
	public void LIST() throws Exception {
		
		System.out.println("listing standard (f - standard, v - verbose): ");
		listingStandard = inFromUser.readLine();
		System.out.println("directory path: ");
		directoryPath = inFromUser.readLine();		
		sentence = "LIST[ " + listingStandard + directoryPath + "]"; 
		outToServer.writeBytes(sentence + "\n"); 
		errorMessage = inFromServer.readLine();
		
		if (errorMessage.charAt(0) == '+') {
			processContinuousInputFromServer(errorMessage);
			//CDIR();
		}
		else if (errorMessage.charAt(0) == '-') {
			LIST();
		}
		else {
			System.out.println("from server: " + errorMessage); 
		}
	}		
	
	
    public static void main(String argv[]) throws Exception 
    { 
		//create new instance of TCPServer
		clientTCP server = new clientTCP();		
		server.attemptConnection();

    } 
} 
