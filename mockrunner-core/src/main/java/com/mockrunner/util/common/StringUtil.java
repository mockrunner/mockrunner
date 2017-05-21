package com.mockrunner.util.common;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

import com.mockrunner.base.NestedApplicationException;

/**
 * Simple util class for <code>String</code> related methods.
 */
public class StringUtil
{
    /**
     * Returns if the specified string is <code>null</code> or
     * the empty string.
     * @param string the string
     * @return <code>true</code> if the specified string is <code>null</code> or
     *         the empty string, <code>false</code> otherwise
     */
    public static boolean isEmptyOrNull(String string)
    {
        return (null == string) || (0 >= string.length());
    }

    /**
     * Returns <code>null</code>, if the specified string is <code>null</code> or
     * the empty string. Returns the specified string otherwise.
     * @param string the string
     * @return <code>null</code> if the specified string is <code>null</code> or
     *         the empty string, the specified string otherwise
     */
    public static String emptyStringToNull(String string)
    {
        return isEmptyOrNull(string) ? null : string;
    }

    /**
     * Replaces all occurrences of <code>match</code> in
     * <code>source</code> with <code>replacement</code>.
     * @param source the source string
     * @param match the string that is searched
     * @param replacement the replacement string
     * @return the modified string
     * @throws IllegalArgumentException if any argument is <code>null</code> or
     *         if <code>match</code> is the empty string
     */
    public static String replaceAll(String source, String match, String replacement)
    {
        if(null == source || null == match || null == replacement)
        {
            throw new IllegalArgumentException("null strings not allowed");
        }
        if(match.length() <= 0)
        {
            throw new IllegalArgumentException("match must not be empty");
        }
        StringBuilder buffer = new StringBuilder(source.length() + 10);
        int index = 0;
        int newIndex = 0;
        while((newIndex = source.indexOf(match, index)) >= 0)
        {
            buffer.append(source.substring(index, newIndex));
            buffer.append(replacement);
            index = newIndex + match.length();
        }
        buffer.append(source.substring(index));
        return buffer.toString();
    }

    /**
     * Compares two strings and returns the last
     * index where the two string are equal. If
     * the first characters of the two string do
     * not match or if at least one of the two strings
     * is empty, -1 is returned.
     * @param string1 the first string
     * @param string2 the second string
     * @return the last index where the strings are equal
     */
    public static int compare(String string1, String string2)
    {
        int endIndex = Math.min(string1.length(), string2.length());
        for(int ii = 0; ii < endIndex; ii++)
        {
            if(string1.charAt(ii) != string2.charAt(ii)) return ii - 1;
        }
        return endIndex - 1;
    }

    /**
     * Converts the character at the specified index to
     * lowercase and returns the resulting string.
     * @param string the string to convert
     * @param index the index where the character is set to lowercase
     * @return the converted string
     * @throws IndexOutOfBoundsException if the index is out of
     *         range
     */
    public static String lowerCase(String string, int index)
    {
        return lowerCase(string, index, -1);
    }

    /**
     * Converts the character in the specified index range to
     * lowercase and returns the resulting string.
     * If the provided endIndex is smaller or equal to startIndex,
     * the endIndex is set to startIndex + 1.
     * @param string the string to convert
     * @param startIndex the index to start, inclusive
     * @param endIndex the index to end, exclusive
     * @return the converted string
     * @throws IndexOutOfBoundsException if the index is out of
     *         range
     */
    public static String lowerCase(String string, int startIndex, int endIndex)
    {
        StringBuilder buffer = new StringBuilder(string);
        if(endIndex <= startIndex) endIndex = startIndex + 1;
        for(int ii = startIndex; ii < endIndex; ii++)
        {
            char character = buffer.charAt(ii);
            buffer.setCharAt(ii, Character.toLowerCase(character));
        }
        return buffer.toString();
    }

    /**
     * Helper method for <code>toString()</code> implementations.
     * Returns a string <code>"field name: value"</code>. Handles
     * <code>null</code> values, collections and arrays. If the
     * field is a collection or an array, the returned string will
     * be:<br>
     * <code>"field name 0: value0\nfield name 1: value1"</code>
     * @param fieldName the field name
     * @param field the field value
     * @return a suitable string for <code>toString()</code> implementations
     */
    public static String fieldToString(String fieldName, Object field)
    {
        StringBuffer buffer = new StringBuffer();
        if(null == field)
        {
            buffer.append(fieldName).append(": ").append("null");
        }
        else if(field.getClass().isArray())
        {
            arrayToString(fieldName, field, buffer);
        }
        else if(field instanceof Collection)
        {
            collectionToString(fieldName, field, buffer);
        }
        else if(field instanceof Map)
        {
            mapToString(fieldName, field, buffer);
        }
        else
        {
            buffer.append(fieldName).append(": ").append(field.toString());
        }
        return buffer.toString();
    }

    private static void arrayToString(String fieldName, Object field, StringBuffer buffer)
    {
        int length = Array.getLength(field);
        if(0 >= length)
        {
            buffer.append(fieldName).append(": ").append("empty");
        }
        else
        {
            for(int ii = 0; ii < length; ii++)
            {
                buffer.append(fieldToString(fieldName + " " + ii, Array.get(field, ii)));
                if(ii < length - 1)
                {
                    buffer.append("\n");
                }
            }
        }
    }

