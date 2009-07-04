/*
 * DISASTEROIDS
 * MainMenu.java
 */
package disasteroids.gui;

import disasteroids.game.levels.Classic;
import disasteroids.Main;
import disasteroids.Settings;
import disasteroids.game.levels.WaveGameplay;
import disasteroids.game.levels.EmptyLevel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;

/**
 * A simple, modular main menu.
 * NOTE: The "options" screen is really hacky. It needs to be rewritten back as a modular menu (which will itself need support for gaps and the blinking cursor).
 * @author Phillip Cohen
 * @since November 16, 2007
 */
public class MainMenu extends AsteroidsMenu implements KeyListener, MouseMotionListener, MouseListener
{
    private static final String title = "DISASTEROIDS!";

    /**
     * User's selection of the menu options.
     */
    private int choice = 0;

    /**
     * Used for the flashing icon.
     */
    private int flashControl = 0;

    private int timeInMenu = 0;

    /**
     * y-value where the options start.
     */
    private final int topOfMenuOptions = 75 + 90;

    public MainMenu()
    {
        // Receive key events.
        addKeyListener( this );
        addMouseListener( this );
        addMouseMotionListener( this );
    }

    @Override
    public void paint( Graphics g )
    {
        // Draw background and shared elements.
        super.paint( g );

        timeInMenu++;

        // Some positioning.
        int y = 0;

        flashControl = ( flashControl + 1 ) % 40;

        Font normal = new Font( "Tahoma", Font.PLAIN, 14 );
        Font accent = new Font( "Tahoma", Font.BOLD, 14 );

        // Draw the title.
        y += 75;
        g.setColor( Settings.getPlayerColor().darker().darker().darker().darker() );
        g.setFont( new Font( "Tahoma", Font.BOLD, 36 ) );
        if ( Settings.isInSetup() && timeInMenu < 50 )
            g.drawString( title, (int) ( 140 - 50 / Math.pow( timeInMenu, 1.2 ) ), 80 );
        else
            g.drawString( title, 140, 80 );

        y += 90;

        if ( Settings.isInSetup() )
        {
            g.setColor( Color.white );
            if ( flashControl < 20 )
            {
                int ff = ( choice == 2 ? 3 : choice );
                g.setFont( accent );
                int[] xp =
                {
                    190, 190, 190 + 5
                };
                int[] yp =
                {
                    y - 10 + 20 * ff, y + 20 * ff, y - 5 + 20 * ff
                };
                g.fillPolygon( new Polygon( xp, yp, 3 ) );
            }

            g.setColor( Color.lightGray );
            g.setFont( choice == 0 ? accent : normal );
            g.drawString( "Name:   " + Settings.getPlayerName(), 210, y );

            y += 20;
            g.setFont( choice == 1 ? accent : normal );
            g.drawString( "Color:", 210, y );

            g.setColor( Settings.getPlayerColor() );
            Polygon outline = new Polygon();
            {
                double angle = 0.3;
                double RADIUS = 10;
                int centerX = 280;
                int centerY = y - 2;
                outline.addPoint( (int) ( centerX + RADIUS * Math.cos( angle ) ), (int) ( centerY - RADIUS * Math.sin( angle ) ) );
                outline.addPoint( (int) ( centerX + RADIUS * Math.cos( angle + Math.PI * .85 ) ), (int) ( centerY - RADIUS * Math.sin( angle + Math.PI * .85 ) ) );
                outline.addPoint( (int) ( centerX + RADIUS * Math.cos( angle - Math.PI * .85 ) ), (int) ( centerY - RADIUS * Math.sin( angle - Math.PI * .85 ) ) );
            }
            g.fillPolygon( outline );
            g.setColor( ( Settings.getPlayerColor().getRed() + Settings.getPlayerColor().getGreen() + Settings.getPlayerColor().getBlue() > 64 * 3 ? Color.black : Color.darkGray ) );
            g.drawPolygon( outline );

            g.setColor( Color.lightGray );

            y += 40;
            g.setFont( choice == 2 ? accent : normal );
            g.drawString( "OK", 240, y );
            repaint();
        }
        else
        {

            g.setColor( Color.lightGray );
            y = topOfMenuOptions;

            // Draw the options.
            for ( int i = 0; i < MenuOption.values().length; i++ )
            {
                int midpoint = WINDOW_WIDTH / 2;
                int string_width = (int) normal.getStringBounds( MenuOption.values()[i].toString(), ( (Graphics2D) g ).getFontRenderContext() ).getWidth();
                g.setFont( choice == i ? accent : normal );
                g.drawString( MenuOption.values()[i].toString(), midpoint - string_width / 2, y );
                if ( choice == i )
                {
                    g.setFont( accent );
                    int[] xp =
                    {
                        midpoint - string_width / 2 - 10, midpoint - string_width / 2 - 10, midpoint - string_width / 2 - 5
                    };
                    int[] yp =
                    {
                        y - 10, y, y - 5
                    };
                    g.fillPolygon( new Polygon( xp, yp, 3 ) );
                }
                y += 25;
            }

            // Draw some settings.
            int height = (int) ( normal.getStringBounds( "|", ( (Graphics2D) g ).getFontRenderContext() ).getHeight() );
            drawSetting( ( (Graphics2D) g ), "Music " + ( Settings.isMusicOn() ? "on" : "off" ), 'M', 14, ( WINDOW_HEIGHT - height ), normal, false );
            drawSetting( ( (Graphics2D) g ), "Sound " + ( Settings.isSoundOn() ? "on" : "off" ), 'S', 14, ( WINDOW_HEIGHT - 2 * height ), normal, false );
            drawSetting( ( (Graphics2D) g ), ( Settings.isUseFullscreen() ? "Fullscreen" : "Windowed" ), 'F', getWidth(), ( WINDOW_HEIGHT - height ), normal, false );
            drawSetting( ( (Graphics2D) g ), ( Settings.isQualityRendering() ? "Quality" : "Speed" ), 'A', getWidth(), ( WINDOW_HEIGHT - height * 2 ), normal, false );
            String mode = "Wave";
            if ( Settings.getLastLevel() == EmptyLevel.class )
                mode = "Deathmatch";
            else if ( Settings.getLastLevel() == Classic.class )
                mode = "Linear";
            drawSetting( ( (Graphics2D) g ), "Game mode: " + mode, 'G', getWidth() / 2 + 8, ( WINDOW_HEIGHT - height ), normal, true );

        }
    }

