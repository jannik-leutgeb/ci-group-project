
public interface MediatorInterface {


    int[] initContract();

    int[] constructProposal(int[] contract);
    int[] constructProposal(int[] contract, double[] operatorWeights, int[] operatorIndex);

    void check(int[] proposal);
}
