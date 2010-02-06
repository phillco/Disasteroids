/*
 * DISASTEROIDS
 * Bullet.java
 */
package disasteroids.game.weapons;

import java.awt.Color;
import java.awt.Graphics;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import disasteroids.gui.MainWindow;

/**
 * A fast and simple bullet that dies on impact.
 * @author Andy Kooiman
 */
class Bullet extends Unit
{
	private BulletManager parent;

	public Bullet( BulletManager parent, Color color, double x, double y, double dx, double dy, double angle )
	{
		super( color, x, y, dx + parent.getSpeed() * Math.cos( angle ), dy - parent.getSpeed() * Math.sin( angle ) );
		this.parent = parent;
	}

	@Override
	public void act()
	{
		super.act();
		if ( age > 50 )
			parent.remove( this );
	}

	public void draw( Graphics g )
	{
		MainWindow.frame().fillCircle( g, color, (int) getX(), (int) getY(), parent.getBonusValue( parent.BONUS_RADIUS ).getValue() );
	}

	/**
	 * Removes the bullet immediately.
	 */
	@Override
	public void explode()
	{
		parent.remove( this );
	}

	@Override
	public double getRadius()
	{
		return parent.getBonusValue( parent.BONUS_RADIUS ).getValue() + 1;
	}

	@Override
	public int getDamage()
	{
		return parent.getBonusValue( parent.BONUS_DAMAGE ).getValue();
	}

	// \\
	// ------------------------------ NETWORKING -------------------------------- \\
	// \\
	/**
	 * Writes <code>this</code> to a stream for client/server transmission.
	 */
	@Override
	public void flatten( DataOutputStream stream ) throws IOException
	{
		super.flatten( stream );
	}

	/**
	 * Reads <code>this</code> from a stream for client/server transmission.
	 */
	public Bullet( DataInputStream stream, BulletManager parent ) throws IOException
	{
		super( stream );

		this.parent = parent;
	}

	@Override
	public void remove()
	{
		parent.remove( this );
	}
}
