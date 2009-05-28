/*
 * DISASTEROIDS
 * ServerCommaands.java
 */
package disasteroids.networking;

import disasteroids.GameObject;
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
            out.writeLong( s.getId() );
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
     * Notifies clients that the <code>Ship</code> with given id has just berserked.
     */
    public static void berserk( long id )
    {
        try
        {
            ByteOutputStream out = new ByteOutputStream();
            out.writeInt( Server.Message.PLAYER_BERSERK.ordinal() );
            out.writeLong( id );
            Server.getInstance().sendPacketToAllPlayers( out );
        }
        catch ( IOException ex )
        {
            Running.warning( "Network stream failure, berkserk", ex );
        }
    }

    /**
     * Notifies clients that the <code>Ship</code> with given id has just strafed.
     */
    public static void strafe( long id, boolean toRight )
    {
        try
        {
            ByteOutputStream out = new ByteOutputStream();
            out.writeInt( Server.Message.PLAYER_STRAFE.ordinal() );
            out.writeLong( id );
            out.writeBoolean( toRight );
            Server.getInstance().sendPacketToAllPlayers( out );
        }
        catch ( IOException ex )
        {
            Running.warning( "Network stream failure, strafe", ex );
        }
    }

    /**
     * Notifies clients that an object was created.
     */
    public static void objectCreatedOrDestroyed( GameObject go, boolean created )
    {
        try
        {
            ByteOutputStream out = new ByteOutputStream();
            if ( created )
            {
                out.writeInt( Server.Message.OBJECT_CREATED.ordinal() );
                out.writeInt( Constants.parseGameObject( go ) );
                go.flatten( out );
            }
            else
            {
                out.writeInt( Server.Message.OBJECT_REMOVED.ordinal() );
                out.writeLong( go.getId() );
            }
            Server.getInstance().sendPacketToAllPlayers( out );
        }
        catch ( IOException ex )
        {
            Running.warning( "Network stream failure, objectCreatedOrDestroyed", ex );
        }
    }
}
