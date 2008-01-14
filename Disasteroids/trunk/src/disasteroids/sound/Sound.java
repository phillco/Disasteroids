/*
 * DISASTEROIDS
 * Sound.java
 */
package disasteroids.sound;

import disasteroids.Settings;

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
    public static final byte[] SHIP_SHOOT_SOUND = new Tone( 200, 10 ).toByteArray() ;

    /**
     * Three low beeps.
     * @since January 9, 2008
     */
    public static final byte[] STATION_SHOOT_SOUND;
    
    static
    {
        Tone[] temp= { new Tone( 150, 20, 2 ), new Tone( 150, 20, 2 ), new Tone( 250, 30, 2 ) };
        STATION_SHOOT_SOUND=Tone.toByteArray(temp);
    }

    /**
     * A long, low wail.
     * @since Classic
     */
    public static final byte[] ASTEROID_DIE_SOUND = new Tone( 50, 30 ).toByteArray();

    /**
     * A descending cresendo with capping beeps.
     * @since Classic
     */
    public static final byte[] SHIP_LOSE_LIFE_SOUND = new byte[8000];

    static
    {
        for(int index=0; index<8000; index++)
             SHIP_LOSE_LIFE_SOUND[index]=(byte)(Math.sin(880f*Math.pow(index,.8)/8000.0*6.28)*127*8000/(8100-index));
    }
    /**
     * A simple rising whine.
     * @since Classic
     */
    public static final byte[] GAME_OVER_SOUND;

    static
    {
        Tone[] temp=new Tone[11];
        int idx = 0;
        for ( float i = 0; i <= 1; i += .1 )
            temp[idx++] = new Tone( (int) ( 220 - 70 * i ), 25 );
        GAME_OVER_SOUND=Tone.toByteArray(temp);
    }
    /**
     * A surging, high blast.
     * @since Classic
     */
    public static final byte[] BERSERK_SOUND;

    static
    {
        Tone[] temp=new Tone[15];
        int idx = 0;
        for ( int i = 0; i < 1400; i += 100 )
            temp[idx++] = new Tone( i, 15 );
        BERSERK_SOUND=Tone.toByteArray(temp);
    }
    /**
     * A low, climbing hum.
     * 
     * @since January 11, 2008
     */
    public static final byte[] MINE_ARM_SOUND;

    static
    {
        Tone[] temp=new Tone[10];
        int idx = 0;
        for ( int i = 40; i < 70; i += 10 )
            temp[idx++] = new Tone( i, 60, 0 );
        temp[idx++] = new Tone( 72, 200, 0 );
        MINE_ARM_SOUND=Tone.toByteArray(temp);
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
    
    /**
     * 
     */
    public static void playInternal( byte[] vals)
    {
        if( Settings.soundOn )
            new SpeakerThread( vals ).start();
    }

    private static class SpeakerThread extends Thread
    {
        /**
         * The list of tones to play.
         * @since Classic
         */
        private Tone[] tones;
        
        /**
         * The set of values to be passed in to the speaker, for higher quality
         * sounds
         * @since January 13, 2008
         */
        private byte[] vals;
        
        /**
         * The ID number of the next thread created
         */
        private static int ID=0;

        /**
         * Creates the thread with a pre-made list of tones.
         * 
         * @param toneList  the list of tones
         * @since January 11, 2008
         */
        public SpeakerThread( Tone[] toneList )
        {
            super( "Speaker Thread #" + ID++ );
            tones=toneList;
        }
        
        /**
         * Creates a new thread with the sound clip specified by the array.
         * 
         * @param The array of values to be passed directly to the speaker
         * @since January 13, 2008
         */
        public SpeakerThread( byte[] vals )
        {
            super( "Speaker Thread #" + ID++ );
            this.vals=vals;
        }

        @Override
        public void run()
        {
            if( tones != null )
                vals=Tone.toByteArray(tones);
            InternalSpeaker.play(vals);
        }
    }
}
