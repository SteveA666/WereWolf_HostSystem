package ui;

import core.ActionController;
import core.GameState;
import core.Phase;
import core.Player;
import core.Role;
import core.Team;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;


public class GameController implements ActionsPanel.ActionListenerUI, PlayerTable.SelectionListener {

	private final GameWindow window;
	private final GameState state;
	private final ActionController actions;

	private Phase currentPhase;
	private Player selectedPlayer;
	private Player pendingDeathAbilityUser;
	private Phase resumePhase;
	
	private static final Color WOLF_COLOR = new Color(200, 50, 50);
	private static final Color GUARD_COLOR = new Color(70, 130, 180);
	private static final Color WITCH_COLOR = new Color(138, 43, 226);
	private static final Color FT_COLOR = new Color(60, 179, 113);
	private static final Color KNIGHT_COLOR = new Color(255, 140, 0);



	public GameController(GameWindow window, List<Player> players) {
		this.window = window;

		this.state = new GameState(players);
		this.actions = new ActionController(state);

		this.currentPhase = Phase.GUARDING;
		this.selectedPlayer = null;

		wireUI();
		startGame();
	}

	
	// Wiring

	private void wireUI() {
		window.getActionPanel().setActionListener(this);
		window.getPlayerTable().setSelectionListener(this);
	}

	private void startGame() {
		updatePhase(Phase.GUARDING);
		window.log("Night falls. Guard, wake up.");
	}

	
	// Phase control

	private void updatePhase(Phase next) {
		this.currentPhase = next;
		state.setCurrentPhase(next);

		window.setPhaseText(next.name());
		window.getActionPanel().setPhase(next);
		
		if (next == Phase.DISCUSSION) {
			boolean hasKnight = hasAliveRole(Role.KNIGHT);
			window.getActionPanel().setDuelEnabled(hasKnight);
		}

		window.getPlayerTable().clearHighlights();

		if (next == Phase.WOLF_HUNT) {
			window.getPlayerTable().setHighlights(getAliveWolves());
		}
		else if (next == Phase.GUARDING) {
			window.getPlayerTable().setHighlights(
				getAliveRole(Role.GUARD, GUARD_COLOR)
			);
		}
		else if (next == Phase.WITCHERY) {
			window.getPlayerTable().setHighlights(
				getAliveRole(Role.WITCH, WITCH_COLOR)
			);
		}
		else if (next == Phase.FORTUNE_TELLING) {
			window.getPlayerTable().setHighlights(
				getAliveRole(Role.FORTUNE_TELLER, FT_COLOR)
			);
		}
		else if (next == Phase.DUEL) {
			window.getPlayerTable().setHighlights(
				getAliveRole(Role.KNIGHT, KNIGHT_COLOR)
			);
		}




		selectedPlayer = null;
		window.getPlayerTable().clearSelection();
	}

	private void advancePhase() {
		if (currentPhase == Phase.GUARDING) {
			updatePhase(Phase.WOLF_HUNT);
			window.log("Wolves, choose a target.");
		}
		else if (currentPhase == Phase.WOLF_HUNT) {
			updatePhase(Phase.WITCHERY);
			window.log("Witch, act if you wish.");
			showWolfTargetToWitch();
		}
		else if (currentPhase == Phase.WITCHERY) {
			updatePhase(Phase.FORTUNE_TELLING);
			window.log("Fortune Teller, investigate.");
		}
		else if (currentPhase == Phase.FORTUNE_TELLING) {
			state.sunRise();
			resolveDeaths();
			updatePhase(Phase.DISCUSSION);
			window.log("Day breaks. Discuss.");
		}
		else if (currentPhase == Phase.DISCUSSION) {
			updatePhase(Phase.VOTING);
			window.log("Vote to eliminate.");
		}
		else if (currentPhase == Phase.VOTING) {
			Player target = state.getVoteTarget();
			if (target != null) {
				state.queueDeath(target);
			}
			resolveDeaths();
			state.checkWinCondition();

			if (state.isGameOver()) {
				updatePhase(Phase.END_OF_GAME);
				window.log("Game over.");
			} else {
				state.nextRound();
				updatePhase(Phase.GUARDING);
				window.log("Night falls again. Guard, wake up.");
			}
		}
	}

	
	
	// Death resolution

	private void resolveDeaths() {
		while (state.hasPendingDeaths()) {
			Player dead = state.pollDeath();
			window.log(dead.name + " has died.");

			if (dead.getRole() == Role.HUNTER || dead.getRole() == Role.ALPHA_WOLF) {
				enterDeathAbilityPhase(dead);
				return; // pause resolution until ability is used
			}
		}


		window.getPlayerTable().refresh();
	}

	
	
	// UI callbacks
	
	@Override
	public void onPlayerSelected(Player player) {
		this.selectedPlayer = player;
	}

