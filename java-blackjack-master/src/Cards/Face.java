package Cards;

/**
 * Class to represent the face value of a Card.
 *
 * @author David Winter
 */
public class Face
{
    /**
     * String representation of the face value.
     */
    private String name;
    
    /**
     * Integer representation of the face value.
     */
    private int value;
    
    private final int HIGH_ACE = 11;
    private final int LOW_ACE = 1;
    
    /**
     * Conversion constructor that creates a Face object based on
     * an Integer code.
     *
     * @param   face    The Integer code that represents a face value.
     */
    public Face(int face)
    {
        switch (face)
        {
            case 1:
                setName("Ace");
                setValue(11);
                break;
            case 2:
                setName("Two");
                setValue(2);
                break;
            case 3:
                setName("Three");
                setValue(3);
                break;
            case 4:
                setName("Four");
                setValue(4);
                break;
            case 5:
                setName("Five");
                setValue(5);
                break;
            case 6:
                setName("Six");
                setValue(6);
                break;
            case 7:
                setName("Seven");
                setValue(7);
                break;
            case 8:
                setName("Eight");
                setValue(8);
                break;
            case 9:
                setName("Nine");
                setValue(9);
                break;
            case 10:
                setName("Ten");
                setValue(10);
                break;
            case 11:
                setName("Jack");
                setValue(10);
                break;
            case 12:
                setName("Queen");
                setValue(10);
                break;
            case 13:
                setName("King");
                setValue(10);
                break;
            default:
                break;
        }
    }
    
    /**
     * Mutator method that sets the String representation of the face value.
     *
     * @param   name    The String representation of the face value.
     */
    private void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * Accessor method that returns the name of a face value.
     *
     * @return The name of the face value.
     */
    public String getName()
    {
        return this.name;
    }
    
    /**
     * Mutator method that sets the Integer value of a face value.
     *
     * @param   value   The Integer value of the face value.
     */
    private void setValue(int value)
    {
        this.value = value;
    }
    
    /**
     * Accessor method that returns the Integer value of the face value.
     *
     * @return  The Integer value of the face value.
     */
    public int getValue()
    {
        return this.value;
    }
    
    public boolean isAce()
    {
        return (name.equals("Ace")) ? true : false;
    }
    
    public boolean isLowAce()
    {
        return (name.equals("Ace") && getValue() == LOW_ACE) ? true : false;
    }
    
    public void switchAce()
    {
        if (isAce())
        {
            if (getValue() == HIGH_ACE)
            {
                setValue(LOW_ACE);
            }
        }
    }
    
    /**
     * String representation of the face value.
     *
     * @return  The String representation of the face value.
     */
    public String toString()
    {
        return getName();
    }
}