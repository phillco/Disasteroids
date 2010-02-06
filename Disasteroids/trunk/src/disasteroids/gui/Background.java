/*
 * DISASTEROIDS
 * Background.java
 */
package disasteroids.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import disasteroids.Main;
import disasteroids.Settings;
import disasteroids.Util;
import disasteroids.game.Game;

/**
 * The star background.
 * @author Andy Kooiman, Phillip Cohen
 * @since Classic, December 23, 2007
 */
public class Background
{
	/**
	 * Dimensions of the background.
	 * @since December 23, 2007
	 */
	private int width, height;

	/**
	 * The array of <code>Star</code>s that are always drawn.
	 * @since December 16, 2007
	 */
	private Star[] theStars;

	/**
	 * Messages that are to be drawn temporarily on the star background.
	 * @since December 16, 2007
	 */
	private ConcurrentLinkedQueue<Background.BackgroundMessage> starMessages = new ConcurrentLinkedQueue<BackgroundMessage>();

	/**
	 * Creates the background.
	 * 
	 * @param width width of the background
	 * @param height height of the background
	 */
	public Background( int width, int height )
	{
		this.width = width;
		this.height = height;
		init();
	}

	/**
	 * Removes all <code>BackgroundMessage</code>s.
	 * 
	 * @since December 23, 2007
	 */
	public void clearMessages()
	{
		starMessages.clear();
	}

	/**
	 * Creates the <code>background</code> image and <code>Star</code> array.
	 * Uses hardware acceleration, if desired.
	 * 
	 * @since Classic
	 */
	public void init()
	{
		// Create the array of stars.
		Random rand = Util.getGraphicsRandomGenerator();
		this.theStars = new Star[( width * height / ( rand.nextInt( 600 ) + 750 ) )];
		for ( int star = 0; star < theStars.length; star++ )
		{
			Color col = Color.getHSBColor( rand.nextFloat(), rand.nextFloat() / 3f, .1f + .9f * rand.nextFloat() );
			theStars[star] = new Star( rand.nextInt( width ), rand.nextInt( height ), col );
		}
	}

	/**
	 * Renders the <code>background</code>.
	 * Uses hardware acceleration, if desired.
	 * 
	 * @return the rendered background
	 * @since Classic
	 */
	public void render( Graphics g )
	{
		// Fill with black. Fill way past the screen, just to be safe
		g.setColor( Color.black );
		g.fillRect( 0, 0, MainWindow.frame().getWidth(), MainWindow.frame().getHeight() );

		// Draw stars.
		try
		{
			GameCanvas.updateQualityRendering( g, false );
			int count = 0;
			for ( Star star : this.theStars )
			{
				synchronized ( this )
				{
					// Skip every 3rd star in speed rendering, don't draw null stars, and make sure nothing else is null
					if ( ( !Settings.isQualityRendering() && ++count % 3 == 0 ) || star == null || MainWindow.frame() == null || Local.getLocalPlayer() == null )
						continue;
					double x = RelativeGraphics.translateX( star.x ), y = RelativeGraphics.translateY( star.y );
					if ( x > 0 && y > 0 && x < MainWindow.frame().getWidth() && y < MainWindow.frame().getHeight() )
					{
						g.setColor( star.getColor() );
						g.drawRect( (int) x, (int) y, 0, 0 );
					}
				}
			}
			GameCanvas.updateQualityRendering( g, Settings.isQualityRendering() );
		}
		catch ( NullPointerException e )
		{
			Main.warning( "Star Null Pointer :(", e );
		}

		// Draw background messages.
		Iterator<BackgroundMessage> itr = starMessages.iterator();
		while ( itr.hasNext() )
		{
			BackgroundMessage m = itr.next();
			m.draw( g );
			if ( m.life-- <= 0 )
				itr.remove();
		}
	}

	public void act()
	{
		if ( theStars == null )
			return;
		for ( Star star : theStars )
		{
			if ( star == null )
				continue;
			star.x += star.dx - Local.getLocalPlayer().getDx() * star.depth;
			star.y += star.dy - Local.getLocalPlayer().getDy() * star.depth;
			star.checkWrap();
		}
	}

