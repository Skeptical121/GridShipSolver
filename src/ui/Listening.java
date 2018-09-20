package ui;

import java.awt.event.*;
import java.awt.*;

public class Listening implements MouseListener, MouseWheelListener {
	public static boolean mouseDown = false;
	public static Point pressLocation = new Point(0, 0);
	public static Point currLocation = new Point(0, 0);
	public static Point lastLocation = new Point(0, 0);

	public static int convertTo = 1;
	
	public void mouseWheelMoved(MouseWheelEvent e) {
		
	}

	public void mouseClicked(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {
		
	}

	public void mousePressed(MouseEvent e) {
		pressLocation = e.getPoint();
		
		int x = (int) pressLocation.getX();
		int y = (int) pressLocation.getY();
		
		if (e.getButton() >= 1 && e.getButton() <= 5) {
			mouseDown = true;
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (e.getButton() >= 1 && e.getButton() <= 5) {
			mouseDown = false;
		}
	}
}
