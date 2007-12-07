/*
 * DISASTEROIDS
 * MainMenu.java
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
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
public class MainMenu extends Frame implements KeyListener
{
    public static final int WINDOW_WIDTH = 400;
    public static final int WINDOW_HEIGHT = 250;
    private int choice;
    private static String title = "DISASTEROIDS";
    private Graphics g;
    private Graphics gBuff;
    private Image virtualMem;

    public MainMenu ()
    {
        choice = 0;

        // Center on the screen.
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation( screenSize.width / 2, screenSize.height / 2 );
        setSize( WINDOW_WIDTH, WINDOW_HEIGHT );

        // Allow us to be closed.
        addWindowListener( new WindowAdapter()
                   {
                       @Override
                       public void windowClosing ( WindowEvent e )
                       {
                           try
                           {
                               System.exit( 0 );
                           }
                           catch ( NullPointerException ex )
                           {
                           }
                           System.exit( 0 );
                       }
                   } );

        // Yes, show the form.
        setVisible( true );
    }

    /**
     * Sets up the double buffering.
     */
    public void init ( Graphics g )
    {
        if ( gBuff == null )
        {
            this.g = g;
            this.addKeyListener( this );
            virtualMem = createImage( getWidth(), getHeight() );
            gBuff = virtualMem.getGraphics();
        }
    }

    @Override
    public void paint ( Graphics g )
    {
        // Check if we need to start buffering.
        if ( gBuff == null || virtualMem == null )
            init( g );

        // Some positioning.
        int y = 75;

        Font normal = new Font( "Tahoma", Font.PLAIN, 14 );
        Font accent = new Font( "Tahoma", Font.BOLD, 14 );

        // Draw the background.
        Graphics2D g2d = (Graphics2D) gBuff;

        // A non-cyclic gradient
        GradientPaint gradient = new GradientPaint( 0, 0, Color.darkGray, WINDOW_WIDTH, WINDOW_HEIGHT, Color.lightGray );
        g2d.setPaint( gradient );
        g2d.fillRect( 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT );

        // 
        gBuff.setColor( Color.BLACK );
        gBuff.setFont( new Font( "Tahoma", Font.BOLD, 36 ) );
        gBuff.drawString( title, 60, 75 );

        y += 30;

        for ( int i = 0; i < MenuOption.values().length; i++ )
        {
            int midpoint = WINDOW_WIDTH / 2;
            int string_width = (int) normal.getStringBounds( MenuOption.values()[i].toString(), ( (Graphics2D) g ).getFontRenderContext() ).getWidth();
            gBuff.setFont( choice == i ? accent : normal );
            gBuff.drawString( MenuOption.values()[i].toString(), midpoint - string_width / 2, y );

            if ( choice == i )
            {
                gBuff.setFont( accent );
                int[] xp = { midpoint - string_width / 2 - 10, midpoint - string_width / 2 - 10, midpoint - string_width / 2 - 5 };
                int[] yp = { y - 10, y, y - 5 };
                gBuff.fillPolygon( new Polygon( xp, yp, 3 ) );
            }
            y += 25;
        }

        // Draw some settings (this is still hard coded).
        String musicString = "Music " + ( Settings.musicOn ? "on" : "off" );
        String soundString = "Sound " + ( Settings.soundOn ? "on" : "off" );
        String fullscreenString = ( Settings.useFullscreen ? "Fullscreen" : "Windowed" );
        int height = (int) ( normal.getStringBounds( "|", ( (Graphics2D) g ).getFontRenderContext() ).getHeight() );
        
        gBuff.setFont( normal );
        gBuff.drawString( musicString, 15, ( WINDOW_HEIGHT - height ) );
        gBuff.drawString( fullscreenString, 15, ( WINDOW_HEIGHT - 2*height ) );        
        gBuff.drawString( soundString,
                          WINDOW_WIDTH - 15 - (int) ( normal.getStringBounds( soundString, ( (Graphics2D) g ).getFontRenderContext() ).getWidth() ),
                          ( WINDOW_HEIGHT - height ) );

        g.drawImage( virtualMem, 0, 0, this );
        repaint();
    }

    /**
     * Don't clear the screen.
     */
    @Override
    public void update ( Graphics g )
    {
        paint( g );
    }

    private void moveSelectionUp ()
    {
        this.choice -= 1;
        if ( choice < 0 )
            choice = MenuOption.values().length - 1;
    }

    private void moveSelectionDown ()
    {
        this.choice = ( choice + 1 ) % MenuOption.values().length;
    }

    /*
     * Keyboard-based code.
     */
    public void keyTyped ( KeyEvent e )
    {

    }

    public void keyReleased ( KeyEvent e )
    {

    }

    public void keyPressed ( KeyEvent e )
    {
        int key = e.getKeyCode();

        // Selecting a choice?
        if ( key == KeyEvent.VK_ENTER )
            Running.exitMenu( MenuOption.values()[choice] );
        // Changing a setting?
        else if ( key == KeyEvent.VK_M )
            Settings.musicOn = !Settings.musicOn;
        else if ( key == KeyEvent.VK_S )
            Settings.soundOn = !Settings.soundOn;
        else if ( key == KeyEvent.VK_F )
            Settings.useFullscreen = !Settings.useFullscreen;
        
        // Scrolling?
        else if ( key == KeyEvent.VK_UP )
            moveSelectionUp();
        else if ( key == KeyEvent.VK_DOWN )
            moveSelectionDown();
    }
}
