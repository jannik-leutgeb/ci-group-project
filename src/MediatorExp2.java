import java.io.FileNotFoundException;

public class MediatorExp2 implements MediatorInterface {
    private int contractSize;

    public MediatorExp2(int contractSizeA, int contractSizeB) throws FileNotFoundException {
        if (contractSizeA != contractSizeB) {
            throw new FileNotFoundException("negotiation can't be processed, problem data incompatible!");
        }
        this.contractSize = contractSizeA;
    }

    public int[] initContract() {
        int[] contract = new int[contractSize];
        boolean[] used = new boolean[contractSize]; // Track which jobs are already assigned

        for (int i = 0; i < contractSize; i++) {
            int jobIndex;
            do {
                jobIndex = (int)(Math.random() * contractSize);
            } while (used[jobIndex]); // Keep generating until we find unused job

            contract[i] = jobIndex;
            used[jobIndex] = true;
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

    private int[] constructProposal_REVERSE(int[] contract) {
        int[] proposal = new int[contractSize];
        System.arraycopy(contract, 0, proposal, 0, contractSize);

        // Select two random indices to define the subarray
        int index1 = (int) (Math.random() * contractSize);
        int index2 = (int) (Math.random() * contractSize);

        // Ensure index1 is less than index2
        if (index1 > index2) {
            int temp = index1;
            index1 = index2;
            index2 = temp;
        }

        // Reverse the subarray
        while (index1 < index2) {
            int temp = proposal[index1];
            proposal[index1] = proposal[index2];
            proposal[index2] = temp;
            index1++;
            index2--;
        }

        check(proposal); // Validate the proposal
        return proposal;
    }

    private int[] constructProposal_SCRAMBLE(int[] contract) {
        int[] proposal = new int[contractSize];
        System.arraycopy(contract, 0, proposal, 0, contractSize);

        int index1 = (int) (Math.random() * contractSize);
        int index2 = (int) (Math.random() * contractSize);

        if (index1 > index2) {
            int temp = index1;
            index1 = index2;
            index2 = temp;
        }

        // Shuffle the subarray
        java.util.List<Integer> subarray = new java.util.ArrayList<>();
        for (int i = index1; i <= index2; i++) {
            subarray.add(proposal[i]);
        }
        java.util.Collections.shuffle(subarray);

        for (int i = index1; i <= index2; i++) {
            proposal[i] = subarray.get(i - index1);
        }

        check(proposal);
        return proposal;
    }

    private int[] constructProposal_TWO_POINT_SWAP(int[] contract) {
        int[] proposal = new int[contractSize];
        System.arraycopy(contract, 0, proposal, 0, contractSize);

        int index1 = (int) (Math.random() * contractSize);
        int index2 = (int) (Math.random() * contractSize);

        // Swap the two elements
        int temp = proposal[index1];
        proposal[index1] = proposal[index2];
        proposal[index2] = temp;

        check(proposal);
        return proposal;
    }

    private int[] constructProposal_CYCLE_SHIFT(int[] contract) {
        int[] proposal = new int[contractSize];
        System.arraycopy(contract, 0, proposal, 0, contractSize);

        int shift = (int) (Math.random() * contractSize);
        boolean leftShift = Math.random() < 0.5;

        if (leftShift) {
            for (int i = 0; i < contractSize; i++) {
                proposal[i] = contract[(i + shift) % contractSize];
            }
        } else {
            for (int i = 0; i < contractSize; i++) {
                proposal[i] = contract[(i - shift + contractSize) % contractSize];
            }
        }

        check(proposal);
        return proposal;
    }

    public int[] constructProposal(int[] contract) {
        double random = Math.random();
        if (random < 0.33) {
            return constructProposal_SHIFT(contract);
        } else if (random < 0.66) {
            return constructProposal_SWAP(contract);
        }
        else if (random < 0.83) {
            return constructProposal_SCRAMBLE(contract);
        } else if (random < 0.90) {
            return constructProposal_TWO_POINT_SWAP(contract);
        } else if (random < 0.95) {
            return constructProposal_CYCLE_SHIFT(contract);
        }
        else {
            return constructProposal_REVERSE(contract);
        }
    }

    public void check(int[] proposal) {
        int sum1 = proposal.length * (proposal.length - 1) / 2;
        int sum2 = 0;
        for (int i : proposal) sum2 += i;
        if (sum1 != sum2) System.err.println("Check the sums");
    }
}
