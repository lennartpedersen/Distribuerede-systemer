import javax.swing.*;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleContext;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GUI extends JFrame implements ActionListener, KeyListener {

	private JFrame window = new JFrame();
	private Container pane;
	private JButton newGameButton = new JButton(), rightMostButton = new JButton(), backButton = new JButton("Back");
	private JPanel buttonViewPanel = new JPanel(), visualsViewPanel = new JPanel(), topViewPanel = new JPanel();
	private JTextArea textArea = new JTextArea();
	private JTextArea userInputTextArea = new JTextArea();
	private JScrollPane scrollPane = new JScrollPane(textArea), userInputScrollPane;
	
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
	
	public GUI() {
		setUpMainWindow();
	}
	
	private void setUpMainWindow() {
		window.setSize(450, 700);
		pane = window.getContentPane();
		window.setResizable(false);
		
		setUpButtons();
		visualsViewPanel.setBackground(Color.WHITE);
		buttonViewPanel.setBackground(Color.GRAY);
		int topBottomPanelHeight = (int) (window.getHeight() * 0.05);
		buttonViewPanel.setPreferredSize(new Dimension(window.getWidth(), topBottomPanelHeight*3));
		topViewPanel.setBackground(Color.GRAY);
		topViewPanel.setPreferredSize(new Dimension(window.getWidth(), topBottomPanelHeight));
		pane.add(topViewPanel, BorderLayout.PAGE_START);
		pane.add(buttonViewPanel, BorderLayout.PAGE_END);
		pane.add(visualsViewPanel, BorderLayout.CENTER);
		window.setVisible(true);
		window.setDefaultCloseOperation(EXIT_ON_CLOSE);
		window.setLocationRelativeTo(null);
	}

	private void setUpButtons() {
		newGameButton.addActionListener(this);
		newGameButton.setText("New Game");
		newGameButton.setActionCommand("start new game");
		rightMostButton.addActionListener(this);
		rightMostButton.setText("Join Game");
		rightMostButton.setActionCommand("join game");
		buttonViewPanel.add(newGameButton, BorderLayout.LINE_START);
		buttonViewPanel.add(rightMostButton, BorderLayout.LINE_END);
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
	
	private void restoreMainWindow() {
		topViewPanel.removeAll();
		buttonViewPanel.removeAll();
		visualsViewPanel.removeAll();
		
		userInputTextArea.removeKeyListener(this);
		textArea.setText(null);
		setUpButtons();
		backButton.removeActionListener(this);
		newGameButton.removeActionListener(this);
		
		window.validate();
		topViewPanel.repaint();
		buttonViewPanel.repaint();
		visualsViewPanel.repaint();
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
			restoreMainWindow();
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
