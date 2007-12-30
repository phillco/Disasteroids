/*
 * DISASTEROIDS
 * Client.java
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client side of the C/S networking.
 * @author Phillip Cohen
 * @since December 28, 2007
 */
public class Client extends DatagramListener
{
    /**
     * Location of the server.
     * @since December 29, 2007
     */
    private Machine server;

    /**
     * Messages that we send to the server.
     * @since December 29, 2007
     */
    public enum Message
    {
        /**
         * We want to connect to the server and join the game.
         */
        CONNECT,
        /**
         * Sending our keystroke.
         */
        KEYSTROKE;

    }
    
    private static Client instance;

    public static Client getInstance()
    {
        return instance;
    }
    
    
    /**
     * Binds this client to the given server, and connects to it.
     * Assumes the default server port.
     * 
     * @param serverAddress     the IP address of the server
     * @throws java.net.UnknownHostException    if the given server can't be found
     * @since December 29, 2007
     */
    public Client( String serverAddress ) throws UnknownHostException
    {
        instance = this;
        server = new Machine( InetAddress.getByName( serverAddress ), Server.DEFAULT_PORT );        
        connect();
    }

    /**
     * Connects to the server by sending it our request packet.
     * After this, we simply wait for a response, which is handled in <code>parseReceived</code>.
     * 
     * @since December 29, 2007
     */
    private void connect()
    {
        try
        {
            System.out.println( "Connecting to " + server + "." );
            beginListening();

            // Send our connection request.
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream d = new DataOutputStream( b );
            d.writeInt( Message.CONNECT.ordinal() );
            sendPacket( server, b );
        }
        catch ( IOException ex )
        {
            ex.printStackTrace();
        }
    }

    void parseReceived( DatagramPacket p )
    {
        try
        {
            // Create streams.
            ByteArrayInputStream bin = new ByteArrayInputStream( p.getData() );
            DataInputStream din = new DataInputStream( bin );

            // Determine the type of message.
            int command = din.readInt();
            if ( ( command >= 0 ) && ( command < Server.Message.values().length ) )
            {
                switch ( Server.Message.values()[command] )
                {
                    case FULL_UPDATE:
                        System.out.println( "Receiving update..." );
                        new Game();

                        // Receive asteroids.
                        Game.getInstance().asteroidManager = new AsteroidManager( din );
                        Game.getInstance().addPlayer( ( Settings.playerName.equals( "" ) ? "Player" : Settings.playerName ) );
                        Game.getInstance().state = Game.Netstate.CLIENT;            
        
                        new AsteroidsFrame( 0 );
                        break;
                }
            }
        }
        catch ( IOException ex )
        {
            ex.printStackTrace();
        }
    }

    void keyStroke( int key )
    {
        try
        {
            // Send our connection request.
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream d = new DataOutputStream( b );

            d.writeInt( Message.KEYSTROKE.ordinal() );
            d.writeInt( key );
            sendPacket( server, b );
        }
        catch ( IOException ex )
        {
            ex.printStackTrace();
        }

    }
}
