/**
 * Code is taken from Computer Networking: A Top-Down Approach Featuring 
 * the Internet, second edition, copyright 1996-2002 J.F Kurose and K.W. Ross, 
 * All Rights Reserved.
 **/

import java.io.*; 
import java.net.*; 

class TCPServer { 
 
	//variable declaration
	public static String args;
	public static String cmd;
	public static String errorMessage = "unidentified error";
	public static boolean loggedIn = false;
	public static String errorCode = "!";	
	
	public void commandSplitter(String command) throws Exception {
		String[] parts = command.split("\\[ ",2);
		cmd = parts[0];
		String a = parts[1];
		String[] kentuts = a.split("\\]",2);
		args = kentuts[0];
	}
	
	public void USER(String cmd) throws Exception {
		
		boolean checkCmdUser = cmd.equalsIgnoreCase("user");

		if (checkCmdUser == true) {
			if (loggedIn == false) {
				if (args.equalsIgnoreCase("syammy")) {
					errorCode = "+";
					errorMessage = "login was succesful";
					loggedIn = true;
				} else {
					errorCode = "-";
					errorMessage = "user does not exist";
				}
			}
			else {
				errorCode = "!";
				errorMessage = "user is already logged in";
			}				
		}	
		
	}
	
    public static void main(String argv[]) throws Exception {
		
		//create new instance of TCPServer
		TCPServer server = new TCPServer();
		
		String command; 
	
		//setup of welcoming socket
		ServerSocket welcomeSocket = new ServerSocket(1024); 

		while(true) { 

			System.out.println("server is running..."); 
			Socket connectionSocket = welcomeSocket.accept(); 
			System.out.println("incoming data..."); 

			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream())); 
			DataOutputStream  outToClient = new DataOutputStream(connectionSocket.getOutputStream()); 
			command = inFromClient.readLine(); 
			
			server.commandSplitter(command);
			server.USER(cmd);


			System.out.println("args: " + args);
			System.out.println("errorCode: " + errorCode);

			outToClient.writeBytes(errorCode); 
        } 
    } 
} 

