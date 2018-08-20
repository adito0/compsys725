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
			//TODO: remove the square brackets loool
			String[] parts = command.split("\\[ ",2);
			cmd = parts[0];
			String a = parts[1];
			String[] b = a.split("\\]",2);
			args = b[0];

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
		
		File path = defaultDirectory;
		path = new File(defaultDirectory.toString() + "/" + dir);
		File files[] = path.listFiles();
		//System.out.println("path: " + path);		
		String outputList = "+" + path + "\n./\n../\n"; 
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
		
		outToClient.writeBytes(outputList + "\0");

	}

	public void CDIR() throws Exception {
		System.out.println("CDIR() called");
		
		String newDirName = "";		
		int strlen = args.length();
		try {
			newDirName = args.substring(0,strlen);
		}
		catch (StringIndexOutOfBoundsException e) {
			errorMessage = "-cant't connect to directory because it doesn't exist";
		}
		
		// Directory is relative to root, set current dir to default, then append requested dir
		if (newDirName.charAt(0) == '~') {
			newDirName = newDirName.replaceAll("~", "/");
			currentDirectory = defaultDirectory;
		}
		
		// Add / for directory
		if (newDirName.charAt(0) != '/') {
			newDirName = String.format("/%s", newDirName);
		}
		
		if (newDirName.charAt(newDirName.length()-1) != '/') {
			newDirName = newDirName.concat("/");
		}
		
		File newDir = new File(currentDirectory.toString().concat(newDirName)).toPath().normalize().toFile();
		System.out.println(newDir); 
		// Client trying access folder above allocated "root" folder.
		if (newDir.compareTo(defaultDirectory.getAbsoluteFile()) < 0){
			outToClient.writeBytes("-Can't connect to directory because permission denied");
		}
		
		// Specified directory is not a directory
		if (!newDir.isDirectory()) {
			outToClient.writeBytes("-Can't connect to directory because no such directory exists");
		}
		
		// Replace portion of the path to ~
		// Client doesn't need to know the absolute directory on the server
		String newDirReply = String.format("~%s", newDir.toString().substring(defaultDirectory.toString().length()));
		
		// Already logged in
		if ((loggedInUsers.contains(currentUser))) {
			currentDirectory = newDir;
			outToClient.writeBytes(String.format("!Changed working dir to %s", newDirReply));
		// Need to log in
		} else {
			outToClient.writeBytes(String.format("+directory ok, send account/password", newDir));
			
			// Run CDIR authentication procedure
			/**if (cdirAuthenticate()) {
				currentDirectory = newDir;
				outToClient.writeBytes(String.format("!Changed working dir to %s", newDirReply));
			}**/
		}
	}	

	public void KILL() throws Exception {
		System.out.println("KILL() called");
		String filename = args;
		
//			if (filename.contains("^[<>|:&]+$")) {
//				errorMessage = "-Not deleted because filename contains reserved symbols");
//			}
		
		Path path = new File(currentDirectory.toString().concat("/").concat(filename)).toPath();
		
		// Delete the file
		try {
			Files.delete(path);
			errorMessage = String.format("+%s deleted", filename);
			
		} catch (NoSuchFileException x) {
		    errorMessage = "-Not deleted because no such file exists in the directory";
		    
		} catch (IOException x) {
		    errorMessage = "-Not deleted because it's protected";
		}
		outToClient.writeBytes(errorMessage + "\n");
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
		outToClient.writeBytes(errorMessage + "\n");			
	}	
	
	public void DONE() throws Exception {
		System.out.println("DONE() called");
		errorMessage = "+bye";
		outToClient.writeBytes(errorMessage + "\n");
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
		outToClient.writeBytes(errorMessage + "\n");
		
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
		outToClient.writeBytes(errorMessage + "\n");	
	}
	
	
	public boolean TOBE() throws Exception {
		command = inFromClient.readLine();
		if (command != null) {
			String[] parts = command.split("\\[ ",2);
			cmd = parts[0];
			String a = parts[1];
			String[] b = a.split("\\]",2);
			args = b[0];	
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
		command = inFromClient.readLine();
		if (command != null) {
			String[] parts = command.split("\\[ ",2);
			cmd = parts[0];
			String a = parts[1];
			String[] b = a.split("\\]",2);
			args = b[0];	
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
		command = inFromClient.readLine();
		if (command != null) {
			String[] parts = command.split("\\[ ",2);
			cmd = parts[0];
			String a = parts[1];
			String[] b = a.split("\\]",2);
			args = b[0];	
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
			String[] parts = args.split("\\[ ",2);
			mode = parts[0];
			String a = parts[1];
			String[] b = a.split("\\]",2);
			filename = b[0];	
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
		
		outToClient.writeBytes(errorMessage + "\n");
		
		
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
		command = inFromClient.readLine();
		if (command != null) {
			String[] parts = command.split("\\[ ",2);
			cmd = parts[0];
			String a = parts[1];
			String[] b = a.split("\\]",2);
			args = b[0];	
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


