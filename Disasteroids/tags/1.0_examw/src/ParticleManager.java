/*
 * DISASTEROIDS
 * by Phillip Cohen and Andy Kooiman
 * 
 * APCS 1, 2006 to 2007, Period 3
 * Version - 1.0 Final (exam release)
 *
 * Run Running.class to start
 */

import java.awt.*;
import java.util.ArrayList;
public class ParticleManager  {
    	private static ArrayList<Particle> parts = new ArrayList<Particle>();
    	public static void createParticle(double x, double y, double size, Color c , double dx, double dy, double lifemax, double lifemin) {
    		parts.add(new Particle(x, y, size, c, dx, dy, lifemax, lifemin));
    	}
    	
    	public static void drawParticles(Graphics g) {
    		
		if(parts.size() > 0) {
			for(int i = 0; i < parts.size(); i += 0) {
				Particle p = parts.get(i);
				if(p.act() == true)
					parts.remove(i);
				else {
					g.setColor(p.color);
					g.fillOval((int)p.x+10,(int)p.y+10,(int)p.size,(int)p.size);
					i++;
				}
			}
		
    	}
    	}
}