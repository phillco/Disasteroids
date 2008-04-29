/*
 * DISASTEROIDS
 * ServerCommaands.java
 */
package disasteroids.networking;

import disasteroids.Running;
import disasteroids.networking.DatagramListener.ByteOutputStream;
import java.io.IOException;

/**
 * All of the updating methods that the game calls to update clients.
 * @author Phillip Cohen
 */
public class ServerCommands
{
    /**
     * Notifies all players about the <code>paused</code> state.
     * 
     * @param paused    whether the game is paused
     */
    public static void updatePause( boolean paused )
    {
        try
        {
            ByteOutputStream out = new ByteOutputStream();

            out.writeInt( Server.Message.PAUSE.ordinal() );
            out.writeBoolean( paused );

            Server.getInstance().sendPacketToAllPlayers( out );
        }
        catch ( IOException ex )
        {
            Running.warning( "Network stream failure, updatePause", ex);
        }
    }
}
