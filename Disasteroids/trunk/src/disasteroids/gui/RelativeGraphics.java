/*
 * DISASTEROIDS
 * RelativeGraphics.java
 */
package disasteroids.gui;

import disasteroids.game.Game;

/**
 * Utility class for drawing graphics around the localPlayer.
 * @author Phillip Cohen
 * @since January 5, 2008
 */
public abstract class RelativeGraphics
{
	public static int translateX( double x, double width )
	{
		double newX = Math.round( ( x - Local.getLocalPlayer().getX() + MainWindow.frame().getWidth() / 2 + 4 * Game.getInstance().GAME_WIDTH ) % Game.getInstance().GAME_WIDTH + MainWindow.frame().getRumbleX() - width / 2 );

		// Some large images (i.e, black holes) need to be wrapped.
		if ( newX + width >= Game.getInstance().GAME_WIDTH )
			newX -= Game.getInstance().GAME_HEIGHT;

		return (int) newX;
	}

	public static int translateX( double x )
	{
		return translateX( x, 0 );
	}

	public static int translateY( double y, double height )
	{
		double newY = (int) Math.round( ( y - Local.getLocalPlayer().getY() + MainWindow.frame().getHeight() / 2 + 4 * Game.getInstance().GAME_HEIGHT ) % Game.getInstance().GAME_HEIGHT + MainWindow.frame().getRumbleY() - height / 2 );

		// Some large images (i.e, black holes) need to be wrapped.
		if ( newY + height >= Game.getInstance().GAME_HEIGHT )
			newY -= Game.getInstance().GAME_HEIGHT;

		return (int) newY;
	}

	public static int translateY( double y )
	{
		return translateY( y, 0 );
	}

	/**
	 * Returns the location directly opposite the first player.
	 */
	public static int oppositeX()
	{
		return (int) ( ( Game.getInstance().getObjectManager().getPlayers().peek().getX() + Game.getInstance().GAME_WIDTH / 2 ) ) % Game.getInstance().GAME_WIDTH;
	}

	/**
	 * Rreturns the location directly opposite the first player.
	 */
	public static int oppositeY()
	{
		return (int) ( Game.getInstance().getObjectManager().getPlayers().peek().getY() + Game.getInstance().GAME_HEIGHT / 2 ) % Game.getInstance().GAME_HEIGHT;
	}
}
