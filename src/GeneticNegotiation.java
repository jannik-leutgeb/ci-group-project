import java.util.*;
import java.util.concurrent.*;

public class GeneticNegotiation {
    // GA parameters
    private static final int POPULATION_SIZE = 50;
    private static final int GENERATIONS = 1_000_000; // 10 million generations
    private static final double CROSSOVER_RATE = 0.8;
    private static final double MUTATION_RATE = 0.2;
    private static final int TOURNAMENT_SIZE = 3;

    public static void main(String[] args) {
        String[] inSu200 = new String[4];
        String[] inCu200 = new String[4];
        inSu200[0] = "data/daten3ASupplier_200.txt";
        inSu200[1] = "data/daten3BSupplier_200.txt";
        inSu200[2] = "data/daten4ASupplier_200.txt";
        inSu200[3] = "data/daten4BSupplier_200.txt";
        inCu200[0] = "data/daten3ACustomer_200_10.txt";
        inCu200[1] = "data/daten3BCustomer_200_20.txt";
        inCu200[2] = "data/daten4ACustomer_200_5.txt";
        inCu200[3] = "data/daten4BCustomer_200_5.txt";

        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<?>> futures = new ArrayList<>();
        ConcurrentHashMap<String, Result> results = new ConcurrentHashMap<>();

        for (int i = 0; i < inSu200.length; i++) {
            for (int j = 0; j < inCu200.length; j++) {
                final int idxI = i;
                final int idxJ = j;
                futures.add(executor.submit(() -> runInstance(idxI, idxJ, inSu200[idxI], inCu200[idxJ], results)));
            }
        }
        // Wait for all tasks to finish
        for (Future<?> f : futures) {
            try { f.get(); } catch (Exception e) { e.printStackTrace(); }
        }
        executor.shutdown();

        // Print all results together
        System.out.println("\n==== Best Results for All Instances ====");
        results.keySet().stream().sorted().forEach(key -> {
            Result r = results.get(key);
            System.out.print("Instance " + key + ": Supplier: ");
            r.supplier.print(r.contract);
            System.out.print("  Customer: ");
            r.customer.print(r.contract);
            System.out.println("  Total Fitness: " + r.fitness);
        });
    }

    private static class Individual {
        int[] contract;
        int supplierCost;
        int customerCost;
        double crowdingDistance;
        Individual(int[] contract, int supplierCost, int customerCost) {
            this.contract = contract;
            this.supplierCost = supplierCost;
            this.customerCost = customerCost;
            this.crowdingDistance = 0.0;
        }
    }

    private static void runInstance(int i, int j, String supplierFile, String customerFile, ConcurrentHashMap<String, Result> results) {
        System.out.println("Instance: " + i + " " + j);
        try {
            SupplierAgent supplier = new SupplierAgent(new java.io.File(supplierFile));
            CustomerAgent customer = new CustomerAgent(new java.io.File(customerFile));
            Mediator mediator = new Mediator(supplier.getContractSize(), customer.getContractSize());
            int contractSize = supplier.getContractSize();

            // Initialize population
            List<Individual> population = new ArrayList<>();
            for (int k = 0; k < POPULATION_SIZE; k++) {
                int[] contract = mediator.initContract();
                population.add(new Individual(contract, supplier.evaluate(contract), customer.evaluate(contract)));
            }

            Individual bestIndividual = null;

            for (int gen = 0; gen < GENERATIONS; gen++) {
                List<Individual> newPopulation = new ArrayList<>();
                while (newPopulation.size() < POPULATION_SIZE) {
                    Individual parent1 = paretoTournamentSelect(population);
                    Individual parent2 = paretoTournamentSelect(population);
                    int[] child1 = Arrays.copyOf(parent1.contract, contractSize);
                    int[] child2 = Arrays.copyOf(parent2.contract, contractSize);

                    // Crossover
                    if (Math.random() < CROSSOVER_RATE) {
                        int[][] children = onePointCrossover(child1, child2);
                        child1 = children[0];
                        child2 = children[1];
                    }

                    // Mutation
                    if (Math.random() < MUTATION_RATE) {
                        child1 = mediator.constructProposal(randomStrategy(), child1);
                    }
                    if (Math.random() < MUTATION_RATE) {
                        child2 = mediator.constructProposal(randomStrategy(), child2);
                    }

                    newPopulation.add(new Individual(child1, supplier.evaluate(child1), customer.evaluate(child1)));
                    if (newPopulation.size() < POPULATION_SIZE) {
                        newPopulation.add(new Individual(child2, supplier.evaluate(child2), customer.evaluate(child2)));
                    }
                }
                // Combine old and new population for elitism
                population.addAll(newPopulation);
                // Pareto sort and diversity preservation
                List<Individual> paretoFront = getParetoFront(population);
                assignCrowdingDistance(paretoFront);
                // Sort by crowding distance descending
                paretoFront.sort((a, b) -> Double.compare(b.crowdingDistance, a.crowdingDistance));
                // Next generation: best diverse Pareto individuals
                population = new ArrayList<>();
                for (int k = 0; k < Math.min(POPULATION_SIZE, paretoFront.size()); k++) {
                    population.add(paretoFront.get(k));
                }
                // Track best
                for (Individual ind : population) {
                    if (bestIndividual == null || dominates(ind, bestIndividual)) {
                        bestIndividual = ind;
                    }
                }
            }

            System.out.println("Best contract found for instance " + i + "," + j + ":");
            supplier.print(bestIndividual.contract);
            System.out.print("  ");
            customer.print(bestIndividual.contract);
            System.out.println();
            // Store result
            results.put(i + "," + j, new Result(supplier, customer, bestIndividual.contract, bestIndividual.supplierCost + bestIndividual.customerCost));
        } catch (Exception e) {
            System.out.println("Error for instance " + i + ", " + j + ": " + e.getMessage());
        }
    }

