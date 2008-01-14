package disasteroids.networking;

/*
 * DISASTEROIDS
 * DatagramListener.java
 */
import disasteroids.Running;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * A superclass for both client and server. Implements all shared code.
 * @author Phillip Cohen
 * @since December 28, 2007
 */
public abstract class DatagramListener
{
    /**
     * Our socket for sending and receiving.
     * @since December 28, 2007
     */
    DatagramSocket socket;

    /**
     * Thread that listens for messages.
     * @since January 1, 2007
     */
    private ListenerThread ear;
    
    /**
     * Thread that calls <code>intervalLogic()</code> periodically.
     * @since January 13, 2008
     */
    private IntervalThread heart;

    /**
     * Starts listening for packets on a definite port (typical of servers).
     * 
     * @param port  port to listen on
     * @throws java.net.SocketException
     * @since December 28, 2007
     */
    void beginListening( int port ) throws SocketException
    {
        socket = new DatagramSocket( port );
        ear = new ListenerThread( this );
        heart = new IntervalThread( this );
    }

    /**
     * Starts listening for packets on any availible port Java can find us (typical of clients).
     * 
     * @since December 28, 2007
     * @throws java.net.SocketException
     */
    void beginListening() throws SocketException
    {
        socket = new DatagramSocket();
        ear = new ListenerThread( this );
        heart = new IntervalThread( this );
    }

    /**
     * Stops our listening and interval threads.
     * 
     * @since January 1, 2007
     */
    void stopListening()
    {
        ear.disable();        
        heart.disable();
        ear = null;
        heart = null;
    }

    /**
     * Called whenever we receive a <code>DatagramPacket</code> through our listener.
     * 
     * @param packet     the <code>DatagramPacket</code>
     */
    abstract void parseReceived( DatagramPacket packet );

    /**
     * Called every few seconds. You can use this method for logic (such as timeout checking) that should be independent of packets.
     * 
     * @see Constants#INTERVAL_TIME
     * @since January 13, 2008
     */
    void intervalLogic()
    {
        
    }

    /**
     * Sends a packet to a machine (a shortcut for stream.toByteArray).
     * bytestream->buffer->packet->machine
     * 
     * @param client    the client to send to
     * @param stream    the bytestream of data to be sent
     * @throws java.io.IOException 
     * @since December 29, 2007
     */
    void sendPacket( Machine client, ByteOutputStream stream ) throws IOException
    {
        sendPacket( client, stream.toByteArray() );
    }

    /**
     * Sends a packet to a machine.
     * buffer->packet->machine
     * 
     * @param client    the client to send to
     * @param buffer    the buffer of data to be sent
     * @throws java.io.IOException 
     * @since December 29, 2007
     */
    void sendPacket( Machine client, byte[] buffer ) throws IOException
    {
        socket.send( new DatagramPacket( buffer, buffer.length, client.address, client.port ) );
    }

    /**
     * A simple combined class for reading a byte array.
     * 
     * @since January 1, 2007
     */
    class ByteInputStream extends DataInputStream
    {
        /**
         * Constructs the data stream from a byte array.
         * 
         * @param buffer    the array of bytes to read
         * @since January 1, 2007
         */
        public ByteInputStream( byte[] buffer )
        {
            super( new ByteArrayInputStream( buffer ) );
        }
    }

    /**
     * A simple combined class for writing to a byte array.
     * 
     * @since January 1, 2007
     */
    class ByteOutputStream extends DataOutputStream
    {
        /**
         * Constructs the data stream to write to a byte array.
         * 
         * @since January 1, 2007
         */
        public ByteOutputStream()
        {
            super( new ByteArrayOutputStream() );
        }

        /**
         * Converts the written stream into a byte array.
         *
         * @return  the current contents of this output stream, as a byte array.
         * @see     java.io.ByteArrayOutputStream#toByteArray()
         * @since January 1, 2007
         */
        public byte[] toByteArray()
        {
            return ( (ByteArrayOutputStream) this.out ).toByteArray();
        }
    }

    /**
     * Child thread for DatagtramListener that can call its methods and be disabled.
     * @since January 13, 2008
     */
    class DatagramThread extends Thread
    {
        /**
         * Parent class that created this.
         * @since December 28, 2007
         */
        DatagramListener parent;

        /**
         * Whether we should continue running.
         * @since January 1, 2007
         */
        private boolean enabled = true;

        /**
         * Creates the thread.
         * 
         * @param parent    parent class
         * @since December 28, 2007
         */
        public DatagramThread( DatagramListener parent )
        {
            this.parent = parent;
        }

        /**
         * Stops the thread as soon as possible.
         * 
         * @since January 1, 2007
         */
        public void disable()
        {
            enabled = false;
        }

        public boolean shouldRun()
        {
            return enabled && ( parent != null );
        }
    }

    /**
     * A <code>Thread</code> that listens for packets and passes them to <code>parseReceived</code>.
     * @since December 28, 2007
     */
    class ListenerThread extends DatagramThread
    {
        /**
         * Creates the listener and starts it.
         * 
         * @param parent    parent class that implements <code>parseReceived</code>
         * @since December 28, 2007
         */
        public ListenerThread( DatagramListener parent )
        {
            super( parent );
            start();
        }

        @Override
        public void run()
        {
            try
            {
                // Continuously wait for packets.
                while ( shouldRun() )
                {
                    // Create a buffer to receive the packet in.
                    byte[] buffer = new byte[2048];
                    DatagramPacket packet = new DatagramPacket( buffer, buffer.length );

                    if ( shouldRun() )
                    {
                        // Receive it.
                        parent.socket.receive( packet );

                        // Pass it off.
                        parent.parseReceived( packet );
                    }
                }
            }
            catch ( IOException e )
            {
                Running.fatalError( "Listening error.", e );
            }
        }
    }

    private class IntervalThread extends DatagramThread
    {
        public IntervalThread( DatagramListener parent )
        {
            super( parent );
            setPriority( MIN_PRIORITY );
            start();
        }

        @Override
        public void run()
        {
            while ( shouldRun() )
            {
                try
                {
                    parent.intervalLogic();
                    Thread.sleep( Constants.INTERVAL_TIME * 1000 );
                }
                catch ( InterruptedException ex )
                {
                }
            }
        }
    }
}
