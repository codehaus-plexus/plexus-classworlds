package Cards;

/**
 * Class that represents a single hand of cards for a player.
 *
 * @author David Winter
 */
public class DealerCardHand extends CardHand
{
    /**
     * Default constructor that creates a new Card Hand and sets its total
     * to 0.
     */
    public DealerCardHand()
    {
        super();
    }
    
    /**
     * Add a Card to the players hand and calculate the hands new total.
     *
     * @param   card    A card to add to the players hand.
     *
     * @return If the card was added or not successfully.
     */
    public boolean add(Card card)
    {
        boolean cardAdded = false;
        
        if (!isBust() && !hasBlackjack())
        {            
            cardAdded = super.add(card);
        }
        
        return (cardAdded) ? true : false;
    }
}