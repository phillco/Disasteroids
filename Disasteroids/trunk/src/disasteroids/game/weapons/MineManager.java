/**
 * DISASTEROIDS
 * MineManager.java
 */
package disasteroids.game.weapons;

import java.awt.Color;
import java.awt.Graphics;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import disasteroids.game.objects.GameObject;
import disasteroids.game.objects.ShootingObject;
import disasteroids.sound.Sound;
import disasteroids.sound.SoundLibrary;

/**
 * A bonus weapon that lays dangerous <code>Mine</code>s.
 * @author Andy Kooiman
 */
public class MineManager extends Weapon
{
	private double berserkAngleOffset = 0;

	/**
	 * The radius in which a mine will latch onto a target.
	 */
	private int sight = 200;

	private Set<GameObject> targetedObjects = new HashSet();

	// Bonus IDs.
	public int BONUS_EXPLODERADIUS, BONUS_TRACKING;

	public MineManager( ShootingObject parent )
	{
		super( parent );
	}

	@Override
	protected void genericInit()
	{
		super.genericInit();
		BONUS_EXPLODERADIUS = getNewBonusID();
		BONUS_TRACKING = getNewBonusID();
		bonusValues.put( BONUS_EXPLODERADIUS, new BonusValue( 85, 150, "Bigger blast area" ) );
		bonusValues.put( BONUS_TRACKING, new BonusValue( 0, 1, "Homing mines!" ) );
	}

	@Override
	public String getName()
	{
		return "Mine Layer";
	}

	@Override
	public int getEntryAmmo()
	{
		return 30;
	}

	@Override
	public void shoot( Color color, double angle )
	{
		if ( !canShoot() )
			return;

		addUnit( new Mine( this, color, parent.getFiringOriginX(), parent.getFiringOriginY(), parent.getDx(), parent.getDy() ) );
		if ( !isInfiniteAmmo() )
			--ammo;

		timeTillNextShot = 20;
		Sound.playInternal( SoundLibrary.MINE_ARM );
	}

	@Override
	public void berserk( Color color )
	{
		berserkAngleOffset += .5;
		int firedShots = 0;
		for ( double angle = 0; angle < Math.PI * 2; angle += Math.PI / 4 )
		{
			if ( !canBerserk() )
				break;

			addUnit( new Mine( this, color, parent.getX() + Math.cos( berserkAngleOffset + angle ) * 50, parent.getY() + Math.sin( berserkAngleOffset + angle ) * 50, parent.getDx(), parent.getDy() ) );

			if ( !isInfiniteAmmo() )
				--ammo;

			++firedShots;
		}

		if ( firedShots > 0 )
		{
			timeTillNextBerserk = firedShots * 30;
			Sound.playInternal( SoundLibrary.MINE_ARM );
		}
	}

	@Override
	public int getMaxUnits()
	{
		return 200;
	}

	@Override
	public void drawOrphanUnit( Graphics g, double x, double y, Color col )
	{
		Mine m = new Mine( this, col, x, y, 0, 0 );
		m.age = 300;
		m.draw( g );
	}

	public int sight()
	{
		return sight;
	}

	/**
	 * Called by mines to reserve a target for homing in on.
	 */
	public boolean reserveTarget( GameObject go )
	{
		if ( isTargetAvailible( go ) )
		{
			targetedObjects.add( go );
			return true;
		}
		else
			return false;
	}

	public boolean isTargetAvailible( GameObject go )
	{
		return !targetedObjects.contains( go );
	}

	public void releaseTarget( GameObject go )
	{
		targetedObjects.remove( go );
	}

	@Override
	public void remove( Unit u )
	{
		if ( ( (Mine) u ).getTarget() != null )
			releaseTarget( ( (Mine) u ).getTarget() );
		super.remove( u );
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
		stream.writeDouble( berserkAngleOffset );
		stream.writeInt( sight );

		// Flatten all of the units.
		stream.writeInt( units.size() );
		for ( Unit u : units )
			( (Mine) u ).flatten( stream );
	}

	/**
	 * Reads <code>this</code> from a stream for client/server transmission.
	 */
	public MineManager( DataInputStream stream, ShootingObject parent ) throws IOException
	{
		super( stream, parent );
		berserkAngleOffset = stream.readDouble();
		sight = stream.readInt();

		// Restore all of the units.
		int size = stream.readInt();
		for ( int i = 0; i < size; i++ )
			addUnit( new Mine( stream, this ) );
	}
}
