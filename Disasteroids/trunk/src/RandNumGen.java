/*
 * DISASTEROIDS
 * by Phillip Cohen and Andy Kooiman
 * 
 * APCS 1, 2006 to 2007, Period 3
 * Version - 1.0 Final (exam release)
 *
 * Run Running.class to start
 */
import java.util.Random;


public class RandNumGen
{
	private static Random numGen;
	private static Random particleGen;
	private static Random starGen;
	private static Random MissileGen;
	private static Random asteroidGen;
	private static boolean initialized=false;
	public  static int seed;
	
	public static void init(int iSeed)
	{
		seed = iSeed;
		numGen=new Random(seed);
		particleGen=new Random(numGen.nextInt(10000));
		starGen=new Random(numGen.nextInt(10000));
		MissileGen=new Random(numGen.nextInt(10000));
		asteroidGen=new Random(numGen.nextInt(10000));
		initialized=true;
	}
	
	public static Random getParticleInstance()
	{
		return particleGen;
	}
	
	public static boolean isInitialized()
	{
		return initialized;
	}
	
	public static Random getStarInstance()
	{
		return starGen;
	}
	
	public static Random getMissileInstance()
	{
		return MissileGen;
	}
	
	public static Random getAsteroidInstance()
	{
		return asteroidGen;
	}
}