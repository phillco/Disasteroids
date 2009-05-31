/*
 * DISASTEROIDS
 * LayeredSound.java
 */

package disasteroids.sound;

import disasteroids.Main;
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
     * The fraction of the loudest possible volume to play the sounds at
     * @since February 22, 2009
     */
    private double volume = .1;
    
    /**
     * All of the sounds waiting to be played
     * @since January 13, 2008
     */
    private ConcurrentLinkedQueue<SoundClip> theSounds;
    
    /**
     * The one instance of this class to be used in all cases
     * @since January 13, 2008
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
        theSounds=new ConcurrentLinkedQueue<LayeredSound.SoundClip>();
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
    public void add(SoundClip s)
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
                int denominator = Math.max(theSounds.size(),1);
                for(int index=0; index<8000; index++)
                {    for(SoundClip s: theSounds)
                        buffer[index]+=s.getValue()/denominator;
                     buffer[index]*=volume;
                }
                sdl.write(buffer, 0, 8000);
            }while(theSounds.size()>0);
            sdl.drain();
            sdl.stop();
            sdl.close();
        }catch(LineUnavailableException e)
        {
            Main.log(e.getMessage());
        }
    }
    
    /**
     * Removes a sound from the sounds being played
     * 
     * @param s The sound to be removed
     * @since January 13, 2008
     */
    public void remove(SoundClip s)
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
     * Increases the volume by 1/20 max volume
     * @since Februrary 22, 2009
     */
    public void volumeUp()
    {
        volume=Math.min(1, volume+.05);
        Main.log("Volume is now "+((int)(volume*20))+"/20.",100);
    }
    
    /**
     * Decreases the volume by 1/20 max volume
     * @since February 22, 2009
     */
    public void volumeDown()
    {
        volume = Math.max(0, volume-.05);
        Main.log("Volume is now "+((int)(volume*20))+"/20.",100);
    }

    /**
     * Internal class to hold and manipulate a large array of bytes (sorry for 
     * same name as Sound.java, will change when I think of a better one)
     */
    public static class SoundClip
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
        public SoundClip(byte[] vals)
        {
            this.vals=vals;
            index=0;
            isDone=false;
        }
        
        public SoundClip(SoundClip s)
        {
            this.vals=s.vals;
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
            if(index>=vals.length)
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
