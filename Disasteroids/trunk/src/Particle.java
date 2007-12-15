/*
 * DISASTEROIDS
 * by Phillip Cohen and Andy Kooiman
 * 
 * APCS 1, 2006 to 2007, Period 3
 * Version - 1.0 Final (exam release)
 *
 * Run Running.class to start
 */
import java.awt.Color;
//uses Random

public class Particle {
        /**
         * The time until this <code>Particle</code> will be removed
         * @since Classic
         */
	public double life;
        
        /**
         * The original life of this <code>Particle</code>
         * @since Classic
         */
	public double life_max;
        
        /**
         * The current <code>Color</code> of this <code>Particle</code>
         * @since Classic
         */
	public Color color;
        
        /**
         * The coordinates of this <code>Particle</code>
         * @since Classic
         */
	public double x,y;
        
        /**
         * The diameter of this <code>Particle</code>
         * @since Classic
         */
	public double size;
        
        /**
         * The x and y components of velocity
         * @since Classic
         */
	public double dx,dy;
        
        /**
         * The <code>Color</code> represented as an array of red, green, and blue, each as a double between 0 and 1.0
         * @since Classic
         */
	public double[] rgb=new double[3];
	
        /**
         * Creates a new <code>Particle</code>
         * @param x The x coordinate
         * @param y The y coordinate
         * @param size The diameter
         * @param c The <code>Color</code>
         * @param speed The speed
         * @param angle The direction
         * @param lifemax The longest this <code>Particle</code> can live
         * @param lifemin The least amount of time this <code>Particle</code> can live
         * @since Classic
         */
	public Particle(double x, double y, double size, Color c, double speed, double angle, double lifemax, double lifemin) {
		life = (RandNumGen.getParticleInstance().nextDouble())*lifemax + lifemin;
		this.life_max = life;
		this.x = x;
		this.y = y;
		this.size = size;
		this.color = c;
		this.dx = speed*Math.cos(angle);
		this.dy = -speed*Math.sin(angle);
		//rgb={c.getRed(), c.getGreen(), c.getBlue()};
		
		rgb[0]=c.getRed();
		rgb[1]=c.getGreen();
		rgb[2]=c.getBlue();
	}

        /**
         * Iterates through one time step:  Moves, ages, slows down, and fades
         * @return whether this needs to be removed
         * @since Classic
         */
	public boolean act() {
		life--;
		x += dx;
		y += dy;
		dx *= 0.998;
		dy *= 0.998;
		
		if(life <= 1)
			return true;		
			
		double fadePct = (double)life/life_max; 
//		color = new Color((double)(color.getRed() * fadePct), (double)(color.getGreen() * fadePct), (double)(color.getBlue() * fadePct));
		color = new Color((int)(rgb[0] * fadePct), (int)(rgb[1] * fadePct), (int)(rgb[2] * fadePct));

		return false;
	}
}