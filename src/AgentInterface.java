public interface AgentInterface {

    public boolean vote(int[] contract, int[] proposal);

    public void print(int[] contract);

    public int getContractSize();

    public int evaluate(int[] contract);

}
