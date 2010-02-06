package disasteroids.game.weapons;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

import disasteroids.Util;
import disasteroids.game.Game;
import disasteroids.game.ObjectManager;
import disasteroids.game.objects.Alien;
import disasteroids.game.objects.Asteroid;
import disasteroids.game.objects.GameObject;
import disasteroids.game.objects.Ship;
import disasteroids.game.objects.Station;
import disasteroids.gui.MainWindow;
import disasteroids.gui.Particle;
import disasteroids.gui.ParticleManager;

/**
 * A projectile that looks just like a normal Missile, but it tracks down a target
 * @author Matt Weir
 * @since May 30, 2009
 */
public class GuidedMissile extends Unit
{
	private GameObject objectToFollow;
	private GuidedMissileManager parent;

	private boolean foundObject = false;

	private double angle;
	private int explosionStage = 0;

	private int radius = 3;

	private final int searchRadius = 1000;

	public GuidedMissile( GuidedMissileManager parent, Color color, double x, double y, double dx, double dy, double angle )
	{
		super( color, x, y, dx, dy );

		this.parent = parent;
		this.angle = angle;
	}

	@Override
	public void move()
	{
		super.move();

		if ( age > 20 )
		{
			if ( objectToFollow == null && !foundObject )
			{
				findObjectToFollow();

			}

			// if (objectToFollow != null)
			// angle = Util.getAngle(this, objectToFollow);
		}

		// setDx( ( getDx() + parent.getSpeed() * Math.cos( angle ) / 50 ) * .98 );
		// setDy( ( getDy() - parent.getSpeed() * Math.sin( angle ) / 50 ) * .98 );
		track();
	}

	public void findObjectToFollow()
	{
		ObjectManager objManager = Game.getInstance().getObjectManager();
		objectToFollow = objManager.getObject( 0 );

		GameObject target = null;

		for ( long l : objManager.getAllIds() )
		{
			GameObject obj = objManager.getObject( l );
			if ( !isValidTarget( obj ) )
				continue;
			if ( target == null )// we haven't found anything, yet.
			{
				target = obj;
				continue;
			}
			// We have two possible targets here, choose the best one.
			target = getBetterTarget( target, obj );
		}

		if ( target != null )
		{
			objectToFollow = target;
			foundObject = true;
		}
		// 0 asteroid, 1 station, 2 alien, 3 ship
		/*int highestPriority = -1;

		for (long l : objManager.getAllIds())
		{
		    if (Util.getDistance(parent.getParent(), objManager.getObject(l)) > searchRadius)
		        continue;

		    if (objManager.getObject(l) instanceof Asteroid && highestPriority < 0)
		        highestPriority = 0;
		    if (objManager.getObject(l) instanceof Station && highestPriority < 1)
		        highestPriority = 1;
		    if (objManager.getObject(l) instanceof Alien && highestPriority < 2)
		        highestPriority = 2;
		    if (objManager.getObject(l) instanceof Ship && highestPriority < 3 && objManager.getObject(l) != parent.getParent())
		        highestPriority = 3;
		}

		double smallestDistance = 100000;
		GameObject closestObject = null;

		int time = 30;
		switch (highestPriority)
		{
		    case 0:
		        for (Asteroid a : objManager.getAsteroids())
		        {
		            double distance = Util.getDistance(a, projectX(time), projectY(time));

		            if (distance < smallestDistance)
		            {
		                smallestDistance = distance;
		                closestObject = a;
		            }
		        }
		        break;
		    case 1:
		        for (Asteroid a : objManager.getAsteroids())
		        {
		            double distance = Util.getDistance( a, projectX(time), projectY(time));

		            if (distance < smallestDistance)
		            {
		                smallestDistance = distance;
		                closestObject = a;
		            }
		        }
		        break;
		    case 2:
		        for (GameObject a : objManager.getBaddies())
		        {
		            double distance = Util.getDistance(a, projectX(time), projectY(time));

		            if (distance < smallestDistance)
		            {
		                smallestDistance = distance;
		                closestObject = a;
		            }
		        }
		        break;
		    case 3:
		        for (Ship a : objManager.getPlayers())
		        {
		            double distance = Util.getDistance(a, projectX(time), projectY(time));

		            if (distance < smallestDistance)
		            {
		                smallestDistance = distance;
		                closestObject = a;
		            }
		        }
		        break;
		}

		objectToFollow = closestObject;*/
	}

	@Override
	public void remove()
	{
		parent.remove( this );
	}

	@Override
	public double getRadius()
	{
		return 3;
	}

	@Override
	public void explode()
	{
		// Already exploding.
		if ( isExploding() )
			return;

		explosionStage = 1;
	}

	public boolean isExploding()
	{
		return explosionStage > 0;
	}

	@Override
	public int getDamage()
	{
		// for now just the same as a normal missile
		if ( isExploding() )
			return 20;
		else
			return 40;
	}

