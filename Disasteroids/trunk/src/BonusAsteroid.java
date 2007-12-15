/*
 * DISASTEROIDS
 * by Phillip Cohen and Andy Kooiman
 * 
 * APCS 1, 2006 to 2007, Period 3
 * Version - 1.0 Final (exam release)
 *
 * Run Running.class to start
 */
import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;



public class BonusAsteroid extends Asteroid
{
        /**
         * The code corresponding to the bonus carried by this <code>BonusAsteroid</code>
         * @since Classic
         */
	private int bonusType;
	
        /**
         * Creates a new instance of <code>BonusAsteroid</code>
         * @param x The x coordinate
         * @param y The y coordinate
         * @param dx The x velocity
         * @param dy The y velocity (up is negative)
         * @param size The diameter
         * @param g The <code>Graphics</code> context in which it will be drawn
         * @param environment The <code>AsteroidManager</code> to which it belongs
         * @since Classic
         */
	public BonusAsteroid(int x, int y, double dx, double dy, int size, Graphics g, AsteroidManager environment)
	{
		super(x,y,dx,dy,size,environment);
		Random rand=RandNumGen.getAsteroidInstance();
		bonusType=rand.nextInt(7);
	}
	
	/**
         * Draws <code>this</code>.  Draws identically to a normal <code>Asteroid</code> except that it is Gray with a White border
         * @since Classic
         */
    @Override
	protected void draw()
	{
                Graphics g = AsteroidsFrame.getGBuff();
		g.setColor(Color.gray);
		g.fillOval((int)(x-size/2),(int)(y-size/2),size,size);
		g.setColor(Color.white);
		g.drawOval((int)(x-size/2),(int)(y-size/2),size,size);

	}
	
    /**
     * Called when the <code>BonusAsteroid</code> is killed.  Splits into two normal <code>Asteroid</code>s and applys bonus
     * @param killer The <code>Ship</code> which killed <code>this</code>
     * @since Classic
     */
    @Override
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
	
        /**
         * Applies the bonus to the killer
         * @param killer The <code>Ship</code> which killed <code>this</code>
         * @since Classic
         */
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
