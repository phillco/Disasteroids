
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/*
 * AsteroidsMenu.java
 */
/**
 *
 * @author Phillip Cohen
 * @since Dec 8, 2007
 */
public class AsteroidsMenu extends BufferedFrame
{
    public static final int WINDOW_HEIGHT = 250;

    public static final int WINDOW_WIDTH = 400;

    public AsteroidsMenu()
    {
        // Center on the screen.
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation( screenSize.width / 2, screenSize.height / 2 );
        setSize( WINDOW_WIDTH, WINDOW_HEIGHT );

        // Allow us to be closed and keyed.
        addWindowListener( new CloseAdapter() );
        setTitle( "Disasteroids!" );
        setAlwaysOnTop( true );
        setResizable( false );

        // Show the form.
        setVisible( true );
    }

    @Override
    public void paint( Graphics g )
    {
        // Menus are always anti-aliased.
        ( (Graphics2D) g ).setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

        // Draw the background.
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gradient = new GradientPaint( 0, 0, Color.darkGray, WINDOW_WIDTH, WINDOW_HEIGHT, Color.lightGray );
        g2d.setPaint( gradient );
        g2d.fillRect( 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT );
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
