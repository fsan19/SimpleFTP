package src.client;

import src.Connection;
import src.FileManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    static Connection connection;
    static Logger logger = Logger.getLogger(Client.class.getName()) ;
    static FileManager fileManager ;
    static int fileSize;
    // RETR function
    static String fileToRetrieve;
    static boolean isRetreive = false;
    // STOR function
    static String fileToSend;
    static boolean isSend = false;

    public static void main(String argv[]) throws Exception {
        fileToRetrieve = "retreivedFile";

        logger.setLevel(Level.SEVERE);

        // File manager
        fileManager = new FileManager(connection,"/res/ClientFiles");
        InetAddress localhost = InetAddress.getLocalHost();
        String hostname = localhost.getHostName();
        int hostPortNo = 6789;
        // connect to the Server welcome socket
        // returns the socket for data exchange after handshaking
        Socket connectionSocket = new Socket(hostname,hostPortNo);
        logger.info("Handshaking Done Success");
        connection = new Connection(connectionSocket);
        // Input from user
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

        while(true){
            logger.info("Check response from server");
            String resp = connection.receiveMsg();
            if (resp.contains("Out to Lunch")==true){
                logger.info("Server Out to Lunch");
                connection.closeConnection();
            }else{
                while (true){

                    try {
                        System.out.println("Give me a sentence or press q to quit:");

                        String msg = inFromUser.readLine();

                        // Make system exit to quit server
                        if(msg.contains("q")==true){
                            break;
                        }else if(msg.contains("STOR")) {
                            // get filename
                            String[] msgParsed = msg.split(" ");
                            try{
                                fileToSend = msgParsed[2];
                                if (!fileManager.checkIfFileExists(fileToSend)){
                                    logger.severe("Client has entered incorrect format,file not found");
                                    continue;
                                }
                            }catch(Exception e){
                                logger.severe("Client has entered incorrect format,file not found");
                                continue;
                            }



                        }else if(msg.contains("SIZE") && isSend){
                            // append filesize and send to server
                            Long size = fileManager.getFileSize(fileToSend);
                            msg= msg.strip()+ (" "+(Long.toString(size)) );
                            logger.info("SEND command msg to server:"+msg);

                        }

                        connection.sendMsg(msg);

                        if (msg.contains("SEND") && isRetreive){
                            byte[] response = connection.receiveFile(fileSize,true);
                            if(fileManager.createNewFile(response,fileToRetrieve)){

                               System.out.println("INFO:Sucessfully Retrieved File");
                            }else{

                                System.out.println("INFO:File Could Be Retrieved");
                            }


                        }else if (msg.contains("STOP")){
                            String response = connection.receiveMsg();
                            System.out.println("FROM SERVER: " + response);
                            isRetreive = false;

                        }
                        else if(msg.contains("RETR")){

                            String response = connection.receiveMsg();
                            try{
                                String[] msgParsed = msg.split(" ");
                                fileToRetrieve = msgParsed[1];
                                fileSize = Integer.parseInt(response);
                                isRetreive = true;
                            }catch(Exception e){
                                logger.warning("Unexpected format from Server ");
                            }

                            System.out.println("FROM SERVER: " + response);
                        }else if(msg.contains("SIZE") && isSend){

                            File file = fileManager.retrieveFile(fileToSend);
                            // Send the file and reset variables
                            String response = connection.receiveMsg();
                            System.out.println("FROM SERVER: " + response);
                            if (response.contains("+")){
                                // Send file
                                connection.sendFile(file);
                                response = connection.receiveMsg();
                                System.out.println("FROM SERVER: " + response);
                            }
                            isSend = false;
                        }else if(msg.contains("SIZE")&& isSend){
                            String response = connection.receiveMsg();
                            System.out.println("FROM SERVER: " + response);
                        }else if (msg.contains("STOR")){
                            String response = connection.receiveMsg();
                            System.out.println("FROM SERVER: " + response);
                            if (response.contains("+")){
                                logger.info("Is send is true");
                                isSend =true;
                            }else{
                                isSend = false;
                            }
                        }
                        else{
                                String response = connection.receiveMsgMultiline();
                                System.out.println("FROM SERVER: " + response);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        logger.severe("Client had an exception");
                    }
                }
                logger.info("Closing Connection");
                connection.closeConnection();
                return;
            }
        }
    }
}
