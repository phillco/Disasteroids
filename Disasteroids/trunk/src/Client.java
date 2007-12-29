
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Phillip Cohen
 * @since December 28, 2007
 */
public class Client extends DatagramListener
{

    public enum Message
    {

        CONNECT,
        KEYSTROKE;

    }
    private InetAddress serverAddress;

    private int serverPort;

    public Client( String stringAddress )
    {
        try
        {
            serverAddress = InetAddress.getByName( stringAddress );
            serverPort = Server.DEFAULT_PORT;
            connect();
        }
        catch ( UnknownHostException ex )
        {
            Logger.getLogger( Client.class.getName() ).log( Level.SEVERE, null, ex );
        }
    }

    private void connect()
    {
        try
        {
            System.out.println( "Connecting to " + serverAddress );
            beginListening();

            // Send our connection request.
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream d = new DataOutputStream( b );
            d.writeInt( Message.CONNECT.ordinal() );
            sendCommand( b );
        }
        catch ( IOException ex )
        {
            Logger.getLogger( Client.class.getName() ).log( Level.SEVERE, null, ex );
        }
    }

    void sendCommand( ByteArrayOutputStream stream )
    {
        sendCommand( stream.toByteArray() );
    }

    void sendCommand( byte[] buf )
    {
        try
        {
            socket.send( new DatagramPacket( buf, buf.length, serverAddress, serverPort ) );
        }
        catch ( IOException ex )
        {
            Logger.getLogger( Client.class.getName() ).log( Level.SEVERE, null, ex );
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
                    case OK:
                    case FULL_UPDATE:
                        System.out.println( "Receiving update..." );
/*
                        ObjectInputStream is = new ObjectInputStream( new BufferedInputStream( bin ) );
                        .asteroidManager = (AsteroidManager) is.readObject();
                        System.out.println(.asteroidManager.size() + " asteroids.");
*/
                        break;
                }
            }
        }
     catch ( IOException ex )
        {
            Logger.getLogger( Client.class.getName() ).log( Level.SEVERE, null, ex );
        }
    }
}
