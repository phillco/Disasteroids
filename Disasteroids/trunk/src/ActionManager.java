
/**
 * DISASTEROIDS
 * ActionManager.java
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The game's manager of <code>Ship</code> <code>Action</code>s.
 * @author Andy Kooiman
 * @since Classic
 */
public class ActionManager implements Serializable
{
    /**
     * The list of <code>Action</code> to be performed
     */
    private ConcurrentLinkedQueue<Action> theActions;

    /**
     * Stores whether theActions should be cleared
     */
    private boolean clearAll;

    /**
     * Constructs a new instance of ActionManager
     * @since Classic
     */
    public ActionManager()
    {
        theActions = new ConcurrentLinkedQueue<Action>();
    }

    /**
     * Executes the <code>Action</code>s applying to the current timestep
     * @param timestep The current game time
     * @throws UnsynchronizedException If an <code>Action</code> was missed
     * @since Classic
     */
    public void act( long timestep ) throws UnsynchronizedException
    {
        // Clear everything if necessary.
        if ( clearAll )
        {
            theActions.clear();
            clearAll = false;
            return;
        }

        Iterator<Action> itr = theActions.iterator();
        while ( itr.hasNext() )
        {
            Action a = itr.next();

            // Apply action if necessary
            if ( a.timestep() == timestep )
            {
                a.applyAction();
                itr.remove();
            }

            // Check if it's too late.
            if ( a.timestep() < timestep )
            {
                throw new UnsynchronizedException( "Actor: " + a.actor() );
            }
        }
    }

    /**
     * Adds an <code>Action</code> to be performed
     * @param a The <code>Action</code> to be performed
     * @since Classic
     */
    public void add( Action a )
    {
        theActions.add( a );
    }

    /**
     * Removes all pending <code>Action</code>s
     * @since Classic
     */
    public void clear()
    {
        clearAll = true;
    }

    /**
     * Writes <code>this</code> to a stream for client/server transmission.
     * 
     * @param d the stream to write to
     * @since December 30, 2007
     */
    public void flatten( DataOutputStream stream ) throws IOException
    {
        // Write the number of elements.
        stream.writeInt( theActions.size() );

        // Write the elements.
        for ( Action a : theActions )
            a.flatten( stream );
    }

    /**
     * Creates <code>this</code> from a stream for client/server transmission.
     * 
     * @param stream    the stream to read from (sent by the server)
     * @since December 30, 2007
     */
    public ActionManager( DataInputStream stream ) throws IOException
    {
        // Create new list.
        theActions = new ConcurrentLinkedQueue<Action>();
        int size = stream.readInt();

        // Import actions.
        for ( int i = 0; i < size; i++ )
            theActions.add( new Action( stream ) );
    }
}
