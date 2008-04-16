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
    /**
     * The player that plays the midis
     */
    private static MusicPlayer musicPlayer;
    
    /**
     * Ensures that the music is being played if it should.
     */
    public static void updateMusic()
    {
        if ( Settings.isMusicOn() && !musicPlaying() )
            startMusic();
        else if ( !Settings.isMusicOn() && musicPlaying() )
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
        Settings.setMusicOn( musicPlaying() );
        return Settings.isMusicOn();

    }

    /**
     * Toggles the internal sound on / off.
     * @return Whether the sound is now on.
     */
    public static boolean toggleSound()
    {
        Settings.setSoundOn( !Settings.isSoundOn() );
        return Settings.isSoundOn();
    }

    /**
     * Returns whether the music is playing.
     * @return whether the music is playing.
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
            musicPlayer = new MusicPlayer( "res\\Music2.mid" );
        else
            musicPlayer.play();
    }

    /**
     * Pauses the music.
     */
    public static void stopMusic()
    {
        if ( musicPlayer == null )
            musicPlayer = new MusicPlayer( "res\\Music2.mid" );
        else
            musicPlayer.pause();
    }

    /**
     * Starts a new thread to play the given sound
     * @param s the <code>Sound</code> to be played
     */
    public static void playInternal( LayeredSound.SoundClip s )
    {
        if( Settings.isSoundOn() )
            new SpeakerThread(s).start();
    }

    private static class SpeakerThread extends Thread
    {
        /**
         * The object holding the sound to play
         * @since Classic
         */
        private LayeredSound.SoundClip sound;
        
        
        /**
         * The ID number of the next thread created
         */
        private static int ID=0;

        /**
         * Constructs a new <code>SpeakerThread</code> to ensure that a given sound is played
         * @param s The sound that <code>this</code> is responsible for playing
         */
        public SpeakerThread( LayeredSound.SoundClip s )
        {
            super( "Speaker Thread #" + ID++ );
            this.sound=new LayeredSound.SoundClip(s);
            
        }


        /**
         * Starts <code>this Thread</code> and either exits quickly (if there are
         * already sounds playing), or lasts indefinitely, playing each sound passed
         * to it until no other sound is left to play, at which point it terminates normally.
         */
         @Override
         public void run()
        {
            LayeredSound.getInstance().add(sound);
        }
    }
}
