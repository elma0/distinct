package org.example;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
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
}
