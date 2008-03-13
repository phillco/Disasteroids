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
    private int currentWave;

    private int wavePoints;

    public WaveGameplay()
    {
        currentWave = 5;
        wavePoints = getWavePoints( currentWave );
    }

    public void act()
    {
        // Is the wave over?
        if ( wavePoints <= 0 && Game.getInstance().asteroidManager().size() <= 0 )
        {
            Running.log( "Wave " + currentWave + " completed!", 300 );
            currentWave += 1;
            wavePoints = getWavePoints( currentWave );
        }
        int x = ( Game.getInstance().players.getFirst().getX() + Game.getInstance().GAME_WIDTH / 2 ) % Game.getInstance().GAME_WIDTH + RandomGenerator.get().nextInt( 60 ) - 30;//RandomGenerator.get().nextBoolean() ? -1999 : 1999;
        int y = ( Game.getInstance().players.getFirst().getX() + Game.getInstance().GAME_HEIGHT / 2 ) % Game.getInstance().GAME_HEIGHT + RandomGenerator.get().nextInt( 60 ) - 30;//RandomGenerator.get().nextBoolean() ? -1999 : 1999;

        // Spawn an asteroid.
        if ( wavePoints >= 50 && RandomGenerator.get().nextInt( 20 ) == 0 )
        {
            wavePoints -= 50;

            // Make it a bonus asteroid.
            if ( RandomGenerator.get().nextInt( 30 ) == 0 )
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
        if ( wavePoints >= 100 && RandomGenerator.get().nextInt( 60 ) == 0 )
        {
            wavePoints -= 100;
            Alien a = new Alien( x, y, RandomGenerator.get().nextInt( 6 ) - 3, RandomGenerator.get().nextInt( 6 ) - 3 );
            Game.getInstance().gameObjects.add( a );
            Game.getInstance().shootingObjects.add( a );
        }

        // Spawn a station.
        if ( wavePoints >= 150 && RandomGenerator.get().nextInt( 90 ) == 0 )
        {
            wavePoints -= 150;
            Station s = new Station( x, y, RandomGenerator.get().nextInt( 4 ) - 2, RandomGenerator.get().nextInt( 4 ) - 2 );
            Game.getInstance().gameObjects.add( s );
            Game.getInstance().shootingObjects.add( s );
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
        return wave * 300;
    }

    public void flatten( DataOutputStream stream ) throws IOException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void optionsKey()
    {
        try
        {
            int newWave = Integer.parseInt( JOptionPane.showInputDialog( null, "Enter the wave to start.", currentWave ) );
            Game.getInstance().asteroidManager().clear();
            Game.getInstance().gameObjects.clear();
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