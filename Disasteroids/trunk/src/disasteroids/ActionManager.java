/**
 * DISASTEROIDS
 * ActionManager.java
 */
package disasteroids;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The game's manager of <code>Ship</code> <code>Action</code>s.
 * @author Andy Kooiman
 * @since Classic
 */
public class ActionManager
{
    /**
     * The list of <code>Action</code> to be performed
     */
    private ConcurrentLinkedQueue<Action> theActions;

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
    public void act( long timestep )
    {

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
            else if ( a.timestep() < timestep )
            {
                // Not critical, but notable.
                Running.warning("Unsyncronized action at timestep " + timestep + " for actor " + a.actor() + ".");
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
        theActions.clear();
    }

    /**
     * Writes <code>this</code> to a stream for client/server transmission.
     * 
     * @param stream the stream to write to
     * @since December 30, 2007
     */
    public void flatten( DataOutputStream stream ) throws IOException
    {
        // Write the number of elements.
        stream.writeInt( theActions.size() );

        // Write the elements.
        for ( Action a : theActions )
        {
            a.flatten( stream );
        }
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
        {
            theActions.add( new Action( stream ) );
        }
    }
}
