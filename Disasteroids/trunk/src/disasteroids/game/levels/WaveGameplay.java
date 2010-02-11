/*
 * DISASTEROIDS
 * WaveGameplay.java
 */
package disasteroids.game.levels;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

import disasteroids.Main;
import disasteroids.Util;
import disasteroids.game.Game;
import disasteroids.game.objects.Alien;
import disasteroids.game.objects.Asteroid;
import disasteroids.game.objects.BlackHole;
import disasteroids.game.objects.Bonus;
import disasteroids.game.objects.BonusAsteroid;
import disasteroids.game.objects.Ship;
import disasteroids.game.objects.Station;
import disasteroids.gui.MainWindow;
import disasteroids.gui.RelativeGraphics;

/**
 * A level where players fend off waves of asteroids.
 * @author Phillip Cohen
 */
public class WaveGameplay implements Level
{
	private int currentWave = 1;

	private boolean warp = false;

	private boolean bossFight = false, bossFightReady = false;

	private int wavePoints;

	public WaveGameplay()
	{
		setUpAsteroidField();
	}

	private void nextWave()
	{
		currentWave++;
		Main.log( "Welcome to wave " + currentWave + "." );
		setUpAsteroidField();
	}

	void setUpAsteroidField()
	{
		wavePoints = getWavePoints( currentWave );
		bossFight = ( currentWave % 5 == 0 );
		bossFightReady = false;
		Random rand = Util.getGameplayRandomGenerator();

		if ( bossFight )
		{
			Main.log( "Boss fight!" );

			for ( int numAsteroids = 0; numAsteroids < 25; numAsteroids++ )
			{
				Game.getInstance().getObjectManager().addObject( new Bonus( rand.nextInt( Game.getInstance().GAME_WIDTH ), rand.nextInt( Game.getInstance().GAME_HEIGHT ) ), false );
			}

		}
		else
		{
			// Create asteroids.
			for ( int numAsteroids = 0; numAsteroids < ( currentWave + 1 ) * 2; numAsteroids++ )
			{
				Game.getInstance().getObjectManager().addObject(
						new Asteroid( rand.nextInt( Game.getInstance().GAME_WIDTH ), rand.nextInt( Game.getInstance().GAME_HEIGHT ), rand.nextDouble() * 6 - 3, rand.nextDouble() * 6 - 3, rand.nextInt( 150 ) + 25,
								rand.nextInt( currentWave * 10 + 10 ) - 9 ), false );

				// Create bonus asteroids.
				if ( rand.nextInt( 10 ) == 1 )
				{
					Game.getInstance().getObjectManager().addObject(
							new BonusAsteroid( rand.nextInt( Game.getInstance().GAME_WIDTH ), rand.nextInt( Game.getInstance().GAME_HEIGHT ), rand.nextDouble() * 6 - 3, rand.nextDouble() * 6 - 3, rand.nextInt( 150 ) + 25, rand
									.nextInt( currentWave * 10 + 10 ) - 9 ), false );
				}
			}
		}

		// All players get invincibility.
		for ( Ship s : Game.getInstance().getObjectManager().getPlayers() )
		{
			s.setInvincibilityCount( 250 );
			s.increaseScore( 2500 );
		}
	}

