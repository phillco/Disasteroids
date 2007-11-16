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


public class ActionManager
{
	private LinkedList<Action> theActions;
	private LinkedList<Action> toBeAdded;
	
	public ActionManager()
	{
		theActions=new LinkedList<Action>();
		toBeAdded=new LinkedList<Action>();
	}
	
	public void act(long timestep) throws UnsynchronizedException
	{
		ListIterator<Action> itr3=toBeAdded.listIterator();
		while(itr3.hasNext())
		{
			Action a=itr3.next();
			theActions.add(a);
			itr3.remove();
		}



		if(theActions.size()==0)
			return;
		ListIterator<Action> itr=theActions.listIterator();
		while(itr.hasNext())
		{
			Action a=itr.next();
			if(a.timestep()==timestep)
			{
				if((a.actor()==Running.environment().getShip())== Running.environment().isPlayerOne())
				{
					a.applyAction();
					itr.remove();
				}
			}
			if(a.timestep()<timestep)
			{
				javax.swing.JOptionPane.showMessageDialog(null, "Action Missed");
				throw new UnsynchronizedException("An Action Was Missed:\n"+a.actor());
			}
		}
		ListIterator<Action> itr2=theActions.listIterator();
		while(itr2.hasNext())
		{
			Action a=itr2.next();
			if(a.timestep()==timestep)
			{
				a.applyAction();
				itr2.remove();
			}
		}
	}		
	
	public void add(Action a)
	{
		toBeAdded.addLast(a);
	}
	
	public void clear()
	{
		theActions.clear();
	}
}