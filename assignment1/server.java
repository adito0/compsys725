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
	public static String command; 
	
	public static String errorMessage = "! unidentified error";
	public static boolean userLoggedIn = false;
	public static boolean accountLoggedIn = false;
	public static boolean freeToConnect = true;
	
	public static ServerSocket welcomeSocket;
	public static Socket connectionSocket;
	public static BufferedReader inFromClient;
	public static DataOutputStream outToClient;
	
	public void checkValidCommand() throws Exception {
		System.out.println("checking for valid command");
		command = inFromClient.readLine();
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
	}
	
	public void USER() throws Exception {
		System.out.println("USER() called");
		
		if (userLoggedIn == false) {
			if (args.equalsIgnoreCase("syammy")) {
				errorMessage = "+ login was succesful";
				userLoggedIn = true;
			} else {
				errorMessage = "- user does not exist";
			}
		}
		else {
			errorMessage = "! user is already logged in";
		}	
		
		try {
			System.out.println("can't send errorMessage to client side"); 
			outToClient.writeBytes(errorMessage + "\n"); 	
			System.out.println("has sent errorMessage to client side");
		}
		catch(IOException e) {
		  	e.printStackTrace();
		}		

	}

	public void ACCT() throws Exception {
		System.out.println("ACCT() called");
		if (accountLoggedIn == false) {		
			if (args.equalsIgnoreCase("syumu")) {
				errorMessage = "+ login was succesful";
				accountLoggedIn = true;
			} else {
				errorMessage = "- account does not exist";
			}	
		}	
		else {
			errorMessage = "! account is already logged in";
		}
		try {
			outToClient.writeBytes(errorMessage + "\n"); 
		}
		catch(IOException e) {
		  	e.printStackTrace();
		}		
	}
	
    public static void main(String argv[]) throws Exception {
		
		//create new instance of serverTCP
		serverTCP server = new serverTCP();
	
		
		//setup of welcoming socket
		welcomeSocket = new ServerSocket(1024); 
		System.out.println("server is running..."); 
		
		connectionSocket = welcomeSocket.accept(); 
		System.out.println("incoming data..."); 
		
		inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream())); 
		outToClient = new DataOutputStream(connectionSocket.getOutputStream()); 
		
		while(true) {
			server.checkValidCommand(); 
		} 

	}
} 


