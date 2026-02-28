package q1_search;

import java.util.*;

public class SearchAlgorithms {

    // --- 1. BREADTH-FIRST SEARCH (BFS) ---
    public static void runBFS(Node root, State goalState) {
        long startTime = System.currentTimeMillis();
        Queue<Node> frontier = new LinkedList<>();
        Set<State> visited = new HashSet<>();

        frontier.add(root);

        int nodesExplored = 0;

        while (!frontier.isEmpty()) {
            Node current = frontier.poll();
            visited.add(current.state);
            nodesExplored++;

            // Goal Check
            if (current.state.equals(goalState)) {
                printSuccess("BFS", current, nodesExplored, startTime);
                return;
            }

            // Expand Children
            for (Node child : MainQ1.getSuccessors(current)) {
                if (!visited.contains(child.state)) {
                    // Quick optimization: Check if child is in frontier (skipped for brevity in BFS)
                    frontier.add(child);
                    // Add to visited immediately to prevent duplicate processing in BFS
                    visited.add(child.state);
                }
            }
        }
        System.out.println("BFS Failed to find a solution.");
    }

    // --- 2. DEPTH-FIRST SEARCH (DFS) ---
    public static void runDFS(Node root, State goalState) {
        long startTime = System.currentTimeMillis();
        Stack<Node> frontier = new Stack<>();
        Set<State> visited = new HashSet<>();

        frontier.push(root);
        int nodesExplored = 0;

        while (!frontier.isEmpty()) {
            Node current = frontier.pop();

            // Goal Check
            if (current.state.equals(goalState)) {
                printSuccess("DFS", current, nodesExplored, startTime);
                return;
            }

            if (!visited.contains(current.state)) {
                visited.add(current.state);
                nodesExplored++;

                // Expand Children
                for (Node child : MainQ1.getSuccessors(current)) {
                    if (!visited.contains(child.state)) {
                        frontier.push(child);
                    }
                }
            }
        }
        System.out.println("DFS Failed to find a solution.");
    }

    // --- 3. A* SEARCH ---
    public static void runAStar(Node root, State goalState, int heuristicType) {
        long startTime = System.currentTimeMillis();
        PriorityQueue<Node> frontier = new PriorityQueue<>();
        Set<State> visited = new HashSet<>();

        // Calculate initial heuristic
        root.heuristic = calculateHeuristic(root.state, goalState, heuristicType);
        frontier.add(root);
        int nodesExplored = 0;

        while (!frontier.isEmpty()) {
            Node current = frontier.poll();

            if (current.state.equals(goalState)) {
                String hName = (heuristicType == 1) ? "h1 (Misplaced)" : "h2 (Manhattan)";
                printSuccess("A* with " + hName, current, nodesExplored, startTime);
                return;
            }

            if (!visited.contains(current.state)) {
                visited.add(current.state);
                nodesExplored++;

                for (Node child : MainQ1.getSuccessors(current)) {
                    if (!visited.contains(child.state)) {
                        child.heuristic = calculateHeuristic(child.state, goalState, heuristicType);
                        frontier.add(child);
                    }
                }
            }
        }
        System.out.println("A* Failed to find a solution.");
    }

    // --- HEURISTICS ---
    private static int calculateHeuristic(State current, State goal, int type) {
        if (type == 1) {
            // h1: Misplaced Manuscripts
            int misplaced = 0;
            for (int i = 0; i < 9; i++) {
                if (current.board[i] != 0 && current.board[i] != goal.board[i]) {
                    misplaced++;
                }
            }
            return misplaced;
        } else {
            // h2: Manhattan Distance
            int distance = 0;
            for (int i = 0; i < 9; i++) {
                int value = current.board[i];
                if (value != 0) {
                    // Find where this value SHOULD be in the goal state
                    int goalIndex = -1;
                    for (int j = 0; j < 9; j++) {
                        if (goal.board[j] == value) {
                            goalIndex = j;
                            break;
                        }
                    }
                    // Convert 1D index to 2D coordinates: x = index % 3, y = index / 3
                    int currentX = i % 3, currentY = i / 3;
                    int goalX = goalIndex % 3, goalY = goalIndex / 3;
                    distance += Math.abs(currentX - goalX) + Math.abs(currentY - goalY);
                }
            }
            return distance;
        }
    }

