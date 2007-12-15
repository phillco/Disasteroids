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
import java.awt.Graphics;
//uses Random once

public class Misile
{
        /**
         * The <code>Color</code> to be drawn in
         * @since Classic
         */
	private Color myColor;
        
        /**
         * The x and y coordinates
         * @since Classic
         */
	private double x,y;
        
        /**
         * The x and y components of velocity
         * @since Classic
         */
	private double dx,dy;
        
        /**
         * The angle the <code>Misile</code> is pointing, but not necessarily the angle at which it is moving
         * @since Classic
         */
        private double angle;
        
        /**
         * How long <code>this</code> has been in existance
         * @since Classic
         */
	private int age;
        
        /**
         * The current stage of explosion
         * @since Classic
         */
	private int explodeCount=0;
        
        /**
         * Whether <code>this</code> is exploding currently or not
         * @since Classic
         */
	private boolean isExploding;
        
        /**
         * The current radius of <code>this</code>
         * @since Classic
         */
	private int radius;
        
        /**
         * The age at which <code>this></code> will automatically explode
         * @since Classic
         */
	private static int life=300;
        
        /**
         * Whether <code>this</code> will have a huge blast or not
         * @since Classic
         */
	private boolean hugeBlast;
        
        /**
         * The <code>MisileManager</code> to which <code>this</code> belongs
         * @since Classic
         */
	private MisileManager manager;
        
	/**
         * Whether <code>this</code> should be removed
         * @since Classic
         */
	private boolean needsRemoval = false;
	
        /**
         * Returns whether <code>this</code> should be removed
         * @return whither <code>this</code> should be removed
         * @since Classic
         */
	public boolean needsRemoval()
	{
		return needsRemoval;
	}
	
	/**
         * Constructs a new instance of <code>Misile</code>
         * @param x The x coordinate
         * @param y The y coordinate
         * @param angle The angle to be pointing
         * @param dx The x velocity
         * @param dy The y velocity (up is negative)
         * @param c The <code>Color to be drawn in
         * @since Classic
         */
	public Misile(MisileManager m, int x, int y, double angle,double dx, double dy, Color c)
	{
		manager=m;
		setData(x,y,angle,dx, dy, c);
	}
        
        /**
         * A utility method called by the constructor to intialize the object
         * @param x The x coordinate
         * @param y The y coordinate
         * @param angle The angle to be pointing
         * @param dx The x velocity
         * @param dy The y velocity (up is negative)
         * @param c The <code>Color to be drawn in
         * @since Classic
         */
	private void setData(int x, int y, double angle,double dx, double dy, Color c)
	{
		age=0;
		this.x=x;
		this.y=y;
		this.dx=manager.speed()*Math.cos(angle)+dx;
		this.dy=-manager.speed()*Math.sin(angle)+dy;
		this.angle=angle;
		radius=3;
		explodeCount=0;
		isExploding=false;
//		exists=true;
		myColor=c;
		hugeBlast=(RandNumGen.getMisileInstance().nextInt(manager.hugeBlastProb())<=1);
	}


	/**
         * Draws <code>this</code>
         * @param g The <code>Graphics</code> context in which to be drawn
         * @since Classic
         */
	public synchronized void draw(Graphics g)
	{
		g.setColor(myColor);
		g.drawLine((int)x,(int)y,(int)(x-10*Math.cos(angle)),(int)(y+10*Math.sin(angle)));
		g.fillOval((int)x-3,(int)y-3,6,6);
	}
	
        /**
         * Moves <code>this</code> according to its speed
         * @since Classic
         */
	public synchronized void move()
	{
		x+=dx;
		y+=dy;
	}
	
        /**
         * Steps <code>this</code> through one iteration and draws
         * @param g The <code>Graphics</code> context in which to be drawn
         * @since Classic
         */
	public synchronized void act(Graphics g)
	{
		age++;
		move();
		checkWrap();
		checkLeave();
		draw(g);
		explode(explodeCount,g);
	}
	
