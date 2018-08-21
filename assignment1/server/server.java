/**
 * author: Ira Sukimin (isuk218@aucklanduni.ac.nz)
 * COMPSYS725 - Computer Networks and Distributed Applications
 **/

import java.io.*; 
import java.net.*; 
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.ArrayList;
import java.util.List; 
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.util.Date;
import java.text.SimpleDateFormat;

class serverTCP { 
 
	//variable declaration
	private static int port = 1500;
	private static String args;
	private static String cmd;
	private static String command; 
	private static String fileType = "b"; 
	private static String currentUser;
	private static String currentAccount;
	private static String currentPassword;
	private static String filename = "";
	private static String dir = "";
	private static String errorMessage = "-unidentified error";
	//flag variable declaration
	private static boolean userLoggedIn = false;
	private static boolean accountLoggedIn = false;
	private static boolean freeToConnect = true;
	private static boolean outToLunch = false;
	private static boolean existsInList = false;
	private static boolean accountSpecified = false;
	private static boolean skipPassword = false;
	private static boolean isConnected = false;
	//data related variable declaration
	private static ServerSocket welcomeSocket;
	private static Socket connectionSocket;
	private static BufferedReader inFromClient;
	private static DataOutputStream outToClient;
	private static DataOutputStream dataOutToClient; 
	private static BufferedInputStream dataInFromClient;
	//file related variable declarations
	private static final File defaultDirectory = FileSystems.getDefault().getPath("").toFile().getAbsoluteFile();
	private File currentDirectory = defaultDirectory; //initialise currentDirectory to the defaultDirectory for now
	private File file;

