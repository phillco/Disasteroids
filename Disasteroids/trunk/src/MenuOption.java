
import java.util.PriorityQueue;



/*
 * MenuOption.java
 * 
 * Phillip Cohen
 * Started on Nov 29, 2007, 12:15:14 PM.
 */

/**
 *
 * @author PC78064
 */
public enum MenuOption
{
    SINGLEPLAYER( "Singleplayer" ),
    MULTIHOST( "Multiplayer (host)" ),
    MULTIJOIN( "Multiplayer (join)" ),
    EXIT( "Exit" );
    
    private final String name;
    MenuOption( String s )
    {
        name = s;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
