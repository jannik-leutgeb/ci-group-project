import java.io.FileNotFoundException;

public class Mediator implements MediatorInterface {

    int contractSize;

    public Mediator(int contractSizeA, int contractSizeB) throws FileNotFoundException {
        if (contractSizeA != contractSizeB) {
            throw new FileNotFoundException("negotiation can't be processed, problem data incompatible!");
        }
        this.contractSize = contractSizeA;
    }

    public int[] initContract() {
        int[] contract = new int[contractSize];
        for (int i = 0; i < contractSize; i++) contract[i] = i;

        for (int i = 0; i < contractSize; i++) {
            int index1 = (int) (Math.random() * contractSize);
            int index2 = (int) (Math.random() * contractSize);

            int temp = contract[index1];
            contract[index1] = contract[index2];
            contract[index2] = temp;
        }

        return contract;
    }

    // two mutation operators (Swap, Shift)
    private int[] constructProposal_SWAP(int[] contract) {
        int[] proposal = new int[contract.length];
        System.arraycopy(contract, 0, proposal, 0, contractSize);

        int i = (int) ((proposal.length - 1) * Math.random());
        int val1 = proposal[i];
        int val2 = proposal[i + 1];
        proposal[i] = val2;
        proposal[i + 1] = val1;

        check(proposal);

        return proposal;
    }

    private int[] constructProposal_SHIFT(int[] contract) {
        int[] proposal = new int[contractSize];
        System.arraycopy(contract, 0, proposal, 0, contractSize);

        int index1 = (int) ((proposal.length - 1) * Math.random());
        int index2 = (int) ((proposal.length - 1) * Math.random());
        if (index1 > index2) {
            int tmp = index1;
            index1 = index2;
            index2 = tmp;
        }
        if (Math.random() < 0.5) {
            int tmp = proposal[index1];
            for (int i = index1; i < index2; i++) {
                proposal[i] = proposal[i + 1];
            }
            proposal[index2] = tmp;
        } else {
            int tmp = proposal[index2];
            for (int i = index2; i > index1; i--) {
                proposal[i] = proposal[i - 1];
            }
            proposal[index1] = tmp;
        }
        check(proposal);
        return proposal;
    }

    public int[] constructProposal(int[] contract) {
        return (Math.random() < 0.5) ? constructProposal_SHIFT(contract) : constructProposal_SWAP(contract);
    }

    public void check(int[] proposal) {
        int sum1 = proposal.length * (proposal.length - 1) / 2;
        int sum2 = 0;
        for (int i : proposal) sum2 += i;
        if (sum1 != sum2) System.err.println("Check the sums");
    }
}
