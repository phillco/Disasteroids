/*
 * DISASTEROIDS
 * LinearGameplay.java
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
import java.util.Random;
import javax.swing.JOptionPane;

/**
 * The classic game mode of level-based gameplay.
 * @author Phillip Cohen
 * @since February 28, 2008
 */
public class LinearGameplay implements GameMode
{
    /**
     * The current level of the game.
     * @since Classic
     */
    int level;

    public LinearGameplay()
    {
        level = 1;
        setUpAsteroidField( level );
    }

    void setUpAsteroidField( int level )
    {
        Random rand = Util.getRandomGenerator();
        int numBonuses = 0;

        // Create regular asteroids.
        for ( int numAsteroids = 0; numAsteroids < ( level + 1 ) * 2; numAsteroids++ )
        {
            Game.getInstance().getObjectManager().addObject( new Asteroid( rand.nextInt( Game.getInstance().GAME_WIDTH ),
                                                                           rand.nextInt( Game.getInstance().GAME_HEIGHT ),
                                                                           rand.nextDouble() * 6 - 3,
                                                                           rand.nextDouble() * 6 - 3,
                                                                           rand.nextInt( 150 ) + 25,
                                                                           rand.nextInt( level * 10 + 10 ) - 9 ) );
            if ( rand.nextInt( 10 ) == 1 )
            {
                numBonuses++;
            }
        }

        // Create bonus asteroids.
        for ( int numAsteroids = 0; numAsteroids < numBonuses; numAsteroids++ )
        {
            Game.getInstance().getObjectManager().addObject( new BonusAsteroid( rand.nextInt( Game.getInstance().GAME_WIDTH ),
                                                                                rand.nextInt( Game.getInstance().GAME_HEIGHT ),
                                                                                rand.nextDouble() * 6 - 3,
                                                                                rand.nextDouble() * 6 - 3,
                                                                                rand.nextInt( 150 ) + 25,
                                                                                rand.nextInt( level * 10 + 10 ) - 9 ) );

        }
    }

    public void act()
    {
        // Advance to the next level if it's time.
        if ( shouldExitLevel() )
        {
            nextLevel();
            return;
        }
    }

    public void draw( Graphics g )
    {
        Graphics2D g2d = (Graphics2D) g;
        String text = "";
        int x = AsteroidsFrame.frame().getPanel().getWidth(), y = AsteroidsFrame.frame().getPanel().getHeight() - 18;

        // Draw the level counter.
        g2d.setColor( Color.lightGray );
        g2d.setFont( new Font( "Tahoma", Font.BOLD, 16 ) );
        text = "" + level;
        x -= (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth();
        g2d.drawString( text, x, y );

        // Draw the "level" string.
        g2d.setFont( new Font( "Tahoma", Font.ITALIC, 12 ) );
        text = "level";
        x -= (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() + 8;
        g2d.drawString( text, x, y );
    }

    /**
     * Advances to the next level.
     */
    void nextLevel()
    {
        warp( level + 1 );
    }

    /**
     * Returns if the game is ready to advance levels.
     * Checks if the <code>Asteroids</code> have been cleared and then if we're on the sandbox level.
     * 
     * @return  whether the game should advance to the next level
     */
    public boolean shouldExitLevel()
    {
        // Have the asteroids been cleared?
        if ( Game.getInstance().getObjectManager().getAsteroids().size() > 0 )
            return false;

        // Level -999 is a sandbox and never exits.
        if ( level == -999 )
            return false;

        // Ready to advance!
        return true;
    }

    /**
     * Advances the game to a new level.
     * 
     * @param newLevel  the level to warp to
     * @since November 15 2007
     */
    public void warp( int newLevel )
    {
        level = newLevel;

        // All players get invincibility.
        for ( Ship s : Game.getInstance().getObjectManager().getPlayers() )
        {
            s.setInvincibilityCount( 100 );
            s.increaseScore( 2500 );
        }

        Game.getInstance().getObjectManager().clearObstacles();
        Game.getInstance().actionManager.clear();

        if ( AsteroidsFrame.frame() != null )
            AsteroidsFrame.frame().nextLevel();

        setUpAsteroidField( level );
        AsteroidsFrame.addNotificationMessage( "Welcome to level " + newLevel + ".", 500 );
    }

    public void flatten( DataOutputStream stream ) throws IOException
    {
        stream.writeInt( level );
    }

    public LinearGameplay( DataInputStream stream ) throws IOException
    {
        this.level = stream.readInt();
    }

    public void optionsKey()
    {
        try
        {
            warp( Integer.parseInt( JOptionPane.showInputDialog( null, "Enter the level number to warp to.", level ) ) );
        }
        catch ( NumberFormatException e )
        {
            // Do nothing with incorrect or cancelled input.
            Running.log( "Invalid warp command.", 800 );
        }
    }
}
