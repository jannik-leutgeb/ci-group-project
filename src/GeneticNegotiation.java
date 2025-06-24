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
        System.out.println("\n==== Best Results for All Instances ====\n");
        results.keySet().stream().sorted().forEach(key -> {
            Result r = results.get(key);
            System.out.print("Instance " + key + ": Supplier: ");
            r.supplier.print(r.contract);
            System.out.print("  Customer: ");
            r.customer.print(r.contract);
            System.out.println("  Total Fitness: " + r.fitness);
        });
    }

    private static void runInstance(int i, int j, String supplierFile, String customerFile, ConcurrentHashMap<String, Result> results) {
        System.out.println("Instance: " + i + " " + j);
        try {
            SupplierAgent supplier = new SupplierAgent(new java.io.File(supplierFile));
            CustomerAgent customer = new CustomerAgent(new java.io.File(customerFile));
            int contractSize = supplier.getContractSize();

            // Run GA for each strategy and keep the best
            int[] bestContract = null;
            int bestFitness = Integer.MAX_VALUE;
            Strategy bestStrategy = null;

            for (Strategy strategy : Strategy.values()) {
                Mediator mediator = new Mediator(contractSize, contractSize);
                List<int[]> population = new ArrayList<>();
                for (int k = 0; k < POPULATION_SIZE; k++) {
                    population.add(mediator.initContract());
                }
                int[] localBestContract = null;
                int localBestFitness = Integer.MAX_VALUE;
                for (int gen = 0; gen < GENERATIONS; gen++) {
                    List<int[]> newPopulation = new ArrayList<>();
                    while (newPopulation.size() < POPULATION_SIZE) {
                        int[] parent1 = tournamentSelect(population, supplier, customer);
                        int[] parent2 = tournamentSelect(population, supplier, customer);
                        int[] child1 = Arrays.copyOf(parent1, contractSize);
                        int[] child2 = Arrays.copyOf(parent2, contractSize);
                        // Crossover
                        if (Math.random() < CROSSOVER_RATE) {
                            int[][] children = onePointCrossover(parent1, parent2);
                            child1 = children[0];
                            child2 = children[1];
                        }
                        // Mutation (use only the current strategy)
                        if (Math.random() < MUTATION_RATE) {
                            child1 = mediator.constructProposal(strategy, child1);
                        }
                        if (Math.random() < MUTATION_RATE) {
                            child2 = mediator.constructProposal(strategy, child2);
                        }
                        newPopulation.add(child1);
                        if (newPopulation.size() < POPULATION_SIZE) {
                            newPopulation.add(child2);
                        }
                    }
                    population = newPopulation;
                    // Find best contract in current population
                    for (int[] contract : population) {
                        int fitness = supplier.evaluate(contract) + customer.evaluate(contract);
                        if (fitness < localBestFitness) {
                            localBestFitness = fitness;
                            localBestContract = Arrays.copyOf(contract, contract.length);
                        }
                    }
                }
                // Track global best
                if (localBestFitness < bestFitness) {
                    bestFitness = localBestFitness;
                    bestContract = localBestContract;
                    bestStrategy = strategy;
                }
            }
            System.out.println("Best contract found for instance " + i + "," + j + " (Strategy: " + bestStrategy + "):");
            supplier.print(bestContract);
            System.out.print("  ");
            customer.print(bestContract);
            System.out.println();
            // Store result
            results.put(i + "," + j, new Result(supplier, customer, bestContract, bestFitness));
        } catch (Exception e) {
            System.out.println("Error for instance " + i + ", " + j + ": " + e.getMessage());
        }
    }

    private static int[] tournamentSelect(List<int[]> population, SupplierAgent supplier, CustomerAgent customer) {
        Random rand = new Random();
        int[] best = null;
        int bestFitness = Integer.MAX_VALUE;
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            int[] candidate = population.get(rand.nextInt(population.size()));
            int fitness = supplier.evaluate(candidate) + customer.evaluate(candidate);
            if (best == null || fitness < bestFitness) {
                best = candidate;
                bestFitness = fitness;
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
