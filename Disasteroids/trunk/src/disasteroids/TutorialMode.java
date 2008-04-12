/**
 * DISASTEROIDS
 * TutorialMode.java
 */
package disasteroids;

import disasteroids.gui.AsteroidsFrame;
import disasteroids.gui.Local;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.swing.JOptionPane;

/**
 * A short tutorial for players.
 * @author Phillip Cohen
 */
public class TutorialMode implements GameMode
{
    int stage = 0, counter = 0;

    double playerStartX = 0, playerStartY = 0;

    public void act()
    {
        if ( Local.isStuffNull() )
            return;

        ++counter;
        String text = "";

        if ( stage == 0 && counter > 0 )
        {
            Font title = new Font( "Tahoma", Font.BOLD, 24 );
            text = "Welcome to Disasteroids!";

            Local.getStarBackground().writeOnBackground( text, (int) Local.getLocalPlayer().getX(),
                                                         (int) AsteroidsFrame.frame().localPlayer().getY() - 800,
                                                         7, 230, Color.white, title );

            playerStartX = Local.getLocalPlayer().getX();
            playerStartY = Local.getLocalPlayer().getY();

            ++stage;
            counter = 0;
        }

        Font textFont = new Font( "Tahoma", Font.BOLD, 12 );
        if ( stage == 1 && counter > 340 )
        {
            drawAbovePlayer("You're the player in the center of the screen.", textFont, 0);
            ++stage;
            counter = 0;
        }
        
        if ( stage == 2 && counter > 80 )
        {
            drawAbovePlayer("To move, use the arrow keys.", textFont, 0);
            ++stage;
            counter = -150;
        }

        if ( stage == 3 && counter > 300 )
        {
            if ( Math.pow( Local.getLocalPlayer().getX() - playerStartX, 2 ) + Math.pow( Local.getLocalPlayer().getY() - playerStartY, 2 ) > Math.pow( 200, 2 ) )
            {
                drawAbovePlayer( "Good!", textFont, 0 );
                ++stage;
                counter = 0;
            }
            else
            {
                drawAbovePlayer( "Try it out! Use the arrow keys to move a bit.", textFont, 0 );
                counter = 0;
            }
        }
        
        if (stage == 4 && counter > 130 )
        {
            drawAbovePlayer("Tutorial ends here. Sorry.", textFont, 0);
            stage = Integer.MAX_VALUE;
        }
    }

    private void drawAbovePlayer( String str, Font f, double dy )
    {
        Local.getStarBackground().writeOnBackground( str, (int) Local.getLocalPlayer().getX(),
                                                     (int) AsteroidsFrame.frame().localPlayer().getY() - 40,
                                                     0, 50, Color.white, f );
    }

    public void draw( Graphics g )
    {

    }

    public void flatten( DataOutputStream stream ) throws IOException
    {
        throw new UnsupportedOperationException( "Tutorials can't be used in net games." );
    }

    public void optionsKey()
    {
        try
        {
            stage = Integer.parseInt( JOptionPane.showInputDialog( null, "Enter the section to skip to.", stage ) );
            counter = Integer.MAX_VALUE / 2;
        }
        catch ( NumberFormatException e )
        {
            // Do nothing with incorrect or cancelled input.
            Running.log( "Invalid section command.", 800 );
        }
    }

    public int id()
    {
        throw new UnsupportedOperationException( "Tutorials can't be used in net games." );
    }
}
