/*
 * DISASTEROIDS
 * DatagramListener.java
 */

import java.io.ByteArrayOutputStream;
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
     * Starts listening for packets on a definite port (typical of servers).
     * 
     * @param port  port to listen on
     * @throws java.net.SocketException     if the port is taken, or a general error
     * @since December 28, 2007
     */
    void beginListening( int port ) throws SocketException
    {
        socket = new DatagramSocket( port );
        ear = new ListenerThread( this );
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
    }
    
    /**
     * Stops the listening thread.
     * 
     * @since January 1, 2007
     */
    void stopListening()
    {
        ear.stopListening();
        ear = null;
    }
    /**
     * Called whenever we receive a <code>DatagramPacket</code> through our listener.
     * 
     * @param packet     the <code>DatagramPacket</code>
     */
    abstract void parseReceived( DatagramPacket packet );

    /**
     * Sends a packet to a machine (a shortcut for stream.toByteArray).
     * bytestream->buffer->packet->machine
     * 
     * @param client    the client to send to
     * @param stream    the bytestream of data to be sent
     * @throws java.io.IOException 
     * @since December 29, 2007
     */
    void sendPacket( Machine client, ByteArrayOutputStream stream ) throws IOException
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
     * A <code>Thread</code> that listens for packets and passes them to <code>parseReceived</code>.
     * @since December 28, 2007
     */
    class ListenerThread extends Thread
    {

        /**
         * Parent class that implements <code>parseReceived</code>.
         * @since December 28, 2007
         */
        private DatagramListener parent;
        
        /**
         * Whether we should be listening.
         * @since January 1, 2007
         */
        private boolean enabled = true;

        /**
         * Creates the listener and starts it.
         * 
         * @param parent    parent class that implements <code>parseReceived</code>
         * @since December 28, 2007
         */
        public ListenerThread( DatagramListener parent )
        {
            this.parent = parent;
            start();
        }

        @Override
        public void run()
        {
            try
            {
                // Continuously wait for packets.
                while ( enabled )
                {
                    // Create a buffer to receive the packet in.
                    byte[] buffer = new byte[2048];
                    DatagramPacket packet = new DatagramPacket( buffer, buffer.length );

                    if( enabled )
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
                System.out.println( "Listening error!" );
                e.printStackTrace();
            }
        }
        
        /**
         * Stops the listening loop.
         * 
         * @since January 1, 2007
         */
        public void stopListening()
        {
            enabled = false;
        }
    }
    /**
     * Represents an IP address and a port.
     * 
     * @since December 29, 2007
     */
    class Machine
    {

        /**
         * IP address of this machine.
         * @since December 29, 2007
         */
        public InetAddress address;

        /**
         * Port of this machine.
         * @since December 29, 2007
         */
        public int port;

        /**
         * Constructs the machine.
         * @param address   its IP address
         * @param port      its port
         */
        public Machine( InetAddress address, int port )
        {
            this.address = address;
            this.port = port;
        }

        /**
         * Returns if our IP and port are equal to another's.
         * @param other     the <code>Machine</code> to compare to
         * @return  <code>true</code> if we have the same IP and port; <code>false</code> otherwise.
         */
        public boolean equals( Machine other )
        {
            // Compare ports first.
            if ( this.port != other.port )
                return false;

            // Compare IP addresses. Inetaddress doesn't implement this (JDK 6).
            for ( int i = 0; i < this.address.getAddress().length; i++ )
            {
                if ( this.address.getAddress()[i] != other.address.getAddress()[i] )
                    return false;
            }

            // Same!
            return true;
        }
        
        @Override
        public String toString()
        {
            return address.toString() + ":" + port;
        }
    }
}
