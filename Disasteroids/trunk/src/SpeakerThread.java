
/**
 * DISASTEROIDS
 * SpeakerThread.java
 */
import java.util.ArrayList;

/**
 * A thread that plays a list of tones.
 * @author Phillip Cohen
 * @since Classic
 */
public class SpeakerThread extends Thread
{
    /**
     * The list of tones to play.
     * @since Classic
     */
    private ArrayList<Tone> tones = new ArrayList<Tone>();

    @Override
    public void run()
    {
        InternalSpeaker is = new InternalSpeaker();
        for ( Tone a : tones )
        {
            // Play this tone. IS will sleep the thread for the duration of the tone.
            is.play( a.frequency, a.duration );

            // Additionally, tones can have a delay after they're played.
            if ( a.delayAfter > 0 )
            {
                try
                {
                    Thread.sleep( a.delayAfter );
                }
                catch ( InterruptedException e )
                {
                }
            }
        }
    }

    /**
     * Queues a tone to be played.
     * 
     * @param newTone   the tone
     * @since Classic
     */
    public void addCue( Tone newTone )
    {
        tones.add( newTone );
    }
}
