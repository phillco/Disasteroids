/**
 * DISASTEROIDS
 * Weapon.java
 */
package disasteroids;

/**
 * A weapon manager's individual bullets.
 * @author Andy Kooiman
 */
public abstract class Weapon extends GameObject
{
    public abstract int getRadius();
    
    public abstract void explode();
    
    public abstract boolean needsRemoval();   
    
    public abstract int getDamage();
}
