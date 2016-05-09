package org.teiohanson.synacor;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VaultPuzzle {
	
	static final Logger logger = LogManager.getLogger(VaultPuzzle.class);
	
	public static final Map<Point, String> vaultGrid = new HashMap<Point, String>();
	static {
		vaultGrid.put(new Point(0, 0), "22");
		vaultGrid.put(new Point(0, 1), "+");
		vaultGrid.put(new Point(0, 2), "4");
		vaultGrid.put(new Point(0, 3), "*");
		vaultGrid.put(new Point(1, 0), "-");
		vaultGrid.put(new Point(1, 1), "4");
		vaultGrid.put(new Point(1, 2), "*");
		vaultGrid.put(new Point(1, 3), "8");
		vaultGrid.put(new Point(2, 0), "9");
		vaultGrid.put(new Point(2, 1), "-");
		vaultGrid.put(new Point(2, 2), "11");
		vaultGrid.put(new Point(2, 3), "-");
		vaultGrid.put(new Point(3, 0), "*");
		vaultGrid.put(new Point(3, 1), "18");
		vaultGrid.put(new Point(3, 2), "*");
		vaultGrid.put(new Point(3, 3), "1");
	}
	
	private boolean pathFound = false;
	
	public void solve() {
		// Find the shortest possible path to the vault that will satisfy the weight requirement.
		int maxRecursion = 30;
		for(int i = 0; i <= maxRecursion && !pathFound; i++) {
			findPath(new ArrayList<Point>(), new Point(0, 0), i);
		}
	}
	
	private void findPath(List<Point> path, Point currentPosition, int maxRecursion) {	
		// Clone the path and add our current position.
		List<Point> clonedPath = new ArrayList<Point>();
		clonedPath.addAll(path);
		clonedPath.add(currentPosition);
		
		// If we've reached the vault door, terminate the recursion.
		if(currentPosition.x == 3 && currentPosition.y == 3) {
			if(calculateWeight(clonedPath) == 30) {
				printVaultPath(clonedPath);
				pathFound = true;
			}
		} else  {
			if(clonedPath.size() < maxRecursion) {
				List<Point> adjacentRooms = locateAdjacentRooms(currentPosition);
				for(Point adjacentRoom : adjacentRooms) {
					findPath(clonedPath, adjacentRoom, maxRecursion);
				}
			}
		}
	}
	
	private static int calculateWeight(List<Point> path) {
		int weight = Integer.valueOf(vaultGrid.get(path.get(0))); 	// Initial value upon picking up the orb.
		
		// Cycle through the path two points at a time applying an operator and an operand to weight each pass.
		for(int i = 1; i + 1 < path.size(); i += 2) {
			if(vaultGrid.get(path.get(i)).equalsIgnoreCase("+")) {
				weight += Integer.valueOf(vaultGrid.get(path.get(i + 1)));
			} else if(vaultGrid.get(path.get(i)).equalsIgnoreCase("-")) {
				weight -= Integer.valueOf(vaultGrid.get(path.get(i + 1)));
			} else if(vaultGrid.get(path.get(i)).equalsIgnoreCase("*")) {
				weight *= Integer.valueOf(vaultGrid.get(path.get(i + 1)));
			}
		}
		return weight;
	}
	
	private static List<Point> locateAdjacentRooms(Point currentPosition) {
		List<Point> adjacentRooms = new ArrayList<Point>();
		if(currentPosition.x - 1 >= 0) adjacentRooms.add(new Point(currentPosition.x - 1, currentPosition.y));
		if(currentPosition.x + 1 <= 3) adjacentRooms.add(new Point(currentPosition.x + 1, currentPosition.y));
		if(currentPosition.y - 1 >= 0) adjacentRooms.add(new Point(currentPosition.x, currentPosition.y - 1));
		if(currentPosition.y + 1 <= 3) adjacentRooms.add(new Point(currentPosition.x, currentPosition.y + 1));
		
		// Remove the orb room if found. The orb will reset if it loops back to it's original starting point.
		adjacentRooms.removeAll(Collections.singleton(new Point(0, 0)));
		return adjacentRooms;	
	}
	
	private static void printVaultPath(List<Point> path) {
		StringBuilder sbPath = new StringBuilder();
		StringBuilder sbMath = new StringBuilder();
		StringBuilder sbGame = new StringBuilder();
		
		Point previous = null;
		for(Point point : path) {
			sbPath.append("[" + point.x + "," + point.y + "] ");
			sbMath.append(vaultGrid.get(point) + " ");
			if(previous != null) {
				if(previous.x < point.x) {
					sbGame.append("east ");
				} else if(previous.x > point.x) {
					sbGame.append("west ");
				} else if(previous.y < point.y) {
					sbGame.append("north ");
				} else if(previous.y > point.y) {
					sbGame.append("south ");
				}
			}
			previous = point;
		}
		logger.info("Path: " + sbPath.toString());
		logger.info("Math: " + sbMath.toString());
		logger.info("Game instructions: " + sbGame.toString());
	}
}
