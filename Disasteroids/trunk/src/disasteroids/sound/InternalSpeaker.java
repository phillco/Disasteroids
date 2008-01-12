package disasteroids.sound;

/*
 * DISASTEROIDS
 * by Phillip Cohen and Andy Kooiman
 * 
 * APCS 1, 2006 to 2007, Period 3
 * Version - 1.0 Final (exam release)
 *
 * Run Running.class to start
 */
/**
 * @(#)InternalSpeaker.java
 *
 * Dll interface for beeping the internal pc speaker.
 *
 * @author Phillip Cohen, Martin Mosisch
 * @version 2.00 2007/4/17
 */
import disasteroids.Settings;
import java.awt.Toolkit;

class InternalSpeaker
{
    /**
     * Plays a auditive beep note at 400 hz for 100 ms.
     */
    public void beep()
    {
        play( 440, 100 );
    }

    /**
     * Plays a auditive click at 200 hz for 25 ms.
     */
    public void click()
    {
        play( 200, 25 );
    }

    /**
     * Method for playing one note or simply sound
     * @param frequency - frequency in hz
     * @param duration  - duration  in milliseconds
     * 
     * Frequency Table
     * ===================
     * C	    130.81	9121	
     * C#	138.59	8609	
     * D	    146.83	8126	
     * D#	155.56	7670	
     * E	    164.81	7239	
     * F	    174.61	6833	
     * F#	185.00	6449	
     * G	    196.00	6087	
     * G#	207.65	5746	
     * A	    220.00	5423	
     * A#	233.08	5119	
     * B	    246.94	4831	
     * Middle C	261.63	4560	
     * C#	277.18	4304	
     * D	    293.66	4063	
     * D#	311.13	3834	
     * E	    329.63	3619	
     * F	    349.23	3416	
     * F#	369.99	3224	
     * G	    391.00	3043	
     * G#	415.30	2873	
     * A	    440.00	2711	
     * A#	466.16	2559	
     * B	    493.88	2415	
     * C	    523.25	2280	
     * C#	554.37	2152	
     * D	    587.33	2031	
     * D#	622.25	1917	
     * E	    659.26	1809	
     * F	    698.46	1715	
     * F#	739.99	1612	
     * G	    783.99	1521	
     * G#	830.61	1436	
     * A	    880.00	1355	
     * A#	923.33	1292	
     * B	    987.77	1207	
     * C	    1046.50	1140
     */
    public synchronized void play( int frequency, int duration )
    {
        // If sound is disabled, exit!
        if ( !Settings.soundOn )
            return;

        InternalSpeaker.beepPCSpeaker( frequency, duration );
        try
        {
            Thread.sleep( duration );
        }
        catch ( InterruptedException interruptedexception )
        {
        }
        InternalSpeaker.beepPCSpeaker( 0, 0 );
    }

    // Interface to the DLL
    static
    {
        System.loadLibrary( "beep" );
    }

    private static native void beepPCSpeaker( int frequency, int duration );

    /**
     * Creates a note by the soundcard output-line.
     */
    public void makeBeep()
    {
        Toolkit.getDefaultToolkit().beep();
    }
}
