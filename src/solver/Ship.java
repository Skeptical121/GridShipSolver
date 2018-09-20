package solver;
import java.awt.image.BufferedImage;


public class Ship {
	byte dir; // 0 = right, 1 = up, 2 = left, 3 = down
	int x;
	int y;
	int health;
	
	public static BufferedImage img;
	
	public static int[] xMove = {1, 0, -1, 0};
	public static int[] yMove = {0, -1, 0, 1};
	
	public static final int MAX_HEALTH = 20;
	public static final int MAX_CANNON_BALLS = 5;
	
	public Ship(int x, int y, byte dir) {
		this.x = x;
		this.y = y;
		this.dir = dir;
		this.health = MAX_HEALTH / 2;
	}
	
	public Ship(Ship ship) {
		this.dir = ship.dir;
		this.x = ship.x;
		this.y = ship.y;
		this.health = ship.health;
	}
	
	public void move(Ship[] other, short action) {
		if (action == 0) {
			// Nothing
		} else if (action == 1) {
			// Go straight
			moveForward(other);
		} else if (action == 2) {
			// Go left
			moveForward(other);
			// Turn:
			dir = (byte)((dir + 1) % 4);
			moveForward(other);
		} else if (action == 3) {
			// Go right
			moveForward(other);
			dir = (byte)((dir + 3) % 4);
			moveForward(other);
		}
	}
	
	public void attemptShoot(Ship[] others, byte action) {
		
		for (Ship other : others) {
			if (this != other) {
				if (((dir == 0 || dir == 2) && x == other.x && Math.abs(y - other.y) <= 3) || ((dir == 1 || dir == 3) && y == other.y && Math.abs(x - other.x) <= 3)) {
					if (action == 0) {
						other.health -= 3; // Strong cannonball
					} else {
						other.health--; // Weak cannonball
					}
				}
			}
		}
	}
	
	public void moveForward(Ship[] others) {
		int newX = x + xMove[dir];
		int newY = y + yMove[dir];
		if (newX >= 0 && newY >= 0 && newX < Board.WIDTH && newY < Board.HEIGHT) {
			if (Board.tiles[newX][newY] != 1) {
				for (Ship other : others) {
					if (this != other) {
						if (other.x == newX && other.y == newY) {
							health--;
							return;
						}
					}
				}
				x = newX;
				y = newY;
			} else {
				health--;
			}
		}
	}
	
	public String toString() {
		return x + " " + y + " " + dir + " " + health;
	}
}
