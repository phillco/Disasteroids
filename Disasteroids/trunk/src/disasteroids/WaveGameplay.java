/*
 * DISASTEROIDS
 * WaveGameplay.java
 */
package disasteroids;

import disasteroids.gui.AsteroidsFrame;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.JOptionPane;

/**
 * A game mode where players fend off waves of asteroids.
 * @author Phillip Cohen
 */
public class WaveGameplay implements GameMode
{
    /**
     * Unique ID for this class. Used for C/S.
     * @since April 11, 2008
     */
    public static final int TYPE_ID = 1;

    private int currentWave;

    private int wavePoints;

    public WaveGameplay()
    {
        currentWave = 1;
        wavePoints = getWavePoints( currentWave );
    }

    public void act()
    {
        // Is the wave over? Advance.
        if ( wavePoints <= 0 && Game.getInstance().baddies.size() <= 0 && Game.getInstance().asteroidManager().size() <= 0 )
        {
            Running.log( "Wave " + currentWave + " completed!", 300 );
            currentWave += 1;
            wavePoints = getWavePoints( currentWave );
            for ( Ship s : Game.getInstance().players )
                s.restoreBonusValues();
        }

        // Spawn asteroids directly opposite from player 1.
        double x = ( Game.getInstance().players.getFirst().getX() + Game.getInstance().GAME_WIDTH / 2 ) % Game.getInstance().GAME_WIDTH + RandomGenerator.get().nextInt( 100 ) - 50;//RandomGenerator.get().nextBoolean() ? -1999 : 1999;
        double y = ( Game.getInstance().players.getFirst().getY() + Game.getInstance().GAME_HEIGHT / 2 ) % Game.getInstance().GAME_HEIGHT + RandomGenerator.get().nextInt( 100 ) - 50;//RandomGenerator.get().nextBoolean() ? -1999 : 1999;

        double spawnRate = Math.min( 9, Math.max( 1, ( Game.getInstance().baddies.size() + Game.getInstance().asteroidManager().size() ) / 20.0 ) );
        // System.out.println(spawnRate + " " + wavePoints);

        // Spawn an asteroid.
        if ( wavePoints >= 50 && RandomGenerator.get().nextDouble() * spawnRate <= 0.3 )
        {
            wavePoints -= 50;

            // Make it a bonus asteroid.
            if ( RandomGenerator.get().nextDouble() * 12 * spawnRate <= 0.3 )
            {
                Game.getInstance().asteroidManager().add(
                        new BonusAsteroid( x, y, RandomGenerator.get().nextInt( 6 ) - 3, RandomGenerator.get().nextInt( 6 ) - 3,
                                           RandomGenerator.get().nextInt( 60 ) + 40, 15 ), true );
            }
            else
            {
                Game.getInstance().asteroidManager().add(
                        new Asteroid( x, y, RandomGenerator.get().nextInt( 6 ) - 3, RandomGenerator.get().nextInt( 6 ) - 3,
                                      RandomGenerator.get().nextInt( 70 ) + 10, 15 ), true );

            }
        }

        // Spawn an alien.
        if ( wavePoints >= 100 && RandomGenerator.get().nextDouble() * 3 * spawnRate <= 0.3 )
        {
            wavePoints -= 100;
            Alien a = new Alien( x, y, RandomGenerator.get().nextDouble() * 8 - 4, RandomGenerator.get().nextDouble() * 8 - 4 );
            Game.getInstance().gameObjects.add( a );
            Game.getInstance().shootingObjects.add( a );
            Game.getInstance().baddies.add( a );
        }

        // Spawn a station.
        if ( wavePoints >= 150 && RandomGenerator.get().nextDouble() * 11 * spawnRate <= 0.3 )
        {
            wavePoints -= 150;
            Station s = new Station( x, y, RandomGenerator.get().nextInt( 4 ) - 2, RandomGenerator.get().nextInt( 4 ) - 2 );
            Game.getInstance().gameObjects.add( s );
            Game.getInstance().shootingObjects.add( s );
            Game.getInstance().baddies.add( s );
        }

    }

    public void draw( Graphics g )
    {
        Graphics2D g2d = (Graphics2D) g;
        String text = "";
        int x = AsteroidsFrame.frame().getPanel().getWidth() - 10;
        int y = AsteroidsFrame.frame().getPanel().getHeight() - 25;

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
        x = AsteroidsFrame.frame().getPanel().getWidth() - 110;
        y += 10;
        g.setColor( Color.darkGray );
        g.drawRect( x, y, 100, 10 );
        int width = (int) ( 100 * ( 1 - (double) wavePoints / getWavePoints( currentWave ) ) );
        g.setColor( Color.lightGray );
        g.fillRect( x, y, width, 10 );
    }

    int getWavePoints( int wave )
    {
        return wave * 1200 + 100;
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
        try
        {
            int newWave = Integer.parseInt( JOptionPane.showInputDialog( null, "Enter the wave to start.", currentWave ) );
            Game.getInstance().asteroidManager().clear();
            Game.getInstance().gameObjects.clear();
            Game.getInstance().baddies.clear();
            Game.getInstance().shootingObjects = new ConcurrentLinkedQueue<ShootingObject>( Game.getInstance().players );
            currentWave = newWave;
            wavePoints = getWavePoints( currentWave );
        }
        catch ( NumberFormatException e )
        {
            // Do nothing with incorrect or cancelled input.
            Running.log( "Invalid warp command.", 800 );
        }
    }
}
