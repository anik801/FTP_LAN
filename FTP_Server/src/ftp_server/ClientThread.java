package ftp_server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import static java.lang.Thread.sleep;
import java.net.Socket;
import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author John
 */
class ClientThread extends Thread{
    BufferedReader fromClient;
    PrintStream toClient;
    String currentUsername;
    Socket con;
    
    String message;
    String getListCommand,getCompleteList;
    String exitCommand;
    int count;
    String str; 
    
    String[] activeUsers = new String[100];
    String[] fileList = new String[100];
    
    public ClientThread(BufferedReader fromClient,PrintStream toClient, String username, Socket con) {
        // Initializing 
        this.fromClient = fromClient;
        this.toClient = toClient;
        this.currentUsername = username;
        this.con = con;
        
        getListCommand = "getList";
        getCompleteList = "getCompleteList";
        exitCommand = "exit";
        count=0;
        
        start();                
    }
    
    
    
    public void run(){        
        evaluateUserCommands();        
    }
    
    private void evaluateUserCommands(){
        while(true){
            try {
                message = fromClient.readLine();        // get command
                if(message.equals(getListCommand)){
                    handleGetListCommand();
                    
                }else if(message.equals(getCompleteList)){
                    handleGetCompleteListCommand();   
                    
                }else if(message.equals(exitCommand)){
                    handleExitCommand();
                    
                    
                }else{
                    toClient.println("Invalid/unknown option(1).\nEnter command: ");
                }
                //sleep(100);                
                             
            } catch (Exception ex) {
                //Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Exception in ClientThread: " + ex);
                this.stop();
            }
        }
    }
    
    //declaring global variables
    int userIndex;
    int userChoice;
    String folderName;
    String sourceFolder;
    ArrayList<String> names;
    int fileIndex;
    //declaration ends
    
    private void handleGetListCommand(){
        try{
            sendActiveUsersListToClient();

            message = fromClient.readLine();    // Get user reply of desired user index
            userChoice = Integer.parseInt(message);

            if(userChoice>=0 && userChoice<userIndex){
                message = "Select from available files (Enter index number only, to abort, enter: -1):";                        
                toClient.println(message);      // Message to client                        

                sendFileListToClient();

                message = fromClient.readLine();    // Get user reply of desired file index
                int fileChoice = Integer.parseInt(message);
                
                if(fileChoice>=0 && fileChoice<=fileIndex){
                    toClient.println("File copied: "+names.get(fileChoice).toString());

                    String from = sourceFolder+names.get(fileChoice).toString();
                    String to = "C:\\Users\\John\\Documents\\NetBeansProjects\\NetworkProgramming\\FTP_Client\\UserFolders\\"+currentUsername+"\\"+names.get(fileChoice).toString();

                    copyFile(from, to);
                }else if(fileChoice == -1){
                    toClient.println("Operation aborted!\nEnter command:");
                }else{
                    toClient.println("Invalid option or command\nEnter command:");
                }                        

            }else{
                toClient.println("Invalid/unknown option(2).\nEnter command: ");
            }

        } catch(Exception ex){
            System.out.println("Exception in handling getList command: "+ex);
        }
    }
   
    private void sendActiveUsersListToClient(){
        try{       
            Scanner fileScanner = new Scanner(new File("activeUsers.txt"));
            userIndex = 0;

            toClient.println("Select from available users (Enter index number only, to abort enter: -1):");     // Message to client

            // Read active users file
            while (fileScanner.hasNext()){
                str = fileScanner.next();
                if(!str.equals(currentUsername))            //// comment this to see own file 
                {
                    //toClient.println(userIndex+". "+str);
                    activeUsers[userIndex]=str;
                    userIndex++;
                }       
            }
            
            
            // Send user list to client
            toClient.println(userIndex);
            if(userIndex==0){
                evaluateUserCommands();
            }
            for(int i=0;i<userIndex;i++){
                toClient.println(activeUsers[i]);
            }

        }catch (FileNotFoundException ex) {
            //Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Exception in handling getList command: "+ex);
        }catch(Exception ex){
            System.out.println("Exception in handling getList command: "+ex);
        }
        
    }
    