    // Pareto dominance: a dominates b if a is no worse in both and better in at least one
    private static boolean dominates(Individual a, Individual b) {
        return (a.supplierCost <= b.supplierCost && a.customerCost <= b.customerCost)
                && (a.supplierCost < b.supplierCost || a.customerCost < b.customerCost);
    }

    // Get Pareto front (non-dominated individuals)
    private static List<Individual> getParetoFront(List<Individual> population) {
        List<Individual> front = new ArrayList<>();
        for (Individual ind : population) {
            boolean dominated = false;
            for (Individual other : population) {
                if (dominates(other, ind)) {
                    dominated = true;
                    break;
                }
            }
            if (!dominated) front.add(ind);
        }
        return front;
    }

    // Assign crowding distance for diversity
    private static void assignCrowdingDistance(List<Individual> front) {
        int n = front.size();
        if (n == 0) return;
        for (Individual ind : front) ind.crowdingDistance = 0.0;
        // Supplier cost
        front.sort(Comparator.comparingInt(a -> a.supplierCost));
        front.get(0).crowdingDistance = front.get(n - 1).crowdingDistance = Double.POSITIVE_INFINITY;
        int minS = front.get(0).supplierCost, maxS = front.get(n - 1).supplierCost;
        for (int i = 1; i < n - 1; i++) {
            if (maxS - minS == 0) continue;
            front.get(i).crowdingDistance += (front.get(i + 1).supplierCost - front.get(i - 1).supplierCost) / (double)(maxS - minS);
        }
        // Customer cost
        front.sort(Comparator.comparingInt(a -> a.customerCost));
        front.get(0).crowdingDistance = front.get(n - 1).crowdingDistance = Double.POSITIVE_INFINITY;
        int minC = front.get(0).customerCost, maxC = front.get(n - 1).customerCost;
        for (int i = 1; i < n - 1; i++) {
            if (maxC - minC == 0) continue;
            front.get(i).crowdingDistance += (front.get(i + 1).customerCost - front.get(i - 1).customerCost) / (double)(maxC - minC);
        }
    }

    // Pareto-based tournament selection
    private static Individual paretoTournamentSelect(List<Individual> population) {
        Random rand = new Random();
        Individual best = null;
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            Individual candidate = population.get(rand.nextInt(population.size()));
            if (best == null || dominates(candidate, best) || (dominates(best, candidate) == false && candidate.crowdingDistance > best.crowdingDistance)) {
                best = candidate;
            }
        }
        return best;
    }

    // Order-1 crossover for permutations
    private static int[][] onePointCrossover(int[] p1, int[] p2) {
        int size = p1.length;
        Random rand = new Random();
        int point = rand.nextInt(size - 1) + 1;
        int[] child1 = new int[size];
        int[] child2 = new int[size];
        Arrays.fill(child1, -1);
        Arrays.fill(child2, -1);
        System.arraycopy(p1, 0, child1, 0, point);
        System.arraycopy(p2, 0, child2, 0, point);
        fillRemaining(child1, p2, point);
        fillRemaining(child2, p1, point);
        return new int[][]{child1, child2};
    }

    private static void fillRemaining(int[] child, int[] parent, int start) {
        int size = child.length;
        int idx = start;
        for (int i = 0; i < size; i++) {
            int gene = parent[i];
            boolean exists = false;
            for (int j = 0; j < start; j++) {
                if (child[j] == gene) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                child[idx++] = gene;
                if (idx == size) break;
            }
        }
    }

    private static Strategy randomStrategy() {
        Strategy[] values = Strategy.values();
        return values[new Random().nextInt(values.length)];
    }

    private static class Result {
        SupplierAgent supplier;
        CustomerAgent customer;
        int[] contract;
        int fitness;
        Result(SupplierAgent s, CustomerAgent c, int[] contract, int fitness) {
            this.supplier = s;
            this.customer = c;
            this.contract = contract;
            this.fitness = fitness;
        }
    }
}