    private void drawSetting( Graphics2D g, String statusString, char hotKey, int x, int y, Font normal, boolean centerAlign )
    {
        // Align right if on the right edge of the window.
        if ( getWidth() < x + normal.getStringBounds( statusString + "    (A)", g.getFontRenderContext() ).getWidth() )
            x -= normal.getStringBounds( statusString + "    (A)", g.getFontRenderContext() ).getWidth();
        else if ( centerAlign )
            x -= normal.getStringBounds( statusString + "    (A)", g.getFontRenderContext() ).getWidth() / 2;

        // Draw the main label.
        g.setFont( normal );
        g.setColor( Color.lightGray );
        g.drawString( statusString, x, y );
        x += (int) ( normal.getStringBounds( statusString, g.getFontRenderContext() ).getWidth() ) + 5;

        // Draw the hotkey.
        g.setFont( normal.deriveFont( Font.ITALIC ) );
        g.setColor( Color.gray );
        g.drawString( "(" + hotKey + ")", x, y );
    }

    private void moveSelectionUp()
    {
        this.choice -= 1;
        if ( choice < 0 )
            choice = ( Settings.isInSetup() ? 2 : MenuOption.values().length - 1 );
    }

    private void moveSelectionDown()
    {
        this.choice = ( choice + 1 ) % ( Settings.isInSetup() ? 3 : MenuOption.values().length );
    }