	/**
	 * Writes a message onto the background.
	 * 
	 * @param message the message to be written
	 * @param x the x coordinate where the message should be drawn
	 * @param y the y coordinate where the message should be drawn
	 * @param col the <code>Color</code> in which the message should be drawn
	 * @since Classic
	 */
	public void writeOnBackground( String message, int x, int y, Color col )
	{
		starMessages.add( new BackgroundMessage( x, y, message, col ) );
	}

	public void writeOnBackground( String message, int x, int y, double dy, int life, Color col, Font f )
	{
		starMessages.add( new BackgroundMessage( x, y, message, dy, col, life, f ) );
	}

	/**
	 * The <code>Star</code> class is little more than an overblown
	 * coordinate class and is used for storing the absolute locations of each Star.
	 * 
	 * @author Andy Kooiman
	 * @since December 16, 2007
	 */
	private static class Star
	{
		public double x, y;

		public double dx, dy;

		private Color color;

		public double depth;

		private boolean twinkle;

		private int twinkleCount = 0;

		public Star( int x, int y, Color col )
		{
			this.x = x;
			this.y = y;
			this.color = col;
			twinkle = Util.getGraphicsRandomGenerator().nextDouble() < .05;
			if ( twinkle )
				twinkleCount = Util.getGraphicsRandomGenerator().nextInt( 50 );
			// Simulated depth. Multiplied by the localPlayer's dx and dy to determine speed.
			depth = Util.getGraphicsRandomGenerator().nextDouble() * .5;

			// Force some to the 'background'.
			if ( Util.getGraphicsRandomGenerator().nextInt( 15 ) == 0 )
				depth /= 3;

			// Some stars also move.
			if ( !twinkle && Util.getGraphicsRandomGenerator().nextInt( 10 ) == 0 )
			{
				dx = Util.getGraphicsRandomGenerator().nextDouble() - 0.5;
				dy = Util.getGraphicsRandomGenerator().nextDouble() - 0.5;
				dx *= .07;
				dy *= .07;
			}
		}

		public void checkWrap()
		{
			// Wrap to stay inside the level.
			if ( x <= Math.abs( dx ) )
				x += Game.getInstance().GAME_WIDTH - 1;
			if ( y <= Math.abs( dy ) )
				y += Game.getInstance().GAME_HEIGHT - 1;
			if ( x > Game.getInstance().GAME_WIDTH - Math.abs( dx ) )
				x -= Game.getInstance().GAME_WIDTH - 1;
			if ( y > Game.getInstance().GAME_HEIGHT - Math.abs( dy ) )
				y -= Game.getInstance().GAME_HEIGHT - 1;
		}

		public Color getColor()
		{
			if ( !twinkle )
				return color;
			twinkleCount++;
			twinkleCount %= 50;
			return twinkleCount == 0 ? Color.black : color;
		}
	}

	/**
	 * A message drawn on the star background.
	 * @author Andy Kooiman
	 * @since December 16, 2007
	 */
	private class BackgroundMessage
	{
		public double x, y;

		public double dy;

		public String message;

		public Color col;

		public int life, lifeMax;

		public Font font;

		public BackgroundMessage( double x, double y, String message, Color col )
		{
			this.x = x;
			this.y = y;
			this.message = message;
			this.col = col;

			life = lifeMax = Util.getGraphicsRandomGenerator().nextInt( 30 ) + 40;
			dy = -Util.getGraphicsRandomGenerator().nextDouble() * 4;
			font = new Font( "Century Gothic", Font.BOLD, 10 );
		}

		public BackgroundMessage( double x, double y, String message, double dy, Color col, int life, Font font )
		{
			this.x = x;
			this.y = y;
			this.dy = dy;
			this.message = message;
			this.col = col;
			this.life = lifeMax = life;
			this.font = font;
		}

		private void draw( Graphics gBack )
		{
			y += dy;

			Color c = new Color( col.getRed() * life / lifeMax, col.getGreen() * life / lifeMax, col.getBlue() * life / lifeMax );

			gBack.setFont( font );
			MainWindow.frame().drawString( gBack, (int) ( dy == 0 ? x : x + 3 * Math.cos( life / 5.0 ) ), (int) y, message, c );
		}
	}
}
