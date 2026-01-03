package core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Queue;

public class GameState {
	// General Info
	private List<Player> players;
	private Phase currentPhase;
	private int round;
	private final Deque<Phase> phaseStack;
	private boolean done;

	// Night Targets
	private Player wolfTarget;
	private Player poisonTarget;
	private Player guardTarget;

	// Day Targets
	private Player voteTarget;
	private Player duelTarget;

	private Queue<Player> deathQueue;
	private ArrayList<Player> deathsThisPhase;

	// Constructor
	public GameState(List<Player> players) {
		this.players = new ArrayList<Player>(players);
		this.phaseStack = new ArrayDeque<Phase>();
		this.deathQueue = new ArrayDeque<Player>();
		this.deathsThisPhase = new ArrayList<Player>();

		this.round = 1;
		this.currentPhase = Phase.GUARDING;
		this.done = false;
	}

	// Phase Control
	public Phase getCurrentPhase() {
		return currentPhase;
	}

	public void setCurrentPhase(Phase currentPhase) {
		this.currentPhase = currentPhase;
	}

	public void interrupt(Phase interruption) {
		phaseStack.push(currentPhase);
		this.currentPhase = interruption;
	}

	public void resume() {
		if (!phaseStack.isEmpty()) {
			currentPhase = phaseStack.pop();
		}
	}

	// Round Control
	public int getRound() {
		return round;
	}

	public void nextRound() {
		round++;
		resetNightState();
	}

	// Player Control
	public List<Player> getPlayers() {
		return players;
	}

	public List<Player> getAlivePlayers() {
		List<Player> alive = new ArrayList<Player>();
		for (int i = 0; i < players.size(); i++) {
			Player p = players.get(i);
			if (p.isAlive()) {
				alive.add(p);
			}
		}
		return alive;
	}

	// Night Actions
	public void setWolfTarget(Player target) {
		wolfTarget = target;
	}

	public void setGuardedTonight(Player target) {
		guardTarget = target;
	}

	public void setPoisonedTarget(Player target) {
		poisonTarget = target;
	}

	public void sunRise() {
		deathsThisPhase.clear();

		if (wolfTarget != null && wolfTarget != guardTarget) {
			queueDeath(wolfTarget);
		}

		if (poisonTarget != null) {
			queueDeath(poisonTarget);
		}

		resetNightState();
	}
	
	public void cancelWolfKill() {
		wolfTarget = null;
	}
	
	public Player getWolfTarget() {
		return wolfTarget;
	}
	
	
	// Day Actions
	public void setVoteTarget(Player voteTarget) {
		this.voteTarget = voteTarget;
	}
	
	public void setDuelTarget(Player duelTarget) {
		this.duelTarget = duelTarget;
	}
	
	public Player getVoteTarget() {
		return voteTarget;
	}
	
	public Player getDuelTarget() {
		return duelTarget;
	}
	
	// Death Queue Related Functions
	public void queueDeath(Player p){
		if(p != null && p.isAlive()) {
			p.death();
			deathQueue.add(p);
			deathsThisPhase.add(p);
		}
		
	}
	
	public boolean hasPendingDeaths() {
        return !deathQueue.isEmpty();
    }

    public Player pollDeath() {
        return deathQueue.poll();
    }

    public List<Player> getDeathsThisPhase() {
        return Collections.unmodifiableList(deathsThisPhase);
    }

    
    // Reset Night State
	public void resetNightState() {
		wolfTarget = null;
		poisonTarget = null;
		guardTarget = null;
	}

	
	// Win Control
	public void checkWinCondition() {
		int wolfCount = 0;
		int humanCount = 0;

		for (int i = 0; i < players.size(); i++) {
			Player p = players.get(i);
			if (!p.isAlive()) {
				continue;
			}

			if (p.isWolf()) {
				wolfCount++;
			} else {
				humanCount++;
			}
		}

		if (wolfCount == 0 || wolfCount >= humanCount) {
			done = true;
		}	
	}
	
	public boolean isGameOver() {
		return done;
	}

}
