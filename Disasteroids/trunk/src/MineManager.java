/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.awt.Color;
import java.awt.Graphics;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 *
 * @author Owner
 */
public class MineManager implements WeaponManager{
    
    private LinkedList<Weapon> mines;
    
    private LinkedList<Weapon> toBeAdded;
    
    private int maxShots=20;

    public MineManager()
    {
        mines=new LinkedList<Weapon>();
        toBeAdded=new LinkedList<Weapon>();
    }
    

    public void act() {
        ListIterator<Weapon> itr=mines.listIterator();
        while(!toBeAdded.isEmpty())
            itr.add(toBeAdded.remove());
        while(itr.hasNext())
        {
            Weapon w=itr.next();
            if(w.needsRemoval())
                itr.remove();
            else
                w.act();

        }
    }

    public void add(LinkedList<Weapon> weapons) {
        while(!weapons.isEmpty())
            toBeAdded.add(weapons.remove());
    }

    public void clear() {
        toBeAdded=new LinkedList<Weapon>();
        mines=new LinkedList<Weapon>();
    }

    public void explodeAll() {
        for(Weapon w: mines)
            w.explode();
    }

    public int getIntervalShoot() {
        return 10;
    }

    public boolean add(int x, int y, double angle, double dx, double dy, Color col) {
        return toBeAdded.add(new Mine(x,y,col));
    }

    public int getNumLiving() {
        return mines.size();
    }

    public LinkedList<Weapon> getWeapons() {
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
        ListIterator<Weapon> itr= mines.listIterator();
        while(itr.hasNext())
            itr.next().draw(g);
    }

}
