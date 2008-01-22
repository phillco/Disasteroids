/*
 * DISASTEROIDS | Networking
 * Constants.java
 */
package disasteroids.networking;

/**
 * Storage class for network constants.
 * @author Phillip Cohen
 * @since January 13, 2008
 */
public class Constants
{
    /**
     * The default port that the server runs on.
     * @since December 29, 2007
     */
    public static int DEFAULT_PORT = 53;

    /**
     * Time (in seconds) that must elapse before a machine is considered to have timed out.
     * @since January 13, 2008
     */
    public static int TIMEOUT_TIME = 15;

    /**
     * Time (in seconds) between intervalLogic() calls.
     * @since January 13, 2008
     */
    public static int INTERVAL_TIME = 5;
    
    /**
     * The max size of a packet (bytes).
     * @since January 22, 2008
     */
    public static int MAX_PACKET_SIZE = 1024;

}