        /**
         * Checks to see if <code>this</code> has left the screen and adjusts accordingly
         * @since Classic
         */
	private synchronized void checkWrap()
	{
            // Wrap to stay inside the level.
            if ( x < 0 )
                x += Running.environment().getWidth() - 1;
            if ( y < 0 )
                y += Running.environment().getHeight() - 1;
            if ( x > Running.environment().getWidth() )
                x -= Running.environment().getWidth() - 1;
            if ( y > Running.environment().getHeight() )
                y -= Running.environment().getHeight() - 1;
	}
        
        /**
         * Checks the age of <code>this</code> and starts the explosion sequence if too old
         * @since Classic
         */
	private synchronized void checkLeave()
	{
		if(age>life)
			explode();
	}	
	
        /**
         * Initiates the explosion sequence
         * @since Classic
         */
	public synchronized void explode()
	{
		if(isExploding)
			return;
		if(RandNumGen.getMisileInstance().nextInt(manager.probPop())<=101)
			pop();
	//	dx=dy=0;
		explodeCount=1;
		isExploding=true;
	}
	
        /**
         * Steps <code>this</code> through the explosion sequence and draws
         * @param explodeCount The current stage of the explosion
         * @param g The <code>Graphics</code> context in which to be drawn
         * @since Classic
         */
	private synchronized void explode(int explodeCount, Graphics g)
	{
		switch (explodeCount)
		{
			case 0:
				return;
			case 1:	case 2:	case 3:	case 4:
				dx*=.8;
				dy*=.8;
				radius=3;
				if(explodeCount%2==0)
					g.setColor(myColor);
				else
					g.setColor(Color.yellow);
				g.fillOval((int)x-3,(int)y-3,2*radius,2*radius);
				this.explodeCount++;
				break;
			case 5:	case 6:	case 7:	case 8:
				dx*=.8;
				dy*=.8;
				if(explodeCount%2==0)
					g.setColor(myColor);
				else
					g.setColor(Color.yellow);
				radius=5;
				g.fillOval((int)x-5,(int)y-5,2*radius,2*radius);
				this.explodeCount++;
				break;
			case 9:	case 10: case 11:
				dx*=.8;
				dy*=.8;
				if(hugeBlast)
				{
					g.setColor(myColor);
					radius=manager.hugeBlastSize();
					g.fillOval((int)x-radius,(int)y-radius,2*radius,2*radius);
					this.explodeCount++;
				}
				else
				{
					radius=14;
					g.setColor(Color.yellow);
					g.fillOval((int)x-radius,(int)y-radius,2*radius,2*radius);
					this.explodeCount++;
				}
				break;
			default :
				needsRemoval = true;
				
		}
	}
	
	/**
         * Splits <code>this</code> into seveal new <code>Misile</code>s
         * @since Classic
         */
	private synchronized void pop()
	{
		for(double ang=0; ang<2*Math.PI; ang+=2*Math.PI/manager.popQuantity())
			manager.addMisile((int)x,(int)y,ang,0,0, myColor);
		needsRemoval = true;
	}
        
	/**
         * Gets the current x coordinate
         * @return The current x coordinate
         * @since Classic
         */
	public synchronized int getX()
	{return (int)x;}
	
        /**
         * Gets the current y coordinate
         * @return The current y coordinate
         * @since Classic
         */
	public synchronized int getY()
	{return (int)y;}
        
	/**
         * Gets the current radius
         * @return The current radius
         * @since Classic
         */
	public synchronized int getRadius()
	{return radius;}

        /**
         * Sets the current stage of explosion
         * @param count The new stage of explosion
         * @since Classic
         */
	public synchronized void setExplodeCount(int count)
	{explodeCount=count;}
        
	/**
         * Getter for the current stage of explosion
         * @return The current stage of explosion
         * @since Classic
         */
	public synchronized int getExplodeCount()
	{return explodeCount;}
}