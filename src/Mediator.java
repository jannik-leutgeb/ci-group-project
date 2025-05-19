import java.io.FileNotFoundException;

public class Mediator {

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
    public int[] constructProposal_SWAP(int[] contract) {
        int[] proposal = new int[contract.length];
        for (int i = 0; i < proposal.length; i++) proposal[i] = contract[i];

        int i = (int) ((proposal.length - 1) * Math.random());
        int val1 = proposal[i];
        int val2 = proposal[i + 1];
        proposal[i] = val2;
        proposal[i + 1] = val1;

        check(proposal);

        return proposal;
    }

    public int[] constructProposal_SHIFT(int[] contract) {
        int[] proposal = new int[contractSize];
        for (int i = 0; i < proposal.length; i++) proposal[i] = contract[i];

        int index1 = (int) ((proposal.length - 1) * Math.random());
        int index2 = (int) ((proposal.length - 1) * Math.random());
        if (index1 > index2) {
            int tmp = index1;
            index1 = index2;
            index2 = tmp;
        }
        if (Math.random() < 0.5) {
            int wert1 = proposal[index1];
            for (int i = index1; i < index2; i++) {
                proposal[i] = proposal[i + 1];
            }
            proposal[index2] = wert1;
        } else {
            int wert2 = proposal[index2];
            for (int i = index2; i > index1; i--) {
                proposal[i] = proposal[i - 1];
            }
            proposal[index1] = wert2;
        }
        check(proposal);
        return proposal;
    }

    public void check(int[] proposal) {
        int sum = 0;
        int summe = proposal.length * (proposal.length - 1) / 2;
        for (int i = 0; i < proposal.length; i++) {
            sum += proposal[i];
        }
        if (sum != summe) System.out.println("Check the sum");
    }

    public int[] constructProposal(int[] contract) {
        int[] proposal;
        if (Math.random() < 0.5) {
            proposal = constructProposal_SHIFT(contract);
        } else {
            proposal = constructProposal_SWAP(contract);
        }
        return proposal;
    }
}
