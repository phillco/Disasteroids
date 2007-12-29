/*
 * DISASTEROIDS
 * ServerListenerThread.java
 */

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Network listener thread for multiplayer games.
 * @author Phillip Cohen, Andy Kooiman 
 */
public class ServerListenerThread extends Thread
{
    /**
     * Our source of messages.
     * @since Classic
     */
    private BufferedReader in;

    /**
     * Creates the listener, but doesn't start it.
     * @param in    the source of messages
     */
    public ServerListenerThread( BufferedReader in )
    {
        this.in = in;
    }

    /**
     * Runs the listening loop.
     * 
     * @since Classic
     */
    @Override
    public void run()
    {
        String fromServer;
        try
        {
            while ( ( fromServer = in.readLine() ) != null )
            {
                process( fromServer );
            }
        }
        catch ( IOException e )
        {
        }
    }

    /**
     * Takes a received command and applies it to the Game.getInstance().
     * Valid commands are:
     *      k###,###    keystrokes from the other computer, comma, timestep
     *      t###        timestep of the other computer
     *      seed###     random number seed to use
     *      exit        quits Disasteroids
     *      ng          new game
     *      hs### ""    high score number, space, name
     * 
     * @param command   the command
     */
    private void process( String command )
    {
        // A keystroke?
        if ( command.charAt( 0 ) == 'k' )
        {
            keystroke( command.substring( 1 ) );
            return;
        }
        
        // Other player's timestep.
        else if ( command.charAt( 0 ) == 't' )
        {
            Game.getInstance().setOtherPlayerTimeStep( Integer.parseInt( command.substring( 1 ) ) );
            return;
        }
        
        // Random number seed.
        else if ( command.indexOf( "Seed" ) == 0 )
        {
            RandNumGen.init( Integer.parseInt( command.substring( 4 ) ) );
            return;
        }
        
        // Quitting time!
        else if ( command.equalsIgnoreCase( "exit" ) )
        {
            Running.quit();
            return;
        }
        
        // New Game.getInstance().
        else if ( command.equals( "ng" ) )
        {
            Running.environment().newGame();
            return;
        }             
    }

    /**
     * Executes a keystroke from the other computer.
     * 
     * @param command   the ###,### string
     *                  keystroke, comma, timestep
     * @since Classic
     */
    private void keystroke( String command )
    {
        int comma = command.indexOf( "," );
        int keyCommand = Integer.parseInt( command.substring( 0, comma ) );
        int timeStep = Integer.parseInt( command.substring( comma + 1 ) );
        
        // TODO: Work for multiple comps.
        Game.getInstance().actionManager().add( new Action( Game.getInstance().players.getLast(), keyCommand, timeStep ) );
    }
}
