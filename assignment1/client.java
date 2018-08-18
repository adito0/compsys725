/**
 * author: Ira Sukimin (isuk218@aucklanduni.ac.nz)
 * COMPSYS725 - Computer Networks and Distributed Applications
 **/

import java.io.*; 
import java.net.*;

class clientTCP { 
   
	public static String sentence; 
	public static String errorMessage; 	
	public static BufferedReader inFromUser;
	public static Socket clientSocket;
	public static DataOutputStream outToServer;
	public static BufferedReader inFromServer;
	
	public void USER() throws Exception {
		System.out.println("username: ");
		sentence = "USER[ " + inFromUser.readLine() + "]"; 
		
		try {
			outToServer.writeBytes(sentence + "\n");  												
			errorMessage = inFromServer.readLine();
			System.out.println("from server: " + errorMessage);
		}
		catch(IOException e) {
		  	e.printStackTrace();
		}

		
		if (errorMessage.charAt(0) == '+') {
			ACCT();
		}
		else if (errorMessage.charAt(0) == '!') {
			System.out.println("u r logged in");
			TYPE();
		}
		else if (errorMessage.charAt(0) == '-') {
			USER();
		}
	}

	public void ACCT() throws Exception {
		
		System.out.println("account: ");
		sentence = "ACCT[ " + inFromUser.readLine() + "]"; 

		try {
			outToServer.writeBytes(sentence + "\n"); 
			errorMessage = inFromServer.readLine();
			System.out.println("from server: " + errorMessage); 
		}
		catch(IOException e) {
		  	e.printStackTrace();
		}
		
		if (errorMessage.charAt(0) == '+') {
			PASS();
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

		try {
			outToServer.writeBytes(sentence + "\n"); 
			errorMessage = inFromServer.readLine();
			System.out.println("from server: " + errorMessage); 
		}
		catch(IOException e) {
		  	e.printStackTrace();
		}
		
		if (errorMessage.charAt(0) == '+') {
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

		try {
			outToServer.writeBytes(sentence + "\n"); 
			errorMessage = inFromServer.readLine();
			System.out.println("from server: " + errorMessage); 
		}
		catch(IOException e) {
		  	e.printStackTrace();
		}
		
		if (errorMessage.charAt(0) == '+') {
		  	System.out.println("file type is valid");
			LIST();
		}
		else if (errorMessage.charAt(0) == '-') {
		  	System.out.println("file type is invalid");
			TYPE();
		}		
	}	

	public void LIST() throws Exception {
		
		System.out.println("directory path: ");
		sentence = "LIST[ " + inFromUser.readLine() + "]"; 

		try {
			outToServer.writeBytes(sentence + "\n"); 
			errorMessage = inFromServer.readLine();
			System.out.println("from server: " + errorMessage); 
		}
		catch(IOException e) {
		  	e.printStackTrace();
		}
		
		if (errorMessage.charAt(0) == '+') {
		  	System.out.println("file type is valid");
			LIST();
		}
		else if (errorMessage.charAt(0) == '-') {
		  	System.out.println("file type is invalid");
			TYPE();
		}		
	}		
	
    public static void main(String argv[]) throws Exception 
    { 
		//create new instance of TCPServer
		clientTCP server = new clientTCP();		
		inFromUser = new BufferedReader(new InputStreamReader(System.in)); 
		clientSocket = new Socket("localhost", 1024); 
		outToServer = new DataOutputStream(clientSocket.getOutputStream()); 
		inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		//errorMessage = inFromServer.readLine();
		//if (errorMessage == "+CS725 SFTP Service") {
			server.USER();
		//} 

    } 
} 
