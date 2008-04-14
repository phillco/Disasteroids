package disasteroids.sound;

import disasteroids.Running;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;

/*
 * DISASTEROIDS
 * MusicPlayer.java
 */



/**
 * MIDI player.
 * @author Andy Kooiman, others.
 */
public class MusicPlayer implements MetaEventListener
{
    public Sequencer sequencer = null;
    public Synthesizer synthesizer = null;
    public Sequence sequence = null;
    private String filepath;
    public int loopcount = 0;
    long start = 0;

    public MusicPlayer( String filepath )
    {
        this.filepath = filepath;
        load( filepath );
        loop();
    }

    public void load( String filepath )//Opens a midi when given a file path
    {
        try
        {
            FileInputStream fis = new FileInputStream( filepath );
            sequence = MidiSystem.getSequence( fis );
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequencer.setSequence( sequence );
        }
        catch ( IOException ioe )
        {
            System.out.println( "Error Reading: " + new File( filepath ).getName() + " (" + ioe + ")" );
        }
        catch ( InvalidMidiDataException imde )
        {
            Running.warning("|         Not a MIDI File         |" , imde);
        }
        catch ( MidiUnavailableException mue )
        {
            Running.warning("| MIDI Device is currently in use |", mue);
        }
    }

    public void close()
    {
        try
        {
            sequencer.stop();
            sequencer.close();
            sequencer.removeMetaEventListener( this );
        }
        catch ( NullPointerException npe )
        {
        }
        catch ( IllegalStateException ise )
        {
        }

        sequencer = null;
        synthesizer = null;//gets rid of the sequencer,sequence,and synthesizer
        sequence = null;
        start = 0;
        loopcount = 0;
    }
    
    /**
     * Returns whether the music is playing.
     */
    public boolean isPlaying()
    {
        return sequencer.isRunning();
    }

    public void play()
    {
        try
        {
            sequencer.start();//plays midi
        }
        catch ( NullPointerException npe )
        {
        }
        catch ( IllegalStateException ise )
        {
        }
    }

    public void stop()
    {
        try
        {
            sequencer.stop();
            sequencer.setMicrosecondPosition( 0 );//resets the sequencer
            sequencer.removeMetaEventListener( this );
            start = 0;
            loopcount = 0;
        }
        catch ( NullPointerException npe )
        {
        }
        catch ( IllegalStateException ise )
        {
        }
    }

    public void pause()
    {
        try
        {
            sequencer.stop();//paused if you play again it will start from the point it left off
        }
        catch ( NullPointerException npe )
        {
        }
        catch ( IllegalStateException ise )
        {
        }
    }

    public void loop()
    {
        try
        {
            sequencer.start();
            sequencer.addMetaEventListener( this );//sends a message when midi is done playing
        }
        catch ( NullPointerException npe )
        {
        }
    }

    public void meta( MetaMessage e )
    {
        if ( e.getType() == 47 )
        {
            load( filepath );
            loop();
        }
    }
}
