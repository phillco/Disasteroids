/*
 * DISASTEROIDS
 * by Phillip Cohen and Andy Kooiman
 * 
 * APCS 1, 2006 to 2007, Period 3
 * Version - 1.0 Final (exam release)
 *
 * Run Running.class to start
 */
public class Driver_Sound {
	
	/*==============================
	 * Static class for makin' sound
	 *============================*/
	 
	static void beep() {
		SpeakerThread s  = new SpeakerThread();
		s.addCue(new Tone(440, 100));
		new Thread(s).start();
	}
	static void click() {
		SpeakerThread s  = new SpeakerThread();
		s.addCue(new Tone(200, 25));
		new Thread(s).start();
	}
	static void play(int freq, int dur) {
		SpeakerThread s  = new SpeakerThread();
		s.addCue(new Tone(freq, dur));
		new Thread(s).start();
	}
	
	// Custom Disasteroids sounds
	static void bloomph() {
		SpeakerThread s  = new SpeakerThread();
		s.addCue(new Tone(50, 400));
		new Thread(s).start();
	}
	static void bleargh() {
		SpeakerThread s  = new SpeakerThread();
		s.addCue(new Tone(600, 20,5));
		s.addCue(new Tone(600, 20,5));
		s.addCue(new Tone(600, 20,5));
		s.addCue(new Tone(600, 20,5));
		
		
		for(int i = 500; i > 0; i-=10)
			s.addCue(new Tone(i, 5));
		for(int i = 0; i < 200; i+=20)
			s.addCue(new Tone(i, 5));
			
		new Thread(s).start();	
	}
	
	static void wheeeargh() {
		SpeakerThread s  = new SpeakerThread();
		for(float sat=0; sat<=1; sat+=.1)
			s.addCue(new Tone((int)(220-70*sat), 25));
		new Thread(s).start();
	}

	static void kablooie() {
		SpeakerThread s  = new SpeakerThread();
		for(int i = 0; i < 1400; i += 100)
			s.addCue(new Tone(i, 20));
		new Thread(s).start();
	}
}