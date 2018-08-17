/**
 * Code is taken from Computer Networking: A Top-Down Approach Featuring 
 * the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross, 
 * All Rights Reserved.
 **/

import java.io.*; 
import java.net.*; 

class TCPServer { 
    
    public static void main(String argv[]) throws Exception 
    { 
	String clientSentence; 
	String capitalizedSentence; 
	
	ServerSocket welcomeSocket = new ServerSocket(1024); 
	
	while(true) { 
	    
		System.out.println("server is running..."); 
        Socket connectionSocket = welcomeSocket.accept(); 
	    System.out.println("incoming data..."); 
		
	    BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream())); 
	    
	    DataOutputStream  outToClient = new DataOutputStream(connectionSocket.getOutputStream()); 
	    
	    clientSentence = inFromClient.readLine(); 
	   
		String[] parts = clientSentence.split("\\[ ",2);
		String cmd = parts[0];
		String a = parts[1];
		String[] kentuts = a.split("\\]",2);
		String args = kentuts[0];
		
		System.out.println("cmd: " + cmd); 
		System.out.println("args: " + args); 
		
	    capitalizedSentence = clientSentence.toUpperCase() + '\n'; 
	    
	    outToClient.writeBytes(capitalizedSentence); 
        } 
    } 
} 

