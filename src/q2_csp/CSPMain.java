package q2_csp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CSPMain {
    // Core CSP Structures
    static List<String> variables = new ArrayList<>(); // Slot1, Slot2, etc.
    static List<String> allBots = new ArrayList<>();   // A, B, C
    static Map<String, List<String>> domains = new HashMap<>();

    public static void main(String[] args) {
        // 1. Read input and set up initial domains
        parseInput("input.txt");

        if (variables.isEmpty() || allBots.isEmpty()) {
            System.out.println("Error parsing input.txt. Make sure the Bots and Slots lines exist.");
            return;
        }

        System.out.println("=== CSP BOT SCHEDULING ===");
        System.out.println("Variables (Time Slots): " + variables);
        System.out.println("Initial Domains (after Unary Maintenance constraint): " + domains);
        System.out.println("Heuristic Used: Minimum Remaining Values (MRV)");
        System.out.println("Inference Used: Forward Checking\n");

        // 2. Run the Backtracking Search
        Map<String, String> assignment = new LinkedHashMap<>();
        boolean success = backtrack(assignment, domains, 1);

        // 3. Print Final Output
        System.out.println("\n=== FINAL RESULT ===");
        if (success) {
            System.out.println("Status: SUCCESS");
            System.out.println("Final Assignment: " + assignment);
        } else {
            System.out.println("Status: FAILURE (No valid schedule found)");
        }
    }

    // --- BACKTRACKING SEARCH ALGORITHM ---
    static boolean backtrack(Map<String, String> assignment, Map<String, List<String>> currentDomains, int step) {
        // Base Case: If all variables are assigned
        if (assignment.size() == variables.size()) {
            // Check the Global Constraint: Minimum Coverage (Every bot used at least once)
            Set<String> usedBots = new HashSet<>(assignment.values());
            if (usedBots.size() == allBots.size()) {
                return true; // We found a completely valid schedule!
            }
            System.out.println("  [!] Global Constraint Failed: Not all bots were used. Backtracking...");
            return false;
        }

        // 1. Heuristic: Select unassigned variable using MRV
        String var = selectMRV(assignment, currentDomains);
        System.out.println("Step " + step + ": MRV selected variable -> " + var + " (Domain: " + currentDomains.get(var) + ")");

        for (String value : currentDomains.get(var)) {
            System.out.println("  Trying assignment: " + var + " = Bot " + value);

            // Check Binary Constraint (No Back-to-Back)
            if (isConsistent(var, value, assignment)) {
                assignment.put(var, value); // Assign the bot

                // 2. Inference: Apply Forward Checking
                Map<String, List<String>> newDomains = applyForwardChecking(var, value, currentDomains, assignment);

                if (newDomains != null) { // If Forward Checking didn't result in an empty domain
                    boolean result = backtrack(assignment, newDomains, step + 1);
                    if (result) return true;
                }

                // Backtrack if the branch failed
                assignment.remove(var);
                System.out.println("  [!] Backtracking from " + var + " = Bot " + value);
            } else {
                System.out.println("  [!] Constraint violated for " + var + " = Bot " + value + " (Back-to-Back rule)");
            }
        }
        return false;
    }

    // --- MINIMUM REMAINING VALUES (MRV) ---
    static String selectMRV(Map<String, String> assignment, Map<String, List<String>> currentDomains) {
        String bestVar = null;
        int minSize = Integer.MAX_VALUE;
        for (String v : variables) {
            if (!assignment.containsKey(v)) {
                int size = currentDomains.get(v).size();
                if (size < minSize) {
                    minSize = size;
                    bestVar = v;
                }
            }
        }
        return bestVar;
    }

    // --- CHECK BINARY CONSTRAINTS ---
    static boolean isConsistent(String var, String value, Map<String, String> assignment) {
        int index = variables.indexOf(var);
        // Check the slot immediately BEFORE this one
        if (index > 0) {
            String prevVar = variables.get(index - 1);
            if (value.equals(assignment.get(prevVar))) return false;
        }
        // Check the slot immediately AFTER this one
        if (index < variables.size() - 1) {
            String nextVar = variables.get(index + 1);
            if (value.equals(assignment.get(nextVar))) return false;
        }
        return true;
    }

    // --- FORWARD CHECKING INFERENCE ---
    static Map<String, List<String>> applyForwardChecking(String assignedVar, String assignedValue,
                                                          Map<String, List<String>> currentDomains,
                                                          Map<String, String> assignment) {
        // Create a deep copy of the domains so we don't accidentally corrupt previous states during backtracking
        Map<String, List<String>> newDomains = new HashMap<>();
        for (String k : currentDomains.keySet()) {
            newDomains.put(k, new ArrayList<>(currentDomains.get(k)));
        }

        int index = variables.indexOf(assignedVar);

        // Find neighboring slots (consecutive constraints)
        String[] neighbors = new String[2];
        if (index > 0) neighbors[0] = variables.get(index - 1);
        if (index < variables.size() - 1) neighbors[1] = variables.get(index + 1);

        for (String neighbor : neighbors) {
            if (neighbor != null && !assignment.containsKey(neighbor)) {
                // Apply the "No Back-to-Back" constraint to the neighbor's domain
                newDomains.get(neighbor).remove(assignedValue);
                System.out.println("    Inference (Forward Checking): Removed Bot '" + assignedValue + "' from domain of " + neighbor);

                // If a domain becomes empty, this assignment is doomed. Fail early!
                if (newDomains.get(neighbor).isEmpty()) {
                    System.out.println("    [!] Forward Checking detected domain wipeout for " + neighbor + ". Pruning this branch.");
                    return null;
                }
            }
        }
        return newDomains;
    }

    // --- INPUT PARSER ---
    static void parseInput(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Bots:")) {
                    String[] b = line.substring(5).trim().split(",\\s*");
                    allBots.addAll(Arrays.asList(b));
                } else if (line.startsWith("Slots:")) {
                    String[] s = line.substring(6).trim().split(",\\s*");
                    variables.addAll(Arrays.asList(s));
                    // Initialize all domains to have all bots initially
                    for (String var : variables) {
                        domains.put(var, new ArrayList<>(allBots));
                    }
                } else if (line.startsWith("Maintenance:")) {
                    // Apply Unary Constraint (e.g., "Slot4 != C")
                    String[] parts = line.substring(12).trim().split("!=");
                    if (parts.length == 2) {
                        String var = parts[0].trim();
                        String val = parts[1].trim();
                        if (domains.containsKey(var)) {
                            domains.get(var).remove(val);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading " + filename);
        }
    }
}