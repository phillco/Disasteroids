/*
 * DISASTEROIDS
 * MenuOption.java
 */
package disasteroids.gui;

/**
 * An enum for the main menu selections.
 * @author Phillip Cohen
 * @since November 29, 2007
 */
public enum MenuOption
{
    SINGLEPLAYER( "Singleplayer", "-single" ),
    TUTORIAL( "Tutorial", "-tutorial" ),
    LOAD( "Load game", "-load" ),
    START_SERVER( "Start server", "-host" ),
    CONNECT( "Join server", "-join" ),
    OPTIONS( "Options", "-options" ),
    EXIT( "Exit" );

    private final String name;

    private final String parameter;

    MenuOption( String name, String parameter )
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
    public String getParameter()
    {
        return parameter;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
