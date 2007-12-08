/*
 * DISASTEROIDS
 * MainMenu.java
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The new main menu.
 * @author Phillip Cohen
 * @since Nov 16, 2007
 */
public class MainMenu extends BufferedFrame implements KeyListener
{
    public static final int WINDOW_WIDTH = 400;
    public static final int WINDOW_HEIGHT = 250;
    private static final String title = "DISASTEROIDS!";
    private int choice = 0;

    public MainMenu()
    {
        // Center on the screen.
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation( screenSize.width / 2, screenSize.height / 2 );
        setSize( WINDOW_WIDTH, WINDOW_HEIGHT );

        // Allow us to be closed and keyed.
        addWindowListener( new CloseAdapter() );
        addKeyListener( this );

        setTitle("Disasteroids!");
        
        // Show the form.
        setVisible( true );
    }

    @Override
    public void paint( Graphics g )
    {
        // Some positioning.
        int y = 75;

        Font normal = new Font( "Tahoma", Font.PLAIN, 14 );
        Font accent = new Font( "Tahoma", Font.BOLD, 14 );

        // Draw the background.
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gradient = new GradientPaint( 0, 0, Color.darkGray, WINDOW_WIDTH, WINDOW_HEIGHT, Color.lightGray );
        g2d.setPaint( gradient );
        g2d.fillRect( 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT );

        // Draw the title.
        g.setColor( Color.BLACK );
        g.setFont( new Font( "Tahoma", Font.BOLD, 36 ) );
        g.drawString( title, 60, 75 );

        y += 30;

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
        String musicString = "Music " + ( Settings.musicOn ? "on" : "off" );
        String soundString = "Sound " + ( Settings.soundOn ? "on" : "off" );
        String fullscreenString = ( Settings.useFullscreen ? "Fullscreen" : "Windowed" );
        int height = (int) ( normal.getStringBounds( "|", ( (Graphics2D) g ).getFontRenderContext() ).getHeight() );

        g.setFont( normal );
        g.drawString( musicString, 15, ( WINDOW_HEIGHT - height ) );
        g.drawString( fullscreenString, 15, ( WINDOW_HEIGHT - 2 * height ) );
        g.drawString( soundString,
                      WINDOW_WIDTH - 15 - (int) ( normal.getStringBounds( soundString, ( (Graphics2D) g ).getFontRenderContext() ).getWidth() ),
                      ( WINDOW_HEIGHT - height ) );
    }

    private void moveSelectionUp()
    {
        this.choice -= 1;
        if ( choice < 0 )
            choice = MenuOption.values().length - 1;
    }

    private void moveSelectionDown()
    {
        this.choice = ( choice + 1 ) % MenuOption.values().length;
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
                Running.exitMenu( MenuOption.values()[choice] );
                break;

            // Changing a setting?
            case KeyEvent.VK_M:
                Settings.musicOn = !Settings.musicOn;
                break;
            case KeyEvent.VK_S:
                Settings.soundOn = !Settings.soundOn;
                break;
            case KeyEvent.VK_F:
                Settings.useFullscreen = !Settings.useFullscreen;
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

    private static class CloseAdapter extends WindowAdapter
    {
        @Override
        public void windowClosing( WindowEvent e )
        {
            Running.quit();
        }
    }
}
