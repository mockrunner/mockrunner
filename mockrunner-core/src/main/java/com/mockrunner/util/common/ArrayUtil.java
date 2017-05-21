package com.mockrunner.util.common;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Util class for arrays
 */
public class ArrayUtil
{
    /**
     * Returns a <code>List</code> containing the bytes from the
     * specified array as <code>Byte</code> objects.
     * @param data the byte data
     * @return the <code>List</code> with the <code>Byte</code> objects
     */
    public static List<Byte> getListFromByteArray(byte[] data)
    {
        ArrayList<Byte> list = new ArrayList<Byte>(data.length);
        for (byte aData : data) {
            list.add(aData);
        }
        return list;
    }

    /**
     * Returns a byte array containing the bytes from the <code>List</code>.
     * The <code>List</code> must contain <code>Byte</code> objects.
     * <code>null</code> entries in the <code>List</code> are
     * allowed, the resulting byte will be 0.
     * @param data the <code>List</code>
     * @return the resulting byte array
     */
    public static byte[] getByteArrayFromList(List<Byte> data)
    {
        return getByteArrayFromList(data, 0);
    }

    /**
     * Returns a byte array containing the bytes from the <code>List</code>.
     * The <code>List</code> must contain <code>Byte</code> objects.
     * <code>null</code> entries in the <code>List</code> are
     * allowed, the resulting byte will be 0.
     * @param data the <code>List</code>
     * @param index the index at which to start
     * @return the resulting byte array
     */
    public static byte[] getByteArrayFromList(List<Byte> data, int index)
    {
        return getByteArrayFromList(data, index, data.size() - index);
    }

    /**
     * Returns a byte array containing the bytes from the <code>List</code>.
     * The <code>List</code> must contain <code>Byte</code> objects.
     * <code>null</code> entries in the <code>List</code> are
     * allowed, the resulting byte will be 0.
     * @param data the <code>List</code>
     * @param index the index at which to start
     * @param len the number of bytes
     * @return the resulting byte array
     */
    public static byte[] getByteArrayFromList(List<Byte> data, int index, int len)
    {
        if(data.size() == 0) return new byte[0];
        if(index >= data.size())
        {
            throw new IndexOutOfBoundsException("Position " + index + " invalid in List of size " + data.size());
        }
        byte[] byteData = new byte[len];
        for(int ii = index; ii < data.size() && ii < index + len; ii++)
        {
            Byte nextValue = data.get(ii);
            if(null != nextValue)
            {
                byteData[ii - index] = nextValue;
            }
        }
        return byteData;
    }

    /**
     * Copies the bytes from the specified array to the specified
     * <code>List</code> as <code>Byte</code> objects starting
     * at the specified index. Grows the list if necessary.
     * <i>index</i> must be a valid index in the list.
     * @param data the byte data
     * @param list the <code>List</code>
     * @param index the index at which to start copying
     */
    public static void addBytesToList(byte[] data, List<Byte> list, int index)
    {
        addBytesToList(data, 0, data.length, list, index);
    }

    /**
     * Copies the bytes from the specified array to the specified
     * <code>List</code> as <code>Byte</code> objects starting
     * at the specified index. Grows the list if necessary.
     * <i>index</i> must be a valid index in the list.
     * @param data the byte data
     * @param offset the offset into the byte array at which to start
     * @param len the number of bytes to copy
     * @param list the <code>List</code>
     * @param index the index at which to start copying
     */
    public static void addBytesToList(byte[] data, int offset, int len, List<Byte> list, int index)
    {
        int bytesToIncrease = index + len - list.size();
        if(bytesToIncrease > 0)
        {
            for(int ii = 0; ii < bytesToIncrease; ii++)
            {
                list.add(null);
            }
        }
        for(int ii = index; ii < index + len; ii++)
        {
            list.set(ii, data[offset + ii - index]);
        }
    }

    /**
     * Returns a truncated copy of <i>sourceArray</i>. <i>len</i>
     * entries are copied.
     * @param sourceArray the source array
     * @param len the truncate length
     * @return the truncated array
     * @throws IllegalArgumentException if the specified object
     *         is not an array (either of reference or primitive
     *         component type)
     */
    public static Object truncateArray(Object sourceArray, int len)
    {
        return truncateArray(sourceArray, 0, len);
    }

    /**
     * Returns a truncated copy of <i>sourceArray</i>. <i>len</i>
     * entries are copied starting at the specified index.
     * @param sourceArray the source array
     * @param index the start index
     * @param len the truncate length
     * @return the truncated array
     * @throws IllegalArgumentException if the specified object
     *         is not an array (either of reference or primitive
     *         component type)
     */
    public static Object truncateArray(Object sourceArray, int index, int len)
    {
        if(!sourceArray.getClass().isArray())
        {
            throw new IllegalArgumentException("sourceArray must be an array");
        }
        Object targetArray = Array.newInstance(sourceArray.getClass().getComponentType(), len);
        System.arraycopy(sourceArray, index, targetArray, 0, len);
        return targetArray;
    }

