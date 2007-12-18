/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.awt.Color;
import java.util.LinkedList;
/**
 *
 * @author Owner
 */
interface WeaponManager {
    
    public void act();

    public void add(LinkedList<Weapon> weapons);

    public void clear();

    public void explodeAll();
    
    public int getIntervalShoot();
    
    public boolean add( int x, int y, double angle, double dx, double dy, Color col  );
    
    public int getNumLiving();
    
    public LinkedList<Weapon> getWeapons();

    public void restoreBonusValues();
    
    public String ApplyBonus(int key);

    public int getMaxShots();
}
