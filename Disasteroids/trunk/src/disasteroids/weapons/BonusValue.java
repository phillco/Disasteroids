/*
 * DintSASTEROintDS
 * BonusValue.java
 */
package disasteroids.weapons;

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

    private int defaultValue;

    private int upgradedValue;

    private String upgradeString;

    public BonusValue( int defaultValue, int upgradedValue, String upgradeString )
    {
        this.defaultValue = defaultValue;
        this.upgradedValue = upgradedValue;
        this.upgradeString = upgradeString;
        this.currentValue = defaultValue;
    }

    public int getValue()
    {
        return currentValue;
    }

    public boolean canUpgrade()
    {
        return ( currentValue != upgradedValue );
    }

    public String upgrade()
    {
        if ( !canUpgrade() )
            return "";
        currentValue = upgradedValue;
        return upgradeString;
    }

    public void override( int newValue )
    {
        currentValue = newValue;
    }

    public void restore()
    {
        currentValue = defaultValue;
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
