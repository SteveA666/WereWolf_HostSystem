package core;

import java.util.Objects;

public class Player {
	
	public final String name;
	private boolean alive;
	private final Role role;
	private final Team affiliation;
	
	private Player guardedTarget; 
	private int healCD = 0;
	private int poisonCD = 1;
	
	public Player(String name, Role role) {
		this.name = Objects.requireNonNull(name, "A name is needed to continue");
		this.role = Objects.requireNonNull(role, "A role is needed to continue");
		this.affiliation = role.getDefaultTeam();
		this.alive = true;
		this.guardedTarget = null;
		this.healCD = 0;
		this.poisonCD = 1;
	}
	
	@Override
	public String toString() {
		return name + (alive ? "" : "(dead)");
	}
	
	public void death() {
		alive = false;
	}
	
	public void resurrect() {
		alive = true;
	}
	
	public void setGuardedTarget(Player guardedTarget) {
		this.guardedTarget = guardedTarget;
	}
	
	public boolean canGuard(Player target) {
		return (target != guardedTarget) && (target.alive) && (target != null);
	}
	
	public void healUsed() {
		healCD = 2;
	}
	
	public void poisonUsed() {
		poisonCD = 2;
	}
	
	public void healRecharge() { healCD--;}
	
	public void poisonRecharge() {poisonCD--;}
	
	public boolean healAvail() { return healCD <= 0; }
	
	public boolean PoisonAvail() { return poisonCD <= 0;}

	public boolean isAlive() {
		return this.alive;
	}

	public boolean isWolf() {
		return (this.affiliation == Team.WLF);
	}
	
	public Role getRole() {
		return role;
	}

}
