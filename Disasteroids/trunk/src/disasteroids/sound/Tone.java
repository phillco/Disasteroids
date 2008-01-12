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

    public Tone(int frequency, int duration)
    {
            this.frequency = frequency;
            this.duration = duration;
            this.delayAfter = 0;
    }

    public Tone(int frequency, int duration, int delayAfter)
    {
            this.frequency = frequency;
            this.duration = duration;
            this.delayAfter = delayAfter;
    }		
}