/*
 * DISASTEROIDS
 * by Phillip Cohen and Andy Kooiman
 * 
 * APCS 1, 2006 to 2007, Period 3
 * Version - 1.0 Final (exam release)
 *
 * Run Running.class to start
 */
import javax.sound.midi.*; 
import java.io.*;
 
public class MusicPlayer implements MetaEventListener
{
	public Sequencer sequencer = null;
	public Synthesizer synthesizer = null;
	public Sequence sequence = null;
 	private String filepath;
	public int loopcount = 0;
 
	long start = 0;
	boolean allways = true;
 
 
	public MusicPlayer(String filepath)
	{
		this.filepath=filepath;
		load(filepath);
		loop();
	}
	
	public void load(String filepath)//Opens a midi when given a file path
	{
		try
		{
			FileInputStream fis = new FileInputStream(filepath);
			sequence = MidiSystem.getSequence(fis);
			sequencer = MidiSystem.getSequencer();
			sequencer.open();
			sequencer.setSequence(sequence);
		}
		catch(IOException ioe)
		{
			System.out.println("Error Reading: "+new File(filepath).getName() + " (" + ioe + ")");
		}
		catch(InvalidMidiDataException imde)
		{
			System.out.println("\n|         Not a MIDI File         |\n");
		}
		catch(MidiUnavailableException mue)
		{
			System.out.println("\n| MIDI Device is currently in use |\n");
		}
	}
 
	public void close()
	{
		try
		{
			sequencer.stop();
			sequencer.close();
			sequencer.removeMetaEventListener(this);
		}
		catch(NullPointerException npe){}
		catch(IllegalStateException ise){}
 
		sequencer = null;
		synthesizer = null;//gets rid of the sequencer,sequence,and synthesizer
		sequence = null;
		start = 0;
		loopcount = 0;
	}
 
	public void play()
	{
		try
		{
			sequencer.start();//plays midi
		}
		catch(NullPointerException npe){}
		catch(IllegalStateException ise){}
	}
 
	public void stop()
	{
		try
		{
			sequencer.stop();
			sequencer.setMicrosecondPosition(0);//resets the sequencer
			sequencer.removeMetaEventListener(this);
			start = 0;
			loopcount = 0;
			allways = true;
		}
		catch(NullPointerException npe){}
		catch(IllegalStateException ise){}
	}
 
	public void pause()
	{
		try
		{
			sequencer.stop();//paused if you play again it will start from the point it left off
		}
		catch(NullPointerException npe){}
		catch(IllegalStateException ise){}
	}
 
	public void loop()
	{
		try
		{
			sequencer.start();
			sequencer.addMetaEventListener(this);//sends a message when midi is done playing
		}
		catch(NullPointerException npe){}
	}
 
	public void meta(MetaMessage e)
	{ 
		if(e.getType()==47)
		{
			load(filepath);
			loop();
		}
	}
}