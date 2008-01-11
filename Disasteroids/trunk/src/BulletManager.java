
import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author Owner
 */
class BulletManager implements WeaponManager
{

    private ConcurrentLinkedQueue<Weapon> theBullets;

    private int speed = 20;

    private int maxShots = 100;

    private boolean threeWayShot = false;

    private int intervalShoot = 4;

    private int radius = 2;

    private int damage = 10;
    
    private int timeTillNextShot=0;
    
    private int timeTillNextBerserk=0;

    public BulletManager()
    {
        theBullets = new ConcurrentLinkedQueue<Weapon>();
    }

    public BulletManager( ConcurrentLinkedQueue<Weapon> start )
    {
        theBullets = start;
    }
    
    public void act(boolean active)
    {
        act();
        if(active)
        {
            timeTillNextShot--;
            timeTillNextBerserk--;
        }
    }

    public void act()
    {
        Iterator<Weapon> iter = theBullets.iterator();
        while ( iter.hasNext() )
        {
            Weapon w = iter.next();
            if ( w.needsRemoval() )
                iter.remove();
            else
                w.act();
        }
    }

    public void clear()
    {
        theBullets = new ConcurrentLinkedQueue<Weapon>();
    }

    public void explodeAll()
    {
        for ( Weapon w : theBullets )
            w.explode();
    }

    public int getIntervalShoot()
    {
        return intervalShoot;
    }

    public boolean add( int x, int y, double angle, double dx, double dy, Color col )
    {
        if ( theBullets.size() > 500 || timeTillNextShot > 0)
            return false;
        if ( threeWayShot )
        {
            theBullets.add( new Bullet( this, x, y, angle + Math.PI / 8, dx, dy, col ) );
            theBullets.add( new Bullet( this, x, y, angle - Math.PI / 8, dx, dy, col ) );
        }
        timeTillNextShot=intervalShoot;

        return theBullets.add( new Bullet( this, x, y, angle, dx, dy, col ) );
    }

    /**
     * Adds all elements of a <code>ConcurrentLinkedQueue</code> to this <code>MissileManager</code>.
     * These elements need not be <code>Missile</code>s, and will be removed from their
     * current location by this method.
     * 
     * @param others The <code>ConcurrentLinkedQueue</code> of <code>Weapon</code>s to be added
     * @since December 17, 2007
     */
    public void add( ConcurrentLinkedQueue<Weapon> others )
    {
        while ( !others.isEmpty() )
            theBullets.add( others.remove() );
    }

    public int getNumLiving()
    {
        return theBullets.size();
    }

    public ConcurrentLinkedQueue<Weapon> getWeapons()
    {
        return theBullets;
    }

    public void restoreBonusValues()
    {
        threeWayShot = false;
        intervalShoot = 4;
        radius = 2;
        damage = 10;
    }

    public int getDamage()
    {
        return damage;
    }

    public String ApplyBonus( int key )
    {
        String ret = "";

        switch ( key )
        {
            case 0:
                damage += 50;
                ret += "Depleted Uranium Bullets!!";
                break;
            case 1:
                intervalShoot = 1;
                ret += "Rapid Fire";
                break;
            case 4:
                threeWayShot = true;
                ret += "Three Way Shot";
                break;
            case 7:
                radius = 6;
                ret += "Huge Bullets";
                break;
            default:
                ret = "";
        }
        return ret;
    }

    public int getSpeed()
    {
        return speed;
    }

    public int getMaxShots()
    {
        return maxShots;
    }

    public int getRadius()
    {
        return radius;
    }

    public void draw( Graphics g )
    {
        for ( Weapon w : theBullets )
            w.draw( g );

    }

    public String getWeaponName()
    {
        return "Bullets";
    }

    public Weapon getWeapon(int x, int y, Color col) {
        return  new Bullet(this, x, y, 0, 0, 0, col);
    }

    public void berserk(Ship s) {
        if(timeTillNextBerserk>0)
            return;
        int temp=timeTillNextShot;
        Sound.kablooie();
        timeTillNextShot=0;
        for(double angle=0; angle<2*Math.PI; angle+=Math.PI/50)
        {
            add(s.getX(), s.getY(), angle, s.getDx(), s.getDy(), s.getColor());
            timeTillNextShot=0;
        }
        timeTillNextShot=temp;
        timeTillNextBerserk=50;
    }

    public boolean canShoot() {
        return ! ( theBullets.size() > 500 || timeTillNextShot > 0 );
    }
}
