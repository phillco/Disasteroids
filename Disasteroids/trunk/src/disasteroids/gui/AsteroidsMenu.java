/*
 * DISASTEROIDS
 * AsteroidsMenu.java
 */
package disasteroids.gui;

import disasteroids.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * A central class for menus.
 * @author Phillip Cohen
 * @since December 8, 2007
 */
public class AsteroidsMenu extends BufferedFrame
{
    public static final int WINDOW_HEIGHT = 400;

    public static final int WINDOW_WIDTH = 500;

    public AsteroidsMenu()
    {
        // Center on the screen.
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize( WINDOW_WIDTH, WINDOW_HEIGHT );
        setLocation( screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2 );

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
        GradientPaint gradient = new GradientPaint( 0, 0, Settings.playerColor.darker().darker(), WINDOW_WIDTH, WINDOW_HEIGHT, Color.black );
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
