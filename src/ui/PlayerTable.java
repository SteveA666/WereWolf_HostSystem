package ui;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.BorderFactory;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Font;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Player;

/**
 * Displays players as buttons arranged around a square table.
 * Alive players are clickable; dead players are gray and disabled.
 *
 * IMPORTANT:
 * - Only Player.isAlive() is checked.
 * - Players queued for death but not yet processed remain selectable.
 */
public class PlayerTable extends JPanel {

	private static final long serialVersionUID = 1L;

	public interface SelectionListener {
		void onPlayerSelected(Player player);
	}

	private final List<Player> players;
	private final Map<Player, JButton> buttonMap;
	
	private List<Player> highlightedPlayers;
	private Map<Player, Color> highlightColors;

	private SelectionListener listener;

	private Player selectedPlayer;

	public PlayerTable(List<Player> players) {
		this.players = new ArrayList<Player>(players);
		this.buttonMap = new HashMap<Player, JButton>();

		this.selectedPlayer = null;
		this.highlightedPlayers = new ArrayList<Player>();
		this.highlightColors = new HashMap<Player, Color>();

		this.setLayout(new GridBagLayout());
		this.setBorder(BorderFactory.createTitledBorder("Players"));

		buildTable();
		refresh();
	}

	// --------------------------------------------------
	// Public API
	// --------------------------------------------------

	public void setSelectionListener(SelectionListener listener) {
		this.listener = listener;
	}

	public Player getSelectedPlayer() {
		return selectedPlayer;
	}

	public void clearSelection() {
		selectedPlayer = null;
		updateSelectionStyles();
	}

	/**
	 * Refresh method, called whenever game state changes.
	 */
	public void refresh() {
		for (int i = 0; i < players.size(); i++) {
			Player p = players.get(i);
			JButton btn = buttonMap.get(p);

			if (btn == null) {
				continue;
			}

			if (p.isAlive()) {
				btn.setEnabled(true);
				btn.setBackground(new Color(220, 220, 220));
			} else {
				btn.setEnabled(false);
				btn.setBackground(Color.GRAY);
			}
		}

		updateSelectionStyles();
	}

	// --------------------------------------------------
	// Layout logic
	// --------------------------------------------------

	private void buildTable() {
		int n = players.size();

		if (n == 0) {
			return;
		}

		List<int[]> positions = computeSquarePositions(n);
		
		if (positions.size() < players.size()) {
		    throw new IllegalStateException(
		        "Not enough table positions: have " +
		        positions.size() + ", need " + players.size()
		    );
		}

		for (int i = 0; i < players.size(); i++) {
			Player p = players.get(i);
			JButton btn = createPlayerButton(p);

			int[] pos = positions.get(i);

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = pos[0];
			gbc.gridy = pos[1];
			gbc.insets = new Insets(8, 8, 8, 8);

			this.add(btn, gbc);
			buttonMap.put(p, btn);
		}
	}

	/**
	 * Places players around the perimeter of a square grid.
	 */
	private List<int[]> computeSquarePositions(int count) {
		List<int[]> result = new ArrayList<int[]>();

		int side = 2;
		while (4 * (side - 1) < count) {
		    side++;
		}

		// Top side
		for (int i = 0; i < side && result.size() < count; i++) {
			result.add(new int[] { i, 0 });
		}

		// Right side
		for (int i = 1; i < side && result.size() < count; i++) {
			result.add(new int[] { side - 1, i });
		}

		// Bottom side
		for (int i = side - 2; i >= 0 && result.size() < count; i--) {
			result.add(new int[] { i, side - 1 });
		}

		// Left side
		for (int i = side - 2; i > 0 && result.size() < count; i--) {
			result.add(new int[] { 0, i });
		}

		return result;
	}

	// --------------------------------------------------
	// Button creation & selection
	// --------------------------------------------------

	private JButton createPlayerButton(Player player) {
		JButton btn = new JButton(player.name);
		btn.setPreferredSize(new Dimension(120, 40));
		btn.setFont(new Font("SansSerif", Font.BOLD, 12));
		btn.setFocusPainted(false);

		btn.addActionListener(new PlayerButtonListener(player));

		return btn;
	}

	private void updateSelectionStyles() {
		for (int i = 0; i < players.size(); i++) {
			Player p = players.get(i);
			JButton btn = buttonMap.get(p);

			if (btn == null) {
				continue;
			}

			// Set highlighted player to red
			if (!p.isAlive()) {
				btn.setBackground(Color.GRAY);
			}
			else if (highlightColors.containsKey(p)) {
				btn.setBackground(highlightColors.get(p));
			}
			else if (p == selectedPlayer) {
				btn.setBackground(new Color(255, 200, 120));
			}
			else {
				btn.setBackground(new Color(220, 220, 220));
			}

		}
	}
	
	// Set and clear player highlights
	public void setHighlights(Map<Player, Color> highlights) {
		highlightColors.clear();
		if (highlights != null) {
			highlightColors.putAll(highlights);
		}
		updateSelectionStyles();
	}

	public void clearHighlights() {
		highlightColors.clear();
		updateSelectionStyles();
	}



	// --------------------------------------------------
	// Listener
	// --------------------------------------------------

	private final class PlayerButtonListener implements ActionListener {

		private final Player player;

		private PlayerButtonListener(Player player) {
			this.player = player;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (!player.isAlive()) {
				return;
			}

			selectedPlayer = player;
			updateSelectionStyles();

			if (listener != null) {
				listener.onPlayerSelected(player);
			}
		}
	}
}
