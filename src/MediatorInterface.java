
public interface MediatorInterface {


    int[] initContract();

    int[] constructProposal(Strategy strategy, int[] contract);

    int[] check(int[] proposal);
}
