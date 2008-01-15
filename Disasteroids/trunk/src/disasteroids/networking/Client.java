/*
 * DISASTEROIDS
 * Client.java
 */
package disasteroids.networking;

import disasteroids.Asteroid;
import disasteroids.gui.AsteroidsFrame;
import disasteroids.Game;
import disasteroids.Running;
import disasteroids.Settings;
import disasteroids.Ship;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.swing.JOptionPane;

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
        KEYSTROKE,
        /**
         * Leaving the server.
         */
        QUITTING;

    }
    private static Client instance;

    public static Client getInstance()
    {
        return instance;
    }

    public static boolean is()
    {
        return ( instance != null );
    }

    /**
     * Binds this client to the given server, and connects to it. Assumes the default server port.
     * 
     * @param serverAddress     the IP address of the server
     * @throws java.net.UnknownHostException    if the given server can't be found
     * @since December 29, 2007
     */
    public Client( String serverAddress ) throws UnknownHostException
    {
        instance = this;
        server = new Machine( InetAddress.getByName( serverAddress ), Constants.DEFAULT_PORT );
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
            System.out.println( "Connecting to " + server + "..." );
            beginListening();

            ByteOutputStream out = new ByteOutputStream();

            // Send our connection request.
            out.writeInt( Message.CONNECT.ordinal() );

            // Send our name.
            out.writeUTF( Settings.getLocalName() );

            sendPacket( server, out );
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
            if ( server == null )
                return;
            
            // Ignore anything that isn't from the server.
            if ( new Machine( p.getAddress(), p.getPort() ).equals( server ) )
                server.see();
            else
                return;

            // Create stream.
            ByteInputStream in = new ByteInputStream( p.getData() );

            // Determine the type of message.
            int command = in.readInt();
            if ( ( command >= 0 ) && ( command < Server.Message.values().length ) )
            {
                switch ( Server.Message.values()[command] )
                {
                    case FULL_UPDATE:
                        System.out.print( "Receiving full update..." );

                        // Receive status of the entire game.
                        new Game( in );

                        // Find which player is ours (ID).
                        int id = in.readInt();
                        System.out.println( "...done. Our ID is: " + id + "." );

                        // Start the game.
                        new AsteroidsFrame( id );
                        break;
                    case PLAYER_UPDATE_POSITION:
                        Game.getInstance().getPlayerFromId( in.readInt() ).restorePosition( in );
                        break;
                    case NEW_ASTEROID:
                        Game.getInstance().asteroidManager().add( new Asteroid( in ), false );
                        break;
                    case REMOVE_ASTEROID:
                        Game.getInstance().asteroidManager().remove( in.readInt(), Game.getInstance().getPlayerFromId( in.readInt() ), false );
                        break;
                    case BERSERK:
                        Game.getInstance().getPlayerFromId( in.readInt() ).berserk();
                        break;
                    case PLAYER_JOINED:
                        Game.getInstance().addPlayer( new Ship( in ) );
                        break;
                    case PLAYER_QUIT:
                        String quitReason = in.readBoolean() ? " timed out." : " quit.";
                        Game.getInstance().removePlayer( Game.getInstance().getPlayerFromId( in.readInt() ), quitReason );
                        break;
                    case PAUSE:
                        Game.getInstance().setPaused( in.readBoolean() );
                        break;
                    case SERVER_QUITTING:
                        Game.getInstance().setPaused( true );
                        if ( AsteroidsFrame.frame() != null )
                            AsteroidsFrame.frame().dispose();
                        JOptionPane.showMessageDialog( null, "Server has quit.", "Disasteroids", JOptionPane.INFORMATION_MESSAGE );
                        Running.quit();
                        break;
                }
            }
        }
        catch ( IOException ex )
        {
            ex.printStackTrace();
        }
    }

    /**
     * Checks for a server timeout.
     * 
     * @return  whether the server has timed out
     * @since January 13, 2008
     */
    public boolean serverTimeout()
    {
        return server.shouldTimeout();
    }

    /**
     * Sends a local keystroke to the server.
     * 
     * @param key   the keycode (e.getKeyCode)
     * @since December 31, 2007
     */
    public void keyStroke( int key )
    {
        try
        {
            ByteOutputStream out = new ByteOutputStream();

            out.writeInt( Message.KEYSTROKE.ordinal() );
            out.writeInt( key );

            sendPacket( server, out );
        }
        catch ( IOException ex )
        {
            ex.printStackTrace();
        }
    }

    /**
     * Disconnects from this server.
     * 
     * @since January 1, 2007
     */
    public void quit()
    {
        try
        {
            ByteOutputStream out = new ByteOutputStream();

            out.writeInt( Message.QUITTING.ordinal() );

            sendPacket( server, out );
        }
        catch ( IOException ex )
        {
            ex.printStackTrace();
        }

        stopListening();
        server = null;
        instance = null;
    }
}
