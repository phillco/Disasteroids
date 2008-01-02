/*
 * DISASTEROIDS
 * Action.java
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * A ship's action (keystroke). Used for synchronization between computers.
 * @author Andy Kooiman
 * @since Classic
 */
public class Action implements Serializable
{

    /**
     * The <code>Ship</code> that executed this action.
     * @since Classic
     */
    private Ship actor;

    /**
     * The key (action) that the <code>actor</code> executed.
     * @since Classic
     */
    private int keyCode;

    /**
     * The game time when the <code>this</code> is executed.
     * @since Classic
     */
    private long timestep;

    /**
     * Creates the action.
     * 
     * @param actor     ship executing the command
     * @param keyCode   key code of the command.
     * @param timestep  game time in which the action occured
     * @since Classic
     */
    public Action( Ship actor, int keyCode, long timestep )
    {
        this.actor = actor;
        this.keyCode = keyCode;
        this.timestep = timestep;
    }

    /**
     * Returns the timestep in which the action was created.
     * @return  the timestep <code>this</code> was created
     * @since Classic
     */
    public long timestep()
    {
        return timestep;
    }

    /**
     * Applies the keystroke to the local Game.getInstance().
     * @since Classic
     */
    public void applyAction()
    {
        Game.performAction( keyCode, actor );
    }

    /**
     * Returns the <code>Ship</code> which invoked this action.
     * @return  the <code>Ship</code> which created <code>this</code>
     * @since Classic
     */
    public Ship actor()
    {
        return actor;
    }
    
     /**
     * Writes <code>this</code> to a stream for client/server transmission.
     * 
     * @param stream the stream to write to
      * @throws java.io.IOException 
      * @since December 30, 2007
     */
    public void flatten( DataOutputStream stream ) throws IOException
    {
        stream.writeInt(actor.id);
        stream.writeInt(keyCode);
        stream.writeLong(timestep);                        
    }

    /**
     * Creates <code>this</code> from a stream for client/server transmission.
     * 
     * @param stream    the stream to read from (sent by the server)
     * @throws java.io.IOException 
     * @since December 30, 2007
     */
    public Action( DataInputStream stream ) throws IOException
    {
        actor = Game.getInstance().getFromId(stream.readInt());
        keyCode = stream.readInt();
        timestep = stream.readLong();
    }
}
