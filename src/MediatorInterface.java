
public interface MediatorInterface {


    int[] initContract();

    int[] constructProposal(int[] contract);

    void check(int[] proposal);
}
