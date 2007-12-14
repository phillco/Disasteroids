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
	public double life;
	public double life_max;
	public Color color;
	public double x;
	public double y;
	public double size;
	public double dx;
	public double dy;
	
	//[AK]The Color was fading too quickly to be seen, so i changed the fading mechanism
	public double[] rgb=new double[3];
	
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
	// Returns whether this needs to be removed
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