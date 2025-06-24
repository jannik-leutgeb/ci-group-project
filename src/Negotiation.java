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
        MediatorInterface med;
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
//                    med = new Mediator(agA.getContractSize(), agB.getContractSize());       // contract size = number of jobs
                    med = new MediatorExp2(agA.getContractSize(), agB.getContractSize());       // contract size = number of jobs
                    contract = med.initContract();                                          // contract = solution = job list
                    output(agA, agB, 0, contract);

                    double[] operatorWeights = {0.2, 0.2, 0.2, 0.2, 0.2}; // Initialize weights equally

                    for (int round = 1; round < maxRounds; round++) {
                        int[] operatorIndex = new int[1]; // Array to store operator index
                        proposal = med.constructProposal(contract);
//                        proposal = med.constructProposal(contract, operatorWeights, operatorIndex);
                        voteA = agA.vote(contract, proposal);
                        voteB = agB.vote(contract, proposal);

                        if (voteA && voteB) {
                            contract = proposal;
                            updateOperatorWeights(operatorWeights, operatorIndex[0], true); // Update weights for success
                        } else {
                            updateOperatorWeights(operatorWeights, operatorIndex[0], false); // Update weights for failure
                        }
                    }
                    System.out.println(operatorWeights[0] + " " + operatorWeights[1] + " " + operatorWeights[2] + " " + operatorWeights[3] + " " + operatorWeights[4]);
                    output(agA, agB, maxRounds, contract);

                }
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    // Example of updating weights based on success
//    private static void updateOperatorWeights(double[] operatorWeights, int operatorIndex, boolean success) {
//        double adjustment = success ? 0.01 : -0.01; // Increase or decrease weight
//        operatorWeights[operatorIndex] = Math.max(0, operatorWeights[operatorIndex] + adjustment); // Ensure weights are non-negative
//
//        // Normalize weights to ensure they sum to 1
//        double sum = 0;
//        for (double weight : operatorWeights) {
//            sum += weight;
//        }
//        for (int i = 0; i < operatorWeights.length; i++) {
//            operatorWeights[i] /= sum;
//        }
//
//        // Prevent any operator's weight from exceeding the maximum threshold
//        double maxThreshold = 0.8;
//        for (int i = 0; i < operatorWeights.length; i++) {
//            if (operatorWeights[i] > maxThreshold) {
//                double excess = operatorWeights[i] - maxThreshold;
//                operatorWeights[i] = maxThreshold;
//
//                // Redistribute excess weight among other operators
//                double redistribution = excess / (operatorWeights.length - 1);
//                for (int j = 0; j < operatorWeights.length; j++) {
//                    if (j != i) {
//                        operatorWeights[j] += redistribution;
//                    }
//                }
//            }
//        }
//
//        // Re-normalize weights after redistribution
//        sum = 0;
//        for (double weight : operatorWeights) {
//            sum += weight;
//        }
//        for (int i = 0; i < operatorWeights.length; i++) {
//            operatorWeights[i] /= sum;
//        }
//    }

    private static void updateOperatorWeights(double[] operatorWeights, int operatorIndex, boolean success) {
        double adjustmentBase = (success ? 1.0 : -1.0) / maxRounds; // Base adjustment scaled by maxRounds
        double scalingFactor = 1 - operatorWeights[operatorIndex]; // Scale adjustment inversely to weight
        double adjustment = adjustmentBase * scalingFactor; // Scaled adjustment

        operatorWeights[operatorIndex] = Math.max(0, operatorWeights[operatorIndex] + adjustment); // Ensure weights are non-negative

        // Normalize weights to ensure they sum to 1
        double sum = 0;
        for (double weight : operatorWeights) {
            sum += weight;
        }
        for (int i = 0; i < operatorWeights.length; i++) {
            operatorWeights[i] /= sum;
        }

        // Prevent any operator's weight from exceeding the maximum threshold
        double maxThreshold = 0.8;
        for (int i = 0; i < operatorWeights.length; i++) {
            if (operatorWeights[i] > maxThreshold) {
                double excess = operatorWeights[i] - maxThreshold;
                operatorWeights[i] = maxThreshold;

                // Redistribute excess weight among other operators
                double redistribution = excess / (operatorWeights.length - 1);
                for (int j = 0; j < operatorWeights.length; j++) {
                    if (j != i) {
                        operatorWeights[j] += redistribution;
                    }
                }
            }
        }

        // Re-normalize weights after redistribution
        sum = 0;
        for (double weight : operatorWeights) {
            sum += weight;
        }
        for (int i = 0; i < operatorWeights.length; i++) {
            operatorWeights[i] /= sum;
        }
    }

    public static void output(Agent a1, Agent a2, int i, int[] contract) {
        System.out.print(i + " -> ");
        a1.print(contract);
        System.out.print("  ");
        a2.print(contract);
        System.out.println();
    }
}