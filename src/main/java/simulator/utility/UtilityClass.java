package simulator.utility;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * This class contains all the utility
 * functions
 *
 * @author umar.tahir@afiniti.com
 */

public class UtilityClass {

    /**
     * Helper function which converts bytes into String
     *
     * @param message in byte array
     * @param start   start index in byte array for specific bytes
     * @param end     end index in byte array for specific bytes.
     *                Remember this exclusive
     * @return String
     */

    public synchronized static String getStringFromByteArray(byte[] message, int start, int end) {
        return new String(Arrays.copyOfRange(message, start, end));
    }

    /**
     * Helper function which converts bytes into integer
     *
     * @param message in byte array
     * @param start   start index in byte array for specific bytes
     * @param end     index in byte array for specific bytes.
     *                Remember this exclusive
     * @return integer
     */

    public synchronized static int getIntFromByteArray(byte[] message, int start, int end) {
        return ByteBuffer.wrap(Arrays.copyOfRange(message, start, end)).getInt();
    }

}
