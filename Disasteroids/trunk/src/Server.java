/*
 * DISASTEROIDS
 * Server.java
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Server side of the C/S networking.
 * @author Phillip Cohen
 * @since December 25, 2007
 */
public class Server extends DatagramListener
{

    /**
     * The default port that the server runs on.
     * @since December 29, 2007
     */
    public static int DEFAULT_PORT = 4919;

    /**
     * Messages that we send to the client.
     * @since December 29, 2007
     */
    public enum Message
    {

        /**
         * We send the client all of the game's data to allow him to join.
         */
        FULL_UPDATE,

    }
    /**
     * List of everyone who has sent us a packet.
     * @since December 29, 2007
     */
    private LinkedList<Machine> clients;

    /**
     * Starts the server and waits for clients.
     * 
     * @since December 28, 2007
     */
    public Server()
    {
        try
        {
            System.out.println( "== DISASTEROIDS SERVER ==\nStarted!" );
            clients = new LinkedList<Machine>();
            beginListening( DEFAULT_PORT );
        }
        catch ( SocketException ex )
        {
            Logger.getLogger( Server.class.getName() ).log( Level.SEVERE, null, ex );
        }
    }

    /**
     * Called when we receive a packet from a client.
     * Deciphers what the client is telling us, and responds.
     * 
     * @param p the packet
     * @since December 28, 2007
     */
    void parseReceived( DatagramPacket p )
    {
        try
        {
            // Ensure this client is on our register.
            Machine client = registerClient( new Machine( p.getAddress(), p.getPort() ) );

            // Create streams.
            ByteArrayInputStream bin = new ByteArrayInputStream( p.getData() );
            DataInputStream din = new DataInputStream( bin );

            // Determine the type of message.
            int command = din.readInt();
            if ( ( command >= 0 ) && ( command < Client.Message.values().length ) )
            {
                switch ( Client.Message.values()[command] )
                {

                    // Client wants to join the game.
                    case CONNECT:

                        // Send him a full update.
                        ByteArrayOutputStream b = new ByteArrayOutputStream();
                        DataOutputStream d = new DataOutputStream( b );
                        d.writeInt( Message.FULL_UPDATE.ordinal() );

                        // Send asteroids.
                        Game.getInstance().asteroidManager.flatten( d );
                        d.close();
                        sendPacket( client, b );
                        break;
                }
            }
        }
        catch ( IOException ex )
        {
            ex.printStackTrace();
            Running.quit();
        }
    }
    
    /**
     * Takes the prospective client, and ensures he's on our <code>clients</code> list.
     * Note that clients are always different; the comparison is just of IP addresses.
     * Thus, even if <code>newbie</code> is already on the list, the version on the list will be returned.
     * 
     * @param newbie    the unknown client
     * @return          the client on the <code>clients</code> list
     * @since December 29, 2007
     */
    Machine registerClient( Machine newbie )
    {
        // See if he's on the list.
        for ( Machine c : clients )
        {
            if ( c.equals( newbie ) )
                return c;
        }
        
        // Nope. Add him.
        clients.addLast( newbie );
        return newbie;
    }       
}
