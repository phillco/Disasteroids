/*
 * DISASTEROIDS
 * Action.java
 */
package disasteroids.game;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import disasteroids.game.objects.Ship;

/**
 * A ship's action (the result of a keystroke).
 * @author Andy Kooiman
 */
public class Action
{
	/**
	 * The <code>Ship</code> that executed this action.
	 */
	private Ship actor;

	/**
	 * The key (action) that the <code>actor</code> executed.
	 */
	private int keyCode;

	/**
	 * The game time when the <code>this</code> is executed.
	 */
	private long timestep;

	/**
	 * Creates the action.
	 * 
	 * @param actor ship executing the command
	 * @param keyCode key code of the command.
	 * @param timestep game time in which the action occured
	 */
	public Action( Ship actor, int keyCode, long timestep )
	{
		this.actor = actor;
		this.keyCode = keyCode;
		this.timestep = timestep;
	}

	/**
	 * Returns the timestep in which the action was created.
	 */
	public long getTimestep()
	{
		return timestep;
	}

	/**
	 * Applies the action immediately.
	 */
	public void applyAction()
	{
		Game.getInstance().performAction( keyCode, actor );
	}

	/**
	 * Returns the <code>Ship</code> which invoked this action.
	 */
	public Ship getActor()
	{
		return actor;
	}

	/**
	 * Writes <code>this</code> to a stream for client/server transmission.
	 */
	public void flatten( DataOutputStream stream ) throws IOException
	{
		stream.writeLong( actor.getId() );
		stream.writeInt( keyCode );
		stream.writeLong( timestep );
	}

	/**
	 * Creates <code>this</code> from a stream for client/server transmission.
	 */
	public Action( DataInputStream stream ) throws IOException
	{
		actor = (Ship) Game.getInstance().getObjectManager().getObject( stream.readLong() );
		keyCode = stream.readInt();
		timestep = stream.readLong();
	}

	public int getKeyCode()
	{
		return keyCode;
	}
}
