import java.io.*;


// DO NOT USE EVALUATE-FUNCTIONS OF AGENTS WITHIN MEDIATOR OR NEGOTIATION!
// THESE OBLECTIVE-VALUES ARE NOT AVAILABLE IN REAL NEGOTIATIONS!!!!!!!!!!!!!!!!!!!!  
// IT IS ONLY ALLOWED TO PRINT THESE OBJECTIVE-VALUES IN THE CONSOLE FOR ANALYZING REASONS

public class Negotiation {
    // Parameter of negotiation
    public static int maxRounds = 1_000_000;

    public static void main(String[] args) {
        int[] contract, proposal;
        AgentInterface agA, agB;
        MediatorInterface med;
        boolean voteA, voteB;
        int[][][] totalCost = new int[maxRounds][4][4];

        try {
            String[] inSu200 = {
                    "data/daten3ASupplier_200.txt",
                    "data/daten3BSupplier_200.txt",
                    "data/daten4ASupplier_200.txt",
                    "data/daten4BSupplier_200.txt"
            };

            String[] inCu200 = {
                    "data/daten3ACustomer_200_10.txt",
                    "data/daten3BCustomer_200_20.txt",
                    "data/daten4ACustomer_200_5.txt",
                    "data/daten4BCustomer_200_5.txt"
            };

            for (int i = 0; i < inSu200.length; i++) {
                for (int j = 0; j < inCu200.length; j++) {
                    System.out.println("Instance: " + i + " " + j);
                    agA = new SupplierAgent(new File(inSu200[i]));
                    agB = new CustomerAgent(new File(inCu200[j]));
                    med = new Mediator(agA.getContractSize(), agB.getContractSize());       // contract size = number of jobs
                    contract = med.initContract();                                          // contract = solution = job list

                    for (int round = 1; round <= maxRounds; round++) {                       // mediator
                        proposal = med.constructProposal(contract);
                        voteA = agA.vote(contract, proposal);                               // autonomy + private infos
                        voteB = agB.vote(contract, proposal);
                        if (voteA && voteB) {
                            contract = proposal;
                        }
                        totalCost[round - 1][i][j] += agA.evaluate(contract) + agB.evaluate(contract);
                    }
                    output(agA, agB, contract);
                }
            }
            print(inSu200.length, inCu200.length, totalCost);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void print( int inSu200Length, int inCu200Length, int[][][] totalCost) {
        String filePath = "data.csv";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Optional: write header
            writer.write("ROUND,SCORE");
            writer.newLine();

            for (int round = 1; round <= maxRounds; round++) {
                int sum = 0;
                for (int i = 0; i < inSu200Length; i++) {
                    for (int j = 0; j < inCu200Length; j++) {
                        sum += totalCost[round-1][i][j];
                    }
                }
                String line = String.format("%d,%d", round, sum);
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void output(AgentInterface a1, AgentInterface a2, int[] contract) {
        System.out.print("Cost:\t\t");
        a1.print(contract);
        System.out.print(" ");
        a2.print(contract);
        System.out.print("\n");
    }
}