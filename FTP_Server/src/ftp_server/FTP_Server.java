package ftp_server;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 * @author John
 */
public class FTP_Server {
    ServerSocket serverSocket;
    Socket clientSocket;
    PrintStream stringToClient;
    BufferedReader clientReader; 
    BufferedReader sout;
    String serversay;
    
    int socketNumber;
    String[][] userdata = new String[100][2];
    int row,col;    //Initializing array index
    String trueFlag,falseFlag;     
    
    ClientThread[] clients = new ClientThread[10];
    int clientIndex;
    
    String activeUsersFileName;
       
    public FTP_Server(int port){
        socketNumber=port;
        
        row=0;col=0;    //Initializing array index
        
        activeUsersFileName= "activeUsers.txt";
        trueFlag="1";
        falseFlag="0";   
        clientIndex=0;
    }
    
    public static void main(String[] args){
        FTP_Server currentServer = new FTP_Server(2020);
        currentServer.readUserDatabase();        
        currentServer.initializeServer();
        
        currentServer.clearLog();
       
        while(true){
            currentServer.waitForUser();
        }        
    }
    
    private void clearLog(){
         //Clear activeUsers.txt        
        try{            
            FileWriter logWriter = new FileWriter(activeUsersFileName,false); //the true will append the new data
            logWriter.write("");//appends the string to the file    
            logWriter.close();            
        }catch(IOException ioe){
            System.err.println("IOException: " + ioe);
        }
    }
    
    private void waitForUser(){
        try{
            clientSocket = serverSocket.accept();   //Establishing connection
            clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));            
            stringToClient = new PrintStream(clientSocket.getOutputStream());            
            
            stringToClient.println(trueFlag);    //1.  Connection open
                        
            String username = clientReader.readLine();
            System.out.println("Client >> Username: "+username);    // Server output         
            stringToClient.println(trueFlag);    //2.  Username
            
            String password = clientReader.readLine();
            System.out.println("Client >> Password: "+password);    // Server output                        
            stringToClient.println(trueFlag);    //3.  Password
            
            
            if( trueFlag.equals( checkUser(username,password) ) ){      //4.  Authentication
                logEntry(username);
                
                stringToClient.println(trueFlag);
                clients[clientIndex] = new ClientThread(clientReader,stringToClient,username,clientSocket);
                clientIndex++;
            }else{
                stringToClient.println(falseFlag);
            }            
            
            
        }catch(Exception e){
            System.err.println("Exception: "+e);            
        }
    }
    
    private Boolean logEntry(String username){        
        String str;        
        Scanner fileScanner = null;
        Boolean returnValue=true;
        
        //Check for duplicate user
        try {
            fileScanner = new Scanner(new File(activeUsersFileName));            
            
            while (fileScanner.hasNext()){
                str = fileScanner.next();
                if(str.equals(username)){
                    returnValue=false;
                    return returnValue;
                }
            }
        }catch(FileNotFoundException e){
            System.err.println("File not found exception: " + e);
        }
        
        //Entry user
        try{
            FileWriter logWriter = new FileWriter(activeUsersFileName,true); //the true will append the new data
            str = username+System.lineSeparator();
            logWriter.write(str);//appends the string to the file    
            logWriter.close(); 
        }catch(Exception e){
            System.err.println("User entry logger exception: "+e);
        }
        
        return returnValue;
    }
    
    private String checkUser(String username, String password){        
        for(int i=0;i<row;i++){            
            if(username.equals(userdata[i][0]) && password.equals(userdata[i][1])){
                System.out.println("Access Granted.");  // Server output
                return trueFlag;                
            }
        }
        System.out.println("Access Denied!");       // Server output
        return falseFlag;
    }
    
    private void initializeServer(){
        try {
            serverSocket = new ServerSocket(socketNumber);    //Creating server
            System.out.println("Server started at socket: "+socketNumber);
        } catch (Exception ex) {
            System.err.println("Server Initialization exception: "+ex);
        }        
    }
    
    private void readUserDatabase(){        
        
        String str;        
        Scanner fileScanner = null;
        try {
            fileScanner = new Scanner(new File("data.txt"));            
            
            while (fileScanner.hasNext()){
                str = fileScanner.next();
                userdata[row][col]=str;     //username
                col++;
                str = fileScanner.next();
                userdata[row][col]=str;     //password
                row++;
                col=0;        
            }
        }catch(FileNotFoundException e){
            System.err.println("File not found exception: " + e);
        }
        
        //showUserDatabase();
    }
    
    private void showUserDatabase(){
        //Testing array input
        for(int i=0;i<row;i++){
            System.out.println(userdata[i][0]+"\t"+userdata[i][1]);
        }
        
    }
}