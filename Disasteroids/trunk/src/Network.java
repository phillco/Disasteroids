/*
 * DISASTEROIDS
 * Network.java
 */

import java.net.DatagramPacket;

/**
 *
 * @author Phillip Cohen
 * @since December 25, 2007
 */
public class Network
{

    private static boolean isNetworked = false;

    private static boolean isServer = false;

    public static void initAsLocal()
    {

    }

    public static void initAsNetwork( boolean startAsServer )
    {
        isServer = startAsServer;

    }
}
