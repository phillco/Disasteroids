package disasteroids.sound;

/**
 * A small class representing an InternalSpeaker tone.
 * @author Phil
 */
 public class Tone
{
    public int frequency;
    public int duration;
    public int delayAfter;
    public int volume;
    
    /**
     * Stores the values passed to the speaker; only non-null if it has been 
     * computed already by <code>toByteArray</code>
     * @since January 13, 2008
     * @see Tone.toByteArray
     */
    private byte[] asBytes;

    public Tone(int frequency, int duration)
    {
            this.frequency = frequency;
            this.duration = duration;
            this.delayAfter = 0;
            volume=30;
    }

    public Tone(int frequency, int duration, int delayAfter)
    {
            this.frequency = frequency;
            this.duration = duration;
            this.delayAfter = delayAfter;
            volume=30;
    }
    
    public Tone(int frequency, int duration, int delayAfter, int volume)
    {
            this.frequency = frequency;
            this.duration = duration;
            this.delayAfter = delayAfter;
            this.volume=volume;
    }
    
    /**
     * Creates and returnes a new <code>byte</code> array and fills said array
     * with the values to be passed on to the speaker representing this <code>Tone</code>
     * as a sine wave.  The array will have the first 8*duration values filled
     * with the sine wave and the remaining values at zero.
     *  
     * @return The byte arrray representing this tone
     * @since January 13, 2008
     */
    public byte[] toByteArray()
    {
        if(asBytes!=null)
            return asBytes;
        int length=duration*8+delayAfter*8;
        byte[] toBeReturned=new byte[length];
        for(int index=0; index<duration*8; index++)
            toBeReturned[index]=(byte)(volume*Math.sin(index/8000f*frequency*2*Math.PI));
        asBytes=toBeReturned;
        return toBeReturned;
    }
    
    public static byte[] toByteArray( Tone[] toneList)
    {
        byte[][] allVals=new byte[toneList.length][];
        int totalVals=0;
        for(int index=0; index<toneList.length; index++)
        {
            allVals[index]=toneList[index]!=null?toneList[index].toByteArray():null;
            totalVals+=allVals[index]==null?0:allVals[index].length;
        }
        byte[] toBeReturned=new byte[totalVals];
        int index=0;
        for(int list=0; list<allVals.length; list++)
        {
            for(int i=0; allVals[list]!=null && i<allVals[list].length; i++)
                toBeReturned[index++]=allVals[list][i];
        }
        return toBeReturned;
    }
}