# compsys725 - assignment 1 (SFTP)
Computer Networks and Distributed Applications

- All commands have been implemented.
- Runs on localhost.
- Developed and testing using Ubuntu 16.04

### Instructions
#### Setup of server and client
1. Clone repo to preferred directory
2. Navigate to /assignment1/server and open a terminal
3. Run ```java serverTCP```
4. Navgate to /assignment1/client and open a terminal
5. Run ```java clientTCP```
#### Logging in
There are 6 accounts stored in the assignment1/server/userList.txt
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
username: xx
account: yy
password: zz
```
```
username: kk
account: ll
password: mm
```
```
username: admin
account: admin (not required)
password: admin (not required)
```
To log in, an existing account with the correct password is needed.
However, the User admin is able to log in without an account or a password.

#### Directory Structure
The server's default directory is ```assignment1/server```.
The client's default directory is ```assignment1/client```.
Files retrieved by the client from the server will be stored in the default directory 
and the files sent to the server by the client can be stored in either the default
directory or whichever subdirectory within its default directory.
Available subdirectories are ```assignment1/server/a``` and ```assignment1/server/b```.

### Commands

USER <username>
Checks whether username exists in the server records.
Grants immediate log in for User admin.
Example:
```
$ USER admin
from server: !admin logged in
$ USER upi123
from server: +user id valid, send account and password
```
ACCT <account>
Checks if the account corresponds to the provided username (for non-admin users)
Example:
```
$ ACCT upi123
from server: +account valid, send password
```
PASS <password>
Example:
```
$ PASS upi123
from server: !logged in
```
TYPE {A|B|C}
Changes the type of transmission byte stream. By default, type is binary.
User has to be logged in to be able to access this command.
Example:
- If user is logged in:
```
PASS <password>
$ PASS upi123
from server: !logged in
```
- If user is not logged in:
```
PASS <password>
$ PASS upi123
from server: !logged in
```
LIST {F|V} <directory>
