public interface AgentInterface {

    public boolean vote(int[] contract, int[] proposal);

    public int getContractSize();

    public int evaluate(int[] contract);

}
