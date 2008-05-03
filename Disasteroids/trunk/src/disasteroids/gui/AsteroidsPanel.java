/*
 * DISASTEROIDS | GUI
 * AsteroidsPanel.java
 */
package disasteroids.gui;

import disasteroids.BlackHole;
import disasteroids.Game;
import disasteroids.GameLoop;
import disasteroids.GameObject;
import disasteroids.Running;
import disasteroids.Settings;
import disasteroids.Ship;
import disasteroids.Util;
import disasteroids.networking.Client;
import disasteroids.sound.Sound;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Panel;
import java.awt.RenderingHints;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

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
    int lastFPS = 0;

    LinkedList<Integer> benchmarkFPS;

    int averageFPS = 0;

    /**
     * Stores whether a high score has been achieved by this player.
     * @since December 20, 2007
     */
    boolean highScoreAchieved;

    /**
     * @since December 29, 2007
     */
    boolean showWarpDialog;

    /*
     * Whether to show localPlayer's coordinates.
     * @since January 18, 2008
     */
    boolean showTracker = false;

    /**
     * The number of times that the paint method has been called, for FPS.
     * @since January 10, 2008
     */
    int paintCount = 0;

    /**
     * Positive number at which the screen rumbles; the greater the value, the greater the (random) distance.
     * @since April 7, 2008
     */
    double rumble = 0.0;

    /**
     * Offsets that objects should be drawn at during rumbling.
     * @since April 7, 2008
     */
    int rumbleX = 0, rumbleY = 0;

    /**
     * The parent frame containing this.
     * @since January 15, 2008
     */
    AsteroidsFrame parent;

    /**
     * Whether we're in the draw() loop.
     */
    private boolean isDrawing = false;

    public AsteroidsPanel( AsteroidsFrame parent )
    {
        this.parent = parent;
        background = new Background( Game.getInstance().GAME_WIDTH, Game.getInstance().GAME_HEIGHT );
    }

    /**
     * Draws all of the game elements.
     */
    private void draw( Graphics g )
    {
        if ( parent.localPlayer() == null )
            return;

        if ( !GameLoop.isRunning() )
        {
            isDrawing = false;
            return;
        }
        isDrawing = true;

        // Adjust the thread's priority if it's in the foreground/background.
        if ( parent.isActive() && Thread.currentThread().getPriority() != Thread.NORM_PRIORITY )
            Thread.currentThread().setPriority( Thread.NORM_PRIORITY );
        else if ( Thread.currentThread().getPriority() != Thread.MIN_PRIORITY )
            Thread.currentThread().setPriority( Thread.MIN_PRIORITY );

        // Anti-alias, if the user wants it.
        updateQualityRendering( g, Settings.isQualityRendering() );

        // Calculate FPS.
        if ( ++paintCount % 10 == 0 )
        {
            long timeSinceLast = -timeOfLastRepaint + ( timeOfLastRepaint = System.currentTimeMillis() );
            if ( timeSinceLast > 0 )
            {
                lastFPS = (int) ( 10000.0 / timeSinceLast );

                if ( benchmarkFPS != null )
                {
                    benchmarkFPS.add( lastFPS );
                    int total = 0;
                    for ( int i : benchmarkFPS )
                        total += i;
                    averageFPS = total / benchmarkFPS.size();
                }
            }
        }

        if ( !highScoreAchieved && parent.localPlayer().getScore() > Settings.getHighScore() )
        {
            Running.log( "New high score of " + Util.insertThousandCommas( parent.localPlayer().getScore() ) + "!", 800 );
            highScoreAchieved = true;
        }

        // Draw the star background.
        if ( background == null )
            background = new Background( Game.getInstance().GAME_WIDTH, Game.getInstance().GAME_HEIGHT );
        else
            g.drawImage( background.render(), 0, 0, this );

        // Draw stuff in order of importance, from least to most.        
        ParticleManager.draw( g );

        Game.getInstance().getAsteroidManager().draw( g );
        for ( GameObject go : Game.getInstance().gameObjects )
        {
            if ( go instanceof BlackHole == false)
                go.draw( g );
            if ( !GameLoop.isRunning() )
            {
                isDrawing = false;
                return;
            }
        }
        
        for ( BlackHole b : Game.getInstance().blackHoles )
            b.draw( g );

        // Draw the on-screen HUD.
        drawHud( g );

        // Draw the entire scoreboard.
        if ( drawScoreboard )
            drawScoreboard( g );

        isDrawing = false;
    }

    /**
     * Does all of the gamer's rendering.
     */
    @Override
    public void paint( Graphics g )
    {
        // Create the image if needed.
        if ( virtualMem == null )
            virtualMem = createImage( getWidth(), getHeight() );

        // Flashing game objects.
        Local.globalFlash = !Local.globalFlash;

        // Shake the screen when hit.
        if ( rumble < 0.1 )
            rumble = 0;
        else
        {
            rumbleX = (int) ( Util.getRandomGenerator().nextDouble() * rumble - rumble / 2 );
            rumbleY = (int) ( Util.getRandomGenerator().nextDouble() * rumble - rumble / 2 );
            rumble *= 0.9;
        }

        if ( showWarpDialog )
        {
            Game.getInstance().getGameMode().optionsKey();
            showWarpDialog = false;
        }

        // Draw the game's graphics.
        draw( virtualMem.getGraphics() );

        // Flip the buffer to the screen.
        g.drawImage( virtualMem, 0, 0, this );

        repaint();
    }

    /**
     * Shows a dialog to warps to a particular level.
     */
    public void warpDialog()
    {
        showWarpDialog = true;
    }

    public void nextLevel()
    {
        background.init();
    }

    /**
     * Draws the on-screen information for the local player.
     */
    private void drawHud( Graphics g )
    {
        Graphics2D g2d = (Graphics2D) g;
        String text = "";
        int x = 0,  y = 0;

        // Draw game mode status.
        Game.getInstance().getGameMode().draw( g );

        if ( parent.localPlayer().livesLeft() < 0 )
        {
            g2d.setFont( new Font( "Tahoma", Font.BOLD, 32 ) );
            g2d.setColor( parent.localPlayer().getColor() );
            text = "GAME OVER!";
            x = getWidth() / 2 - (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() / 2;
            y = (int) ( getHeight() / 2 - g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getHeight() ) - 32;
            g2d.drawString( text, x, y );
            return;
        }

        // Draw the score counter.
        g2d.setColor( Color.gray );
        g2d.setFont( new Font( "Tahoma", Font.BOLD, 16 ) );
        text = Util.insertThousandCommas( parent.localPlayer().getScore() );
        y = 18;
        x = getWidth() - (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() - 12;
        g2d.drawString( text, x, y );

        // Draw the "score" string.
        g2d.setFont( new Font( "Tahoma", Font.ITALIC, 12 ) );
        text = "score";
        x -= (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() + 8;
        g2d.drawString( text, x, y );
        x -= (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() + 8;

        if ( showTracker )
            g2d.drawString( parent.localPlayer().toString(), 60, 60 );

        // Draw the "lives" string.
        g2d.setColor( parent.localPlayer().getColor() );
        g2d.setFont( new Font( "Tahoma", Font.ITALIC, 12 ) );
        if ( parent.localPlayer().livesLeft() == 1 )
            text = "life";
        else
            text = "lives";

        g2d.drawString( text, x, y );
        x -= 10;

        // Draw the lives counter.
        g2d.setFont( new Font( "Tahoma", Font.BOLD, 16 ) );
        text = "" + parent.localPlayer().livesLeft();
        x -= (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth();
        g2d.drawString( text, x, y );
        x -= 15;

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

        if ( benchmarkFPS != null )
        {
            g2d.setColor( benchmarkFPS.size() == 1 ? Color.lightGray : Color.darkGray );
            g2d.setFont( new Font( "Tahoma", Font.ITALIC, 12 ) );
            x -= 10;
            y += 15;
            text = "[avg: " + averageFPS + " ]";
            g2d.drawString( text, x, y );
        }

        // Draw energy.
        x = getWidth() / 2 - 50;
        y = 18;
        {
            g2d.setColor( new Color( 9, 68, 12 ) );
            g2d.fillRect( x, y, (int) ( parent.localPlayer().getHealth() ), 20 );
            g2d.setColor( new Color( 21, 98, 28 ) );
            g2d.drawRect( x, y, 100, 20 );
        }
        if ( parent.localPlayer().getShield() > 0 )
        {
            g2d.setColor( new Color( 5, 100, 100 ) );
            g2d.fillRect( x, y, (int) ( parent.localPlayer().getShield() ), 20 );
            g2d.setColor( new Color( 5, 150, 150 ) );
            g2d.drawRect( x, y, 100, 20 );
        }

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

        // Draw "pauses"...
        if ( Game.getInstance().isPaused() )
        {
            g2d.setFont( new Font( "Tahoma", Font.BOLD, 24 ) );
            g2d.setColor( Local.getLocalPlayer().getColor() );

            text = "Paused.";
            x = getWidth() / 2 - (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() / 2;
            y = getHeight() / 3 - 50;

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
        int x = 0,  y = 0;

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
        text = Util.insertThousandCommas( Game.getInstance().getAsteroidManager().size() ) + " asteroid" + ( Game.getInstance().getAsteroidManager().size() == 1 ? ", " : "s, " );
        text += Util.insertThousandCommas( Game.getInstance().baddies.size() ) + " baddie" + ( Game.getInstance().baddies.size() == 1 ? "" : "s" );
        text += " remain";
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
                        text = Util.insertThousandCommas( s.score() );
                        break;
                    case 2:
                        text = Util.insertThousandCommas( s.livesLeft() );
                        break;
                    case 3:
                        text = Util.insertThousandCommas( s.getNumAsteroidsKilled() );
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
        text = "All-time high scorer " + ( highScoreAchieved ? "was " : "is " ) + Settings.getHighScoreName() + " with " + Util.insertThousandCommas( (int) Settings.getHighScore() ) + " points.";
        x = getWidth() / 2 - (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() / 2;
        y += 40;
        g2d.setColor( Color.white );
        g2d.drawString( text, x, y );

        // Boost player egos.
        if ( highScoreAchieved )
        {
            y += (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getHeight() + 3;
            if ( parent.localPlayer().getName().equals( Settings.getHighScoreName() ) )
                text = "But hey, everyone likes to beat their own score.";
            else
                text = "But you're much better with your shiny " + Util.insertThousandCommas( parent.localPlayer().getScore() ) + "!";
            x = getWidth() / 2 - (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() / 2;
            g2d.drawString( text, x, y );
        }
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
    public void toggleSound()
    {
        Running.log( "Sound " + ( Sound.toggleSound() ? "on." : "off." ) );
    }

    /**
     * Toggles high-quality rendering on/off, and writes the setting to the background.
     * 
     * @since December 15, 2007
     */
    public void toggleReneringQuality()
    {
        Settings.setQualityRendering( !Settings.isQualityRendering() );
        Running.log( ( Settings.isQualityRendering() ? "Quality rendering." : "Speed rendering." ) );
    }

    public void toggleTracker()
    {
        showTracker = !showTracker;
    }

    public void toggleScoreboard()
    {
        drawScoreboard = !drawScoreboard;
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
     * Included to prevent the clearing of the screen between repaints.
     */
    @Override
    public void update( Graphics g )
    {
        paint( g );
    }

    public void startBenchmarkingFPS()
    {
        benchmarkFPS = new LinkedList<Integer>();
    }

    public Background getStarBackground()
    {
        return background;
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

    /**
     * Returns whether the panel is drawing game elements.
     * 
     * Should be used with <code>GameLoop.stopLoop()</code> to change the game's structure without the panel trying to draw it and getting NullPointers.
     * @see GameLoop#stopLoop()
     */
    public boolean isDrawing()
    {
        return isDrawing;
    }
}
