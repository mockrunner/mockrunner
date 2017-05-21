package com.mockrunner.util.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Util class for collections
 */
public class CollectionUtil
{
    /**
     * Fills a <code>List</code> with <code>null</code>
     * by calling {@link #fillList(List, int, Object)}
     * with a <code>null</code> object.
     * @param list the <code>List</code> that should be filled
     * @param size the resulting size of the <code>List</code>
     */
    public static <T> void fillList(List<T> list, int size)
    {
        fillList(list, size, null);
    }

    /**
     * Fills a <code>List</code> with with the specified object
     * until it has the specified size. If the specified size is
     * equal or lower the <code>List</code> size, nothing happens.
     * @param list the <code>List</code> that should be filled
     * @param size the resulting size of the <code>List</code>
     * @param object the object to fill the list with
     */
    public static <T> void fillList(List<T> list, int size, T object)
    {
        for(int ii = list.size(); ii <  size; ii++)
        {
            list.add(object);
        }
    }

    /**
     * Returns a truncated version of the specified <code>List</code>.
     * @param list the <code>List</code>
     * @param len the truncate length
     * @return the truncated <code>List</code>
     */
    public static <T> List<T> truncateList(List<T> list, int len)
    {
        if(len >= list.size()) return list;
        ArrayList<T> newList = new ArrayList<T>(len);
        for(int ii = 0; ii < len; ii++)
        {
            newList.add(list.get(ii));
        }
        return newList;
    }
}
