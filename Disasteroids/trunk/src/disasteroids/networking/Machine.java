/*
 * DISASTEROIDS | Networking
 * Machine.java
 */
package disasteroids.networking;

import java.net.InetAddress;

/**
 * Represents an IP address, port, and a last seen time.
 * 
 * @since December 29, 2007
 * @author Phillip Cohen
 */
public class Machine
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
     * The last time, in milliseconds, that we last saw this machine.
     * @see System#currentTimeMillis()
     * @since January 10, 2007
     */
    public long lastSeen;
    
    public static int multPacketId = 0;
    
    /**
     * Constructs the machine.
     * 
     * @param address   its IP address
     * @param port      its port
     */
    public Machine( InetAddress address, int port )
    {
        this.address = address;
        this.port = port;
        this.lastSeen = System.currentTimeMillis();
    }

    /**
     * Should be called whenever we receive a packet from this machine.
     * Updates our <code>lastSeen</code> field for timeout calculation.
     * 
     * @since January 10, 2007
     */
    public void see()
    {
        lastSeen = System.currentTimeMillis();
    }

    /**
     * Runs a check to see if this machine has timed out.
     * This is true when our <code>lastSeen</code> time is more than <code>Constants.TIMEOUT_TIME</code>.
     * 
     * @return  whether this machine has timed out
     * @since January 10, 2007
     */
    public boolean shouldTimeout()
    {
        return ( System.currentTimeMillis() - lastSeen >= Constants.TIMEOUT_TIME * 1000 );
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

    /**
     * Returns IP and port.
     * @return "xxx.xxx.xxx.xxx:xxxx"
     */
    @Override
    public String toString()
    {
        return address.toString() + ":" + port;
    }
}
