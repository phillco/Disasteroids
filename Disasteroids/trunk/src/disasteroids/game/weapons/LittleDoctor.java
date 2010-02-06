/*
 * DISASTEROIDS
 * LittleDoctor.java
 */
package disasteroids.game.weapons;

import java.awt.Color;
import java.awt.Graphics;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import disasteroids.gui.MainWindow;

/**
 * A more elaborate bullet that sets off chain reactions.
 * @author Andy Kooiman
 * @since Classic
 */
public class LittleDoctor extends Unit
{
	/**
	 * The angle that we're pointing. Not necessarily the angle that we're moving at.
	 * @since Classic
	 */
	protected double angle;

	/**
	 * The current radius of <code>this</code>.
	 * @since Classic
	 */
	protected double radius = 3;

	/**
	 * The missile launcher that fired us.
	 * @since Classic
	 */
	protected LittleDoctorManager parent;

	/**
	 * What "generation" this missile is. Missiles shot from the ship are generation 0, wile those formed
	 * through splitting are higher generations.
	 * @since May 31, 2009
	 */
	protected int generation;

	public LittleDoctor( LittleDoctorManager parent, Color color, double x, double y, double dx, double dy, double angle, int generation )
	{
		super( color, x, y, dx + parent.speed() * Math.cos( angle ), dy - parent.speed() * Math.sin( angle ) );
		this.parent = parent;
		this.angle = angle;
		this.generation = generation;
	}

	/**
	 * Draws the missile and any of its explosions.
	 * 
	 * @param g
	 * @since Classic
	 */
	public void draw( Graphics g )
	{
		// Draw the body.
		// MainWindow.frame().drawLine( g, color, (int) getX(), (int) getY(), 10, angle + Math.PI
		double multiplier = 1 - (double) age / parent.life( generation );
		multiplier = Math.max( 0, multiplier );
		multiplier = Math.min( 1, multiplier );
		Color c = new Color( (int) ( color.getRed() * multiplier ), (int) ( color.getGreen() * multiplier ), (int) ( color.getBlue() * multiplier ) );
		MainWindow.frame().fillCircle( g, c, (int) getX(), (int) getY(), (int) radius );
	}

	/**
	 * Moves, then slows down.
	 * 
	 * @since Classic
	 */
	@Override
	public void move()
	{
		super.move();
	}

	/**
	 * Steps <code>this</code> through one iteration.
	 * 
	 * @author Andy Kooiman
	 * @since Classic
	 */
	@Override
	public void act()
	{
		super.act();

		// Explode when old.
		if ( age > parent.life( generation ) )
		{
			if ( generation == 0 )
				explode();
			else
				remove();
		}

	}

	/**
	 * Starts an elaborate explosion sequence. Also potentially pops the missile into several clones.
	 * 
	 * @since Classic
	 */
	@Override
	public void explode()
	{
		// Already exploding.
		parent.pop( this );
		remove();
	}

	/**
	 * Returns the damage this <code>Unit</code> will do.
	 * 
	 * @return The damage done by this <code>Unit</code>
	 */
	@Override
	public int getDamage()
	{
		return 1;
	}

	/**
	 * Returns the radius of the damage area.
	 */
	@Override
	public double getRadius()
	{
		return radius;
	}

	@Override
	public void remove()
	{
		parent.remove( this );
	}

	/**
	 * Getter for the generation field
	 * @return <code>this</code> Missile's generation.
	 */
	public int getGeneration()
	{
		return generation;
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
		stream.writeDouble( angle );
		stream.writeDouble( radius );
		stream.writeInt( generation );
	}

	/**
	 * Reads <code>this</code> from a stream for client/server transmission.
	 */
	public LittleDoctor( DataInputStream stream, LittleDoctorManager parent ) throws IOException
	{
		super( stream );
		angle = stream.readDouble();
		radius = stream.readDouble();
		generation = stream.readInt();

		this.parent = parent;
	}

}
