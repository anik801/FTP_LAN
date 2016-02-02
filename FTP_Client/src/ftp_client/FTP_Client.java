package ftp_client;

import java.io.*;
import java.net.*;


/**
 *
 * @author John
 */
public class FTP_Client {
    BufferedReader serverReader,clientInput;
    String str;
    PrintStream stringToServer;
    Socket socketConnection;
    
    String trueFlag,falseFlag;  
    String serverName,username,password;
    int socketNumber;
                    
    public FTP_Client(String name, int port){
        //initializing
        serverName=name;
        socketNumber=port;
        trueFlag="1";
        falseFlag="0"; 
    }
    
    public static void main(String[] args) {
        FTP_Client currentClient = new FTP_Client("localhost",2020);
        
        while(true){
            currentClient.userActivity();
        }        
    }
    
    private void userActivity(){
        try{            
            getConnectionAddress();
            str = serverReader.readLine();            
            
            if(str.equals(trueFlag)){
                System.out.println("Connected to "+serverName+" through Socket: "+socketNumber);    //1.  Connection open
            }else{
                //Throw user defined exception
                throw new ConnectionOpenException();                    
            }

            getUsername();
            str = serverReader.readLine();
            if(str.equals(trueFlag)){
                System.out.println("\nUsername: "+username+" received by server.");    //2.  Username
            }else{                    
                //Throw user defined exception
                throw new ConnectionLostException();
            }

            getPassword();
            str = serverReader.readLine();
            if(str.equals(trueFlag)){
                System.out.println("\nPassword received by server. Authenticatinng, please wait.");    //3.  Password
            }else{
                //Throw user defined exception
                throw new ConnectionLostException();
            }

            str = serverReader.readLine();
            if(str.equals(trueFlag)){
                System.out.println("\nAuthentication Successful.\nEnter command:");    //4.  Authentication
                while(true){
                    evaluateUserCommand();                    
                }
            }else{
                System.out.println("\nInvalid username/password!.");    //4.  Authentication                    
            }

        }catch(Exception e){
            System.err.println("Exception occured: " + e);            
            System.err.println("USER LOGGED OUT!");
        }        
    }
    
    private void evaluateUserCommand(){
        
        try {        
            str  = clientInput.readLine();
            String command = str;
            stringToServer.println(str);    //Give command
            str  = serverReader.readLine(); 
            if(str.equals("exit0")){
                socketConnection.close();
                System.exit(0);
            }else if(command.equals("getList")){
                showSpecificFiles();
            }else if(command.equals("getCompleteList")){
                showAllFiles();
            }else{
                System.out.println("Invalid command. Try Again:");
            }
              
            
        } catch (IOException ex) {
            System.err.println("User command exception: "+ex.getMessage());
            evaluateUserCommand();
        }

    }
    
    private void showSpecificFiles(){
        try{
            System.out.println(str);        //User list message
            str  = serverReader.readLine(); //user count
            int userCount = Integer.parseInt(str);
            if(userCount == 0){
                System.out.println("No online users!\nEnter command: ");
                evaluateUserCommand();
            }
            for(int i=0;i<userCount;i++){
                str  = serverReader.readLine();
                System.out.println(i+". "+str);
            }

            str = getUserChoice(0,userCount);
            if(str.equals("-1")){
                throw new userAbortException();
            }
            stringToServer.println(str);    //Give command                    



            str  = serverReader.readLine();            
            System.out.println(str);        //File list message                    

            str  = serverReader.readLine(); //file count                    
            int fileCount = Integer.parseInt(str);            
            if(fileCount == 0){
                System.out.println("No files!\nEnter command: ");
                evaluateUserCommand();
            }
            
            for(int i=0;i<fileCount;i++){
                str  = serverReader.readLine();
                System.out.println(str);
            }

            str = getUserChoice(0,fileCount);
            if(str.equals("-1")){
                throw new userAbortException();
            }
            stringToServer.println(str);    //Give command

            str = serverReader.readLine();
            System.out.println(str);        //confirmation message
            evaluateUserCommand();
        }catch (userAbortException ex) {
            System.err.println(ex);
            evaluateUserCommand();
        } catch (IOException ex) {
            System.err.println("User command exception: "+ex.getMessage());
            evaluateUserCommand();
        }
    }
    
    private void showAllFiles(){
        try{            
            //str = serverReader.readLine();      //read file count            
            int fileCount = Integer.parseInt(str);
            if(fileCount == 0){
                System.out.println("No files!\nEnter command: ");
                evaluateUserCommand();
            }
            
            System.out.println("Enter index number of desired file:");        //User list message
            for(int i=0;i<fileCount;i++){
                str = serverReader.readLine();
                System.out.println(str);
            }
            str = getUserChoice(0,fileCount);
            stringToServer.println(str);
            
            str = serverReader.readLine();
            System.out.println(str);
            evaluateUserCommand();
        }catch (Exception ex) {
            System.err.println("Show all files exception: "+ex);
            evaluateUserCommand();
        }
    }
    
    private void getConnectionAddress(){        
        try{
            clientInput = new BufferedReader(new InputStreamReader(System.in));
            
            System.out.println("\n_____________________________\nWelcome To Client Panel\n_____________________________");
            
            /*
            System.out.print("Enter server name: ");    //localhost
            serverName = clientInput.readLine();
            System.out.print("Enter server socket number: ");    //localhost
            socketNumber = Integer.parseInt(clientInput.readLine());            
            */
            
            socketConnection = new Socket(serverName,socketNumber);
            serverReader =  new BufferedReader(new InputStreamReader(socketConnection.getInputStream()));        
            stringToServer = new PrintStream(socketConnection.getOutputStream());
        }catch(Exception e){
            System.err.println("Connection exception: " + e);
            getConnectionAddress(); //Recall
        }
        
    }
    
    private void getUsername(){         
        try {
            System.out.print("Enter username: ");
            username = clientInput.readLine();
            stringToServer.println(username);
        } catch (IOException ex) {
            System.err.println(ex);            
        }
    }
    
    private void getPassword(){
        try {
            System.out.print("Enter password: ");
            password = clientInput.readLine();
            stringToServer.println(password);
        } catch (IOException ex) {
            System.err.println(ex);            
        }
    }
    
    private String getUserChoice(int start, int end){
        String choice = "";
        try {            
            choice  = clientInput.readLine();
            int num = Integer.parseInt(choice);
            if(num==-1){
                return choice;
            }else if(num<start || num>end){
                throw new userChoiceException();
                //System.out.println("Invalid option, try again: ");
                //getUserChoice(start,end);
            }        
        } catch (Exception ex) {
            System.err.println("Invalid entry, try again." + ex);
            choice = getUserChoice(start,end);
        }
        /*
        catch(userChoiceException ex){
            System.err.println("Invalid entry, try again." + ex.getMessage());
            choice = getUserChoice(start,end);
        }
        */
        return choice;
    }
}




//==========================================================
class ConnectionOpenException extends Exception{
    @Override
    public String toString()
    {
        return "Exception in connecting to server. Could not connect to server!";
    }
}
//==========================================================


//==========================================================
class ConnectionLostException extends Exception{
    @Override
    public String toString()
    {
        return "Exception in connecting to server. Connection lost!";
    }
}
//==========================================================


//==========================================================
class userChoiceException extends Exception{
    @Override
    public String toString()
    {
        return "Invalid user choice!";
    }
}
//==========================================================

//==========================================================
class userAbortException extends Exception{
    @Override
    public String toString()
    {
        return "Operation aborted by user!";
    }
}
//==========================================================