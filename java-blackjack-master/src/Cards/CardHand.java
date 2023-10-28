package Cards;

import java.util.*;

/**
 * Class that represents a single hand of cards for a player.
 *
 * @author David Winter
 */
public abstract class CardHand extends Vector<Card>
{    
    /**
     * Default constructor that creates a new Card Hand and sets its total
     * to 0.
     */
    public CardHand()
    {
        super();
    }
    
    /**
     * Clear the card hand. Remove all cards.
     */
    public void clear()
    {
        super.clear();
    }
    
    /**
     * Returns the card hand total.
     *
     * @return The card hand total.
     */
    public int getTotal()
    {  
        int total = 0;
        
        for (Card eachCard : this)
        {
            total += eachCard.getValue();
        }
        
        return total;
    }
    
    /**
     * Checks whether card hand is bust or not.
     *
     * @return Returns true if the hand is bust.
     */
    public boolean isBust()
    {
        return (getTotal() > 21) ? true : false;
    }
    
    /**
     * Check to see if hand has Blackjack - that being equal to 21 and 
     * only two cards.
     * 
     * @return Returns true if the player has Blackjack.
     */
    public boolean hasBlackjack()
    {
        return (getTotal() == 21 && this.size() == 2) ? true : false;
    }
    
    /**
     * String representation of card hand.
     *
     * @return String representation of a Card Hand.
     */
    public String toString()
    {
        return super.toString() + " (" + getTotal() + ")"; 
    }
}