import java.io.File;
import java.io.FileNotFoundException;


// DO NOT USE EVALUATE-FUNCTIONS OF AGENTS WITHIN MEDIATOR OR NEGOTIATION!
// THESE OBLECTIVE-VALUES ARE NOT AVAILABLE IN REAL NEGOTIATIONS!!!!!!!!!!!!!!!!!!!!  
// IT IS ONLY ALLOWED TO PRINT THESE OBJECTIVE-VALUES IN THE CONSOLE FOR ANALYZING REASONS

public class Negotiation {
    // Parameter of negotiation
    public static int maxRounds = 10_000_000;

    public static void main(String[] args) {
        int[] contract, proposal;
        Agent agA, agB;
        Mediator med;
        boolean voteA, voteB;

        try {
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

            for (int i = 0; i < inSu200.length; i++) {
                for (int j = 0; j < inCu200.length; j++) {
                    System.out.println("Instance: " + i + " " + j);
                    agA = new SupplierAgent(new File(inSu200[i]));
                    agB = new CustomerAgent(new File(inCu200[j]));
                    med = new Mediator(agA.getContractSize(), agB.getContractSize());       // contract size = number of jobs
                    contract = med.initContract();                                          // contract = solution = job list
                    output(agA, agB, contract);

                    int populationSize = 20;
                    int[][] population = new int[populationSize][];
                    for (int p = 0; p < populationSize; p++) {
                        population[p] = med.initContract();
                    }

                    for (int round = 1; round < maxRounds; round++) {
                        // Tournament selection
                        int idx1 = (int)(Math.random() * populationSize);
                        int idx2 = (int)(Math.random() * populationSize);
                        int[] parent1 = population[idx1];
                        int[] parent2 = population[idx2];

                        // Crossover
                        int[] child = med.crossover(parent1, parent2);

                        // Mutation
                        child = med.constructProposal(child);

                        // Voting
                        if (agA.vote(parent1, child) && agB.vote(parent1, child)) {
                            // Replace the worst individual (highest sum of objectives)
                            int worstIdx = 0;
                            int worstScore = Integer.MIN_VALUE;
                            for (int m = 0; m < populationSize; m++) { // <-- changed from i to m
                                int score = ((SupplierAgent)agA).evaluate(population[m]) + ((CustomerAgent)agB).evaluate(population[m]);
                                if (score > worstScore) {
                                    worstScore = score;
                                    worstIdx = m;
                                }
                            }
                            population[worstIdx] = child;
                        }
                    }

                    // Output the best contract in the population
                    int bestIdx = 0;
                    int bestScore = Integer.MAX_VALUE;
                    for (int k = 0; k < populationSize; k++) { // <-- changed from i to k
                        int score = ((SupplierAgent)agA).evaluate(population[k]) + ((CustomerAgent)agB).evaluate(population[k]);
                        if (score < bestScore) {
                            bestScore = score;
                            bestIdx = k;
                        }
                    }
                    output(agA, agB, population[bestIdx]);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void output(Agent a1, Agent a2, int[] contract) {
        a1.print(contract);
        System.out.print("  ");
        a2.print(contract);
        System.out.println();
    }
}