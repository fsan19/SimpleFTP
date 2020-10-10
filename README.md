# Simple FTP
Author: Feneel Sanghavi  <br>

# OS and Tools
Mac OS 64 bit and IntelliJ IDE with Oracle JDK 11.08

# General Information
Details of SFTP (Simple File Transfer Protocol) and valid commands can be found in   
<https://tools.ietf.org/html/rfc913>

## Excerpt from the document
SFTP is a simple file transfer protocol, easier than FTP to implement and more useful than TFTP
 
It supports user control, file transfers, directory listing, directory changing, file
renaming and deleting.
                   
```
  <command> : = <cmd> [<SPACE> <args>] <NULL>
  <cmd> : =  USER ! ACCT ! PASS ! TYPE ! LIST ! CDIR
             KILL ! NAME ! DONE ! RETR ! STOR
  <response> : = <response-code> [<message>] <NULL>
  <response-code> : =  + | - |   | !
```

SFTP is used by opening a TCP connection to the remote hosts' SFTP
port (115 decimal).  You then send SFTP commands and wait for
replies.  SFTP commands sent to the remote server are always 4 ASCII
letters (of any case) followed by a space, the argument(s), and a
<NULL>.  The argument can sometimes be null in which case the command
is just 4 characters followed by <NULL>.  Replies from the server are
always a response character followed immediately by an ASCII message
string terminated by a <NULL>.  A reply can also be just a response
character and a <NULL>.

# Port and IP Address
Port number is 6789 and localhost as destination IP address for client

# Folder Info

res: Contains ServerFiles which hosts the server files
	       and ClientFiles which hosts the client's files
	       
src: Contains source code

# Compile Instructions

For command line:  
`javac src/*.java src/server/*.java src/client/*.java`

# Run Instructions

**Note:-** Run Server and Client on different terminals

`java -cp ./ src.server.Server`

`java -cp ./ src.client.Client`


# Test Cases for Commands
First command must be ```USER <username>``` 
Example:  
```
Input command: PASS random
FROM SERVER: -No User-id selected
```

Account is required for all commands (except CDIR)  after a user-id is specified
```
Input command: LIST F
FROM SERVER: -No Account Selected
```
Invalid Command Response (Commands must be 4 letters and only the specified ones)

```
Input command: LOL
FROM SERVER: - Invalid Command
```

```
Input command: TEST
FROM SERVER: -Invalid Cmd
```

### USER
The available users are **UoA and admin**  
Credentials file is in res folder (credentials.csv)

```
Input command: USER admin 
FROM SERVER : !admin logged in
```

```
Input command: USER uoa
FROM SERVER: +UoA valid, send account and password
```

```
Input command: USER rip 
FROM SERVER: -Invalid user-id, try again
```

### ACCT



```
Input command: ACCT fsan110
FROM SERVER: +Account valid, send password

```

```
Input command: ACCT rip
FROM SERVER: -Invalid account, try again
```


### PASS

The ACCT is fsan110
```
Input command: PASS feneel
FROM SERVER: ! Logged in

```
No account is selected. Will ask for an account
```
Input command: USER UoA
FROM SERVER: +UoA valid, send account and password


Input command: PASS feneel
Server response: +Account
```

Account is selected, try with incorrect/correct password


```
Input command: ACCT fsan110
FROM SERVER: +Account valid, send password

```
Wrong Password
```
Input command: PASS wrong
FROM SERVER: -Wrong password, try again
```
Correct Password
```
Input command: PASS feneel
FROM SERVER: ! Logged in
```

### TYPE
```
Input command: TYPE A
FROM SERVER: +Using Ascii mode
```

```
Input command: TYPE B
FROM SERVER: +Using Binary mode
```

```
Input command: TYPE C
FROM SERVER: +Using Continous mode
```

```
Input command: TYPE D
FROM SERVER: -Invalid type
```

### LIST 

LIST F/V DIR

If no directory given will just list current directory 


Testing for root directory 

  Ignore .DS_STORE it is automatically generated in MAC OS directories)
  Directories are listed as dirname/ and files as file.format     
