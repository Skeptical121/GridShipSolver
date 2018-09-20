package solver;
import java.text.DecimalFormat;
import java.util.*;

import ui.Component;
import ui.Listening;

public class Solver implements Runnable {
	
	public static HashMap<Long,State> strategy = new HashMap<Long,State>();
	
	
	public static State getState(Board board, int player) {
		long data = State.getData(board, player);
		if (strategy.containsKey(data)) {
			return strategy.get(data);
		} else {
			State state = new State(data);
			state.generateChoices();
			return addState(data, state);
		}
	}
	
	public static synchronized State addState(long data, State state) {
		if (strategy.containsKey(data)) {
			return strategy.get(data);
		} else {
			strategy.put(data, state);
			if (strategy.size() % 10000 == 0)
				System.out.println(strategy.size());
			return state;
		}
	}
	
	public static double bestRegret(State state) {
		double max = -100000000;
		for (Action action : state.choices) {
			max = Math.max(max, action.regret);
		}
		return max;
	}
	
	public static Board test;
	public static long timeStart;
	public static State[] initState;
	public static int SIM_SECTION = 10;
	public static int NUM_SECTIONS = 100000;
	public static void main(String[] args) {
		Board.initializeBoard();
		Component.compMain();

		

		
		timeStart = System.currentTimeMillis();
		System.out.println("Begin.");
		
		test = new Board();
		test.createStates();
		initState = new State[]{getState(test, 0), getState(test, 1)};
		for (int k = 0; k < 6; k++) {
			new Thread(new Solver(k)).start();
		}
	}
	
	public int threadId;
	public Solver(int k) {
		threadId = k;
	}
	
	public static boolean stopped = false;
	public static int y = 0;
	public void run() {
		
		for (;;y++) {
			if (stopped)
				break;
			
			learn(test, SIM_SECTION);
			if ((y + 1) % 100 == 0) {
				System.out.println("Iteration " + (y + 1) + " done at " + (System.currentTimeMillis() - timeStart) + "ms with Learning Rate: " + Action.LEARNING_RATE);
				Action.LEARNING_RATE = Math.pow(Action.LEARNING_RATE, 0.95);

				double[] EV = getEV(test, initState);
				System.out.println("EV: " + EV[0] + " / " + EV[1]);
			}
			
			if (Listening.mouseDown) {
				if (threadId == 0) {
					System.out.println("Stopped. ");
					stopped = true;
					test();
					stopped = false;
				}
			}
		}
		System.out.println("End: " + threadId);
	}
	
	public static double[] getEV(Board test, State[] initState) {
		double[] EV = new double[test.ships.length];
		for (int p = 0; p < test.ships.length; p++) {
			double[] strat = initState[p].getStrategy();
			double[] sumMax = initState[p].getStrategySum(strat);
			for (int t = 0; t < initState[p].choices.length; t++) {
				EV[p] += initState[p].choices[t].regret * Action.getRegret(strat[t], sumMax[1], initState[p].maxHits) / sumMax[0];
			}
		}
		return EV;
	}
	
	public static void learn(Board test, int numIterations) {
		for (int i = 0; i < numIterations; i++) {
			for (int x = 0; x < test.ships.length; x++) {
				Board b = new Board(test);
				b.playStrategy(x);
			}
		}
	}
	
	public static Scanner in = new Scanner(System.in);
	
	public static void printDecisions(Board b, int myPlayer, String toDo) {
		double[] strat = b.states[myPlayer].getStrategy();
		double[] sumMax = b.states[myPlayer].getStrategySum(strat);
		for (int i = 0; i < b.states[myPlayer].choices.length; i++) {
			if (toDo.equals("play")) {
				System.out.println(i + ": " + b.states[myPlayer].choices[i].toString(b.states[myPlayer]));
			} else {
				if (Action.getRegret(strat[i], sumMax[1], b.states[myPlayer].maxHits) / sumMax[0] > 0.01) {
					System.out.println(i + ": " + b.states[myPlayer].choices[i].toString(b.states[myPlayer]) + " (" + new DecimalFormat("#0.00").format(b.states[myPlayer].choices[i].regret) + ", " + new DecimalFormat("#0.00").format(Action.getRegret(strat[i], sumMax[1], b.states[myPlayer].maxHits) / sumMax[0] * 100) + "%) Hits: " + b.states[myPlayer].choices[i].numHits);
				}
			}
		}
	}
	
	public static void test() {
		
		String toDo = in.nextLine();
		
		
		
		int myPlayer = 1;
		while (true) {
			Board b = new Board();
			myPlayer = 1 - myPlayer;
			System.out.println("Player: " + myPlayer);
			boolean toBreak = false;
			while (true) {
				b.createStates();
				int choice = b.states[myPlayer].getStrategyPick();
				if (toDo.equals("next")) {
					toBreak = true;
					break;
				}
				
				printDecisions(b, myPlayer, toDo);
				
				if (toDo.equals("play")) {
					System.out.print("Your Decision: ");
				}
				Component.board = b;
				String str = in.nextLine();
				
				while (str.startsWith("sim")) {
					learn(b, Integer.parseInt(str.substring(3)));
					printDecisions(b, myPlayer, toDo);
					str = in.nextLine();
				}
				if (str != null && !str.equals("")) {
					if (str.equals("next")) {
						toBreak = true;
						break;
					}
					choice = Integer.parseInt(str);
				}
				
				System.out.println(b.states[myPlayer].choices[choice].toString(b.states[myPlayer]));
				
				Board nextBoard = new Board(b);
				Action[] actionChoices = new Action[b.ships.length];
				actionChoices[myPlayer] =  b.states[myPlayer].choices[choice];
				for (int i = 0; i < b.ships.length; i++) {
					if (i != myPlayer)
						actionChoices[i] = b.states[i].choices[b.states[i].getStrategyPick()];
				}
				
				Object next = Action.play(nextBoard, actionChoices, true);
				

				b = nextBoard;
				if (next == null) {
					System.out.println("Game over! " + b.ships[myPlayer].health + " (" + b.getResult(myPlayer) + ")");
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					break;
				}			
			}
			if (toBreak)
				break;
		}
	}
}
