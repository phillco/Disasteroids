/*
 * DISASTEROIDS
 * Server.java
 */
package disasteroids.networking;

import disasteroids.Action;
import disasteroids.Game;
import disasteroids.Running;
import disasteroids.Ship;
import java.awt.Color;
import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 * Server side of the C/S networking.
 * @author Phillip Cohen
 * @since December 25, 2007
 */
public class Server extends DatagramListener
{
    /**
     * Messages that we send to the client.
     * @since December 29, 2007
     */
    public enum Message
    {
        //====================
        // GENERAL NETWORKING
        //====================
        MULTI_PACKET,
        CONNECT_ERROR_OLDNETCODE,
        FULL_UPDATE,
        PAUSE,
        SERVER_QUITTING,
        //=================
        // PLAYER COMMANDS
        //=================
        PLAYER_JOINED,
        PLAYER_QUIT,
        PLAYER_UPDATE_POSITION,
        PLAYER_STRAFE,
        PLAYER_BERSERK,
        //======
        // MISC
        //======
        OBJECT_CREATED,
        OBJECT_REMOVED;

    }
    /**
     * List of everyone who has sent us a packet.
     * @since December 29, 2007
     */
    private LinkedList<ClientMachine> clients;

    /**
     * The one server instance.
     */
    private static Server instance;

    /**
     * Starts the server, and waits for clients.
     */
    public Server()
    {
        instance = this;
        System.out.println( "== SERVER v" + Constants.NETCODE_VERSION + " == started at IP address: " + getLocalIP() );
        clients = new LinkedList<ClientMachine>();

        try
        {
            beginListening( Constants.DEFAULT_PORT );
        }
        catch ( BindException e )
        {
            Running.fatalError( "Couldn't bind to port " + Constants.DEFAULT_PORT + ". Perhaps a server is already running?\n\n" + e.getLocalizedMessage() );
        }
        catch ( SocketException ex )
        {
            ex.printStackTrace();
        }
    }

    /**
     * Returns the running server instance.
     */
    public static Server getInstance()
    {
        return instance;
    }

    /**
     * Is Disasteroids running as a server?
     */
    public static boolean is()
    {
        return ( instance != null );
    }

    /**
     * Called when we receive a packet from a client.
     * Deciphers what the client is telling us, and responds.
     */
    void parseReceived( DatagramPacket p )
    {
        try
        {
            // Ensure this client is on our register.
            ClientMachine client = registerClient( new Machine( p.getAddress(), p.getPort() ) );

            ByteInputStream in = new ByteInputStream( p.getData() );

            // Determine the type of message.
            int command = in.readInt();
            if ( ( command >= 0 ) && ( command < Client.Message.values().length ) )
            {
                switch ( Client.Message.values()[command] )
                {
                    // Client wants to join the game.
                    case CONNECT:

                        ByteOutputStream out = new ByteOutputStream();

                        // If he's using an older netcode version, slam the door in his face.
                        int version = in.readInt();
                        if ( version < Constants.NETCODE_VERSION )
                        {
                            out.writeInt( Message.CONNECT_ERROR_OLDNETCODE.ordinal() );
                            out.writeInt( Constants.NETCODE_VERSION );
                            sendPacket( client, out );
                            Running.log( "Connection from " + client.toString() + " refused, using old version: " + version + ".", 800 );
                            return;
                        }
                        Running.log( "Connection from " + client.toString() + " accepted." );

                        // Spawn him in (so he'll be included in the update).
                        long id = Game.getInstance().addPlayer( in.readUTF(), new Color( in.readInt() ) );

                        // Send him a full update.
                        out.writeInt( Message.FULL_UPDATE.ordinal() );
                        Game.getInstance().flatten( out );
                        out.writeLong( id );

                        // Associate this client with the ship.
                        client.inGamePlayer = (Ship) Game.getInstance().getObjectManager().getObject( id );

                        // Waddle this fat packet out the door!
                        sendPacket( client, out );

                        // Tell everyone else about the player joining.
                        out = new ByteOutputStream();
                        out.writeInt( Message.PLAYER_JOINED.ordinal() );
                        client.inGamePlayer.flatten( out );
                        sendPacketToAllButOnePlayer( out, client );
                        break;

                    // Client is sending us a keystroke.
                    case KEYSTROKE:

                        if ( !client.isInGame() )
                            break;

                        int keyCode = in.readInt();
                        Game.getInstance().getActionManager().add( new Action( client.inGamePlayer, keyCode, Game.getInstance().timeStep + 2 ) );
                        break;

                    // Client wishes to resume life.
                    case QUITTING:

                        if ( !client.isInGame() )
                            break;

                        Game.getInstance().removePlayer( client.inGamePlayer );

                        // Tell everyone else.
                        out = new ByteOutputStream();

                        out.writeInt( Message.PLAYER_QUIT.ordinal() );
                        out.writeBoolean( false ); // Not a timeout.
                        out.writeLong( client.inGamePlayer.getId() );

                        sendPacketToAllButOnePlayer( out, client );
                }
            }
        }
        catch ( IOException ex )
        {
            Running.fatalError( "Server parsing exception.", ex );
        }
    }

