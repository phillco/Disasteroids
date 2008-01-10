/*
 * DISASTEROIDS
 * ShootingObject.java
 */

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Just a small interface for objects that shoot things (have managers).
 * @author Phillip Cohen
 * @since January 6, 2008
 */
public interface ShootingObject
{
    /**
     * Returns a linked queue of all of our weapon managers.
     * @return  a linked queue of all of our weapon managers
     * @since January 6, 2008
     */
    ConcurrentLinkedQueue<WeaponManager> getManagers();

    /**
     * Returns whether we can shoot.
     * 
     * @return  whether we're ready to fire
     * @since January 6, 2008
     */
    boolean canShoot();
}
