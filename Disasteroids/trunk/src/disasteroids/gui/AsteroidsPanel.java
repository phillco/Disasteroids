/*
 * DISASTEROIDS | GUI
 * AsteroidsPanel.java
 */
package disasteroids.gui;

import disasteroids.Game;
import disasteroids.GameObject;
import disasteroids.Running;
import disasteroids.Settings;
import disasteroids.Ship;
import disasteroids.networking.Client;
import disasteroids.sound.Sound;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Panel;
import java.awt.RenderingHints;
import java.awt.image.VolatileImage;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.JOptionPane;

/**
 *
 * @author Phillip Cohen
 * @since January 15, 2008
 */
public class AsteroidsPanel extends Panel
{
    /**
     * The <code>Image</code> used for double buffering.
     * @since Classic
     */
    Image virtualMem;

    /**
     * The star background.
     * @since Classic
     */
    Background background;

    /**
     * The time stored at the beginning of the last call to paint; Used for FPS.
     * @since December 15, 2007
     */
    long timeOfLastRepaint;

    /**
     * Whether the user is pressing the scoreboard key.
     * @since December 15 2007
     */
    boolean drawScoreboard;

    /**
     * Notification messages always shown in the top-left corner.
     * @since December 19, 2004
     */
    ConcurrentLinkedQueue<NotificationMessage> notificationMessages = new ConcurrentLinkedQueue<NotificationMessage>();

    /**
     * The current amount of FPS. Updated in <code>paint</code>.
     * @since December 18, 2007
     */
    int lastFPS;

    /**
     * Stores whether a high score has been achieved by this player.
     * @since December 20, 2007
     */
    boolean highScoreAchieved;

    /**
     * @since December 29, 2007
     */
    boolean showWarpDialog;

    /**
     * Whether the endgame should be shown on next paint cycle.
     * @since December 30, 2007
     */
    boolean shouldEnd;
        
    /**
     * Whether to show localPlayer's coordinates.
     * @since January 18, 2008
     */
    boolean showTracker = false;

    /**
     * The number of times that the paint method has been called, for FPS
     * @since January 10, 2008
     */
    int paintCount;

    AsteroidsFrame parent;

    public AsteroidsPanel( AsteroidsFrame parent )
    {
        paintCount = 0;
        this.parent = parent;

    }

    /**
     * Draws game elements
     * @param g     buffered <code>Graphics</code> context to draw on
     * @since December 21, 2007
     */
    private void draw( Graphics g )
    {
        if ( parent.localPlayer() == null )
            return;

        // Anti-alias, if the user wants it.
        updateQualityRendering( g, Settings.qualityRendering );

        // Calculate FPS.
        if ( ++paintCount % 10 == 0 )
        {
            long timeSinceLast = -timeOfLastRepaint + ( timeOfLastRepaint = System.currentTimeMillis() );
            if ( timeSinceLast > 0 )
                lastFPS = (int) ( 10000.0 / timeSinceLast );
        }

        if ( !highScoreAchieved && parent.localPlayer().getScore() > Settings.highScore )
        {
            Running.log( "New high score of " + AsteroidsFrame.insertThousandCommas( parent.localPlayer().getScore() ) + "!", 800 );
            highScoreAchieved = true;
        }

        // Draw the star background.
        if ( background == null )
            background = new Background( Game.getInstance().GAME_WIDTH, Game.getInstance().GAME_HEIGHT );
        else
            g.drawImage( background.render(), 0, 0, this );

        // Draw stuff in order of importance, from least to most.        
        ParticleManager.draw( g );

        Game.getInstance().asteroidManager().draw( g );

        for ( GameObject go : Game.getInstance().gameObjects )
            go.draw( g );

        // Update the ships.
        for ( Ship s : Game.getInstance().players )
            s.draw( g );

        if ( shouldEnd )
            endGame( g );
        
        // Draw the on-screen HUD.
        drawHud( g );

        // Draw the entire scoreboard.
        if ( drawScoreboard )
            drawScoreboard( g );
    }

    /**
     * Sets up double buffering.
     * Uses hardware acceleration, if desired.
     * 
     * @since December 25, 2007
     */
    private void initBuffering()
    {
        // Create the buffer.
        if ( Settings.hardwareRendering )
            virtualMem = getGraphicsConfiguration().createCompatibleVolatileImage( getWidth(), getHeight() );
        else
            virtualMem = createImage( getWidth(), getHeight() );

        Game.getInstance().startGame();
    }

