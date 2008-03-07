package disasteroids.sound;

import disasteroids.sound.LayeredSound.SoundClip;

/**
 *
 * @author Owner
 */
public class SoundLibrary
{
    /**
     * A short, quick click
     */
    public static final SoundClip BULLET_SHOOT = bulletShoot();

    public static final SoundClip MISSILE_SHOOT = missileShoot();

    public static final SoundClip MINE_ARM = mineArm();

    public static final SoundClip BERSERK = berserkSound();

    public static final SoundClip SHIP_DIE = shipDie();

    public static final SoundClip ASTEROID_DIE = asteroidDie();

    public static final SoundClip ALIEN_SHOOT = alienShoot();

    public static final SoundClip ALIEN_DIE = alienDie();

    public static final SoundClip STATION_SHOOT = stationShoot();

    public static final SoundClip STATION_DIE = stationDie();

    public static final SoundClip GAME_OVER = gameOver();

    /*
     * All of the computing methods
     */
    private static SoundClip bulletShoot()
    {
        return new SoundClip( new Tone( 500, 8 ).toByteArray() );
    }

    private static SoundClip missileShoot()
    {
        return new SoundClip( new Tone( 200, 10 ).toByteArray() );
    }

    private static SoundClip mineArm()
    {
        Tone[] temp = new Tone[10];
        int idx = 0;
        for ( int i = 40; i < 70; i += 10 )
            temp[idx++] = new Tone( i, 60, 0 );
        temp[idx++] = new Tone( 72, 200, 0 );
        return new LayeredSound.SoundClip( Tone.toByteArray( temp ) );
    }

    private static SoundClip alienDie()
    {
        return new SoundClip( new Tone( 0, 0 ).toByteArray() );
    }

    private static SoundClip alienShoot()
    {
        return new SoundClip( new Tone( 0, 0 ).toByteArray() );
    }

    private static SoundClip asteroidDie()
    {
        return new SoundClip( new Tone( 50, 30 ).toByteArray() );
    }

    private static SoundClip shipDie()
    {
        byte[] temp = new byte[8000];
        for ( int index = 0; index < 8000; index++ )
            temp[index] = (byte) ( Math.sin( 880f * Math.pow( index, .8 ) / 8000.0 * 6.28 ) * 127 * 8000 / ( 8100 - index ) );
        return new SoundClip( temp );
    }

    private static SoundClip stationDie()
    {
        return new SoundClip( new Tone( 0, 0 ).toByteArray() );
    }

    private static SoundClip stationShoot()
    {
        Tone[] temp = { new Tone( 150, 20, 2 ), new Tone( 150, 20, 2 ), new Tone( 250, 30, 2 ) };
        return new SoundClip( Tone.toByteArray( temp ) );
    }

    private static SoundClip berserkSound()
    {
        Tone[] temp = new Tone[15];
        int idx = 0;
        for ( int i = 0; i < 1400; i += 100 )
            temp[idx++] = new Tone( i, 15 );
        return new SoundClip( Tone.toByteArray( temp ) );
    }

    private static SoundClip gameOver()
    {
        Tone[] temp = new Tone[11];
        int idx = 0;
        for ( float i = 0; i <= 1; i += .1 )
            temp[idx++] = new Tone( (int) ( 220 - 70 * i ), 25 );
        return new SoundClip( Tone.toByteArray( temp ) );
    }
}
