package Players;

import java.io.*;

import Cards.*;

/**
 * Class that represents a normal Blackjack player.
 *
 * @author David Winter
 */
public class Player extends BlackjackPlayer implements Serializable
{
    /**
     * The card hand the player holds.
     */
    public transient PlayerCardHand hand = new PlayerCardHand();
    
    /**
     * The players wallet size...
     */
    private double wallet = 100.00;
    
    /**
     * The players current bet.
     */
    private double  bet = 0.0;
    
    public Player()
    {
        super();
    }
    
    /**
     * Conversion constructor that creates a new player.
     *
     * @param   name    The name of the player.
     * @param   age     The age of the player.
     * @param   gender  The players gender.
     */
    public Player(String name, int age, String gender)
    {
        super(name, age, gender);
    }
    
    /**
     * Sets the players wallet size.
     *
     * @param   amount  The amount in the players wallet.
     */
    public void setWallet(double amount)
    {
        this.wallet = amount;
    }
    
    /**
     * Returns the size of the players wallet.
     *
     * @return The size of the players wallet.
     */
    public double getWallet()
    {
        return this.wallet;
    }
    
    /** 
     * Set the players bet.
     * 
     * @param   bet     The total size of bet the player wishes to place.
     *
     * @return Whether or not the bet was valid.
     */
    public boolean setBet(double bet)
    {
        boolean betMade = false;
        
        if (bet <= (getWallet() + getBet()))
        {
            this.wallet += this.bet; // reset old bet
            this.bet = bet; // set new bet
            this.wallet -= bet; // update wallet
            betMade = true;
        }
        
        return betMade;
    }
    
    /**
     * Get the players current bet.
     *
     * @return The players current bet.
     */
    public double getBet()
    {
        return this.bet;
    }
    
    public void clearBet()
    {
        this.wallet += this.bet;
        this.bet = 0.0;
    }
    
    /**
     * The amount the player wishes to double.
     *
     * @return Whether the bet was valid or not.
     */
    public boolean doubleBet()
    {
        boolean betDoubled = false;
        
        if (setBet(getBet() * 2))
        {
            betDoubled = true;
        }
        
        return betDoubled;
    }
    
    // insurance
    
    public void loses()
    {
        this.bet = 0.0;
    }
    
    public void wins(double amount)
    {
        this.wallet += amount;
        this.bet = 0.0;
    }
    
    /**
     * Has the player placed a bet yet?
     *
     * @return True if the player has placed a bet, otherwise false.
     */
    public boolean betPlaced()
    {
        return (getBet() > 0.0) ? true : false;
    }
    
    // wallet less than 1 in case .5 is in there from a blackjack
    public boolean isBankrupt()
    {
        return (getWallet() < 1 && getBet() <= 0) ? true : false;
    }
    
    public boolean canDouble()
    {
        boolean answer = false;
        
        if (getBet() <= getWallet())
        {
            answer = true;
        }
        
        return answer;
    }
    
    /**
     * Shows (returns) the players hand.
     *
     * @return The players card hand.
     */
    public PlayerCardHand getHand()
    {
        return this.hand;
    }
}