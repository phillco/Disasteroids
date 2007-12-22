
import java.awt.Color;
import java.awt.Graphics;
import java.util.LinkedList;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.ListIterator;

/**
 *
 * @author Owner
 */
class BulletManager implements WeaponManager {

    private LinkedList<Weapon> theBullets;
    
    private LinkedList<Weapon> toBeAdded;
    
    private int speed=20;
    
    private int maxShots=100;
    private boolean threeWayShot=false;
    private int intervalShoot=4;
    private int radius=2;
    private int damage=10;
    
    public BulletManager() {
        theBullets=new LinkedList<Weapon>();
        toBeAdded=new LinkedList<Weapon>();
    }
    
    public BulletManager(LinkedList<Weapon> start)
    {
        theBullets=start;
        toBeAdded=new LinkedList<Weapon>();
    }

    public void act() {
        ListIterator<Weapon> iter = theBullets.listIterator();
        while ( iter.hasNext() )
        {
            Weapon w = iter.next();
            if ( w.needsRemoval() )
                iter.remove();
            else
                w.act();
        }

        while ( !toBeAdded.isEmpty() )
        {
            theBullets.add( toBeAdded.remove() );
        } 
    }

    public void clear() {
        theBullets=new LinkedList<Weapon>();
        toBeAdded=new LinkedList<Weapon>();
    }

    public void explodeAll() {
        for(Weapon w: theBullets)
            w.explode();
    }

    public int getIntervalShoot() {
        return intervalShoot;
    }

    public boolean add(int x, int y, double angle, double dx, double dy, Color col) {
        if ( theBullets.size() > 500 )
            return false;
        if(threeWayShot)
        {
            toBeAdded.add(new Bullet(this,x,y,angle+Math.PI/8, dx, dy, col));
            toBeAdded.add(new Bullet(this,x,y,angle-Math.PI/8, dx, dy, col));
        }
        
        return toBeAdded.add( new Bullet( this, x, y, angle, dx, dy, col ) );
    }
    
     /**
     * Adds all elements of a <code>LinkedList</code> to this <code>MissileManager</code>.
     * These elements need not be <code>Missile</code>s, and will be removed from their
     * current location by this method.
     * 
     * @param others The <code>LinkedList</code> of <code>Weapon</code>s to be added
     * @since December 17, 2007
     */
    public void add(LinkedList<Weapon> others)
    {
        ListIterator<Weapon> itr= others.listIterator();
        while(itr.hasNext())
        {
            toBeAdded.add(itr.next());
            itr.remove();
        }
    }

    public int getNumLiving() {
        return theBullets.size();
    }

    public LinkedList<Weapon> getWeapons() {
        return theBullets;
    }

    public void restoreBonusValues() {
      threeWayShot=false;
      intervalShoot=4;
      radius=2;
      damage=10;
    }
    
    public int getDamage()
    {
        return damage;
    }

    public String ApplyBonus(int key) {
        String ret="";
        
        switch(key)
        {
            case 0: 
                damage+=50;
                ret+="Depleted Uranium Bullets!!";
                break;
            case 1:
                intervalShoot=1;
                ret += "Rapid Fire";
                break;
            case 4:
                threeWayShot=true;
                ret += "Three Way Shot";
                break;
            case 7:
                radius=6;
                ret += "Huge Bullets";
                break;
            default:
                ret="";
        }
        return ret;
    }
    
    public int getSpeed()
    {
        return speed;
    }

    public int getMaxShots() {
        return maxShots;
    }

    public int getRadius() {
        return radius;
    }

    public void draw( Graphics g )
    {
        ListIterator<Weapon> iter = theBullets.listIterator();
        while ( iter.hasNext() )
        {
            Weapon w = iter.next();
            w.draw(g);
        }

    }

}
