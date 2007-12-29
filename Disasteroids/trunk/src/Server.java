/*
 * DISASTEROIDS
 * Network.java
 */

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Phillip Cohen
 * @since December 25, 2007
 */
public class Server extends DatagramListener
{

    public enum Message
    {

        OK,
        FULL_UPDATE,
        PLAYER_POSITION,
        PLAYER_SHOOT,
        NEXT_LEVEL;

    }
    public static int DEFAULT_PORT = 4919;

    private LinkedList<Connector> clients;

    public Server()
    {
        clients = new LinkedList<Connector>();
        beginListening( DEFAULT_PORT );
    }

    void parseReceived( DatagramPacket p )
    {
        try
        {
            // Ensure this client is on our register.
            Connector client = registerClient( new Connector( p.getAddress(), p.getPort() ) );

            // Create streams.
            ByteArrayInputStream bin = new ByteArrayInputStream( p.getData() );
            DataInputStream din = new DataInputStream( bin );

            // Determine the type of message.
            int command = din.readInt();
            if ( ( command >= 0 ) && ( command < Client.Message.values().length ) )
            {
                switch ( Client.Message.values()[command] )
                {

                    case CONNECT:
                        System.out.println( "Client wishes to connect." );

                        // Send him a full update.
                        ByteArrayOutputStream b = new ByteArrayOutputStream();
                        DataOutputStream d = new DataOutputStream( b );
                        d.writeInt( Message.FULL_UPDATE.ordinal() );

                        // Send asteroids.
                        ObjectOutputStream os = new ObjectOutputStream( new BufferedOutputStream( b ) );
                        os.flush();
                        os.writeObject( Game.getInstance().asteroidManager );
                        os.flush();

                        sendCommand( client, b );
                        break;

                    case KEYSTROKE:
                        System.out.println( "Client sends us a keystroke." );
                        break;
                }
            }


        // send request
//            byte[] buf = "Oddsbolikans".getBytes();
//            System.out.println( p.getPort() );
//            DatagramPacket packet = new DatagramPacket( buf, buf.length, p.getAddress(), p.getPort() );
//            socket.send( packet );
        }
        catch ( IOException ex )
        {
            ex.printStackTrace();
            Running.quit();
        }
    }

    void sendCommand( Connector c, ByteArrayOutputStream stream )
    {
        sendCommand( c, stream.toByteArray() );
    }

    void sendCommand( Connector c, byte[] buf )
    {
        try
        {
            socket.send( new DatagramPacket( buf, buf.length, c.address, c.port ) );
        }
        catch ( IOException ex )
        {
            Logger.getLogger( Client.class.getName() ).log( Level.SEVERE, null, ex );
        }
    }

    Connector registerClient( Connector newbie )
    {
        for ( Connector c : clients )
        {
            if ( c.equals( newbie ) )
            {
                c.numTimesSeen++;
                return c;
            }
        }
        clients.addLast( newbie );
        return newbie;
    }

    private class Connector
    {

        public InetAddress address;

        public int port;

        public int numTimesSeen = 0;

        public Ship inPlayer;

        public Connector( InetAddress address, int port )
        {
            this.address = address;
            this.port = port;
            numTimesSeen = 1;
        }

        public boolean equals( Connector other )
        {
            return true;
//            
//            for(int i = 0; i < this.address.getAddress().length; i++)
//            {
//                if(this.address.getAddress()[i] != other.address.getAddress()[i])
//                    return false;
//            }
//            return true;
        }
    }
}
