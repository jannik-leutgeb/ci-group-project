import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


// DO NOT USE EVALUATE-FUNCTIONS OF AGENTS WITHIN MEDIATOR OR NEGOTIATION!
// THESE OBLECTIVE-VALUES ARE NOT AVAILABLE IN REAL NEGOTIATIONS!!!!!!!!!!!!!!!!!!!!  
// IT IS ONLY ALLOWED TO PRINT THESE OBJECTIVE-VALUES IN THE CONSOLE FOR ANALYZING REASONS

public class Negotiation {
    // Parameter of negotiation
    public static int maxRounds = 1_000_000;

    public static void main(String[] args) {
        int[] proposal;
        List<AgentTriplet> approaches = new ArrayList<>();
        AgentTriplet bestApproach = null;
        boolean voteA, voteB;
        int minCost;

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

                    approaches.add(new AgentTriplet(Strategy.SWAP, new SupplierAgent(new File(inSu200[i])), new CustomerAgent(new File(inCu200[j]))));
                    approaches.add(new AgentTriplet(Strategy.SHIFT, new SupplierAgent(new File(inSu200[i])), new CustomerAgent(new File(inCu200[j]))));
                    approaches.add(new AgentTriplet(Strategy.REVERSE, new SupplierAgent(new File(inSu200[i])), new CustomerAgent(new File(inCu200[j]))));
                    approaches.add(new AgentTriplet(Strategy.SCRAMBLE, new SupplierAgent(new File(inSu200[i])), new CustomerAgent(new File(inCu200[j]))));
                    approaches.add(new AgentTriplet(Strategy.TWO_POINT_SWAP, new SupplierAgent(new File(inSu200[i])), new CustomerAgent(new File(inCu200[j]))));
                    approaches.add(new AgentTriplet(Strategy.CYCLE_SHIFT, new SupplierAgent(new File(inSu200[i])), new CustomerAgent(new File(inCu200[j]))));

                    for (AgentTriplet approach : approaches) {
                        approach.setMediator(approach.getSupplier().getContractSize(), approach.getCustomer().getContractSize());
                        approach.setContract(approach.getMediator().initContract());
                    }

                    for (int round = 1; round < maxRounds; round++) {

                        minCost = Integer.MAX_VALUE;

                        for (AgentTriplet approach : approaches) {
                            proposal = approach.getMediator().constructProposal(approach.getStrategy(), approach.getContract());                            // mediator constructs proposal
                            voteA = approach.getSupplier().vote(approach.getContract(), proposal);                                  // autonomy + private infos
                            voteB = approach.getCustomer().vote(approach.getContract(), proposal);

                            if (voteA && voteB) approach.setContract(proposal);
                            approach.setCost(approach.getSupplier().evaluate(approach.getContract()) + approach.getCustomer().evaluate(approach.getContract()));

                            if (approach.getCost() < minCost) minCost = approach.getCost();
                        }

                        for (AgentTriplet approach : approaches) {
                            if (approach.getCost() == minCost) approach.setScore(approach.getScore() + 1);
                        }
                    }

                    bestApproach = approaches.stream().max(Comparator.comparingInt(AgentTriplet::getScore)).orElse(bestApproach);

                    output(bestApproach.getSupplier(), bestApproach.getCustomer(), bestApproach.getContract());
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void output(AgentInterface a1, AgentInterface a2, int[] contract) {
        a1.print(contract);
        System.out.print("  ");
        a2.print(contract);
        System.out.println();
    }
}