	public void draw( Graphics g )
	{
		// Draw the body.
		MainWindow.frame().drawLine( g, color, (int) getX(), (int) getY(), 10, angle + Math.PI );
		MainWindow.frame().fillCircle( g, color, (int) getX(), (int) getY(), radius );

		// if( objectToFollow!=null)
		// MainWindow.frame().drawLine(g, Color.WHITE, (int)getX(),(int)getY() ,(int)objectToFollow.getX(),
		// (int)objectToFollow.getY());
		// MainWindow.frame().drawLine(g, Color.GREEN, (int)getX(), (int)getY(), (int)projectX(40), (int)projectY(40));
		// MainWindow.frame().drawCircle(g, Color.green,(int) projectX(40), (int)projectY(40), 200);
		// Draw the explosion.
		Color col = color;
		switch ( explosionStage )
		{
			case 1:
			case 2:
			case 3:
			case 4:
				if ( explosionStage % 2 != 0 )
					col = Color.yellow;
				MainWindow.frame().fillCircle( g, col, (int) getX(), (int) getY(), radius );
				break;
			case 5:
			case 6:
			case 7:
			case 8:
				if ( explosionStage % 2 != 0 )
					col = Color.yellow;
				radius = 5;
				MainWindow.frame().fillCircle( g, col, (int) getX(), (int) getY(), radius );
				break;
			case 9:
			case 10:
			case 11:
				radius = 14;
				col = Color.yellow;
				MainWindow.frame().fillCircle( g, col, (int) getX(), (int) getY(), radius );
				this.explosionStage++;
				break;
		}
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

		// Create particles when launched.
		if ( age < 30 )
		{
			Random rand = Util.getGameplayRandomGenerator();
			for ( int i = 0; i < (int) ( 7 - Math.sqrt( getDx() * getDx() + getDy() * getDy() ) ); i++ )
				ParticleManager.addParticle( new Particle( getX() + rand.nextInt( 8 ) - 4, getY() + rand.nextInt( 8 ) - 4, rand.nextInt( 4 ), color, rand.nextDouble() * 3, angle + rand.nextDouble() * .4 - .2 + Math.PI, 30, 10 ) );
		}
		// Explode when old.
		if ( age > parent.life() && explosionStage == 0 )
			explode();

		// Move through the explosion sequence.
		if ( explosionStage > 0 )
		{
			this.explosionStage++;
			switch ( explosionStage )
			{
				case 0:
					return;
				case 1:
				case 2:
				case 3:
				case 4:
					setDx( getDx() * .8 );
					setDy( getDy() * .8 );
					break;
				case 5:
				case 6:
				case 7:
				case 8:
					setDx( getDx() * .8 );
					setDy( getDy() * .8 );
					radius = 3;
					break;
				case 9:
				case 10:
				case 11:
					setDx( getDx() * .8 );
					setDy( getDy() * .8 );
					break;
				default:
					parent.remove( this );
			}
		}
	}

	/**
	 * Determines and returns whichever parameter is the better target to chase,
	 * based on object types and locations
	 * @param one The first object to consider
	 * @param two The second object to consider
	 * @return The better of the two targets
	 * @pre both one and two are valid targets and are both non-null
	 */
	private GameObject getBetterTarget( GameObject one, GameObject two )
	{
		int time = 40;// how long in the future to look;
		double distanceOne = Util.getDistance( one, projectX( time ), projectY( time ) );
		double distanceTwo = Util.getDistance( two, projectX( time ), projectY( time ) );

		if ( ( distanceOne > 400 ) ^ ( distanceTwo > 400 ) )// one or the other is far away. Finally got to use an XOR :)
		{
			if ( distanceOne < 400 )// one is far away
				return two;
			return one;// two is far away
		}

		// either both are far away or both are close, see if one is a better target in general
		int priorityOne = priority( one ), priorityTwo = priority( two );
		if ( priorityOne != priorityTwo )
		{
			if ( priorityOne > priorityTwo )
				return one;
			return two;
		}

		// either both are close or both are far away; both are the same type of object
		if ( distanceOne < distanceTwo )
			return one;// get the closer one
		return two;
	}

	/**
	 * Makes sure the given object is a vaild target. Only shoots at Asteroids,
	 * Aliens, Stations, and other Ships
	 * @param obj The target in question
	 * @return Whether the target is vaild or not
	 */
	private boolean isValidTarget( GameObject obj )
	{
		if ( obj == null )
			return false;// don't chase nothing
		if ( obj == parent.getParent() )// don't shoot yourself
			return false;
		if ( Util.getDistance( this, obj ) > searchRadius )
			return false;// too far away

		return ( obj instanceof Asteroid ) || ( obj instanceof Alien ) || ( obj instanceof Station ) || ( obj instanceof Ship );

		// return true;//no objections
	}

	private int priority( GameObject obj )
	{
		if ( obj instanceof Asteroid )
			return 1;
		if ( obj instanceof Alien )
			return 2;
		if ( obj instanceof Station )
			return 3;
		if ( obj instanceof Ship )
			return 4;
		return 0;
	}

	private void track()
	{
		// basic acceleration of a missile
		setDx( ( getDx() + parent.getSpeed() * Math.cos( angle ) / 50 ) * .98 );
		setDy( ( getDy() - parent.getSpeed() * Math.sin( angle ) / 50 ) * .98 );
		if ( objectToFollow == null )
			return;// nothing to chase

		double angleToTarget = Util.getAngle( this, objectToFollow );
		double turnRate = Math.PI / 64;
		if ( Math.abs( ( angle - angleToTarget + 4 * Math.PI ) % ( 2 * Math.PI ) ) < turnRate )// we're close enough, just
																								// point the right direction
		{
			turn( angleToTarget - angle );
			angle = angleToTarget;
		}
		if ( ( angleToTarget - angle + 4 * Math.PI ) % ( 2 * Math.PI ) < Math.PI )
		{
			turn( turnRate );
			angle += turnRate;
		}
		else
		{
			turn( -turnRate );
			angle -= turnRate;
		}
	}
}