	@Override
	public void onActionRequested(ActionsPanel.ActionType action) {

		if (action == ActionsPanel.ActionType.END_PHASE) {
			advancePhase();
			return;
		}

		if (action == ActionsPanel.ActionType.FORCE_ALIVE) {
			forcePlayerAliveStatus(true);
			return;
		}

		if (action == ActionsPanel.ActionType.FORCE_DEAD) {
			forcePlayerAliveStatus(false);
			return;
		}

		if (selectedPlayer == null) {
			window.log("No player selected.");
			return;
		}

		boolean success = false;

		if (action == ActionsPanel.ActionType.GUARD) {
			success = actions.guard(findSelf(Role.GUARD), selectedPlayer);
		}
		else if (action == ActionsPanel.ActionType.WOLF_HUNT) {
			success = actions.wolf_hunt(selectedPlayer);
		}
		else if (action == ActionsPanel.ActionType.WITCH_HEAL) {
			success = actions.witch_potion(findSelf(Role.WITCH), selectedPlayer, true);
		}
		else if (action == ActionsPanel.ActionType.WITCH_POISON) {
			success = actions.witch_potion(findSelf(Role.WITCH), selectedPlayer, false);
		}
		else if (action == ActionsPanel.ActionType.INVESTIGATE) {
			Team result = actions.investigate(
					findSelf(Role.FORTUNE_TELLER),
					selectedPlayer
				);

				if (result != null) {
					showInvestigationResult(selectedPlayer, result);
					success = true;
				} else {
					success = false;
				}
		}
		else if (action == ActionsPanel.ActionType.VOTE) {
			success = actions.vote(selectedPlayer);
		}
		else if (action == ActionsPanel.ActionType.DUEL) {
			success = actions.duel(findSelf(Role.KNIGHT), selectedPlayer);
		}
		else if (action == ActionsPanel.ActionType.DEATH_ABILITY) {

			if (pendingDeathAbilityUser == null) {
				window.log("No death ability available.");
				return;
			}

			if (selectedPlayer == null) {
				window.log("Select a target for the death ability.");
				return;
			}

			actions.triggerDeathAbility(pendingDeathAbilityUser, selectedPlayer);

			pendingDeathAbilityUser = null;

			// Resolve any new deaths caused by the ability
			resolveDeaths();

			// Resume previous phase
			updatePhase(resumePhase);
		}


		if (!success) {
			window.log("Action failed.");
		}
	}

	// *******
	// Helpers
	// *******

	private Player findSelf(Role role) {
		List<Player> players = state.getPlayers();
		for (int i = 0; i < players.size(); i++) {
			Player p = players.get(i);
			if (p.isAlive() && p.getRole() == role) {
				return p;
			}
		}
		return null;
	}
	
	private void showInvestigationResult(Player target, Team result) {

		String message;

		if (result == Team.WLF) {
			message = target.name + " is a WOLF.";
		} else {
			message = target.name + " is NOT a wolf.";
		}

		JOptionPane.showMessageDialog(
			window,
			message,
			"Investigation Result",
			JOptionPane.INFORMATION_MESSAGE
		);
	}
	
	private void showWolfTargetToWitch() {

		Player wolfTarget = state.getWolfTarget();

		if (wolfTarget == null) {
			JOptionPane.showMessageDialog(
				window,
				"The wolves did not choose a target tonight.",
				"Witch Information",
				JOptionPane.INFORMATION_MESSAGE
			);
			return;
		}

		JOptionPane.showMessageDialog(
			window,
			"The death today:\n\n" + wolfTarget.name,
			"Witch Information",
			JOptionPane.INFORMATION_MESSAGE
		);
	}
	
	private boolean hasAliveRole(Role role) {
		List<Player> players = state.getPlayers();
		for (int i = 0; i < players.size(); i++) {
			Player p = players.get(i);
			if (p.isAlive() && p.getRole() == role) {
				return true;
			}
		}
		return false;
	}
	
	private List<Player> getAlivePlayersWithRole(Role role) {
		List<Player> result = new ArrayList<Player>();
		List<Player> players = state.getPlayers();

		for (int i = 0; i < players.size(); i++) {
			Player p = players.get(i);
			if (p.isAlive() && p.getRole() == role) {
				result.add(p);
			}
		}

		return result;
	}
	
	private Map<Player, Color> getAliveWolves() {
		Map<Player, Color> result = new HashMap<Player, Color>();
		List<Player> players = state.getPlayers();

		for (int i = 0; i < players.size(); i++) {
			Player p = players.get(i);
			if (p.isAlive() && p.isWolf()) {
				result.put(p, WOLF_COLOR);
			}
		}
		return result;
	}
	
	private Map<Player, Color> getAliveRole(Role role, Color color) {
		Map<Player, Color> result = new HashMap<Player, Color>();
		List<Player> players = state.getPlayers();

		for (int i = 0; i < players.size(); i++) {
			Player p = players.get(i);
			if (p.isAlive() && p.getRole() == role) {
				result.put(p, color);
			}
		}
		return result;
	}



	
	private void enterDeathAbilityPhase(Player dead) {
		pendingDeathAbilityUser = dead;
		resumePhase = currentPhase;

		updatePhase(Phase.DEATH_ABILITY_TRIGGER);
		window.log(dead.name + " may use their death ability.");
	}

	private void forcePlayerAliveStatus(boolean alive) {
		Player target = promptForPlayer(
			alive ? "Force Alive" : "Force Dead",
			alive ? "Select a player to revive." : "Select a player to mark dead."
		);

		if (target == null) {
			window.log("No player selected.");
			return;
		}

		if (alive) {
			if (target.isAlive()) {
				window.log(target.name + " is already alive.");
			} else {
				state.revivePlayer(target);
				window.log(target.name + " was forced alive.");
			}
		} else {
			if (!target.isAlive()) {
				window.log(target.name + " is already dead.");
			} else {
				target.death();
				window.log(target.name + " was forced dead.");
			}
		}

		selectedPlayer = null;
		window.getPlayerTable().clearSelection();
		window.getPlayerTable().refresh();
	}

	private Player promptForPlayer(String title, String message) {
		List<Player> players = state.getPlayers();
		if (players.isEmpty()) {
			return null;
		}

		return (Player) JOptionPane.showInputDialog(
			window,
			message,
			title,
			JOptionPane.QUESTION_MESSAGE,
			null,
			players.toArray(new Player[0]),
			players.get(0)
		);
	}




}