```
Input command: LIST F
FROM SERVER: +/Users/Feneel/IdeaProjects/A1_725/res/ServerFiles
.DS_Store
client1.txt
deleteThis.txt
dir1/
dir2/
Server1.txt
```
Verbose gives attributes of files and directories as well
```
Input command: LIST V
FROM SERVER: +/Users/Feneel/IdeaProjects/A1_725/res/ServerFiles
.DS_Store
File Size (Bytes):8196
File Created at:2020-08-31T09:45:53Z
File Last Modified at:2020-09-01T06:32:41Z
File Last Accessed at:2020-08-31T09:46:45Z
deleteThis.txt
File Size (Bytes):0
File Created at:2020-08-27T02:01:51Z
File Last Modified at:2020-08-27T02:01:51Z
File Last Accessed at:2020-09-02T07:22:26Z
dir1/
Directory Size:136
Directory Created at:2020-08-25T07:43:04Z
Directory Last Modified at:2020-08-31T09:46:10Z
Directory Last Accessed at:2020-09-02T07:18:54Z
dir2/
Directory Size:136
Directory Created at:2020-08-25T08:03:19Z
Directory Last Modified at:2020-08-31T09:46:15Z
Directory Last Accessed at:2020-09-02T06:46:54Z
Server1.txt
File Size (Bytes):43
File Created at:2020-08-24T09:22:15Z
File Last Modified at:2020-08-24T09:22:26Z
File Last Accessed at:2020-09-02T07:22:26Z

```


LIST from a certain directory 
```
Input command: LIST F dir1
FROM SERVER: +/Users/Feneel/IdeaProjects/A1_725/res/ServerFiles/dir1
.DS_Store
dir1.txt


```

```
Input command: LIST V dir1
Server response: 
FROM SERVER: +/Users/Feneel/IdeaProjects/A1_725/res/ServerFiles/dir1
.DS_Store
File Size (Bytes):6148
File Created at:2020-08-31T09:46:10Z
File Last Modified at:2020-08-31T09:46:15Z
File Last Accessed at:2020-08-31T09:46:10Z
dir1.txt
File Size (Bytes):11
File Created at:2020-08-26T09:04:30Z
File Last Modified at:2020-08-26T09:04:49Z
File Last Accessed at:2020-09-01T03:48:21Z
```

LIST from root directory
```
Input command: LIST F root
FROM SERVER: +/Users/Feneel/IdeaProjects/A1_725/res/ServerFiles
.DS_Store
client1.txt
deleteThis.txt
dir1/
dir2/
Server1.txt
```
```
Input command: LIST V root
FROM SERVER: +/Users/Feneel/IdeaProjects/A1_725/res/ServerFiles
.DS_Store
File Size (Bytes):8196
File Created at:2020-08-31T09:45:53Z
File Last Modified at:2020-09-01T06:32:41Z
File Last Accessed at:2020-08-31T09:46:45Z
deleteThis.txt
File Size (Bytes):0
File Created at:2020-08-27T02:01:51Z
File Last Modified at:2020-08-27T02:01:51Z
File Last Accessed at:2020-09-02T07:22:26Z
dir1/
Directory Size:136
Directory Created at:2020-08-25T07:43:04Z
Directory Last Modified at:2020-08-31T09:46:10Z
Directory Last Accessed at:2020-09-02T07:18:54Z
dir2/
Directory Size:136
Directory Created at:2020-08-25T08:03:19Z
Directory Last Modified at:2020-08-31T09:46:15Z
Directory Last Accessed at:2020-09-02T06:46:54Z
Server1.txt
File Size (Bytes):43
File Created at:2020-08-24T09:22:15Z
File Last Modified at:2020-08-24T09:22:26Z
File Last Accessed at:2020-09-02T07:22:26Z

```

### CDIR

CDIR if NOT Logged in (command is implemented is executed upon login)

```
Input command: CDIR dir1
FROM SERVER: +directory ok, send account/password
Input command: ACCT fsan110
FROM SERVER: +Account valid, send password
Input command:PASS feneel
FROM SERVER: !Changed working dir to dir
```

CDIR if directory doesnt exist
```
Input command: CDIR dir3
FROM SERVER: -Cant connect to directory: Doesnt exist
```

CDIR to root directory
```
Input command: CDIR root
FROM SERVER: !Changed working dir to root
```


### KILL 

Kill a file in root directory (must be in root folder)
```

Input command: KILL deleteThis.txt
FROM SERVER: +deleteThis.txt deleted
```
Try to delete again wont work as already deleted
```
Input command: KILL deleteThis.txt
FROM SERVER: -Not deleted because:File doesn't exist
```

### NAME

Rename a file in dir1 (must CDIR to that dir first)

```
Input command: NAME dir1.txt
FROM SERVER: +File exists

Input command: TOBE d.txt
FROM SERVER: +dir1.txt renamed to d.txt

```
When file doesnt exist (in this case TOBE wont work as NAME command is terminated if file is not found)
```
Input command: NAME lol.txt 
FROM SERVER: -Cant findlol.txt

Input command: TOBE exampleNew.txt
Server response: -Invalid Command
```

