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
				Running.environment().writeOnBackground(killer.getWeaponManager().ApplyBonus(0),(int)x, (int) y, killer.getColor());
				break;
			case 1:
				Running.environment().writeOnBackground(killer.getWeaponManager().ApplyBonus(1),(int)x, (int) y, killer.getColor());
				break;
			case 2:
				Running.environment().writeOnBackground(killer.getWeaponManager().ApplyBonus(2),(int)x, (int) y, killer.getColor());
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
				Running.environment().writeOnBackground(killer.getWeaponManager().ApplyBonus(6),(int)x, (int) y, killer.getColor());
				break;
			case 4:
				Running.environment().writeOnBackground(killer.getWeaponManager().ApplyBonus(4),(int)x, (int) y, killer.getColor());
                        case 7:
				Running.environment().writeOnBackground(killer.getWeaponManager().ApplyBonus(7),(int)x, (int) y, killer.getColor());
			
		}
	}
}
