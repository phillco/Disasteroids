
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @since December 28, 2007
 * @author CHAFX005
 */
public abstract class DatagramListener
{

    DatagramSocket socket;

    void beginListening(int port)
    {
        try
        {
            socket = new DatagramSocket( port );
        }
        catch ( SocketException ex )
        {
            Logger.getLogger( DatagramListener.class.getName() ).log( Level.SEVERE, null, ex );
        }
    }
    
    void beginListening()
    {
        try
        {
            socket = new DatagramSocket();
        }
        catch ( SocketException ex )
        {
            Logger.getLogger( DatagramListener.class.getName() ).log( Level.SEVERE, null, ex );
        }
        new ListenerThread(this).start();
    }
    abstract void parseReceived( DatagramPacket p );

    class ListenerThread extends Thread
    {

        private DatagramListener parent;

        public ListenerThread( DatagramListener parent )
        {
            this.parent = parent;
        }

        @Override
        public void run()
        {
            try
            {
                listenLoop();
            }
            catch ( Exception e )
            {
                System.out.println( "Listening error." );
                e.printStackTrace();
            }
        }

        private void listenLoop() throws IOException
        {
            while ( true )
            {
                byte[] buf = new byte[256];

                // receive request
                DatagramPacket packet = new DatagramPacket( buf, buf.length );
                parent.socket.receive( packet );
                parent.parseReceived( packet );
            }
        }
    }
}
