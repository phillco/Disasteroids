
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * @since December 28, 2007
 * @author CHAFX005
 */
public abstract class DatagramListener
{

    DatagramSocket socket;

    abstract void parseReceived( DatagramPacket p );

    class ListenerThread extends Thread
    {

        private DatagramListener parent;

        private int port;

        public ListenerThread( DatagramListener parent )
        {
            this( parent, 0 );
        }

        public ListenerThread( DatagramListener parent, int port )
        {
            this.parent = parent;
            this.port = port;
        }

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
            if ( port == 0 )
                parent.socket = new DatagramSocket();
            else
                parent.socket = new DatagramSocket( port );

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
