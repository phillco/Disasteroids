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

    public static final SoundClip SHIP_HIT = shipHit();

    public static final SoundClip SHIP_DIE = shipDie();

    public static final SoundClip ASTEROID_DIE = asteroidDie();

    public static final SoundClip ALIEN_SHOOT = alienShoot();

    public static final SoundClip ALIEN_DIE = alienDie();

    public static final SoundClip STATION_SHOOT = stationShoot();

    public static final SoundClip STATION_DISABLED = stationDisabled();

    public static final SoundClip STATION_DIE = stationDie();

    public static final SoundClip GAME_OVER = gameOver();

    public static final SoundClip GET_BONUS = getBonus();

    public static final SoundClip BONUS_SPAWN = bonusSpawn();

    public static final SoundClip BONUS_FIZZLE = bonusFizzle();

    public static final SoundClip SNIPER_SHOOT = sniperShoot();

    /*
     * All of the computing methods
     */
    private static SoundClip bulletShoot()
    {
        return new SoundClip( new Tone( 500, 8, 0, 6 ).toByteArray() );
    }

    //:)
    private static SoundClip missileShoot()
    {
        return new SoundClip( new Tone( 200, 10 ).toByteArray() );
    }

    //:)
    private static SoundClip mineArm()
    {
        Tone[] temp = new Tone[10];
        int idx = 0;
        for ( int i = 40; i < 130; i += 20 )
            temp[idx++] = new Tone( i, 60, 0 );
        temp[idx++] = new Tone( 72, 200, 0 );
        return new LayeredSound.SoundClip( Tone.toByteArray( temp ) );
    }

    //:)
    private static SoundClip alienDie()
    {
        byte[] temp = new byte[3000];
        int phase = 0;
        int freqPhase = 0;
        int frequency = 440;
        for ( int index = 0; index < 3000; index++ )
        {
            temp[index] = (byte) ( 100 * Math.sin( phase ) );
            phase += frequency;
            frequency = (int) ( 440 + 100 * Math.tan( freqPhase / 20 ) );
            freqPhase++;
        }
        return new SoundClip( temp );
    }

    private static SoundClip alienShoot()
    {
        return new SoundClip( new Tone( 0, 0 ).toByteArray() );
    }

    //:)
    private static SoundClip asteroidDie()
    {
        return new SoundClip( new Tone( 50, 30 ).toByteArray() );
    }

    //:)
    private static SoundClip shipHit()
    {
        byte[] temp = new byte[2000];
        int index = 0;
        for ( int i = 1200; i > 800; i-- )
            temp[index++] = (byte) ( Math.sin( 80f * Math.pow( i, .8 ) / 8000.0 * 6.28 ) * 127 * 8000 / ( 8100 - i ) );
        return new SoundClip( temp );
    }

    private static SoundClip shipDie()
    {
        byte[] temp = new byte[8000];
        for ( int index = 0; index < 8000; index++ )
            temp[index] = (byte) ( Math.sin( 880f * Math.pow( index, .8 ) / 8000.0 * 6.28 ) * 127 * 8000 / ( 8100 - index ) );
        return new SoundClip( temp );
    }

    private static SoundClip stationDisabled()
    {
        Tone[] temp = new Tone[11];
        int idx = 0;
        for ( float i = 0; i <= 1; i += .1 )
            temp[idx++] = new Tone( (int) ( 220 - 70 * i ), 25 );
        return new SoundClip( Tone.toByteArray( temp ) );
    }

    private static SoundClip stationDie()
    {
        byte[] temp = new byte[8000];
        for ( int index = 0; index < 8000; index++ )
            temp[index] = (byte) ( Math.sin( 280f * Math.pow( index, .8 ) / 8000.0 * 6.28 ) * 127 * 8000 / ( 8100 - index ) );
        return new SoundClip( temp );
    }


    //:)
    private static SoundClip stationShoot()
    {
        Tone[] temp = { new Tone( 150, 20, 2 ), new Tone( 150, 20, 2 ), new Tone( 250, 30, 2 ) };
        return new SoundClip( Tone.toByteArray( temp ) );
    }

    //:)
    private static SoundClip berserkSound()
    {
        Tone[] temp = new Tone[15];
        int idx = 0;
        for ( int i = 0; i < 1400; i += 100 )
            temp[idx++] = new Tone( i, 15 );
        return new SoundClip( Tone.toByteArray( temp ) );
    }

    //:)
    private static SoundClip gameOver()
    {
        byte[] temp = new byte[28000];
        for ( int index = 0; index < 28000; index++ )
            temp[index] = (byte) ( Math.tan( 880f * Math.pow( index, .85 ) / 4000.0 * 6.28 ) * 127 * 28000 / ( 28100 - index ) );
        return new SoundClip( temp );
    }

    private static SoundClip getBonus()
    {
        byte[] temp = new byte[3000];
        int phase = 0;
        int freqPhase = 0;
        int frequency = 440;
        for ( int index = 0; index < 3000; index++ )
        {
            temp[index] = (byte) ( 100 * Math.sin( phase ) );
            phase += frequency / 4;
            frequency = (int) ( 484 + 200 * Math.cos( freqPhase / 500 ) );
            freqPhase++;
        }
        return new SoundClip( temp );
    }

    /**
     * 
     * @return
     * @since March 12, 2008
     */
    private static SoundClip bonusSpawn()
    {
        byte[] temp = new byte[3000];
        int phase = 0;
        int freqPhase = 5;
        int frequency = 440;
        for ( int index = 0; index < 1000; index++ )
        {
            temp[index] = (byte) ( 100 * Math.sin( phase ) );
            phase += frequency / 4;
            frequency = (int) ( 20 * Math.cos( 5000 / freqPhase ) );
            freqPhase++;
        }
        return new SoundClip( temp );
    }

    private static SoundClip bonusFizzle()
    {
        byte[] temp = new byte[3000];
        int frequency = 20000;
        double phase = 0;
        for ( int index = 0; index < 2000; index++ )
        {
            frequency *= .999;
            phase += frequency / 400.0;
            temp[index] = (byte) ( 20 * Math.sin( phase ) );
        }
        return new SoundClip( temp );
    }

    private static SoundClip sniperShoot()
    {
        byte[] temp = new byte[2000];// 1/4 second
        int frequency = 20000;
        double phase = 0;
        for ( int index = 0; index < 2000; index++ )
        {
            frequency *= .99;
            phase += frequency / 8000.0;
            temp[index] = (byte) ( 100 * Math.sin( phase ) );
        }
        return new SoundClip( temp );

    }
}
