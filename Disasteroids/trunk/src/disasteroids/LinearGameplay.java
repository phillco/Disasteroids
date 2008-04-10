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
        Game.getInstance().asteroidManager().setUpAsteroidField( level );
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
     * @author Phillip Cohen
     * @since November 15 2007
     */
    void nextLevel()
    {
        warp( level + 1 );
    }

    /**
     * Returns the current level.
     * 
     * @return  the current level
     * @since Classic
     */
    public int getLevel()
    {
        return level;
    }

    /**
     * Returns if the game is ready to advance levels.
     * Checks if the <code>Asteroids</code> have been cleared, then if we're on the sandbox level, and finally if the <code>Missile</code>s have been cleared.
     * 
     * @see Settings#waitForMissiles
     * @return  whether the game should advance to the next level
     */
    public boolean shouldExitLevel()
    {
        // Have the asteroids been cleared?
        if ( Game.getInstance().asteroidManager.size() > 0 )
            return false;

        // Level -999 is a sandbox and never exits.
        if ( level == -999 )
            return false;

        // The user can choose to wait for missiles.
        if ( Settings.waitForMissiles )
            for ( Ship s : Game.getInstance().players )
                if ( s.getWeaponManager().getNumLiving() > 0 )
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

        // All players get bonuses.
        for ( Ship s : Game.getInstance().players )
        {
            s.addLife();
            s.setInvincibilityCount( 100 );
            s.increaseScore( 2500 );
            s.clearWeapons();
            s.setNumAsteroidsKilled( 0 );
            s.setNumShipsKilled( 0 );
        }

        Game.getInstance().asteroidManager.clear();
        Game.getInstance().actionManager.clear();

        if ( AsteroidsFrame.frame() != null )
            AsteroidsFrame.frame().nextLevel();
        Game.getInstance().restoreBonusValues();
        Game.getInstance().asteroidManager.setUpAsteroidField( level );
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
