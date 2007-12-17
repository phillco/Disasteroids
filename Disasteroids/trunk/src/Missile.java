/*
 * DISASTEROIDS
 * Missile.java
 */

import java.awt.Color;
import java.util.Random;

/**
 * The bullets that each <code>Ship</code> shoots.
 * @author Andy Kooiman
 */
public class Missile implements Weapon
{
    /**
     * The <code>Color</code> to be drawn in.
     * @since Classic
     */
    private Color myColor;

    /**
     * The x and y coordinates.
     * @since Classic
     */
    private double x,  y;

    /**
     * The x and y components of velocity.
     * @since Classic
     */
    private double dx,  dy;

    /**
     * The angle the <code>Missile</code> is pointing (not necessarily the angle at which it is moving).
     * @since Classic
     */
    private double angle;

    /**
     * How long <code>this</code> has been in existance.
     * @since Classic
     */
    private int age;

    /**
     * The current stage of explosion.
     * @since Classic
     */
    private int explodeCount = 0;

    /**
     * Whether <code>this</code> is currently exploding.
     * @since Classic
     */
    private boolean isExploding;

    /**
     * The current radius of <code>this</code>.
     * @since Classic
     */
    private int radius;

    /**
     * The age at which <code>this></code> will automatically explode.
     * @since Classic
     */
    private static int life = 300;

    /**
     * Whether <code>this</code> will have a huge blast or not.
     * @since Classic
     */
    private boolean hugeBlast;

    /**
     * The <code>MissileManager</code> to which <code>this</code> belongs.
     * @since Classic
     */
    private MissileManager manager;

    /**
     * Whether <code>this</code> should be removed.
     * @since Classic
     */
    private boolean needsRemoval = false;

    /**
     * Constructs a new instance of <code>Missile</code>.
     * @param m The <code>MissileManager</code> responsible for <code>this</code>.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param angle The angle to be pointing.
     * @param dx The x velocity.
     * @param dy The y velocity (up is negative).
     * @param c The <code>Color to be drawn in.
     * @author Andy Kooiman
     * @since Classic
     */
    public Missile( MissileManager m, int x, int y, double angle, double dx, double dy, Color c )
    {
        manager = m;
        setData( x, y, angle, dx, dy, c );
    }

    /**
     * A utility method called by the constructor to intialize the object.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param angle The angle to be pointing.
     * @param dx The x velocity.
     * @param dy The y velocity (up is negative).
     * @param c The <code>Color to be drawn in.
     * @author Andy Kooiman
     * @since Classic
     */
    private void setData( int x, int y, double angle, double dx, double dy, Color c )
    {
        age = 0;
        this.x = x;
        this.y = y;
        this.dx = /*manager.speed() * Math.cos( angle )*/  dx;
        this.dy =/* -manager.speed() * Math.sin( angle ) */ dy;
        this.angle = angle;
        radius = 3;
        explodeCount = 0;
        isExploding = false;
        myColor = c;
        hugeBlast = ( RandNumGen.getMissileInstance().nextInt( manager.hugeBlastProb() ) <= 1 );
    }

	/**
         * Draws <code>this</code>
         * @param g The <code>Graphics</code> context in which to be drawn
         * @since Classic
         */
	public void draw()
	{
            Running.environment().drawLine(myColor,(int)x+1,(int)y+1,10,angle+Math.PI);
            Running.environment().fillCircle(myColor, (int)x,(int)y, radius);
	}

    /**
     * Moves <code>this</code> according to its speed.
     * @author Andy Kooiman
     * @since Classic
     */
    public synchronized void move()
    {
        x += dx;
        y += dy;
        dx+=manager.speed()*Math.cos(angle)/50;
        dy-=manager.speed()*Math.sin(angle)/50;
        dx*=.98;
        dy*=.98;
    }

    /**
     * Steps <code>this</code> through one iteration and draws it.
     * @param g The <code>Graphics</code> context in which to be drawn.
     * @author Andy Kooiman
     * @since Classic
     */
    public synchronized void act()
    {
        if ( age<30 )
        {
            Random rand = RandNumGen.getParticleInstance();
            for ( int i = 0; i < (int) ( 7-Math.sqrt( dx * dx + dy * dy ) ); i++ )
                ParticleManager.addParticle( new Particle(
                                             x + rand.nextInt( 8 ) - 4,
                                             y + rand.nextInt( 8 ) - 4,
                                             rand.nextInt( 4 ),
                                             myColor,
                                             rand.nextDouble() * 3,
                                             angle + rand.nextDouble() * .4 - .2 + Math.PI,
                                             30, 10 ) );
        }
        age++;
        move();
        checkWrap();
        checkLeave();
        draw();
        explode( explodeCount);
    }

