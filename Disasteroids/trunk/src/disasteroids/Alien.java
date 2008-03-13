/*gh
 * Alien.java
 * 
 * Phillip Cohen.
 * Started on Feb 6, 2008.
 */
package disasteroids;

import disasteroids.gui.AsteroidsFrame;
import disasteroids.gui.ParticleManager;
import disasteroids.gui.RelativeGraphics;
import disasteroids.sound.Sound;
import disasteroids.sound.SoundLibrary;
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

    private Color color;

    private int size;

    private int life;

    private int explosionTime;

    public Alien( int x, int y, double dx, double dy )
    {
        setSpeed( dx, dy );
        setLocation( x, y );
        size = RandomGenerator.get().nextInt( 50 ) + 30;
        manager = new AlienMissileManager( size );
        color = new Color( RandomGenerator.get().nextInt( 255 ), RandomGenerator.get().nextInt( 255 ), RandomGenerator.get().nextInt( 255 ) );
        life = 100;
        explosionTime = 0;
    }

    public void act()
    {
        move();
        checkCollision();
        manager.act( true );

        // Prepare to flash.
        if ( life <= 0 )
        {
            if ( explosionTime == 0 )
            {
                explosionTime = 20;
                
                if ( RandomGenerator.get().nextInt( 10 ) == 0 )
                    Game.getInstance().createBonus( this );
            }

            if ( explosionTime == 1 )
            {
                Game.getInstance().removeObject( this );
                Sound.playInternal( SoundLibrary.ALIEN_DIE );
            }

            explosionTime--;
        }

        // Smoke when low on health.
        if ( life < 80 )
            ParticleManager.createSmoke( getX() + RandomGenerator.get().nextInt( size ), centerY(), 1 );

        // Flames when doomed!
        if ( life < 40 )
            ParticleManager.createFlames( getX() + RandomGenerator.get().nextInt( size ), centerY(), ( 50 - life ) / 10 );

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
            manager.add( (int) centerX(), (int) centerY(), 0 - randomizeAngle( angle ), Math.cos( 0 - angle ) * 5, Math.sin( 0 - angle ) * 5, color, false );
        }
    }

    private void checkCollision()
    {
        if ( explosionTime > 0 )
            return;

        // Check for missile collision.
        for ( ShootingObject s : Game.getInstance().shootingObjects )
        {
            if ( s == this )
                continue;

            // Loop through the mangers.
            for ( WeaponManager wm : s.getManagers() )
            {
                // Loop through the bullets.
                for ( WeaponManager.Unit m : wm.getWeapons() )
                {
                    // Were we hit by a bullet?
                    if ( ( m.getX() + m.getRadius() > getX() && m.getX() - m.getRadius() < getX() + size ) &&
                            ( m.getY() + m.getRadius() > getY() && m.getY() - m.getRadius() < getY() + size ) )
                    {
                        m.explode();
                        life -= m.getDamage();
                    }
                }
            }
        }

        // Check for ship collision.  
        for ( Ship s : Game.getInstance().players )
        {
            // Were we hit by the ship's body?
            if ( s.livesLeft() >= 0 )
            {
                if ( ( s.getX() + Ship.RADIUS > getX() && s.getX() - Ship.RADIUS < getX() + size ) &&
                        ( s.getY() + Ship.RADIUS > getY() && s.getY() - Ship.RADIUS < getY() + size ) )
                {
                    if ( s.looseLife( s.getName() + " was abducted." ) )
                        return;
                }
            }
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

        if ( explosionTime > 0 )
        {
            AsteroidsFrame.frame().fillCircle( g, Color.orange, (int) getX(), (int) getY(), (int) ( size * 0.1 * ( explosionTime - 1 ) ) );
            return;
        }

        g.setColor( color );
        g.fillOval( rX, rY, size, (int) ( size * 0.6 ) );
        g.setColor( color.darker() );
        g.drawOval( rX, rY, size, (int) ( size * 0.6 ) );

        Color window = new Color( 40, 40, 45 );
        g.setColor( window );
        g.fillOval( rX + (int) ( size * 0.22 ), (int) ( rY - size / 7.5 ), (int) ( size * 0.6 ), (int) ( size * 0.58 ) );
        g.setColor( window.brighter().brighter() );
        g.drawOval( rX + (int) ( size * 0.22 ), (int) ( rY - size / 7.5 ), (int) ( size * 0.6 ), (int) ( size * 0.58 ) );
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

        private int timeTillNextShot;

        public AlienMissileManager( int size )
        {
            setPopQuantity( 0 );
            setLife( (int) ( size * 1.2 ) );
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
            if ( timeTillNextShot > 0 )
                return false;
            timeTillNextShot = getIntervalShoot();
            return add( new AlienBullet( this, x, y, angle, dx * 10, dy * 10, col ), playShootSound );
        }

        @Override
        public void act()
        {
            super.act();
            timeTillNextShot--;
        }

        @Override
        public int getIntervalShoot()
        {
            return Math.max( life, 3 );
        }

        private class AlienBullet extends Missile
        {
            public AlienBullet( MissileManager m, int x, int y, double angle, double dx, double dy, Color c )
            {
                super( m, x, y, angle, dx, dy, c );
                setRadius( 1 );
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
                setRadius( 5 + ( getAge() - getManager().life() * .2 ) * ( getManager().life() - getAge() ) * 0.05 );
                finRotation += ( 0.002 * Math.PI ) % Math.PI * 2;
            }

            @Override
            public void move()
            {
                super.move();
                setDx( ( getDx() + getDy() * -0.2 ) * 0.7 );
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
