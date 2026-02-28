package q1_search;

public class Node implements Comparable<Node> {
    public State state;
    public Node parent;
    public String action; // "Up", "Down", "Left", "Right"
    public int pathCost;  // g(n): System Energy spent so far
    public int heuristic; // h(n): Estimated cost to goal

    // Constructor for the Root Node
    public Node(State state) {
        this.state = state;
        this.parent = null;
        this.action = "Start";
        this.pathCost = 0;
        this.heuristic = 0;
    }

    // Constructor for Child Nodes
    public Node(State state, Node parent, String action, int stepCost) {
        this.state = state;
        this.parent = parent;
        this.action = action;
        this.pathCost = parent.pathCost + stepCost;
        this.heuristic = 0;
    }

    // f(n) = g(n) + h(n)
    public int getFCost() {
        return pathCost + heuristic;
    }

    // This allows PriorityQueue to automatically sort nodes by lowest f(n) for A*
    @Override
    public int compareTo(Node other) {
        return Integer.compare(this.getFCost(), other.getFCost());
    }
}