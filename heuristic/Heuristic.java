package heuristic;

import communicationclient.Node;
import level.Box;
import level.GoalType;
import level.Level;

import java.util.Comparator;
import java.util.HashMap;

public abstract class Heuristic implements Comparator<Node> {

	//Cache the heuristic of a node
	private HashMap<Node,Integer> heuristicMap = new HashMap<>();

	public Heuristic(Node initialState) {
		// Here's a chance to pre-process the static parts of the level.
	}
	public Heuristic() {}

	public int h(Node n) {
		Integer h = heuristicMap.get(n);
		if (h==null){
			switch (n.getGoal().getGoalType()){
				case AgentToBox:
					h = HeuristicHelper.manhattanDistance(n.agentRow,n.agentCol,n.getGoal().getRow(),n.getGoal().getCol());
					h += HeuristicHelper.goalCount(n);
					break;
				case BoxToGoal:
					for (int row = 0; row < Level.getInstance().MAX_ROW; row++) {
						for (int col = 0; col < Level.getInstance().MAX_COL; col++){
							Box box = n.boxes[row][col];
							if (box!=null){
								if(box.equals(n.getGoal().getGoalBox())){
									int boxRow = row;
									int boxCol = col;
									h = HeuristicHelper.manhattanDistance(boxRow, boxCol, n.getGoal().getRow(), n.getGoal().getCol());
									h += HeuristicHelper.goalCount(n);
									break;
								}
							}

						}
					}
			}
//			int goalCount = HeuristicHelper.goalCount(n);
//			int boxDistance = HeuristicHelper.boxDistanceToGoal(n);
//			h = Math.max(goalCount,boxDistance);
			heuristicMap.put(n,h);
		}
		return h;

	}
	public void clearMap(){
		heuristicMap.clear();
	}
	public abstract int f(Node n);

	@Override
	public int compare(Node n1, Node n2) {
		return this.f(n1) - this.f(n2);
	}

	public static class AStar extends Heuristic {
		public AStar(Node initialState) {
			super(initialState);
		}

		@Override
		public int f(Node n) {
			return n.g() + this.h(n);
		}

		@Override
		public String toString() {
			return "A* evaluation";
		}
	}

	public static class WeightedAStar extends Heuristic {
		private int W;

		public WeightedAStar(int W) {
			super();
			this.W = W;
		}

		@Override
		public int f(Node n) {
			return n.g() + this.W * this.h(n);
		}

		@Override
		public String toString() {
			return String.format("WA*(%d) evaluation", this.W);
		}
	}

	public static class Greedy extends Heuristic {
		public Greedy(Node initialState) {
			super(initialState);
		}
		public Greedy() {
			super();
		}

		@Override
		public int f(Node n) {
			return this.h(n);
		}

		@Override
		public String toString() {
			return "Greedy evaluation";
		}
	}
}