    // --- 4. ITERATIVE DEEPENING A* (IDA*) ---
    public static void runIDAStar(Node root, State goalState, int heuristicType) {
        long startTime = System.currentTimeMillis();
        int bound = calculateHeuristic(root.state, goalState, heuristicType);

        while (true) {
            // We use an array to pass the goal node back out of the recursive function
            Node[] resultNode = new Node[1];
            int t = searchIDA(root, goalState, 0, bound, heuristicType, resultNode);

            if (t == -1) {
                printSuccess("IDA*", resultNode[0], -1, startTime); // -1 for explored since IDA* doesn't track it easily
                return;
            }
            if (t == Integer.MAX_VALUE) {
                System.out.println("IDA* Failed to find a solution.");
                return;
            }
            bound = t; // Increase the depth bound for the next iteration
        }
    }

    private static int searchIDA(Node node, State goal, int g, int bound, int hType, Node[] result) {
        int f = g + calculateHeuristic(node.state, goal, hType);
        if (f > bound) return f;
        if (node.state.equals(goal)) {
            result[0] = node;
            return -1; // -1 acts as our "Found" flag
        }
        int min = Integer.MAX_VALUE;
        for (Node child : MainQ1.getSuccessors(node)) {
            child.pathCost = g + 1;
            int t = searchIDA(child, goal, g + 1, bound, hType, result);
            if (t == -1) return -1;
            if (t < min) min = t;
        }
        return min;
    }

    // --- 5. SIMULATED ANNEALING ---
    public static void runSimulatedAnnealing(Node root, State goalState) {
        long startTime = System.currentTimeMillis();
        Node current = root;
        double temperature = 1000.0;
        double coolingRate = 0.99;
        int explored = 0;

        while (temperature > 0.1) {
            if (current.state.equals(goalState)) {
                printSuccess("Simulated Annealing", current, explored, startTime);
                return;
            }

            // Get random neighbor
            List<Node> neighbors = MainQ1.getSuccessors(current);
            Node next = neighbors.get((int) (Math.random() * neighbors.size()));
            explored++;

            // We use Negative Manhattan Distance as our "Energy" (we want to maximize it to 0)
            int currentE = -calculateHeuristic(current.state, goalState, 2);
            int nextE = -calculateHeuristic(next.state, goalState, 2);
            int deltaE = nextE - currentE;

            if (deltaE > 0) {
                current = next; // Accept the better state
            } else {
                // Accept worse state with some probability based on Temperature
                double probability = Math.exp(deltaE / temperature);
                if (Math.random() <= probability) {
                    current = next;
                }
            }
            temperature *= coolingRate; // Cool down the system
        }
        System.out.println("\n=== SIMULATED ANNEALING RESULTS ===");
        System.out.println("Status: FAILED (System cooled down before reaching the goal).");
    }

    // --- HELPER: PRINT RESULTS ---
    private static void printSuccess(String algoName, Node goalNode, int explored, long startTime) {
        long timeTaken = System.currentTimeMillis() - startTime;

        // Backtrack to find the path
        List<String> path = new ArrayList<>();
        Node current = goalNode;
        while (current.parent != null) {
            path.add(current.action);
            current = current.parent;
        }
        Collections.reverse(path); // Reverse because we traced from goal to root

        System.out.println("\n=== " + algoName + " RESULTS ===");
        System.out.println("Status: SUCCESS");
        System.out.println("Path Length: " + path.size() + " moves (System Energy: " + goalNode.pathCost + ")");
        System.out.println("Total States Explored: " + explored);
        System.out.println("Time Taken: " + timeTaken + " ms");
        System.out.println("Optimal/Sub-Optimal Path: " + String.join(" -> ", path));
    }
}