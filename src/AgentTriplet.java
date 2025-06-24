import java.io.FileNotFoundException;

public class AgentTriplet {

    private final Strategy strategy;
    private final AgentInterface supplier, customer;
    private MediatorInterface mediator;
    private int[] contract;
    private int cost, score;

    public AgentTriplet(Strategy strategy, AgentInterface supplier, AgentInterface customer) {
        this.strategy = strategy;
        this.supplier = supplier;
        this.customer = customer;
        this.cost = Integer.MAX_VALUE;
        this.score = 0;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public AgentInterface getSupplier() {
        return supplier;
    }

    public AgentInterface getCustomer() {
        return customer;
    }

    public MediatorInterface getMediator() {
        return mediator;
    }

    public void setMediator(int supplierContractSize, int customerContractSize) throws FileNotFoundException {
        this.mediator = new Mediator(supplierContractSize, customerContractSize);
    }

    public int[] getContract() {
        return contract;
    }

    public void setContract(int[] contract) {
        this.contract = contract;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}