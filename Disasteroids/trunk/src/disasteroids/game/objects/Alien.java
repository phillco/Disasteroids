/*
 * DISASTEROIDS
 * Alien.java
 */
package disasteroids.game.objects;

import java.awt.Color;
import java.awt.Graphics;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import disasteroids.Util;
import disasteroids.game.Game;
import disasteroids.game.weapons.Missile;
import disasteroids.game.weapons.MissileManager;
import disasteroids.game.weapons.Unit;
import disasteroids.game.weapons.Weapon;
import disasteroids.gui.ImageLibrary;
import disasteroids.gui.MainWindow;
import disasteroids.gui.ParticleManager;
import disasteroids.networking.Server;
import disasteroids.networking.ServerCommands;
import disasteroids.sound.Sound;
import disasteroids.sound.SoundLibrary;

/**
 * Little UFOs that fire at players.
 * @author Phillip Cohen
 * @since February 6, 2008
 */
public class Alien extends ShootingObject
{
	/**
	 * The color of this <code>Alien</code>
	 */
	protected Color color;

	/**
	 * The diameter(?!?) of this <code>Alien</code>
	 */
	private int size;

	/**
	 * The health of this <code>Alein</code>. Starts at 100
	 */
	private int life;

	/**
	 * The current status in an explosion
	 */
	private int explosionTime;

	/**
	 * Acceleration vectors.
	 * @since March 30, 2008
	 */
	private double ax = 0, ay = 0;

	/**
	 * The direction this <code>Alien</code> is "pointing."
	 */
	double angle = Util.getGameplayRandomGenerator().nextAngle();

	/**
	 * Creates a new <code>Alien</code>
	 */
	public Alien( double x, double y, double dx, double dy )
	{
		super( x, y, dx, dy, 1 );
		size = Util.getGameplayRandomGenerator().nextInt( 50 ) + 30;
		weapons[0] = new AlienMissileManager( size, this );
		color = new Color( Util.getGameplayRandomGenerator().nextInt( 60 ), Util.getGameplayRandomGenerator().nextInt( 128 ) + 96, Util.getGameplayRandomGenerator().nextInt( 60 ) );
		life = 500;
		explosionTime = 0;
	}

	/**
	 * Iterates through the necessary actions of a single timestep
	 */
	@Override
	public void act()
	{
		super.act();
		generalActBehavior();

		ax *= 0.94;
		ay *= 0.94;

		double oldAx = ax, oldAy = ay;
		if ( Math.abs( ax ) <= 0.01 || Util.getGameplayRandomGenerator().nextInt( 60 ) == 0 )
			ax = Util.getGameplayRandomGenerator().nextDouble() * 0.12 - 0.06;

		if ( Math.abs( ay ) <= 0.01 || Util.getGameplayRandomGenerator().nextInt( 60 ) == 0 )
			ay = Util.getGameplayRandomGenerator().nextDouble() * 0.12 - 0.06;
		if ( Util.getGameplayRandomGenerator().nextInt( 90 ) == 0 && ( Math.abs( ax ) == ax ) == ( Math.abs( getDx() ) == getDx() ) )
		{
			ax *= -1.8;
			ay *= -1.8;
		}

		// Sync this movement change.
		if ( ( ax != oldAx || ay != oldAy ) && Server.is() )
			ServerCommands.updateObjectVelocity( this );

		// Find players within our range.
		int range = 300;
		Ship closestShip = null;
		{
			Ship closestInvincible = null;
			for ( Ship s : Game.getInstance().getObjectManager().getPlayers() )
				if ( Util.getDistance( this, s ) < range )
				{
					if ( closestShip == null || Util.getDistance( this, s ) > Util.getDistance( this, s ) )
						closestShip = s;
					if ( closestInvincible == null || Util.getDistance( this, s ) > Util.getDistance( this, s ) )
						closestInvincible = s;
				}
			if ( closestShip == null && closestInvincible != null )
				closestShip = closestInvincible;
		}

		// Aim towards closest ship.
		if ( closestShip != null )
		{
			double mAngle = calculateAngle( closestShip );
			getActiveWeapon().shoot( color, mAngle );
		}

		angle += size / 560.0 + 0.015;
	}

	/**
	 * Executes generic behavior for the Alien class
	 * (workaround for the subclasses)
	 */
	protected void generalActBehavior()
	{
		move();
		setSpeed( Math.min( 3, getDx() + ax ), Math.min( 3, getDy() + ay ) );
		checkCollision();

		// Prepare to flash.
		if ( life <= 0 )
		{
			if ( explosionTime == 0 )
			{
				explosionTime = 20;

				if ( Util.getGameplayRandomGenerator().nextInt( 5 ) == 0 )
					Game.getInstance().createBonus( this );
			}

			if ( explosionTime == 1 )
			{
				Game.getInstance().getObjectManager().removeObject( this );
				Sound.playInternal( SoundLibrary.ALIEN_DIE );
			}

			explosionTime--;
		}

		// Smoke when low on health.
		if ( life < 80 )
			ParticleManager.createSmoke( getX() + Util.getGameplayRandomGenerator().nextInt( size ), centerY(), 1 );

		// Flames when doomed!
		if ( life < 40 )
			ParticleManager.createFlames( getX() + Util.getGameplayRandomGenerator().nextInt( size ), centerY(), ( 50 - life ) / 10 );

	}

