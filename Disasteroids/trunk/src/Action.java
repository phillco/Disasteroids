/*
 * DISASTEROIDS
 * Action.java
 */

/**
 * A ship's action (keystroke). Used for synchronization between computers.
 * @author Andy Kooiman
 * @since Classic
 */
public class Action
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
     * @see AsteroidsFrame#timestep
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
     * Applies the keystroke to the local game.
     * @since Classic
     */
    public void applyAction()
    {
        Running.environment().performAction( keyCode, actor );
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
}
