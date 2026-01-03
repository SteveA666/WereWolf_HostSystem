package ui;

import javax.swing.JPanel;
import javax.swing.JButton;

import java.awt.Font;
import java.awt.FlowLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import core.Phase;

/**
 * Displays phase-dependent action buttons.
 * Buttons are rebuilt whenever the phase changes.
 */
public class ActionsPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	public interface ActionListenerUI {
		void onActionRequested(ActionType action);
	}

	public enum ActionType {
		GUARD,
		WOLF_HUNT,
		WITCH_HEAL,
		WITCH_POISON,
		INVESTIGATE,
		VOTE,
		DUEL,
		DEATH_ABILITY,
		END_PHASE
	}

	private ActionListenerUI listener;
	private JButton btnDuel;

	public ActionsPanel() {
		super(new FlowLayout(FlowLayout.CENTER, 12, 8));
	}

	// --------------------------------------------------
	// Public API
	// --------------------------------------------------

	public void setActionListener(ActionListenerUI listener) {
		this.listener = listener;
	}

	public void setPhase(Phase phase) {
		removeAll();
		btnDuel = null;

		if (phase == null) {
			revalidate();
			repaint();
			return;
		}

		if (phase == Phase.GUARDING) {
			addButton("Guard Player", ActionType.GUARD);
			addEndPhaseButton();
		}
		else if (phase == Phase.WOLF_HUNT) {
			addButton("Kill", ActionType.WOLF_HUNT);
			addEndPhaseButton();
		}
		else if (phase == Phase.WITCHERY) {
			addButton("Heal", ActionType.WITCH_HEAL);
			addButton("Poison", ActionType.WITCH_POISON);
			addEndPhaseButton();
		}
		else if (phase == Phase.FORTUNE_TELLING) {
			addButton("Investigate", ActionType.INVESTIGATE);
			addEndPhaseButton();
		}
		else if (phase == Phase.DISCUSSION) {
			btnDuel = addButtonReturn("Duel", ActionType.DUEL);
			addEndPhaseButton();
		}
		else if (phase == Phase.VOTING) {
			addButton("Vote", ActionType.VOTE);
			addEndPhaseButton();
		}
		else if (phase == Phase.DUEL) {
			// Duel resolution is automatic, no buttons
		}
		else if (phase == Phase.DEATH_ABILITY_TRIGGER) {
			addButton("Death Ability...", ActionType.DEATH_ABILITY);
		}
		else if (phase == Phase.ANNOUNCEMENT || phase == Phase.TRANSITION_TO_DAY) {
			addButton("Continue", ActionType.END_PHASE);
		}
		else if (phase == Phase.END_OF_GAME) {
			addButton("Game Over", ActionType.END_PHASE);
		}

		revalidate();
		repaint();
	}

	// --------------------------------------------------
	// Internal helpers
	// --------------------------------------------------

	private void addEndPhaseButton() {
		addButton("End Phase", ActionType.END_PHASE);
	}

	private void addButton(String text, ActionType type) {
		JButton btn = new JButton(text);
		btn.setFont(new Font("SansSerif", Font.BOLD, 14));
		btn.addActionListener(new ButtonHandler(type));
		add(btn);
	}

	private final class ButtonHandler implements ActionListener {

		private final ActionType type;

		private ButtonHandler(ActionType type) {
			this.type = type;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (listener != null) {
				listener.onActionRequested(type);
			}
		}
	}
	
	private JButton addButtonReturn(String text, ActionType type) {
		JButton btn = new JButton(text);
		btn.setFont(new Font("SansSerif", Font.BOLD, 14));
		btn.addActionListener(new ButtonHandler(type));
		add(btn);
		return btn;
	}
	
	public void setDuelEnabled(boolean enabled) {
		if (btnDuel != null) {
			btnDuel.setEnabled(enabled);
		}
	}

}
