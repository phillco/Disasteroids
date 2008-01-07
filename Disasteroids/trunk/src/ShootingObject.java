/*
 * ShootingObject.java
 */

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author Phillip Cohen
 * @since Jan 6, 2008
 */
public interface ShootingObject
{
    ConcurrentLinkedQueue<WeaponManager> getManagers();
    boolean canShoot();
}
