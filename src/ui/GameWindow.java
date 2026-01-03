package ui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Font;

import java.util.List;

import core.Phase;
import core.Player;

public class GameWindow extends JFrame {

	private static final long serialVersionUID = 1L;

	// Root container
	private JPanel rootPanel;

	// Init screen
	private InitPanel initPanel;
	
	// Player Table
	private PlayerTable playerTable;
	
	// Game Controller
	private GameController controller;

	// Game screen components
	private JPanel gamePanel;
	private JLabel phaseLabel;
	private LogPanel logArea;
	private ActionsPanel actionPanel;

	public GameWindow() {
		super("WereWolf");

		// Window basics
		this.setSize(1280, 720);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);

		rootPanel = new JPanel(new BorderLayout());
		this.setContentPane(rootPanel);

		buildInitPanel();
	}

	// --------------------------------------------------
	// Init Panel
	// --------------------------------------------------

	private void buildInitPanel() {
		initPanel = new InitPanel();
		initPanel.setInitListener(new InitListenerImpl());

		rootPanel.removeAll();
		rootPanel.add(initPanel, BorderLayout.CENTER);

		revalidate();
		repaint();
	}

	// --------------------------------------------------
	// Game Panel
	// --------------------------------------------------

	private void buildGamePanel() {
		gamePanel = new JPanel(new BorderLayout());

		buildTopPanel();
		buildCenterPanel();
		buildBottomPanel();

		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(logArea, BorderLayout.CENTER);
		centerPanel.add(playerTable, BorderLayout.EAST);

		gamePanel.add(buildTopWrapper(), BorderLayout.NORTH);
		gamePanel.add(centerPanel, BorderLayout.CENTER);
		gamePanel.add(actionPanel, BorderLayout.SOUTH);

		rootPanel.removeAll();
		rootPanel.add(gamePanel, BorderLayout.CENTER);

		revalidate();
		repaint();
	}

	private JPanel buildTopWrapper() {
		JPanel topPanel = new JPanel();
		topPanel.add(phaseLabel);
		return topPanel;
	}

	private void buildTopPanel() {
		phaseLabel = new JLabel("Phase: (not started)");
		phaseLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
	}

	private void buildCenterPanel() {
		logArea = new LogPanel();
	}

	private void buildBottomPanel() {
		actionPanel = new ActionsPanel();
	}

	
	// Public UI hooks

	public void setPhaseText(String text) {
		if (phaseLabel != null) {
			phaseLabel.setText("Phase: " + text);
		}
	}

	public void log(String message) {
		if (logArea != null) {
			logArea.log(message);
		}
	}

	public ActionsPanel getActionPanel() {
		return actionPanel;
	}

	// Init listener implementation

	private final class InitListenerImpl implements InitPanel.InitListener {

		@Override
		public void onGameInitialized(List<Player> players) {

			playerTable = new PlayerTable(players);
			buildGamePanel();
			
			controller = new GameController(GameWindow.this, players);

			log("Game initialized with " + players.size() + " players.");
		}
	}
	
	public PlayerTable getPlayerTable() {
		return playerTable;
	}
}
