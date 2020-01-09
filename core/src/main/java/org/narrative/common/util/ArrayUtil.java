package org.narrative.common.util;

/**
 * ArrayUtil holds some miscellaneous functions
 *
 * @author Peter Bryant(pbryant@bigfoot.com)
 */
final public class ArrayUtil {
    /**
     * return an array with a length of iNewBufferSize containing as many
     * elements from array as will fit.  Null padded.
     */
    static public Object[] resizeArray(Object[] array, int iNewBufferSize) {
        if (iNewBufferSize == array.length) {
            return array;
        }
        // the resulting array will match the input array type e.g. String[] in, String[] out
        Object[] bTempBuffer = (Object[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), iNewBufferSize);
        System.arraycopy(array, 0, bTempBuffer, 0, Math.min(array.length, iNewBufferSize));
        return bTempBuffer;
    }

    /**
     * return an array with a length of iNewBufferSize containing as many
     * elements from array as will fit.  Null padded.
     */
    static public String[] resizeArray(String[] array, int iNewBufferSize) {
        if (iNewBufferSize == array.length) {
            return array;
        }
        String[] bTempBuffer = new String[iNewBufferSize];
        System.arraycopy(array, 0, bTempBuffer, 0, Math.min(array.length, iNewBufferSize));
        return bTempBuffer;
    }

    /**
     * return an array with a length of iNewBufferSize containing as many
     * elements from array as will fit.  Null padded.
     */
    static public int[] resizeArray(int[] array, int iNewBufferSize) {
        if (iNewBufferSize == array.length) {
            return array;
        }
        int[] bTempBuffer = new int[iNewBufferSize];
        System.arraycopy(array, 0, bTempBuffer, 0, Math.min(array.length, iNewBufferSize));
        return bTempBuffer;
    }

    /**
     * return an array with a length of iNewBufferSize containing as many
     * elements from array as will fit.  Null padded.
     */
    static public char[] resizeArray(char[] array, int iNewBufferSize) {
        if (iNewBufferSize == array.length) {
            return array;
        }
        char[] bTempBuffer = new char[iNewBufferSize];
        System.arraycopy(array, 0, bTempBuffer, 0, Math.min(array.length, iNewBufferSize));
        return bTempBuffer;
    }

    /**
     * return an array with a length of iNewBufferSize containing as many
     * elements from array as will fit.  Null padded.
     */
    static public byte[] resizeArray(byte[] array, int iNewBufferSize) {
        if (iNewBufferSize == array.length) {
            return array;
        }
        byte[] bTempBuffer = new byte[iNewBufferSize];
        System.arraycopy(array, 0, bTempBuffer, 0, Math.min(array.length, iNewBufferSize));
        return bTempBuffer;
    }

    public static boolean equal(Object[] arrayOne, Object[] arrayTwo) {
        if (arrayOne == null || arrayTwo == null || arrayOne.length != arrayTwo.length) {
            return false;
        }

        for (int i = 0; i < arrayOne.length; i++) {
            if (!arrayOne[i].equals(arrayTwo[i])) {
                return false;
            }
        }

        return true;
    }
}
