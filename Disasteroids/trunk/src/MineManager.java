/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author Owner
 */
public class MineManager implements WeaponManager{
    
    private ConcurrentLinkedQueue<Weapon> mines;    
    
    private int maxShots=20;
    
    private double berserkAngleOffset=0;

    public MineManager()
    {
        mines=new ConcurrentLinkedQueue<Weapon>();
    }
    

    public void act() {
        Iterator<Weapon> itr=mines.iterator();
        while(itr.hasNext())
        {
            Weapon w=itr.next();
            if(w.needsRemoval())
                itr.remove();
            else
                w.act();

        }
    }

    public void add(ConcurrentLinkedQueue<Weapon> weapons) {
        while(!weapons.isEmpty())
            mines.add(weapons.remove());
    }

    public void clear() {
        mines=new ConcurrentLinkedQueue<Weapon>();
    }

    public void explodeAll() {
        for(Weapon w: mines)
            w.explode();
    }

    public int getIntervalShoot() {
        return 10;
    }

    public boolean add(int x, int y, double angle, double dx, double dy, Color col) {
        return mines.add(new Mine(x,y,col));
    }

    public int getNumLiving() {
        return mines.size();
    }

    public ConcurrentLinkedQueue<Weapon> getWeapons() {
        return mines;
    }

    public void restoreBonusValues() {
        
    }

    public String ApplyBonus(int key) {
        return "";
    }

    public int getMaxShots() {
        return maxShots;
    }

    public void draw(Graphics g) {
        for( Weapon w : mines)
            w.draw(g);
    }

    public String getWeaponName()
    {
        return "Mines";
    }

    public Weapon getWeapon(int x, int y, Color col) {
        Mine m=new Mine(x,y,col);
        m.setLife(500);
        return m;
    }

    public void berserk(Ship s) {
        Sound.kablooie();
        berserkAngleOffset+=.5;
        for(double angle=0; angle<Math.PI*2; angle+=Math.PI/4)
            add((int)(s.getX()+Math.cos(berserkAngleOffset+angle)*50),(int)(s.getY()+Math.sin(berserkAngleOffset+angle)*50), angle, 0, 0, s.getColor());
    }

}
