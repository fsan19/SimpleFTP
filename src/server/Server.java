package src.server;

import org.w3c.dom.events.EventException;
import src.Connection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Server {



    static int port = 6789;
    static ServerSocket welcomeSocket ;
    static Connection connection;
    static boolean outToLunch = false;
    static CmdHandler handler;
    static ArrayList<User> users;



    static Logger logger = Logger.getLogger(Server.class.getName()) ;



    public static void setupUserCredentials(){
        try{
            String root = System.getProperty("user.dir");
            String pathToCsv = root+"/res/credentials.csv";
            BufferedReader csvReader = new BufferedReader(new FileReader(pathToCsv));
            String row;
            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split(",");
                if (row == null) continue;
                if (row.equals("admin")){
                    User admin = new User("admin");
                    admin.setAdmin(true);
                    users.add(admin);
                }else{
                    Iterator itr = Arrays.stream(data).iterator();
                    String username = (String) itr.next();
                    User usr = new User(username);
                    while (itr.hasNext()){
                        // Separate into user and password
                        String acct = (String) itr.next();
                        String[] acctInfo = acct.split(":");
                        usr.addAccount(new Account(acctInfo[0],acctInfo[1]));
                    }
                    users.add(usr);
                }
            }
            csvReader.close();
        }catch (Exception e){
            logger.severe(e.toString()+"Credentials could not be registered manually creating them");

            users = new ArrayList<User>();
            User user1 = new User("admin");
            user1.setAdmin(true);
            User user2 = new User("UoA");
            Account act1 = new Account("fsan110","feneel");
            Account act2 = new Account("utri092","utsav");
            user2.addAccount(act1);
            user2.addAccount(act2);
            users.add(user1);
            users.add(user2);
        }
    }



    public static void main(String argv[]) throws Exception {
        logger.setLevel(Level.INFO);

        users = new ArrayList<User>();
        setupUserCredentials();

        welcomeSocket = new ServerSocket(port);
        logger.info("Server Running");

        while(true){
            logger.info("Awaiting client requests");
            Socket connectionSocket = welcomeSocket.accept();
            logger.info("Established connection");
            connection = new Connection(connectionSocket);
            handler = new CmdHandler(connection,users);

            if (outToLunch==true){
                connection.sendMsg("-Feneel's Server Out to Lunch");
            }else{
                connection.sendMsg("+ Feneel's Server SFTP Service");

                while(true) {
                    try {
                        String msg = connection.receiveMsg();
                        if (handler.checkValidCommand(msg) == true) {
                            // Service Command
                            int run = handler.serviceCommand(msg);

                            if (run == -1) {
                                //close connection
                                connection.sendMsg("Thanks for using Feneel's Server, Goodbye.");
                                connection.closeConnection();
                                break;
                            }

                        } else {
                            if (msg != null) {
                                // Return invalid command
                                connection.sendMsg("- Invalid Command");
                            }
                        }
                    } catch (IOException e) {
                        logger.severe("Server Crashed");
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
