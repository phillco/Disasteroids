/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Owner
 */
public interface Weapon {

    public int getX();
    
    public int getY();
    
    public int getRadius();
    
    public void explode();
    
    public boolean needsRemoval();
    
    public void act();
}