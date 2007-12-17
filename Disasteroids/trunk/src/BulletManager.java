
import java.awt.Color;
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
    }

    public String ApplyBonus(int key) {
        switch(key)
        {
            case 7:
                intervalShoot=1;
                return "Rapid Fire";
            case 4:
                threeWayShot=true;
                return "Three Way Shot";
            case 1:
                radius=6;
                return "Huge Bullets";
        }
        return "";
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

}
