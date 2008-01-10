/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Owner
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
