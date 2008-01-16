/*
 * DISASTEROIDS
 * LayeredSound.java
 */

package disasteroids.sound;

import disasteroids.Running;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 *
 * @author Owner
 */
public class LayeredSound {
    
    /**
     * All of the sounds waiting to be played
     * @since January 13, 2007
     */
    private ConcurrentLinkedQueue<Sound> theSounds;
    
    /**
     * The one instance of this class to be used in all cases
     * @since January 13, 2007
     */
    private static LayeredSound instance= new LayeredSound();
    
    /**
     * Creates and initializes the one instance
     * @since January 13, 2008
     */
    private LayeredSound()
    {
        if( instance == null )
            instance=this;
        theSounds=new ConcurrentLinkedQueue<LayeredSound.Sound>();
    }
    
    /**
     * Adds a new sound to be played, and starts the sound player if it was not
     * going already.  Note that the <code>Thread</code> that calls this method 
     * when no other sounds are being played will be responsible for waiting for
     * all sounds to finish.  Other <code>Thread</code>s will simply return 
     * normally
     * 
     * @param s The sound to be played
     * @since January 13, 2007
     */
    public void add(Sound s)
    {
        theSounds.add(s);
        if(theSounds.size()==1)
            play();
    }
    
    /**
     * Begins playing all enqueued sounds simultaneously, and stops when no 
     * sounds are remaining
     * 
     * @since January 13, 2008
     */
    private void play()
    {
        try{
            AudioFormat af = new AudioFormat(8000f, 8, 1, true, false);
            SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
            sdl.open(af);
            sdl.start();
            do
            {
                //get 1 second of sound prepared
                byte[] buffer=new byte[8000];
                for(Sound s: theSounds)
                    for(int index=0; index<8000; index++)
                        buffer[index]+=s.getValue()/(Math.max(theSounds.size(),1));
                sdl.write(buffer, 0, 8000);
            }while(theSounds.size()>0);
            sdl.drain();
            sdl.stop();
            sdl.close();
        }catch(LineUnavailableException e)
        {
            Running.log(e.getMessage());
        }
    }
    
    /**
     * Removes a sound from the sounds being played
     * 
     * @param s The sound to be removed
     * @since January 13, 2008
     */
    public void remove(Sound s)
    {
        theSounds.remove(s);
    }
    
    /**
     * Gets the instance of the class to be used in all cases
     * 
     * @return the instance of <code>LayeredSound</code> to be used
     * @since January 13, 2008
     */
    public static LayeredSound getInstance()
    {
        return instance;
    }

    /**
     * Internal class to hold and manipulate a large array of bytes (sorry for 
     * same name as Sound.java, will change when I think of a better one)
     */
    public static class Sound
    {
        /**
         * The data being stored
         * @since January 13, 2008
         */
        private byte[] vals;
        
        /**
         * The index of the next value to be returned
         * @since January 13, 2008
         */
        private int index;
        
        /**
         * true if all values have been returned, false otherwise
         * @since January 13, 2008
         */
        private boolean isDone;
        
        /**
         * Creates a new Sound
         * 
         * @param vals The data to be stored
         * @since January 13, 2008
         */
        public Sound(byte[] vals)
        {
            this.vals=vals;
            index=0;
            isDone=false;
        }
        
        /**
         * Gets the next value and increments the counte. Also, removes <code>this</code>
         * if necessary.
         * 
         * @return the next value, or zero if no next value exists
         * @since January 13, 2008
         */
        public byte getValue()
        {
            if(index==vals.length)
            {
                LayeredSound.instance.remove(this);
                return 0;
            }
            return vals[index++];
        }
        
        /**
         * Returns true iff all values have been returned
         * 
         * @return true iff all values have been returne
         * @since January 13, 2008
         */
        public boolean isDone()
        {
            return isDone;
        }
        
    }
}