	/**
	 * Checks if any Weapons or players have collided, and updates health,
	 * explosions, and lives as necessary.
	 */
	private void checkCollision()
	{
		if ( explosionTime > 0 )
			return;
		if ( life < 0 )
		{
			explosionTime = 20;
			return;
		}

		// Check for missile collision.
		for ( ShootingObject s : Game.getInstance().getObjectManager().getShootingObjects() )
		{
			if ( s == this )
				continue;

			// Loop through the mangers.
			for ( Weapon wm : s.getWeapons() )
			{
				// Loop through the bullets.
				for ( Unit m : wm.getUnits() )
				{
					// Were we hit by a bullet?
					if ( ( m.getX() + m.getRadius() > getX() && m.getX() - m.getRadius() < getX() + size ) && ( m.getY() + m.getRadius() > getY() && m.getY() - m.getRadius() < getY() + size ) )
					{
						m.explode();
						life -= m.getDamage();
						if ( life < 0 && s instanceof Ship )
							( (Ship) s ).increaseScore( 1000 );
					}
					if ( life < 0 )
						break;
				}
				if ( life < 0 )
					break;
			}
			if ( life < 0 )
				break;
		}

		// Check for ship collision.
		for ( Ship s : Game.getInstance().getObjectManager().getPlayers() )
		{
			// Were we hit by the ship's body?
			if ( s.livesLeft() >= 0 )
			{
				if ( ( s.getX() + s.getRadius() > getX() && s.getX() - s.getRadius() < getX() + size ) && ( s.getY() + s.getRadius() > getY() && s.getY() - s.getRadius() < getY() + size ) )
				{
					if ( s.damage( 5, s.getName() + " was abducted." ) )
						life -= 20;
				}
			}
		}
	}

	/**
	 * Calculates the desired angle at which to shoot to hit the target.
	 * @param target The <code>Ship</code> to aim at
	 * @return The angle to aim at to hit.
	 */
	protected double calculateAngle( Ship target )
	{
		double desiredAngle = 0.0;
		double distance = Util.getDistance( this, target );
		// TODO: Sync
		double time = Math.log( distance ) * ( 5 + Util.getGameplayRandomGenerator().nextInt( 2 ) );
		double projectedX = target.getX() + time * target.getDx();
		double projectedY = target.getY() + time * target.getDy();

		desiredAngle = Math.atan( ( projectedY - centerY() ) / -( projectedX - centerX() ) );
		if ( projectedX - ( centerX() ) < 0 )
			desiredAngle += Math.PI;

		return desiredAngle;
	}

	/**
	 * The x coordinate of the center of <code>this</code>
	 * @return The x coordinate of the center of <code>this</code>
	 */
	double centerX()
	{
		return getX() + 8;
	}

	/**
	 * The y coordinate of the center of <code>this</code>
	 * @return The y coordinate of the center of <code>this</code>
	 */
	double centerY()
	{
		return getY() + 3;
	}

	/**
	 * Draws <code>this</code> in the given context, using an <code>Image</code>
	 * @param g The context in which to draw.
	 */
	@Override
	public void draw( Graphics g )
	{
		super.draw( g );
		if ( explosionTime > 0 )
		{
			MainWindow.frame().fillCircle( g, color.darker().darker(), (int) getX(), (int) getY(), (int) ( size * 0.1 * ( explosionTime - 1 ) ) );
			return;
		}
		MainWindow.frame().drawImage( g, ImageLibrary.getAlien(), (int) getX() + size / 2, (int) getY() + size / 3, angle, ( size * 1.6 ) / ImageLibrary.getAlien().getWidth( null ) );
	}

	@Override
	public void flattenPosition( DataOutputStream stream ) throws IOException
	{
		super.flattenPosition( stream );
		stream.writeDouble( ax );
		stream.writeDouble( ay );
	}

	@Override
	public void restorePosition( DataInputStream stream ) throws IOException
	{
		super.restorePosition( stream );
		ax = stream.readDouble();
		ay = stream.readDouble();
	}

	/**
	 * Writes <code>this</code> to a stream for client/server transmission.
	 * 
	 * @param stream the stream to write to
	 * @throws java.io.IOException
	 * @since April 11, 2008
	 */
	@Override
	public void flatten( DataOutputStream stream ) throws IOException
	{
		super.flatten( stream );
		stream.writeDouble( angle );

		stream.writeInt( color.getRGB() );
		stream.writeInt( explosionTime );
		stream.writeInt( life );
		stream.writeInt( size );
		getActiveWeapon().flatten( stream );
	}

