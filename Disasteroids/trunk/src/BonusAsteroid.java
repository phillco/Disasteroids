/*
 * DISASTEROIDS
 * by Phillip Cohen and Andy Kooiman
 * 
 * APCS 1, 2006 to 2007, Period 3
 * Version - 1.0 Final (exam release)
 *
 * Run Running.class to start
 */
import java.util.Random;
import java.awt.*;
import java.util.ArrayList;


public class BonusAsteroid extends Asteroid
{
	private int bonusType;
	
	public BonusAsteroid(int x, int y, double dx, double dy, int size, Graphics g, AsteroidManager environment)
	{
		super(x,y,dx,dy,size,g,environment);
		Random rand=RandNumGen.getAsteroidInstance();
		bonusType=rand.nextInt(7);
	}
	
	
	protected void draw()
	{
		g.setColor(Color.gray);
		g.fillOval((int)(x-size/2),(int)(y-size/2),size,size);
		g.setColor(Color.white);
		g.drawOval((int)(x-size/2),(int)(y-size/2),size,size);

	}
	
	protected void split(Ship killer)
	{
		if(children>2)
		{
			shouldRemove=true;
			return;
		}
		if(size<25)
			shouldRemove=true;
		else
		{
			environment.add(new Asteroid((Asteroid)this));
			environment.add(new Asteroid((Asteroid)this));
			applyBonus(killer);
			shouldRemove=true;
		}
	}
	
	private void applyBonus(Ship killer)
	{
		switch (bonusType)
		{
			case 0:
				killer.getMisileManager().setHugeBlastProb(2);
				Running.environment().writeOnBackground("Huge Blast Probable", (int) x, (int) y, killer.getColor());
				break;
			case 1:
				killer.getMisileManager().setHugeBlastSize(100);
				Running.environment().writeOnBackground("Huge Blast Radius", (int) x, (int) y, killer.getColor());
				//AsteroidsFrame.setJuggernut(true);
				//Misile.increaseSpeed(1);
				break;
			case 2:
				killer.getMisileManager().setProbPop(500);
				Running.environment().writeOnBackground("Probability of Popping increased", (int) x, (int) y, killer.getColor());
				break;
			case 3:
				killer.addLife();
				Running.environment().writeOnBackground("+1 Life", (int) x, (int) y, killer.getColor());
				break;
			//case 4:
				//AsteroidsFrame.staticNextLevel();
				//break;
			case 5:
				killer.increaseScore(10000);
				Running.environment().writeOnBackground("+10,000 Points", (int) x, (int) y, killer.getColor());
				break;
			case 6:
				killer.setMaxShots(50);
				Running.environment().writeOnBackground("Max Shots => 50", (int) x, (int) y, killer.getColor());
				break;
			case 4:
				killer.getMisileManager().increasePopQuantity(15);
				Running.environment().writeOnBackground("Pop Quantity /\\ 15", (int) x, (int) y, killer.getColor());
			
		}
	}
}