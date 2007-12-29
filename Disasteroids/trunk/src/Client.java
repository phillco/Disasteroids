
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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

    public enum Messages
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
            new ListenerThread( this ).start();

            // Send our server challenge.
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream d = new DataOutputStream( b );
            d.writeShort( 108 );

            sendCommand( b.toByteArray() );
        }
        catch ( IOException ex )
        {
            Logger.getLogger( Client.class.getName() ).log( Level.SEVERE, null, ex );
        }
    }

    void sendCommand( ByteArrayOutputStream stream)
    {
        sendCommand(stream.toByteArray());
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
        System.out.println( "Mister client has received!" );
    }
}
