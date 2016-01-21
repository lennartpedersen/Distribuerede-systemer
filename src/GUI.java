import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.AttributeSet;
import javax.swing.text.PlainDocument;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class GUI extends JFrame implements ActionListener {
	private static final long serialVersionUID = -198111177139175434L;
	
	private JFrame mainWindow = new JFrame(); //The desktop window for the application.
	private JFrame newgameWindow, gameoverWindow; //The popup window for creating a new game.
	private JTextField statusBarField; //The status field, used to always show errors and information.
	
	private JList<String> gameList, choicesList; //Reference to the list showing available games and the list showing possible choices in game.
	private JTextField loginField, chatField, questionField, answerField, gamenameField, numPlayersField, numRoundsField; //References to necessary textfields for getting written input.
	private JTextArea receivedMessagesArea, scoreArea; //Reference to the text areas.
	private JLabel requestStartLabel, correctAnswerLabel; //Reference to the labels.
	private JButton loginButton, newgameButton, answerButton, choiceButton, sendButton, requestStartButton; //Reference to buttons for disable and enabling.
	private JPanel choicePhasePanel; //Reference to the panel showing possible answers to a question.
	
	private CardLayout stateManager, gamePhaseManager; //The layoutmanagers that controls the current state of the GUI.
	private JPanel gamePanel; //The panel containing the phases of the actual game. Changes phases with gamePhaseManager.
	private JPanel statePanel; //The panel containing the GUI states, this panel changes between states with the stateManager (CardLayout).
	private JPanel loginState, joingameState, pregameState, gameState; //The different states of the GUI.
	
	private Client client; //Reference to the client for given input to the client.
	private boolean hasRequestedStart = false;
	
	public static final String LOGINSTATE = "LOGIN", JOINGAMESTATE = "JOIN", PREGAMESTATE = "PREGAME", GAMESTATE = "GAME"; //Constants for the possible GUI states.
	
	public GUI(Client client) {
		this.client = client;
		
		setUpMainWindow();
		setUpLoginState(); //Creates the join game state.
		statePanel.add(loginState, LOGINSTATE); //Adds the join game state as a possible state.
		setUpJoinGameState(); //Creates the login state.
		statePanel.add(joingameState, JOINGAMESTATE); //Adds the login state as a possible state.
		setUpPreGameState(); //Creates the pre-game state.
		statePanel.add(pregameState, PREGAMESTATE); //Adds the pre-game state as a possible state.
		setUpGameState(); //Creates the game state.
		statePanel.add(gameState, GAMESTATE); //Adds the game state as a possible state.
		setUpNewGamePopup(); //Creates the newgame popup window. Doesn't show it.
		setUpGameoverPopup(); //Creates the gameover popup window. Doesn't show it.
		
		mainWindow.setVisible(true);
	}

	//Methods for constructing the GUI.
	private void setUpMainWindow() { //Sets up the desktop window for the application. Anything regarding the setup of the mainWindow and statePanel goes here.
		mainWindow.setSize(450, 700);
		mainWindow.setResizable(false);
		mainWindow.setDefaultCloseOperation(EXIT_ON_CLOSE);
		mainWindow.setLocationRelativeTo(null);
		mainWindow.setLayout(new BorderLayout());
		
		//Panel to contain the main application GUI.
		JPanel mainWindowPanel = new JPanel();
		stateManager = new CardLayout();
		mainWindowPanel.setLayout(stateManager);
		statePanel = new JPanel(stateManager);
		
		mainWindowPanel.add(statePanel);
		mainWindow.add(mainWindowPanel);
		mainWindow.add(newStatusBar(), BorderLayout.SOUTH);
	}
	
	private JPanel newStatusBar(){
		JPanel statusBarPanel = new JPanel(new BorderLayout());
		
		//Create status bar as textfield.
		statusBarField = new JTextField();
		statusBarField.setEditable(false);
		statusBarField.setFocusable(false);
		statusBarField.setMaximumSize(
				new Dimension(Integer.MAX_VALUE, statusBarField.getPreferredSize().height));
		
		statusBarPanel.add(statusBarField);
		return statusBarPanel;
	}
	
	@SuppressWarnings("serial")
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
		
		//Anonymous class that listens for key presses on the game list.
		loginField.addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e) {
				//Implements to login on pressing ENTER.
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					sendLogin(loginField.getText());
			}
		});
		
		//Anonymous class to limit the number of characters that can be entered into the textfield.
		loginField.setDocument(new PlainDocument(){
			public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
				if (str == null)
					return;
			
				if ((getLength() + str.length()) <= 10) {
					super.insertString(offset, str, attr);
				}
			}
		});
		loginField.setHorizontalAlignment(JTextField.CENTER);
		centerElement(loginField);
		
		//Creates and configures login button.
		loginButton = createNewButton("Login", "login");
		centerElement(loginButton);
		
		loginState.add(Box.createVerticalGlue());
		loginState.add(username);
		loginState.add(loginField);
		loginState.add(loginButton);
		loginState.add(Box.createVerticalGlue());
	}
	
	private void setUpJoinGameState() { //Sets up the join game state. Anything regarding the setup of the join game state and its elements goes here.
		joingameState = new JPanel();
		joingameState.setLayout(new BoxLayout(joingameState, BoxLayout.Y_AXIS));
		
		//Create the list showing games.
		gameList = new JList<String>();
		gameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		//Anonymous class that listens for selection change in the game list.
		gameList.addListSelectionListener(new ListSelectionListener(){
		    @Override  
			public void valueChanged(ListSelectionEvent e) {
		        //Can implement to join game on selection. Does nothing right now.
		    }
		});
		
		//Anonymous class that listens for mouse clicks on the game list.
		gameList.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				//Implements to join game on double click.
				if (1 < e.getClickCount())
					sendJoingame(gameList.getSelectedValue());
			}
		});
		
		//Anonymous class that listens for key presses on the game list.
		gameList.addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e) {
				//Implements to join game on pressing ENTER after selecting.
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					sendJoingame(gameList.getSelectedValue());
			}
		});
		
		//Creates a new scrollpane to allow scrolling in the game list, if necessary.
		JScrollPane scrollingList = new JScrollPane(gameList);
		scrollingList.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollingList.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		//Create a join game button.
		JButton refreshButton = createNewButton("Refresh Games", "refresh");
		centerElement(refreshButton);
		
		//Create a join game button.
		JButton joingameButton = createNewButton("Join Game", "joingame");
		centerElement(joingameButton);
		
		//Create a new game button.
		JButton newGameButton = createNewButton("New Game", "shownewgame");
		centerElement(newGameButton);
		
		joingameState.add(scrollingList);
		joingameState.add(refreshButton);
		joingameState.add(joingameButton);
		joingameState.add(newGameButton);
	}
	
	private void setUpPreGameState() { //Sets up the pre-game state. Anything regarding the setup of the pre-game state and its elements goes here.
		//Using GridBagLayout, precise grid-based component placement.
		pregameState = new JPanel(new GridBagLayout());
		
		//Creates the area for reading chat messages.
		receivedMessagesArea = new JTextArea();
		receivedMessagesArea.setEditable(false);
		receivedMessagesArea.setFocusable(false);
		
		//Creates a new scrollpane to allow scrolling in the chat message area, if necessary.
		JScrollPane scrollingList = new JScrollPane(receivedMessagesArea);
		scrollingList.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollingList.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
				
		//Creates the textfield for sending chat messages as well as a send button.
		chatField = new JTextField();
		chatField.setMaximumSize(
				new Dimension(Integer.MAX_VALUE, loginField.getPreferredSize().height));
		sendButton = createNewButton("Send", "send");
		
		//Anonymous class for listening to key presses in the chatfield.
		chatField.addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) { //If ENTER is pressed, sends message.
					sendMsg(chatField.getText());
				}
			}
		});
		
		//Creates a request start of game button and label for showing how many players are ready.
		requestStartButton = createNewButton("Request Start Game", "startgame");
		requestStartLabel = new JLabel("0/0");
		
		//GridBagConstraints
		GridBagConstraints scrollingListConstraints = new GridBagConstraints(0, 0, 4, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
		GridBagConstraints chatFieldConstraints = new GridBagConstraints(0, 1, 3, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
		GridBagConstraints sendButtonConstraints = new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);
		GridBagConstraints fillConstraints = new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);
		GridBagConstraints requestStartLabelConstraints = new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 10, 0);
		GridBagConstraints requestStartButtonConstraints = new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);
		
		pregameState.add(scrollingList, scrollingListConstraints);
		pregameState.add(chatField, chatFieldConstraints);
		pregameState.add(sendButton, sendButtonConstraints);
		pregameState.add(Box.createHorizontalGlue(), fillConstraints);
		pregameState.add(requestStartButton, requestStartButtonConstraints);
		pregameState.add(requestStartLabel, requestStartLabelConstraints);
	}
	
	private void setUpGameState(){ //Sets up the game state. Anything regarding the setup of the game state and its elements goes here.
		//Should include a New Game button and a list of currently active games to join.
		gameState = new JPanel(new BorderLayout());
		
		//Create text field for showing the current question in the game.
		questionField = new JTextField();
		questionField.setMaximumSize(
				new Dimension(Integer.MAX_VALUE, questionField.getPreferredSize().height));
		questionField.setEditable(false);
		
		//Create a panel for a new cardlayout for rotating between the phases of the game and a textarea for displaying the current score of the players.
		//ScorePanel
		JLabel scoreLabel = new JLabel("Scores:");
		scoreLabel.setHorizontalAlignment(JLabel.CENTER);
		JPanel scorePanel = new JPanel(new BorderLayout());
		scoreArea = new JTextArea();
		scoreArea.setEditable(false);
		scoreArea.setFocusable(false);
		scoreArea.setPreferredSize(
				new Dimension(200, scoreArea.getPreferredSize().height));
		scorePanel.add(scoreLabel, BorderLayout.NORTH);
		scorePanel.add(scoreArea, BorderLayout.CENTER);
		//GamePanel
		gamePanel = new JPanel();
		gamePhaseManager = new CardLayout();
		gamePanel.setLayout(gamePhaseManager);
		JPanel answerPhase = createAnswerPhase();
		JPanel choosePhase = createChoosePhase();
		gamePanel.add(answerPhase, "ANSWER");
		gamePanel.add(choosePhase, "CHOOSE");
		
		gameState.add(questionField, BorderLayout.NORTH);
		gameState.add(scorePanel, BorderLayout.EAST);
		gameState.add(gamePanel, BorderLayout.CENTER);
	}
	
	private void setUpNewGamePopup(){ //Sets up the newgame popup window. Anything regarding the setup of the newgame popup window and its elements goes here.
		newgameWindow = new JFrame();
		newgameWindow.setResizable(false);
		newgameWindow.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		newgameWindow.setLocationRelativeTo(null);
		
		//A Panel for collecting elements.
		JPanel mainNewGamePanel = new JPanel(new FlowLayout());
		
		//Panel for game name label and textfield.
		JPanel gamenamePanel = new JPanel();
		gamenamePanel.setLayout(new BoxLayout(gamenamePanel, BoxLayout.Y_AXIS));
		JLabel gamenameLabel = new JLabel("Game name:");
		gamenameField = new JTextField();
		gamenamePanel.add(gamenameLabel);
		gamenamePanel.add(gamenameField);
		
		//Panel for max number of players label and counter.
		JPanel numPlayersPanel = new JPanel();
		numPlayersPanel.setLayout(new BoxLayout(numPlayersPanel, BoxLayout.Y_AXIS));
		JLabel numPlayersLabel = new JLabel("Maximum number of players:");
		numPlayersField = new JTextField(); //TODO Should change to a real counter to avoid invalid input.
		numPlayersPanel.add(numPlayersLabel);
		numPlayersPanel.add(numPlayersField);
		
		//Panel for number of rounds label and counter.
		JPanel numRoundsPanel = new JPanel();
		numRoundsPanel.setLayout(new BoxLayout(numRoundsPanel, BoxLayout.Y_AXIS));
		JLabel numRoundsLabel = new JLabel("Number of rounds:");
		numRoundsField = new JTextField(); //TODO Should change to a real counter to avoid invalid input.
		numRoundsPanel.add(numRoundsLabel);
		numRoundsPanel.add(numRoundsField);
		
		//Create new game button.
		newgameButton = createNewButton("Create", "newgame");
		
		//Add parts to main panel.
		mainNewGamePanel.add(gamenamePanel);
		mainNewGamePanel.add(numPlayersPanel);
		mainNewGamePanel.add(numRoundsPanel);
		mainNewGamePanel.add(newgameButton);
		
		newgameWindow.add(mainNewGamePanel);
		newgameWindow.pack();
	}
	
	private void setUpGameoverPopup(){ //Sets up the gameover popup window. Anything regarding the setup of the gameover popup window and its elements goes here.
		//Create
		gameoverWindow = new JFrame();
		gameoverWindow.setResizable(false);
		gameoverWindow.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		gameoverWindow.setLocationRelativeTo(null);
		gameoverWindow.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e) {
				changeGUIState(GUI.JOINGAMESTATE);
			}
		});
		
		JPanel gameoverPanel = new JPanel(new BorderLayout());
		JLabel gameoverLabel = new JLabel("Game Over");
		gameoverLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		JButton returnButton = createNewButton("Return", "gameover");
		centerElement(gameoverLabel);
		centerElement(returnButton);
		
		gameoverPanel.add(gameoverLabel, BorderLayout.CENTER);
		gameoverPanel.add(returnButton, BorderLayout.SOUTH);
		gameoverWindow.add(gameoverPanel);
		gameoverWindow.pack();
	}
	
	private JPanel createAnswerPhase(){ //Creates panel for the games answer phase.
		JPanel answerPanel = new JPanel();
		answerPanel.setLayout(new BoxLayout(answerPanel, BoxLayout.Y_AXIS));
		
		//Label for telling if correct answer was entered.
		correctAnswerLabel = new JLabel("");
		correctAnswerLabel.setHorizontalAlignment(JLabel.CENTER);
		centerElement(correctAnswerLabel);
		
		//Textfield for entering an answer.
		answerField = new JTextField();
		answerField.setMaximumSize(
				new Dimension(Integer.MAX_VALUE, answerField.getPreferredSize().height));
		
		//Button for sending answer.
		answerButton = createNewButton("Answer", "answer");
		answerButton.setHorizontalAlignment(JButton.CENTER);
		centerElement(answerButton);
		
		//Anonymous class for listening to keypresses in the answerField.
		answerField.addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) { //If ENTER is pressed, sends entered answer.
					sendAnswer(answerField.getText());
				}
			}
		});
		
		answerPanel.add(Box.createVerticalGlue());
		answerPanel.add(correctAnswerLabel);
		answerPanel.add(answerField);
		answerPanel.add(answerButton);
		answerPanel.add(Box.createVerticalGlue());
		return answerPanel;
	}
	
	private JPanel createChoosePhase(){ //Creates panel for the games choice phase.
		choicePhasePanel = new JPanel();
		choicePhasePanel.setLayout(new BoxLayout(choicePhasePanel, BoxLayout.Y_AXIS));
		
		//JList for displaying all answers.
		choicesList = new JList<String>();
		choicesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		//Creates a new scrollpane to allow scrolling in the answer list, if necessary.
		JScrollPane scrollingChoicesList = new JScrollPane(choicesList);
		scrollingChoicesList.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollingChoicesList.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		//Button for choosing an answer.
		choiceButton = createNewButton("Choose", "choose");
		centerElement(choiceButton);
		
		choicePhasePanel.add(scrollingChoicesList);
		choicePhasePanel.add(choiceButton);
		return choicePhasePanel;
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
	
	//Methods for managing GUI elements.
	private void changeGUIState(String state){
		//String LOGINSTATE = "LOGIN", JOINGAMESTATE = "JOIN", PREGAMESTATE = "PREGAME", GAMESTATE = "GAME";
		switch(state){
		case LOGINSTATE:
			resetLoginState();
			break;
		case JOINGAMESTATE:
			sendGameListRequest();
			break;
		case PREGAMESTATE:
			resetPregameState();
			client.startChatThread();
			break;
		case GAMESTATE:
			resetGameState();
			break;
		}
		stateManager.show(statePanel, state); //Changes to the game state. Check constants for possible states.
	}
	
	@SuppressWarnings("unused")
	private void threadRefreshGameList() { //Creates new thread to smoothly update game list.
		Thread thread = new Thread(){
			@Override
			public void run(){
				sendGameListRequest();
			}
		};
		thread.start();
	}
	
	@SuppressWarnings("unused")
	private void threadRefreshScoreArea() { //Creates new thread to smoothly update game list.
		Thread thread = new Thread(){
			@Override
			public void run(){
				sendScoreListRequest();
			}
		};
		thread.start();
	}

	protected void sendGameListRequest() { //Called to request a new active games list.
		try {
			client.read(Tuple.SHOWGAMES);
		} catch (Exception e) {
			statusMessage(e.getMessage() ,true);
		}
	}
	
	protected void sendScoreListRequest() { //Called to request a new score list.
		try {
			client.read(Tuple.SCORES);
		} catch (Exception e) {
			statusMessage(e.getMessage());
		}
	}

	private void nextGamePhase(){ //Rotates between answer and choices phase of the game.
		gamePhaseManager.next(gamePanel);
	}

	private void showNewgame(){ //Shows the new game window.
		gamenameField.setText("");
		numPlayersField.setText("");
		numRoundsField.setText("");
		newgameWindow.setEnabled(true);
		newgameWindow.setLocationRelativeTo(null);
		newgameButton.setVisible(true);
		newgameWindow.setVisible(true);
	}

	void showGameover() { //Shows the game over window.
		gameoverWindow.setLocationRelativeTo(null);
		gameoverWindow.setVisible(true);
	}
	
	void statusMessage(String msg){
		statusMessage(msg, false);
	}
	
	void statusMessage(String msg, boolean isError){
		if (isError)
			statusBarField.setForeground(Color.RED);
		else
			statusBarField.setForeground(Color.BLACK);
		statusBarField.setText(msg);
	}
	
	void refreshGameList(List<?> gameNames){ //Updates the game list with the game names in the given array. Always call before and during join game state.
		String list[] = gameNames.toArray(new String[gameNames.size()]);
		gameList.setListData(list);
	}

	void refreshChoicesList(List<?> choices){ //Updates the answer choices with the answers in the given array. Always call before choice phase.
		String list[] = choices.toArray(new String[choices.size()]);
		choicesList.setListData(list);
	}
	
	void refreshScoreArea(HashMap<?, ?> scores){
		scoreArea.setText("Users with their corresponding score:\n");
		for (Entry<?, ?> entry : scores.entrySet())
			scoreArea.append(((String) entry.getKey()) + ": " + ((int) entry.getValue()) +"\n");
	}
	
	private void resetLoginState(){ //Reset all manipulatable elements in the login state.
		loginField.setText("");
		loginField.setEnabled(true);
		loginButton.setEnabled(true);
	}
	
	private void resetPregameState(){ //Reset all manipulatable elements in the pregame state.
		hasRequestedStart = false;
		requestStartButton.setEnabled(true);
		sendButton.setEnabled(true);
		chatField.setEnabled(true);
		chatField.setText("");
		receivedMessagesArea.setText("You can now chat with everyone in the game.\n"+
									"When you are ready to begin the game press the 'Request Start Game' button.\n");
		requestStartLabel.setText("");
	}
	
	private void resetGameState(){ //Reset all manipulatable elements in the game state.
		resetAnswerPhase();
		resetChoosePhase();
		sendScoreListRequest();
		gamePhaseManager.show(gamePanel, "ANSWER");
	}
	
	private void resetAnswerPhase(){ //Reset all manipulatable elements in the answer phase of the game.
		answerField.setEnabled(false);
		answerButton.setEnabled(false);
		boolean needsNewQuestion = true;
		answerField.setText("");
		correctAnswerLabel.setText("");
		while(needsNewQuestion){
			try {
				client.read(Tuple.QUESTION);
				needsNewQuestion = false;
				answerField.setEnabled(true);
				answerButton.setEnabled(true);
			} catch (Exception e) {
				statusMessage(e.getMessage(), true);
			}
		}
	}
	
	private void resetChoosePhase(){ //Reset all manipulatable elements in the choice phase of the game.
		choicesList.setEnabled(true);
		choiceButton.setEnabled(true);
	}
	
	void startGame() { //Starts a counter in the chat area and starts game after 5 seconds.
		ActionListener action = new ActionListener(){
			int count = 5;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				receiveChatMessage(""+count);
				count--;
				if (count < 0){
					changeGUIState(GUI.GAMESTATE);
					((Timer)e.getSource()).stop();
				}
			}
		};
		Timer timer = new Timer(1000, action);
		timer.start();
		sendButton.setEnabled(false);
		chatField.setEnabled(false);
		requestStartButton.setEnabled(false);
	}
	
	void receiveChatMessage(String msg){ //Call with a given chat message to be printed to the receivedMessagesArea.
		receivedMessagesArea.append(msg + "\n");
	}
	
	void receiveQuestion(String question){ //Prints given question to the questionField.
		questionField.setText(question);
	}
	
	private void sendMsg(String msg) { //Sends chat messages in the pre-game state.
		ArrayList<Object> data = new ArrayList<Object>();
		data.add(hasRequestedStart);
		data.add(msg);
		if (msg.toLowerCase().equals("start"))
			hasRequestedStart = true;
		client.put(Tuple.STARTGAME, data);
		
		chatField.setText("");
	}
	
	private void sendLogin(String userName){ //Sends login tuple to the server.
		//Send login to server.
		try {
			loginField.setEnabled(false);
			loginButton.setEnabled(false);
			client.putread(Tuple.LOGIN, userName);
			changeGUIState(GUI.JOINGAMESTATE);
		} catch (Exception e) {
			statusMessage(e.getMessage(), true);
			loginField.setEnabled(true);
			loginButton.setEnabled(true);
		}
		
	}
	
	private void sendAnswer(String answer){ //Sends answer to server and moves on to choices phase.
		answerField.setEnabled(false);
		answerButton.setEnabled(false);
		
		try {
			client.putread(Tuple.ANSWER, answer);
		} catch (Exception e) {
			correctAnswerLabel.setText(e.getMessage());
			answerField.setEnabled(true);
			answerButton.setEnabled(true);
			return; //If answer is correct, don't go to next phase.
		}
		
		try {
			client.read(Tuple.CHOICES);
			resetChoosePhase();
			nextGamePhase();
		} catch (Exception e) {
			statusMessage(e.getMessage(), true);
		}
	}
	
	private void sendChoice(int choice){ //Sends answer to server, refreshes scores and returns to answer phase.
		choicesList.setEnabled(false);
		choiceButton.setEnabled(false);
		
		client.put(Tuple.CHOOSE, choice++);
		sendScoreListRequest();
		
		resetAnswerPhase();
		nextGamePhase();
	}
	
	private void sendNewgame(String gameName, int gameSize, int gameLength){ //Send new game request to server.
		ArrayList<Object> data = new ArrayList<Object>();
		data.add(gameName);
		data.add(gameSize);
		data.add(gameLength);
		try {
			newgameWindow.setEnabled(false);
			client.putread(Tuple.CREATEGAME, data);
		} catch (Exception e) {
			statusMessage(e.getMessage(), true);
			newgameWindow.setEnabled(true);
			return;
		}
		sendJoingame(gameName);
	}
	
	private void sendJoingame(String gameName) {
		if (gameName == null)
			return;
		try {
			joingameState.setEnabled(false);
			client.putread(Tuple.JOINGAME, gameName);
		} catch (Exception e) {
			statusMessage(e.getMessage(), true);
			joingameState.setEnabled(true);
			return;
		}
		changeGUIState(GUI.PREGAMESTATE);
		joingameState.setEnabled(true);
	}

	//Listener methods.
	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
		case "login":
			//Send login tuple to server.
			sendLogin(loginField.getText());
			break;
		case "shownewgame":
			//Show newgame window.
			showNewgame();
			break;
		case "newgame":
			//Send newgame tuple to server.
			try {
				sendNewgame(gamenameField.getText(), Integer.parseInt(numPlayersField.getText()), Integer.parseInt(numRoundsField.getText()));
			} catch(NumberFormatException ex) {
				statusMessage("NumberFormatException", true);
			}
			newgameWindow.dispatchEvent(new WindowEvent(newgameWindow, WindowEvent.WINDOW_CLOSING));
			break;
		case "joingame":
			//Send join game tuple to server.
			sendJoingame(gameList.getSelectedValue());
			break;
		case "startgame":
			//Send start game request to server.
			sendMsg("start");
			break;
		case "answer":
			//Send answer tuple to server.
			sendAnswer(answerField.getText());
			break;
		case "choose":
			//Send a choice tuple to server.
			sendChoice(choicesList.getSelectedIndex());
			break;
		case "send":
			//Send a chat message to all players.
			sendMsg(chatField.getText());
			break;
		case "refresh":
			sendGameListRequest();
			break;
		case "gameover":
			//Closes the gameover window and changes gui state to joingame state.
			gameoverWindow.dispatchEvent(new WindowEvent(gameoverWindow, WindowEvent.WINDOW_CLOSING));
			break;
		default:
			break;
		}
	}
}
