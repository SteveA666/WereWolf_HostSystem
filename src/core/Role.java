package core;

public enum Role {
	WEREWOLF(Team.WLF),
	ALPHA_WOLF(Team.WLF),
	CIVILIAN(Team.HUM),
	FORTUNE_TELLER(Team.HUM),
	WITCH(Team.HUM),
	HUNTER(Team.HUM),
	KNIGHT(Team.HUM),
	GUARD(Team.HUM);

	private final Team defaultTeam;
	
	Role(Team t) {
		this.defaultTeam = t;
	}
	
	public Team getDefaultTeam() {
		return this.defaultTeam;
	}
}
