package org.example;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println(ipv4Cardinality(new File("ip_addresses.zip")));
    }

    private static long ipv4Cardinality(File file) throws IOException {
        BitSet bs = new BitSet();
        try (ZipInputStream stream = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            ZipFile zipFile = new ZipFile(file);
            ZipEntry ze;
            InputStream is = null;
            if ((ze = stream.getNextEntry()) != null) {
                is = zipFile.getInputStream(ze);
            }
            if (is == null) {
                throw new IOException();
            }
            BufferedInputStream reader1 = new BufferedInputStream(is);
            byte[] arr;
            byte[] characters = new byte[3];
            byte[] digits = new byte[4]; //represents ipv4 digits
            int charPos = 0;
            int digitPos = 0;
            while ((arr = reader1.readNBytes(8192)).length > 0) {
                for (byte a : arr) {
                    if (a == 0b00101110 || a == 10) {
                        digits[digitPos] = getDigitFromUTF8Chars(characters, charPos);
                        digitPos++;
                        if (digitPos == 4) {
                            bs.set(ByteBuffer.wrap(digits).getInt());
                            digitPos = 0;
                        }
                        charPos = 0;
                    } else {
                        characters[charPos] = a;
                        charPos++;
                    }
                }
            }
            return bs.cardinality();
        }
    }

    private static byte getDigitFromUTF8Chars(byte[] characters, int arity) {
        byte dig;
        if (arity == 3) {
            dig = (byte) ((byte) (characters[0] - 0b00110000) * 100
                    + (byte) (characters[1] - 0b00110000) * 10
                    + (byte) (characters[2] - 0b00110000));
        } else if (arity == 2) {
            dig = (byte) ((byte) (characters[0] - 0b00110000) * 10
                    + (byte) (characters[1] - 0b00110000));
        } else {
            dig = (byte) (characters[0] - 0b00110000);
        }
        return dig;
    }

    // Just java.util.BitSet with negative bitIndex support and max cardinality 2^32
    public static class BitSet implements Cloneable, java.io.Serializable {
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
}
