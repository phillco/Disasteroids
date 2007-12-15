/*
 * DISASTEROIDS
 * by Phillip Cohen and Andy Kooiman
 * 
 * APCS 1, 2006 to 2007, Period 3
 * Version - 1.0 Final (exam release)
 *
 * Run Running.class to start
 */

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Queue;

public class ActionManager {
    
    /**
     * The list of <code>Action</code> to be performed
     */
    private LinkedList<Action> theActions;
    
    /**
     * The list of <code>Action</code> to be added
     */
    private Queue<Action> toBeAdded;
    
    /**
     * Stores whether theActions should be cleared
     */
    private boolean clearAll;
    
    /**
     * Constructs a new instance of ActionManager
     * @since Classic
     */
    public ActionManager() {
        theActions = new LinkedList<Action>();
        toBeAdded = new LinkedList<Action>();
    }
    
    /**
     * Executes the <code>Action</code>s applying to the current timestep
     * @param timestep The current game time
     * @throws UnsynchronizedException If an <code>Action</code> was missed
     * @since Classic
     */
    public void act(long timestep) throws UnsynchronizedException {
        //Clear everything if necessary
        if (clearAll) {
            theActions.clear();
            toBeAdded.clear();
            clearAll = false;
            return;
        }
        //Get the ListIterator
        ListIterator<Action> itr = theActions.listIterator();

        //Add everything waiting
        while (!toBeAdded.isEmpty()) {
            itr.add(toBeAdded.remove());
        }
        while (itr.hasNext()) {
            Action a = itr.next();
            //Apply action if necessary
            if (a.timestep() == timestep) {
                    a.applyAction();
                    itr.remove();
            }
            //Check if its too late
            if (a.timestep() < timestep) {
                throw new UnsynchronizedException("Actor: " + a.actor());
            }
        }
        //Dispose of iterator
        itr = null;
    }
    
    /**
     * Adds an <code>Action</code> to be performed
     * @param a The <code>Action</code> to be performed
     * @since Classic
     */
    public void add(Action a) {
        toBeAdded.add(a);
    }
    
    /**
     * Removes all pending <code>Action</code>s
     * @since Classic
     */
    public void clear() {
        clearAll = true;
    }
}