    /**
     * Sends a packet to all <code>clients</code> who are playing.
     */
    void sendPacketToAllPlayers( ByteOutputStream stream ) throws IOException
    {
        if ( clients == null )
            return;

        byte[] message = stream.toByteArray();
        for ( ClientMachine c : clients )
            if ( c.isInGame() )
                sendPacket( c, message );
    }

    /**
     * Sends a packet to all <code>clients</code> who are playing, except <code>excluded</code>.
     */
    void sendPacketToAllButOnePlayer( ByteOutputStream stream, ClientMachine excluded ) throws IOException
    {
        Set<ClientMachine> excludeList = new HashSet<ClientMachine>();
        excludeList.add( excluded );
        sendPacketToMostPlayers( stream, excludeList );
    }

    /**
     * Sends a packet to all <code>clients</code> who are playing, except those on the <code>excludeList</code>.
     */
    void sendPacketToMostPlayers( ByteOutputStream stream, Set<ClientMachine> excludeList ) throws IOException
    {
        byte[] message = stream.toByteArray();
        for ( ClientMachine c : clients )
        {
            if ( !excludeList.contains( c ) )
            {
                if ( c.isInGame() )
                    sendPacket( c, message );
            }
        }
    }

    /**
     * Returns our IP address and port. ("xxx.xxx.xxx.xxx:1234")
     */
    public static String getLocalIP()
    {
        try
        {
            InetAddress localHost = InetAddress.getLocalHost();
            InetAddress[] all_IPs = InetAddress.getAllByName( localHost.getHostName() );
            return ( all_IPs[0].toString().split( "/" ) )[1] + ":" + Constants.DEFAULT_PORT;
        }
        catch ( UnknownHostException e )
        {
            return "Unknown";
        }
    }

    /**
     * Takes the unknown IP and matches him to a <code>ClientMachine</code> on our <code>clients</code> list.
     */
    ClientMachine registerClient( Machine unknownMachine )
    {
        // See if he's on the list.
        for ( ClientMachine c : clients )
        {
            if ( c.equals( unknownMachine ) )
            {
                c.see();
                return c;
            }
        }

        // Nope. Add him.
        ClientMachine newClient = new ClientMachine( unknownMachine );
        clients.addLast( newClient );
        return newClient;
    }

    @Override
    void intervalLogic()
    {
        // Check for timeouts.
        Iterator<ClientMachine> i = clients.iterator();
        while ( i.hasNext() )
        {
            ClientMachine cm = i.next();
            if ( cm.shouldTimeout() )
            {
                // Remove the player.
                if ( cm.isInGame() )
                {
                    Game.getInstance().removePlayer( cm.inGamePlayer, "" );
                    Running.log( cm.inGamePlayer.getName() + " timed out." );

                    try
                    {
                        // Tell clients.
                        ByteOutputStream out = new ByteOutputStream();

                        out.writeInt( Message.PLAYER_QUIT.ordinal() );
                        out.writeBoolean( true );
                        out.writeLong( cm.inGamePlayer.getId() );
                        sendPacketToAllButOnePlayer( out, cm );
                    }
                    catch ( IOException ex )
                    {
                    }
                }
                i.remove();
            }
        }
    }

    /**
     * Shuts down the server and tells all clients to quit.
     * The local game can continue to run after this is called.
     */
    public void quit()
    {
        stopListening();
        try
        {
            ByteOutputStream out = new ByteOutputStream();
            out.writeInt( Message.SERVER_QUITTING.ordinal() );
            sendPacketToAllPlayers( out );
        }
        catch ( IOException ex )
        {
            ex.printStackTrace();
        }
        clients = null;
        socket = null;
    }

    /**
     * An extension of <code>Machine</code> that represents anyone who has pinged us.
     * If that person is in the game, he's bound to his <code>Ship</code> here.
     * 
     * @since December 31, 2007
     */
    private class ClientMachine extends Machine
    {
        /**
         * The in-game <code>Ship</code> this client is bound to.
         * If the client isn't in the game, it's <code>null</code>.
         */
        public Ship inGamePlayer;

        /**
         * Creates <code>this</code> from an existing machine.
         * 
         * @param m     the <code>Machine</code> to extend
         * @since December 31, 2007
         */
        public ClientMachine( Machine m )
        {
            super( m.address, m.port );
        }

        /**
         * Returns if this is client is in the game.
         * 
         * @see inGamePlayer
         * @return  if this is bound to a <code>Ship</code> in the game
         * @since December 31, 2007
         */
        public boolean isInGame()
        {
            return ( inGamePlayer != null );
        }
    }
}
