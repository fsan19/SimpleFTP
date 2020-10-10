package src.server;

import src.Connection;
import src.FileManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class CmdHandler {
    private Connection connection;
    private ArrayList<User> users;
    private User currentUser;
    private Account currentAccount;
    private FileManager fileManager;
    private String cdirPending;
    private String fileToRename;
    private File fileToSend;
    private boolean isFileToSend;
    private Long space;
    private String fileToReceive;
    private boolean isFileToReceive;
    private String sendFormat;
    private Long spaceLimit ;
    private boolean isApp;

    public CmdHandler(Connection connection, ArrayList<User> users) {
        this.connection = connection;
        this.users = users;
        this.currentUser = null;
        this.currentAccount = null;
        fileManager = new FileManager(connection,"/res/ServerFiles");
        this.cdirPending =" ";
        this.fileToRename =" ";
        this.fileToSend = null;
        // Set 5KB Limit
        this.space = Long.valueOf(5*1024);
        this.isFileToSend = false;
        // STOR
        this.fileToReceive = " ";
        this.spaceLimit = Long.valueOf(100000);

    }
    // Most commands cannot work without auth
    private boolean checkIfAuthenticated(){
        if(this.currentUser!=null){

            if (this.currentUser.isAdmin()==true) {
                return true;
            }else if (this.currentAccount!=null && this.currentAccount.isLoggedIn()==true){

                return true;
            }



        }
        return false;

    }
    // Error Handling if not authenticated
    private void handleAuthErrorMsg(){



        if (this.currentUser == null){
            this.connection.sendMsg("-No User-id selected");

        }else if(this.currentAccount == null){
            this.connection.sendMsg("-No Account Selected");

        }else if(!this.currentAccount.isLoggedIn()){
            this.connection.sendMsg("-Not Logged In");
        }


    }
    /*
    * ALL USER COMMANDS BELOW
    * NOTE CMDS FOR AUTH USER,ACCT,PASS
    * LIST,CDIR for Directory managment
    * TYPE for encoding
    * RETR for sending to client (coupled with SEND)
    * STOR for receiving from client (coupled with SIZE)
    * NAME,KILL for file management (TOBE coupled with NAME)
    * */

    private void USER(String[] msgParsed){
        int numArguments = msgParsed.length-1;

        // If no users in database return + and Personal Name
        if (this.users==null) {
            this.connection.sendMsg("+Feneel's Server");
            return;
        }

        // Can only login to 1 user at a time
        if (numArguments != 1 ){
            this.connection.sendMsg("-Invalid command");
            return;
        }

        // Check if user exists in records
        String username = msgParsed[1];
        boolean foundUser = false;
        for (User usr : this.users) {

            if ( usr.getUsername().equals(username) == true ){
                foundUser =true;
                this.currentUser = usr;

                if (this.currentAccount!=null) this.currentAccount.logout();
                this.currentAccount = null;
                break;
            }

        }
        if (foundUser==true){
            // Check if user is admin if yes
            if (this.currentUser.isAdmin()==true){

                this.connection.sendMsg("!"+this.currentUser.getUsername()+" logged in");
            }else{
                this.connection.sendMsg("+"+this.currentUser.getUsername()+" valid, send account and password");
            }

        }else{
            this.connection.sendMsg("-Invalid user-id, try again");
        }
    }
    private void ACCT(String[] msgParsed){


        int numArguments = msgParsed.length-1;
        // Can only login to 1 account at a time

        if (numArguments != 1 ){
            this.connection.sendMsg("-Invalid command");
            return;
        }
        if (this.currentUser==null){
            this.connection.sendMsg("-No User-id selected");
            return;
        }
        // No need for account if user is an admin
        if (this.currentUser.isAdmin() == true){
            this.connection.sendMsg("!Account valid, logged-in" );
            return;
        }
        // Find the account
        String account = msgParsed[1];
        Account acc = this.currentUser.findAccount(account) ;
        if (acc != null){
            this.currentAccount = acc;
            this.connection.sendMsg("+Account valid, send password");
        }else{
            this.connection.sendMsg("-Invalid account, try again");
        }
    }
    private void PASS(String[] msgParsed) {


        int numArguments = msgParsed.length-1;
        if (numArguments != 1 ){

            this.connection.sendMsg("-Invalid password, try again");
            return;
        }

        // Check if user is set, if not just return an invalid cmd
        if (this.currentUser==null){
            this.connection.sendMsg("-No User-id selected");
            return;
        }
        // If current account is specified accept password
        if (this.currentAccount != null){
            String password = msgParsed[1];
            if(this.currentAccount.login(password)==true){
                // Success
                if (!this.cdirPending.equals(" ")){
                    System.out.println("WARNING: Showing cdir cmd");
                    // Have already checked valid dir in CDIR cmd
                    this.fileManager.changeDirectory(this.cdirPending);
                    connection.sendMsg("!Changed working dir to "+this.cdirPending);
                    // Erase since command is executed
                    this.cdirPending = " ";
                    return;
                }
                this.connection.sendMsg("! Logged in");
            }else{
                // Fail
                this.connection.sendMsg("-Wrong password, try again");
            }
        }else{
            this.connection.sendMsg("+Account");
        }
    }
    private void LIST(String[] msgParsed) throws IOException {


        int numArguments = msgParsed.length-1;
        // Should not have more than 2 args
        if (numArguments > 2 || numArguments==0 ){
            this.connection.sendMsg("- Invalid Command");
            return;
        }
        // Check correct args provided
        String format = msgParsed[1];
        if (!format.equals("V") && (!format.equals("F"))){
            this.connection.sendMsg("- Invalid Command");
            return;
        }


        String directory = "";

        if (numArguments == 2){
            directory = msgParsed[2];

        }

        if(checkIfAuthenticated()==true){
            String resp = this.fileManager.listFiles(format,directory);
            this.connection.sendMsg(resp);

        }else{
            this.handleAuthErrorMsg();
        }
    }
    private void CDIR(String[] msgParsed) throws IOException {
        int numArguments = msgParsed.length-1;
        // Should have 1 arg
        if (numArguments != 1){
            this.connection.sendMsg("- Invalid Command");
            return;
        }
        String dir = msgParsed[1];

        if(!this.fileManager.checkIfDirectoryExists(dir) && dir.equals("root")==false){

            connection.sendMsg("-Cant connect to directory: Doesnt exist");
            return;
        }

        if(checkIfAuthenticated()==true) {

            this.fileManager.changeDirectory(dir);
            connection.sendMsg("!Changed working dir to "+dir);

        }else{

            if (this.currentUser==null){
                this.connection.sendMsg("-No User-id selected");
                return;
            }

            this.connection.sendMsg("+directory ok, send account/password");
            // To make sure it executes once logged in
            this.cdirPending = dir;

        }
    }
    private void TYPE(String[] msgParsed){
        int numArguments = msgParsed.length-1;
        // Should have 1 arg
        if (numArguments != 1){
            this.connection.sendMsg("- Invalid Command");
            return;
        }
        String type = msgParsed[1];

        if (!this.checkIfAuthenticated()){
            this.connection.sendMsg("-Permission Denied: Not Logged In");
            return;
        }

        if(this.fileManager.changeType(type)){
            this.connection.sendMsg("+Using "+ this.fileManager.getTypeName()+" mode");
        }else{
            this.connection.sendMsg("-Invalid type");
        }


    }
    private void RETR(String[] msgParsed){
        int numArguments = msgParsed.length-1;
        if (numArguments != 1){
            this.connection.sendMsg("- Invalid Command");
            return;
        }
        String fileName = msgParsed[1];

        if (!this.checkIfAuthenticated()){
            this.connection.sendMsg("-Permission Denied: Not Logged In");
            return;
        }

        if(this.fileManager.checkIfFileExists(fileName)){
            // Get the file size and send in Ascii
            this.connection.sendMsg(this.fileManager.checkFileSize(fileName));
            // Store the file to send it
            this.fileToSend = this.fileManager.retrieveFile(fileName);
            this.isFileToSend = true;

        }else{
            this.connection.sendMsg("-File doesn't exist");
        }


    }
    public void  KILL(String[] msgParsed){
        int numArguments = msgParsed.length-1;
        if (numArguments != 1){
            this.connection.sendMsg("- Invalid Command");
            return;
        }
        String fileName = msgParsed[1];

        if (!this.checkIfAuthenticated()){
            this.connection.sendMsg("-Permission Denied: Not Logged In");
            return;
        }

        if(this.fileManager.checkIfFileExists(fileName)){
            this.fileManager.deleteFile(fileName);
            this.connection.sendMsg("+"+fileName+" deleted");
        }else{
            this.connection.sendMsg("-Not deleted because:File doesn't exist");
        }


    }
    private void NAME(String[] msgParsed) {
        int numArguments = msgParsed.length-1;
        if (numArguments != 1){
            this.connection.sendMsg("- Invalid Command");
            return;
        }
        String fileName = msgParsed[1];

        if (!this.checkIfAuthenticated()){
            this.connection.sendMsg("-Permission Denied: Not Logged In");
            return;
        }

        if(this.fileManager.checkIfFileExists(fileName)){

            this.connection.sendMsg("+File exists");
            this.fileToRename = fileName;


        }else{
            this.connection.sendMsg("-Cant find"+fileName);
        }


    }
    public void  TOBE(String[] msgParsed){
        int numArguments = msgParsed.length-1;
        if (numArguments != 1){
            this.connection.sendMsg("- Invalid Command");
            return;
        }
        String fileName = msgParsed[1];
        if (!this.checkIfAuthenticated()){
            this.connection.sendMsg("-Permission Denied: Not Logged In");
            return;
        }

        if (!this.fileToRename.equals(" ")){
            if(this.fileManager.renameFile(this.fileToRename,fileName)){
                this.connection.sendMsg("+"+this.fileToRename+" renamed to "+ fileName);
            }else{
                this.connection.sendMsg("File wasnt renamed because: No modify permissions");
            }
            // Reset this
            this.fileToRename = " ";
        }else{
            this.connection.sendMsg("-Invalid Cmd");
        }


    }
    public void  SEND(String[] msgParsed) throws IOException {
        int numArguments = msgParsed.length-1;
        if (numArguments != 0){
            this.connection.sendMsg("- Invalid Command");
            return;
        }
        if (!this.checkIfAuthenticated()){
            this.connection.sendMsg("-Permission Denied: Not Logged In");
            return;
        }

        if (this.isFileToSend){
            this.connection.sendFile(this.fileToSend);
            this.isFileToSend = false;
        }else{
            this.connection.sendMsg("- Invalid Command");
        }
    }
    public void  STOP(String[] msgParsed){
        int numArguments = msgParsed.length-1;
        if (numArguments != 0){
            this.connection.sendMsg("- Invalid Command");
            return;
        }
        if (!this.checkIfAuthenticated()){
            this.connection.sendMsg("-Permission Denied: Not Logged In");
            return;
        }


        if (this.isFileToSend) {
            this.isFileToSend = false;
            this.connection.sendMsg("+ok,RETR aborted");
        }else{
            this.connection.sendMsg("- Invalid Command");
        }

    }
    public void  STOR(String[] msgParsed){
        int numArguments = msgParsed.length-1;
        if (numArguments != 2){
            this.connection.sendMsg("- Invalid Command");
            return;
        }
        if (!this.checkIfAuthenticated()){
            this.connection.sendMsg("-Permission Denied: Not Logged In");
            return;
        }
        String condition = msgParsed[1];
        String fileName = msgParsed[2];

        this.sendFormat= condition;
        this.isApp = false;

        if (condition.equals("NEW")){
            // Create a new file with the name
            if (this.fileManager.checkIfFileExists(fileName)){

                this.connection.sendMsg("-File exists, but system doesn't support generations");
            }
            else{
                this.connection.sendMsg(" +File does not exist, will create new file");
                this.fileToReceive = fileName;
                this.isFileToReceive = true;
            }
            return;
        }else if(condition.equals("OLD")){
            // If file exists write over it

            if (this.fileManager.checkIfFileExists(fileName)){

                this.connection.sendMsg("+Will write over old file");
            }
            // If not just make a new file
            else{
                this.connection.sendMsg("+Will create new file");

            }
            this.fileToReceive = fileName;
            this.isFileToReceive = true;

        }else if(condition.equals("APP")){
            if (this.fileManager.checkIfFileExists(fileName)){

                this.connection.sendMsg("+Will append to file");
            }
            // If not just make a new file
            else{
                this.connection.sendMsg("+Will create file");

            }
            this.isApp = true;
            this.fileToReceive = fileName;
            this.isFileToReceive = true;


        }else{
            this.connection.sendMsg("-Invalid Command");
        }


    }
    public void SIZE(String[] msgParsed){

        int numArguments = msgParsed.length-1;
        if (numArguments != 1){
            this.connection.sendMsg("- Invalid Command");
            return;
        }
        if (!this.checkIfAuthenticated()){
            this.connection.sendMsg("-Permission Denied: Not Logged In");
            return;
        }




        if (!this.isFileToReceive){
            this.connection.sendMsg("-Invalid Command");
            return;
        }
        Long size = Long.parseLong(msgParsed[1]);

        if (size+this.fileManager.getSpaceUsed()>this.spaceLimit){
            System.out.println("Space used: "+this.fileManager.getSpaceUsed());
            this.connection.sendMsg("-Not enough room, don't send it");
            this.isFileToReceive= false;
            return;
        }



        try {

            this.connection.sendMsg("+ok,waiting for file");

            byte[] bytes = this.connection.receiveFile(Integer.valueOf(msgParsed[1]),false);

            /*Only if command is APP*/
            if (isApp && this.fileManager.checkIfFileExists(this.fileToReceive) ){


                if ((this.fileManager.appendToFile(bytes,this.fileToReceive))){
                    this.connection.sendMsg("+saved "+this.fileManager.retrieveFile(fileToReceive).getPath());
                }else{
                    this.connection.sendMsg("-Error:file not saved,write to file failed");
                }
                this.isFileToReceive = false;
                return;
            }


            if(this.fileManager.createNewFile(bytes,this.fileToReceive)){

                Server.logger.info("Server sends a saved file msg");
                File file = this.fileManager.retrieveFile(fileToReceive);
                this.connection.sendMsg("+saved "+file.getPath());
            }else{

                Server.logger.info("Server sends a file not saved msg");
                this.connection.sendMsg("-Error:file not saved,write to file failed");
            }
            this.isFileToReceive = false;



        } catch (IOException e) {

            e.printStackTrace();
        }


    }

    /*Get raw message from server, parse it and service the command*/
    public int serviceCommand(String msg) throws IOException {

        int run = 1;

        /*Check the first word for the command*/
        String cmd ;
        String[] msgParsed = msg.split(" ");
        cmd = msgParsed[0];
        try{
            switch(cmd){
                case "USER":
                    USER(msgParsed);
                    break;
                case "ACCT":
                    ACCT(msgParsed);
                    break;
                case "PASS":
                    PASS(msgParsed);
                    break;
                case "DONE":
                    this.currentAccount=null;
                    this.currentUser=null;
                    run = -1;
                    break;
                case "LIST":
                    LIST(msgParsed);
                    break;
                case"CDIR":
                    CDIR(msgParsed);
                    break;
                case "TYPE":
                    TYPE(msgParsed);
                    break;
                case "RETR":
                    RETR(msgParsed);
                    break;
                case "KILL":
                    KILL(msgParsed);
                    break;
                case "NAME":
                    NAME(msgParsed);
                    break;
                case "TOBE":
                    TOBE(msgParsed);
                    break;
                case "SEND":
                    SEND(msgParsed);
                    break;
                case "STOP":
                    STOP(msgParsed);
                    break;
                case "STOR":
                    STOR(msgParsed);
                    break;
                case"SIZE":
                    SIZE(msgParsed);
                    break;
                default:
                    this.connection.sendMsg("-Invalid Cmd");
                    break;

            }
        }catch(Exception e){
            this.connection.sendMsg("Something went Wrong Error: "+e.toString());
        }

        return run;
    }
    /*Verify that command is valid*/
    public static boolean checkValidCommand(String msg ){

        if (msg==null) return false;

        if (msg.length() < 4) return false;

        String[] msgParsed = msg.split(" ");

        String cmd = msgParsed[0];

        if (cmd.length() != 4) return false;

        return true;


    }

}
