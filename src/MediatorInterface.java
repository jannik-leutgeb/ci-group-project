
public interface MediatorInterface {


    int[] initContract();

    int[] constructProposal(Strategy strategy, int[] contract);

    void check(int[] proposal);
}
