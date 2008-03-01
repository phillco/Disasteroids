/*
 * DISASTEROIDS
 * PacketSeries.java
 */
package disasteroids.networking;

import disasteroids.Running;
import java.io.IOException;
import java.net.DatagramPacket;

/**
 * A series of split packets; used by clients to fuse and complete them.
 * @author Phillip Cohen
 * @since January 22, 2008
 */
public class PacketSeries
{
    /**
     * The individual packets in the series.
     * @since January 22, 2008
     */
    private DatagramPacket[] packets;

    /**
     * The series' unique ID.
     * @since January 22, 2008
     */
    private int seriesId;

    /**
     * The merged data.
     * @since January 22, 2008
     */
    private byte[] contiguousData;

    /**
     * Creates the packet series.
     * 
     * @param seriesId      the unique ID sent by the server
     * @param numPackets    the number of packets in the sequence
     * @since January 22, 2008
     */
    public PacketSeries( int seriesId, int numPackets )
    {
        this.seriesId = seriesId;
        packets = new DatagramPacket[numPackets];
        contiguousData = new byte[numPackets * Constants.MULTIPACKET_DATA_SIZE];
    }

    /**
     * Plugs a packet into the series.
     * 
     * @param index     the packet's position the sequence
     * @param p         the packet
     * @since January 22, 2008
     */
    public void addPacket( int index, DatagramPacket p )
    {
        packets[index] = p;
        try
        {
            DatagramListener.ByteInputStream in = new DatagramListener.ByteInputStream( p.getData() );

            // Read off the overhead.
            for ( int i = 0; i < Constants.MULTIPACKET_HEADER_SIZE; i++ )
                in.readByte();

            // Read in the data.
            in.read( contiguousData, index * Constants.MULTIPACKET_DATA_SIZE, Constants.MULTIPACKET_DATA_SIZE );
        }
        catch ( IOException ex )
        {
            Running.warning( "Multiseries read", ex );
        }
    }

    /**
     * Returns the array of merged data from the individual packets.
     * If a certain packet hasn't been received yet, its section will be filled with zeros.
     * 
     * @return  the data array
     * @since January 22, 2008
     */
    public byte[] getContiguousData()
    {
        return contiguousData;
    }

    /**
     * Returns the series' unique ID (assigned by the server).
     * 
     * @return  the ID
     * @since January 22, 2008
     */
    public int getSeriesId()
    {
        return seriesId;
    }

    /**
     * Returns whether we've received all of the packets in this series.
     * 
     * @return <code>true</code> if the sequence is complete; <code>false</code> if a packet is missing
     * @since January 22, 2008
     */
    public boolean isComplete()
    {
        for ( DatagramPacket p : packets )
            if ( p == null )
                return false;

        return true;
    }
}
