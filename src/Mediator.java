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

    // Order Crossover (OX) for permutations
    public int[] crossover(int[] parent1, int[] parent2) {
        int size = parent1.length;
        int[] child = new int[size];
        boolean[] inChild = new boolean[size];

        int start = (int) (Math.random() * size);
        int end = start + (int) (Math.random() * (size - start));
        if (end > size) end = size;

        // Copy a slice from parent1
        for (int i = start; i < end; i++) {
            child[i] = parent1[i];
            inChild[parent1[i]] = true;
        }

        // Fill the rest from parent2
        int current = 0;
        for (int i = 0; i < size; i++) {
            if (!inChild[parent2[i]]) {
                while (current >= start && current < end) current++;
                child[current++] = parent2[i];
            }
        }
        return child;
    }

    private void check(int[] proposal) {
        int sum1 = proposal.length * (proposal.length - 1) / 2;
        int sum2 = 0;
        for (int i : proposal) sum2 += i;
        if (sum1 != sum2) System.err.println("Check the sums");
    }
}
