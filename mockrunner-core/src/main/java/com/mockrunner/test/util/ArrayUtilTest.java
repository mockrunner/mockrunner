package com.mockrunner.test.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import com.mockrunner.util.common.ArrayUtil;

public class ArrayUtilTest extends TestCase
{
    public void testGetListFromByteArray()
    {
        byte[] testArray = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8};
        List<Byte> list = ArrayUtil.getListFromByteArray(testArray);
        assertTrue(list.size() == 9);
        for(int ii = 0; ii < testArray.length; ii++)
        {
            byte nextByteFromList = list.get(ii);
            assertEquals(testArray[ii], nextByteFromList);
        }
    }

    public void testGetByteArrayFromList()
    {
        List<Byte> list = new ArrayList<>();
        for(int ii = 0; ii < 9; ii++)
        {
            list.add((byte) ii);
        }
        list.add(null);
        list.add((byte) 0);
        byte[] data = ArrayUtil.getByteArrayFromList(list);
        assertTrue(data.length == 11);
        for(int ii = 0; ii < 9; ii++)
        {
            assertEquals(ii, data[ii]);
        }
        assertEquals(0, data[9]);
        assertEquals(0, data[10]);
        data = ArrayUtil.getByteArrayFromList(list, 5);
        assertTrue(data.length == 6);
        assertEquals(5, data[0]);
        assertEquals(6, data[1]);
        assertEquals(7, data[2]);
        assertEquals(8, data[3]);
        assertEquals(0, data[4]);
        assertEquals(0, data[5]);
        data = ArrayUtil.getByteArrayFromList(list, 5, 4);
        assertTrue(data.length == 4);
        assertEquals(5, data[0]);
        assertEquals(6, data[1]);
        assertEquals(7, data[2]);
        assertEquals(8, data[3]);
        data = ArrayUtil.getByteArrayFromList(list, 0, 0);
        assertTrue(data.length == 0);
        data = ArrayUtil.getByteArrayFromList(list, 0, 1);
        assertTrue(data.length == 1);
        assertEquals(0, data[0]);
        data = ArrayUtil.getByteArrayFromList(new ArrayList<>(), 0, 0);
        assertNotNull(data);
        assertTrue(data.length == 0);
    }

    public void testAddBytesToArrayList()
    {
        List<Byte> list = new ArrayList<>();
        byte[] testArray = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8};
        ArrayUtil.addBytesToList(testArray, list, 0);
        assertTrue(list.size() == 9);
        for(int ii = 0; ii < testArray.length; ii++)
        {
            byte nextByteFromList = list.get(ii);
            assertEquals(testArray[ii], nextByteFromList);
        }
        ArrayUtil.addBytesToList(testArray, 1, 3, list, 9);
        assertTrue(list.size() == 12);
        for(int ii = 0; ii < testArray.length; ii++)
        {
            byte nextByteFromList = list.get(ii);
            assertEquals(testArray[ii], nextByteFromList);
        }
        assertEquals(1, list.get(9).byteValue());
        assertEquals(2, list.get(10).byteValue());
        assertEquals(3, list.get(11).byteValue());
        ArrayUtil.addBytesToList(testArray, 1, 3, list, 0);
        assertTrue(list.size() == 12);
        assertEquals(1, list.get(0).byteValue());
        assertEquals(2, list.get(1).byteValue());
        assertEquals(3, list.get(2).byteValue());
        assertEquals(3, list.get(3).byteValue());
        ArrayUtil.addBytesToList(testArray, list, 100);
        assertTrue(list.size() == 109);
        for(int ii = 12; ii < 100; ii++)
        {
            assertNull(list.get(ii));
        }
        for(int ii = 100; ii < 109; ii++)
        {
            byte nextByteFromList = list.get(ii);
            assertEquals(testArray[ii - 100], nextByteFromList);
        }
    }

    public void testTruncateArray()
    {
        String[] stringArray = new String[] {"This", "is", "an", "array", "of", "strings"};
        stringArray = (String[])ArrayUtil.truncateArray(stringArray, 3);
        assertTrue(stringArray.length == 3);
        assertEquals("This", stringArray[0]);
        assertEquals("is", stringArray[1]);
        assertEquals("an", stringArray[2]);
        boolean[] booleanArray = new boolean[] {true, true, false, true, false, false};
        booleanArray = (boolean[])ArrayUtil.truncateArray(booleanArray, 2, 3);
        assertTrue(booleanArray.length == 3);
        assertEquals(false, booleanArray[0]);
        assertEquals(true, booleanArray[1]);
        assertEquals(false, booleanArray[2]);
    }

    public void testIndexOf()
    {
        byte[] testArray = new byte[] {1, 1, 5, 3, 4, 5, 7, 7, 8, 3, 2};
        assertEquals(-1, ArrayUtil.indexOf(testArray, new byte[] {0}));
        assertEquals(-1, ArrayUtil.indexOf(testArray, new byte[] {0, 2}));
        assertEquals(0, ArrayUtil.indexOf(testArray, new byte[] {1}));
        assertEquals(1, ArrayUtil.indexOf(testArray, new byte[] {1, 5}));
        assertEquals(6, ArrayUtil.indexOf(testArray, new byte[] {7, 7, 8}));
        assertEquals(0, ArrayUtil.indexOf(testArray, new byte[] {1, 1, 5, 3, 4, 5, 7, 7, 8, 3}));
        assertEquals(0, ArrayUtil.indexOf(testArray, testArray));
        assertEquals(-1, ArrayUtil.indexOf(testArray, new byte[] {1, 1, 5, 3, 4, 5, 7, 7, 8, 3, 0}));
        assertEquals(10, ArrayUtil.indexOf(testArray, new byte[] {2}));
        assertEquals(2, ArrayUtil.indexOf(testArray, new byte[] {5, 3}));
        assertEquals(0, ArrayUtil.indexOf(testArray, new byte[] {}));

        assertEquals(2, ArrayUtil.indexOf(testArray, new byte[] {5, 3}), 1);
        assertEquals(-1, ArrayUtil.indexOf(testArray, new byte[] {5, 3}), 5);
        assertEquals(-1, ArrayUtil.indexOf(testArray, testArray), 1);
        assertEquals(1, ArrayUtil.indexOf(testArray, new byte[] {1}), 1);
        assertEquals(6, ArrayUtil.indexOf(testArray, new byte[] {7, 7, 8}), 6);
    }

    public void testConvertToObjectArray()
    {
        int[] intArray = new int[] {1, 2, 3};
        Object[] integerWrappedArray = ArrayUtil.convertToObjectArray(intArray);
        assertTrue(integerWrappedArray instanceof Integer[]);
        assertEquals(1, integerWrappedArray[0]);
        assertEquals(2, integerWrappedArray[1]);
        assertEquals(3, integerWrappedArray[2]);
        double[] doubleArray = new double[] {1.3};
        Object[] doubleWrappedArray = ArrayUtil.convertToObjectArray(doubleArray);
        assertTrue(doubleWrappedArray instanceof Double[]);
        assertEquals(1.3, doubleWrappedArray[0]);
        String[] stringArray = new String[] {"This", "is", "an", "array", "of", "strings"};
        Object[] stringTargetArray = ArrayUtil.convertToObjectArray(stringArray);
        assertTrue(stringArray == stringTargetArray);
        boolean[] booleanArray = new boolean[] {true, true, false};
        Object[] booleanWrappedArray = ArrayUtil.convertToObjectArray(booleanArray);
        assertTrue(booleanWrappedArray instanceof Boolean[]);
        assertEquals(Boolean.TRUE, booleanWrappedArray[0]);
        assertEquals(Boolean.TRUE, booleanWrappedArray[1]);
        assertEquals(Boolean.FALSE, booleanWrappedArray[2]);
    }

    public void testConvertToPrimitiveArray()
    {
        Integer[] integerArray = new Integer[] {new Integer(1), new Integer(2), new Integer(3)};
        int[] intUnwrappedArray = (int[])ArrayUtil.convertToPrimitiveArray(integerArray);
        assertEquals(1, intUnwrappedArray[0]);
        assertEquals(2, intUnwrappedArray[1]);
        assertEquals(3, intUnwrappedArray[2]);
        Float[] floatArray = new Float[] {new Float(1)};
        float[] floatUnwrappedArray = (float[])ArrayUtil.convertToPrimitiveArray(floatArray);
        assertEquals(1.0, floatUnwrappedArray[0], 0);
        Boolean[] booleanArray = new Boolean[] {Boolean.TRUE, Boolean.FALSE};
        boolean[] booleanUnwrappedArray = (boolean[])ArrayUtil.convertToPrimitiveArray(booleanArray);
        assertTrue(booleanUnwrappedArray[0]);
        assertFalse(booleanUnwrappedArray[1]);
        intUnwrappedArray = (int[])ArrayUtil.convertToPrimitiveArray(new Integer[0]);
        assertEquals(0, intUnwrappedArray.length);
        try
        {
            ArrayUtil.convertToPrimitiveArray(new String[] {"1"});
            fail();
        }
        catch (IllegalArgumentException exc)
        {
            //should throw exception
        }
        try
        {
            ArrayUtil.convertToPrimitiveArray(new Integer[] {new Integer(1), null});
            fail();
        }
        catch (IllegalArgumentException exc)
        {
            //should throw exception
        }
    }

    public void testConvertToArray()
    {
        int[] intArray = new int[] {1, 2, 3};
        Object array = ArrayUtil.convertToArray(intArray);
        assertSame(intArray, array);
        String[] stringArray = new String[] {"This", "is", "an", "array", "of", "strings"};
        array = ArrayUtil.convertToArray(stringArray);
        assertTrue(stringArray == array);
        array = ArrayUtil.convertToArray("Test");
        assertTrue(array instanceof String[]);
        assertTrue(((String[])array).length == 1);
        assertEquals("Test", ((String[])array)[0]);
    }

    public void testCopyArray()
    {
        byte[] byteArray = new byte[] {1, 2, 3, 4, 5};
        byte[] copyByteArray = (byte[])ArrayUtil.copyArray(byteArray);
        assertNotSame(byteArray, copyByteArray);
        assertTrue(Arrays.equals(byteArray, copyByteArray));
        String testWrong = (String)ArrayUtil.copyArray("testWrong");
        assertEquals(testWrong, "testWrong");
        String[] stringArray = new String[] {"This", "is", "an", "array", "of", "strings"};
        String[] copyStringArray = (String[])ArrayUtil.copyArray(stringArray);
        assertNotSame(stringArray, copyStringArray);
        assertTrue(Arrays.equals(stringArray, copyStringArray));
        Object myObject = new Object();
        Object[] myArray = new Object[] {myObject};
        Object[] myCopy = (Object[])ArrayUtil.copyArray(myArray);
        assertNotSame(myArray, myCopy);
        assertSame(myArray[0], myCopy[0]);
        String[] firstStringArray = new String[] {"1", "2", "3"};
        String[] secondStringArray = new String[] {"4", "5", "6"};
        String[][] dim2StringArray = new String[][] {firstStringArray, secondStringArray};
        String[][] copy2Dim = (String[][])ArrayUtil.copyArray(dim2StringArray);
        assertNotSame(dim2StringArray, copy2Dim);
        assertSame(firstStringArray, copy2Dim[0]);
        assertSame(secondStringArray, copy2Dim[1]);
    }

    public void testAreArraysEqual()
    {
        assertTrue(ArrayUtil.areArraysEqual(null, null));
        assertTrue(ArrayUtil.areArraysEqual("1", "1"));
        assertTrue(ArrayUtil.areArraysEqual(1L, 1L));
        assertFalse(ArrayUtil.areArraysEqual(null, "1"));
        assertFalse(ArrayUtil.areArraysEqual("1", null));
        assertFalse(ArrayUtil.areArraysEqual("1", "2"));
        assertFalse(ArrayUtil.areArraysEqual("1", new String[] {"1"}));
        assertFalse(ArrayUtil.areArraysEqual(new String[] {"1", "2"}, 2));
        assertFalse(ArrayUtil.areArraysEqual(new String[] {}, new String[] {"1"}));
        assertTrue(ArrayUtil.areArraysEqual(new String[] {"1"}, new String[] {"1"}));
        assertTrue(ArrayUtil.areArraysEqual(new int[] {1, 2, 3}, new int[] {1, 2, 3}));
        assertFalse(ArrayUtil.areArraysEqual(new int[] {1, 2}, new int[] {1, 2, 3}));
        assertFalse(ArrayUtil.areArraysEqual(new Object[] {new Object()}, new Object[] {new Object()}));
        assertFalse(ArrayUtil.areArraysEqual(new Object[] {new Object()}, new Object[] {new Object()}));
        assertTrue(ArrayUtil.areArraysEqual(new Object[] {"1"}, new String[] {"1"}));
        assertFalse(ArrayUtil.areArraysEqual(new String[] {"1"}, new String[][] {{"1"}}));
        assertFalse(ArrayUtil.areArraysEqual(new String[][] {{"1"}}, new String[][] {{"1"}}));
        String[][][] dim3Array = new String[][][] {{{"1"}}, {{"2"}}, {{"3"}}};
        assertTrue(ArrayUtil.areArraysEqual(dim3Array, dim3Array));
    }

    public void testComputeHashCode()
    {
        assertEquals(0, ArrayUtil.computeHashCode(null));
        assertEquals("123".hashCode(), ArrayUtil.computeHashCode("123"));
        assertEquals(ArrayUtil.computeHashCode(new String[] {"1", null}), ArrayUtil.computeHashCode(new String[] {null, "1"}));
        assertEquals(16369, ArrayUtil.computeHashCode(new byte[] {1, 1}));
        assertFalse(ArrayUtil.computeHashCode(new String[] {"1", "2"}) == ArrayUtil.computeHashCode(new String[] {"1", "2", "3"}));
    }

    public void testEnsureUnique()
    {
        String[] testArray = new String[] {"test1", "test2"};
        ArrayUtil.ensureUnique(testArray);
        assertTrue(Arrays.equals(new String[] {"test1", "test2"}, testArray));
        testArray = new String[] {"test", "test", "xyz"};
        ArrayUtil.ensureUnique(testArray);
        assertTrue(Arrays.equals(new String[] {"test1", "test2", "xyz"}, testArray));
        testArray = new String[] {"x", "y", "z", "z", "x"};
        ArrayUtil.ensureUnique(testArray);
        assertTrue(Arrays.equals(new String[] {"x1", "y", "z1", "z2", "x2"}, testArray));
    }
}
