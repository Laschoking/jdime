package de.fosd.jdime.matcher.ordered.mceSubtree;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.fosd.jdime.common.Artifact;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Trees can be described as balanced sequences. A balanced sequence is a sequence of even length over the alphabet
 * {0, 1} here represented by a boolean array. The balanced sequence of a leaf node is the empty sequence.
 * The balanced sequence of a non leaf node is the concatenation of the balanced sequences of its children, every
 * one preceded by a 0 and followed by a 1. The balanced sequence of a tree is the balanced sequence of its root node.
 *
 * @author Georg Seibt
 * @see <a href="http://www.cs.upc.edu/~antoni/subtree.pdf">This Paper</a>
 */
public class BalancedSequence {

	private static final BalancedSequence EMPTY_SEQ = new BalancedSequence(new boolean[0]);

	private boolean[] seq;

	/**
	 * Constructs a new <code>BalancedSequence</code> representing the given <code>tree</code> structure.
	 *
	 * @param tree
	 * 		the tree of <code>Artifact</code>s
	 * @param <T>
	 * 		the type of the <code>Artifact</code>
	 */
	public <T extends Artifact<T>> BalancedSequence(Artifact<T> tree) {
		this.seq = new boolean[tree.getSubtreeSize() * 2];
		initSeq(tree, 0);
	}

	/**
	 * Constructs a new <code>BalancedSequence</code> wrapping the given <code>boolean[]</code>.
	 *
	 * @param seq
	 * 		the sequence to wrap
	 */
	private BalancedSequence(boolean[] seq) {
		this.seq = seq;
	}

	/**
	 * Initializes the <code>seq</code> array to the balanced sequence of the <code>tree</code>.
	 *
	 * @param tree
	 * 		the tree whose balanced sequence is to be inserted in the <code>seq</code> array
	 * @param index
	 * 		the index for the 0 before the first child
	 * @param <T>
	 * 		the type of the artifact
	 *
	 * @return the index after the last index written to; this return value is only relevant for the recursive calls
	 * of this method as the following 1 will be placed there
	 */
	private <T extends Artifact<T>> int initSeq(Artifact<T> tree, int index) {

		for (T t : tree.getChildren()) {
			index++;
			index = initSeq(t, index);
			seq[index++] = true;
		}

		return index;
	}

	/**
	 * Partitions the balanced sequence into its head and tail. The head and tail of a balanced sequence <code>s</code>
	 * are unique balanced sequences such that <code>s = 0 head(s) 1 tail(s)</code>.
	 *
	 * @return a <code>Pair</code> of (<code>head(s), tail(s)</code>)
	 */
	public Pair<BalancedSequence, BalancedSequence> partition() {

		if (seq.length == 0 || seq.length == 2) {
			return Pair.of(EMPTY_SEQ, EMPTY_SEQ);
		}

		int numZeros = 0;
		int index = 0;

		do {
			if (seq[index++]) {
				numZeros--;
			} else {
				numZeros++;
			}
		} while (numZeros > 0);

		BalancedSequence head;
		BalancedSequence tail;

		int headLength = index - 2;
		int tailLength = seq.length - index;

		if (headLength != 0) {
			boolean[] headArray = new boolean[headLength];
			System.arraycopy(seq, 1, headArray, 0, headLength);

			head = new BalancedSequence(headArray);
		} else {
			head = EMPTY_SEQ;
		}

		if (tailLength != 0) {
			boolean[] tailArray = new boolean[tailLength];
			System.arraycopy(seq, index, tailArray, 0, tailLength);

			tail = new BalancedSequence(tailArray);
		} else {
			tail = EMPTY_SEQ;
		}

		return Pair.of(head, tail);
	}

	/**
	 * Returns the decomposition of this balanced sequence. The decomposition of the empty balanced sequence is a set
	 * containing only the empty balanced sequence. For all other sequences s the decomposition is the union of a
	 * set containing s and the decompositions of head(s), tail(s) and the concatenation of head(s) and tail(s).
	 *
	 * @return the decomposition of this balanced sequence
	 */
	public Set<BalancedSequence> decompose() {

		if (isEmpty()) {
			return Collections.singleton(EMPTY_SEQ);
		}

		Set<BalancedSequence> decomposition = new HashSet<>(Collections.singleton(this));

		Pair<BalancedSequence, BalancedSequence> partition = partition();
		BalancedSequence head = partition.getLeft();
		BalancedSequence tail = partition.getRight();

		decomposition.addAll(head.decompose());
		decomposition.addAll(tail.decompose());
		decomposition.addAll(concatenate(head, tail).decompose());

		return decomposition;
	}

	/**
	 * Concatenates the two given <code>BalancedSequence</code>s.
	 *
	 * @param left
	 * 		the left part of the resulting <code>BalancedSequence</code>
	 * @param right
	 * 		the right part of the resulting <code>BalancedSequence</code>
	 *
	 * @return the concatenation result
	 */
	private static BalancedSequence concatenate(BalancedSequence left, BalancedSequence right) {
		boolean[] result = new boolean[left.seq.length + right.seq.length];

		if (result.length == 0) {
			return EMPTY_SEQ;
		}

		System.arraycopy(left.seq, 0, result, 0, left.seq.length);
		System.arraycopy(right.seq, 0, result, left.seq.length, right.seq.length);

		return new BalancedSequence(result);
	}

