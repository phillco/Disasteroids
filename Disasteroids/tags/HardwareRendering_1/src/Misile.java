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
import java.util.Scanner;
//uses Random once

public class Misile
{
	private Color myColor;
	private double x,y;
	private double dx,dy,angle;
	private int age;
	private int explodeCount=0;
	private boolean isExploding;
	private int radius;
	private static int life=300;//how long before it explodes on its own
	private boolean hugeBlast;
	private MisileManager manager;
	
	private boolean needsRemoval = false;
	
	public boolean needsRemoval()
	{
		return needsRemoval;
	}
	
	
	public Misile(MisileManager m, int x, int y, double angle,double dx, double dy, Color c)
	{
		manager=m;
		setData(x,y,angle,dx, dy, c);
	}

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


	
	public synchronized void draw(Graphics g)
	{
		g.setColor(myColor);
		g.drawLine((int)x,(int)y,(int)(x-10*Math.cos(angle)),(int)(y+10*Math.sin(angle)));
		g.fillOval((int)x-3,(int)y-3,6,6);
	}
	
	public synchronized void move()
	{
		x+=dx;
		y+=dy;
	}
	
	public synchronized void act(Graphics g)
	{
		age++;
		move();
		checkBounce();
		checkLeave();
		draw(g);
		explode(explodeCount,g);
	}
	
	private synchronized void checkBounce()
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
        
	private synchronized void checkLeave()
	{
		if(age>life)
			explode();
	}	
	
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
	
	
	private synchronized void pop()
	{
		for(double angle=0; angle<2*Math.PI; angle+=2*Math.PI/manager.popQuantity())
			manager.addMisile((int)x,(int)y,angle,0,0, myColor);
		needsRemoval = true;
	}
	
	public synchronized int getX()
	{return (int)x;}
	
	public synchronized int getY()
	{return (int)y;}
	
	public synchronized int getRadius()
	{return radius;}

	public synchronized void setExplodeCount(int count)
	{explodeCount=count;}
	
	public synchronized int getExplodeCount()
	{return explodeCount;}
}