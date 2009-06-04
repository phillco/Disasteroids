
package disasteroids.weapons;

import disasteroids.Alien;
import disasteroids.Asteroid;
import disasteroids.Game;
import disasteroids.GameObject;
import disasteroids.ObjectManager;
import disasteroids.Ship;
import disasteroids.Station;
import disasteroids.Util;
import disasteroids.gui.MainWindow;
import disasteroids.gui.Particle;
import disasteroids.gui.ParticleManager;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

/**
 * A projectile that looks just like a normal Missile, but it tracks down a target
 * @author Matt Weir
 * @since May 30, 2009
 */
public class GuidedMissile extends Unit
{
    private GameObject objectToFollow;
    private GuidedMissileManager parent;

    private boolean foundObject = false;

    private double angle;
    private int explosionStage = 0;

    private int radius = 3;

    private final int searchRadius = 1000;

    public GuidedMissile(GuidedMissileManager parent, Color color, double x, double y, double dx, double dy, double angle)
    {
        super(color, x, y, dx, dy);

        this.parent = parent;
        this.angle = angle;
    }

    @Override
    public void move()
    {   
        super.move();

        if (age > 30)
        {
            if (objectToFollow == null && !foundObject)
            {
                findObjectToFollow();
                foundObject = true;
            }
            
            if (objectToFollow != null)
                angle = Util.getAngle(this, objectToFollow);
        }

        setDx( ( getDx() + parent.getSpeed() * Math.cos( angle ) / 50 ) * .98 );
        setDy( ( getDy() - parent.getSpeed() * Math.sin( angle ) / 50 ) * .98 );
    }

    public void findObjectToFollow()
    {
        ObjectManager objManager = Game.getInstance().getObjectManager();
        objectToFollow = objManager.getObject(0);

        //0 asteroid, 1 station, 2 alien, 3 ship
        int highestPriority = -1;

        for (long l : objManager.getAllIds())
        {
            if (Util.getDistance(parent.getParent(), objManager.getObject(l)) > searchRadius)
                continue;

            if (objManager.getObject(l) instanceof Asteroid && highestPriority < 0)
                highestPriority = 0;
            if (objManager.getObject(l) instanceof Station && highestPriority < 1)
                highestPriority = 1;
            if (objManager.getObject(l) instanceof Alien && highestPriority < 2)
                highestPriority = 2;
            if (objManager.getObject(l) instanceof Ship && highestPriority < 3 && objManager.getObject(l) != parent.getParent())
                highestPriority = 3;
        }

        double smallestDistance = 100000;
        GameObject closestObject = null;

        switch (highestPriority)
        {
            case 0:
                for (Asteroid a : objManager.getAsteroids())
                {
                    double distance = Util.getDistance(this, a);

                    if (distance < smallestDistance)
                    {
                        smallestDistance = distance;
                        closestObject = a;
                    }
                }
                break;
            case 1:
                for (Asteroid a : objManager.getAsteroids())
                {
                    double distance = Util.getDistance(this, a);

                    if (distance < smallestDistance)
                    {
                        smallestDistance = distance;
                        closestObject = a;
                    }
                }
                break;
            case 2:
                for (GameObject a : objManager.getBaddies())
                {
                    double distance = Util.getDistance(this, a);

                    if (distance < smallestDistance)
                    {
                        smallestDistance = distance;
                        closestObject = a;
                    }
                }
                break;
            case 3:
                for (Ship a : objManager.getPlayers())
                {
                    double distance = Util.getDistance(this, a);

                    if (distance < smallestDistance)
                    {
                        smallestDistance = distance;
                        closestObject = a;
                    }
                }
                break;
        }

        objectToFollow = closestObject;
    }

    @Override
    public void remove()
    {
        parent.remove(this);
    }

    @Override
    public double getRadius()
    {
        return 3;
    }

    @Override
    public void explode()
    {
        // Already exploding.
        if ( isExploding() )
            return;

        explosionStage = 1;
    }

    public boolean isExploding()
    {
        return explosionStage > 0;
    }

    @Override
    public int getDamage()
    {
        //for now just the same as a normal missile
        if (isExploding())
            return 20;
        else
            return 40;
    }

    public void draw(Graphics g)
    {
        // Draw the body.
        MainWindow.frame().drawLine( g, color, (int) getX(), (int) getY(), 10, angle + Math.PI );
        MainWindow.frame().fillCircle( g, color, (int) getX(), (int) getY(), (int) radius );

        // Draw the explosion.
        Color col = color;
        switch ( explosionStage )
        {
            case 1:
            case 2:
            case 3:
            case 4:
                if ( explosionStage % 2 != 0 )
                    col = Color.yellow;
                MainWindow.frame().fillCircle( g, col, (int) getX(), (int) getY(), (int) radius );
                break;
            case 5:
            case 6:
            case 7:
            case 8:
                if ( explosionStage % 2 != 0 )
                    col = Color.yellow;
                radius = 5;
                MainWindow.frame().fillCircle( g, col, (int) getX(), (int) getY(), (int) radius );
                break;
            case 9:
            case 10:
            case 11:
                radius = 14;
                col = Color.yellow;
                MainWindow.frame().fillCircle( g, col, (int) getX(), (int) getY(), (int) radius );
                this.explosionStage++;
                break;
        }
    }

    /**
     * Steps <code>this</code> through one iteration.
     *
     * @author Andy Kooiman
     * @since Classic
     */
    @Override
    public void act()
    {
        super.act();

        // Create particles when launched.
        if ( age < 30 )
        {
            Random rand = Util.getGameplayRandomGenerator();
            for ( int i = 0; i < (int) ( 7 - Math.sqrt( getDx() * getDx() + getDy() * getDy() ) ); i++ )
                ParticleManager.addParticle( new Particle(
                                             getX() + rand.nextInt( 8 ) - 4,
                                             getY() + rand.nextInt( 8 ) - 4,
                                             rand.nextInt( 4 ),
                                             color,
                                             rand.nextDouble() * 3,
                                             angle + rand.nextDouble() * .4 - .2 + Math.PI,
                                             30, 10 ) );
        }
        // Explode when old.
        if ( age > parent.life() && explosionStage == 0 )
            explode();

        // Move through the explosion sequence.
        if ( explosionStage > 0 )
        {
            this.explosionStage++;
            switch ( explosionStage )
            {
                case 0:
                    return;
                case 1:
                case 2:
                case 3:
                case 4:
                    setDx( getDx() * .8 );
                    setDy( getDy() * .8 );
                    break;
                case 5:
                case 6:
                case 7:
                case 8:
                    setDx( getDx() * .8 );
                    setDy( getDy() * .8 );
                    radius = 3;
                    break;
                case 9:
                case 10:
                case 11:
                    setDx( getDx() * .8 );
                    setDy( getDy() * .8 );
                    break;
                default:
                    parent.remove( this );
            }
        }
    }
}
