import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.AttributeSet;
import javax.swing.text.PlainDocument;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GUI extends JFrame implements ActionListener, KeyListener {

	private JFrame mainWindow = new JFrame();
	/*private JButton newGameButton = new JButton(), rightMostButton = new JButton(), backButton = new JButton("Back");
	private JPanel buttonViewPanel = new JPanel(), visualsViewPanel = new JPanel(), topViewPanel = new JPanel();
	*/
	
	private JTextArea receivedMessagesArea = new JTextArea(); //Reference to the text area containing received chat messages.
	private JList<String> gameList; //Reference to the list showing available games.
	
	private JScrollPane scrollPane = new JScrollPane(receivedMessagesArea);
	
	private JTextField loginField, chatField; //References to necessary textfields for getting written input.
	
	private CardLayout stateManager = new CardLayout(); //The layoutmanager that controls the current state of the GUI.
	private JPanel statePanel; //The panel containing the GUI states, this panel changes between states with the stateManager (CardLayout).
	private JPanel loginState, joingameState, gameState; //The different states of the GUI.
	private Client client; //Reference to the client for given input to the client.
	
	public static final String LOGINSTATE = "LOGIN", JOINGAMESTATE = "JOIN", GAMESTATE = "GAME";
	
	public GUI(Client client) {
		this.client = client; //TODO Communicate with the Client.
		setUpMainWindow();
		
		setUpLoginState(); //Creates the join game state.
		statePanel.add(loginState, LOGINSTATE); //Adds the join game state as a possible state.
		
		setUpJoinGameState(); //Creates the login state.
		statePanel.add(joingameState, JOINGAMESTATE); //Adds the login state as a possible state.
		
		setUpGameState(); //Creates the game state.
		statePanel.add(gameState, GAMESTATE); //Adds the game state as a possible state.
		
		mainWindow.setVisible(true);
		
		
		//TODO Everything below only for TESTING PURPOSES. SHOULD BE REMOVED before finalization.
		stateManager.show(statePanel, JOINGAMESTATE); //Changes to the game state.
		String[] list = {"One", "Two", "Three", "Foo", "Bar", "Foobar"};
		refreshGameList(list);
		
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
	
	private void setUpJoinGameState() { //Sets up the join game state. Anything regarding the setup of the join game state and its elements goes here.
		joingameState = new JPanel();
		joingameState.setLayout(new BoxLayout(joingameState, BoxLayout.Y_AXIS));
		
		//Create the list showing games.
		gameList = new JList<String>();
		gameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		//Anonymous class that listens for selection change in the list.
		gameList.addListSelectionListener(new ListSelectionListener(){
		    @Override  
			public void valueChanged(ListSelectionEvent e) {
		        //TODO Can implement to join game on selection.
		      }
		    });
		
		//Creates a new scrollpane to allow scrolling in the game list if necessary.
		JScrollPane scrollingList = new JScrollPane(gameList);
		scrollingList.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollingList.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		//Create a join game button.
		JButton joingameButton = createNewButton("Join Game", "join game");
		centerElement(joingameButton);
		
		//Create a new game button.
		JButton newGameButton = createNewButton("New Game", "new game");
		centerElement(newGameButton);
		
		joingameState.add(scrollingList);
		joingameState.add(joingameButton);
		joingameState.add(newGameButton);
	}
	
	private void refreshGameList(String[] list){
		gameList.setListData(list);
	}
	
	private void setUpGameState(){ //Sets up the game state. Anything regarding the setup of the game state and its elements goes here.
		//Should include a New Game button and a list of currently active games to join.
		gameState = new JPanel(new BorderLayout());
		JPanel buttonViewPanel = new JPanel();
		JPanel visualsViewPanel = new JPanel();
		JPanel topViewPanel = new JPanel();
		JButton newGameButton = createNewButton("New Game", "start new game");
		JButton rightMostButton = createNewButton("Join Game", "join game");
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

	private void setUpLoginState(){ //Sets up the login state. Anything regarding the setup of the login state and its elements goes here.
		loginState = new JPanel();
		loginState.setLayout(new BoxLayout(loginState, BoxLayout.Y_AXIS));
		centerElement(loginState);
		
		//Creates and configures login label.
		JLabel username = new JLabel("Username:");
		centerElement(username);
		
		//Creates and configures login field for username input.
		loginField = new JTextField();
		loginField.setMaximumSize(
				new Dimension(200, loginField.getPreferredSize().height));
		loginField.setDocument(new JTextFieldLimit(10));
		loginField.setHorizontalAlignment(JTextField.CENTER);
		centerElement(loginField);
		
		//Creates and configures login button.
		JButton loginButton = createNewButton("Login", "login");
		loginButton.addActionListener(this);
		centerElement(loginButton);
		
		loginState.add(username);
		loginState.add(loginField);
		loginState.add(loginButton);
	}

	
	
	private JButton createNewButton(String text, String actionCommand){ //Creates new JButton with necessary attributes.
		JButton button = new JButton(text);
		button.addActionListener(this);
		button.setActionCommand(actionCommand);
		return button;
	}
	
	private void centerElement(JComponent comp){ //Centers given element.
		comp.setAlignmentX(CENTER_ALIGNMENT);
		comp.setAlignmentY(CENTER_ALIGNMENT);
	}
	
	private void sendMsg(String msg) {
		receivedMessagesArea.append(msg + "\n");
		chatField.setText("");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		/*
		 * TODO
		 * Create action for login button "login".
		 * For new game "new game".
		 * For join game "join game".
		 * 
		 */
		switch (e.getActionCommand()) {
		case "new game":
			System.out.println("Player wants to start a new game.");
			
			break;
			
		case "join game":
			System.out.println("Player wants to join a game.");
			
			break;
			
		case "request start":
			System.out.println("Player is requesting a start.");
			
			break;
			
		case "back":
			
			break;
			
		case "send":
			
			break;

		default:
			break;
		}
	}


	@Override
	public void keyPressed(KeyEvent e) { //When pressing enter in the chat textfield, send the chat message.
		if(e.getKeyCode() == KeyEvent.VK_ENTER && chatField.isFocusOwner()) {
			sendMsg(chatField.getText());
			chatField.setCaretPosition(1);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {}
	@Override
	public void keyReleased(KeyEvent e) {}

	@SuppressWarnings("serial")
	class JTextFieldLimit extends PlainDocument { //A simple document extension to limit the amount of characters able to be typed in the JTextField.
		  private int limit;
		  JTextFieldLimit(int limit) {
		    super();
		    this.limit = limit;
		  }

		  public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
		    if (str == null)
		      return;

		    if ((getLength() + str.length()) <= limit) {
		      super.insertString(offset, str, attr);
		    }
		  }
		}
}
