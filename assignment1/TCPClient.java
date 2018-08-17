/**
 * Code is taken from Computer Networking: A Top-Down Approach Featuring 
 * the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross, 
 * All Rights Reserved.
 **/

import java.io.*; 
import java.net.*;

class TCPClient { 
   
	public static String sentence; 
	public static String errorMessage; 	
	
	public void USER() throws Exception {
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
		TCPClient server = new TCPClient();		

		while (true) {
			server.USER();

			//clientSocket.close(); 
		}
    } 
} 
