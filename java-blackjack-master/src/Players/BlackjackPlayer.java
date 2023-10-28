package Players;

import java.io.*;

/**
 * Class that represents a general Blackjack player. Including the Dealer.
 * The dealer though plays to certain rules, and because of this, will need
 * to override some methods in the Dealer class.
 *
 * @author David Winter
 */
public class BlackjackPlayer implements Serializable
{
    /**
     * Name of Blackjack player.
     */
    private String name;
    
    /**
     * Age of Blackjack player.
     */
    private int age;
    
    /** 
     * Gender of Blackjack player.
     */
    private String gender;
    
    public BlackjackPlayer()
    {
        
    }
    
    /**
     * Conversion constructor that creates a new player.
     *
     * @param   name    The name of the player.
     * @param   age     The age of the player.
     * @param   gender  The players gender.
     */
    public BlackjackPlayer(String name, int age, String gender)
    {
        setName(name);
        setAge(age);
        setGender(gender);
    }
    
    /**
     * Mutator method that sets the name of the player.
     * 
     * @param   name    The players name.
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * Accessor method that returns the players name.
     *
     * @return The players name.
     */
    public String getName()
    {
        return this.name;
    }
    
    /**
     * Mutator method that sets the players age! If only...
     *
     * @param   age     The players age.
     */
    public void setAge(int age)
    {
        this.age = age;
    }
    
    /**
     * Accessor method that returns the players age.
     *
     * @return Players age.
     */
    public int getAge()
    {
        return this.age;
    }
    
    /**
     * Mutator method that sets the players gender.
     *
     * @param   gender  The players gender.
     */
    public void setGender(String gender)
    {
        this.gender = gender;
    }
    
    /**
     * Accessor method that returns the players gender.
     */
    public String getGender()
    {
        return this.gender;
    }
    
    /**
     * String representation of the player.
     *
     * @return The players name.
     */
    public String toString()
    {
        return getName();
    }    
}