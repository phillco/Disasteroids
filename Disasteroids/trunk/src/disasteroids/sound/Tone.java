package disasteroids.sound;

/**
 * A small class representing an InternalSpeaker tone.
 * Always a simple sine wave
 * @author Phil
 */
public class Tone
{
    /**
     * The frequency of this <code>Tone</code>, measured in Hertz
     */
    public int frequency;

    /**
     * How long the audible portion of this <code>Tone</code> lasts, measured in milliseconds
     */
    public int duration;

    /**
     * How long the silent portion of this <code>Tone</code> lasts after the audible portion, measured in milliseconds
     */
    public int delayAfter;

    /**
     * How loud this <code>Tone</code> is; that is, what the amplitude of the sine wave is.  
     * [0,127] is the range of valid values.
     */
    public int volume;

    /**
     * Stores the values passed to the speaker; only non-null if it has been 
     * computed already by <code>toByteArray</code>
     * @since January 13, 2008
     * @see Tone.toByteArray
     */
    private byte[] asBytes;

    /**
     * Constructs a <code>new Tone</code>.
     * 
     * Equivalent to calling <code>Tone ( frequency, duration, 0, 30 );</code>
     * 
     * @param frequency The frequency, in Hz
     * @param duration The duration, in milliseconds
     */
    public Tone( int frequency, int duration )
    {
        this( frequency, duration, 0, 30 );
    }

    /**
     * Constructs a <code>new Tone</code>.
     * 
     * Equivalent to calling <code>Tone ( frequency, duration, delayAfter, 30 );</code>
     * 
     * @param frequency The frequency, in Hz
     * @param duration The duration, in milliseconds
     * @param delayAfter The delay after the sound is played, in milliseconds
     */
    public Tone( int frequency, int duration, int delayAfter )
    {
        this( frequency, duration, delayAfter, 30 );
    }

    /**
     * Constructs a <code>new Tone</code>
     * 
     * @param frequency The frequency, in Hz
     * @param duration The duration, in milliseconds
     * @param delayAfter The delay after the sound is played, in milliseconds
     * @param volume The volume of the sound, in the range [0,127]
     */
    public Tone( int frequency, int duration, int delayAfter, int volume )
    {
        this.frequency = frequency;
        this.duration = duration;
        this.delayAfter = delayAfter;
        this.volume = volume;
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
        if ( asBytes != null )
            return asBytes;
        int length = duration * 8 + delayAfter * 8;
        byte[] toBeReturned = new byte[ length ];
        for ( int index = 0; index < duration * 8; index++ )
            toBeReturned[index] = (byte) ( volume * Math.sin( index / 8000f * frequency * 2 * Math.PI ) );
        asBytes = toBeReturned;
        return toBeReturned;
    }

    /**
     * Complies every <code>Tone</code> in the list to a <code>byte</code> array and concatenates
     * these arrays to form one sound.
     * 
     * @param toneList The list of <code>Tone</code>s to be converted
     * @return A <code>byte</code> array of each tone in series
     */
    public static byte[] toByteArray( Tone[] toneList )
    {
        byte[][] allVals = new byte[ toneList.length ][];
        int totalVals = 0;
        for ( int index = 0; index < toneList.length; index++ )
        {
            allVals[index] = toneList[index] != null ? toneList[index].toByteArray() : null;
            totalVals += allVals[index] == null ? 0 : allVals[index].length;
        }
        byte[] toBeReturned = new byte[ totalVals ];
        int index = 0;
        for ( int list = 0; list < allVals.length; list++ )
        {
            for ( int i = 0; allVals[list] != null && i < allVals[list].length; i++ )
                toBeReturned[index++] = allVals[list][i];
        }
        return toBeReturned;
    }
}