### DONE

User does not need to be logged in 
The q command is to end the program as well
```
DONE
FROM SERVER: Thanks for using Feneel's Server, Goodbye.
Give me a sentence or press q to quit:
q
Sep 02, 2020 10:28:41 PM src.Connection closeConnection
INFO: Closed Connection

```
Server will become ready for handshaking to other clients. (Can relaunch client if desired)

### RETR

Retrieve file from root directory Server (sets timeout of 10s)
  
```
Input command: RETR Server1.txt
FROM SERVER: 43
Input command: SEND

(client prints log once it recevies succesfully)
INFO: Sucessfully Retrieved File

```
After RETR we can choose to STOP (will terminate the RETR command)

  Stop the server from sending
```
Input command: RETR Server1.txt
FROM SERVER: 43

Input command: STOP
FROM SERVER: +ok,RETR aborted

```
  
```
Input command: RETR lol.txt
FROM SERVER: -File doesn't exist
```

### STOR
Storing file from client to server

##### OLD

File doesnt exist has to create new
```
Input command: STOR OLD client1.txt
FROM SERVER: +Will create new file
Sep 02, 2020 8:31:13 PM src.client.Client main
INFO: Is send is true
Give me a sentence or press q to quit:
SIZE
Sep 02, 2020 8:31:19 PM src.client.Client main
INFO: SEND command msg to server:SIZE 44
FROM SERVER: +ok,waiting for file
FROM SERVER: +saved /Users/Feneel/IdeaProjects/A1_725/res/ServerFiles/client1.txt
```
File overwrites existing file in server
```
Input command: STOR OLD client1.txt
FROM SERVER: +Will write over old file
Sep 02, 2020 8:31:42 PM src.client.Client main
INFO: Is send is true
Give me a sentence or press q to quit:
SIZE
Sep 02, 2020 8:31:45 PM src.client.Client main
INFO: SEND command msg to server:SIZE 44
FROM SERVER: +ok,waiting for file
FROM SERVER: +saved /Users/Feneel/IdeaProjects/A1_725/res/ServerFiles/client1.txt
```
Not enough space in server for pdf file(terminates STOR command)
```
Input command: STOR OLD a1.pdf
Server response: +Will create new file
Input command: SIZE
Server response: -Not nough room, don't send it
```
If file does not exist in client (it throws an error and wont send command to client)
```
Input command: STOR OLD a.bc
SEVERE: Client has entered incorrect format,file not found

```

##### APP
Delete the existing client1.txt file (we created it in STOR OLD Tests) and run the following
```

Input command: STOR APP client1.txt
FROM SERVER: +Will create file

Input command: SIZE
FROM SERVER: +ok,waiting for file
FROM SERVER: +saved /Users/Feneel/IdeaProjects/A1_725/res/ServerFiles/client1.txt

```
Append to existing file

```
Input command: STOR APP client1.txt
FROM SERVER: +Will append to file

Input command: SIZE
FROM SERVER: +ok,waiting for file
FROM SERVER: +saved /Users/Feneel/IdeaProjects/A1_725/res/ServerFiles/client1.txt

```
Try with pdf file that is too big

```
Input command: STOR APP a1.pdf
FROM SERVER: +Will create file
Input command: SIZE
FROM SERVER: -Not enough room, don't send it
```
If file does not exist in client (it throws an error and wont send command to client)
```
Input command: STOR APP a.bc
SEVERE: Client has entered incorrect format,file not found

```
##### NEW
Delete the existing client1.txt file (we created it in STOR APP Tests) and run the following
```

Input command: STOR NEW client1.txt
FROM SERVER:  +File does not exist, will create new file
Input command: SIZE
FROM SERVER: +ok,waiting for file
FROM SERVER: +saved /Users/Feneel/IdeaProjects/A1_725/res/ServerFiles/client1.txt
```
File is already created, new generations are not allowed in this system (will terminate STOR command)
```
Input command: STOR NEW client1.txt
FROM SERVER: -File exists, but system doesn't support generations
```


```
Try with pdf file that is too big

```
Input command: STOR NEW a1.pdf
FROM SERVER:  +File does not exist, will create new file
Input command: SIZE
FROM SERVER: -Not enough room, don't send it
```
If file does not exist in client (it throws an error and wont send command to client)
```
Input command: STOR NEW a.bc
SEVERE: Client has entered incorrect format,file not found

```