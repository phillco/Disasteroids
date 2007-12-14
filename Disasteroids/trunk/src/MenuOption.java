/*
 * DISASTEROIDS
 * MenuOption.java
 */

/**
 * An enum for the main menu selections.
 * @author Phillip Cohen
 * @since Nov 29, 2007
 */
public enum MenuOption
{
    SINGLEPLAYER( "Singleplayer", "-single" ),
    MULTIHOST( "Multiplayer (host)", "-host" ),
    MULTIJOIN( "Multiplayer (join)", "-join" ),
    EXIT( "Exit" );
    
    private final String name;
    private final String parameter;   

    MenuOption ( String name, String parameter )
    {
        this.name = name;
        this.parameter = parameter;
    }
        
    MenuOption( String s )
    {
        name = s;
        parameter = "";
    }
    
    /**
     * Returns the command-line code used to select this option.
     */
    public String getParameter ()
    {
        return parameter;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
