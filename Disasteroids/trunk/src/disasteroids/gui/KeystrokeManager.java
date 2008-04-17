/*
 * DISASTEROIDS
 * KeystrokeManager.java
 */

package disasteroids.gui;

import disasteroids.Action;
import disasteroids.Game;
import disasteroids.GameObject;
import disasteroids.Running;
import disasteroids.Station;
import disasteroids.networking.Client;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;

/**
 *
 * @author Andy
 */
public class KeystrokeManager implements KeyListener{

    public enum ActionType{
    UNKNOWN, QUIT, TOGGLE_MUSIC, TOGGLE_SOUND, TOGGLE_FULL_SCREEN,
    SET_EASTER_EGG, WARP, TOGGLE_ANTIALIASING, TOGGLE_SCOREBOARD, TOGGLE_TRACKER,
    START_SHOOT, STOP_SHOOT, LEFT, RIGHT, FORWARDS, BACKWARDS, UN_LEFT, UN_RIGHT,
    UN_FORWARDS, UN_BACKWARDS, BRAKE, UN_BRAKE, BERSERK, STRAFE_RIGHT,
    STRAFE_LEFT, EXPLODE_ALL, ROTATE_WEAPONS, SET_WEAPON_1, SET_WEAPON_2,
    SET_WEAPON_3, SET_WEAPON_4, SET_WEAPON_5, SET_WEAPON_6, SET_WEAPON_7,
    SET_WEAPON_8, SET_WEAPON_9, PAUSE, SAVE, LOAD};
    
    private HashMap<Integer,ActionType> keyboardLayout = new HashMap<Integer,ActionType>();
    
    public static KeystrokeManager instance = new KeystrokeManager();
    
    
    public KeystrokeManager()
    {
        keyboardLayout.put(KeyEvent.VK_ESCAPE, ActionType.QUIT);
        keyboardLayout.put(KeyEvent.VK_M, ActionType.TOGGLE_MUSIC);
        keyboardLayout.put(KeyEvent.VK_S, ActionType.TOGGLE_SOUND);
        keyboardLayout.put(KeyEvent.VK_F4, ActionType.TOGGLE_FULL_SCREEN);
        keyboardLayout.put(KeyEvent.VK_SCROLL_LOCK, ActionType.SET_EASTER_EGG);
        keyboardLayout.put(KeyEvent.VK_W, ActionType.WARP);
        keyboardLayout.put(KeyEvent.VK_A, ActionType.TOGGLE_ANTIALIASING); 
        keyboardLayout.put(KeyEvent.VK_BACK_SLASH, ActionType.TOGGLE_SCOREBOARD);
        keyboardLayout.put(KeyEvent.VK_F12, ActionType.TOGGLE_TRACKER);
        keyboardLayout.put(KeyEvent.VK_SPACE, ActionType.START_SHOOT); 
        keyboardLayout.put(KeyEvent.VK_CLEAR, ActionType.START_SHOOT);//numpad 5 w/o numlock
        keyboardLayout.put(-KeyEvent.VK_SPACE, ActionType.STOP_SHOOT);
        keyboardLayout.put(-KeyEvent.VK_NUMPAD5, ActionType.STOP_SHOOT);  
        keyboardLayout.put(KeyEvent.VK_LEFT, ActionType.LEFT);
        keyboardLayout.put(-KeyEvent.VK_LEFT, ActionType.UN_LEFT);
        keyboardLayout.put(KeyEvent.VK_RIGHT, ActionType.RIGHT);  
        keyboardLayout.put(-KeyEvent.VK_RIGHT, ActionType.UN_RIGHT);
        keyboardLayout.put(KeyEvent.VK_UP, ActionType.FORWARDS);
        keyboardLayout.put(-KeyEvent.VK_UP, ActionType.UN_FORWARDS);  
        keyboardLayout.put(KeyEvent.VK_DOWN, ActionType.BACKWARDS);
        keyboardLayout.put(-KeyEvent.VK_DOWN, ActionType.UN_BACKWARDS);
        keyboardLayout.put(KeyEvent.VK_INSERT, ActionType.STRAFE_RIGHT);
        keyboardLayout.put(KeyEvent.VK_END, ActionType.BRAKE);
        keyboardLayout.put(-KeyEvent.VK_END, ActionType.UN_BRAKE);
        keyboardLayout.put(192, ActionType.BERSERK); //tilde `
        keyboardLayout.put(KeyEvent.VK_NUMPAD0, ActionType.STRAFE_RIGHT);
        keyboardLayout.put(KeyEvent.VK_CONTROL, ActionType.STRAFE_LEFT);
        keyboardLayout.put(KeyEvent.VK_HOME, ActionType.EXPLODE_ALL); 
        keyboardLayout.put(KeyEvent.VK_Q, ActionType.ROTATE_WEAPONS);
        keyboardLayout.put(KeyEvent.VK_1, ActionType.SET_WEAPON_1);
        keyboardLayout.put(KeyEvent.VK_2, ActionType.SET_WEAPON_2);
        keyboardLayout.put(KeyEvent.VK_3, ActionType.SET_WEAPON_3);
        keyboardLayout.put(KeyEvent.VK_4, ActionType.SET_WEAPON_4);
        keyboardLayout.put(KeyEvent.VK_5, ActionType.SET_WEAPON_5);
        keyboardLayout.put(KeyEvent.VK_6, ActionType.SET_WEAPON_6);
        keyboardLayout.put(KeyEvent.VK_7, ActionType.SET_WEAPON_7);
        keyboardLayout.put(KeyEvent.VK_8, ActionType.SET_WEAPON_8);
        keyboardLayout.put(KeyEvent.VK_9, ActionType.SET_WEAPON_9);
        keyboardLayout.put(KeyEvent.VK_P, ActionType.PAUSE);
        keyboardLayout.put(KeyEvent.VK_T, ActionType.SAVE);
        keyboardLayout.put(KeyEvent.VK_Y, ActionType.LOAD);
    }