    private void makeSelection()
    {
        if ( Settings.isInSetup() )
        {
            if ( choice == 0 )
            {
                String n = JOptionPane.showInputDialog( this, "Enter your name.", Settings.getPlayerName() );
                if ( n != null && !n.equals( "" ) )
                    Settings.setPlayerName( n );
            }
            else if ( choice == 1 )
            {
                Color oldColor = Settings.getPlayerColor();
                Settings.setPlayerColor( JColorChooser.showDialog( this, "Select player color...", Settings.getPlayerColor() ) );
                if ( Settings.getPlayerColor().getRed() + Settings.getPlayerColor().getGreen() + Settings.getPlayerColor().getBlue() < 12 * 3 )
                {
                    JOptionPane.showMessageDialog( this, "Sorry, that's a bit too dark." );
                    Settings.setPlayerColor( oldColor );
                }
            }
            else
            {
                choice = 0;
                Settings.setInSetup( false );
            }
        }
        else
        {
            if ( MenuOption.values()[choice] == MenuOption.OPTIONS )
            {
                // Go to the setup menu.
                choice = 0;
                timeInMenu = 0;
                Settings.setInSetup( true );
            }
            else
            {
                if ( MenuOption.values()[choice] == MenuOption.LOAD && !new File( "res\\Game.save" ).exists() )
                {
                    JOptionPane.showMessageDialog( this, "No saved games found.", "Couldn't load...", JOptionPane.INFORMATION_MESSAGE );
                    return;
                }
                setVisible( false );
                dispose();
                Main.startGame( MenuOption.values()[choice] );
            }
        }
    }

    /*
     * Keyboard-based code.
     */
    public void keyTyped( KeyEvent e )
    {
    }

    public void keyReleased( KeyEvent e )
    {
    }

    public void keyPressed( KeyEvent e )
    {
        switch ( e.getKeyCode() )
        {
            // Selecting a choice?
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_SPACE:
                makeSelection();
                break;
            // Changing a setting?
            case KeyEvent.VK_M:
                Settings.setMusicOn( !Settings.isMusicOn() );
                break;
            case KeyEvent.VK_S:
                Settings.setSoundOn( !Settings.isSoundOn() );
                break;
            case KeyEvent.VK_F:
                Settings.setUseFullscreen( !Settings.isUseFullscreen() );
                break;
            case KeyEvent.VK_A:
                Settings.setQualityRendering( !Settings.isQualityRendering() );
                break;
            case KeyEvent.VK_G:
                if ( Settings.getLastLevel() == WaveGameplay.class )
                    Settings.setLastLevel( EmptyLevel.class );
                else if ( Settings.getLastLevel() == EmptyLevel.class )
                    Settings.setLastLevel( Classic.class );
                else
                    Settings.setLastLevel( WaveGameplay.class );
                break;

            // Scrolling?
            case KeyEvent.VK_UP:
                moveSelectionUp();
                break;
            case KeyEvent.VK_DOWN:
                moveSelectionDown();
                break;

            // Exiting?
            case KeyEvent.VK_ESCAPE:
                Main.quit();
        }
        repaint();
    }

    public void mouseMoved( MouseEvent e )
    {
        if ( Settings.isInSetup() )
        {
            int newChoice = ( e.getY() - topOfMenuOptions + 10 ) / 20;
            if ( newChoice < 0 )
                newChoice = 0;
            else if ( newChoice > 3 )
                newChoice = 3;

            if ( newChoice != 2 ) // Hack for the gap before the OK.
            {
                if ( newChoice == 3 )
                    newChoice = 2;
                this.choice = newChoice;
                repaint();
            }
        }
        else
        {
            int newChoice = ( e.getY() - topOfMenuOptions + 12 ) / 25;
            if ( newChoice < 0 )
                newChoice = 0;
            else if ( newChoice >= MenuOption.values().length )
                newChoice = MenuOption.values().length - 1;
            this.choice = newChoice;
        }
        repaint();
    }

    public void mouseClicked( MouseEvent e )
    {
        if ( ( e.getX() < WINDOW_WIDTH / 3.0 ) || ( e.getX() > WINDOW_WIDTH * 2.0 / 3.0 ) )
            return;

        makeSelection();
    }

    public void mouseDragged( MouseEvent e )
    {
    }

    public void mousePressed( MouseEvent e )
    {
    }

    public void mouseReleased( MouseEvent e )
    {
    }

    public void mouseEntered( MouseEvent e )
    {
    }

    public void mouseExited( MouseEvent e )
    {
    }
}
