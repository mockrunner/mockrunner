package com.mockrunner.util.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassUtil
{
    private final static String[] KEYWORDS = new String[]
    {
        "abstract", "assert", "boolean", "break", "byte",
        "case", "catch", "char", "class", "const",
        "continue", "default", "do", "double", "else", "enum",
        "extends", "final", "finally", "float", "for",
        "goto", "if", "implements", "import", "instanceof",
        "int", "interface", "long", "native", "new",
        "package", "private", "protected", "public", "return",
        "short", "static", "strictFP", "super", "switch",
        "synchronized", "this", "throw", "throws", "transient",
        "try", "void", "volatile", "while"
    };

    /**
     * Returns the name of the package of the specified class.
     * If the class has no package, an empty String will be
     * returned.
     * @param clazz the Class
     * @return the package name
     */
    public static String getPackageName(Class<?> clazz)
    {
        Package classPackage = clazz.getPackage();
        if(null == classPackage) return "";
        return classPackage.getName();
    }

    /**
     * Returns the name of the specified class. This method
     * only returns the class name without package information.
     * If the specified class represents a primitive type, the
     * name of the primitive type will be returned. If the
     * specified class is an array, <code>[]</code> will be
     * appended to the name (once for each dimension).
     * @param clazz the Class
     * @return the class name
     */
    public static String getClassName(Class<?> clazz)
    {
        String dimensions = "";
        while(clazz.isArray())
        {
            clazz = clazz.getComponentType();
            dimensions += "[]";
        }
        String classPackage = getPackageName(clazz);
        if(classPackage.length() == 0)
        {
            return clazz.getName() + dimensions;
        }
        else
        {
            return clazz.getName().substring(classPackage.length() + 1) + dimensions;
        }
    }

    /**
     * Returns all interfaces implemented by the specified class
     * including all interfaces implemented by super classes.
     * If the specified class is itself an interfaces or the
     * specified class does not implement any interfaces, this
     * method returns an empty array.
     * @param clazz the Class
     * @return all interfaces implemented by the specified class
     */
    public static Class<?>[] getImplementedInterfaces(Class<?> clazz)
    {
        if(clazz.isInterface()) return new Class[0];
        Class<?>[] classes = getInheritanceHierarchy(clazz);
        Set<Class<?>> interfaceSet = new HashSet<>();
        for (Class<?> aClass : classes) {
            interfaceSet.addAll(Arrays.asList(aClass.getInterfaces()));
        }
        return interfaceSet.toArray(new Class[interfaceSet.size()]);
    }

    /**
     * Returns the inheritance hierarchy of the specified class.
     * The returned array includes all superclasses of the specified class
     * starting with the most general superclass, which is
     * <code>java.lang.Object</code>. The returned array also
     * includes the class itself as the last element. Implemented
     * interfaces are not included.
     * @param clazz the Class
     * @return all superclasses, most general superclass first
     */
    public static Class<?>[] getInheritanceHierarchy(Class<?> clazz)
    {
        List<Class<?>> classes = new ArrayList<>();
        Class<?> currentClass = clazz;
        while(null != currentClass)
        {
            classes.add(currentClass);
            currentClass = currentClass.getSuperclass();
        }
        Collections.reverse(classes);
        return classes.toArray(new Class[classes.size()]);
    }

    /**
     * Returns if the specified string is a Java language
     * keyword.
     * @param name the string
     * @return <code>true</code> if it is a keyword,
     *         <code>false</code> otherwise
     */
    public static boolean isKeyword(String name)
    {
        for (String KEYWORD : KEYWORDS) {
            if (KEYWORD.equals(name)) return true;
        }
        return false;
    }

    /**
     * Returns a suitable argument name for arguments
     * of type <code>argumentType</code>. Simply takes
     * the class name and converts the starting characters
     * to lower case (by preserving one upper case character).
     * E.g. the result of <code>JMSTestModule</code> is
     * <code>jmsTestModule</code>.
     * If the specified <code>argumentType</code> is an array,
     * an <code>"s"</code> is appended to the string.
     * If the resulting string is a Java keyword, <code>"Value"</code>
     * is appended to the string (which is always the case with
     * primitive types).
     * @param argumentType the argument type
     * @return a suitable mixed case argument name
     */
    public static String getArgumentName(Class<?> argumentType)
    {
        String dimensions = "";
        while(argumentType.isArray())
        {
            argumentType = argumentType.getComponentType();
            dimensions = "s";
        }
        String name = getClassName(argumentType);
        int index = 0;
        while(index < name.length() - 1 && Character.isUpperCase(name.charAt(index)) && Character.isUpperCase(name.charAt(index + 1)))
        {
            index++;
        }
        if(index == name.length() - 1)
        {
            index++;
        }
        name = StringUtil.lowerCase(name, 0, index);
        if(isKeyword(name))
        {
             name += "Value";
        }
        name += dimensions;
        return name;
    }
}