	/**
	 * Returns the length of the longest common balanced sequence between the balanced sequences <code>s</code> and
	 * <code>t</code>.
	 *
	 * @param s
	 * 		the first <code>BalancedSequence</code>
	 * @param t
	 * 		the second <code>BalancedSequence</code>
	 *
	 * @return the length of the longest common balanced sequence
	 */
	public static int lcs(BalancedSequence s, BalancedSequence t) {
        Map<Integer, Integer> codes = new HashMap<>();
        Integer[][] results;
        int code = 0;

        Set<BalancedSequence> dec = new HashSet<>(s.decompose());
        dec.addAll(t.decompose());

        for (BalancedSequence seq : dec) {
            codes.put(seq.hashCode(), code++);
        }

        results = new Integer[codes.size()][];

        for (int i = 0; i < results.length; i++) {
            results[i] = new Integer[i + 1];
        }

        return lcsRec(s, t, codes, results);
    }

    /**
     * Recursive helper function for {@link BalancedSequence#lcs(BalancedSequence, BalancedSequence)}. Computes
     * the longest common balanced sequence between the balanced sequences <code>s</code> and <code>t</code> using
     * the <code>results</code> array to store results of sub-problems. <code>codes</code> must
     * contain mappings for the hashes of every balanced sequence in the decompositions of <code>s</code> and
     * <code>t</code> to an index in the array <code>results</code>.
     *
     * @param s
     *         the first <code>BalancedSequence</code>
     * @param t
     *         the seconds <code>BalancedSequence</code>
     * @param codes
     *         the codes of the <code>BalancedSequence</code>s in the decompositions
     * @param results
     *         the array of solutions to sub-problems
     *
     * @return the length of the longest common balanced sequence
     */
    private static Integer lcsRec(BalancedSequence s, BalancedSequence t, Map<Integer, Integer> codes, Integer[][] results) {

        if (s.isEmpty() || t.isEmpty()) {
            return 0;
        }

        Integer codeS = codes.get(s.hashCode());
        Integer codeT = codes.get(t.hashCode());
        Integer result = lookup(codeS, codeT, results);

        if (result != null) {
            return result;
        }

        Pair<BalancedSequence, BalancedSequence> sPart = s.partition();
        Pair<BalancedSequence, BalancedSequence> tPart = t.partition();
        BalancedSequence sHead = sPart.getLeft();
        BalancedSequence tHead = tPart.getLeft();
        BalancedSequence sTail = sPart.getRight();
        BalancedSequence tTail = tPart.getRight();

        Integer a = lcsRec(sHead, tHead, codes, results) + lcsRec(sTail, tTail, codes, results) + 1;
        Integer b = lcsRec(concatenate(sHead, sTail), t, codes, results);
        Integer c = lcsRec(s, concatenate(tHead, tTail), codes, results);

        result = max(a, max(b, c));
        store(codeS, codeT, results, result);

        return result;
    }

    /**
     * Looks up the result of the lcs problem for the balanced sequences with the given codes in the
     * <code>results</code> array.
     *
     * @param codeA
     *         the code of the first balanced sequence
     * @param codeB
     *         the code of the second balanced sequence
     * @param results
     *         the results array
     *
     * @return the solution to the lcs problem or <code>null</code> if <code>results</code> contains none
     */
    private static Integer lookup(Integer codeA, Integer codeB, Integer[][] results) {
        return codeA.compareTo(codeB) > 0 ? results[codeA][codeB] : results[codeB][codeA];
    }

    /**
     * Stores the solution to an lcs problem between the balanced sequences with the given codes in the
     * <code>results</code> array.
     *
     * @param codeA
     *         the code of the first balanced sequence
     * @param codeB
     *         the code of the second balanced sequence
     * @param results
     *         the results array
     * @param result
     *         the result to be stored
     */
    private static void store(Integer codeA, Integer codeB, Integer[][] results, Integer result) {

        if (codeA.compareTo(codeB) > 0) {
            results[codeA][codeB] = result;
        } else {
            results[codeB][codeA] = result;
        }
    }

    /**
     * Returns the maximum of two <code>Integer</code>s.
     *
     * @param a
     *         the first <code>Integer</code>
     * @param b
     *         the second <code>Integer</code>
     *
     * @return the bigger of both <code>Integer</code>s
     */
    private static Integer max(Integer a, Integer b) {
        return a.compareTo(b) > 0 ? a : b;
    }

	/**
	 * Returns whether this <code>BalancedSequence</code> is empty.
	 *
	 * @return true iff the <code>BalancedSequence</code> is empty
	 */
	public boolean isEmpty() {
		return seq.length == 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		BalancedSequence sequence = (BalancedSequence) o;

		if (!Arrays.equals(seq, sequence.seq)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(seq);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(seq.length);

		for (boolean bit : seq) {
			builder.append(bit ? '1' : '0');
		}

		return builder.toString();
	}
}
