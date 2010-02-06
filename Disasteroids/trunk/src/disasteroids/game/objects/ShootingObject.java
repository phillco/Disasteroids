/*
 * DISASTEROIDS
 * ShootingObject.java
 */
package disasteroids.game.objects;

import java.awt.Graphics;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import disasteroids.game.GameElement;
import disasteroids.game.weapons.Weapon;

/**
 * A small extension of GameObject for objects that shoot things (have weapons).
 * @author Phillip Cohen
 */
public abstract class ShootingObject extends GameObject implements GameElement
{
	protected Weapon[] weapons;

	protected int activeWeapon = 0;

	public ShootingObject()
	{
		super();
	}

	public ShootingObject( double x, double y, double dx, double dy, int numWeapons )
	{
		super( x, y, dx, dy );
		weapons = new Weapon[numWeapons];
	}

	public void act()
	{
		for ( Weapon wm : weapons )
			wm.act();
		getActiveWeapon().reload();
	}

	public void draw( Graphics g )
	{
		for ( Weapon wm : weapons )
			wm.draw( g );
	}

	/**
	 * Returns the index of the given weapon in our array, or -1 if we don't have it.
	 */
	public int getIndexOfWeapon( Weapon w )
	{
		for ( int i = 0; i < weapons.length; i++ )
		{
			if ( w == weapons[i] )
				return i;
		}
		return -1;
	}

	public Weapon getWeapon( int index )
	{
		return weapons[index];
	}

	/**
	 * Returns an array all of our weapons.
	 */
	public Weapon[] getWeapons()
	{
		return weapons;
	}

	/**
	 * Returns the weapon currently in use.
	 */
	public Weapon getActiveWeapon()
	{
		return weapons[activeWeapon];
	}

	/**
	 * Writes <code>this</code> to a stream for client/server transmission.
	 * NOTE: Flatten/restore the individual weapons in the subclass methods.
	 */
	@Override
	public void flatten( DataOutputStream stream ) throws IOException
	{
		super.flatten( stream );
	}

	/**
	 * Creates <code>this</code> from a stream for client/server transmission.
	 * NOTE: Flatten/restore the individual weapons in the subclass methods.
	 */
	public ShootingObject( DataInputStream stream, int numWeapons ) throws IOException
	{
		super( stream );
		weapons = new Weapon[numWeapons];
	}
}
