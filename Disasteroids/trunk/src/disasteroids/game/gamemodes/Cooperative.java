/*
 * DISASTEROIDS | Game
 * Cooperative.java
 */
package disasteroids.game.gamemodes;

/**
 * Game mode where players work together to destroy enemies.
 */
public class Cooperative extends GameMode
{
    public Cooperative()
    {
        super( FLAG_PLAYERSEARNPOINTS );
    }

    public String getName()
    {
        return "Cooperative";
    }
}
