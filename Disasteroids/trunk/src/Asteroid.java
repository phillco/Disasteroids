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
import java.util.ListIterator;
import java.util.Random;

public class Asteroid
{       /**
         * The <code>Graphics</code> context where this <code>Asteroid</code> will be drawn 
         * @since Classic
         */
	protected Graphics g;
        
        /**
         * The <code>AsteroidManager</code> to which this <code>Asteroid</code> belongs
         * @since Classic
         */
	protected AsteroidManager environment;
        /**
         * The x and y coordinates of this <code>Asteroid</code>
         * @since Classic
         */
	protected double x,y;
        /**
         * The x and y components of this <code>Asteroid</code>'s velocity
         * @since Classic
         */
	protected double dx,dy;
        
        /**
         * The number of child <code>Asteroid</code>s this <code>Asteroid</code> has
         * @since Classic
         */
	protected int children;
        
        /**
         * The diameter of this <code>Asteroid</code>
         * @since Classic
         */
        protected int size;
        
        /**
         * Stores whether this <code>Asteroid</code> should be removed
         * @since Classic
         */
	protected boolean shouldRemove=false;
	
        /**
         * Constructs a new Asteroid from scratch
         * @param x The x coordinate
         * @param y The y coordinate
         * @param dx The x velocity
         * @param dy The y velocity (up is negative)
         * @param size The diameter
         * @param g The <code>Graphics</code> to be drawn in
         * @param environment The <code>AsteroidManager</code> responsible for <code>this</code>
         * @since Classic
         */
	public Asteroid(int x, int y, double dx, double dy, int size, Graphics g, AsteroidManager environment)
	{
		Random rand=RandNumGen.getAsteroidInstance();
		this.x=x;
		this.y=y;
		this.dx=dx;
		this.dy=dy;
		this.size=size;
                //Make sure the Asteroid is not too small
		if(size<25)
			size=25+rand.nextInt(25);
		this.g=g;
		this.environment=environment;
                //Regulates the maximum speed
		checkMovement();
	}
	
        /**
         * Constructs a new <code>Asteroid</code> from an existing <code>Asteroid</code>, as in by splitting
         * @param parent The parent <code>Asteroid</code> to <code>this</code>
         * @since Classic
         */
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
                //Regulates the maximum speed
		checkMovement();
	}
	
        /**
         * Draws <code>this</code> in its default <code>Graphics</code> context
         * @since Classic
         */
	protected void draw()
	{
		g.setColor(Color.white);
		g.fillOval((int)(x-size/2),(int)(y-size/2),size,size);
		g.setColor(Color.gray);
		g.drawOval((int)(x-size/2),(int)(y-size/2),size,size);

	}
	
        /**
         * Steps <code>this</code> through one timestep, then draws <code>this</code>
         * @since Classic
         */
	public void act()
	{
		if(children>1||size==5)
			shouldRemove=true;
		move();
		checkCollision();
		draw();
	}
	
        /**
         * Moves <code>this</code> for one timestep, and calculates Gravity towards ships
         * @since Classic
         */
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
		checkWrap();
	}
	
        /**
         * Makes sure that the ship remains on screen, and wraps around if necessary
         * @since Classic
         */
	private void checkWrap()
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
         * Called when the <code>Asteroid</code> is killed, as an indication to split into two new <code>Asteroid</code>s
         * @param killer The <code>Ship</code> which killed <code>this</code>
         * @since Classic
         */
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
        
        /**
         * Checks for a collision between <code>this</code> and <code>Ship</code>s and their <code>Misile</code>s
         * @since Classic
         */
	private void checkCollision()
	{
            //TODO: Clean up and optomize
            
                //Don't check if already dead
                if(shouldRemove)
                    return;
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
		//Same song, second verse
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
                //Check for collisions with the Ships
    public Asteroid( Asteroid parent )
    {
        parent.children++;
        if ( parent.children > 2 )
            this.size = 5;
        else
            this.size = parent.size / 2;
        Random rand = RandNumGen.getAsteroidInstance();
        this.x = parent.x;
        this.y = parent.y;
        this.dx = rand.nextDouble() * 2 - 1;
        this.dy = rand.nextDouble() * 2 - 1;
        this.environment = parent.environment;
        this.victim = parent.victim;
        checkMovement();
    }

	}
	
        /**
         * Makes sure that the <code>Asteroid</code> is moving fast enough
         * @since Classic
         */
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
	
        /**
         * Checks to see if <code>this</code> thinks it should be removed
         * @return If <code>this</code> should be removed
         * @since Classic
         */
	public boolean shouldRemove()
	{
		return shouldRemove;
	}
}
