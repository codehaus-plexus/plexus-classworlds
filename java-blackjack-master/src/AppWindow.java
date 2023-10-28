import javax.swing.*;
import java.awt.*;

import java.awt.event.*;


/**
 * Application window.
 * Holds the menu-bar etc.
 *
 * @author David Winter
 */
public class AppWindow extends JFrame 
    implements ActionListener, ComponentListener
{
    private GamePanel gamePanel;
    private Color defaultTableColour = new Color(6, 120, 0);
    
    private JMenuItem savePlayer = new JMenuItem("Save Current Player");
    private JMenuItem openPlayer = new JMenuItem("Open Existing Player");
    
    final int WIDTH = 600;
    final int HEIGHT = 500;

	public AppWindow()
    {
        super("Blackjack");
        
        addComponentListener(this);
        
        Dimension windowSize = new Dimension(WIDTH, HEIGHT);
        setSize(windowSize);
        setLocationRelativeTo(null); // put game in centre of screen
        
        this.setBackground(defaultTableColour);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // menu bar
        JMenuBar menuBar = new JMenuBar();
        
        JMenu playerMenu = new JMenu("Player");
        JMenuItem updatePlayerDetails = new JMenuItem("Update Player Details");
        playerMenu.add(updatePlayerDetails);
        playerMenu.addSeparator();
        playerMenu.add(savePlayer);
        playerMenu.add(openPlayer);
        menuBar.add(playerMenu);
        
        JMenu actionMenu = new JMenu("Actions");
        JMenuItem dealAction = new JMenuItem("Deal");
        JMenuItem hitAction = new JMenuItem("Hit");
        JMenuItem doubleAction = new JMenuItem("Double");
        JMenuItem standAction = new JMenuItem("Stand");
        actionMenu.add(dealAction);
        actionMenu.add(hitAction);
        actionMenu.add(doubleAction);
        actionMenu.add(standAction);
        menuBar.add(actionMenu);
        
        JMenu betMenu = new JMenu("Bet");
        JMenuItem oneChip = new JMenuItem("$1");
        JMenuItem fiveChip = new JMenuItem("$5");
        JMenuItem tenChip = new JMenuItem("$10");
        JMenuItem twentyFiveChip = new JMenuItem("$25");
        JMenuItem hundredChip = new JMenuItem("$100");
        betMenu.add(oneChip);
        betMenu.add(fiveChip);
        betMenu.add(tenChip);
        betMenu.add(twentyFiveChip);
        betMenu.add(hundredChip);
        menuBar.add(betMenu);
        
        JMenu windowMenu = new JMenu("Window");
        JMenuItem windowTableColourMenu = new JMenuItem("Change Table Colour");
        windowMenu.add(windowTableColourMenu);
        menuBar.add(windowMenu);
        
        JMenu helpMenu = new JMenu("Help");
        JMenuItem helpBlackjackRulesMenu = new JMenuItem("Blackjack Rules");
        JMenuItem helpAboutMenu = new JMenuItem("About Blackjack");
        helpMenu.add(helpBlackjackRulesMenu);
        helpMenu.addSeparator();
        helpMenu.add(helpAboutMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
        
        // keyboard shortcuts
        
        updatePlayerDetails.setAccelerator(
            KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_U,                                            
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        savePlayer.setAccelerator(
            KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_S,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        openPlayer.setAccelerator(
            KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_O,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));   
        dealAction.setAccelerator(
            KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_N,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        hitAction.setAccelerator(
            KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_C,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        doubleAction.setAccelerator(
            KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_D,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        standAction.setAccelerator(
            KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_F,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        oneChip.setAccelerator(
            KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_1,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        fiveChip.setAccelerator(
            KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_2,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        tenChip.setAccelerator(
            KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_3,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        twentyFiveChip.setAccelerator(
            KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_4,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        hundredChip.setAccelerator(
            KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_5,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        
        
		// action listeners
		dealAction.addActionListener(this);
        hitAction.addActionListener(this);
        doubleAction.addActionListener(this);
        standAction.addActionListener(this);
		updatePlayerDetails.addActionListener(this);
		savePlayer.addActionListener(this);
		openPlayer.addActionListener(this);
		windowTableColourMenu.addActionListener(this);
		helpAboutMenu.addActionListener(this);
		oneChip.addActionListener(this);
        fiveChip.addActionListener(this);
        tenChip.addActionListener(this);
        twentyFiveChip.addActionListener(this);
        hundredChip.addActionListener(this);
        		
        gamePanel = new GamePanel();
        gamePanel.setBackground(defaultTableColour);
		add(gamePanel);
        
        setVisible(true);
    }

	public void actionPerformed(ActionEvent evt)
    {
        String act = evt.getActionCommand();
        
        if (act.equals("$1"))
        {
            gamePanel.increaseBet(1);
        }
        else if (act.equals("$5"))
        {
            gamePanel.increaseBet(5);
        }
        else if (act.equals("$10"))
        {
            gamePanel.increaseBet(10);
        }
        else if (act.equals("$25"))
        {
            gamePanel.increaseBet(25);
        }
        else if (act.equals("$100"))
        {
            gamePanel.increaseBet(100);
        }
        else if (act.equals("Deal"))
        {
            gamePanel.newGame();
        }
        else if (act.equals("Hit"))
        {
            gamePanel.hit();
        }
        else if (act.equals("Double"))
        {
            gamePanel.playDouble();
        }
        else if (act.equals("Stand"))
        {
            gamePanel.stand();
        }
        else if (act.equals("Update Player Details"))
        {
            gamePanel.updatePlayer();
        }
        else if (act.equals("Save Current Player"))
        {
            gamePanel.savePlayer();
        }
        else if (act.equals("Open Existing Player"))
        {
            gamePanel.openPlayer();
        }
		else if (act.equals("Change Table Colour"))
		{
		    Color tableColour = JColorChooser.showDialog(this, "Select Table Colour", defaultTableColour);
		    this.setBackground(tableColour);
		    gamePanel.setBackground(tableColour);
		    gamePanel.repaint();
		    this.repaint();
		}
		else if (act.equals("About Blackjack"))
		{
		    String aboutText = "<html><p align=\"center\" style=\"padding-bottom: 10px;\">Written by David Winter &copy; 2006<br>Version 1.0</p><p align=\"center\" style=\"padding-bottom: 10px;\"><small>Become such an expert while developing this, <br>I won $1000 online in a game of Blackjack!</small></p><p align=\"center\">email: djw@davidwinter.me.uk<br>web: davidwinter.me.uk</p></html>";
		    JOptionPane.showMessageDialog(this, aboutText, "About Blackjack", JOptionPane.PLAIN_MESSAGE);
		}
		
		gamePanel.updateValues();
	}
	
	public void componentResized(ComponentEvent e)
	{
	    int currentWidth = getWidth();
	    int currentHeight = getHeight();
	    
	    boolean resize = false;
	    
	    if (currentWidth < WIDTH)
	    {
	        resize = true;
	        currentWidth = WIDTH;
	    }
	    
	    if (currentHeight < HEIGHT)
	    {
	        resize = true;
	        currentHeight = HEIGHT;
	    }
	    
	    if (resize)
	    {
	        setSize(currentWidth, currentHeight);
	    }
	}
	
	public void componentMoved(ComponentEvent e) { }
	public void componentShown(ComponentEvent e) { }
	public void componentHidden(ComponentEvent e) { }
}