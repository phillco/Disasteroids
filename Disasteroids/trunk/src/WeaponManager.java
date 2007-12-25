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
interface WeaponManager extends GameElement{
    
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
    
    /**
     * Returns the name of the <code>Weapon</code>.
     * Examples: "Missiles", "Bullets".
     * 
     * @since December 25, 2007
     * @return  plural name of the <code>Weapon</code>
     */
    public String getWeaponName();
}
