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
	private static boolean fromCDIR = false;
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

				//System.out.println("cmd: " + cmd);
				//System.out.println("args: " + args);	


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
			userLoggedIn = true;
			if (!fromCDIR) {
				errorMessage = "!account was not needed. skip the password";
			}
		}
		else if (args.equals(currentAccount)) {
			accountSpecified = true;
			if (skipPassword) {
				userLoggedIn = true;
				if (!fromCDIR) {
					errorMessage = "!account ok. skip the password";
				}
			}
			else {
				errorMessage = "+account valid, send password";
			}
		}
		else if (args.equalsIgnoreCase("")) {
			accountSpecified = false;
			if (!fromCDIR) {
				errorMessage = "+send password";
			}
			else {
				errorMessage = "-invalid account";
			}
		}
		else {
			errorMessage = "-invalid account, try again";
		}
		if (!fromCDIR) {
			sendResponseToClient(errorMessage); 
		}
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
				userLoggedIn = true;
				if (!fromCDIR) {
					errorMessage = "!logged in";
				}
			}
			else {
				skipPassword = true;
				if (!fromCDIR) {
					errorMessage = "+send account";				
				}
				else {
					errorMessage = "-invalid password";
				}
			}
		}
		else {
			errorMessage = "-wrong password, try again";
		}
		if (!fromCDIR) {
			sendResponseToClient(errorMessage); 
		}
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
		//System.out.println("CDIR() called");
		String directoryToBe = args;	
		if (directoryToBe.charAt(0) == '~') {
			directoryToBe = directoryToBe.replaceAll("~", "/");
			currentDirectory = defaultDirectory; //set the currentDirectory back to the defaultDirectory
		}
		//handle directory argument if '/' is included
		if (directoryToBe.charAt(0) != '/') {
			directoryToBe = String.format("/%s", directoryToBe);
		}
		//append a '/' if not included in directory argument
		if (directoryToBe.charAt(directoryToBe.length()-1) != '/') {
			directoryToBe = directoryToBe.concat("/");
		}
		File newDirectory = new File(currentDirectory.toString().concat(directoryToBe)).toPath().normalize().toFile();
		//check if directory specified exists
		if (!newDirectory.isDirectory()) {
			errorMessage = ("-cant't connect to directory because it doesn't exist");
		}
		else {
			if (newDirectory.compareTo(defaultDirectory.getAbsoluteFile()) < 0){
				errorMessage = ("-can't connect to directory because permission denied");
			}
			else {
				String newDirectoryStr = String.format("~%s", newDirectory.toString().substring(defaultDirectory.toString().length()));
				if (userLoggedIn) {
					errorMessage = (String.format("!changed working dir to %s", newDirectoryStr));
					currentDirectory = newDirectory;
				} 
				else {
					errorMessage = String.format("+directory ok, send account/password", newDirectory);
					sendResponseToClient(errorMessage);
					if (verifyIdentity()) {
						currentDirectory = newDirectory;
						errorMessage = String.format("!changed working dir to %s", newDirectoryStr);
					}
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
	private boolean verifyIdentity() throws Exception {
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
			//call the appropriate command
			if (cmd.equalsIgnoreCase("ACCT")) {
				ACCT();
			}
			else if (cmd.equalsIgnoreCase("PASS")) {
				PASS();
			}
			else {
				return false;
			}
			return true;
		}
		else {
			return false;
		}
	}
	
	
	/** 
	*	descripton	: 	KILL() deletes the file argument provided by the client in the current directory if the
	*					file is valid (i.e exists) and not protected
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	private void KILL() throws Exception {
		//System.out.println("KILL() called");
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
	*	descripton	: 	NAME() renames a file that exists in the current directory to the name specified by the
	*					name argument provided by the client
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	private void NAME() throws Exception {
		//System.out.println("NAME() called");
		String oldFilename = args;
		File oldFile = new File(currentDirectory.toString() + "/" + oldFilename);
		//check if the file requested exists
		if (!oldFile.isFile()) {
			errorMessage = String.format("-Can't find %s", oldFilename);
		}
		else {
			errorMessage = String.format("+File exists");
			sendResponseToClient(errorMessage);	
			if (TOBE()) {
				String newFilename = args;
				File newFile = new File(currentDirectory.toString() + "/" + newFilename);
				//check if a file with the same name already exists in the directory
				if (newFile.exists()) {
					errorMessage = String.format("-file wasn't renamed because new file name already exists");
				}
				else {
					//rename file
					if (oldFile.renameTo(newFile)) {
						errorMessage = String.format("+%s renamed to %s", oldFilename, newFilename);
					} else {
						errorMessage = String.format("-file wasn't renamed because it is protected");
					}
				}
			}
			else {
				errorMessage = String.format("-file wasn't renamed because command was not \"TOBE\"");
			}		
		}
		sendResponseToClient(errorMessage);			
	}	

	/** 
	*	descripton	: 	DONE() closes the server and informs the client of it by returning appropriate
	*					response code
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	private void DONE() throws Exception {
		//System.out.println("DONE() called");
		errorMessage = "+closing connection. bye";
		isConnected = false;
		sendResponseToClient(errorMessage);
		connectionSocket.close();
	}

	/** 
	*	descripton	: 	RETR() retrieves a specified file from the server and send it to the client
	*					given that the file is valid and returns response code accordingly 
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	private void RETR() throws Exception {
		//System.out.println("RETR() called");
		filename = args;
		Boolean skip = false; 
		file = new File(currentDirectory.toString() + "/" + filename);
		//check if specified file exists in the directory
		if (!file.isFile()) {
			errorMessage = ("-File doesn't exist");
			skip = true;
		}
		else {
			long fileSize = file.length();
			errorMessage = (String.format("%s", String.valueOf(fileSize)));	
		}
		sendResponseToClient(errorMessage);
		if (!skip) {
			if (SEND()) {
				sendFile(file);
				//no errorMessage is sent to the client if file is sent
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
	*	descripton	: 	SEND() checks if the client has sent a SEND command 
	*					
	*	args		: 	NONE
	*	returns		:	boolean true if client sent a SEND command
	**/		
	private boolean SEND() throws Exception {
		System.out.println("SEND() called");
		cmd = readClientResponse();		
		if (cmd.equalsIgnoreCase("SEND")) {
			return true;
		}	
		else {
			return false;
		}	
	}	

	/** 
	*	descripton	: 	sendFile() sends file over to the client when the client sends a
	*					RETR() command to the server with valid arguments
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
			System.out.println("FileNotFoundException");
		} catch (IOException e) {
			System.out.println("IOException");
		}
	}	
	
	/** 
	*	descripton	: 	STOP() checks if the client has sent a STOP command
	*	args		: 	NONE
	*	returns		:	boolean true if client sent a STOP command
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
	*	descripton	: 	TOBE() checks if the client has sent a TOBE command
	*	args		: 	NONE
	*	returns		:	boolean true if client sent a TOBE command
	**/		
	private boolean TOBE() throws Exception {
		command = readClientResponse();
		if (command != null) {
			String[] parts = command.split("\\ ",2);
			cmd = parts[0];
			args = parts[1];	
		}
		if (cmd.equalsIgnoreCase("TOBE")) {
			return true;
		}	
		else {
			return false;
		}
	}

	/** 
	*	descripton	: 	STOR() retrieves a file from the client and store it in the currentDirectory
	*					if the file sent is valid and the server has sufficient disk space
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
		File file = new File(currentDirectory.toString() + "/" + filename);
		System.out.println("File to be written = " + file.toPath().toAbsolutePath().toString());
		boolean overwrite = false;
		if (mode.equalsIgnoreCase("NEW")) {
			if (file.isFile()) {
				errorMessage = "-file exists but system doesn't support generations";
			}
			else {
				errorMessage = "+file does not exist, will create new file";
			}
		}
		else if (mode.equalsIgnoreCase("OLD")) {
			if (file.isFile()) {
				errorMessage = "+will write over old file";
				overwrite = true;
			} 
			else {
				errorMessage = "+will create new file";
			}
		}
		else if (mode.equalsIgnoreCase("APP")) {
			if (file.isFile()) {
				errorMessage = "+will append to file";
			} 
			else {
				errorMessage = "+will create file";
			}		
		}
		else {
			errorMessage = "-invalid mode";
		}
		sendResponseToClient(errorMessage);
		
		if (SIZE()) {
			long fileSize = Long.parseLong(args);
			//check if server has sufficient disk space
			try {
				if (!diskSpaceSufficient(fileSize)) {
					errorMessage = "-not enough room, don't send it";
				}
				else {
					errorMessage = "+ok, waiting for file";
				}

			} catch (IOException e) {
				errorMessage = "-error reading free space, don't send it";
			}
			sendResponseToClient(errorMessage);
			//receive file
			try {
				receiveFile(file, fileSize, overwrite);
				errorMessage = String.format("+Ssved %s to server", filename);
			} catch (IOException e) {
				e.printStackTrace();
				errorMessage = "-couldn't save because write access permissions";
			}			
		}
		else {
			errorMessage = "-invalid argument";
		}
		sendResponseToClient(errorMessage);
	}	

	/** 
	*	descripton	: 	receiveFile() receives file from the client when client sends over a 
	*					STOR command
	*	args		: 	NONE
	*	returns		:	NONE
	**/		
	private void receiveFile(File file, long fileSize, boolean overwrite) throws IOException {
		System.out.println("receiveFile() called");
		FileOutputStream fileOutStream = new FileOutputStream(file, overwrite);
		BufferedOutputStream bufferedOutStream = new BufferedOutputStream(fileOutStream);
		//read the file and write to buffer byte by byte
		for (int i = 0; i < fileSize; i++) {
			bufferedOutStream.write(dataInFromClient.read());
		}
		bufferedOutStream.close();
		fileOutStream.close();
	}

	/** 
	*	descripton	: 	SIZE() checks if the client has sent a SIZE command
	*					
	*	args		: 	NONE
	*	returns		:	boolean true if user has sent a SIZE command
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
	*	descripton	: 	diskSpaceSufficient() checks if the server has sufficient disk space when client
	*					attempts to send over a file using the STOR command
	*	args		: 	fileSize - size of file to be sent by client
	*	returns		:	boolean true if there is sufficient disk space
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
		try {	
			while(true) {
				if (!isConnected) {
					server.acceptConnection();
				}
				server.checkValidCommand(); 
			} 
		}
		catch (IOException e) {
			//if client got disconnected
			System.out.print("IOException has occured");
		}

	}
} 