    /**
     * Returns a copy of the specified array. If <i>array</i>
     * is not an array, the object itself will be returned.
     * Otherwise a copy of the array will be returned. The components
     * themselves are not cloned.
     * @param array the array
     * @return the copy of the array
     */
    public static Object copyArray(Object array)
    {
        if(!array.getClass().isArray()) return array;
        Class<?> componentType = array.getClass().getComponentType();
        int length = Array.getLength(array);
        Object copy = Array.newInstance(componentType, Array.getLength(array));
        for(int ii = 0; ii < length; ii++)
        {
            Array.set(copy, ii, Array.get(array, ii));
        }
        return copy;
    }

    /**
     * Returns an object array by wrapping primitive types. If the
     * specified array is of primitive component type, an <code>Object[]</code>
     * with the corresponding wrapper component type is returned.
     * If the specified array is already an object array, the instance is
     * returned unchanged.
     * @param sourceArray the array
     * @return the corresponding object array
     * @throws IllegalArgumentException if the specified object
     *         is not an array (either of reference or primitive
     *         component type)
     */
    public static Object[] convertToObjectArray(Object sourceArray)
    {
        if(!sourceArray.getClass().isArray())
        {
            throw new IllegalArgumentException("sourceArray must be an array");
        }
        Class<?> componentType = sourceArray.getClass().getComponentType();
        if(!componentType.isPrimitive())
        {
            return (Object[])sourceArray;
        }
        if(componentType.equals(Boolean.TYPE))
        {
            componentType = Boolean.class;
        }
        else if(componentType.equals(Byte.TYPE))
        {
            componentType = Byte.class;
        }
        else if(componentType.equals(Character.TYPE))
        {
            componentType = Character.class;
        }
        else if(componentType.equals(Short.TYPE))
        {
            componentType = Short.class;
        }
        else if(componentType.equals(Integer.TYPE))
        {
            componentType = Integer.class;
        }
        else if(componentType.equals(Long.TYPE))
        {
            componentType = Long.class;
        }
        else if(componentType.equals(Float.TYPE))
        {
            componentType = Float.class;
        }
        else if(componentType.equals(Double.TYPE))
        {
            componentType = Double.class;
        }
        int length = Array.getLength(sourceArray);
        Object[] targetArray = (Object[])Array.newInstance(componentType, length);
        for(int ii = 0; ii < length; ii++)
        {
            targetArray[ii] = Array.get(sourceArray, ii);
        }
        return targetArray;
    }

    /**
     * Returns a primitive array by unwrapping the corresponding types. If the
     * specified array is not an array of primitive wrapper types (e.g. <code>Integer[]</code>),
     * an <code>IllegalArgumentException</code> will be thrown.
     * If an array element is <code>null</code>, an <code>IllegalArgumentException</code>
     * will be thrown.
     * @param sourceArray the array
     * @return the corresponding primitive array
     * @throws IllegalArgumentException if the specified array
     *         is not an array of primitive wrapper types or if an
     *         array element is <code>null</code>
     */
    public static Object convertToPrimitiveArray(Object[] sourceArray)
    {
        Class<?> componentType = sourceArray.getClass().getComponentType();
        if(componentType.equals(Boolean.class))
        {
            componentType = Boolean.TYPE;
        }
        else if(componentType.equals(Byte.class))
        {
            componentType = Byte.TYPE;
        }
        else if(componentType.equals(Character.class))
        {
            componentType = Character.TYPE;
        }
        else if(componentType.equals(Short.class))
        {
            componentType = Short.TYPE;
        }
        else if(componentType.equals(Integer.class))
        {
            componentType = Integer.TYPE;
        }
        else if(componentType.equals(Long.class))
        {
            componentType = Long.TYPE;
        }
        else if(componentType.equals(Float.class))
        {
            componentType = Float.TYPE;
        }
        else if(componentType.equals(Double.class))
        {
            componentType = Double.TYPE;
        }
        else
        {
            throw new IllegalArgumentException("sourceArray is of type " + componentType + " which is not allowed");
        }
        int length = Array.getLength(sourceArray);
        Object targetArray = Array.newInstance(componentType, length);
        for(int ii = 0; ii < length; ii++)
        {
            Array.set(targetArray, ii, Array.get(sourceArray, ii));
        }
        return targetArray;
    }

    /**
     * Creates an array with a single object as component.
     * If the specified object is an array, it will be returned
     * unchanged. Otherwise an array with the specified object
     * as the single element will be returned.
     * @param object the object
     * @return the corresponding array
     */
    public static Object convertToArray(Object object)
    {
        if(object.getClass().isArray()) return object;
        Object array = Array.newInstance(object.getClass(), 1);
        Array.set(array, 0, object);
        return array;
    }

