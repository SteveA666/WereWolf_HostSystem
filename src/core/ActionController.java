package core;

import java.util.Objects;

public class ActionController {
	
	private final GameState state;
	
	public ActionController(GameState state) {
		// Ensure a valid Game State class is passed
		this.state = Objects.requireNonNull(state, "Cannot create ActionController without a valid GameState");
	}
	
	
	// Actions, returning false means that the action fails
	
	
	// Night Actions
	
	// Guard guarding a player
	public boolean guard(Player guard, Player target) {
		if(guard == null || target == null) { return false; }
		if(state.getCurrentPhase() != Phase.GUARDING) {return false;}
		if(!guard.isAlive() || guard.getRole() != Role.GUARD) {return false;}
		if(!guard.canGuard(target)) {return false;}
		
		guard.setGuardedTarget(target);
		state.setGuardedTonight(target);
		return true;
	}
	
	// Wolves choose a target to kill
	public boolean wolf_hunt(Player target) {
		if(state.getCurrentPhase() != Phase.WOLF_HUNT) { return false; }
		if(target == null || !target.isAlive()) { return false; }
		
		state.setWolfTarget(target);
		return true;
	}
	
	// Witch uses a potion
	public boolean witch_potion(Player witch, Player target, boolean isHealing) {
		if(witch == null || target == null) { return false; }
		if(state.getCurrentPhase() != Phase.WITCHERY) { return false; }
		if(!witch.isAlive() || witch.getRole() != Role.WITCH) { return false; }
		
		if (isHealing) {
			if (!witch.healAvail()) { return false; }

			// Witch can only heal the wolf target
			Player wolfTarget = state.getWolfTarget();
			if (wolfTarget == null || wolfTarget != target) {
				return false;
			}

			witch.healUsed();
			state.cancelWolfKill(); 
			return true;
		}else {
			if(!witch.PoisonAvail() || !target.isAlive()) { return false; }
			state.setPoisonedTarget(target);
			witch.poisonUsed();
			return true;
		}
		
	}
	
	// Fortune teller investigates, returning null means investigation failed
	public Team investigate(Player ft, Player target) {
		if(ft == null || target == null) { return null; }
		if(state.getCurrentPhase() != Phase.FORTUNE_TELLING) { return null; }
		if(!ft.isAlive() || ft.getRole() != Role.FORTUNE_TELLER) { return null; }
		return target.isWolf() ? Team.WLF : Team.HUM;
	}
	
	
	// Day Actions
	
	// Voting
	public boolean vote(Player target) {
		if(state.getCurrentPhase() != Phase.VOTING) { return false; }
		if(target == null || !target.isAlive()) { return false; }
		
		state.setVoteTarget(target);
		return true;
	}
	
	public boolean duel(Player knight, Player target) {
		if(knight == null || target == null) { return false; }
		if(!knight.isAlive() || !target.isAlive() ) { return false; }
		if(state.getCurrentPhase() != Phase.DISCUSSION) { return false; }
		
		state.interrupt(Phase.DUEL);
		
		if(target.isWolf()) { state.queueDeath(target); } else { state.queueDeath(knight); }
		
		return true;
	}
	
	public void triggerDeathAbility(Player deadPlayer, Player chosenTarget) {
		if (deadPlayer == null || chosenTarget == null) { return; }

		if (!chosenTarget.isAlive()) { return; }

		Role role = deadPlayer.getRole();

		if (role == Role.ALPHA_WOLF || role == Role.HUNTER) {
			state.interrupt(Phase.DEATH_ABILITY_TRIGGER);
			state.queueDeath(chosenTarget);
		}
	}

}
