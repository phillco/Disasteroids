/*
 * DISASTEROIDS
 * AsteroidsServer.java
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * The TCP networking class.
 * @since Spring 2007
 * @author Phillip Cohen, Andy Kooiman
 */
public class AsteroidsServer
{
    /**
     * Our little status window.
     */
    private static NetworkStatus statusFrame;
    /**
     * This computer's IP address.
     */
    private static String localIP = null;
    private static final int DEFAULT_PORT = 53;
    private static int net_myPort = DEFAULT_PORT;
    private static ServerSocket serverSocket = null;
    private static Socket clientSocket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static Socket kkSocket;

    /**
     * Connects to a server and runs the game as a client.
     * @param address The IP address to connect to.
     */
    public static void slave( String address ) throws ConnectException
    {
        // Popup the status screen while the user waits.
        statusFrame = new NetworkStatus( NetworkStatus.StatusState.CLIENT, address );

        // Connect.
        try
        {
            kkSocket = new Socket( address, net_myPort );
            out = new PrintWriter( kkSocket.getOutputStream(), true );
            in = new BufferedReader( new InputStreamReader( kkSocket.getInputStream() ) );
        }
        catch ( UnknownHostException e )
        {
            connectionError( e.toString(), "Disasteroids couldn't look up the host at " + address + "." );
            throw new ConnectException();
        }
        catch ( IOException e )
        {
            connectionError( e.toString(), "Couldn't get an I/O connection to " + address + "." );
            throw new ConnectException();
        }

        // Success!
        statusFrame.dispose();
        ( new ServerListenerThread( in ) ).start();
    }

    /**
     * Creates a server and starts the game once a client connects.
     * @return Whether the connection was made.
     */
    public static void master() throws ConnectException
    {
        // Popup the status screen while the user waits.
        statusFrame = new NetworkStatus( NetworkStatus.StatusState.SERVER, myIP() );

        // Start the server.    	
        try
        {
            serverSocket = new ServerSocket( net_myPort );
        }
        catch ( IOException e )
        {
            connectionError( "Could not listen on port " + net_myPort + ".", "Another server may already be running at this port." );
            throw new ConnectException();
        }

        // Now we hang while we wait for everyone to hook up.
        clientSocket = null;
        try
        {
            clientSocket = serverSocket.accept(); // Stop here until the client connects.
            out = new PrintWriter( clientSocket.getOutputStream(), true );
            in = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );
        }
        catch ( IOException e )
        {
            connectionError( "Accept failed.", "Could not accept client connection." );
            throw new ConnectException();
        }

        // Success!
        statusFrame.dispose();
        ( new ServerListenerThread( in ) ).start();
    }

    public static void send( String message )
    {
        try
        {
            out.println( message );
            out.flush();
        }
        catch ( NullPointerException e )
        {
        }
    }

    private static void connectionError( String title, String body )
    {
        // Show the status window.
        if ( statusFrame == null )
            statusFrame = new NetworkStatus();

        // Display the error message.
        statusFrame.setError( title, body );

        // Close our streams.
        dispose();
    }

    public static void flush()
    {
        out.flush();
    }

    public static String myIP()
    {
        try
        {
            InetAddress localHost = InetAddress.getLocalHost();
            InetAddress[] all_IPs = InetAddress.getAllByName( localHost.getHostName() );
            return ( all_IPs[0].toString().split( "/" ) )[1];
        }
        catch ( UnknownHostException e )
        {
            return "Could not detect IP.";
        }
    }

    public static void dispose()
    {
        try
        {
            if ( out != null )
                out.close();

            if ( in != null )
                in.close();

            if ( kkSocket != null )
                kkSocket.close();
        }
        catch ( IOException e )
        {
        }
    }
}
