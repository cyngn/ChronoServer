package com.cyngn.chrono.data;

import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * @author truelove@cyngn.com (Jeremy Truelove) 9/10/14
 */
public class SampleGeneratorTest {

    @Test
    public void testGetKbOfData() {
        assertTrue(SampleGenerator.getKbOfData(50).length() == SampleGenerator.KILOBYTE_SIZE * 50);
        assertTrue(SampleGenerator.getMbOfData(1).length() == SampleGenerator.KILOBYTE_SIZE * 1000);
    }
}
