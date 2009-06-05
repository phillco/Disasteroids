/*
 * DISASTEROIDS | Networking
 * Constants.java
 */
package disasteroids.networking;

import disasteroids.*;
import disasteroids.weapons.*;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Storage class for network constants.
 * @author Phillip Cohen
 * @since January 13, 2008
 */
public class Constants
{
    /**
     * The version of our net code protocol. Bump to ensure older clients don't connect and cause havoc.
     * @since import disasteroids.weapons.Weapon;
    April 11, 2008
     */
    public static final int NETCODE_VERSION = 6;

    /**
     * The default port that the server runs on.
     * @since December 29, 2007
     */
    public static final int DEFAULT_PORT = 1024;

    /**
     * Time (in seconds) that must elapse before a machine is considered to have timed out.
     * @since January 13, 2008
     */
    public static final int TIMEOUT_TIME = 5;

    /**
     * Time (in seconds) between intervalLogic() calls.
     * @since January 13, 2008
     */
    public static final int INTERVAL_TIME = TIMEOUT_TIME / 5;

    /**
     * The max size of a packet (bytes).
     * @since January 22, 2008
     */
    public static final int MAX_PACKET_SIZE = 1024;

    /**
     * Size of the overhead for a packet in a series.
     * @since January 22, 2008
     */
    public static final int MULTIPACKET_HEADER_SIZE = ( 4 * Integer.SIZE / 8 );

    /**
     * Remaining size for data for packet in a series.
     * @since January 22, 2008
     */
    public static final int MULTIPACKET_DATA_SIZE = MAX_PACKET_SIZE - MULTIPACKET_HEADER_SIZE;

    public static enum LevelTID
    {
        CLASSIC( Classic.class ), WAVE( WaveGameplay.class ), EMPTY( EmptyLevel.class );

        final Class myClass;

        private LevelTID( Class myClass )
        {
            this.myClass = myClass;
        }
    }

    public static enum GameObjectTIDs
    {
        ALIEN, ASTEROID, BONUS_ASTEROID, BLACK_HOLE, BONUS, SHIP, STATION;

    }

    /**
     * Weapon units that need to be synched.
     */
    public static enum WeaponUnitTID
    {
        MISSILE, BIGNUKE, BIGNUKE_CHARGE;

    }

    public static WeaponUnitTID parseWeaponUnit( Unit u )
    {
        if ( u instanceof Missile )
            return WeaponUnitTID.MISSILE;
        else if ( u instanceof BigNuke )
            return WeaponUnitTID.BIGNUKE;
        else if ( u instanceof BigNukeCharge )
            return WeaponUnitTID.BIGNUKE_CHARGE;
        else
            return null;
    }

    public static Unit parseWeaponUnit( int type, Weapon w, DataInputStream stream ) throws IOException
    {
        switch ( WeaponUnitTID.values()[type] )
        {
            case MISSILE:
                return new Missile( stream, (MissileManager) w );
            case BIGNUKE:
                return new BigNuke( stream, (BigNukeLauncher) w );
            case BIGNUKE_CHARGE:
                return new BigNukeCharge( stream, (BigNukeLauncher) w );
        }

        return null;
    }

    public static int parseLevel( Level m )
    {
        if ( m instanceof Classic )
            return LevelTID.CLASSIC.ordinal();
        else if ( m instanceof WaveGameplay )
            return LevelTID.WAVE.ordinal();
        else if ( m instanceof EmptyLevel )
            return LevelTID.EMPTY.ordinal();
        else
            throw new IllegalArgumentException( "Unknown level: " + m + "." );
    }

    public static Level parseLevel( int type, DataInputStream stream ) throws IOException
    {
        switch ( LevelTID.values()[type] )
        {
            case CLASSIC:
                return new Classic( stream );
            case WAVE:
                return new WaveGameplay( stream );
            case EMPTY:
                return new EmptyLevel( stream );
            default:
                throw new IllegalArgumentException( "Unknown level: " + type + "." );
        }
    }

    public static int parseGameObject( GameObject o )
    {
        if ( o instanceof Alien )
            return GameObjectTIDs.ALIEN.ordinal();
        else if ( o instanceof BonusAsteroid )
            return GameObjectTIDs.BONUS_ASTEROID.ordinal();
        else if ( o instanceof Asteroid )
            return GameObjectTIDs.ASTEROID.ordinal();
        else if ( o instanceof Bonus )
            return GameObjectTIDs.BONUS.ordinal();
        else if ( o instanceof Station )
            return GameObjectTIDs.STATION.ordinal();
        else if ( o instanceof Ship )
            return GameObjectTIDs.SHIP.ordinal();
        else if ( o instanceof BlackHole )
            return GameObjectTIDs.BLACK_HOLE.ordinal();
        else
            throw new IllegalArgumentException( "Unknown game object: " + o + "." );
    }
}