	/** 
	*	descripton	: 	acceptConnection() accepts connection made through the welcome socket, sets up the required 
						buffer readers to allow communication between client and server, and then sends back the 
						appropriate response code along along with a brief errorMessage	
	*	args		: 	NONE
	*	returns		:	NONE
	**/	
	private void acceptConnection() throws Exception {
		System.out.println("server is running..."); 
		connectionSocket = welcomeSocket.accept();	
		System.out.println("client is connected..."); 
		isConnected = true;
		inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream())); 
		outToClient = new DataOutputStream(connectionSocket.getOutputStream()); 
		dataOutToClient = new DataOutputStream(connectionSocket.getOutputStream());
		dataInFromClient = new BufferedInputStream(connectionSocket.getInputStream());
		if (outToLunch == true) {
			errorMessage = "-CS725 SFTP Service";
			sendResponseToClient(errorMessage);
			connectionSocket.close();
		}
		else {
			errorMessage = "+CS725 SFTP Service";
			sendResponseToClient(errorMessage);;		
		}
	}

	/** 
	*	descripton	: 	sendResponse() appends a <null> character at the end of errorMessage intended for the 
	*					client side and then sends it to the client. 
	*	args		: 	NONE
	*	returns		:	NONE
	**/
	private void sendResponseToClient(String errorMessage) throws Exception {
		try {
			outToClient.writeBytes(errorMessage + "\0");
		}
		catch (IOException e) {
			System.out.println("IOException has occured sendResponseToClient");
		}
	}

	/** 
	*	descripton	: 	readClientResponse() reads the response from the client and concatenates char by char 
	*					until a <null> character is found. The concatenatedString is then returned.
	*	args		: 	NONE
	*	returns		:	concatenatedString - the whole line sent by the client without the null character
	**/
	private String readClientResponse() {
		String concatenatedString = "";
		int character = 0;

		while (true) {
			try {
				character = inFromClient.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (character == 0) {
				break;
			}
			// Concatenate char into concatenatedString.
			concatenatedString = concatenatedString.concat(Character.toString((char)character));
		}
		return concatenatedString;
	}

	/** 
	*	descripton	:  	checkValidCommand() first breaks down the line sent by the client into two parts.
	*					The first part, cmd which is the commande is checked for its validity. The second
	*					part, args which is the argument is then assigned if the client has provided any 
	*					arguments.
	*	args		: 	NONE
	*	returns		:	NONE
	**/	
	private void checkValidCommand() throws Exception {
		String[] validCommands = new String[] {"TYPE","LIST","CDIR","KILL","NAME","DONE","RETR","STOR","TOBE","SEND","STOP"};
		cmd = "";
		args = "";
		command = readClientResponse();
	
		if (command != null) {
			try {
				String[] parts = command.split("\\ ",2);
				cmd = parts[0];
				args = parts[1];
			}
			catch (ArrayIndexOutOfBoundsException e) {
				cmd = command;
			}
			
			System.out.println("cmd: " + cmd);
			System.out.println("args: " + args);	
			
			if (cmd.equalsIgnoreCase("USER")) {
				USER();
			}
			else if (cmd.equalsIgnoreCase("ACCT")) {
				ACCT();
			}
			else if (cmd.equalsIgnoreCase("PASS")) {
				PASS();
			}	
			else {
				if (userLoggedIn) {
					if (cmd.equalsIgnoreCase("TYPE")) {
						TYPE();
					}	
					else if (cmd.equalsIgnoreCase("LIST")) {
						LIST();
					}			
					else if (cmd.equalsIgnoreCase("CDIR")) {
						CDIR();
					}
					else if (cmd.equalsIgnoreCase("KILL")) {
						KILL();
					}
					else if (cmd.equalsIgnoreCase("NAME")) {
						NAME();
					}	
					else if (cmd.equalsIgnoreCase("DONE")) {
						DONE();
					}	
					else if (cmd.equalsIgnoreCase("RETR")) {
						RETR();
					}		
					else if (cmd.equalsIgnoreCase("STOR")) {
						STOR();
					}		
					else if (cmd.equalsIgnoreCase("TOBE")) {
						TOBE();
					}		
					else if (cmd.equalsIgnoreCase("SEND")) {
						SEND();
					}		
					else if (cmd.equalsIgnoreCase("STOP")) {
						STOP();
					}
					else {
						errorMessage = "-invalid command, pls try again";
						sendResponseToClient(errorMessage);
						checkValidCommand();
					}
				}
				else {
					if (Arrays.asList(validCommands).contains(cmd)) {
						errorMessage = "-you are not logged in. please do so";
					}
					else {
						errorMessage = "-invalid command, pls try again";
					}
					sendResponseToClient(errorMessage);
					checkValidCommand();					
				}
			}
		}
		else {
			System.out.println("client has disconnected..."); 
			acceptConnection();
			checkValidCommand();
		}	
	}

	/** 
	*	descripton	: 	findUser() reads the .txt file passed line by line and breaks it down into three parts
	*					which are the username, account and the password. The file is looped through until
	*					the details of the current client is found. Updates the flag variable existsInList
	*					when user is found.
	*					Each line is formatted in this format: <username>[<account>]<password> 
	*	args		: 	fileName - name of the file in the default directory containing the users' details
	*					args - string of the current user
	*	returns		:	NONE
	**/		
	private void findUser(String fileName, String args) throws Exception {
		BufferedReader foo = new BufferedReader(new FileReader(fileName));
		StringBuilder bar = new StringBuilder();
		String line = foo.readLine();
		String[] parts0;
		String parts1;
		String[] parts2;
		currentUser = null;
		currentAccount = null;
		currentPassword = null;
		existsInList = false;
		
		while (line != null) {
			bar.append(line);
			bar.append(System.lineSeparator());
			line = foo.readLine();
			if (line != null) {
				parts0 = line.split("\\[",2);
				currentUser = parts0[0];
				parts1 = parts0[1];
				parts2 = parts1.split("\\]",2);
				currentAccount = parts2[0];
				currentPassword = parts2[1];
				if (currentUser.equals(args)) {
					existsInList = true;
					break;
				}
				else {
					currentUser = args;
				}
			}
		}	
 		foo.close();		
	}

/**
*	--------------------------------------------------------------------------------------------------------------
*   --------------------------------------------------------------------------------------------------------------
* 	----------METHODS HANDLING THE COMMANDS IN THE SFTP PROTOCOL--------------------------------------------------
*	--------------------------------------------------------------------------------------------------------------
*	--------------------------------------------------------------------------------------------------------------
**/
	
	/** 
	*	descripton	: 	USER() checks if the username argument provided by client is a user within the userList and 
	*					sends our the appropriate errorMessage to the client.
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	private void USER() throws Exception {
		//System.out.println("USER() called");
		findUser("userList.txt",args);
		if (existsInList == false) {
			errorMessage = "-invalid user id, try again";
			System.out.println("line224");
		}
		else {
			if (userLoggedIn) {
				errorMessage = "!" + currentUser + " already logged in";
				userLoggedIn = true;
			}
			else {
				if (currentUser.equals("admin")) {
					errorMessage = "!" + currentUser + " logged in";
					userLoggedIn = true;
				}
				else {
					errorMessage = "+user id valid, send account and password";
				}	
			}
		
		}
		sendResponseToClient(errorMessage);		
	}

	/** 
	*	descripton	: 	ACCT() checks if the account argument provided by client matches currentAccount retrieved
	*					from the server's userList and sends our the appropriate errorMessage to the client.
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	private void ACCT() throws Exception {
		//System.out.println("ACCT() called"); 
		if ((currentUser.equals("admin")) || (userLoggedIn)) {
			accountSpecified = true;
			errorMessage = "!account was not needed. skip the password";
			System.out.println("(currentUser.equals(admin): " + (currentUser.equals("admin")));
			System.out.println("userLoggedIn: " + userLoggedIn);
			userLoggedIn = true;
		}
		else if (args.equals(currentAccount)) {
			accountSpecified = true;
			if (skipPassword) {
				errorMessage = "!account ok. skip the password";
				userLoggedIn = true;
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

		sendResponseToClient(errorMessage); 
	}

	/** 
	*	descripton	: 	PASS() checks if the password argument provided by client matches currentPassword retrieved
	*					from the server's userList and sends our the appropriate errorMessage to the client.
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	private void PASS() throws Exception {
		//System.out.println("PASS() called");
		if ((currentUser.equals("admin")) || (userLoggedIn) || (args.equals(currentPassword))) {
			if (accountSpecified) {
				errorMessage = "!logged in";
				userLoggedIn = true;
			}
			else {
				skipPassword = true;
				errorMessage = "+send account";
			}
		}
		else {
			errorMessage = "-wrong password, try again";
		}

		sendResponseToClient(errorMessage); 
	}	

	/** 
	*	descripton	: 	TYPE() changes the type of transmission byte stream according to the argument provided
	*					by the client and sends our the appropriate errorMessage to the client.
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	private void TYPE() throws Exception {
		//System.out.println("TYPE() called");
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
		sendResponseToClient(errorMessage);
	}

	/** 
	*	descripton	: 	LIST() lists down the files and subdirectories alphabetically within the currentDirectory 
	*					according to the listingFormat argument provided by the client. If client has provided a 
	*					directory argument then the listing is done for the specified directory. Listing of the
	*					files is returned following the appropriate response code.
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	private void LIST() throws Exception {
		//System.out.println("LIST() called");
		int strlen = args.length();
		String listingFormat = "";
		String dir = "";
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy kk:mm");
		File path = defaultDirectory;
		//splitting of the arguments provided
		try {
			String[] parts = args.split("\\ ",2);
			listingFormat = parts[0];
			dir = parts[1];
			if (dir == "") {
				//set path to currentDirectory if no directory argument provided
				path = currentDirectory;
			}
			else {
				path = new File(currentDirectory.toString() + "/" + dir);	
			}
		}
		catch (ArrayIndexOutOfBoundsException e) { 
			listingFormat = args;
			path = currentDirectory;
		}		
		File files[] = path.listFiles();	
		String outputList = "";
		//check if the listingFormat argument provided by user is valid
		if ((listingFormat.equalsIgnoreCase("v")) || (listingFormat.equalsIgnoreCase("f"))) {		
			outputList = "+" + path + "\n./\n../\n"; 
			for (File f : files) {
				String filename = f.getName();
				if (f.isDirectory()) {
					filename = filename.concat("/");
				}
				//for verbose directory listing
				if (listingFormat.equalsIgnoreCase("v"))  {
					long modifiedTime = f.lastModified();
					String modifiedDate = dateFormat.format(new Date(modifiedTime));
					String size = String.valueOf(f.length());
					String owner = "";

					try {
						 FileOwnerAttributeView attr = Files.getFileAttributeView(f.toPath(), FileOwnerAttributeView.class);
						 owner = attr.getOwner().getName();
					} catch (IOException e) {	
						e.printStackTrace();
					}
					outputList = outputList.concat(String.format("%-30s %-20s %10s %20s \r\n", filename, modifiedDate, size, owner));
				} 
				//for standard formatting directory listing
				else {
					outputList = outputList.concat(String.format("%s \r\n", filename));
				}
			}
		}
		//the listingFormat argument provided is invalid
		else {
			outputList = "-invalid file listing format";
		}
		sendResponseToClient(outputList);
	}

	/** 
	*	descripton	: 	CDIR() changes the directory to the directory argument provided by the client if the
	*					directory is valid (i.e exists).					
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	private void CDIR() throws Exception {
		System.out.println("CDIR() called");
		String directoryToBe = args;	
			
		if (directoryToBe.charAt(0) == '~') {
			directoryToBe = directoryToBe.replaceAll("~", "/");
			currentDirectory = defaultDirectory;
		}
		if (directoryToBe.charAt(0) != '/') {
			directoryToBe = String.format("/%s", directoryToBe);
		}
		
		if (directoryToBe.charAt(directoryToBe.length()-1) != '/') {
			directoryToBe = directoryToBe.concat("/");
		}

		File newDir = new File(currentDirectory.toString().concat(directoryToBe)).toPath().normalize().toFile();
		//check if directory speciified exists
		if (!newDir.isDirectory()) {
			errorMessage = ("-cant't connect to directory because it doesn't exist");
			System.out.println(newDir);
		}
		else {
			if (newDir.compareTo(defaultDirectory.getAbsoluteFile()) < 0){
				errorMessage = ("-can't connect to directory because permission denied");
			}
			else {
				// Replace portion of the path to ~
				// Client doesn't need to know the absolute directory on the server
				String newDirReply = String.format("~%s", newDir.toString().substring(defaultDirectory.toString().length()));
				// Already logged in
				if (userLoggedIn) {
					errorMessage = (String.format("!Changed working dir to %s", newDirReply));
					currentDirectory = newDir;
				} else {
					errorMessage = String.format("+directory ok, send account/password", newDir);
					// Run CDIR authentication procedure
					/**if (cdirAuthenticate()) {
						currentDirectory = newDir;
						outToClient.writeBytes(String.format("!Changed working dir to %s", newDirReply));
					}**/
				}			
			}
		
		}
		sendResponseToClient(errorMessage);
	}

	/** 
	*	descripton	: 	KILL() deletes the file argument provided by the client in the current directory if the
	*					file is valid (i.e exists) and not protected
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	private void KILL() throws Exception {
		System.out.println("KILL() called");
		String filename = args;
		
		Path path = new File(currentDirectory.toString().concat("/").concat(filename)).toPath();
		
		try {
			Files.delete(path);
			errorMessage = String.format("+%s deleted", filename);
			
		} catch (NoSuchFileException x) {
		    errorMessage = "-no such file exist in the directory";
		    
		} catch (IOException x) {
		    errorMessage = "-file is proteccc";
		}
		sendResponseToClient(errorMessage);
	}

	/** 
	*	descripton	: 	
	*					
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	private void NAME() throws Exception {
		System.out.println("NAME() called");
		
		String oldFilename = args;
		File oldFile = new File(currentDirectory.toString() + "/" + oldFilename);
		
		// Check if file exists
		if (!oldFile.isFile()) {
			errorMessage = String.format("-Can't find %s", oldFilename);
		}
		else {
			errorMessage = String.format("+File exists");
			sendResponseToClient(errorMessage);	
			// Wait for TOBE command
			if (TOBE()) {
				String newFilename = args;
				File newFile = new File(currentDirectory.toString() + "/" + newFilename);

				// Check if the new filename is already taken
				if (newFile.exists()) {
					errorMessage = String.format("-File wasn't renamed because new file name already exists");
				}

				// Rename
				if (oldFile.renameTo(newFile)) {
					errorMessage = String.format("+%s renamed to %s", oldFilename, newFilename);
				} else {
					errorMessage = String.format("-File wasn't renamed because it's protected");
				}
			}
			else {
				errorMessage = String.format("-File wasn't renamed because command was not \"TOBE\"");
			}		
		}
		sendResponseToClient(errorMessage);			
	}	

	/** 
	*	descripton	: 	
	*					
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	private void DONE() throws Exception {
		System.out.println("DONE() called");
		errorMessage = "+bye";
		sendResponseToClient(errorMessage);
		connectionSocket.close();
	}

	/** 
	*	descripton	: 	
	*					
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	private void RETR() throws Exception {
		System.out.println("RETR() called");
		filename = args;
		Boolean skip = false; 
		// Specified file
		file = new File(currentDirectory.toString() + "/" + filename);
		
		// Specified file is not a file
		if (!file.isFile()) {
			errorMessage = ("-File doesn't exist");
			skip = true;
		}
		else {
			System.out.println("File of interest = " + file.toPath().toAbsolutePath().toString());
			/*		step 1:	send file size	*/
			// Get file size
			long fileSize = file.length();
			errorMessage = (String.format("%s", String.valueOf(fileSize)));	
		}
		sendResponseToClient(errorMessage);
		
		if (!skip) {
			if (SEND()) {
				sendFile(file);
			}
			else if (STOP()) {
				errorMessage = ("+ok, RETR aborted");
				sendResponseToClient(errorMessage);
			}
			else {
				errorMessage = ("-Invalid response");
				sendResponseToClient(errorMessage);
			}
		}
			
	}

	/** 
	*	descripton	: 	
	*					
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	private boolean SEND() throws Exception {
		System.out.println("SEND() called");
		cmd = readClientResponse();
		System.out.println(cmd);		
		if (cmd.equalsIgnoreCase("SEND")) {
			return true;
		}	
		else {
			return false;
		}	
	}	

	/** 
	*	descripton	: 	
	*					
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	private void sendFile(File file) throws Exception {
		System.out.println("sendFile() called");
		byte[] bytes = new byte[(int) file.length()];

		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bufferedInStream = new BufferedInputStream(new FileInputStream(file));

			int content = 0;
			
			// Read and send file until the whole file has been sent
			while ((content = bufferedInStream.read(bytes)) >= 0) {
				dataOutToClient.write(bytes, 0, content);
			}
			
			bufferedInStream.close();
			fis.close();
			dataOutToClient.flush();
	
		} catch (FileNotFoundException e) {
			
		} catch (IOException e) {
			
		}
	}	
	
	/** 
	*	descripton	: 	
	*					
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	private boolean STOP() throws Exception {
		System.out.println("STOP() called");
		command = readClientResponse();
		if (command != null) {
			String[] parts = command.split("\\ ",2);
			cmd = parts[0];
			args = parts[1];	
		}
		if (cmd.equalsIgnoreCase("STOP")) {
			return true;
		}	
		else {
			return false;
		}		
	}

	/** 
	*	descripton	: 	
	*					
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	private boolean TOBE() throws Exception {
		command = readClientResponse();
		if (command != null) {
			String[] parts = command.split("\\ ",2);
			cmd = parts[0];
			args = parts[1];	
		}
		if (cmd.equalsIgnoreCase("TOBE")) {
			System.out.println("True");
			return true;
		}	
		else {
			System.out.println("False");
			return false;
		}
	}

	/** 
	*	descripton	: 	
	*					
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	private void STOR() throws Exception {
		System.out.println("STOR() called");

		String mode = "";
		String filename = "";		
		
		if (args != null) {
			String[] parts = args.split("\\ ",2);
			mode = parts[0];
			filename = parts[1];
		}
		
		// Specified file
		File file = new File(currentDirectory.toString() + "/" + filename);
		System.out.println(file);
		System.out.println("File to be written = " + file.toPath().toAbsolutePath().toString());

		boolean overwrite = false;
		
		if (mode.equalsIgnoreCase("NEW")) {
			if (file.isFile()) {
				errorMessage = "-File exists, but system doesn't support generations";
			}
			else {
				errorMessage = "+File does not exist, will create new file";
			}
		}
		else if (mode.equalsIgnoreCase("OLD")) {
			if (file.isFile()) {
				errorMessage = "+Will write over old file";
				overwrite = true;
			} 
			else {
				errorMessage = "+Will create new file";
			}
		}
		else if (mode.equalsIgnoreCase("APP")) {
			if (file.isFile()) {
				errorMessage = "+Will append to file";
			} 
			else {
				errorMessage = "+Will create file";
			}		
		}
		else {
			errorMessage = "-Invalid mode";
		}
		
		sendResponseToClient(errorMessage);
		
		
		/*		step 2: Check file size	*/
		if (SIZE()) {
			
			long fileSize = Long.parseLong(args);
		
			// File doesn't fit on server
			try {
				if (!diskSpaceSufficient(fileSize)) {
					errorMessage = "-Not enough room, don't send it";
				}
				else {
					errorMessage = "+ok, waiting for file";
				}

			} catch (IOException e) {
				errorMessage = "-Error reading free space, don't send it";
			}

			sendResponseToClient(errorMessage);

			/*		step 3: receive file	*/

			// Receive the file
			try {
				System.out.println("1");
				receiveFile(file, fileSize, overwrite);
				errorMessage = String.format("+Saved %s", filename);
			} catch (IOException e) {
				e.printStackTrace();
				errorMessage = "-Couldn't save because write access permissions";
			}			
		}
		else {
			errorMessage = "-Invalid argument";
		}
		sendResponseToClient(errorMessage);
	}	

	/** 
	*	descripton	: 	
	*					
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	private void receiveFile(File file, long fileSize, boolean overwrite) throws IOException {
		System.out.println("receiveFile() called");
		FileOutputStream fileOutStream = new FileOutputStream(file, overwrite);
		BufferedOutputStream bufferedOutStream = new BufferedOutputStream(fileOutStream);

		// Read and write for all bytes
		for (int i = 0; i < fileSize; i++) {
			bufferedOutStream.write(dataInFromClient.read());
		}

		bufferedOutStream.close();
		fileOutStream.close();
	}

	/** 
	*	descripton	: 	
	*					
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	private boolean SIZE() throws Exception {
		System.out.println("SIZE() called");
		command = readClientResponse();
		if (command != null) {
			String[] parts = command.split("\\ ",2);
			cmd = parts[0];
			args = parts[1];
		}
		if (cmd.equalsIgnoreCase("SIZE")) {
			return true;
		}	
		else {
			return false;
		}		
	}	

	/** 
	*	descripton	: 	
	*					
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	private boolean diskSpaceSufficient(long fileSize) throws IOException {
		long freeSpace = Files.getFileStore(currentDirectory.toPath().toRealPath()).getUsableSpace();
		if (fileSize < freeSpace) {
			return true;
		}
		else {
			return false;
		}		
	}	
	
    public static void main(String argv[]) throws Exception {
		//create new instance of serverTCP
		serverTCP server = new serverTCP();
		//setup of welcoming socket
		welcomeSocket = new ServerSocket(port); 
			
		while(true) {
			if (!isConnected) {
				server.acceptConnection();
			}
			server.checkValidCommand(); 
		} 

	}
} 


