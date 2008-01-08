/*
 * Station.java
 */

import java.awt.Color;
import java.awt.Graphics;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author Phillip Cohen
 * @since Jan 6, 2008
 */
public class Station extends GameObject implements ShootingObject
{
    private Ship target;

    private int shootTimer = 0;
    
    private double angle;

    private MissileManager manager;

    int size = 35;

    public Station( double x, double y )
    {
        setLocation( x, y );
        setDx(Math.random()*2-1);
        setDy(Math.random()*2-1);
        angle = 0;
        manager = new MissileManager();
        manager.setPopQuantity(0);
        shootTimer = 0;
    }

    @Override
    boolean wrapWhenOutside()
    {
        return true;
    }

    @Override
    boolean destroyWhenOutside()
    {
        return false;
    }

    public void act()
    {
        this.addToX(getDx());
        this.addToY(getDy());
        int range = 300;
        Ship closestShip = null;

        // Find players within our range.
        for ( Ship s : Game.getInstance().players )
        {
            if ( getProximity( s ) < range )
            {
                if ( closestShip == null || getProximity( s ) > getProximity( closestShip ) )
                    closestShip = s;
            }
        }

        // Aim towards closest ship.
        if ( closestShip != null )
        {
            angle = Math.atan( ( closestShip.getY() - getY() ) / (double) ( closestShip.getX() - getX() ) );
            if(closestShip.getX()-getX()<0)
                angle+=Math.PI;
            // Fire!
            if(canShoot())
            {
                manager.add( (int)(centerX()+25*Math.cos(0-angle)), (int)(centerY()-25*Math.sin(0-angle)), 0 - angle, 0d, 0d, Color.white );
                shootTimer = 10;
            }
        }
        else
            angle += 0.01;

        if(!canShoot())
            shootTimer--;
        
        manager.act();
    }

    private double getProximity( Ship s )
    {
        return Math.sqrt( Math.pow( getX() - s.getX(), 2 ) + Math.pow( getY() - s.getY(), 2 ) );
    }

    public void draw( Graphics g )
    {
        int rX = RelativeGraphics.translateX( getX() );
        int rY = RelativeGraphics.translateY( getY() );

        g.setColor( Color.darkGray );
        g.fillRect( rX, rY, size, size );
        g.setColor( new Color( 20, 20, 20 ) );
        g.drawRect( rX, rY, size, size );

        g.setColor( new Color( 20, 20, 20 ) );
        g.fillRect( rX - 2, rY - 2, 10, 10 );
        g.fillRect( rX + 27, rY, 10, 10 );
        g.fillRect( rX + 27, rY + 27, 10, 10 );
        g.fillRect( rX, rY + 27, 10, 10 );


        //
        g.setColor( Color.white );

        int cX = RelativeGraphics.translateX( centerX() );
        int cY = RelativeGraphics.translateY( centerY() );

        int eX = (int) ( cX + 15 * Math.cos( angle ) );
        int eY = (int) ( cY + 15 * Math.sin( angle ) );
        g.drawLine( cX, cY, eX, eY );
        g.drawLine( cX, cY + 1, eX, eY + 1 );
        g.drawLine( cX + 1, cY, eX + 1, eY );

        manager.draw( g );

    }

    int centerX()
    {
        return (int) ( getX() + size / 2 );
    }

    int centerY()
    {
        return (int) ( getY() + size / 2 );
    }

    public ConcurrentLinkedQueue<WeaponManager> getManagers()
    {
        ConcurrentLinkedQueue<WeaponManager> c = new ConcurrentLinkedQueue<WeaponManager>();
        c.add( manager );
        return c;
    }

    public boolean canShoot()
    {
        return ( shootTimer == 0);
    }
}
