# compsys725 - assignment 1 (SFTP)
Computer Networks and Distributed Applications

- All commands have been implemented.
- Runs on localhost.
- Developed and tested using Ubuntu 16.04

### Instructions

#### Setup of server and client
1. Clone repo to preferred directory
2. Navigate to /assignment1/server and open a terminal
3. Run ```java serverTCP```
4. Navgate to /assignment1/client and open a terminal
5. Run ```java clientTCP```

#### Logging in
There are 4 accounts stored in the assignment1/server/userList.txt
```
username: aA
account: bB
password: cC
```
```
username: upi123
account: upi123
password: upi123
```
```
username: aa
account: bb
password: cc
```
```
username: admin
account: admin (not required)
password: admin (not required)
```
To log in, an existing account with the correct password is needed.
However, admin is able to log in without an account or a password.

### Directory Structure
The server's default directory is ```assignment1/server```.<br>
The client's default directory is ```assignment1/client```.<br>
Files retrieved by the client from the server will be stored in the default directory.<br> 
The files sent to the server by the client can be stored in either the default
directory or whichever subdirectory within its default directory.

Available subdirectories are ```assignment1/server/a``` and ```assignment1/server/b```.

### Commands

#### ```USER <username>```<br>
Checks whether username exists in the server records.<br>
Grants immediate log in for User admin.<br>
Example:
```
$ USER admin
from server: !admin logged in
$ USER upi123
from server: +user id valid, send account and password
```

#### ```ACCT <account>```<br>
Checks if the account corresponds to the provided username (for non-admin users)<br>
If password has been provided, account corresponding to the username and password
will be logged in without having to provide the password again.

```
$ ACCT upi123
from server: +account valid, send password
```

#### ```PASS <password>```<br>
Checks if the password corresponds to the provided account (for non-admin users)<br>
Password can also be provided before providing the account.

- If user has provided account
```
$ PASS upi123
from server: !logged in
```
- If user has yet to provide an account
```
$ PASS upi123
from server: +send account
```

#### ```TYPE {A|B|C}```<br>
Changes the type of transmission byte stream. By default, type is binary.<br>
Requires user to be logged in.

- If user is logged in:
```
$ TYPE a
from server: +using Ascii mode
```
- If user is not logged in:
```
$ TYPE A
from server: -you are not logged in. please do so
```

#### ```LIST {F|V} <directory>```<br>
Lists all the files and folders in the specified <directory>. <br>
If directory is not specified, a list of the files and folders within the current
directory is returned. Requires user to be logged in.

If {F} is provided by the client, the listing will only include the filenames.<br><br>

If {V} is provided by the client, the listing will include the details relating 
to the file which are the filenames, last modified time/date, filesize in bytes 
and the owner of the file.

- If user did not specify a directory:
```
$ LIST f
from server: 
+
./
../
a/ 
server.java 
b/ 
serverTCP.class 
userList.txt 
```
```
$ LIST v
from server: 
+
./
../
a/                             22/08/2018 02:22           4096              syamira 
server.java                    22/08/2018 01:41          28184              syamira 
b/                             22/08/2018 01:58           4096              syamira 
serverTCP.class                22/08/2018 16:19          15397              syamira 
userList.txt                   22/08/2018 02:13             76              syamira 
```
- If user spcified a directory:
```
$ LIST f a
from server: 
+
./
../
p3.jpg 
p1.jpg 
p0.jpg 
p2.jpg 

```
```
$ LIST v a
from server: 
+
./
../
p3.jpg                         21/08/2018 22:49           9122              syamira 
p1.jpg                         21/08/2018 07:04         186623              syamira 
p0.jpg                         21/08/2018 16:56         478525              syamira 
p2.jpg                         21/08/2018 16:47         628817              syamira 

```

#### ```CDIR <new-directory>```<br>
Changes the current working directory to the specified <new-directory>.<br>
If user is not logged in, their account and password will be needed before
being able to change the directory. Directory can only be changed to the 
subdirectories within the default one. Requires user to be logged in.

- If user is logged in:
```
$ CDIR a
from server: !changed working directory to ~/a

```
- If user is not logged in:
```
$ CDIR a
from server: -you are not logged in. please do so

```
- If directory is invalid:
```
$ CDIR k
from server: -cant't connect to directory because it doesn't exist
```

#### ```KILL <filename>```<br>
Deletes the specified <filename> in the current directory if the file exists and is
not protected. Requires user to be logged in.
	
- If file can be deleted:
```
$ KILL t1.txt
from server: +t1.txt deleted

```
- If file cannot be deleted:
```
$ KILL tp.txt
from server: -no such file exist in the directory

```

#### ```NAME <filename>```<br>
Renames specified <filename> if it exists in the current directory. <br>
If server sends a '+', user needs to reply with a TOBE <filename>.
Requires user to be logged in.
	
```
$ NAME t5.txt
from server: +File exists

```

- ```TOBE <filename>```<br>
Provides the server with the <filename> following the NAME command.<br>
Should only be provided when user has received a '+' from server after
sending a NAME command.
	
```
$ TOBE t1.txt
from server: +t5.txt renamed to t1.txt

```

#### ```DONE```
Disconnects the client from the server.

```
$ DONE
from server: +closing connection. bye
```

#### ```RETR <filename>```<br>
Retrieves the specified <filename> from the server if the file fits in the client's
system and if the file is in the current directory (subdirectories are not allowed).
If the server has replied with the size of the file requested, user needs to reply 
with a SEND command if they wish to proceed. Requires user to be logged in.

Retrieve p2.jpg from server:
```
$ RETR p2.jpg
from server: 628817

```
Send a SEND command to proceed with file transfer. <br>
The client will be asked (by the client side, not server)
to enter a filename for the incoming file.
```
$ SEND
save file as: 

```
```
$ p2.jpg
p2.jpg has been saved to the directory
```


#### ```STOR {NEW|OLD|APP} <filename>```<br>
Sends a file to the server's current directory with a specified {NEW|OLD|APP} 
mode. Requires user to be logged in.

If {NEW} is provided by the client, the server will create a new file if the file
is yet to exist. <br><br>

If {OLD} is provided by the client, the server will overwrite the existing file if it 
already exists in the current directory. If it does not exist, a new file will be
created.<br><br>

If {APP} is provided by the client, the server will append to the existing file if it
already exists in the current directory. If it does not exist, a new file will be
created.<br><br>

Send a cat.jpg to the server:
The client will be asked (by the client side, not server)
to enter the filename of the file to be sent and the size of the file will be returned 
so that the user do not need to check for the file size manually for the SIZE command.
```
$ STOR NEW cat.jpg
enter filename to be sent to the server: 

```
The client will provide the file size and it can be entered following the SEND command
sent to the server
```
$ p2.jpg
filesize : 628817
from server: +file does not exist, will create new file

```
The server returns the status of the file
```
$ SIZE 628817
from server: +ok, waiting for file
from server: +saved cat.jpg to server

```
