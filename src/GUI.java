import javax.swing.*;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleContext;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GUI extends JFrame implements ActionListener, KeyListener {

	private JFrame mainWindow = new JFrame();
	private JButton newGameButton = new JButton(), rightMostButton = new JButton(), backButton = new JButton("Back");
	private JPanel buttonViewPanel = new JPanel(), visualsViewPanel = new JPanel(), topViewPanel = new JPanel();
	
	private JTextArea textArea = new JTextArea();
	private JTextArea userInputTextArea = new JTextArea();
	private JScrollPane scrollPane = new JScrollPane(textArea), userInputScrollPane;
	
	private CardLayout stateManager = new CardLayout(); //The layoutmanager that controls the current state of the GUI.
	private JPanel statePanel; //The panel containing the GUI states, this panel changes between states with the stateManager (CardLayout).
	private JPanel loginState, gameState; //The different states of the GUI.
	private Client client; //Reference to the client for given input to the client.
	
	public static final String LOGINSTATE = "LOGIN", GAMESTATE = "GAME";
	
	/*
	 * rightMostButton genbruges i hele GUI'en e.g. den skifter navn og action fra tid til anden.
	 * Layout:
	 * ______________________________________
	 * |									|
	 * |		topViewPanel				|  (back button)
	 * |____________________________________|
	 * |									|
	 * |									|
	 * |									|
	 * |									|
	 * |									|
	 * |		visualsViewPanel			|  (textArea)
	 * |									|
	 * |									|
	 * |									|
	 * |									|
	 * |									|
	 * |____________________________________|
	 * |									|
	 * |		buttonViewPanel				|  (newGameButton, morphingButton, userInputTextArea)
	 * |____________________________________|
	 */
	
	public GUI(Client client) {
		this.client = client;
		setUpMainWindow();
		
		setUpLoginState(); //Creates the login state.
		statePanel.add(loginState, LOGINSTATE); //Adds the login state as a possible state.
		
		setUpGameState(); //Creates the game state.
		statePanel.add(gameState, GAMESTATE); //Adds the game state as a possible state.
		
		mainWindow.setVisible(true);
		
		stateManager.show(statePanel, GAMESTATE); //Changes to the game state.
	}
	
	private void setUpMainWindow() { //Sets up the desktop window for the application. Anything regarding the setup of the mainWindow and statePanel goes here.
		mainWindow.setSize(450, 700);
		mainWindow.setResizable(false);
		mainWindow.setDefaultCloseOperation(EXIT_ON_CLOSE);
		mainWindow.setLocationRelativeTo(null);
		mainWindow.setLayout(stateManager);
		statePanel = new JPanel(stateManager);
		mainWindow.add(statePanel);
	}
	
	private void setUpGameState(){ //Sets up the game state. Anything regarding the setup of the game state its elements goes here.
		//Should be in main game state.
		gameState = new JPanel(new BorderLayout());
		
		newGameButton = createNewButton("New Game", "start new game");
		rightMostButton = createNewButton("Join Game", "join game");
		buttonViewPanel.add(newGameButton, BorderLayout.LINE_START);
		buttonViewPanel.add(rightMostButton, BorderLayout.LINE_END);
		
		visualsViewPanel.setBackground(Color.WHITE);
		buttonViewPanel.setBackground(Color.GRAY);
		int topBottomPanelHeight = (int) (mainWindow.getHeight() * 0.05);
		buttonViewPanel.setPreferredSize(new Dimension(mainWindow.getWidth(), topBottomPanelHeight*3));
		topViewPanel.setBackground(Color.GRAY);
		topViewPanel.setPreferredSize(new Dimension(mainWindow.getWidth(), topBottomPanelHeight));
		gameState.add(topViewPanel, BorderLayout.PAGE_START);
		gameState.add(buttonViewPanel, BorderLayout.PAGE_END);
		gameState.add(visualsViewPanel, BorderLayout.CENTER);
	}
	
	private JButton createNewButton(String text, String actionCommand){
		JButton button = new JButton(text);
		button.addActionListener(this);
		button.setActionCommand(actionCommand);
		return button;
	}

	private void setUpLoginState(){ 
		loginState = new JPanel();
		loginState.setLayout(new BoxLayout(loginState, BoxLayout.Y_AXIS));
		JLabel username = new JLabel("Username:");
		JTextField loginField = new JTextField();
		JButton loginButton = new JButton("Login");
		loginButton.addActionListener(this);
		loginState.add(username);
		loginState.add(loginField);
		loginState.add(loginButton);
	}
	
	private void setUpJoinGameWindow() {
		buttonViewPanel.removeAll();
		
		JTextField textField = new JTextField(20);
		buttonViewPanel.add(textField, BorderLayout.LINE_START);
		
		rightMostButton.setText("Request Start");
		rightMostButton.setActionCommand("request start");
		
		addBackButton();
		buttonViewPanel.add(rightMostButton, BorderLayout.LINE_END);
		buttonViewPanel.validate();
		buttonViewPanel.repaint();
	}

	private void addBackButton() {
		backButton.addActionListener(this);
		backButton.setActionCommand("back");
		topViewPanel.add(backButton, BorderLayout.LINE_START);
		topViewPanel.validate();
		topViewPanel.repaint();
	}
	
	private void setUpGameWindow() {
		buttonViewPanel.removeAll();

		userInputScrollPane = new JScrollPane(userInputTextArea);
		userInputScrollPane.setPreferredSize(new Dimension(buttonViewPanel.getWidth() - rightMostButton.getWidth() - 20, buttonViewPanel.getHeight()-10));
		userInputScrollPane.setAutoscrolls(true);
		userInputScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		userInputScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		buttonViewPanel.add(userInputScrollPane, BorderLayout.LINE_START);
		
		rightMostButton.setText("SEND!");
		rightMostButton.setActionCommand("send");
		buttonViewPanel.add(rightMostButton, BorderLayout.LINE_END);
		if(topViewPanel.getComponentCount() < 1) addBackButton();
		
		userInputTextArea.addKeyListener(this);
		userInputTextArea.requestFocus();
		userInputTextArea.setLineWrap(true);
		userInputTextArea.setWrapStyleWord(true);
		userInputTextArea.setText("");
		
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		
		scrollPane.setPreferredSize(new Dimension(visualsViewPanel.getWidth()-10, visualsViewPanel.getHeight()-10));
		scrollPane.setAutoscrolls(true);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		visualsViewPanel.add(scrollPane, BorderLayout.CENTER);
		
		visualsViewPanel.validate();
		visualsViewPanel.repaint();
		
		buttonViewPanel.validate();
		buttonViewPanel.repaint();
	}
	
	private void sendMsg(String msg) {
		textArea.append(msg + "\n");
		userInputTextArea.setText("");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
		case "start new game":
			System.out.println("Player wants to start a new game.");
			setUpGameWindow();
			break;
			
		case "join game":
			System.out.println("Player wants to join a game.");
			setUpJoinGameWindow();
			break;
			
		case "request start":
			System.out.println("Player is requesting a start.");
			setUpGameWindow();
			break;
			
		case "back":
			break;
			
		case "send":
			sendMsg(userInputTextArea.getText());
			break;

		default:
			break;
		}
	}


	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ENTER && userInputTextArea.isFocusOwner()) {
			sendMsg(userInputTextArea.getText());
			userInputTextArea.setCaretPosition(1);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {}
	@Override
	public void keyReleased(KeyEvent e) {}
	
}
