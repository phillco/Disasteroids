/*
 * DISASTEROIDS
 * by Phillip Cohen and Andy Kooiman
 * 
 * APCS 1, 2006 to 2007, Period 3
 * Version - 1.0 Final (exam release)
 *
 * Run Running.class to start
 */
import java.util.ArrayList;

public class SpeakerThread extends Thread {
	
	// For each thread.
	// There's little reason to edit this.
	private ArrayList<Tone> tones = new ArrayList<Tone>();
	public void run(){
		InternalSpeaker is = new InternalSpeaker();
		for(int i = 0; i < tones.size(); i++) {
			is.play(tones.get(i).frequency, tones.get(i).duration); 	// Speaker handle
			 try {
				 Thread.sleep(tones.get(i).delayAfter);
		 	}
		 	catch(InterruptedException interruptedexception) {}
		}
	}
	
	public void addCue(Tone newTone) {
		tones.add(newTone);
	}
}