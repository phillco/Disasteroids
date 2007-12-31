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
        FULL_UPDATE;

    }
    /**
     * List of everyone who has sent us a packet.
     * @since December 29, 2007
     */
    private LinkedList<ClientMachine> clients;

    /**
     * Starts the server and waits for clients.
     * 
     * @since December 28, 2007
     */
    public Server()
    {
        try
        {
            System.out.println( "== DISASTEROIDS SERVER == Started!" );
            clients = new LinkedList<ClientMachine>();
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
            ClientMachine client = registerClient( new Machine( p.getAddress(), p.getPort() ) );

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

                        ByteArrayOutputStream b = new ByteArrayOutputStream();
                        DataOutputStream d = new DataOutputStream( b );

                        // Send him a full update.
                        d.writeInt( Message.FULL_UPDATE.ordinal() );

                        // Spawn him in (so he'll be included in the update).
                        int id = Game.getInstance().addPlayer( din.readUTF() );

                        // Send status of the entire game.
                        Game.getInstance().flatten( d );

                        // Send him his player ID.
                        d.writeInt( id );

                        // Associate this client with the ship.
                        client.inGamePlayer = Game.getInstance().getFromId( id );

                        // Waddle this fat packet out the door.
                        sendPacket( client, b );

                        break;

                    // Client is sending us a keystroke.
                    case KEYSTROKE:
                        int keyCode = din.readInt();
                        Game.getInstance().actionManager.add( new Action( client.inGamePlayer, keyCode, Game.getInstance().timeStep + 2 ) );
                }
            }
        }
        catch ( IOException ex )
        {
            Running.fatalError( "Server parsing exception.", ex );
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
                return c;
        }

        // Nope. Add him.
        ClientMachine newClient = new ClientMachine( unknownMachine );
        clients.addLast( newClient );
        return newClient;
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
         * Returns if this is bound to a <code>Ship</code> in the game.
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
