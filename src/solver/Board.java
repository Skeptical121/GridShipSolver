package solver;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;


public class Board {
	public static final int TILE_SIZE = 50;
	
	public static final int WIDTH = 9;
	public static final int HEIGHT = 9;
	public static final int BONUS_MIN = 3;
	public static final int BONUS_MAX = 6;
	public static int[][] tiles = new int[WIDTH][HEIGHT];
	public static BufferedImage rock;
	
	public Ship[] ships;
	public State[] states;
	
	public static final int ROUNDS = 3;
	byte roundsLeft;
	
	public Board() {
		ships = new Ship[]{new Ship(5, 4, (byte)1), new Ship(7, 1, (byte)2)};
		roundsLeft = ROUNDS;
	}
	
	public static void initializeBoard() {
		Random r = new Random(75);
		for (int x = 0; x < WIDTH; x++) {
			for (int y = 0; y < HEIGHT; y++) {
				if (r.nextInt(20) == 0) {
					tiles[x][y] = 1;
				}
			}
		}
	}
	
	public Board(Board board) {
		roundsLeft = board.roundsLeft;
		ships = new Ship[board.ships.length];
		for (int i = 0; i < ships.length; i++) {
			ships[i] = new Ship(board.ships[i]);
		}
	}
	
	public void createStates() {
		states = new State[ships.length];
		for (int i = 0; i < ships.length; i++) {
			states[i] = Solver.getState(this, i);
		}
	}
	
	public void render(Graphics2D g) {
		for (int x = 0; x < WIDTH; x++) {
			for (int y = 0; y < HEIGHT; y++) {
				if (x >= BONUS_MIN && x <= BONUS_MAX && y >= BONUS_MIN && y <= BONUS_MAX) {
					if ((x + y) % 2 == 0) {
						g.setColor(new Color(255, 148, 128));
					} else {
						g.setColor(new Color(235, 198, 128));
					}
				} else {
					if ((x + y) % 2 == 0) {
						g.setColor(new Color(128, 148, 255));
					} else {
						g.setColor(new Color(128, 198, 235));
					}
				}
				g.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
				if (tiles[x][y] == 1) {
					g.drawImage(rock, x * TILE_SIZE, y * TILE_SIZE, null);
				}
			}
		}
		
		for (Ship ship : ships) {
			AffineTransform original = g.getTransform();
			g.rotate(-ship.dir * Math.PI / 2, ship.x * TILE_SIZE + TILE_SIZE / 2, ship.y * TILE_SIZE + TILE_SIZE / 2);
			g.drawImage(Ship.img, ship.x * TILE_SIZE, ship.y * TILE_SIZE, null);
			g.setTransform(original);
		}
		
	}
	
	public double getResult(int regretPlayer) {
		int val = ships[regretPlayer].health;
		for (int x = 0; x < ships.length; x++) {
			if (x != regretPlayer) {
				val -= ships[x].health;
			}
		}
		return val;
	}
	
	// Returns the value by taking the given strategy for 1 particular iteration
	public double playStrategy(int regretPlayer) {
		int[] choice = new int[ships.length];
		Action[] actionChoices = new Action[ships.length];
		createStates();
		for (int i = 0; i < ships.length; i++) {
			choice[i] = states[i].getStrategyPick();
			actionChoices[i] = states[i].choices[choice[i]];
		}
		
		Board nextBoard = new Board(this);
		Object cont = Action.play(nextBoard, actionChoices, false);
		
		double actualResult = 0;
		if (cont instanceof Board) {
			actualResult = ((Board)cont).playStrategy(regretPlayer);
		} else if (cont == null) {
			actualResult = nextBoard.getResult(regretPlayer);
		} else {
			System.out.println("Not a possibility");
		}

		double supposedMax = states[regretPlayer].choices[choice[regretPlayer]].regret;
		for (int i = 0; i < states[regretPlayer].choices.length; i++) {
			double regretResult = 0;
			if (i != choice[regretPlayer]) {
				nextBoard = new Board(this);
				// At least some amount of the time we calculate regret anyways
				if (states[regretPlayer].choices[i].regret < supposedMax && Math.random() - Action.MINIMUM_REGRET_CALCULATION > Action.getRegret(states[regretPlayer].choices[i].regret, supposedMax, states[regretPlayer].maxHits) * Action.CALCULATE_REGRET_MULTIPLIER) {
					// No regret calculation..
					continue;
				}
				if (Math.random() < Action.IGNORE_REGRET_CALCULATION) {
					continue; // Sometimes, we don't do regret calculation. This is because of stalemates
				}
				actionChoices[regretPlayer] = states[regretPlayer].choices[i];
				cont = Action.play(nextBoard, actionChoices, false);
				if (cont instanceof Board) {
					regretResult = ((Board)cont).playStrategy(regretPlayer);
				} else if (cont == null) {
					regretResult = nextBoard.getResult(regretPlayer);
				} else {
					System.out.println("Not a possibility (Regret) " + cont);
				}
			} else {
				regretResult = actualResult;
			}
			
			states[regretPlayer].choices[i].addHit(regretResult);
			if (states[regretPlayer].choices[i].numHits > states[regretPlayer].maxHits) {
				states[regretPlayer].maxHits = states[regretPlayer].choices[i].numHits;
			}
		}
		
		return actualResult;
	}
}
