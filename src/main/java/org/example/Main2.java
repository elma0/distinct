package org.example;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import sun.net.util.IPAddressUtil;

public class Main2 {

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
            BufferedReader reader1 = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = reader1.readLine()) != null) {
                bs.set(ByteBuffer.wrap(IPAddressUtil.textToNumericFormatV4(line)).getInt());
            }
            return bs.cardinality();
        }
    }
}
