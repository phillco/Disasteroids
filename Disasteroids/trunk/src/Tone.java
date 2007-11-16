/*
 * DISASTEROIDS
 * by Phillip Cohen and Andy Kooiman
 * 
 * APCS 1, 2006 to 2007, Period 3
 * Version - 1.0 Final (exam release)
 *
 * Run Running.class to start
 */
public class Tone {
	public int frequency;
	public int duration;
	public int delayAfter;
	
	public Tone(int freq, int dur) {
		this.frequency = freq;
		this.duration = dur;
		this.delayAfter = 0;
	}
	
	public Tone(int freq, int dur, int da) {
		this.frequency = freq;
		this.duration = dur;
		this.delayAfter = da;
	}
	
	
}