    /**
     * Steps the game and paints all components onto the screen.
     * Uses hardware acceleration, if desired.
     * 
     * @param g the <code>Graphics</code> context of the screen
     * @since Classic
     */
    @Override
    public void paint( Graphics g )
    {
        // Create the image if needed.
        if ( virtualMem == null )
            initBuffering();

        if ( showWarpDialog )
        {
            try
            {
                Game.getInstance().setPaused( false );
                Game.getInstance().warp( Integer.parseInt( JOptionPane.showInputDialog( null, "Enter the level number to warp to.", Game.getInstance().getLevel() ) ) );
            }
            catch ( NumberFormatException e )
            {
                // Do nothing with incorrect input or cancel.
                Running.log( "Invalid warp command.", 800 );
            }
            showWarpDialog = false;
        }
        // Render in hardware mode.
        if ( Settings.hardwareRendering )
            do
            {
                // If the resolution has changed causing an incompatibility, re-create the VolatileImage.
                if ( ( (VolatileImage) virtualMem ).validate( getGraphicsConfiguration() ) == VolatileImage.IMAGE_INCOMPATIBLE )
                    initBuffering();

                // Draw the game's graphics.
                draw( virtualMem.getGraphics() );

            } while ( ( (VolatileImage) virtualMem ).contentsLost() ); // Render in software mode.
        else
            // Draw the game's graphics.
            draw( virtualMem.getGraphics() );

        // Flip the buffer to the screen.
        g.drawImage( virtualMem, 0, 0, this );

        repaint();

    }

    /**
     * Shows a dialog to warps to a particular level.
     * 
     * @since November 15 2007
     */
    void warpDialog()
    {
        showWarpDialog = true;
    }

    public void nextLevel()
    {
        background.init();
    }

