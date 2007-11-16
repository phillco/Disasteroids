/*
 * DISASTEROIDS
 * by Phillip Cohen and Andy Kooiman
 * 
 * APCS 1, 2006 to 2007, Period 3
 * Version - 1.0 Final (exam release)
 *
 * Run Running.class to start
 */
import java.io.*;
import javax.swing.JOptionPane;


public class ServerListenerThread extends Thread
{
	
	private BufferedReader in;
	
	public ServerListenerThread(BufferedReader in)
	{
		this.in=in;
	}
	
	
	
	public void run()
	{
		String fromServer;
		try{
			while((fromServer=in.readLine())!=null)
			{
//				System.out.println("RECEIVED: "+fromServer);
				process(fromServer);
			}
		}catch(IOException e){}
	}
	
	
	 /*******************************************************************************************************\
	 *Called by run to figure out what the command is intended to accomplish.								 *
	 *																										 *
	 *valid commands:   "k###" is interpreted as key strokes from the other computer						 *
	 *					"t###" is the other computer stating its timestep									 *
	 *					"Seed###" is a call to initialize the random number generator with ### as the seed	 *
	 *					"EXIT" will cause the program to exit												 *
	 *					"ng" will start a new game															 *
	 *					"HS!@#$" will set the high score name to !@#$										 *
	 *																										 *
	 \*******************************************************************************************************/
	public void process(String command)
	{
		if(command.charAt(0)=='k')//denotes that the command was a keystroke
		{try{
			keystroke(command.substring(1));
			}catch(NumberFormatException e){System.out.println(e);}
		}
		else if(command.charAt(0)=='t')
		{try{
			Running.environment().setOtherPlayerTimeStep(Integer.parseInt(command.substring(1)));
			}catch(NumberFormatException e){System.out.println(e);}
			catch(NullPointerException e){}
		}else if(command.indexOf("Seed")==0)
		{try{
			RandNumGen.init(Integer.parseInt(command.substring(4)));
		
			
			}catch(NumberFormatException e){System.out.println(e);}
		}
		else if(command.equalsIgnoreCase("exit"))
		{
			System.exit(0);
		}else if(command.equals("PING"))
		{
			AsteroidsServer.send("PONG");
			return;
		}else if(command.equals("ng"))
		{
			Running.environment().newGame();
		}else if(command.indexOf("HS")==0)
		{
			Running.environment().setHighScore(command.substring(2));
		}
	}
	
	private void keystroke(String command)
	{
		int comma=command.indexOf(",");
		int keyCommand=Integer.parseInt(command.substring(0,comma));
		int timeStep=Integer.parseInt(command.substring(comma+1));
		Running.environment().actionManager().add(new Action(2, keyCommand, timeStep));
	}
}