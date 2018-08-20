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
	public static DataOutputStream dataOutToClient; 
	public static BufferedInputStream dataInFromClient;
	
	private static final File defaultDirectory = FileSystems.getDefault().getPath("").toFile().getAbsoluteFile();
	private File currentDirectory = defaultDirectory;
	
	public void acceptConnection() throws Exception {
		System.out.println("server is running..."); 
		connectionSocket = welcomeSocket.accept();		
		inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream())); 
		outToClient = new DataOutputStream(connectionSocket.getOutputStream()); 
		dataOutToClient = new DataOutputStream(connectionSocket.getOutputStream());
		dataInFromClient = new BufferedInputStream(connectionSocket.getInputStream());
		if (outToLunch == true) {
			errorMessage = "-CS725 SFTP Service";
			outToClient.writeBytes(errorMessage + "\0");
			connectionSocket.close();
		}
		else {
			errorMessage = "+CS725 SFTP Service";
			outToClient.writeBytes(errorMessage + "\0");		
			System.out.println("a client is connected..."); 
		}
	}

	public String readMessage() {
		String sentence = "";
		int character = 0;

		while (true){
			try {
				character = inFromClient.read();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// '\0' detected, return sentence.
			if (character == 0) {
				break;
			}

			// Concatenate char into sentence.
			sentence = sentence.concat(Character.toString((char)character));
		}

		return sentence;
	}
	
	public void checkValidCommand() throws Exception {
		cmd = "";
		args = "";
		command = readMessage();
		
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
						outToClient.writeBytes(errorMessage + "\0");
						checkValidCommand();
					}
				}
				else {
					errorMessage = "-you are not logged in. please do so";
					outToClient.writeBytes(errorMessage + "\0");
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
			errorMessage = "-invalid user id, try again";
		}
		else {
			if (loggedInUsers.contains(currentUser)) {
				errorMessage = "!" + currentUser + " logged in";
				System.out.println("line223");
				userLoggedIn = true;
			}
			else {
				if (currentUser.equalsIgnoreCase("admin")) {
					errorMessage = "!" + currentUser + " logged in";
					System.out.println("line228");
					loggedInUsers.add(currentUser);
					userLoggedIn = true;
				}
				else {
					errorMessage = "+user id valid, send account and password";
				}	
			}
		
		}
		outToClient.writeBytes(errorMessage + "\0");		
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

		outToClient.writeBytes(errorMessage + "\0"); 
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

		outToClient.writeBytes(errorMessage + "\0"); 
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
		outToClient.writeBytes(errorMessage + "\0");
	}
	
	public void LIST() throws Exception {
		System.out.println("LIST() called");
		int strlen = args.length();
		String listingFormat = "";
		String dir = "";

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy kk:mm");
		File path = defaultDirectory;
		try {
			String[] parts = args.split("\\ ",2);
			listingFormat = parts[0];
			dir = parts[1];	
			
			path = new File(defaultDirectory.toString() + "/" + dir);			
		}
		catch (ArrayIndexOutOfBoundsException e) { 
			listingFormat = args;
			path = currentDirectory;
			
		}		
		File files[] = path.listFiles();	
		
		String outputList = "";
		if ((listingFormat.equalsIgnoreCase("v")) || (listingFormat.equalsIgnoreCase("f"))) {
			//System.out.println("path: " + path);		
			outputList = "+" + path + "\n./\n../\n"; 
			// Go through each file in the directory
			for (File f : files) {
				String filename = f.getName();

				// Append / to directories
				if (f.isDirectory()) {
					filename = filename.concat("/");
				}

				// Verbose, get information on the file
				if (listingFormat.equalsIgnoreCase("v"))  {
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
		}
		else {
			outputList = "-invalid file listing format";
		}

		outToClient.writeBytes(outputList + "\0");
	}

	public void CDIR() throws Exception {
		System.out.println("CDIR() called");
		String newDirectoryString = "";		
		int strlen = args.length();
		try {
			newDirectoryString = args.substring(0,strlen);
		}
		catch (StringIndexOutOfBoundsException e) {
			errorMessage = "-cant't connect to directory because it doesn't exist";
		}		
		// Directory is relative to root, set current dir to default, then append requested dir
		if (newDirectoryString.charAt(0) == '~') {
			newDirectoryString = newDirectoryString.replaceAll("~", "/");
			currentDirectory = defaultDirectory;
		}
		
		// Add / for directory
		if (newDirectoryString.charAt(0) != '/') {
			newDirectoryString = String.format("/%s", newDirectoryString);
		}
		
		if (newDirectoryString.charAt(newDirectoryString.length()-1) != '/') {
			newDirectoryString = newDirectoryString.concat("/");
		}

		File newDir = new File(currentDirectory.toString().concat(newDirectoryString)).toPath().normalize().toFile();
		System.out.println("1");
		//check if it is a valid directory
		if (!newDir.isDirectory()) {
			errorMessage = ("-Can't connect to directory because no such directory exists");
			System.out.println(newDir);
		}
		else {
			// Client trying access folder above allocated "root" folder.
			if (newDir.compareTo(defaultDirectory.getAbsoluteFile()) < 0){
				errorMessage = ("-Can't connect to directory because permission denied");
			}
			else {
				// Replace portion of the path to ~
				// Client doesn't need to know the absolute directory on the server
				String newDirReply = String.format("~%s", newDir.toString().substring(defaultDirectory.toString().length()));
				// Already logged in
				if ((loggedInUsers.contains(currentUser))) {
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
		outToClient.writeBytes(errorMessage + "\0");
	}

	public void KILL() throws Exception {
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
		outToClient.writeBytes(errorMessage + "\0");
	}
	
	public void NAME() throws Exception {
		System.out.println("KILL() called");
		
		String oldFilename = args;
		File oldFile = new File(currentDirectory.toString() + "/" + oldFilename);
		
		// Check if file exists
		if (!oldFile.isFile()) {
			errorMessage = String.format("-Can't find %s", oldFilename);
		}
		
		errorMessage = String.format("+File exists");
		
		
		// Wait for TOBE command
		if (TOBE()) {
			String newargs = args;
			// Get new filename from argument
			String newFilename = newargs.substring(5, newargs.length());
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
		outToClient.writeBytes(errorMessage + "\0");			
	}	
	
	public void DONE() throws Exception {
		System.out.println("DONE() called");
		errorMessage = "+bye";
		outToClient.writeBytes(errorMessage + "\0");
		connectionSocket.close();
	}
	
	public void RETR() throws Exception {
		System.out.println("RETR() called");
		
		/*		step 0:	Check file validity	*/
		
		String filename = args;
		
		// Specified file
		File file = new File(currentDirectory.toString() + "/" + filename);
		System.out.println("File of interest = " + file.toPath().toAbsolutePath().toString());
		
		// Specified file is not a file
		if (!file.isFile()) {
			errorMessage = ("-File doesn't exist");
		}
		outToClient.writeBytes(errorMessage + "\0");
		
		/*		step 1:	send file size	*/
		// Get file size
		long fileSize = file.length();
		errorMessage = (String.format(" %s", String.valueOf(fileSize)));

		if (SEND()) {
			/*		step 2:	send file		*/
			sendFile(file);
		}
		else if (STOP()) {
			errorMessage = ("+ok, RETR aborted");
		}
		else {
			errorMessage = ("-Invalid response");
		}
		outToClient.writeBytes(errorMessage + "\0");	
	}
	
	
	public boolean TOBE() throws Exception {
		command = readMessage();
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
	
	public boolean SEND() throws Exception {
		System.out.println("SEND() called");
		command = readMessage();
		if (command != null) {
			String[] parts = command.split("\\ ",2);
			cmd = parts[0];
			args = parts[1];	
		}
		if (cmd.equalsIgnoreCase("SEND")) {
			return true;
		}	
		else {
			return false;
		}	
	}
	
	public boolean STOP() throws Exception {
		System.out.println("STOP() called");
		command = readMessage();
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
	
	public boolean sendFile(File file) {
		System.out.println("sendFile() called");
		byte[] bytes = new byte[(int) file.length()];

		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bufferedInStream = new BufferedInputStream(new FileInputStream(file));
			
			System.out.println("Total file size to read (in bytes) : " + fis.available());

			int content = 0;
			
			// Read and send file until the whole file has been sent
			while ((content = bufferedInStream.read(bytes)) >= 0) {
				dataOutToClient.write(bytes, 0, content);
			}
			
			bufferedInStream.close();
			fis.close();
			dataOutToClient.flush();
	
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		return true;
	}	

	public void STOR() throws Exception {
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
		System.out.println("File to be written = " + file.toPath().toAbsolutePath().toString());

		boolean overwrite = false;
		
		if (mode.equalsIgnoreCase("NEW")) {
			if (file.isFile()) {
				errorMessage = "-File exists, but system doesn't support generations";
			}
			errorMessage = "+File does not exist, will create new file";
		}
		else if (mode.equalsIgnoreCase("OLD")) {
			if (file.isFile()) {
				errorMessage = "+Will write over old file";
				overwrite = true;
			} else {
				errorMessage = "+Will create new file";
			}
		}
		else if (mode.equalsIgnoreCase("APP")) {
			if (file.isFile()) {
				errorMessage = "+Will append to file";
			} else {
				errorMessage = "+Will create file";
			}		
		}
		else {
			errorMessage = "-Invalid mode";
		}
		
		outToClient.writeBytes(errorMessage + "\0");
		
		
		/*		step 2: Check file size	*/
		if (SIZE()) {
			long fileSize = Long.parseLong(args);
		
			// File doesn't fit on server
			try {
				if (!diskSpaceSufficient(fileSize)) {
					errorMessage = "-Not enough room, don't send it";
				}

			} catch (IOException e) {
				errorMessage = "-Error reading free space, don't send it";
			}

			errorMessage = "+ok, waiting for file";

			/*		step 3: receive file	*/

			// Receive the file
			try {
				receiveFile(file, fileSize, overwrite);
			} catch (IOException e) {
				e.printStackTrace();
				errorMessage = "-Couldn't save because write access permissions";
			}			
		}
		else {
			errorMessage = "-Invalid argument";
		}
		

		errorMessage = String.format("+Saved %s", filename);
	}	

	public void receiveFile(File file, long fileSize, boolean overwrite) throws IOException {
		FileOutputStream fileOutStream = new FileOutputStream(file, overwrite);
		BufferedOutputStream bufferedOutStream = new BufferedOutputStream(fileOutStream);

		// Read and write for all bytes
		for (int i = 0; i < fileSize; i++) {
			bufferedOutStream.write(dataInFromClient.read());
		}

		bufferedOutStream.close();
		fileOutStream.close();
	}
	
	public boolean SIZE() throws Exception {
		System.out.println("SIZE() called");
		command = readMessage();
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

	public boolean diskSpaceSufficient(long fileSize) throws IOException {
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
		loggedInUsers = new ArrayList<String>();
	
		//setup of welcoming socket
		welcomeSocket = new ServerSocket(1500); 
		server.acceptConnection();
			
		while(true) {
			server.checkValidCommand(); 
		} 

	}
} 


