package Cards;

/**
 * Class to represent a playing Card.
 *
 * @author David Winter
 */
public class Card
{
    /**
     * Suit value of Card.
     */
    private Suit suit;
    
    /**
     * Face value of Card.
     */
    private Face face;
    
    /**
     * Card number/code in pack.
     */
    private int code;
    
    /**
     * Conversion constructor that creates a new playing Card.
     *
     * @param   face    The Face value of the card.
     * @param   suit    The Suit of the card.
     * @param   code    The card number in a pack.
     */
    public Card(Face face, Suit suit, int code)
    {
        setFace(face);
        setSuit(suit);
        setCode(code);
    }
    
    /**
     * Mutator method that sets the face of the card.
     *
     * @param   face    The Face value of the card.
     */
    private void setFace(Face face)
    {
        this.face = face;
    }
    
    /**
     * Accessor method that returns the Face value of the card.
     *
     * @return The Face value of the card.
     */
    public Face getFace()
    {
        return this.face;
    }
    
    /**
     * Mutator method that sets the Suit of the card.
     *
     * @param   suit    The Suit of the card.
     */
    private void setSuit(Suit suit)
    {
        this.suit = suit;
    }
    
    /**
     * Accessor method that returns the Suit of the card.
     *
     * @return The Suit of the card.
     */
    public Suit getSuit()
    {
        return this.suit;
    }
    
    /**
     * Mutator method that sets the card number code.
     *
     * @param code  The card number code.
     */
    private void setCode(int code)
    {
        this.code = code;
    }
    
    /**
     * Accessor method that returns card number code.
     *
     * @return The card number code.
     */
    public int getCode()
    {
        return code;
    }
    
    /**
     * Returns the Integer value of a playing Card.
     *
     * @return  The Integer value of the playing Card.
     */
    public int getValue()
    {
        return this.getFace().getValue();
    }
    
    /**
     * String representation of the playing Card.
     *
     * @return  String representation of the playing Card.
     */
    public String toString()
    {
        return getFace() + " of " + getSuit();
    }
}