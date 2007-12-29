/*
 * DISASTEROIDS
 * Network.java
 */

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.LinkedList;

/**
 *
 * @author Phillip Cohen
 * @since December 25, 2007
 */
public class Server extends DatagramListener
{

    public enum Messages
    {

        OK,
        FULL_UPDATE,
        PLAYER_UPDATE,
        NEXT_LEVEL;

    }
    public static int DEFAULT_PORT = 4919;
    private LinkedList<Client> clients;

    public Server()
    {
        clients = new LinkedList<Client>();
        new ListenerThread( this, 3344 ).start();
    }

    void parseReceived( DatagramPacket p )
    {
        try
        {
            ByteArrayInputStream bin =
                    new ByteArrayInputStream(
                    p.getData() );
            DataInputStream din =
                    new DataInputStream( bin );

            System.out.println( "Rec3ived from " + p.getAddress() + "." );

            long l = din.readShort();
            System.out.println( "Command is " + l );

            // send request
            byte[] buf = "Oddsbolikans".getBytes();
            System.out.println( p.getPort() );
            DatagramPacket packet = new DatagramPacket( buf, buf.length, p.getAddress(), p.getPort() );
            socket.send( packet );
        }
        catch ( IOException ex )
        {
            ex.printStackTrace();
            Running.quit();
        }

        Running.quit();
    }


    private class Client
    {

        public InetAddress address;

        public int numTimesSeen = 0;

        public Client()
        {
            numTimesSeen = 1;
        }
    }
}
