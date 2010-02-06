package disasteroids.networking;

/*
 * DISASTEROIDS
 * DatagramListener.java
 */
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

import disasteroids.Main;

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
	 * @since January 1, 2008
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
	 * @param port port to listen on
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
	 * @since January 1, 2008
	 */
	void stopListening()
	{
		if ( ear != null )
			ear.disable();

		if ( heart != null )
			heart.disable();
		ear = null;
		heart = null;
	}

	/**
	 * Called whenever we receive a <code>DatagramPacket</code> through our listener.
	 * 
	 * @param packet the <code>DatagramPacket</code>
	 */
	abstract void parseReceived( DatagramPacket packet );

	/**
	 * Called every few seconds. You can use this method for logic (such as timeout checking) that should be independent of
	 * packets.
	 * 
	 * @see Constants#INTERVAL_TIME
	 * @since January 13, 2008
	 */
	abstract void intervalLogic();

	/**
	 * Sends a packet to a machine (a shortcut for stream.toByteArray).
	 * bytestream->buffer->packet->machine
	 */
	void sendPacket( Machine destination, ByteOutputStream stream ) throws IOException
	{
		sendPacket( destination, stream.toByteArray() );
	}

	/**
	 * Returns a string with the packet's length and data (for debugging use).
	 * 
	 * @param data the packet's data
	 * @return the debugging string
	 * @since February 28, 2008
	 */
	String hashPacket( byte[] data )
	{
		return "Length " + data.length + " bytes. " + Arrays.toString( data );
	}

	/**
	 * Sends a packet to a machine.
	 * buffer->packet->machine
	 */
	void sendPacket( Machine destination, byte[] buffer ) throws IOException
	{
		// Will this packet have to be split?
		if ( buffer.length > Constants.MAX_PACKET_SIZE )
		{
			// Servers can't parse multipackets yet (no need to).
			if ( !( this instanceof Server ) )
			{
				Main.warning( "Trying to send a big packet to the server, which isn't supported. " );
				return;
			}

			int packetCount = (int) Math.ceil( (double) buffer.length / Constants.MULTIPACKET_DATA_SIZE );
			// System.out.println( "Original contiguous data: " + hashPacket( buffer ) + "\nWill need " + packetCount +
			// " packets ( each containing " + Constants.MULTIPACKET_DATA_SIZE + " bytes)." );

			// Create the series of packets.
			int seriesId = Machine.multPacketId++;
			for ( int i = 0; i < packetCount; i++ )
			{
				ByteOutputStream out = new ByteOutputStream();
				out.writeInt( ServerCommands.Message.MULTI_PACKET.ordinal() );
				out.writeInt( seriesId );
				out.writeInt( packetCount );
				out.writeInt( i );

				out.write( buffer, i * Constants.MULTIPACKET_DATA_SIZE, Math.min( buffer.length - i * Constants.MULTIPACKET_DATA_SIZE, Constants.MULTIPACKET_DATA_SIZE ) );
				// System.out.println( "Sending packet " + i + "/" + packetCount + " in series " + seriesId + " - " +
				// hashPacket( out.toByteArray() ) );
				sendPacket( destination, out );
			}
			System.out.println();
		}
		else
			socket.send( new DatagramPacket( buffer, buffer.length, destination.address, destination.port ) );

		destination.sentMessage();
	}

	/**
	 * A simple combined class for reading a byte array.
	 * 
	 * @since January 1, 2008
	 */
	static class ByteInputStream extends DataInputStream
	{
		/**
		 * Constructs the data stream from a byte array.
		 * 
		 * @param buffer the array of bytes to read
		 * @since January 1, 2008
		 */
		public ByteInputStream( byte[] buffer )
		{
			super( new ByteArrayInputStream( buffer ) );
		}
	}

	/**
	 * A simple combined class for writing to a byte array.
	 * 
	 * @since January 1, 2008
	 */
	static class ByteOutputStream extends DataOutputStream
	{
		/**
		 * Constructs the data stream to write to a byte array.
		 * 
		 * @since January 1, 2008
		 */
		public ByteOutputStream()
		{
			super( new ByteArrayOutputStream() );
		}

		/**
		 * Converts the written stream into a byte array.
		 * 
		 * @return the current contents of this output stream, as a byte array.
		 * @see java.io.ByteArrayOutputStream#toByteArray()
		 * @since January 1, 2008
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
	abstract class DatagramThread extends Thread
	{
		/**
		 * Parent class that created this.
		 * @since December 28, 2007
		 */
		DatagramListener parent;

		/**
		 * Whether we should continue running.
		 * @since January 1, 2008
		 */
		private boolean enabled = true;

		/**
		 * Creates the thread.
		 * 
		 * @param parent parent class
		 * @since December 28, 2007
		 */
		public DatagramThread( String name, DatagramListener parent )
		{
			super( name );
			setPriority( MAX_PRIORITY );
			this.parent = parent;
		}

		/**
		 * Stops the thread as soon as possible.
		 * 
		 * @since January 1, 2008
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
		 * @param parent parent class that implements <code>parseReceived</code>
		 * @since December 28, 2007
		 */
		public ListenerThread( DatagramListener parent )
		{
			super( "Network listening thread", parent );
			setPriority( MAX_PRIORITY );
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
					byte[] buffer = new byte[Constants.MAX_PACKET_SIZE];
					DatagramPacket packet = new DatagramPacket( buffer, buffer.length );

					if ( shouldRun() )
					{
						// Receive it.
						socket.receive( packet );

						// Pass it off.
						parseReceived( packet );
					}
				}
			}
			catch ( IOException e )
			{
				Main.fatalError( "Listening error.", e );
			}
		}
	}

	private class IntervalThread extends DatagramThread
	{
		public IntervalThread( DatagramListener parent )
		{
			super( "Network interval thread", parent );
			setPriority( MAX_PRIORITY );
			start();
		}

		@Override
		public void run()
		{
			while ( shouldRun() )
			{
				try
				{
					intervalLogic();
					Thread.sleep( Constants.INTERVAL_TIME * 1000 );
				}
				catch ( InterruptedException ex )
				{
				}
			}
		}
	}
}
