package com.cyngn.chrono.data;

import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * Handles generating random data samples.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 9/10/14
 */
public class SampleGenerator {

    private static final char[] sampleChars = {
        // lower
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
        'w', 'x', 'y', 'z',
        // upper
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
        'W', 'X', 'Y', 'Z',
        // numeric
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };

    public static final long KILOBYTE_SIZE = 1024L;
    private static final Random rand = new Random();

    private static StringBuilder getKBSample(StringBuilder builder) {
        // Assume this data will go out as ASCII wrapped by UTF-8
        long charsNeeded = KILOBYTE_SIZE;
        for (int i = 0; i < charsNeeded; i++) {
            builder.append(getRandomChar());
        }
        return  builder;
    }

    /**
     * Gets 'n' Kilobytes of random data
     * @param numberOfKbs the number of KBs of random data to return.
     * @return the random data in UTF-8 encoding
     */
    public static String getKbOfData(int numberOfKbs) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < numberOfKbs; i++) {
            getKBSample(builder);
        }
        return new String(builder.toString().getBytes(), StandardCharsets.UTF_8);
    }

    /**
     * Gets 'n' Megabytes of random data
     * @param numberOfMbs the number of MBs of random data to return.
     * @return the random data in UTF-8 encoding
     */
    public static String getMbOfData(int numberOfMbs) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < numberOfMbs; i++) {
            builder.append(getKbOfData(1000));
        }
        return new String(builder.toString().getBytes(), StandardCharsets.UTF_8);
    }

    private static char getRandomChar() {
        return sampleChars[rand.nextInt(sampleChars.length)];
    }
}
