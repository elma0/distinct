package org.example;

import java.util.Arrays;

// Just java.util.BitSet with negative bitIndex support and max cardinality 2^32
public class BitSet implements Cloneable, java.io.Serializable {
    private static final int ADDRESS_BITS_PER_WORD = 6;
    private static final int BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;
    private final long[][] words = new long[2][];
    private transient int wordsInUse = 0;
    private transient int nWordsInUse = 0;

    private static int wordIndex(int bitIndex) {
        return bitIndex >> ADDRESS_BITS_PER_WORD;
    }

    private void checkInvariants() {
        assert (wordsInUse == 0 || words[1][wordsInUse - 1] != 0);
        assert (nWordsInUse == 0 || words[0][nWordsInUse - 1] != 0);
        assert wordsInUse <= words.length;
        assert nWordsInUse <= words.length;
        assert (wordsInUse == words[1].length || words[1][wordsInUse] == 0);
        assert (nWordsInUse == words[0].length || words[0][nWordsInUse] == 0);
    }

    public BitSet() {
        int idx = wordIndex(BITS_PER_WORD - 1);
        words[0] = new long[-idx + 1];
        words[1] = new long[idx + 1];
    }

    private void ensureCapacity(int wordsRequired, boolean negative) {
        int length = negative ? words[0].length : words[1].length;
        if (length < wordsRequired) {
            // Allocate larger of doubled size or required size
            int request = Math.max(2 * length, wordsRequired);
            if (negative) {
                words[0] = Arrays.copyOf(words[0], request);
            } else {
                words[1] = Arrays.copyOf(words[1], request);
            }
        }
    }

    private void expandTo(int wordIndex) {
        int wordsRequired;
        if (wordIndex < 0) {
            wordsRequired = -wordIndex + 1;
            if (nWordsInUse < wordsRequired) {
                ensureCapacity(wordsRequired, true);
                nWordsInUse = wordsRequired;
            }
        } else {
            wordsRequired = wordIndex + 1;
            if (wordsInUse < wordsRequired) {
                ensureCapacity(wordsRequired, false);
                wordsInUse = wordsRequired;
            }
        }
    }

    public void set(int bitIndex) {
        int wordIndex = wordIndex(bitIndex);
        expandTo(wordIndex);
        if (wordIndex < 0) {
            words[0][-wordIndex] |= (1L << bitIndex);
        } else {
            words[1][wordIndex] |= (1L << bitIndex);
        }
        checkInvariants();
    }

    public long cardinality() {
        long sum = 0;
        for (int i = 0; i < nWordsInUse; i++) {
            sum += Long.bitCount(words[0][i]);
        }
        for (int i = 0; i < wordsInUse; i++) {
            sum += Long.bitCount(words[1][i]);
        }
        return sum;
    }
}
