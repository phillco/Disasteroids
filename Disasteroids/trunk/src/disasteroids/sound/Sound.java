/*
 * DISASTEROIDS
 * Sound.java
 */
package disasteroids.sound;

import disasteroids.Settings;
import java.util.Arrays;
import java.util.List;

/**
 * The central sound and music class.
 * @author Phillip Cohen
 */
public class Sound
{
    private static MusicPlayer musicPlayer;

    /**
     * A simple click.
     * @since Classic
     */
    public static final Tone[] SHIP_SHOOT_SOUND = { new Tone( 200, 25 ) };

    /**
     * Three low beeps.
     * @since January 9, 2008
     */
    public static final Tone[] STATION_SHOOT_SOUND = { new Tone( 150, 20, 2 ), new Tone( 150, 20, 2 ), new Tone( 250, 30, 2 ) };

    /**
     * A long, low wail.
     * @since Classic
     */
    public static final Tone[] ASTEROID_DIE_SOUND = { new Tone( 50, 350 ) };

    /**
     * A descending cresendo with capping beeps.
     * @since Classic
     */
    public static final Tone[] SHIP_LOSE_LIFE_SOUND = new Tone[65];

    static
    {
        int idx = 0;
        SHIP_LOSE_LIFE_SOUND[idx++] = new Tone( 600, 20, 5 );
        SHIP_LOSE_LIFE_SOUND[idx++] = new Tone( 600, 20, 5 );
        SHIP_LOSE_LIFE_SOUND[idx++] = new Tone( 600, 20, 5 );

        for ( int i = 500; i > 0; i -= 10 )
            SHIP_LOSE_LIFE_SOUND[idx++] = new Tone( i, 5 );

        for ( int i = 0; i < 200; i += 20 )
            SHIP_LOSE_LIFE_SOUND[idx++] = new Tone( i, 5 );
    }
    /**
     * A simple rising whine.
     * @since Classic
     */
    public static final Tone[] GAME_OVER_SOUND = new Tone[11];

    static
    {
        int idx = 0;
        for ( float i = 0; i <= 1; i += .1 )
            GAME_OVER_SOUND[idx++] = new Tone( (int) ( 220 - 70 * i ), 25 );
    }
    /**
     * A surging, high blast.
     * @since Classic
     */
    public static final Tone[] BERSERK_SOUND = new Tone[15];

    static
    {
        int idx = 0;
        for ( int i = 0; i < 1400; i += 100 )
            BERSERK_SOUND[idx++] = new Tone( i, 20 );
    }
    /**
     * A low, climbing hum.
     * 
     * @since January 11, 2008
     */
    public static final Tone[] MINE_ARM_SOUND = new Tone[10];

    static
    {
        int idx = 0;
        for ( int i = 40; i < 70; i += 10 )
            MINE_ARM_SOUND[idx++] = new Tone( i, 60, 0 );
        MINE_ARM_SOUND[idx++] = new Tone( 72, 200, 0 );
    }

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
        Settings.musicOn = musicPlaying();
        return Settings.musicOn;

    }

    /**
     * Toggles the internal sound on / off.
     * @return Whether the sound is now on.
     */
    public static boolean toggleSound()
    {
        Settings.soundOn = !Settings.soundOn;
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

    /**
     * Plays a sound sequence on the internal speaker.
     * 
     * @param tones the array of <code>Tones</code> (see Sound constants)
     * @since January 11, 2008
     */
    public static void playInternal( Tone[] tones )
    {
        if ( Settings.soundOn )
            new SpeakerThread( tones ).start();
    }

    private static class SpeakerThread extends Thread
    {
        /**
         * The list of tones to play.
         * @since Classic
         */
        private List<Tone> tones;

        /**
         * Creates the thread with a pre-made list of tones.
         * 
         * @param toneList  the list of tones
         * @since January 11, 2008
         */
        public SpeakerThread( Tone[] toneList )
        {
            tones = (List<Tone>) Arrays.asList( toneList );
        }

        @Override
        public void run()
        {
            InternalSpeaker is = new InternalSpeaker();
            for ( Tone a : tones )
            {
                if ( a == null )
                    continue;

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
    }
}
