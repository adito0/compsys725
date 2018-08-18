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
	
	public boolean USER() throws Exception {
		
		System.out.println("username: ");
		sentence = "USER[ " + inFromUser.readLine() + "]"; 
		outToServer.writeBytes(sentence + '\n'); 

		errorMessage = inFromServer.readLine(); 
		
		if (errorMessage.indexOf(0) == '+') {
			
			return false;
		}
		else {
			return true;
		}
	}

	public void ACCT() throws Exception {
		
		System.out.println("username: ");
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in)); 
		Socket clientSocket = new Socket("localhost", 1024); 
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream()); 
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); 
		sentence = "USER[ " + inFromUser.readLine() + "]"; 
		outToServer.writeBytes(sentence + '\n'); 

		errorMessage = inFromServer.readLine(); 
		System.out.println("from server: " + errorMessage); 		
		
	}
	
    public static void main(String argv[]) throws Exception 
    { 
		//create new instance of TCPServer
		clientTCP server = new clientTCP();		
		inFromUser = new BufferedReader(new InputStreamReader(System.in)); 
		clientSocket = new Socket("localhost", 1024); 
		outToServer = new DataOutputStream(clientSocket.getOutputStream()); 
		inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); 												

		while (server.USER()) {
			server.USER();
			System.out.println("from server: " + errorMessage); 
		};

    } 
} 
