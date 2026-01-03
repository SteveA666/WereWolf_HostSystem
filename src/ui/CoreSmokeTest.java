package ui;

import java.util.ArrayList;
import java.util.List;

import core.GameState;
import core.Phase;
import core.Player;
import core.Role;
import core.Team;
import core.ActionController;

public final class CoreSmokeTest {

	private CoreSmokeTest() {
		
	}

	public static void run() {
		System.out.println("=== Core Smoke Test ===");

		Player guard = new Player("Gina", Role.GUARD);
		Player witch = new Player("Willow", Role.WITCH);
		Player seer = new Player("Felix", Role.FORTUNE_TELLER);
		Player wolf = new Player("Wolfy", Role.WEREWOLF);
		Player villager = new Player("Charlie", Role.CIVILIAN);

		List<Player> players = new ArrayList<Player>();
		players.add(guard);
		players.add(witch);
		players.add(seer);
		players.add(wolf);
		players.add(villager);

		GameState state = new GameState(players); // <-- adjust to match your GameState
		ActionController actions = new ActionController(state);

		state.setCurrentPhase(Phase.GUARDING);
		boolean guardOk = actions.guard(guard, villager);
		System.out.println("Guard action ok? " + guardOk);

		state.setCurrentPhase(Phase.WOLF_HUNT);
		boolean huntOk = actions.wolf_hunt(villager);
		System.out.println("Wolf hunt ok? " + huntOk);

		state.setCurrentPhase(Phase.WITCHERY);
		boolean poisonOk = actions.witch_potion(witch, wolf, false);
		System.out.println("Witch poison ok? " + poisonOk + " (should be false on night 1 if poisonCD starts at 1)");

		state.setCurrentPhase(Phase.FORTUNE_TELLING);
		Team seenTeam = actions.investigate(seer, wolf);
		System.out.println("Seer sees wolf as: " + seenTeam + " (expect " + Team.WLF + ")");

		state.sunRise();

		System.out.println("Deaths this phase: " + state.getDeathsThisPhase());
		System.out.println("Villager alive after guarded wolf attack? " + villager.isAlive() + " (expect true)");

		witch.poisonRecharge();

		state.setCurrentPhase(Phase.WITCHERY);
		boolean poisonOk2 = actions.witch_potion(witch, wolf, false);
		System.out.println("Witch poison ok on night 2? " + poisonOk2 + " (expect true)");

		state.sunRise();
		System.out.println("Deaths after poison: " + state.getDeathsThisPhase());
		System.out.println("Wolf alive? " + wolf.isAlive() + " (expect false if poison was applied)");

		state.checkWinCondition();
		System.out.println("Game over? " + state.isGameOver() + " (expect true if wolf died and only humans remain)");

		System.out.println("=== Core Smoke Test done ===");
	}
}
