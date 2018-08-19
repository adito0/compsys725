/**
 * author: Ira Sukimin (isuk218@aucklanduni.ac.nz)
 * COMPSYS725 - Computer Networks and Distributed Applications
 **/

import java.io.*; 
import java.net.*; 
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.util.Date;
import java.text.SimpleDateFormat;

class serverTCP { 
 
	//variable declaration
	public static String args;
	public static String cmd;
	public static String command; 
	public static String fileType = "b"; 
	public static String currentUser;
	public static String currentAccount;
	public static String currentPassword;
	
	public static ArrayList<String> loggedInUsers;
	
	
	public static String errorMessage = "! unidentified error";
	public static boolean userLoggedIn = false;
	public static boolean accountLoggedIn = false;
	public static boolean freeToConnect = true;
	public static boolean outToLunch = false;
	public static boolean existsInList = false;
	public static boolean accountSpecified = false;
	public static boolean skipPassword = false;
	
	public static ServerSocket welcomeSocket;
	public static Socket connectionSocket;
	public static BufferedReader inFromClient;
	public static DataOutputStream outToClient;
	
	private File currentDirectory = FileSystems.getDefault().getPath("").toFile().getAbsoluteFile();
	
	public void acceptConnection() throws Exception {
		System.out.println("server is running..."); 
		connectionSocket = welcomeSocket.accept();		
		inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream())); 
		outToClient = new DataOutputStream(connectionSocket.getOutputStream()); 
		if (outToLunch == true) {
			outToClient.writeBytes("-CS725 SFTP Service\n");
			connectionSocket.close();
		}
		else {
			outToClient.writeBytes("+CS725 SFTP Service\n");	
			System.out.println("a client is connected..."); 
		}
	}
	
	public void checkValidCommand() throws Exception {

		command = inFromClient.readLine();
		
		if (command != null) {
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
			else if (cmd.equalsIgnoreCase("PASS")) {
				PASS();
			}	
			else if (cmd.equalsIgnoreCase("TYPE")) {
				TYPE();
			}	
			else if (cmd.equalsIgnoreCase("LIST")) {
				LIST();
			}				
		}
		else {
			System.out.println("client has disconnected..."); 
			acceptConnection();
			checkValidCommand();
		}
				
	}

	public void readFile(String fileName, String args) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(fileName));

		StringBuilder sb = new StringBuilder();
		String line = br.readLine();
		String[] parts0;
		String parts1;
		String[] parts2;
		String a;
		currentUser = null;
		currentAccount = null;
		currentPassword = null;
		existsInList = false;
		
		while (line != null) {

			sb.append(line);
			sb.append(System.lineSeparator());

			line = br.readLine();
			if (line != null) {
				parts0 = line.split("\\[",2);
				currentUser = parts0[0];
				parts1 = parts0[1];
				parts2 = parts1.split("\\]",2);
				currentAccount = parts2[0];
				currentPassword = parts2[1];
				if (currentUser.equalsIgnoreCase(args)) {
					System.out.println("currentUser: " + currentUser); 
					System.out.println("currentAccount: " + currentAccount); 
					System.out.println("currentPassword: " + currentPassword);
					existsInList = true;
					break;
				}
			}
		}
				
 		br.close();		
	}
	
	public void USER() throws Exception {
		System.out.println("USER() called");
		readFile("userList.txt",args);
		if (existsInList == false) {
			errorMessage = "-invalid user-id, try again";
		}
		else {
			if (loggedInUsers.contains(currentUser)) {
				errorMessage = "!" + currentUser + " logged in";
			}
			else {
				if (currentUser.equalsIgnoreCase("admin")) {
					errorMessage = "!" + currentUser + " logged in";
					loggedInUsers.add(currentUser);
				}
				else {
					errorMessage = "+user-id valid, send account and password";
				}	
			}
		
		}
		outToClient.writeBytes(errorMessage + "\n");		
	}
	
	public void ACCT() throws Exception {
		System.out.println("ACCT() called"); 
		
		if ((currentUser.equalsIgnoreCase("admin")) || (loggedInUsers.contains(currentUser))) {
			accountSpecified = true;
			errorMessage = "!account was not needed. skip the password";
		}
		else if (args.equalsIgnoreCase(currentAccount)) {
			accountSpecified = true;
			if (skipPassword) {
				errorMessage = "!account ok. skip the password";
			}
			else {
				errorMessage = "+account valid, send password";
			}
		}
		else if (args.equalsIgnoreCase("")) {
			accountSpecified = false;
			errorMessage = "+send password";
		}
		else {
			errorMessage = "-invalid account, try again";
		}

		outToClient.writeBytes(errorMessage + "\n"); 
	}

	public void PASS() throws Exception {
		System.out.println("PASS() called");
		
		if ((currentUser.equalsIgnoreCase("admin")) || (loggedInUsers.contains(currentUser)) || (args.equalsIgnoreCase(currentPassword))) {
			if (accountSpecified) {
				errorMessage = "!logged in";
			}
			else {
				skipPassword = true;
				errorMessage = "+send account";
			}
		}
		else {
			errorMessage = "-wrong password, try again";
		}

		outToClient.writeBytes(errorMessage + "\n"); 
	}	

	public void TYPE() throws Exception {
		System.out.println("TYPE() called");
	
		if (args.equalsIgnoreCase("a")) {
			fileType = "a";
			errorMessage = "+using Ascii mode";
		} 
		else if (args.equalsIgnoreCase("b")) {
			fileType = "b";
			errorMessage = "+using Binary mode";
		} 
		else if (args.equalsIgnoreCase("c")) {
			fileType = "c";
			errorMessage = "+using Continuous mode"; 
		}
		else {
			errorMessage = "-type not valid";
		}
		outToClient.writeBytes(errorMessage + "\n");
	}
	
	public void LIST() throws Exception {
		System.out.println("LIST() called");
		int strlen = args.length();
		char listingFormat = '\0';
		String dir;
		String outputList = "+\n./\n../\n"; ;
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy kk:mm");
		
		try {
			listingFormat = args.charAt(0);
			if ((listingFormat != 'v') || (listingFormat != 'f')) {
				errorMessage = "-invalid file listing format";
			}
		}
		catch (StringIndexOutOfBoundsException e) {
			errorMessage = "-invalid file listing format";
		}
		
		try {
			dir = args.substring(1,strlen);
		}
		catch (StringIndexOutOfBoundsException e) {
			dir = "";
		}
		
		File path = currentDirectory;
		path = new File(currentDirectory.toString() + "/" + dir);
		File files[] = path.listFiles();
		//System.out.println("path: " + path);		
		
		// Go through each file in the directory
		for (File f : files) {
			String filename = f.getName();
			
			// Append / to directories
			if (f.isDirectory()) {
				filename = filename.concat("/");
			}
			
			// Verbose, get information on the file
			if (listingFormat == 'v') {
				long modifiedTime = f.lastModified();
				String modifiedDate = dateFormat.format(new Date(modifiedTime));
				String size = String.valueOf(f.length());
				String owner = "";

				// Get file owner's name
				try {
					 FileOwnerAttributeView attr = Files.getFileAttributeView(f.toPath(), FileOwnerAttributeView.class);
					 owner = attr.getOwner().getName();
				} catch (IOException e) {	
					e.printStackTrace();
				}

				// print structure:   filename   modified time    size    owner
				outputList = outputList.concat(String.format("%-30s %-20s %10s %20s \r\n", filename, modifiedDate, size, owner));
			
			// Non verbose, filename only
			} else {
				outputList = outputList.concat(String.format("%s \r\n", filename));
			}
		}
		
		outToClient.writeBytes(outputList);

	}
	
    public static void main(String argv[]) throws Exception {
		
		//create new instance of serverTCP
		serverTCP server = new serverTCP();
		loggedInUsers = new ArrayList();
	
		//setup of welcoming socket
		welcomeSocket = new ServerSocket(4206); 
		server.acceptConnection();
			
		while(true) {
			server.checkValidCommand(); 
		} 

	}
} 


