/*
 * DISASTEROIDS
 * Server.java
 */
package disasteroids.networking;

import disasteroids.Action;
import disasteroids.Asteroid;
import disasteroids.Game;
import disasteroids.Running;
import disasteroids.Ship;
import java.awt.Color;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;

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
        /**
         * Multipacket coming in.
         */
        MULTI_PACKET,
        /**
         * Client was using an old version.
         */
        CONNECT_ERROR_OLDNETCODE,
        /**
         * We send the client all of the game's data to allow him to join.
         */
        FULL_UPDATE,
        /**
         * Updating a player's position and speed.
         */
        PLAYER_UPDATE_POSITION,
        /**
         * Asteroid has kill from a parent.
         */
        NEW_ASTEROID,
        /**
         * Existing asteroid being removed.
         */
        REMOVE_ASTEROID,
        /**
         * A new player has entered the game.
         */
        PLAYER_JOINED,
        /**
         * An existing player is quitting.
         */
        PLAYER_QUIT,
        /**
         * Server is pausing or unpausing the game.
         */
        PAUSE,
        /**
         * A ship has berserked.
         */
        BERSERK,
        /**
         * Server's going down!
         */
        SERVER_QUITTING;

    }
    /**
     * List of everyone who has sent us a packet.
     * @since December 29, 2007
     */
    private LinkedList<ClientMachine> clients;

    private static Server instance;

    public static Server getInstance()
    {
        return instance;
    }

    public static boolean is()
    {
        return ( instance != null );
    }

    /**
     * Starts the server and waits for clients.
     * 
     * @since December 28, 2007
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
        catch ( SocketException ex )
        {
            ex.printStackTrace();
        }
    }

    /**
     * Returns our IP address and port.
     * 
     * @return  the IP string ("xxx.xxx.xxx.xxx:1234")
     * @since Classic
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
            ClientMachine client = registerClient( new Machine( p.getAddress(), p.getPort() ) );

            // Create streams.
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
                            Iterator i;
                            out.writeInt( Message.CONNECT_ERROR_OLDNETCODE.ordinal() );
                            out.writeInt( Constants.NETCODE_VERSION );
                            sendPacket( client, out );
                            Running.log( "Connection from " + client.toString() + " refused, using old version: " + version + ".", 800 );
                            return;
                        }

                        Running.log( "Connection from " + client.toString() + " accepted." );

                        // Spawn him in (so he'll be included in the update).
                        int id = Game.getInstance().addPlayer( in.readUTF(), new Color( in.readInt() ) );

                        // Send him a full update.
                        out.writeInt( Message.FULL_UPDATE.ordinal() );

                        // Send the status of the entire game.
                        Game.getInstance().flatten( out );

                        // Send him his player ID.
                        out.writeInt( id );

                        // Associate this client with the ship.
                        client.inGamePlayer = Game.getInstance().getPlayerFromId( id );

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
                        out.writeInt( client.inGamePlayer.id );
                        
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
     * 
     * @param stream    the bytestream of data to be sent
     * @throws java.io.IOException
     * @since December 31, 2007
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
     * 
     * @param stream        the bytestream of data to be sent
     * @param excluded      <code>ClientMachine</code>s to skip
     * @throws java.io.IOException
     * @since December 31, 2007
     */
    void sendPacketToAllButOnePlayer( ByteOutputStream stream, ClientMachine excluded ) throws IOException
    {
        ClientMachine[] excludeList = new ClientMachine[1];
        excludeList[0] = excluded;
        sendPacketToMostPlayers( stream, excludeList );
    }

    /**
     * Sends a packet to all <code>clients</code> who are playing, except those on the <code>excludeList</code>.
     * 
     * @param stream        the bytestream of data to be sent
     * @param exludeList    array of <code>ClientMachine</code>s to exclude
     * @throws java.io.IOException
     * @since December 31, 2007
     */
    void sendPacketToMostPlayers( ByteOutputStream stream, ClientMachine[] exludeList ) throws IOException
    {
        byte[] message = stream.toByteArray();
        for ( ClientMachine c : clients )
        {
            for ( ClientMachine ex : exludeList )
            {
                if ( c != ex )
                {
                    if ( c.isInGame() )
                        sendPacket( c, message );
                }
            }
        }
    }

    /**
     * Takes the unknown IP and matches him to a <code>ClientMachine</code> on our <code>clients</code> list.
     * 
     * @param unknownMachine    the unknown <code>Machine</code>
     * @return  the client on the <code>clients</code> list
     * @since December 29, 2007
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
                        out.writeInt( cm.inGamePlayer.id );
                        sendPacketToAllButOnePlayer( out, cm );
                    }
                    catch (  IOException ex )
                    {

                    }
                }
                i.remove();
            }
        }
    }

    /**
     * Notifies all players about the <code>paused</code> state.
     * 
     * @param paused    whether the game is paused
     * @throws java.io.IOException 
     * @since December 31, 2007
     */
    public void updatePause( boolean paused ) throws IOException
    {
        ByteOutputStream out = new ByteOutputStream();

        out.writeInt( Message.PAUSE.ordinal() );
        out.writeBoolean( paused );

        sendPacketToAllPlayers( out );
    }

    /**
     * Shuts down the server and tells all clients to quit.
     * The local game can continue to run after this is called.
     * 
     * @since January 1, 2007
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
     * Updates a player's position and speed.
     * 
     * @param s player to update
     * @since January 2, 2008
     */
    public void updatePlayerPosition( Ship s )
    {
        try
        {
            ByteOutputStream out = new ByteOutputStream();
            out.writeInt( Message.PLAYER_UPDATE_POSITION.ordinal() );
            out.writeInt( s.id );
            s.flattenPosition( out );
            out.writeInt( s.getWeaponIndex() );
            sendPacketToAllPlayers( out );
        }
        catch ( IOException ex )
        {
            ex.printStackTrace();
        }
    }

    /**
     * Notifies clients of an asteroid being created.
     * 
     * @param a the asteriod
     * @since January 7, 2007
     */
    public void newAsteroid( Asteroid a )
    {
        try
        {
            ByteOutputStream out = new ByteOutputStream();
            out.writeInt( Message.NEW_ASTEROID.ordinal() );
            a.flatten( out );
            sendPacketToAllPlayers( out );
        }
        catch ( IOException ex )
        {
            ex.printStackTrace();
        }
    }

    /**
     * Notifies clients of an asteroid being removed.
     * 
     * @param id the asteriod's ID
     * @since January 10, 2007
     */
    public void removeAsteroid( int id, Ship killer )
    {
        try
        {
            ByteOutputStream out = new ByteOutputStream();
            out.writeInt( Message.REMOVE_ASTEROID.ordinal() );
            out.writeInt( id );

            if ( killer == null )
                out.writeBoolean( false );
            else
            {
                out.writeBoolean( true );
                out.writeInt( killer.id );
            }

            sendPacketToAllPlayers( out );
        }
        catch ( IOException ex )
        {
            ex.printStackTrace();
        }
    }

    /**
     * Notifies clients that the <code>Ship</code> with given id has just berserked.
     * 
     * @param id The id of the <code>Ship</code>
     */
    public void berserk( int id )
    {
        try
        {
            ByteOutputStream out = new ByteOutputStream();
            out.writeInt( Message.BERSERK.ordinal() );
            out.writeInt( id );
            sendPacketToAllPlayers( out );
        }
        catch ( IOException ex )
        {
            ex.printStackTrace();
        }
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