	public void act()
	{
		if ( warp )
		{
			warp = false;
			Game.getInstance().getObjectManager().clearObstacles();
			nextWave();
		}
		if ( bossFight && !bossFightReady )
		{
			if ( Game.getInstance().getObjectManager().getBonuses().size() == 0 )
			{
				MainWindow.frame().showStartMessage( "Here they come!" );
				bossFightReady = true;
			}
			else
				return;
		}
		// Spawn asteroids directly opposite from player 1.
		// TODO: Sync all of these creations.
		double x = RelativeGraphics.oppositeX() + Util.getGameplayRandomGenerator().nextInt( 100 ) - 50;
		double y = RelativeGraphics.oppositeY() + Util.getGameplayRandomGenerator().nextInt( 100 ) - 50;

		double spawnRate = Math.min( 9, Math.max( 1, ( Game.getInstance().getObjectManager().getBaddies().size() + Game.getInstance().getObjectManager().getAsteroids().size() ) / 20.0 ) );
		if ( bossFight )
		{
			spawnRate /= 4;

			// Spawn an alien.
			if ( wavePoints >= 0 && Util.getGameplayRandomGenerator().nextDouble() * 30 * spawnRate <= 0.3 )
			{
				wavePoints -= 400;
				Game.getInstance().getObjectManager().addObject( new Alien( x, y, Util.getGameplayRandomGenerator().nextDouble() * 8 - 4, Util.getGameplayRandomGenerator().nextDouble() * 8 - 4, Util.getGameplayRandomGenerator().nextInt( 10 ) + 85 ),
						false );
			}
		}
		else
		{

			// Spawn an asteroid.
			if ( !bossFight && wavePoints >= 50 && Util.getGameplayRandomGenerator().nextDouble() * spawnRate <= 0.3 )
			{
				wavePoints -= 50;

				// Make it a bonus asteroid.
				if ( Util.getGameplayRandomGenerator().nextDouble() * 8 * spawnRate <= 0.3 )
					Game.getInstance().getObjectManager().addObject(
							new BonusAsteroid( x, y, Util.getGameplayRandomGenerator().nextInt( 6 ) - 3, Util.getGameplayRandomGenerator().nextInt( 6 ) - 3, Util.getGameplayRandomGenerator().nextInt( 60 ) + 80, 15 ), false );
				else
					Game.getInstance().getObjectManager().addObject(
							new Asteroid( x, y, Util.getGameplayRandomGenerator().nextInt( 6 ) - 3, Util.getGameplayRandomGenerator().nextInt( 6 ) - 3, Util.getGameplayRandomGenerator().nextInt( 70 ) + 30, 15 ), false );
			}

			// Spawn an alien.
			if ( wavePoints >= 100 && Util.getGameplayRandomGenerator().nextDouble() * 30 * spawnRate <= 0.3 )
			{
				wavePoints -= 100;
				Game.getInstance().getObjectManager().addObject( new Alien( x, y, Util.getGameplayRandomGenerator().nextDouble() * 8 - 4, Util.getGameplayRandomGenerator().nextDouble() * 8 - 4 ), false );
			}

			// Spawn a station.
			if ( !bossFight && wavePoints >= 150 && Util.getGameplayRandomGenerator().nextDouble() * 25 * spawnRate <= 0.3 )
			{
				wavePoints -= 150;
				Game.getInstance().getObjectManager().addObject( new Station( x, y, Util.getGameplayRandomGenerator().nextInt( 4 ) - 2, Util.getGameplayRandomGenerator().nextInt( 4 ) - 2 ), false );
			}

			// Create a black hole.
			if ( !bossFight && wavePoints >= 100 && Util.getGameplayRandomGenerator().nextDouble() * 40 * Math.pow( Game.getInstance().getObjectManager().getBlackHoles().size() + 1, 3 ) * spawnRate <= 0.3 )
			{
				wavePoints -= 100;
				Game.getInstance().getObjectManager().addObject( new BlackHole( Util.getGameplayRandomGenerator().nextInt( Game.getInstance().GAME_WIDTH ), Util.getGameplayRandomGenerator().nextInt( Game.getInstance().GAME_HEIGHT ), 30, 50 ), false );
			}
		}

		// Is the wave over? Advance.
		if ( wavePoints <= 0 && Game.getInstance().getObjectManager().getAsteroids().isEmpty() && Game.getInstance().getObjectManager().getBaddies().isEmpty() )
		{
			Main.log( "Wave " + currentWave + " completed!", 300 );
			nextWave();
		}
	}

	public void drawHUD( Graphics g )
	{
		Graphics2D g2d = (Graphics2D) g;
		String text = "";
		int x = MainWindow.frame().getPanel().getWidth() - 10;
		int y = MainWindow.frame().getPanel().getHeight() - 25;

		// Draw the counter.
		g2d.setColor( Color.lightGray );
		g2d.setFont( new Font( "Tahoma", Font.BOLD, 16 ) );
		text = "" + currentWave;
		x -= (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth();
		g2d.drawString( text, x, y );

		// Draw the "wave" string.
		g2d.setFont( new Font( "Tahoma", Font.ITALIC, 12 ) );
		text = "wave";
		x -= (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() + 8;
		g2d.drawString( text, x, y );

		// Draw the progress box.
		x = MainWindow.frame().getPanel().getWidth() - 110;
		y += 10;
		g.setColor( Color.darkGray );
		g.drawRect( x, y, 100, 10 );
		int width = (int) ( 100 * ( 1 - (double) wavePoints / getWavePoints( currentWave ) ) );
		g.setColor( Color.lightGray );
		g.fillRect( x, y, width, 10 );
	}

	public String getName()
	{
		return "Wave onslaught";
	}

	int getWavePoints( int wave )
	{
		return wave * 1200 + 200;
	}

	public void flatten( DataOutputStream stream ) throws IOException
	{
		stream.writeInt( currentWave );
		stream.writeInt( wavePoints );
	}

	public WaveGameplay( DataInputStream stream ) throws IOException
	{
		this.currentWave = stream.readInt();
		this.wavePoints = stream.readInt();
	}

	public void optionsKey()
	{
		warp = true;
	}
}
