package hs.ss16.asp;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class HighscorePanel extends JPanel {
	
	private static final String HIGHSCORE_FILE = "/txt/highscore.txt";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int G_Width = 600;
	private static final int G_Height = 300;
	
	private static final int MAX_HIGHSCORE_NAME_LENGTH = 10;
	private static final int SPACE_UNTIL_TIMESCORE_DISPLAYED = MAX_HIGHSCORE_NAME_LENGTH + 5;
	private static final int NUMBER_OF_SAVED_SCORES = 5;
	
	private Board board;
	
	private JLabel headline;
	private JLabel scoreLines[];
	JButton newGameButton;
	
	private String scoreNames[];
	private int scoreTimes[];
	
	private int newestHighscoreIndex;
	
	private KeyListenerHighscore keyListenerHighscore;
	
	
	
	public HighscorePanel(Board board) {
		
		this.board = board;
		
		setBounds(200, ((int)World.screenSize.getHeight()/2)-148,G_Width, G_Height);
		setLayout(new GridLayout(7,1));
		
		loadScores();
		
		headline = new JLabel("Highscore", SwingConstants.CENTER);
		add(headline);
		
		for(int i = 0; i < scoreLines.length; i++) {
			add(scoreLines[i]);
		}
		setVisible(false);
		this.setOpaque( false ) ;
	}
	
	
	private void loadScores() {
		JLabel loadedScores[] = new JLabel[5];
		scoreNames = new String[5];
		scoreTimes = new int[5];
		
		String line;
		
		try {
			InputStream inputStream = getClass().getResourceAsStream(HIGHSCORE_FILE);
			InputStreamReader isr = new InputStreamReader(inputStream);
			BufferedReader bufferReader = new BufferedReader(isr);
			
			for(int i = 0; i < NUMBER_OF_SAVED_SCORES; i++) {
				line = bufferReader.readLine();
				loadScoreValuesFromLine(line, i);
				loadedScores[i] = new JLabel(convertToLabelString(scoreNames[i], scoreTimes[i]), SwingConstants.CENTER);
			}
		} catch(Exception e) {
			System.out.println("Error while reading highscore file line by line:" + e.getMessage()); 
		}
		
		scoreLines = loadedScores;
	}
	
	
	private String convertToLabelString(String name, int score) {
		String space = "";
		
		for(int i = 0; i < (SPACE_UNTIL_TIMESCORE_DISPLAYED - name.length()); i++) {
			space += " ";
		}
		
		int minutes = score / 60;
		int seconds = score % 60;
		
		String labelString = name + space + minutes + ":";
		
		if(seconds < 10)
			labelString += "00";
		else
			labelString += seconds;
		
		return labelString;
	}
	
	
	private void loadScoreValuesFromLine(String line, int placementIndex) {
		String sequence = "";
		
		for(int i = 0; i < line.length(); i++) {
			if(line.charAt(i) == ',') {
				scoreNames[placementIndex] = sequence;
				sequence = "";
				i += 2;	 //skip comma and space
			}
			
			sequence += line.charAt(i);
		}
		
		scoreTimes[placementIndex] = new Integer(sequence);
	}
		
	
	private boolean isNewHighscore(int score) {
		if(score > scoreTimes[NUMBER_OF_SAVED_SCORES - 1])
			return true;
		return false;
	}
	
	
	public void activateHighscorePanelAfterGame(int score) {
		setVisible(true);
		setFocusable(true);
		
		if(isNewHighscore(score))
			addNewHighscore(score);
		
		initializeNewGameButton();		
	}
	
	
	private void addNewHighscore(int score) {
		int newHighscoreIndex = 0;
		
		//find index of the score to be replaced
		while(score <= scoreTimes[newHighscoreIndex] && newHighscoreIndex < NUMBER_OF_SAVED_SCORES) {
			newHighscoreIndex++;
		}
		
		newestHighscoreIndex = newHighscoreIndex;
		insertScoreAtIndex(score, newHighscoreIndex);
		
		updateHighscoreLabels();
		
		keyListenerHighscore = new KeyListenerHighscore(this);
		addKeyListener(keyListenerHighscore);
	}
	
	
	private void insertScoreAtIndex(int score, int insertIndex) {
		int scoreIndex = scoreTimes.length - 1;
		
		
		while(scoreIndex > insertIndex && scoreIndex > 0) {
			scoreTimes[scoreIndex] = scoreTimes[scoreIndex - 1];
			
			scoreIndex--;
		}
		
		scoreTimes[insertIndex] = score;
		scoreNames[insertIndex] = "_";
	}
	

	private void initializeNewGameButton() {
		newGameButton = new JButton("Neues Spiel");
		newGameButton.setBounds(425, 400, 150, 40);
		newGameButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
					closeHighscorePanelAndStartNewGame();
			}
		});
		this.add(newGameButton);
		this.repaint();
	}
	
	
	private void updateHighscoreLabels() {
		for(int i = 0; i < NUMBER_OF_SAVED_SCORES; i++) {
			scoreLines[i].setText(convertToLabelString(scoreNames[i], scoreTimes[i]));
		}
		
		revalidate();
		repaint();
	}
	
	
	private void closeHighscorePanelAndStartNewGame() {
		endNameEntry();
		remove(newGameButton);
		
		setFocusable(false);
		setVisible(false);

		board.newGame();
	}
	
	
	public void typedLetter(String typedLetter) {
		String name = scoreNames[newestHighscoreIndex];
		
		//+1 because there is a '_' behind the name while KeyListenerHighscore is active
		if(name.length() < MAX_HIGHSCORE_NAME_LENGTH + 1) {
			name = name.substring(0, name.length() - 1)  + typedLetter + "_";
		}
		
		scoreNames[newestHighscoreIndex] = name;
		
		updateHighscoreLabels();
	}
	
	
	public void removeLastTypedLetter() {
		int nameLength = scoreNames[newestHighscoreIndex].length();
		
		if(nameLength > 1) {
			String newName = "";
			
			//-2 because there is a '_' behind the name while KeyListener is active
			newName = scoreNames[newestHighscoreIndex].substring(0, nameLength - 2);
			newName += "_";
			
			scoreNames[newestHighscoreIndex] = newName;
		}
		
		updateHighscoreLabels();
	}
	
	
	public void endNameEntry() {
		removeKeyListener(keyListenerHighscore);
		
		scoreNames[newestHighscoreIndex] 
				= scoreNames[newestHighscoreIndex].substring(0, scoreNames[newestHighscoreIndex].length() - 1);
		
		updateHighscoreLabels();
	}
}