    /**
     * Compares the two specified arrays. If both passed objects are
     * <code>null</code>, <code>true</code> is returned. If both passed
     * objects are not arrays, they are compared using <code>equals</code>.
     * Otherwise all array elements are compared using <code>equals</code>.
     * This method does not handle multidimensional arrays, i.e. if an
     * array contains another array, comparison is based on identity.
     * @param array1 the first array
     * @param array2 the second array
     * @return <code>true</code> if the arrays are equal, <code>false</code>
     *         otherwise
     */
    public static boolean areArraysEqual(Object array1, Object array2)
    {
        if(null == array1 && null == array2) return true;
        if(null == array1 || null == array2) return false;
        if(!array1.getClass().isArray() && !array2.getClass().isArray()) return array1.equals(array2);
        if(!array1.getClass().isArray() || !array2.getClass().isArray()) return false;
        int length1 = Array.getLength(array1);
        int length2 = Array.getLength(array2);
        if(length1 != length2) return false;
        for(int ii = 0; ii < length1; ii++)
        {
            Object value1 = Array.get(array1, ii);
            Object value2 = Array.get(array2, ii);
            if(null != value1 && !value1.equals(value2)) return false;
            if(null == value1 && null != value2) return false;
        }
        return true;
    }

    /**
     * Returns a suitable hash code for the specified array. If the passed
     * object is <code>null</code>, <code>0</code> is returned.
     * It is allowed to pass an object that is not an array, in this case,
     * the hash code of the object will be returned. Otherwise the hash code
     * will be based on the array elements. <code>null</code> elements are
     * allowed.
     * This method does not handle multidimensional arrays, i.e. if an
     * array contains another array, the hash code is based on identity.
     * @param array the array
     * @return a suitable hash code
     */
    public static int computeHashCode(Object array)
    {
        if(null == array) return 0;
        if(!array.getClass().isArray()) return array.hashCode();
        int length = Array.getLength(array);
        int hashCode = 17;
        for(int ii = 0; ii < length; ii++)
        {
            Object value = Array.get(array, ii);
            if(null != value) hashCode = (31 * hashCode) + value.hashCode();
        }
        return hashCode;
    }

    /**
     * Returns the index of the first occurence of the
     * array <i>bytes</i> in the array <i>source</i>.
     * @param source the array in which to search
     * @param bytes the array to search
     * @return the index of the first occurence or
     *         -1, if <i>source</i> does not contain <i>bytes</i>
     */
    public static int indexOf(byte[] source, byte[] bytes)
    {
        return indexOf(source, bytes, 0);
    }

    /**
     * Returns the index of the first occurence of the
     * array <i>bytes</i> in the array <i>source</i>.
     * @param source the array in which to search
     * @param bytes the array to search
     * @param index the index where to begin the search
     * @return the index of the first occurence or
     *         -1, if <i>source</i> does not contain <i>bytes</i>
     */
    public static int indexOf(byte[] source, byte[] bytes, int index)
    {
        if(index + bytes.length > source.length) return -1;
        for(int ii = index; ii <= source.length - bytes.length; ii++)
        {
            int yy = 0;
            while(yy < bytes.length && bytes[yy] == source[ii + yy]) yy++;
            if(yy == bytes.length) return ii;
        }
        return -1;
    }

    /**
     * Ensures that each entry in the specified string array
     * is unique by adding a number to duplicate entries.
     * I.e. if the string <code>"entry"</code> occurs three
     * times, the three entries will be renamed to <code>"entry1"</code>,
     * <code>"entry2"</code> and <code>"entry3"</code>.
     * @param values the array of strings
     */
    public static void ensureUnique(String[] values)
    {
        Map<String, Integer> nameMap = collectOccurences(values);
        renameDuplicates(values, nameMap);
    }

    private static void renameDuplicates(String[] names, Map<String, Integer> nameMap)
    {
        for (Object o : nameMap.keySet()) {
            String nextName = (String) o;
            Integer nextValue = nameMap.get(nextName);
            if (nextValue > 1) {
                int number = 1;
                for (int ii = 0; ii < names.length; ii++) {
                    if (names[ii].equals(nextName)) {
                        names[ii] = nextName + number;
                        number++;
                    }
                }
            }
        }
    }

    private static Map<String, Integer> collectOccurences(String[] names)
    {
        Map<String, Integer> nameMap = new HashMap<>();
        for (String name : names) {
            Integer currentValue = nameMap.get(name);
            if (null == currentValue) {
                nameMap.put(name, 1);
            } else {
                nameMap.put(name, currentValue + 1);
            }
        }
        return nameMap;
    }
}
