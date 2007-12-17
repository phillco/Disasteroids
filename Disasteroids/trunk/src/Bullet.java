
import java.awt.Color;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Owner
 */
class Bullet implements Weapon {

    private double x, y;
    private double dx, dy;
    private BulletManager env;
    private Color myColor;
    private boolean needsRemoval=false;
    private int age=0;
    
    public Bullet(BulletManager env, int x, int y, double angle, double dx, double dy, Color col) {
        this.x=x;
        this.y=y;
        this.dx=dx+env.getSpeed()*Math.cos(angle);
        this.dy=dy-env.getSpeed()*Math.sin(angle);
        this.myColor=col;
        this.env=env;
    }

    public int getX() {
        return (int)x;
    }

    public int getY() {
        return (int)y;
    }

    public int getRadius() {
        return env.getRadius()+1;
    }

    public void explode() {
        needsRemoval=true;
    }

    public boolean needsRemoval() {
        return needsRemoval;
    }

    public void act() {
        age++;
        if(age>50)
            needsRemoval=true;
        move();
        draw();
    }

    private void draw() {
        Running.environment().fillCircle(myColor, (int)x, (int)y, env.getRadius());
    }

    private void move() {
        x+=dx;
        y+=dy;
    }

}
