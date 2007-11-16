/*
 * DISASTEROIDS
 * MenuFrame.java
 */

import java.awt.*;
import java.awt.event.*;

/**
 * The little menu screen shown at startup.
 * @author Phillip Cohen
 */
public class MenuFrame extends Frame implements KeyListener
{
    public static final int WINDOW_WIDTH = 400;
    public static final int WINDOW_HEIGHT = 250;
    private int choice; // 0 - single - 1 - multi
    private static Graphics gBuff;
    private boolean isFirst = true;
    private Graphics g;
    private static Image virtualMem;

    public MenuFrame()
    {
        this.choice = 0;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation( screenSize.width / 2, screenSize.height / 2 );
        setSize( WINDOW_WIDTH, WINDOW_HEIGHT );
        addWindowListener( new WindowAdapter()
                   {
                       @Override
                       public void windowClosing( WindowEvent e )
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

        setVisible( true );

    }

    public void init( Graphics g )
    {
        this.g = g;
        virtualMem = createImage( WINDOW_WIDTH, WINDOW_HEIGHT );
        gBuff = virtualMem.getGraphics();
        if ( isFirst )
        {
            this.addKeyListener( this );
            virtualMem = createImage( getWidth(), getHeight() );
            gBuff = virtualMem.getGraphics();
            isFirst = false;
        }
    }

    @Override
    public void paint( Graphics g )
    {
        this.g = g;
        if ( isFirst )
            init( g );

        if ( virtualMem == null )
            init( g );

        Font normal = new Font( "Tahoma", Font.PLAIN, 14 );
        Font accent = new Font( "Tahoma", Font.BOLD, 14 );

        gBuff.setColor( Color.GRAY );
        gBuff.fillRect( 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT );
        gBuff.setColor( Color.BLACK );
        gBuff.setFont( new Font( "Tahoma", Font.BOLD, 36 ) );
        gBuff.drawString( "DISASTEROIDS!", 60, 75 );

        // Singleplayer
        gBuff.setFont( normal );
        if ( this.choice == 0 )
        {
            gBuff.setFont( accent );
            int[] xp = { 150, 150, 155 };
            int[] yp = { 100, 110, 105 };
            gBuff.fillPolygon( new Polygon( xp, yp, 3 ) );
        }
        gBuff.drawString( "Singleplayer", 160, 110 );

        // Hoster
        gBuff.setFont( normal );
        if ( choice == 1 )
        {
            gBuff.setFont( accent );
            int[] xp = { 130, 130, 135 };
            int[] yp = { 125, 135, 130 };
            gBuff.fillPolygon( new Polygon( xp, yp, 3 ) );
        }
        gBuff.drawString( "Multiplayer (hoster)", 140, 135 );

        // Client
        gBuff.setFont( normal );
        if ( choice == 2 )
        {
            gBuff.setFont( accent );
            int[] xp = { 135, 135, 140 };
            int[] yp = { 150, 160, 155 };
            gBuff.fillPolygon( new Polygon( xp, yp, 3 ) );
        }
        gBuff.drawString( "Multiplayer (client)", 145, 160 );
        gBuff.setFont( normal );

        // Exit
        gBuff.setFont( normal );
        if ( choice == 3 )
        {
            gBuff.setFont( accent );
            int[] xp = { 175, 175, 180 };
            int[] yp = { 175, 185, 180 };
            gBuff.fillPolygon( new Polygon( xp, yp, 3 ) );
        }
        gBuff.drawString( "Exit", 185, 185 );


        // Draw some settings.
        String musicString = "Music " + ( Settings.musicOn ? "on" : "off" );
        String soundString = "Sound " + ( Settings.soundOn ? "on" : "off" );

        gBuff.setFont( normal );
        int height = (int) ( normal.getStringBounds( "|", ( (Graphics2D) g ).getFontRenderContext() ).getHeight() );
        gBuff.drawString( musicString, 15, ( WINDOW_HEIGHT - height ) );
        gBuff.setFont( normal );
        gBuff.drawString( soundString,
                          WINDOW_WIDTH - 15 - (int) ( normal.getStringBounds( soundString, ( (Graphics2D) g ).getFontRenderContext() ).getWidth() ),
                          ( WINDOW_HEIGHT - height ) );

        g.drawImage( virtualMem, 0, 0, this );
        repaint();
    }

    public void keyReleased( KeyEvent e )
    {
    }

    public void keyTyped( KeyEvent e )
    {
    }

    public void keyPressed( KeyEvent e )
    {
        int key = e.getKeyCode();

        // Is the user selecting a choice?
        if ( key == KeyEvent.VK_ENTER )
            if ( choice == 3 )
                System.exit( 0 );
            else
                Running.exitMenu( choice );
        else
            // Changing a setting?
            if ( key == KeyEvent.VK_M )
                Settings.musicOn = !Settings.musicOn;
            else if ( key == KeyEvent.VK_S )
                Settings.soundOn = !Settings.soundOn;
        
            // Scrolling?
            else if ( key == KeyEvent.VK_UP )
            {
                this.choice -= 1;
                if ( choice < 0 )
                    choice = 3;
            }
            else if ( key == KeyEvent.VK_DOWN )
                this.choice = ( choice + 1 ) % 4;
    }
}
