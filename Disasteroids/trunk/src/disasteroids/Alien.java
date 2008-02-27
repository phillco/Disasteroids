/*
 * Alien.java
 * 
 * Phillip Cohen.
 * Started on Feb 6, 2008.
 */
package disasteroids;

import disasteroids.gui.AsteroidsFrame;
import disasteroids.gui.RelativeGraphics;
import java.awt.Color;
import java.awt.Graphics;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author Phillip Cohen
 */
public class Alien extends GameObject implements ShootingObject
{
    /**
     * Our firing manager.
     * @since January 6, 2008
     */
    private MissileManager manager;

    public Alien()
    {
        setSpeed( 2, 1 );
        setLocation( 1050, 900 );
        manager = new AlienMissileManager();
    }

    public void act()
    {
        move();
        manager.act( true );

        // Find players within our range.        
        int range = 300;
        Ship closestShip = null;
        {
            Ship closestInvincible = null;
            for ( Ship s : Game.getInstance().players )
                if ( getProximity( s ) < range )
                {
                    if ( closestShip == null || getProximity( s ) > getProximity( closestShip ) )
                        closestShip = s;
                    if ( closestInvincible == null || getProximity( s ) > getProximity( closestInvincible ) )
                        closestInvincible = s;
                }
            if ( closestShip == null && closestInvincible != null )
                closestShip = closestInvincible;
        }

        // Aim towards closest ship.
        if ( closestShip != null )
        {
            double angle = calculateAngle( closestShip );
            manager.add( (int) centerX(), (int) centerY(), 0 - randomizeAngle( angle ), Math.cos( 0 - angle ) * 5, Math.sin( 0 - angle ) * 5, Color.pink, false );
        }
    }

    private double calculateAngle( Ship target )
    {
        double desiredAngle = 0.0;
        double distance = getProximity( target );
        double time = Math.log( distance ) * ( 5 + RandomGenerator.get().nextInt( 2 ) );
        double projectedX = target.getX() + time * target.getDx();
        double projectedY = target.getY() + time * target.getDy();

        desiredAngle = Math.atan( ( projectedY - centerY() ) / -( projectedX - centerX() ) );
        if ( projectedX - ( centerX() ) < 0 )
            desiredAngle += Math.PI;

        return desiredAngle;
    }

    private double randomizeAngle( double d )
    {
        return d;
    }

    double centerX()
    {
        return getX() + 8;
    }

    double centerY()
    {
        return getY() + 3;
    }

    /**
     * Returns the distance to a given ship using Pythagoras.
     * 
     * @param s     the ship
     * @return      the distance to it
     * @since January 6, 2008
     */
    private double getProximity( Ship s )
    {
        return Math.sqrt( Math.pow( getX() - s.getX(), 2 ) + Math.pow( getY() - s.getY(), 2 ) );
    }

    public void draw( Graphics g )
    {
        int rX = RelativeGraphics.translateX( getX() );
        int rY = RelativeGraphics.translateY( getY() );

        manager.draw( g );

        Color base = new Color( 50, 138, 80 );
        g.setColor( base );
        g.fillOval( rX, rY, 30, 20 );
        g.setColor( base.darker() );
        g.drawOval( rX, rY, 30, 20 );

        Color window = new Color( 40, 40, 45 );
        g.setColor( window );
        g.fillOval( rX + 5, rY - 4, 20, 18 );
        g.setColor( window.brighter().brighter() );
        g.drawOval( rX + 5, rY - 4, 20, 18 );
    }

    /**
     * Returns a linked queue containing our one weapon manager. Used for ShootingObject.
     * 
     * @return  thread-safe queue containing our <code>MissileManager</code>
     * @since January 6, 2008
     */
    public ConcurrentLinkedQueue<WeaponManager> getManagers()
    {
        ConcurrentLinkedQueue<WeaponManager> c = new ConcurrentLinkedQueue<WeaponManager>();
        c.add( manager );
        return c;
    }

    private class AlienMissileManager extends MissileManager
    {
        double finRotation = 0.0;

        public AlienMissileManager()
        {
            setPopQuantity( 0 );
            setLife( 100 );
        }

        /**
         * Creates and prepares to add a <code>Missile</code> with the specified properties.
         * @param x The x coordinate.
         * @param y The y coordinate.
         * @param angle The angle the <code>Missile</code> will be pointing (not necessarily the angle it will be traveling).
         * @param dx The x component of velocity.
         * @param dy The y component of velocity (up is negative).
         * @param col The <code>Color</code> of the <code>Missile</code>..
         * @return True if the <code>Missile</code> was successfully added, false otherwise.
         * @author Andy Kooiman
         * @since Classic
         */
        @Override
        public boolean add( int x, int y, double angle, double dx, double dy, Color col, boolean playShootSound )
        {
            return add( new AlienBullet( this, x, y, angle, dx *10, dy *10, col ), playShootSound );
        }

        @Override
        public int getIntervalShoot()
        {
            return 60;
        }

        private class AlienBullet extends Missile
        {
            public AlienBullet( MissileManager m, int x, int y, double angle, double dx, double dy, Color c )
            {
                super( m, x, y, angle, dx, dy, c );
//                if ( RandomGenerator.get().nextBoolean() && RandomGenerator.get().nextBoolean() )
//                    setSpeed( getDy(), getDx() );
                setRadius( 8 );
            }

            @Override
            public int getDamage()
            {
                return 50;
            }

            @Override
            public void act()
            {
                super.act();
                setRadius( 5 + ( getAge() - getManager().life() * .2 ) * ( getManager().life() - getAge() ) * 0.05);
                finRotation += ( 0.002 * Math.PI ) % Math.PI * 2;
            }

            @Override
            public void move()
            {
                super.move();
                setDx( ( getDx() + getDy() * -0.2 ) * 0.7 ) ;
                setDy( ( getDy() + getDx() * 0.2 ) * 0.7 );
            }

            @Override
            public void draw( Graphics g )
            {
                AsteroidsFrame.frame().drawLine( g, getMyColor(), (int) getX(), (int) getY(), (int) ( getRadius() * 1.7 ), Math.PI * finRotation );
                AsteroidsFrame.frame().drawLine( g, getMyColor(), (int) getX(), (int) getY(), (int) ( getRadius() * 1.7 ), Math.PI * ( finRotation + 0.6 ) );
                AsteroidsFrame.frame().drawLine( g, getMyColor(), (int) getX(), (int) getY(), (int) ( getRadius() * 1.7 ), Math.PI * ( finRotation + 1.2 ) );
                AsteroidsFrame.frame().fillCircle( g, getMyColor(), (int) getX(), (int) getY(), getRadius() );
            }
        }
    }
}
