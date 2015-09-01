package com.cyngn.chrono.data;

import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Handles storing pre-computed payload sizes for easy access.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 9/10/14
 */
public class SampleMapper {
    private static SampleMapper instance = new SampleMapper();

    public final int [] supportedKbPayloads = {0, 1, 5, 10, 25, 32, 50, 500};
    public final int [] supportedMbPayloads = {1, 5, 10};

    private Map<Integer, String> kbMap;
    private Map<Integer, String> mbMap;

    public static String SIZE_PARAM = "size";
    public static String UNIT_PARAM = "unit";
    public static String KB_UNIT = "kb";
    public static String MB_UNIT = "mb";

    private SampleMapper() {
        kbMap = Maps.newHashMap();
        mbMap = Maps.newHashMap();

        for (int payload : supportedKbPayloads) {
            kbMap.put(payload, SampleGenerator.getKbOfData(payload));
        }

        for (int payload : supportedMbPayloads) {
            mbMap.put(payload, SampleGenerator.getMbOfData(payload));
        }
    }

    public static SampleMapper getInstance() {
        return instance;
    }

    /**
     * Get a random payload of data.
     *
     * @param unit the unit of data to retrieve either mb or kb
     * @param size the amount of data to get back
     * @return the data in UTF-8 encoding
     */
    public String getPayload(String unit, int size) {
        if (StringUtils.equalsIgnoreCase(KB_UNIT, unit)) {
            return getKbPayload(size);
        } else if (StringUtils.equalsIgnoreCase(MB_UNIT, unit)) {
            return getMbPayload(size);
        } else {
            return "";
        }
    }

    /**
     * Get a random payload of data measured in KBs.
     *
     * @param payloadSize the number of KBs of data to get
     * @return the data in UTF-8 encoding
     */
    public String getKbPayload(int payloadSize) {
        if (kbMap.containsKey(payloadSize)) {
            return kbMap.get(payloadSize);
        }
        return null;
    }

    /**
     * Adds an arbitrary payload to our in memory store
     * @param payloadSize the payload size to add
     */
    public void addKbPayload(int payloadSize) {
        if (!hasKbPayload(payloadSize)) {
            kbMap.put(payloadSize, SampleGenerator.getKbOfData(payloadSize));
        }
    }

    /**
     * Do we have a measure of data already in KB for the payload size?
     *
     * @param payloadSize the payload size we are looking for
     * @return true if we've pre-computed the payload false otherwise
     */
    public boolean hasKbPayload(int payloadSize) {
        return kbMap.containsKey(payloadSize);
    }

    /**
     * Get a random payload of data measured in MBs.
     *
     * @param payloadSize the number of MBs of data to get
     * @return the data in UTF-8 encoding
     */
    public String getMbPayload(int payloadSize) {
        if (mbMap.containsKey(payloadSize)) {
            return mbMap.get(payloadSize);
        }
        return null;
    }

    /**
     * Adds an arbitrary payload to our in memory store
     * @param payloadSize the payload size to add
     */
    public void addMbPayload(int payloadSize) {
        if (!hasKbPayload(payloadSize)) {
            mbMap.put(payloadSize, SampleGenerator.getMbOfData(payloadSize));
        }
    }

    /**
     * Do we have a measure of data already in MB for the payload size?
     *
     * @param payloadSize the payload size we are looking for
     * @return true if we've pre-computed the payload false otherwise
     */
    public boolean hasMbPayload(int payloadSize) {
        return mbMap.containsKey(payloadSize);
    }

    /**
     * Are the parameters valid and do the represent how we measure things?
     *
     * @param unit the type of measurement (ie 'kb' or 'mb')
     * @param size the size of data we are looking for
     * @return true if the params represent a valid request false otherwise
     */
    public static boolean areDataParamsValid(String unit, int size) {
        return (StringUtils.equalsIgnoreCase(unit, KB_UNIT) && SampleMapper.getInstance().hasKbPayload(size)) ||
                (StringUtils.equalsIgnoreCase(unit, MB_UNIT) && SampleMapper.getInstance().hasMbPayload(size));
    }
}