    /**
     * Checks to see if <code>this</code> has left the screen and adjusts accordingly.
     * @author Andy Kooiman
     * @since Classic
     */
    private synchronized void checkWrap()
    {
        // Wrap to stay inside the level.
        if ( x < 0 )
            x += Running.environment().getWidth() - 1;
        if ( y < 0 )
            y += Running.environment().getHeight() - 1;
        if ( x > Running.environment().getWidth() )
            x -= Running.environment().getWidth() - 1;
        if ( y > Running.environment().getHeight() )
            y -= Running.environment().getHeight() - 1;
    }

    /**
     * Checks the age of <code>this</code> and starts the explosion sequence if too old.
     * @author Andy Kooiman
     * @since Classic
     */
    private synchronized void checkLeave()
    {
        if ( age > life )
            explode();
    }

    /**
     * Initiates the explosion sequence.
     * @author Andy Kooiman
     * @since Classic
     */
    public synchronized void explode()
    {
        if ( isExploding )
            return;
        
        // Simply pop into several other <code>Missiles</code>.
        if ( RandNumGen.getMissileInstance().nextInt( manager.probPop() ) <= 101 )
            pop();

        explodeCount = 1;
        isExploding = true;
    }

    /**
     * Steps <code>this</code> through the explosion sequence and draws.
     * @param explodeCount The current stage of the explosion.
     * @param g The <code>Graphics</code> context in which to be drawn.
     * @author Andy Kooiman
     * @since Classic
     */
    private synchronized void explode( int explodeCount)
    {
            Color col;
		switch (explodeCount)
		{
			case 0:
				return;
			case 1:	case 2:	case 3:	case 4:
				dx*=.8;
				dy*=.8;
				radius=3;
				if(explodeCount%2==0)
					col=myColor;
				else
					col=Color.yellow;
				Running.environment().fillCircle(col,(int)x,(int)y,radius);
				this.explodeCount++;
				break;
			case 5:	case 6:	case 7:	case 8:
				dx*=.8;
				dy*=.8;
				if(explodeCount%2==0)
					col=myColor;
				else
					col=Color.yellow;
				radius=5;
				Running.environment().fillCircle(col,(int)x,(int)y,radius);
				this.explodeCount++;
				break;
			case 9:	case 10: case 11:
				dx*=.8;
				dy*=.8;
				if(hugeBlast)
				{
					col=myColor;
					radius=manager.hugeBlastSize();
					Running.environment().fillCircle(col,(int)x,(int)y,radius);
					this.explodeCount++;
				}
				else
				{
					radius=14;
					col=Color.yellow;
					Running.environment().fillCircle(col,(int)x,(int)y,radius);
					this.explodeCount++;
				}
				break;
			default :
				needsRemoval = true;
				
		}
    }

    /**
     * Splits <code>this</code> into seveal new <code>Missile</code>s.
     * @author Andy Kooiman
     * @since Classic
     */
    private synchronized void pop()
    {
        for ( double ang = 0; ang < 2 * Math.PI; ang += 2 * Math.PI / manager.popQuantity() )
            manager.add( (int) x, (int) y, ang, 0, 0, myColor );
        needsRemoval = true;
    }

    /**
     * Gets the current x coordinate.
     * @return The current x coordinate.
     * @author Andy Kooiman
     * @since Classic
     */
    public synchronized int getX()
    {
        return (int) x;
    }

    /**
     * Gets the current y coordinate.
     * @return The current y coordinate.
     * @author Andy Kooiman
     * @since Classic
     */
    public synchronized int getY()
    {
        return (int) y;
    }

    /**
     * Gets the current radius.
     * @return The current radius.
     * @author Andy Kooiman
     * @since Classic
     */
    public synchronized int getRadius()
    {
        return radius;
    }

    /**
     * Sets the current stage of explosion
     * @param count The new stage of explosion.
     * @author Andy Kooiman
     * @since Classic
     */
    public synchronized void setExplodeCount( int count )
    {
        explodeCount = count;
    }

    /**
     * Gets the current stage of explosion.
     * @return The current stage of explosion.
     * @author Andy Kooiman
     * @since Classic
     */
    public synchronized int getExplodeCount()
    {
        return explodeCount;
    }

    /**
     * Returns whether <code>this</code> need to be removed.
     * @return Whether <code>this</code> should be removed.
     * @author Andy Kooiman
     * @since Classic
     */
    public boolean needsRemoval()
    {
        return needsRemoval;
    }
}