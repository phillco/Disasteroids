/*
 * DISASTEROIDS
 * by Phillip Cohen and Andy Kooiman
 * 
 * APCS 1, 2006 to 2007, Period 3
 * Version - 1.0 Final (exam release)
 *
 * Run Running.class to start
 */
//this simple class is going to store information about 
//actions that will happen so that they will happen at the 
//same time on the other computer

public class Action
{
	private Ship actor;
	private int keyCode;
	private long timestep;
	
	public Action(Ship actor, int keyCode, long timestep)
	{
		this.actor=actor;
		this.keyCode=keyCode;
		this.timestep=timestep;
	}
	
	public long timestep()
	{
		return timestep;
	}
	
	public void applyAction()
	{
		Running.environment().performAction(keyCode, actor);
	}
	
	public Ship actor()
	{
		return actor;
	}
}