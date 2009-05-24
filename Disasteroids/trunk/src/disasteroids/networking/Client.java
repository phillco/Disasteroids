/*
 * DISASTEROIDS
 * Client.java
 */
package disasteroids.networking;

import disasteroids.Asteroid;
import disasteroids.gui.AsteroidsFrame;
import disasteroids.Game;
import disasteroids.GameLoop;
import disasteroids.Running;
import disasteroids.Settings;
import disasteroids.Ship;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
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
    LinkedList<PacketSeries> packetSeries;

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
        packetSeries = new LinkedList<PacketSeries>();
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

            // Send our name and color.
            out.writeInt( Constants.NETCODE_VERSION );
            out.writeUTF( Settings.getPlayerName() );
            out.writeInt( Settings.getPlayerColor().getRGB() );

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
            // Ignore anything that isn't from the server.
            if ( server != null && new Machine( p.getAddress(), p.getPort() ).equals( server ) )
                server.see();
            else
                return;

            ByteInputStream in = new ByteInputStream( p.getData() );

            // Determine the type of message.
            int command = in.readInt();
            if ( ( command >= 0 ) && ( command < Server.Message.values().length ) )
            {
                switch ( Server.Message.values()[command] )
                {
                    case MULTI_PACKET:
                        processMultiPacket( p, in );
                        break;
                    case CONNECT_ERROR_OLDNETCODE:
                        Running.fatalError( "Couldn't connect because the server is using a newer version.\nTheirs: " + in.readInt() + "\nOurs: " + Constants.NETCODE_VERSION + "\n\nYou'll have to update." );
                        return;
                    case FULL_UPDATE:
                        System.out.print( "Receiving full update..." );

                        // Receive status of the entire game.
                        new Game( in );
                        int id = in.readInt();
                        System.out.println( "...done. Our ID is: " + id + "." );

                        // Start the game.
                        new AsteroidsFrame( id );
                        AsteroidsFrame.frame().showStartMessage( "Welcome to this server!\nPress F1 for help." );
                        break;
                    case PAUSE:
                        Game.getInstance().setPaused( in.readBoolean(), true );
                        break;
                    case SERVER_QUITTING:
                        GameLoop.stopLoop();
                        if ( AsteroidsFrame.frame() != null )
                            AsteroidsFrame.frame().dispose();
                        JOptionPane.showMessageDialog( null, "Server has quit.", "Disasteroids", JOptionPane.INFORMATION_MESSAGE );
                        Running.quit();
                        break;
                    case PLAYER_JOINED:
                        Game.getInstance().addPlayer( new Ship( in ) );
                        break;
                    case PLAYER_QUIT:
                        String quitReason = in.readBoolean() ? " timed out." : " quit.";
                        Game.getInstance().removePlayer( (Ship) Game.getInstance().getObjectManager().getObject( in.readInt() ), quitReason );
                        break;
                    case PLAYER_UPDATE_POSITION:
                        ( (Ship) Game.getInstance().getObjectManager().getObject( in.readInt() ) ).restorePosition( in );
                        break;
                    case PLAYER_BERSERK:
                        ( (Ship) Game.getInstance().getObjectManager().getObject( in.readInt() ) ).berserk();
                        break;
                    case PLAYER_STRAFE:
                        ( (Ship) Game.getInstance().getObjectManager().getObject( in.readInt() ) ).strafe( in.readBoolean() );
                        break;
                    case OBJECT_CREATED:
                        Game.getInstance().getObjectManager().addObjectFromStream( in );
                        break;
                    case OBJECT_REMOVED:
                        Game.getInstance().getObjectManager().removeObject( Game.getInstance().getObjectManager().getObject( in.readInt() ) );
                        break;
                    default:
                        System.out.println( "Weird packet - " + command + "." );
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
     * Handles a multipacket series.
     */
    private void processMultiPacket( DatagramPacket p, ByteInputStream in ) throws IOException
    {
        int seriesId = in.readInt();
        int count = in.readInt();
        int index = in.readInt();

        // Are we continuing an existing series?
        PacketSeries series = null;
        for ( PacketSeries s : packetSeries )
        {
            if ( s.getSeriesId() == seriesId )
            {
                series = s;
                break;
            }
        }

        if ( series == null )
        {
            // No, so start a new one.
            series = new PacketSeries( seriesId, count );
            packetSeries.add( series );
        }

        // Plug in this packet.
        series.addPacket( index, p );
        // System.out.println("Received packet " + index + "/" + count + " in series " + seriesId + " - " + hashPacket(p.getData()));

        // Does this complete the series? Rejoice!
        if ( series.isComplete() )
        {
            // System.out.println( "Series complete!\nContigous data: " + hashPacket(series.contiguousData));
            parseReceived( new DatagramPacket( series.getContiguousData(), 0, series.getContiguousData().length, server.address, server.port ) );
            packetSeries.remove( series );
        }
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
