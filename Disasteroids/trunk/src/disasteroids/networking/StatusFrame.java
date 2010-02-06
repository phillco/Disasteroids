/*
 * DISASTEROIDS
 * NetworkStatus.java
 */
package disasteroids.networking;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import disasteroids.gui.AsteroidsMenu;

/**
 * A nice status window for net games.
 * @author Phillip Cohen
 * @deprecated Currently not used and designed for an earlier, threadless design.
 * @since Dec 7, 2007
 */
@Deprecated
public class StatusFrame extends AsteroidsMenu
{
	public enum StatusState
	{
		BLANK, CLIENT, SERVER, ERROR

	}

	private StatusState currentState;

	private String IP;

	private String errorTitle;

	private String errorBody;

	public StatusFrame()
	{
		currentState = StatusState.BLANK;
		IP = "";
		errorBody = "";
		errorTitle = "";
	}

	public StatusFrame( StatusState state, String IP )
	{
		// Clear variables.
		this();

		this.currentState = state;
		this.IP = IP;

		// Display the form (hackish).
		paint( getGraphics() );
	}

	public void setError( String errorTitle, String errorBody )
	{
		this.errorBody = errorBody;
		this.errorTitle = errorTitle;
		this.currentState = StatusState.ERROR;

	}

	@Override
	public void paint( Graphics g )
	{
		// Draw background and shared elements.
		super.paint( g );

		// Some positioning.
		int y = 0;

		Font normal = new Font( "Tahoma", Font.PLAIN, 14 );
		Font accent = new Font( "Tahoma", Font.BOLD, 14 );

		y += 75;
		g.setColor( Color.BLACK );
		g.setFont( accent );

		switch ( currentState )
		{
			case CLIENT:
				g.drawString( "Connecting to server...", 60, 75 );
				g.setFont( normal );
				g.drawString( "Waiting for " + IP + ".", 60, 100 );
				break;
			case SERVER:
				g.drawString( "Hosting a server...", 60, 75 );
				g.setFont( normal );
				g.drawString( "Your IP is: " + IP + ".", 60, 100 );
				break;
			case ERROR:
				g.setColor( new Color( 64, 0, 0 ) );
				g.drawString( errorTitle, 60, 75 );
				g.setFont( normal );
				g.drawString( errorBody, 60, 100 );
				break;
		}
	}
}
