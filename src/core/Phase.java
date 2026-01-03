package core;

public enum Phase {
	// Night
	GUARDING,
	WOLF_HUNT,
	WITCHERY,
	FORTUNE_TELLING,
	
	// Day
	TRANSITION_TO_DAY,
	ANNOUNCEMENT,
	DISCUSSION,
	VOTING,
	
	// Can trigger any time during the day
	DUEL,
	DEATH_ABILITY_TRIGGER,
	
	END_OF_GAME;

}
