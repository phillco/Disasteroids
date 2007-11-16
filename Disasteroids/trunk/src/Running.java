/*
 * DISASTEROIDS
 * Running.java
 */

import java.awt.event.*;
import java.awt.*;
import javax.swing.JOptionPane;

/**
 * Main startup-related code.
 * @author Andy Kooiman
 */
public class Running
{
    private static AsteroidsFrame aF;
    private static MenuFrame mF;

    /**
     * Application entry point.
     * @param args Command line arguments. Ignored.
     */
    public static void main( String[] args )
    {
        mF = new MenuFrame();
    }

    /**
     * Called from within <code>MenuFrame</code> when the user selects an option.
     * @param option The magic number option.
     */
    public static void exitMenu( int option )
    {
        mF.hide();
        mF = null;
        startGame( option );
    }

    /**
     * Method that starts the game (host, slve, or single player).
     * @param option The magic number option.
     */
    private static void startGame( int option )
    {
        try
        {
            boolean isPlayerOne;
            boolean isMultiPlayer = true;
            int seed = 0;

            if ( option == 1 ) // Server
            {
                AsteroidsServer.master();
                isPlayerOne = true;
                seed = (int) ( Math.random() * 10000 );
                AsteroidsServer.send( "Seed" + String.valueOf( seed ) );
                RandNumGen.init( seed );

            }
            else if ( option == 2 ) // Client
            {
                // String address=JOptionPane.showInputDialog("Enter the IP address of the host computer.");
                String address = JOptionPane.showInputDialog( "Enter the IP address of the host computer.", "165.199.176.51" );

                AsteroidsServer.slave( address );
                isPlayerOne = false;
                while ( !RandNumGen.isInitialized() )
                    ;
            }
            else
            {
                RandNumGen.init( seed );
                isPlayerOne = true;
                isMultiPlayer = false;
            }
            Sound.updateMusic();
            aF = new AsteroidsFrame( isPlayerOne, isMultiPlayer );
            aF.setSize( AsteroidsFrame.WINDOW_WIDTH, AsteroidsFrame.WINDOW_HEIGHT );
            aF.addWindowListener( new WindowAdapter()
                          {
                              @Override
                              public void windowClosing( WindowEvent e )
                              {
                                  try
                                  {
                                      AsteroidsServer.send( "exit" );
                                      AsteroidsServer.close();
                                  }
                                  catch ( NullPointerException ex )
                                  {
                                  }
                                  System.exit( 0 );
                              }
                          } );
            aF.setVisible( true );
        }
        catch ( Exception e )
        {
            JOptionPane.showMessageDialog( null, "There has been a fatal error:\n" + e.toString() + "\nThe system is down." );
            System.exit( 0 );
        }
    }

    public static AsteroidsFrame environment()
    {
        return aF;
    }
}