    private void sendFileListToClient(){
        try{
            folderName = activeUsers[userChoice];
            sourceFolder = "C:\\Users\\John\\Documents\\NetBeansProjects\\NetworkProgramming\\FTP_Client\\UserFolders\\"+folderName+"\\";
            File f = new File(sourceFolder);
            names = new ArrayList<String>(Arrays.asList(f.list()));

            toClient.println(names.size());            
            if(names.size()==0){
                evaluateUserCommands();
            }
            
            for(fileIndex=0; fileIndex<names.size(); fileIndex++){
                //System.out.println(names.get(fileIndex).toString());
                message = fileIndex+". "+names.get(fileIndex).toString();
                toClient.println(message);
            }
        }catch(Exception ex){
            System.out.println("Exception in sending file list: "+ex);
        }
    }
    
    
    private void handleGetCompleteListCommand(){
        try{
            //show complete file list                  
            //////////////////////////////////////////////////////////////////////
            readAllFiles();
            //System.out.println(fileCount);
            toClient.println(fileCount);        //send file count
            
            showFileListToClient();
            
            str = fromClient.readLine();        //read user option
            int option = Integer.parseInt(str);

            if(option>=0 && option<fileCount){
                String sourceFolder = filesList[option][0];
                String sourceFile = filesList[option][1];

                String from = "C:\\Users\\John\\Documents\\NetBeansProjects\\NetworkProgramming\\FTP_Client\\UserFolders\\"+sourceFolder+"\\"+sourceFile;
                String to = "C:\\Users\\John\\Documents\\NetBeansProjects\\NetworkProgramming\\FTP_Client\\UserFolders\\"+currentUsername+"\\"+sourceFile;

                copyFile(from,to);

                toClient.println("File copied: "+sourceFolder+"\\"+sourceFile);
            }else{
                toClient.println("Operation aborted!");
            }
            //////////////////////////////////////////////////////////////////////
        }catch(Exception ex){
            System.out.println("Exception handling getCompleteList: "+ex);
        }
    }
    
    private void handleExitCommand(){
        try{
            toClient.println("exit0");
            System.out.println(currentUsername+" has logged out.");
            removeUserEntry();
            
            con.close();            
            this.stop();        
        }catch(Exception ex){
            System.out.println("Exception handling getCompleteList: "+ex);
        }
    }
    
    private void removeUserEntry(){
        BufferedWriter writer = null;
        try {
            File inputFile = new File("activeUsers.txt");
            File tempFile = new File("activeUsers_temp.txt");
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            writer = new BufferedWriter(new FileWriter(tempFile));
            String currentLine;
            while((currentLine = reader.readLine()) != null) {
                if(null!=currentLine && !currentLine.equalsIgnoreCase(currentUsername)){
                    writer.write(currentLine + System.getProperty("line.separator"));
                }
            }   writer.close();
            reader.close();          
            
            System.gc();
            inputFile.setWritable(true);            
            tempFile.setWritable(true);            
            boolean bool = inputFile.delete(); 
            tempFile.renameTo(inputFile);
            
        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void copyFile(String from, String to){
        try {
            Path FROM = Paths.get(from);
            Path TO = Paths.get(to);
            CopyOption[] options = new CopyOption[]{
              StandardCopyOption.REPLACE_EXISTING,
              StandardCopyOption.COPY_ATTRIBUTES
            }; 
            java.nio.file.Files.copy(FROM, TO, options);
            //keeping server log
            System.out.println("File copied:\tFROM: " +from+"\tTO: "+to);
        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("File copy exception: " + ex);
        }
    }
    
    
    //declaring global variables
    String[] folders = new String[100];
    int folderCount;
    String[][] filesList = new String[100][2];
    int fileCount;
    //declaration ends
    
    private void readAllFiles(){                
        String str;        
        Scanner fileScanner = null;
        fileCount = 0;
        folderCount = 0;
        try {
            fileScanner = new Scanner(new File("data.txt"));                        
            while (fileScanner.hasNext()){
                str = fileScanner.next();
                folders[folderCount]=str;     //username
                folderCount++;
                                
                String sourceFolder = "C:\\Users\\John\\Documents\\NetBeansProjects\\NetworkProgramming\\FTP_Client\\UserFolders\\"+str+"\\";
                File f = new File(sourceFolder);
                ArrayList<String> names = new ArrayList<String>(Arrays.asList(f.list()));
    

                for(int fileIndex=0; fileIndex<names.size(); fileIndex++){                    
                    filesList[fileCount][0]=str;
                    filesList[fileCount][1]=names.get(fileIndex).toString();
                    
                    //message = fileCount+".\tUser: "+str+"\tFile: "+filesList[fileCount][1];
                    //System.out.println(message);
                    //toClient.println(message);
                    
                    fileCount++;
                }   
                
                str = fileScanner.next();   // skipping the password
            }
            if(fileCount==0){
                evaluateUserCommands();
            }
                
        }catch(FileNotFoundException e){
            System.err.println("File not found exception: " + e);
        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("File reading exception: "+ex);
        }finally{
            fileScanner.close();
        }
    }
    
    private void showFileListToClient(){
        try{            
            for(int i =0; i<fileCount; i++){
                message = i+".\tUser: "+filesList[i][0]+"\t"+filesList[i][1];
                toClient.println(message);
            }
        }catch(Exception ex){
            System.out.println(ex);
        }
    }
}