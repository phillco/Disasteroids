/*
 * DISASTEROIDS
 * Sound.java
 */

/**
 * The central sound and music class.
 * @author Phillip Cohen
 */
public class Sound
{
    private static MusicPlayer musicPlayer;

    /**
     * Ensures that the music is being played if it should.
     */
    public static void updateMusic()
    {
        if ( Settings.musicOn && !musicPlaying() )
            startMusic();
        else if ( !Settings.musicOn && musicPlaying() )
            stopMusic();
    }

    /**
     * Toggles the music on / off.
     * @return Whether the music is now on.
     */
    public static boolean toggleMusic()
    {
        if ( musicPlaying() )
            stopMusic();
        else
            startMusic();
        Settings.musicOn  = musicPlaying();
        return Settings.musicOn;

    }
    
    /**
     * Toggles the internal sound on / off.
     * @return Whether the sound is now on.
     */
    public static boolean toggleSound()
    {
        Settings.soundOn = ! Settings.soundOn;
        return Settings.soundOn;
    }

    /**
     * Returns whether the music is playing.
     */
    public static boolean musicPlaying()
    {
        if ( musicPlayer == null )
            return false;
        else
            return musicPlayer.isPlaying();
    }

    /**
     * Starts or resumes the music.
     */
    public static void startMusic()
    {
        if ( musicPlayer == null )
            musicPlayer = new MusicPlayer( "Music2.mid" );
        else
            musicPlayer.play();
    }

    /**
     * Pauses the music.
     */
    public static void stopMusic()
    {
        if ( musicPlayer == null )
            musicPlayer = new MusicPlayer( "Music2.mid" );
        else
            musicPlayer.pause();
    }

    static void beep()
    {    
        if(!Settings.soundOn)
            return;
        SpeakerThread s = new SpeakerThread();
        s.addCue( new Tone( 440, 100 ) );
        new Thread( s ).start();
    }

    static void click()
    {      
        if(!Settings.soundOn)
            return;
        SpeakerThread s = new SpeakerThread();
        s.addCue( new Tone( 200, 25 ) );
        new Thread( s ).start();
    }

    static void play( int freq, int dur )
    {    
        if(!Settings.soundOn)
            return;
        SpeakerThread s = new SpeakerThread();
        s.addCue( new Tone( freq, dur ) );
        new Thread( s ).start();
    }

    // Custom Disasteroids sounds
    static void bloomph()
    {
        if(!Settings.soundOn)
            return;
        SpeakerThread s = new SpeakerThread();
        s.addCue( new Tone( 50, 400 ) );
        new Thread( s ).start();
    }
    
    /**
     * Makes two low beeps.
     * 
     * @since January 9, 2008
     */
    static void stationFire()
    {
        if( !Settings.soundOn )
            return;
        
        SpeakerThread s = new SpeakerThread();
        s.addCue( new Tone(150, 20, 2));
        s.addCue( new Tone(150, 20, 2));
        s.addCue( new Tone(250, 30, 2));
        
        s.start();       
    }

    static void bleargh()
    {
         if(!Settings.soundOn)
            return;
        SpeakerThread s = new SpeakerThread();
        s.addCue( new Tone( 600, 20, 5 ) );
        s.addCue( new Tone( 600, 20, 5 ) );
        s.addCue( new Tone( 600, 20, 5 ) );
        s.addCue( new Tone( 600, 20, 5 ) );


        for ( int i = 500; i > 0; i -= 10 )
            s.addCue( new Tone( i, 5 ) );
        for ( int i = 0; i < 200; i += 20 )
            s.addCue( new Tone( i, 5 ) );

        new Thread( s ).start();
    }

    static void wheeeargh()
    {        
        if(!Settings.soundOn)
            return;
        SpeakerThread s = new SpeakerThread();
        for ( float sat = 0; sat <= 1; sat += .1 )
            s.addCue( new Tone( (int) ( 220 - 70 * sat ), 25 ) );
        new Thread( s ).start();
    }

    static void kablooie()
    {      
        if(!Settings.soundOn)
            return;
        SpeakerThread s = new SpeakerThread();
        for ( int i = 0; i < 1400; i += 100 )
            s.addCue( new Tone( i, 20 ) );
        new Thread( s ).start();
    }
}
