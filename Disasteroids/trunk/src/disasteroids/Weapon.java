/**
 * DISASTEROIDS
 * Weapon.java
 */
package disasteroids;

/**
 * Interface for a weapon manger's individual bullets/missiles/mines/etc.
 * @author Andy Kooiman
 */
public interface Weapon extends GameElement 
{
    public double getX();
    
    public double getY();
    
    public int getRadius();
    
    public void explode();
    
    public boolean needsRemoval();   
    
    public int getDamage();
}
