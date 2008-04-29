/*
 * DISASTEROIDS
 * ServerCommaands.java
 */
package disasteroids.networking;

import disasteroids.Asteroid;
import disasteroids.Running;
import disasteroids.Ship;
import disasteroids.networking.DatagramListener.ByteOutputStream;
import java.io.IOException;

/**
 * All of the updating methods that the game calls to update clients.
 * @author Phillip Cohen
 */
public class ServerCommands
{
    /**
     * Notifies all players as to whether the game is paused.
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
            Running.warning( "Network stream failure, updatePause", ex );
        }
    }

    /**
     * Updates clients about a player's position and speed.
     */
    public static void updatePlayerPosition( Ship s )
    {
        try
        {
            ByteOutputStream out = new ByteOutputStream();
            out.writeInt( Server.Message.PLAYER_UPDATE_POSITION.ordinal() );
            out.writeInt( s.id );
            s.flattenPosition( out );
            out.writeInt( s.getWeaponIndex() );
            Server.getInstance().sendPacketToAllPlayers( out );
        }
        catch ( IOException ex )
        {
            Running.warning( "Network stream failure, updatePlayerPosition", ex );
        }
    }

    /**
     * Notifies clients of an asteroid being created.
     */
    public static void newAsteroid( Asteroid a )
    {
        try
        {
            ByteOutputStream out = new ByteOutputStream();
            out.writeInt( Server.Message.NEW_ASTEROID.ordinal() );
            a.flatten( out );
            Server.getInstance().sendPacketToAllPlayers( out );
        }
        catch ( IOException ex )
        {
            Running.warning( "Network stream failure, newAsteroid", ex );
        }
    }

    /**
     * Notifies clients of an asteroid being removed.
     * 
     * @param id the asteriod's ID
     */
    public static void removeAsteroid( int id, Ship killer )
    {
        try
        {
            ByteOutputStream out = new ByteOutputStream();
            out.writeInt( Server.Message.REMOVE_ASTEROID.ordinal() );
            out.writeInt( id );

            if ( killer == null )
                out.writeBoolean( false );
            else
            {
                out.writeBoolean( true );
                out.writeInt( killer.id );
            }

            Server.getInstance().sendPacketToAllPlayers( out );
        }
        catch ( IOException ex )
        {
            Running.warning( "Network stream failure, removeAsteroid", ex );
        }
    }

    /**
     * Notifies clients that the <code>Ship</code> with given id has just berserked.
     * 
     * @param id The id of the <code>Ship</code>
     */
    public static void berserk( int id )
    {
        try
        {
            ByteOutputStream out = new ByteOutputStream();
            out.writeInt( Server.Message.BERSERK.ordinal() );
            out.writeInt( id );
            Server.getInstance().sendPacketToAllPlayers( out );
        }
        catch ( IOException ex )
        {
            Running.warning( "Network stream failure, berkserk", ex );
        }
    }
}
