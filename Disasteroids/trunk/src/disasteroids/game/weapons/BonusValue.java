/*
 * DintSASTEROintDS
 * BonusValue.java
 */
package disasteroids.game.weapons;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A field for a weapon that can be upgraded.
 * @author Phillip Cohen
 */
public class BonusValue
{
    private int currentValue;

    private int[] values;

    private int upgradeIndex = 0;

    private String upgradeString;

    public BonusValue( int defaultValue, int upgradedValue, String upgradeString )
    {
        this.values = new int[2];
        values[0] = currentValue = defaultValue;
        values[1] = upgradedValue;
        this.upgradeString = upgradeString;
    }

    public BonusValue( int[] values, String upgradeString )
    {
        if ( values.length == 0 )
            throw new IllegalArgumentException();

        this.values = values;
        this.upgradeString = upgradeString;
        this.currentValue = values[0];
    }

    public int getValue()
    {
        return currentValue;
    }

    public boolean canUpgrade()
    {
        return ( upgradeIndex < values.length - 1 );
    }

    public String upgrade()
    {
        if ( !canUpgrade() )
            return "";
        upgradeIndex++;
        currentValue = values[upgradeIndex];
        return upgradeString;
    }

    public void override( int newValue )
    {
        currentValue = newValue;
    }

    public void restore()
    {
        currentValue = values[0];
    }

    public void flatten( DataOutputStream stream ) throws IOException
    {
        stream.writeInt( currentValue );
    }

    public void loadFromSteam( DataInputStream stream ) throws IOException
    {
        currentValue = stream.readInt();
    }
}