    public ActionType translate(int keystroke) {
        return keyboardLayout.containsKey(keystroke)?keyboardLayout.get(keystroke):ActionType.UNKNOWN;
    }

    
    public static KeystrokeManager getInstance() {
        return instance;
    }
    /**
     * Called automatically by key listener when keys are released.
     * The keyCodes are made negative to show this.
     * 
     * @param e the <code>KeyEvent</code> generated by the key listener
     * @since Classic
     */
    public synchronized void keyReleased( KeyEvent e )
    {
        Game.getInstance().getActionManager().add( new Action( AsteroidsFrame.frame().localPlayer(), 0 - e.getKeyCode(), Game.getInstance().timeStep + 2 ) );
        if ( e.isShiftDown() )
            AsteroidsFrame.frame().localPlayer().setSnipeMode( true );
        else
            AsteroidsFrame.frame().localPlayer().setSnipeMode( false );
        if ( Client.is() )
            Client.getInstance().keyStroke( 0 - e.getKeyCode() );
    }


    /**
     * Called automatically by key listener for all keys pressed, and creates an <code>Action</code> to store the relevent data.
     * 
     * @param e the <code>KeyEvent</code> generated by the key listener
     * @since Classic
     */
    public synchronized void keyPressed( KeyEvent e )
    {
        if ( Game.getInstance().isPaused() )
        {
            Game.getInstance().setPaused( false );
            return;
        }

        if ( e.isShiftDown() )
            AsteroidsFrame.frame().localPlayer().setSnipeMode( true );
        else
            AsteroidsFrame.frame().localPlayer().setSnipeMode( false );

        // Is it a local action?
        switch ( translate(e.getKeyCode() ) )
        {
            case QUIT:
                Running.quit();
                break;
            case TOGGLE_MUSIC:
                AsteroidsFrame.frame().getPanel().toggleMusic();
                break;
            case TOGGLE_SOUND:
                AsteroidsFrame.frame().getPanel().toggleSound();
                break;
            case TOGGLE_FULL_SCREEN:
                AsteroidsFrame.frame().toggleFullscreen();
                break;
            case SET_EASTER_EGG:
                for ( GameObject go : Game.getInstance().gameObjects )
                    if ( go instanceof Station )
                        ( (Station) go ).setEasterEgg();
                break;
            case WARP:
                if ( !Client.is() )
                    AsteroidsFrame.frame().getPanel().warpDialog();
                break;
            case TOGGLE_ANTIALIASING:
                AsteroidsFrame.frame().getPanel().toggleReneringQuality();
                break;
            case TOGGLE_SCOREBOARD:
                AsteroidsFrame.frame().getPanel().toggleScoreboard();
                break;
            case TOGGLE_TRACKER:
                AsteroidsFrame.frame().getPanel().toggleTracker();
                break;
            default:
                Game.getInstance().getActionManager().add( new Action( AsteroidsFrame.frame().localPlayer(), e.getKeyCode(), Game.getInstance().timeStep + 2 ) );

                if ( Client.is() )
                    Client.getInstance().keyStroke( e.getKeyCode() );
        }
    }
    
    /**
     * Dummy method to satisfy <code>KeyListener</code> interface.
     * 
     * @param e the <code>KeyEvent</code> generated by the key listener
     * @since Classic
     */
    public void keyTyped( KeyEvent e )
    {
    }

}
