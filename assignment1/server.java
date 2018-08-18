/**
 * author: Ira Sukimin (isuk218@aucklanduni.ac.nz)
 * COMPSYS725 - Computer Networks and Distributed Applications
 **/

import java.io.*; 
import java.net.*; 

class serverTCP { 
 
	//variable declaration
	public static String args;
	public static String cmd;
	
	public static String errorMessage = "! unidentified error";
	public static boolean loggedIn = false;
	
	public static ServerSocket welcomeSocket;
	public static Socket connectionSocket;
	public static BufferedReader inFromClient;
	public static DataOutputStream  outToClient;
	
	public void checkValidCommand(String command) throws Exception {
		String[] parts = command.split("\\[ ",2);
		cmd = parts[0];
		String a = parts[1];
		String[] kentuts = a.split("\\]",2);
		args = kentuts[0];
		
		if (cmd.equalsIgnoreCase("USER")) {
			USER();
		}
		else if (cmd.equalsIgnoreCase("ACCT")) {
			ACCT();
		}
		System.out.println("errorMessage: " + errorMessage);
		outToClient.writeBytes(errorMessage);				
	}
	
	public void USER() throws Exception {
		
		if (loggedIn == false) {
			if (args.equalsIgnoreCase("syammy")) {
				errorMessage = "+ login was succesful";
				loggedIn = true;
			} else {
				errorMessage = "- user does not exist";
			}
		}
		else {
			errorMessage = "! user is already logged in";
		}					
		System.out.println("args: " + args);
	}

	public void ACCT() throws Exception {
		
		if (loggedIn == false) {
			if (args.equalsIgnoreCase("syammy")) {
				errorMessage = "+ login was succesful";
				loggedIn = true;
			} else {
				errorMessage = "- user does not exist";
			}
		}
		else {
			errorMessage = "! user is already logged in";
		}					
		System.out.println("args: " + args);	
	}
	
    public static void main(String argv[]) throws Exception {
		
		//create new instance of serverTCP
		serverTCP server = new serverTCP();
		
		String command; 
	
		//setup of welcoming socket
		welcomeSocket = new ServerSocket(1024); 

		while(true) { 

			System.out.println("server is running..."); 
			connectionSocket = welcomeSocket.accept(); 
			System.out.println("incoming data..."); 
			inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream())); 
			outToClient = new DataOutputStream(connectionSocket.getOutputStream()); 
			command = inFromClient.readLine(); 
			server.checkValidCommand(command); 
 
        } 
    } 
} 

