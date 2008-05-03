/*
 * DISASTEROIDS
 * WaveGameplay.java
 */
package disasteroids;

import disasteroids.gui.AsteroidsFrame;
import disasteroids.gui.RelativeGraphics;
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
        if ( wavePoints <= 0 && Game.getInstance().baddies.size() <= 0 && Game.getInstance().getAsteroidManager().size() <= 0 )
        {
            Running.log( "Wave " + currentWave + " completed!", 300 );
            currentWave += 1;
            wavePoints = getWavePoints( currentWave );
            for ( Ship s : Game.getInstance().players )
                s.restoreBonusValues();
        }

        // Spawn asteroids directly opposite from player 1.
        double x = RelativeGraphics.oppositeX() + Util.getRandomGenerator().nextInt( 100 ) - 50;
        double y = RelativeGraphics.oppositeY() + Util.getRandomGenerator().nextInt( 100 ) - 50;

        double spawnRate = Math.min( 9, Math.max( 1, ( Game.getInstance().baddies.size() + Game.getInstance().getAsteroidManager().size() ) / 20.0 ) );
        // System.out.println(spawnRate + " " + wavePoints);

        // Spawn an asteroid.
        if ( wavePoints >= 50 && Util.getRandomGenerator().nextDouble() * spawnRate <= 0.3 )
        {
            wavePoints -= 50;

            // Make it a bonus asteroid.
            if ( Util.getRandomGenerator().nextDouble() * 12 * spawnRate <= 0.3 )
            {
                Game.getInstance().getAsteroidManager().add(
                        new BonusAsteroid( x, y, Util.getRandomGenerator().nextInt( 6 ) - 3, Util.getRandomGenerator().nextInt( 6 ) - 3,
                                           Util.getRandomGenerator().nextInt( 60 ) + 40, 15 ), true );
            }
            else
            {
                Game.getInstance().getAsteroidManager().add(
                        new Asteroid( x, y, Util.getRandomGenerator().nextInt( 6 ) - 3, Util.getRandomGenerator().nextInt( 6 ) - 3,
                                      Util.getRandomGenerator().nextInt( 70 ) + 10, 15 ), true );

            }
        }

        // Spawn an alien.
        if ( wavePoints >= 100 && Util.getRandomGenerator().nextDouble() * 3 * spawnRate <= 0.3 )
        {
            wavePoints -= 100;
            Alien a = new Alien( x, y, Util.getRandomGenerator().nextDouble() * 8 - 4, Util.getRandomGenerator().nextDouble() * 8 - 4 );
            Game.getInstance().gameObjects.add( a );
            Game.getInstance().shootingObjects.add( a );
            Game.getInstance().baddies.add( a );
        }

        // Spawn a station.
        if ( wavePoints >= 150 && Util.getRandomGenerator().nextDouble() * 11 * spawnRate <= 0.3 )
        {
            wavePoints -= 150;
            Station s = new Station( x, y, Util.getRandomGenerator().nextInt( 4 ) - 2, Util.getRandomGenerator().nextInt( 4 ) - 2 );
            Game.getInstance().gameObjects.add( s );
            Game.getInstance().shootingObjects.add( s );
            Game.getInstance().baddies.add( s );
        }

        if ( wavePoints >= 100 && Util.getRandomGenerator().nextDouble() * 11 * Math.pow( Game.getInstance().blackHoles.size() + 1, 3 ) * spawnRate <= 0.3 )
        {
            wavePoints -= 100;
            Game.getInstance().gameObjects.add( new BlackHole( Util.getRandomGenerator().nextInt(Game.getInstance().GAME_WIDTH), Util.getRandomGenerator().nextInt(Game.getInstance().GAME_HEIGHT) ) );
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
            Game.getInstance().getAsteroidManager().clear();
            Game.getInstance().baddies.clear();
            Game.getInstance().blackHoles.clear();
            Game.getInstance().shootingObjects = new ConcurrentLinkedQueue<ShootingObject>( Game.getInstance().players );
            Game.getInstance().gameObjects = new ConcurrentLinkedQueue<GameObject>( Game.getInstance().players );
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
