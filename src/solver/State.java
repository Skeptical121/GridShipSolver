package solver;
import java.util.*;


public class State {
	
	long data;
	
	Action[] choices;
	int maxHits = 0;
	
	public State(long data) {
		this.data = data;
	}
	
	public static long getData(Board board, int s) {
		long val = board.ships[s].x + board.ships[s].y * Board.WIDTH + board.ships[s].dir * Board.WIDTH * Board.HEIGHT + board.roundsLeft * Board.WIDTH * Board.HEIGHT * 4;
		
		long mult = Board.WIDTH * Board.HEIGHT * 4 * Board.ROUNDS;
		for (int x = 0; x < board.ships.length; x++) {
			if (x != s) {
				val += board.ships[x].x * mult + board.ships[x].y * mult * Board.WIDTH + board.ships[x].dir * mult * Board.WIDTH * Board.HEIGHT;
				mult = mult * Board.WIDTH * Board.HEIGHT * 4;
			}
		}
		return val;
	}
	
	public void generateChoices() {
		this.choices = new Action[(int)Math.pow(4, Action.NUM_MOVES)];
		byte[] vals = new byte[Action.NUM_MOVES];
		for (int i = 0; i < vals.length; i++) {
			vals[i] = 0;
		}
		int x = 0;
		do {
			this.choices[x++] = new Action(vals);
			vals = Arrays.copyOf(vals, vals.length);
		} while (iterate(vals, Action.NUM_MOVES - 1));
	}
	
	public static boolean iterate(byte[] vals, int i) {
		if (i == -1)
			return false;
		vals[i]++;
		if (vals[i] == 4) {
			vals[i] = 0;
			return iterate(vals, i - 1);
		} else {
			return true;
		}
	}
	
	public double[] getStrategySum(double[] choices) {
		double max = choices[0];
		for (int i = 1; i < choices.length; i++) {
			if (choices[i] > max) {
				max = choices[i];
			}
		}
		double sum = 0;
		for (int i = 0; i < choices.length; i++) {
			sum += Action.getRegret(choices[i], max, maxHits);
		}
		return new double[]{sum, max};
	}
	
	public double[] getStrategy() {
		double[] strat = new double[choices.length];
		for (int i = 0; i < choices.length; i++) {
			strat[i] = choices[i].regret;
		}
		return strat;
	}
	
	public int getStrategyPick() {
		// Sum is 1:
		double total = 0;
		double rand = Math.random();
		
		double[] strat = getStrategy();
		double[] sumMax = getStrategySum(strat);
		
		for (int i = 0; i < choices.length; i++) {
			total += Action.getRegret(strat[i], sumMax[1], maxHits);
			
			if (rand < total / sumMax[0]) {
				return i;
			}
		}
		return (int)(Math.random() * choices.length); // else random choice
	}
	
	public long longHashCode() {
		return data;
	}
}
