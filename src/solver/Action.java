package solver;
import ui.Component;



public class Action {

	public static final int NUM_MOVES = 2;
	byte[] action;
	
	int numHits = 0; // numHits can easily go over 32767
	
	
	
	double regret; // High regret means a good action. This is in actual units. Probably useful to be accurate
	
	public static final double REGRET_DROP_OFF = 0.5; // "new" regret drop off
	public static final double NUM_HITS_EXPONENT = 0.333;
	public static final double IGNORE_REGRET_CALCULATION = 0; // This makes the tree smaller.. helps for earlier EV calculations
	public static final double MINIMUM_REGRET_CALCULATION = 0.02; // We really don't need to calculate things that are 0 EV very much
	public static final double CALCULATE_REGRET_MULTIPLIER = 10; // Anything 10% or higher always gets calculated for
	public static double LEARNING_RATE = 0.8; // Higher # means slower learning- 
	
	public Action(byte[] action) {
		this.action = action;
	}
	
	public static double getRegret(double regret, double max, int maxHits) {
		return Math.exp(REGRET_DROP_OFF * Math.pow(maxHits, NUM_HITS_EXPONENT) * (regret - max));
	}
	
	public void addHit(double newRegret) {
		numHits++;
		if (numHits == 1) {
			regret = newRegret;
		} else {
			regret = (newRegret + regret * (Math.pow(numHits, LEARNING_RATE) - 1)) / Math.pow(numHits, LEARNING_RATE);
		}
	}
	
	// Returns the states that will be if continuing is what should happen
	public static Object play(Board board, Action[] actions, boolean runningForReal) {
		for (int x = 0; x < NUM_MOVES; x++) {
			for (int i = 0; i < actions.length; i++) {
				board.ships[i].move(board.ships, actions[i].action[x]);
			}
			for (int i = 0; i < actions.length; i++) {
				// 1 other ship:
				board.ships[i].attemptShoot(board.ships, actions[i].action[x]);
			}
			if (runningForReal) {
				Component.board = board;
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		for (int i = 0; i < actions.length; i++) {
			if (board.ships[i].x >= Board.BONUS_MIN && board.ships[i].x <= Board.BONUS_MAX && board.ships[i].y >= Board.BONUS_MIN && board.ships[i].y <= Board.BONUS_MAX) {
				if (board.ships[i].health < Ship.MAX_HEALTH)
					board.ships[i].health++;
			}
		}
		
		board.roundsLeft--;
		if (board.roundsLeft <= 0) {
			return null;
		}
		for (int i = 0; i < actions.length; i++) {
			if (board.ships[i].health <= 0) {
				board.ships[i].health = 0;
				return null;
			}
		}
		
		
		
		return board;
	}
	
	public String toString(State state) {
		String s = "";
		for (int x = 0; x < NUM_MOVES; x++) {
			if (action[x] == 0) {
				s += "Wait";
			} else if (action[x] == 1) {
				s += "Forward";
			} else if (action[x] == 2) {
				s += "Left";
			} else if (action[x] == 3) {
				s += "Right";
			}
			s += " ";
		}
		return s;
	}
}
