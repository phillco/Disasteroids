/*
 * DISASTEROIDS
 * by Phillip Cohen and Andy Kooiman
 * 
 * APCS 1, 2006 to 2007, Period 3
 * Version - 1.0 Final (exam release)
 *
 * Run Running.class to start
 */

/* Adapted from the Shockit program by Phillip Cohen
 * for use with the Disasteroids Project.
 * only uses a single client and host*/



import java.net.*;
import java.io.*;
import javax.swing.JOptionPane;





public class AsteroidsServer
{
    private static boolean 			net_ishosting 		= false;					// If true, we have clients to talk to.
    private static int				net_targetclients	= 0; 						// User specified amount of clients
    private static String			net_myIP			= null;
    private static int				net_myPort			= 3344; 					// This default is used unless the user runs the PORT command.
	private static ServerSocket		serverSocket 		= null;
	private static Socket 			clientSocket;
	private static PrintWriter		serverOutStream;
	private static boolean			isMaster			= false;					//stores whether this computer is the master
    private static PrintWriter		out;
    private static BufferedReader	in;
    private static Socket			kkSocket; 
    
    public static void slave(String address) {
        
        JOptionPane.showMessageDialog(null,"\nConnecting to " + address + "...");

        try {
            kkSocket = new Socket(address, 3344);
            out = new PrintWriter(kkSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
        } catch (UnknownHostException e) {
            JOptionPane.showMessageDialog(null,"Don't know about host: " + address);
            return;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Couldn't get I/O for the connection to: " + address);
            return;
        }

        String fromServer;
        String fromUser;
       
        
        (new ServerListenerThread(in)).start();
    }
    
    
    public static void master() {
    		isMaster=true;
    	// Did the user already start a server?
	    	if(net_ishosting) {
	    		JOptionPane.showMessageDialog(null,
	    		  "A server is already running, at IP " + net_myIP + ":" + net_myPort + "." );
	    		return;
	    	}
    	
    		//there is only one client
	   		net_targetclients =1;
	   
	    // Now actually start the server.    	
	        try {
	            serverSocket = new ServerSocket(net_myPort);
	        } catch (IOException e) {
	            JOptionPane.showMessageDialog(null,"Could not listen on port: " + net_myPort+".\nAnother server may already be running.");
	            return;
	        }
	        
        // The server is running. Tell the user his IP address.
	        net_myIP =  myIP();
	    	JOptionPane.showMessageDialog(null,"Server ready!\nYour IP address is:\n" + net_myIP + "\nPress ok...");
	    	safeSleep(500);
    	
    	// Now we hang while we wait for everyone to hook up.
	        clientSocket = null;
		    try {
		        	clientSocket = serverSocket.accept(); // Stop here until the client connects.
		        	out = new PrintWriter(clientSocket.getOutputStream(), true);
		        	in=new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		     //   	JOptionPane.showMessageDialog(null,"Other computer has connected.");
		       } catch (IOException e) {
		        JOptionPane.showMessageDialog(null,"Accept failed.");
		          return;
		   }   	          
        net_ishosting = true;
        (new ServerListenerThread(in)).start();
    }
    
    
    public static void send(String message)
    {
    	try{
   		out.println(message);
//  	System.out.println("SENT: "+message);
		out.flush();
   		}catch(NullPointerException e){}
    }
    
    public static void flush()
    {
    	out.flush();
    }
    
    public static String myIP() {
    	try {
	    	InetAddress localHost = InetAddress.getLocalHost();
			InetAddress[] all_IPs = InetAddress.getAllByName(localHost.getHostName());
			return (all_IPs[0].toString().split("/"))[1];
    	}
    	catch (UnknownHostException e) { return "Could not detect IP."; }
    }
	
	
	
    public static void safeSleep(int duration) 
    {
	   	try 
	   	{
			 Thread.sleep(duration);
		}
		 catch(InterruptedException interruptedexception) {}    	

    }

	public static void close()
	{
		try{
			out.close();
			in.close();
			kkSocket.close();			
		}catch(IOException e){}
	}
}