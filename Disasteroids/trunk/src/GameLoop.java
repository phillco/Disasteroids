public class GameLoop extends Thread
{
    @Override
    public void run()
    {
        System.out.println( "Game loop started." );
        while ( true )
        {
            try
            {
                if ( !Game.getInstance().paused )
                    Game.getInstance().act();

                Thread.sleep( 10 );

            }
            catch ( InterruptedException ex )
            {
                Running.fatalError( "Game loop interrupted while sleeping.", ex );
            }
        }
    }
}
