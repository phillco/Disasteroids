/*
 * DISASTEROIDS
 * MainMenu.java
 */
package disasteroids.gui;

import disasteroids.Running;
import disasteroids.Settings;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;

/**
 * A simple, modular main menu.
 * @author Phillip Cohen
 * @since November 16, 2007
 */
public class MainMenu extends AsteroidsMenu implements KeyListener
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

    public MainMenu()
    {
        // Receive key events.
        addKeyListener( this );
    }

    @Override
    public void paint( Graphics g )
    {
        // Draw background and shared elements.
        super.paint( g );

        // Some positioning.
        int y = 0;

        flashControl = ( flashControl + 1 ) % 50;

        Font normal = new Font( "Tahoma", Font.PLAIN, 14 );
        Font accent = new Font( "Tahoma", Font.BOLD, 14 );

        // Draw the title.
        y += 75;
        g.setColor( Color.darkGray );
        g.setFont( new Font( "Tahoma", Font.BOLD, 36 ) );
        g.drawString( title, 110, 80 );

        y += 80;

        if ( Settings.isInSetup() )
        {
            g.setColor( Color.white );
            if ( flashControl < 25 )
            {
                int ff = ( choice == 2 ? 3 : choice );
                g.setFont( accent );
                int[] xp = { 160, 160, 160 + 5 };
                int[] yp = { y - 10 + 20 * ff, y + 20 * ff, y - 5 + 20 * ff };
                g.fillPolygon( new Polygon( xp, yp, 3 ) );
            }

            g.setColor( Color.lightGray );
            g.setFont( choice == 0 ? accent : normal );
            g.drawString( "Name:   " + Settings.getPlayerName(), 180, y );

            y += 20;
            g.setFont( choice == 1 ? accent : normal );
            g.drawString( "Color:", 180, y );

            g.setColor( Settings.getPlayerColor() );
            Polygon outline = new Polygon();
            {
                double angle = 0.3;
                double RADIUS = 10;
                int centerX = 250;
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
            g.drawString( "OK", 210, y );
            repaint();
        }
        else
        {

            g.setColor( Color.lightGray );

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
                    int[] xp = { midpoint - string_width / 2 - 10, midpoint - string_width / 2 - 10, midpoint - string_width / 2 - 5 };
                    int[] yp = { y - 10, y, y - 5 };
                    g.fillPolygon( new Polygon( xp, yp, 3 ) );
                }
                y += 25;
            }

            // Draw some settings (this is still hard coded).
            String musicString = "Music " + ( Settings.isMusicOn() ? "on" : "off" );
            String soundString = "Sound " + ( Settings.isSoundOn() ? "on" : "off" );
            String fullscreenString = ( Settings.isUseFullscreen() ? "Fullscreen" : "Windowed" );
            String renderingString = ( Settings.isQualityRendering() ? "Quality" : "Speed" );
            int height = (int) ( normal.getStringBounds( "|", ( (Graphics2D) g ).getFontRenderContext() ).getHeight() );
            g.setFont( normal );
            g.drawString( musicString, 15, ( WINDOW_HEIGHT - height ) );
            g.drawString( soundString, 15, ( WINDOW_HEIGHT - 2 * height ) );
            g.drawString( fullscreenString,
                          WINDOW_WIDTH - 15 - (int) ( normal.getStringBounds( soundString, ( (Graphics2D) g ).getFontRenderContext() ).getWidth() ),
                          ( WINDOW_HEIGHT - height ) );
            g.drawString( renderingString,
                          WINDOW_WIDTH - 15 - (int) ( normal.getStringBounds( soundString, ( (Graphics2D) g ).getFontRenderContext() ).getWidth() ),
                          ( WINDOW_HEIGHT - 2 * height ) );
        }
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
                {
                    if ( Settings.isInSetup() )
                    {
                        if ( choice == 0 )
                            Settings.setPlayerName( JOptionPane.showInputDialog( this, "Enter your name.", Settings.getPlayerName() ) );
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
                            choice = 0;
                            Settings.setInSetup( true );
                        }
                        else
                        {
                            setVisible(false);
                            dispose();
                            Running.startGame( MenuOption.values()[choice] );
                        }
                    }
                }
                break;

            // Changing a setting?
            case KeyEvent.VK_M:
                Settings.setMusicOn( !Settings.isMusicOn() );
                break;
            case KeyEvent.VK_S:
                Settings.setSoundOn( !Settings.isSoundOn() );
                break;
            case KeyEvent.VK_W:
            case KeyEvent.VK_F:
                Settings.setUseFullscreen( !Settings.isUseFullscreen() );
                break;
            case KeyEvent.VK_A:
                Settings.setQualityRendering( !Settings.isQualityRendering() );
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
                Running.quit();
        }
        repaint();
    }
}
