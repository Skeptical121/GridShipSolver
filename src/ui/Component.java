package ui;
import javax.imageio.*;
import javax.swing.*;

import solver.Board;
import solver.Ship;
import solver.Solver;

import java.awt.*;
import java.awt.image.*;
import java.io.*;

public class Component extends JPanel implements Runnable {
	private static final long serialVersionUID = 1L;
	public static boolean isRunning = false;
	int ticksPerSecond = 60;
	public static Point screenPos = new Point(0, 0);
	public static Point mousePos = new Point(0, 0);
	public Image screen;


	public static int screenW = 690;//630;
	public static int screenH = screenW - 40;

	public static Board board;

	public Component() {
		setPreferredSize(new Dimension(screenW + 170, screenH));
		addMouseListener(new Listening());
		addMouseWheelListener(new Listening());

	}


	public void start() {

		try {
			Ship.img = ImageIO.read(new File("res/Ship.png"));
			Board.rock = ImageIO.read(new File("res/Rock.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		board = new Board();
		
		isRunning = true;
		new Thread(this).start();
	}

	public void stop() {
		isRunning = false;
		Solver.stopped = false;
	}

	public static void compMain() {
		Component component = new Component();
		JFrame frame = new JFrame();

		component.setLayout(null);


		frame.add(component);
		frame.setTitle("Ship Battle");
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		component.start();
	}

	public void render() {
		((VolatileImage) screen).validate(getGraphicsConfiguration());
		
		Graphics g = screen.getGraphics();
		screenPos = getLocationOnScreen();
		mousePos = getMousePosition();
		
		if (mousePos != null && Listening.mouseDown) {
			

		}
		
		
		//draw:
		g.setColor(new Color(210, 210, 225));
		g.fillRect(0, 0, screenW, screenH);

		board.render((Graphics2D)g);

		g = getGraphics();
		g.drawImage(screen, 0, 0, screenW, screenH, 0, 0, screenW, screenH, null);
		g.dispose();
	}

	public void run() {
		screen = createVolatileImage(screenW, screenH);
		long lastTime = System.nanoTime();
		double unprocessed = 0;
		double nsPerTick = 1000000000.0 / ticksPerSecond;
		while (isRunning) {
			long now = System.nanoTime();
			unprocessed += (now - lastTime) / nsPerTick;
			lastTime = now;
			while (unprocessed >= 1) {
				unprocessed -= 1;
			}
			{
				render();
				if (unprocessed < 1) {
					try {
						// So X / 10^6 ms + X % 10^6 ns:
						Thread.sleep((int) ((1 - unprocessed) * nsPerTick) / 1000000, (int) ((1 - unprocessed) * nsPerTick) % 1000000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