    private static void collectionToString(String fieldName, Object field, StringBuffer buffer)
    {
        @SuppressWarnings("unchecked")
        List<Object> list = new ArrayList<>((Collection<Object>)field);
        if(0 >= list.size())
        {
            buffer.append(fieldName).append(": ").append("empty");
        }
        else
        {
            for(int ii = 0; ii < list.size(); ii++)
            {
                buffer.append(fieldToString(fieldName + " " + ii, list.get(ii)));
                if(ii < list.size() - 1)
                {
                    buffer.append("\n");
                }
            }
        }
    }

    private static void mapToString(String fieldName, Object field, StringBuffer buffer)
    {
        @SuppressWarnings("unchecked")
        Map<Object, Object> map = (Map<Object,Object>)field;
        if(map.isEmpty())
        {
            buffer.append(fieldName).append(": ").append("empty");
        }
        else
        {
            Iterator<Object> keys = map.keySet().iterator();
            int ii = 0;
            while(keys.hasNext())
            {
                Object key = keys.next();
                Object value = map.get(key);
                buffer.append(fieldToString(fieldName + " " + key, value));
                if(ii < map.size() - 1)
                {
                    buffer.append("\n");
                }
                ii++;
            }
        }
    }

    /**
     * Appends the entries in the specified <code>List</code> as strings
     * with a terminating <i>"\n"</i> after each row.
     * @param buffer the buffer
     * @param data the <code>List</code> with the data
     */
    public static void appendObjectsAsString(StringBuffer buffer, List<? extends Object> data)
    {
        for (Object aData : data) {
            buffer.append(aData);
            buffer.append("\n");
        }
    }

    /**
     * Appends <i>number</i> tabs (\t) to the buffer.
     * @param buffer the buffer
     * @param number the number of tabs to append
     */
    public static void appendTabs(StringBuffer buffer, int number)
    {
        for(int ii = 0; ii < number; ii++)
        {
            buffer.append("\t");
        }
    }

    /**
     * Splits a string into tokens. Similar to <code>StringTokenizer</code>
     * except that empty tokens are recognized and added as <code>null</code>.
     * With a delimiter of <i>";"</i> the string
     * <i>"a;;b;c;;"</i> will split into
     * <i>["a"] [null] ["b"] ["c"] [null]</i>.
     * @param string the String
     * @param delim the delimiter
     * @param doTrim should each token be trimmed
     * @return the array of tokens
     */
    public static String[] split(String string, String delim, boolean doTrim)
    {
        int pos = 0, begin = 0;
        List<String> resultList = new ArrayList<>();
        while((-1 != (pos = string.indexOf(delim, begin))) && (begin < string.length()))
        {
            String token = string.substring(begin, pos);
            if(doTrim) token = token.trim();
            if(token.length() == 0) token = null;
            resultList.add(token);
            begin = pos + delim.length();
        }
        if(begin < string.length())
        {
            String token = string.substring(begin);
            if(doTrim) token = token.trim();
            if(token.length() == 0) token = null;
            resultList.add(token);
        }
        return resultList.toArray(new String[resultList.size()]);
    }

    /**
     * Returns how many times <code>string</code> contains
     * <code>other</code>.
     * @param string the string to search
     * @param other the string that is searched
     * @return the number of occurences
     */
    public static int countMatches(String string, String other)
    {
        if(null == string) return 0;
        if(null == other) return 0;
        if(0 >= string.length()) return 0;
        if(0 >= other.length()) return 0;
        int count = 0;
        int index = 0;
        while((index <= string.length() - other.length()) && (-1 != (index = string.indexOf(other, index))))
        {
            count++;
            index += other.length();
        }
        return count;
    }


    /**
     * Returns if the specified strings are equal, ignoring
     * case, if <code>caseSensitive</code> is <code>false</code>.
     * @param source the source String
     * @param target the target String
     * @param caseSensitive is the comparison case sensitive
     * @return <code>true</code> if the strings matches
     *         <code>false</code> otherwise
     */
    public static boolean matchesExact(String source, String target, boolean caseSensitive)
    {
        if(!caseSensitive)
        {
            source = source.toLowerCase();
            target = target.toLowerCase();
        }
        return (source.equals(target));
    }

    /**
     * Returns if <code>source</code> contains <code>target</code>,
     * ignoring case, if <code>caseSensitive</code> is <code>false</code>.
     * @param source the source String
     * @param target the target String
     * @param caseSensitive is the comparison case sensitive
     * @return <code>true</code> if the strings matches
     *         <code>false</code> otherwise
     */
    public static boolean matchesContains(String source, String target, boolean caseSensitive)
    {
        if(!caseSensitive)
        {
            source = source.toLowerCase();
            target = target.toLowerCase();
        }
        return (source.contains(target));
    }

    /**
     * Returns if the regular expression <code>target</code> matches
     * <code>source</code>, ignoring case, if <code>caseSensitive</code>
     * is <code>false</code>.
     * @param source the source String
     * @param target the target String
     * @param caseSensitive is the comparison case sensitive
     * @return <code>true</code> if the strings matches
     *         <code>false</code> otherwise
     */
    public static boolean matchesPerl5(String source, String target, boolean caseSensitive)
    {
        int mask = Perl5Compiler.CASE_INSENSITIVE_MASK;
        if(caseSensitive)
        {
            mask = Perl5Compiler.DEFAULT_MASK;
        }
        try
        {
            Pattern pattern = new Perl5Compiler().compile(target, mask);
            return (new Perl5Matcher().matches(source, pattern));
        }
        catch(MalformedPatternException exc)
        {
            throw new NestedApplicationException(exc);
        }
    }
}
