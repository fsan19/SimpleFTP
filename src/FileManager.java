package src;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileManager {
    private Connection connection;
    private String directory = System.getProperty("user.dir");
    private String root ;
    private Logger logger = Logger.getLogger(FileManager.class.getName());
    private String type;

    public FileManager(Connection connection,String path) {

        this.logger.setLevel(Level.SEVERE);
        this.connection = connection;
        this.directory += path;
        this.root = this.directory;
        // Default is binary
        this.type = "B";

        logger.info("SPACE USED: "+Long.toString(getSpaceUsed()));

    }


    public Long getSpaceUsed(){
        Long space = Long.valueOf(0);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(this.root))) {
            for (Path path : stream) {
                if (!Files.isDirectory(path)) {
                    BasicFileAttributes fatr = Files.readAttributes(path,BasicFileAttributes.class);
                    space+=fatr.size();
                }
            }
            return space;
        }catch (Exception e){
            this.logger.severe("File Manager CRASHED,COUNTING SPACE USED");
            return space;
        }


    }
    // Appends existing file contents to a file only works for text files
    public boolean appendToFile(byte[] bytes,String fileName){
        logger.info("Appending new file");
        try{
            File file = this.retrieveFile(fileName);
            OutputStream os = new FileOutputStream(file, true);
            os.write(bytes, 0, bytes.length);
            os.close();

        }
        catch (Exception e){
            return false;
        }

        return true;

    }
    public boolean  createNewFile(byte[] bytes,String fileName) {



        logger.info("Creating new file");
        try{



            Files.write(Path.of(this.directory+"/"+fileName),bytes);
        }
        catch (Exception e){
            return false;
        }

        return true;


    }
    // For encoding purpose
    public Boolean changeType(String type){
        if (type.equals("A")||type.equals("B")||type.equals("C")) {this.type= type; return true;}
        return false;
    }
    public String getTypeName(){
        if (this.type.equals("A")) return "Ascii";
        else if (this.type.equals("B")) return "Binary";
        else if (this.type.equals("C")) return "Continous";
        else return "Unknown Type";
    }
    // Assumes file exists (otherwise sends null)
    public File retrieveFile(String filename){
        File file = null;

        file = new File(this.directory+"/"+filename);

        return file;
    }
    // Assumes file exists (otherwise sends 0)
    public Long getFileSize(String fileName){

        try {
            File file = retrieveFile(fileName);
            BasicFileAttributes fatr = null;
            fatr = Files.readAttributes(Path.of(file.getPath()), BasicFileAttributes.class);
            return fatr.size();
        } catch (IOException e) {
            e.printStackTrace();
            return Long.valueOf(0);
        }


    }
    // Send in a multiline format
    public String listFiles(String format,String dir) {

        Set<String> fileList = new HashSet<>();

        String inc = this.root+"/"+dir;
        if (dir.equals("")) inc = this.directory;

        if (dir.equals("root")) inc = this.root;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(inc))) {
            for (Path path : stream) {
                if (!Files.isDirectory(path)) {
                    fileList.add(path.getFileName().toString());
                    inc+=("\r\n"+path.getFileName().toString());
                    if(format.equals("V")){
                        BasicFileAttributes fatr = Files.readAttributes(path,BasicFileAttributes.class);
                        inc+=("\r\n"+"File Size (Bytes):" + Long.toString(fatr.size()) );
                        inc+=("\r\n"+"File Created at:"+fatr.creationTime().toString());
                        inc+=("\r\n"+"File Last Modified at:"+fatr.lastModifiedTime().toString());
                        inc+=("\r\n"+"File Last Accessed at:"+fatr.lastAccessTime().toString());

                    }

                }else{
                    inc+=("\r\n"+path.getFileName()+"/");
                    if(format.equals("V")){
                        BasicFileAttributes fatr = Files.readAttributes(path,BasicFileAttributes.class);
                        inc+=("\r\n"+"Directory Size:" + Long.toString(fatr.size()) );
                        inc+=("\r\n"+"Directory Created at:"+fatr.creationTime().toString());
                        inc+=("\r\n"+"Directory Last Modified at:"+fatr.lastModifiedTime().toString());
                        inc+=("\r\n"+"Directory Last Accessed at:"+fatr.lastAccessTime().toString());

                    }

                }
            }


        }catch (Exception e){

            return "-"+e.toString();
        }
        return "+"+inc;
    }
    public boolean checkIfDirectoryExists(String dir){
        Path path = Paths.get(this.root+"/"+dir);


        if (Files.exists(path)) {

            if (Files.isDirectory(path)) {
               return true;
            }

        } else {
            return false;
        }
        return false;

    }
    public boolean checkIfFileExists(String filename){
        File file = new File(this.directory+"/"+filename);
        return file.exists();

    }
    // !Method assumes file exists
    public String checkFileSize(String filename){
        File file = new File(this.directory+"/"+filename);
        return Long.toString(file.length());

    }
    // will fail if the directory is not valid (returns error msg in string)
    public String changeDirectory(String directory){
        if (directory.equals("root")){
            this.directory = this.root;
            return this.directory;
        }
        this.directory = this.root +"/"+ directory;
        return this.directory;
    }
    // assumes file exists (can check using method)
    public void deleteFile(String filename){
        File file = new File(this.directory+"/"+filename);
        file.delete();

    }
    // assumes file exists (can check using method)
    public boolean renameFile(String oldName,String newName){
        File file = new File(this.directory+"/"+oldName);

        return file.renameTo(new File(this.directory+"/"+newName));
    }

}
