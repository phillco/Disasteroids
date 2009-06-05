/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package disasteroids.gui;

import disasteroids.gameobjects.Alien;
import disasteroids.gameobjects.Asteroid;
import disasteroids.gameobjects.BlackHole;
import disasteroids.gameobjects.BonusAsteroid;
import disasteroids.game.Game;
import disasteroids.gameobjects.GameObject;
import disasteroids.gameobjects.Ship;
import disasteroids.gameobjects.ShootingObject;
import disasteroids.gameobjects.Station;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;

/**
 *
 * @author Andy
 */
public class Radar {

    private static final int width = 400;
    private static final int height = 400;



    private Radar() {
        //don't go here :/
    }

    public static void draw(Graphics g, int cornerX, int cornerY, boolean onTop) {
        int shipX = (int) (Local.getLocalPlayer().getX());
        int shipY = (int) (Local.getLocalPlayer().getY());
        int topLeftX = cornerX - width - 100;
        int topLeftY = cornerY - height - 100;
        g.setColor(Color.black);
        if (onTop) {
            g.fillRect(topLeftX, topLeftY, width, height);
        }
        g.setColor(Color.white);
        g.drawRect(topLeftX, topLeftY, width, height);
        for (Asteroid a : Game.getInstance().getObjectManager().getAsteroids()) {
            drawAsteroid(g, a, shipX, shipY, topLeftX, topLeftY);
        }
        for (ShootingObject so : Game.getInstance().getObjectManager().getShootingObjects()) {
            drawObject(g, so, shipX, shipY, topLeftX, topLeftY);
        }
        
        for (BlackHole bh : Game.getInstance().getObjectManager().getBlackHoles())
            drawBlackHole(g, bh, shipX, shipY, topLeftX, topLeftY);

        for (Ship s : Game.getInstance().getObjectManager().getPlayers()) {
            drawShip(g, s, shipX, shipY, topLeftX, topLeftY);
        }
    }

    private static void drawAsteroid(Graphics g, Asteroid a, int shipX, int shipY, int topLeftX, int topLeftY)
    {
        g.setColor(a instanceof BonusAsteroid ? Color.GREEN : Color.LIGHT_GRAY);
        int centerX = (int) (((a.getX() - shipX) / Game.getInstance().GAME_WIDTH * width) + 3.5 * width) % width + topLeftX;
        int centerY = (int) (((a.getY() - shipY) / Game.getInstance().GAME_HEIGHT * height) + 3.5 * height) % height + topLeftY;
        g.drawOval(centerX - 2, centerY - 2, 4, 4);
    }

    private static void drawObject(Graphics g, ShootingObject so, int shipX, int shipY, int topLeftX, int topLeftY)
    {
        if (!(so instanceof GameObject)) {
            return;
        }
        GameObject go = (GameObject) so;
        int centerX = (int) (((go.getX() - shipX) / Game.getInstance().GAME_WIDTH * width) + 3.5 * width) % width + topLeftX;
        int centerY = (int) (((go.getY() - shipY) / Game.getInstance().GAME_HEIGHT * height) + 3.5 * height) % height + topLeftY;
        if (go instanceof Alien) {
            g.setColor(Color.GREEN);
            g.fillOval(centerX - 3, centerY - 3, 6, 6);
            return;
        }
        if (go instanceof Station) {
            g.setColor(Color.DARK_GRAY);
            g.fillRect(centerX - 3, centerY - 3, 6, 6);
            return;
        }
    }
    
    private static void drawBlackHole(Graphics g, BlackHole bh, int shipX, int shipY, int topLeftX, int topLeftY) {
        int centerX = (int) (((bh.getX() - shipX) / Game.getInstance().GAME_WIDTH * width) + 3.5 * width) % width + topLeftX;
        int centerY = (int) (((bh.getY() - shipY) / Game.getInstance().GAME_HEIGHT * height) + 3.5 * height) % height + topLeftY; 
        g.setColor(Color.DARK_GRAY.darker());
        g.fillOval(centerX - 8, centerY - 8, 16, 16 );
    }
    
    private static void drawShip(Graphics g, Ship s, int shipX, int shipY, int topLeftX, int topLeftY)
    {
        int centerX = (int) (((s.getX() - shipX) / Game.getInstance().GAME_WIDTH * width) + 3.5 * width) % width + topLeftX;
        int centerY = (int) (((s.getY() - shipY) / Game.getInstance().GAME_HEIGHT * height) + 3.5 * height) % height + topLeftY;
        g.setColor(s.getColor());
        Polygon p = new Polygon();
        p.addPoint( centerX - 2, centerY + 2);
        p.addPoint( centerX + 2, centerY + 2);
        p.addPoint( centerX, centerY - 5);
        g.drawPolygon(p);

    }
}



























