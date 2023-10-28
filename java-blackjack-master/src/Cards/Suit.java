package Cards;

/**
 * Class to represent a card Suit.
 *
 * @author David Winter
 */
public class Suit
{
    /**
     * Name of the Suit.
     */
    private String name;
    
    /**
     * Conversion constructor that creates a new Suit based on the name.
     *
     * @param   name    The name of the Suit.
     */
    public Suit(String name)
    {
        this.name = name;
    }
    
    /**
     * Accessor method to get the name of the Suit.
     *
     * @return  The name of the Suit.
     */
    public String getName()
    {
        return this.name;
    }
    
    /**
     * String representation of the Suit.
     *
     * @return  String representation of the Suit.
     */
    public String toString()
    {
        return getName();
    }
}