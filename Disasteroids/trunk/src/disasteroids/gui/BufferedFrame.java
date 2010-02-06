/*
 * DISASTEROIDS
 * BufferedFrame.java
 */
package disasteroids.gui;

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

/**
 * An awt frame with built in double-buffering.
 * @author Phillip Cohen
 * @since Dec 7, 2007
 */
public abstract class BufferedFrame extends Frame
{
	@Override
	public void update( Graphics g )
	{
		// Only update the changed area to boost performance.
		Rectangle clipBounds = g.getClipBounds();
		Image backgroundImage = createImage( clipBounds.width, clipBounds.height );
		Graphics backgroundGraphics = backgroundImage.getGraphics();

		// Clear the display.
		backgroundGraphics.setColor( getBackground() );
		backgroundGraphics.fillRect( 0, 0, clipBounds.width, clipBounds.height );
		backgroundGraphics.setColor( getForeground() );

		// Draw the graphics onto the buffer.
		backgroundGraphics.translate( -clipBounds.x, -clipBounds.y );
		paint( backgroundGraphics );

		// Flip the buffer to the screen.
		g.drawImage( backgroundImage, clipBounds.x, clipBounds.y, this );
	}
}
