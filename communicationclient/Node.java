package communicationclient;

import java.util.*;

import communicationclient.Command.Type;
import level.Box;
import level.Color;
import level.Goal;
import level.Level;

public class Node {
	private static final Random RND = new Random(1);

	public static int MAX_ROW = Level.getInstance().MAX_ROW;
	public static int MAX_COL = Level.getInstance().MAX_COL;
	private Goal goal;
	public int agentRow;
	public int agentCol;
	public Color agentColor;

	public Goal getGoal() {
		return goal;
	}

	public void setGoal(Goal goal) {
		this.goal = goal;
	}
	// Arrays are indexed from the top-left of the level, with first index being row and second being column.
	// Row 0: (0,0) (0,1) (0,2) (0,3) ...
	// Row 1: (1,0) (1,1) (1,2) (1,3) ...
	// Row 2: (2,0) (2,1) (2,2) (2,3) ...
	// ...
	// (Start in the top left corner, first go down, then go right)
	// E.g. this.walls[2] is an array of booleans having size MAX_COL.
	// this.walls[row][col] is true if there's a wall at (row, col)
	//

	public boolean[][] walls = Level.getInstance().getWalls();
	public Box[][] boxes = new Box[MAX_ROW][MAX_COL];

	public Box[][] getBoxesCopy() {
		Box[][] copy = new Box[MAX_ROW][MAX_COL];
		for (int row = 0; row < MAX_ROW; row++) {
			System.arraycopy(this.boxes[row], 0, copy[row], 0, MAX_COL);
		}
		return copy;
	}

	public void setBoxes(Box[][] boxes) {
		this.boxes = boxes;
	}

	public Node parent;
	public Command action;

	private int g;

	private int _hash = 0;

	public Node(Node parent) {
		this.parent = parent;
		if (parent == null) {
			this.g = 0;
		} else {
			this.g = parent.g() + 1;
			this.goal = parent.goal;
			this.agentColor = parent.agentColor;
		}
	}

	public Node(Goal goal, Color agentColor) {
		this.parent = null;
		this.goal = goal;
		this.g = 0;
		this.agentColor = agentColor;
	}

	public int g() {
		return this.g;
	}

	public boolean isInitialState() {
		return this.parent == null;
	}
	public void addBox(Box box){
		this.boxes[box.getRow()][box.getCol()] = box;
	}

	public boolean isGoalState() {
		switch (goal.getGoalType()) {
			case BoxToGoal:
				char goalChar = goal.getGoalChar();
				Box box = boxes[goal.getRow()][goal.getCol()];
				if (box!=null){
					char b = Character.toLowerCase(box.getBoxChar());
					if (b == goalChar) {
						return true;
					}
				}
				break;
			case AgentToBox:
				if((Math.abs(agentRow-goal.getRow()) == 1 && Math.abs(agentCol-goal.getCol())==0) || (Math.abs(agentCol-goal.getCol())==1 && Math.abs(agentRow-goal.getRow())==0)) return true;
				break;
		}
		return false;
	}

	public ArrayList<Node> getExpandedNodes() {
		ArrayList<Node> expandedNodes = new ArrayList<Node>(Command.EVERY.length);
		for (Command c : Command.EVERY) {
			// Determine applicability of action
			int newAgentRow = this.agentRow + Command.dirToRowChange(c.dir1);
			int newAgentCol = this.agentCol + Command.dirToColChange(c.dir1);

			if (c.actionType == Type.Move) {
				// Check if there's a wall or box on the cell to which the agent is moving
				if (this.cellIsFree(newAgentRow, newAgentCol)) {
					Node n = this.ChildNode();
					n.action = c;
					n.agentRow = newAgentRow;
					n.agentCol = newAgentCol;
					expandedNodes.add(n);
				}
			} else if (c.actionType == Type.Push) {
				// Make sure that there's actually a box to move
				if (this.boxAt(newAgentRow, newAgentCol)) {
					int newBoxRow = newAgentRow + Command.dirToRowChange(c.dir2);
					int newBoxCol = newAgentCol + Command.dirToColChange(c.dir2);
					// .. and that new cell of box is free
					if (this.cellIsFree(newBoxRow, newBoxCol)) {
						Node n = this.ChildNode();
						n.action = c;
						n.agentRow = newAgentRow;
						n.agentCol = newAgentCol;
						n.boxes[newBoxRow][newBoxCol] = this.boxes[newAgentRow][newAgentCol];
						n.boxes[newAgentRow][newAgentCol] = null;
						expandedNodes.add(n);
					}
				}
			} else if (c.actionType == Type.Pull) {
				// Cell is free where agent is going
				if (this.cellIsFree(newAgentRow, newAgentCol)) {
					int boxRow = this.agentRow + Command.dirToRowChange(c.dir2);
					int boxCol = this.agentCol + Command.dirToColChange(c.dir2);
					// .. and there's a box in "dir2" of the agent
					if (this.boxAt(boxRow, boxCol)) {
						Node n = this.ChildNode();
						n.action = c;
						n.agentRow = newAgentRow;
						n.agentCol = newAgentCol;
						n.boxes[this.agentRow][this.agentCol] = this.boxes[boxRow][boxCol];
						n.boxes[boxRow][boxCol] = null;
						expandedNodes.add(n);
					}
				}
			}
		}
		Collections.shuffle(expandedNodes, RND);
		return expandedNodes;
	}

	private boolean cellIsFree(int row, int col) {
		return !this.walls[row][col] && this.boxes[row][col] == null;
	}

	private boolean boxAt(int row, int col) {
		Box box = this.boxes[row][col];
		if(box!=null){
			return box.getBoxColor() == agentColor;
		}
		return false;
	}

	private Node ChildNode() {
		Node copy = new Node(this);
		for (int row = 0; row < MAX_ROW; row++) {
			System.arraycopy(this.boxes[row], 0, copy.boxes[row], 0, MAX_COL);
		}
		return copy;
	}

	public LinkedList<Node> extractPlan() {
		LinkedList<Node> plan = new LinkedList<Node>();
		Node n = this;
		while (!n.isInitialState()) {
			plan.addFirst(n);
			n = n.parent;
		}
		return plan;
	}

	@Override
	public int hashCode() {
		if (this._hash == 0) {
			final int prime = 31;
			int result = 1;
			result = prime * result + this.agentCol;
			result = prime * result + this.agentRow;
			result = prime * result + Arrays.deepHashCode(this.boxes);
			result = prime * result + Arrays.deepHashCode(this.walls);
			this._hash = result;
		}
		return this._hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (this.agentRow != other.agentRow || this.agentCol != other.agentCol)
			return false;
		if (!Arrays.deepEquals(this.boxes, other.boxes))
			return false;
		if (!Arrays.deepEquals(this.walls, other.walls))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (int row = 0; row < MAX_ROW; row++) {
			if (!this.walls[row][0]) {
				break;
			}
			for (int col = 0; col < MAX_COL; col++) {

				if (this.boxes[row][col] !=null ) {
					s.append(this.boxes[row][col].getBoxChar());
				} else if (this.walls[row][col]) {
					s.append("+");
				}else if(row == goal.getRow() && col == goal.getCol()){
						s.append(goal.getGoalChar());
				} else if (row == this.agentRow && col == this.agentCol) {
					s.append("0");
				} else {
					s.append(" ");
				}
			}
			s.append("\n");
		}
		return s.toString();
	}

}