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
     * @param s
     */
    public static void playInternal( LayeredSound.SoundClip s )
    {
        if( Settings.soundOn )
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

        public SpeakerThread( LayeredSound.SoundClip s )
        {
            super( "Speaker Thread #" + ID++ );
            this.sound=new LayeredSound.SoundClip(s);
            
        }

        @Override
        public void run()
        {
            LayeredSound.getInstance().add(sound);
        }
    }
}
