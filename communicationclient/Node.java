package communicationclient;

import java.util.*;

import communicationclient.Command.Type;
import goal.Goal;
import level.Box;
import level.Color;
import level.Level;

public class Node {
	private static final Random RND = new Random(1);

	public static int MAX_ROW = Level.getInstance().MAX_ROW;
	public static int MAX_COL = Level.getInstance().MAX_COL;

	private Goal subGoal;
	public int agentRow;
	public int agentCol;

	public Color agentColor;
	public char agentId;

	public Node parent;
	public Command action;

	public ArrayList<Box> potentialBoxes = new ArrayList<>();
	public HashSet<Box> potentialBoxesAdded = new HashSet<>();

	private int g;

	private int _hash = 0;

	public Goal getSubGoal() {
		return subGoal;
	}

	public void setSubGoal(Goal subGoal) {
		this.subGoal = subGoal;
	}

	public boolean[][] walls = Level.getInstance().getWalls();

	public Box[][] boxes = new Box[MAX_ROW][MAX_COL];
	public Box boxMoved;
	public int boxMovedRow;
	public int boxMovedCol;
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

	public Node(Node parent) {
		this.parent = parent;
		if (parent == null) {
			this.g = 0;
		} else {
			this.g = parent.g() + 1;
			this.subGoal = parent.subGoal;
			this.agentColor = parent.agentColor;
		}
	}

	public Node(Goal subGoal, Agent agent) {
		this.parent = null;
		this.g = 0;
		this.subGoal = subGoal;
		this.agentColor = agent.getColor();
		this.agentId = agent.getId();
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
		return subGoal.isGoalSatisfied(this);
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
						n.boxMoved = n.boxes[newBoxRow][newBoxCol];
						n.boxMovedRow = newBoxRow;
						n.boxMovedCol = newBoxCol;
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
						n.boxMoved = n.boxes[this.agentRow][this.agentCol];
						n.boxMovedRow = this.agentRow;
						n.boxMovedCol = this.agentCol;
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
		return !this.walls[row][col] && this.boxes[row][col] == null;//TODO Maybe do something here about agent is stuck
	}

	private boolean boxAt(int row, int col) {
		Box box = this.boxes[row][col];
		if(box!=null){
			if( box.getBoxColor() == agentColor){
				return  true;
			}else{
				if(!potentialBoxesAdded.contains(box)){
					potentialBoxes.add(box);
					potentialBoxesAdded.add(box);
				}

			}
		}

		return false;
	}

	private Node ChildNode() {
		Node copy = new Node(this);
		copy.potentialBoxes = this.potentialBoxes;
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

	public ArrayList<Box> getPotentialBoxes() {
		return potentialBoxes;
	}

	public int getAgentRow() {
		return agentRow;
	}

	public int getAgentCol() {
		return agentCol;
	}

	public Box[][] getBoxes() {
		return boxes;
	}


	@Override
	public int hashCode() {
		if (this._hash == 0) {
			final int prime = 31;
			int result = 1;
			result = prime * result + this.agentCol;
			result = prime * result + this.agentRow;
			result = prime * result + this.agentColor.ordinal();//Agent color not in hashCode as well
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