	/**
	 * Creates <code>this</code> from a stream for client/server transmission.
	 * 
	 * @param stream the stream to read from (sent by the server)
	 * @throws java.io.IOException
	 * @since April 11, 2008
	 */
	public Alien( DataInputStream stream ) throws IOException
	{
		super( stream, 1 );
		angle = stream.readDouble();

		color = new Color( stream.readInt() );
		explosionTime = stream.readInt();
		life = stream.readInt();
		size = stream.readInt();
		weapons[0] = new AlienMissileManager( stream, this );
	}

	/**
	 * The alien's version of a <code>MissileManager</code>, which shoots weird units.
	 */
	public class AlienMissileManager extends MissileManager
	{
		/**
		 * keeps track of the angle to draw the lines coming radially out of the unit
		 */
		double finRotation = 0.0;

		/**
		 * Creates a new <code>AlienMissileManager</code>
		 * @param life How long the units last
		 */
		public AlienMissileManager( int life, ShootingObject parent )
		{
			super( parent );
			bonusValues.get( BONUS_POPPINGQUANTITY ).override( 0 );
			setLife( (int) ( life * 1.2 ) );
		}

		@Override
		public void flatten( DataOutputStream stream ) throws IOException
		{
			super.flatten( stream );

			// Also, flatten all of the units.
			stream.writeInt( units.size() );
			for ( Unit u : units )
			{
				stream.writeBoolean( u instanceof AlienBomb );
				( (Missile) u ).flatten( stream );
			}
		}

		private AlienMissileManager( DataInputStream stream, ShootingObject parent ) throws IOException
		{
			super( stream, parent );

			// Restore all of the units.
			int size = stream.readInt();
			for ( int i = 0; i < size; i++ )
			{
				if ( stream.readBoolean() )
					addUnit( new AlienBomb( stream, this ) );
				else
					addUnit( new AlienMissile( stream, this ) );
			}
		}

		@Override
		public void shoot( Color color, double angle )
		{
			if ( !canShoot() )
				return;

			// Shoot a missile, and, randomly, an alien bullet.
			// TODO: Sync
			addUnit( new AlienMissile( this, color, parent.getFiringOriginX(), parent.getFiringOriginY(), parent.getDx() / 8, parent.getDy() / 8, angle ) );
			if ( Util.getGameplayRandomGenerator().nextBoolean() )
				addUnit( new AlienBomb( this, color, parent.getFiringOriginX(), parent.getFiringOriginY(), parent.getDx(), parent.getDy(), angle ) );

			if ( !isInfiniteAmmo() )
				--ammo;

			timeTillNextShot = 12;
		}

		/**
		 * Just a normal missile that does little damage.
		 */
		private class AlienMissile extends Missile
		{
			public AlienMissile( AlienMissileManager parent, Color color, double x, double y, double dx, double dy, double angle )
			{
				super( parent, color, x, y, dx, dy, angle, 0 );
			}

			private AlienMissile( DataInputStream stream, AlienMissileManager parent ) throws IOException
			{
				super( stream, parent );
			}

			@Override
			public int getDamage()
			{
				return 2;
			}
		}

		/**
		 * A circular, spinning unit that does constant damage to whoever touches it.
		 */
		private class AlienBomb extends Missile
		{
			public AlienBomb( AlienMissileManager parent, Color color, double x, double y, double dx, double dy, double angle )
			{
				super( parent, color, x, y, dx, dy, angle, 0 );
				radius = 1;
			}

			private AlienBomb( DataInputStream stream, AlienMissileManager parent ) throws IOException
			{
				super( stream, parent );
			}

			@Override
			public int getDamage()
			{
				return 5;
			}

			@Override
			public void act()
			{
				super.act();
				radius = 5 + ( age - parent.life() * .2 ) * ( parent.life() - age ) * 0.05;
				finRotation += ( 0.004 * Math.PI ) % Math.PI * 2;
			}

			/**
			 * Updates position and speed as per current velocity.
			 */
			@Override
			public void move()
			{
				super.move();
				setDx( ( getDx() + getDy() * -0.2 ) * 0.7 );
				setDy( ( getDy() + getDx() * 0.2 ) * 0.7 );
			}

			@Override
			public void draw( Graphics g )
			{
				MainWindow.frame().drawLine( g, color, (int) getX(), (int) getY(), (int) ( getRadius() * 1.7 ), Math.PI * finRotation );
				MainWindow.frame().drawLine( g, color, (int) getX(), (int) getY(), (int) ( getRadius() * 1.7 ), Math.PI * ( finRotation + 0.6 ) );
				MainWindow.frame().drawLine( g, color, (int) getX(), (int) getY(), (int) ( getRadius() * 1.7 ), Math.PI * ( finRotation + 1.2 ) );
				MainWindow.frame().fillCircle( g, color, (int) getX(), (int) getY(), (int) getRadius() );
			}
		}
	}
}
