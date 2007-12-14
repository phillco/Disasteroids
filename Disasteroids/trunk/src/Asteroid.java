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
import java.util.ListIterator;
import java.util.LinkedList;
import java.util.Random;


public class Asteroid
{
	protected Graphics g;
	protected AsteroidManager environment;
	protected double x,y;
	protected double dx,dy;
	protected int children,size;
	protected boolean shouldRemove=false;
	
	public Asteroid(int x, int y, double dx, double dy, int size, Graphics g, AsteroidManager environment)
	{
		Random rand=RandNumGen.getAsteroidInstance();
		this.x=x;
		this.y=y;
		this.dx=dx;
		this.dy=dy;
		this.size=size;
		if(size<25)
			size=25+rand.nextInt(25);
		this.g=g;
		this.environment=environment;
		checkMovement();
	}
	
	public Asteroid(Asteroid parent)
	{
		parent.children++;
		if(parent.children>2)
			this.size=5;
		else
			this.size=parent.size/2;
		Random rand=RandNumGen.getAsteroidInstance();
		this.x=parent.x;
		this.y=parent.y;
		this.dx=rand.nextDouble()*2-1;
		this.dy=rand.nextDouble()*2-1;
		this.g=parent.g;
		this.environment=parent.environment;
		checkMovement();
	}
	
	protected void draw()
	{
		g.setColor(Color.white);
		g.fillOval((int)(x-size/2),(int)(y-size/2),size,size);
		g.setColor(Color.gray);
		g.drawOval((int)(x-size/2),(int)(y-size/2),size,size);

	}
	
	public void act()
	{
		if(children>1||size==5)
			shouldRemove=true;
		move();
		checkCollision();
		draw();
	}
	
	private void move()
	{
		Ship target=AsteroidsFrame.getShip();
		if(target!=null)
		{
			dx+=(-x+target.getX())*(double)(AsteroidsFrame.getLevel())/500000.0;
			dy+=(-y+target.getY())*(double)(AsteroidsFrame.getLevel())/500000.0;
		}
		target = AsteroidsFrame.getShip2();
		if(target!=null)
		{
			dx+=(-x+target.getX())*(double)(AsteroidsFrame.getLevel())/500000.0;
			dy+=(-y+target.getY())*(double)(AsteroidsFrame.getLevel())/500000.0;
		}
		
		x+=dx;
		y+=dy;
		checkBounce();
	}
	
	private void checkBounce()
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
	
	protected void split(Ship killer)
	{
		if(children>2)
		{
			shouldRemove=true;
			return;
		}
		killer.increaseScore(size);
		Running.environment().writeOnBackground("+"+String.valueOf(size), (int) x, (int) y, killer.getColor().darker());
		if(size<25)
			shouldRemove=true;
		else
		{
			environment.add(new Asteroid(this));
			environment.add(new Asteroid(this));
			shouldRemove=true;
		}
	}
	private void checkCollision()
	{
            // Get the misiles of the first ship
		ListIterator<Misile> iter = AsteroidsFrame.getShip().getMisileManager().getMisiles().listIterator();
		// Loop through all misiles
		while(iter.hasNext())
		{
			Misile m = iter.next();
			if (Math.pow(x-m.getX(),2)+Math.pow(y-this.size/2-m.getY(),2)<Math.pow(size+m.getRadius(),2))//hit by misile
				{	
					Sound.bloomph();
					m.explode();
					split(AsteroidsFrame.getShip());
					return;
				}
		}
		
		if(AsteroidsFrame.getShip2()!=null)
		{
		
		ListIterator<Misile> iter2 = AsteroidsFrame.getShip2().getMisileManager().getMisiles().listIterator();
			while(iter2.hasNext())
			{
				Misile m = iter2.next();
				if (Math.pow(x-m.getX(),2)+Math.pow(y-this.size/2-m.getY(),2)<Math.pow(size+m.getRadius(),2))//hit by misile
					{
						Sound.bloomph();
						m.explode();
						split(AsteroidsFrame.getShip2());
						return;
					}
			}
		}

		if(AsteroidsFrame.getShip().livesLeft()>=0)
			if(Math.sqrt(Math.pow(x-AsteroidsFrame.getShip().getX()+Ship.RADIUS,2)+(Math.pow(y-AsteroidsFrame.getShip().getY()+Ship.RADIUS,2)))<size+Ship.RADIUS)//hit by ship
			{
				if(AsteroidsFrame.getShip().looseLife())
					split(AsteroidsFrame.getShip());
			}
		if(AsteroidsFrame.getShip2()!=null)
		if(AsteroidsFrame.getShip2().livesLeft()>=0)
			if(Math.sqrt(Math.pow(x-AsteroidsFrame.getShip2().getX()+Ship.RADIUS,2)+(Math.pow(y-AsteroidsFrame.getShip2().getY()+Ship.RADIUS,2)))<size+10)//hit by ship2
			{
				if(AsteroidsFrame.getShip2().looseLife())
					split(AsteroidsFrame.getShip2());
			}

	}
	
	private void checkMovement()
	{
		if (Math.abs(dx)<.5)
			if(dx<0)
				dx-=1;
			else
				dx+=1;
		if (Math.abs(dy)<.5)
			if(dy<0)
				dy-=1;
			else
				dy+=1;
	}
	
	public boolean shouldRemove()
	{
		return shouldRemove;
	}
}
