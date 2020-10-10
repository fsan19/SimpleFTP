package src;
import java.io.*;

import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Connection {
    private Socket connectionSocket;


    private BufferedReader inputStream;

    private DataOutputStream outputStream;

    private Logger logger = Logger.getLogger(Connection.class.getName());

    public Connection(Socket connectionSocket) throws IOException {

        this.logger.setLevel(Level.SEVERE);
        this.connectionSocket = connectionSocket;

        this.inputStream  = new BufferedReader(new InputStreamReader(this.connectionSocket.getInputStream()));
        this.outputStream = new DataOutputStream(connectionSocket.getOutputStream());

    }
    // these only support txt files
    public void sendFile(File file) throws IOException {

        this.outputStream.write(Files.readAllBytes(Path.of(file.getPath())));
        this.outputStream.flush();


    }
    public byte[] receiveFile(int size,boolean isClient) throws IOException {
        if (isClient) this.connectionSocket.setSoTimeout(10000);


        char[] fileBytes = new char[size];

        this.inputStream.read(fileBytes,0,size);

        if (isClient) this.connectionSocket.setSoTimeout(0);


        byte[] bytes = new String(fileBytes).getBytes();


        return bytes;


    }
    // send single line msg
    public  void sendMsg(String msg) {

        try{

            String resp = msg+'\n';

            this.outputStream.writeBytes(resp);

            this.outputStream.flush();
        }catch(Exception e) {
            logger.severe("Msg Failed to send");
        }


    }
    // receive single line msg
    public String receiveMsg() throws IOException {
        String msg = "";


        msg = this.inputStream.readLine();


        return msg;
    }
    // receive multi line msg
    public String receiveMsgMultiline() throws IOException {
        String msg = this.inputStream.readLine();
        while (this.inputStream.ready()){
            msg+= ("\n"+this.inputStream.readLine());
        }

        return msg;
    }

    // close the connection
    public void closeConnection() throws IOException {
        try{
            this.connectionSocket.close();
            logger.info("Closed Connection");
        }catch(Exception e){
            logger.severe("Could not close connection");
        }

    }






}
