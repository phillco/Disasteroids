/*
 * DISASTEROIDS
 * BonusAsteroid.java
 */

import java.awt.Color;
import java.util.Random;

/**
 * A darker <code>Asteroid</code> that givs bonuses when shot.
 * @author Andy Kooiman
 */
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
	public BonusAsteroid(int x, int y, double dx, double dy, int size, AsteroidManager environment)
	{
		super(x,y,dx,dy,size,environment);
		Random rand=RandNumGen.getAsteroidInstance();
		bonusType=rand.nextInt(8);
                fill=Color.gray;
                outline=Color.white;
	}
	
    /**
     * Called when the <code>BonusAsteroid</code> is killed.
     * Splits into two normal <code>Asteroid</code>s and applies bonus.
     * @param killer The <code>Ship</code> which killed <code>this</code>.
     * @author Andy Kooiman
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
		if(radius<12)
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
				killer.getMissileManager().setHugeBlastProb(2);
				Running.environment().writeOnBackground("Huge Blast Probable", (int) x, (int) y, killer.getColor());
				break;
			case 1:
				killer.getMissileManager().setHugeBlastSize(100);
				Running.environment().writeOnBackground("Huge Blast Radius", (int) x, (int) y, killer.getColor());
				//AsteroidsFrame.setJuggernut(true);
				//Misile.increaseSpeed(1);
				break;
			case 2:
				killer.getMissileManager().setProbPop(500);
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
				killer.getMissileManager().increasePopQuantity(15);
				Running.environment().writeOnBackground("Pop Quantity /\\ 15", (int) x, (int) y, killer.getColor());
                       case 7:
                           killer.getMissileManager().setIntervalShoot(3);
                           Running.environment().writeOnBackground("Rapid Fire", (int)x, (int) y, killer.getColor());
			
		}
	}
}