    /**
     * Draws the on-screen information for the local player.
     * 
     * @param g <code>Graphics</code> to draw on
     * @since December 15, 2007
     */
    private void drawHud( Graphics g )
    {
        Graphics2D g2d = (Graphics2D) g;
        String text = "";
        int x = 0, y = 0;

        // Draw the "lives" string.
        g2d.setColor( parent.localPlayer().getColor() );
        g2d.setFont( new Font( "Tahoma", Font.ITALIC, 12 ) );
        if ( parent.localPlayer().livesLeft() == 1 )
            text = "life";
        else
            text = "lives";
        x = getWidth() - 40;
        y = getHeight() - 15;
        g2d.drawString( text, x, y );
        x -= 10;

        // Draw the lives counter.
        g2d.setFont( new Font( "Tahoma", Font.BOLD, 16 ) );
        text = "" + parent.localPlayer().livesLeft();
        x -= (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth();
        g2d.drawString( text, x, y );
        x -= 15;

        // Draw the level counter.
        g2d.setColor( Color.lightGray );
        g2d.setFont( new Font( "Tahoma", Font.BOLD, 16 ) );
        text = "" + Game.getInstance().level;
        x -= (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth();
        g2d.drawString( text, x, y );

        // Draw the "level" string.
        g2d.setFont( new Font( "Tahoma", Font.ITALIC, 12 ) );
        text = "level";
        x -= (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() + 8;
        g2d.drawString( text, x, y );

        // Draw the score counter.
        g2d.setColor( Color.gray );
        g2d.setFont( new Font( "Tahoma", Font.BOLD, 16 ) );
        text = AsteroidsFrame.insertThousandCommas( parent.localPlayer().getScore() );
        y = 18;
        x = getWidth() - (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() - 12;
        g2d.drawString( text, x, y );

        // Draw the "score" string.
        g2d.setFont( new Font( "Tahoma", Font.ITALIC, 12 ) );
        text = "score";
        x -= (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() + 8;
        g2d.drawString( text, x, y );

        if ( showTracker )
            g2d.drawString( parent.localPlayer().toString(), 60, 60 );

        // Draw the fps counter.
        g2d.setColor( Color.darkGray );
        g2d.setFont( new Font( "Tahoma", Font.BOLD, 16 ) );
        text = "" + lastFPS;
        x = 8;
        y = (int) ( getHeight() - g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getHeight() ) - 2;
        g2d.drawString( text, x, y );

        // Draw the "fps" string.
        x += (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() + 8;
        g2d.setFont( new Font( "Tahoma", Font.ITALIC, 12 ) );
        text = "fps";
        g2d.drawString( text, x, y );

        // Draw notification messages.
        x = 8;
        y = 20;
        g2d.setFont( new Font( "Tahoma", Font.ITALIC, 12 ) );
        g2d.setColor( Color.lightGray );
        Iterator<NotificationMessage> itr = notificationMessages.iterator();

        while ( itr.hasNext() )
        {
            NotificationMessage m = itr.next();
            text = m.message;
            g2d.drawString( text, x, y );
            y += (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getHeight() + 2;
            if ( m.life-- <= 0 )
                itr.remove();
        }

        // Draw "waiting for server...".
        if ( Client.is() && Client.getInstance().serverTimeout() )
        {
            g2d.setFont( new Font( "Tahoma", Font.BOLD, 18 ) );
            g2d.setColor( Color.GREEN );

            text = "Waiting for server...";
            x = getWidth() / 2 - (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() / 2;
            y = getHeight() / 2 - 50;

            g2d.drawString( text, x, y );
        }
    }

    /**
     * Draws the full scoreboard of all players.
     * 
     * @param g <code>Graphics</code> context to draw on
     * @since December 15, 2007
     */
    private void drawScoreboard( Graphics g )
    {
        Graphics2D g2d = (Graphics2D) g;
        String text = "";
        int x = 0, y = 0;

        // Draw the "scoreboard" string.
        g2d.setColor( Color.white );
        g2d.setFont( new Font( "Tahoma", Font.BOLD, 24 ) );
        text = "Scoreboard";
        x = getWidth() / 2 - (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() / 2;
        y = getHeight() / 4;
        g2d.drawString( text, x, y );
        y += (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getHeight() + 10;

        // Draw the asteroid count.
        g2d.setColor( Color.lightGray );
        g2d.setFont( new Font( "Tahoma", Font.PLAIN, 16 ) );
        text = AsteroidsFrame.insertThousandCommas( Game.getInstance().asteroidManager().size() ) + " asteroid" + ( Game.getInstance().asteroidManager().size() == 1 ? " remains" : "s remain" );
        x = getWidth() / 2 - (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() / 2;
        g2d.drawString( text, x, y );
        y += (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getHeight() + 10;

        // Create the columns.
        ScoreboardColumn[] columns = new ScoreboardColumn[4];
        columns[0] = new ScoreboardColumn( getWidth() * 2 / 7, "Name" );
        columns[1] = new ScoreboardColumn( getWidth() * 1 / 2, "Score" );
        columns[2] = new ScoreboardColumn( getWidth() * 3 / 5, "Lives" );
        columns[3] = new ScoreboardColumn( getWidth() * 2 / 3, "# Destroyed" );

        // Draw the column headers.
        y += 15;
        g2d.setColor( Color.darkGray );
        g2d.setFont( new Font( "Tahoma", Font.PLAIN, 14 ) );
        for ( ScoreboardColumn c : columns )
            g2d.drawString( c.title, c.x, y );

        g2d.drawLine( columns[0].x, y + 5, columns[columns.length - 1].x + (int) g2d.getFont().getStringBounds( columns[columns.length - 1].title, g2d.getFontRenderContext() ).getWidth(), y + 5 );
        y += (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getHeight() + 10;

        // Draw the entries.
        for ( Ship s : Game.getInstance().players )
        {
            g2d.setColor( s.getColor() );
            if ( s == parent.localPlayer() )
                g2d.setFont( g2d.getFont().deriveFont( Font.BOLD ) );
            else
                g2d.setFont( g2d.getFont().deriveFont( Font.PLAIN ) );
            for ( int i = 0; i < columns.length; i++ )
            {
                switch ( i )
                {
                    case 0:
                        text = s.getName();
                        break;
                    case 1:
                        text = AsteroidsFrame.insertThousandCommas( s.score() );
                        break;
                    case 2:
                        text = AsteroidsFrame.insertThousandCommas( s.livesLeft() );
                        break;
                    case 3:
                        text = AsteroidsFrame.insertThousandCommas( s.getNumAsteroidsKilled() );
                        break;
                    default:
                        text = "";
                }
                g2d.drawString( text, columns[i].x, y );
            }
            y += (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getHeight() + 3;
        }

        // Draw the high scorer.
        g2d.setFont( new Font( "Tahoma", Font.PLAIN, 12 ) );
        text = "All-time high scorer " + ( highScoreAchieved ? "was " : "is " ) + Settings.highScoreName + " with " + AsteroidsFrame.insertThousandCommas( (int) Settings.highScore ) + " points.";
        x = getWidth() / 2 - (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() / 2;
        y += 40;
        g2d.setColor( Color.white );
        g2d.drawString( text, x, y );

        // Boost player egos.
        if ( highScoreAchieved )
        {
            y += (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getHeight() + 3;
            if ( parent.localPlayer().getName().equals( Settings.highScoreName ) )
                text = "But hey, everyone likes to beat their own score.";
            else
                text = "But you're much better with your shiny " + AsteroidsFrame.insertThousandCommas( parent.localPlayer().getScore() ) + "!";
            x = getWidth() / 2 - (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() / 2;
            g2d.drawString( text, x, y );
        }
    }

    /**
     * Displays the end game messages.
     * @param g the <code>Graphics</code> context of the screen
     * 
     * @since Classic
     */
    private void endGame( Graphics g )
    {
        shouldEnd = false;
        g.setFont( new Font( "Tahoma", Font.BOLD, 32 ) );
        if ( Settings.soundOn )
            Sound.playInternal( Sound.GAME_OVER_SOUND );
        for ( float sat = 0; sat <= 1; sat += .005 )
        {
            g.setColor( Color.getHSBColor( sat, sat, sat ) );
            g.drawString( "Game Over", 250, 400 );
        }
        this.setIgnoreRepaint( true );

        // Find the player with the highest score.
        Ship highestScorer = Game.getInstance().players.getFirst();
        for ( Ship s : Game.getInstance().players )
            if ( s.getScore() > highestScorer.getScore() )
                highestScorer = s;

        String victoryMessage = ( Game.getInstance().players.size() > 1 ) ? highestScorer.getName() + " wins" : "You died";
        victoryMessage += " with a score of " + AsteroidsFrame.insertThousandCommas( highestScorer.getScore() ) + "!";

        // Is this a new high score?
        if ( highestScorer.getScore() > Settings.highScore )
        {
            victoryMessage += "\nThis is a new high score!";
            Settings.highScoreName = highestScorer.getName();
            Settings.highScore = highestScorer.getScore();
        }

        // Show the message box declaring victory!
        JOptionPane.showMessageDialog( this, victoryMessage );

        // Easter egg!
        //        if ( highScore > 1000000 )
        //        {
        //            try
        //            {
        //                JOptionPane.showMessageDialog( null, "HOLY CRAP!!!! YOUR SCORE IS HIGH!!!\nI NEED HELP TO COMPUTE IT" );
        //                Runtime.getRuntime().exec( "C:/Windows/system32/calc" );
        //            }
        //            catch ( Exception e )
        //            {
        //            }
        //        }

        Game.getInstance().newGame();
        this.setIgnoreRepaint( false );
        repaint();
        shouldEnd = false;
    }

    /**
     * Toggles music on/off, and writes the setting to the background.
     * 
     * @since November 15, 2007
     */
    public void toggleMusic()
    {
        Running.log( "Music " + ( Sound.toggleMusic() ? "on." : "off." ) );
    }

    /**
     * Toggles sound on/off, and writes the setting to the background.
     * 
     * @since November 15, 2007
     */
    void toggleSound()
    {
        Running.log( "Sound " + ( Sound.toggleSound() ? "on." : "off." ) );
    }

    /**
     * Toggles high-quality rendering on/off, and writes the setting to the background.
     * 
     * @since December 15, 2007
     */
    void toggleReneringQuality()
    {
        Settings.qualityRendering = !Settings.qualityRendering;
        Running.log( ( Settings.qualityRendering ? "Quality rendering." : "Speed rendering." ) );
    }

    /**
     * Enables or disables fancy rendering of the provided <code>Graphics</code>.
     * 
     * @param qualityRendering   whether to use higher-quality rendering
     * @since December 15, 2007
     */
    private void updateQualityRendering( Graphics g, boolean qualityRendering )
    {
        Graphics2D g2d = (Graphics2D) g;

        // Adjust the setting.
        if ( qualityRendering )
        {
            g2d.setRenderingHint( RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY );
            g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
            g2d.setRenderingHint( RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY );
            g2d.setRenderingHint( RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE );
            g2d.setRenderingHint( RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON );
            g2d.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );
            g2d.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
            g2d.setRenderingHint( RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE );
            g2d.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
        }
        else
        {
            g2d.setRenderingHint( RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED );
            g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
            g2d.setRenderingHint( RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED );
            g2d.setRenderingHint( RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE );
            g2d.setRenderingHint( RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF );
            g2d.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR );
            g2d.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED );
            g2d.setRenderingHint( RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT );
            g2d.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF );
        }
    }

    public void addNotificationMessage( String message, int life )
    {
        if ( message.equals( "" ) )
            return;

        notificationMessages.add( new NotificationMessage( message, life ) );
    }

    /**
     * Included to prevent the clearing of the screen between repaints
     * 
     * @param g the <code>Graphics</code> context of the screen
     * @since Classic
     */
    @Override
    public void update( Graphics g )
    {
        paint( g );
    }

    /**
     * A small class for the storage of scoreboard colums.
     * 
     * @see <code>drawScoreboard</code>
     * @author Phillip Cohen
     * @since December 15, 2007
     */
    private static class ScoreboardColumn
    {
        /**
         * x coordinate of the column's left edge.
         */
        public int x;

        /**
         * Header text.
         */
        public String title;

        /**
         * Assigns this column's data.
         * 
         * @param x     the x coordinate of the left edge
         * @param title the text of the column's header
         */
        public ScoreboardColumn( int x, String title )
        {
            this.x = x;
            this.title = title;
        }
    }

    /**
     */
    private static class NotificationMessage
    {
        public String message;

        public int life;

        public NotificationMessage( String message, int life )
        {
            this.message = message;
            this.life = life;
        }
    }
}