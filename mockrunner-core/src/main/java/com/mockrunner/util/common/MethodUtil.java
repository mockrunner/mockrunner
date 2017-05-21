package com.mockrunner.util.common;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mockrunner.base.NestedApplicationException;

public class MethodUtil
{
    /**
     * Invokes the method with the specified name on the specified object
     * and throws a {@link com.mockrunner.base.NestedApplicationException},
     * if the invocation fails. The method must be public and must not
     * have any parameters.
     * @param object the object the method is invoked from
     * @param methodName the name of the method
     * @return the result of the method invocation
     */
    public static Object invoke(Object object, String methodName)
    {
        try
        {
            Method method = object.getClass().getMethod(methodName, (Class<?>[])null);
            return method.invoke(object, (Object[])null);
        }
        catch(Exception exc)
        {
            throw new NestedApplicationException(exc);
        }
    }

    /**
     * Invokes the method with the specified name on the specified object
     * and throws a {@link com.mockrunner.base.NestedApplicationException},
     * if the invocation fails. The method must be public and must have
     * exactly one parameter of the type specified by the given
     * <code>parameter</code>.
     * @param object the object the method is invoked from
     * @param methodName the name of the method
     * @param parameter the parameter, must not be <code>null</code>
     * @return the result of the method invocation
     */
    public static Object invoke(Object object, String methodName, Object parameter)
    {
        try
        {
            Method method = object.getClass().getMethod(methodName, parameter.getClass());
            return method.invoke(object, parameter);
        }
        catch(Exception exc)
        {
            throw new NestedApplicationException(exc);
        }
    }

    /**
     * Returns if the two specified methods are equal as
     * defined by <code>Method.equals()</code> except that
     * the methods can be defined by different classes.
     * @param method1 the first method to compare
     * @param method2 the second method to compare
     * @return <code>true</code> if the methods are equal, <code>false</code>
     *         otherwise
     * @throws NullPointerException if one of the methods is <code>null</code>
     */
    public static boolean areMethodsEqual(Method method1, Method method2)
    {
        if(method1.equals(method2)) return true;
        if(!method2.getName().equals(method1.getName())) return false;
        if(!method1.getReturnType().equals(method2.getReturnType())) return false;
        return Arrays.equals(method1.getParameterTypes(), method2.getParameterTypes());
    }

    /**
     * Returns if <code>method2</code> overrides <code>method1</code>.
     * @param method1 method to be overridden
     * @param method2 overriding method
     * @return <code>true</code> if <code>method2</code> overrides <code>method1</code>, <code>false</code>
     *         otherwise
     * @throws NullPointerException if one of the methods is <code>null</code>
     */
    public static boolean overrides(Method method1, Method method2)
    {
        if(method1.equals(method2)) return false;
        if(Modifier.isPrivate(method1.getModifiers())) return false;
        if(Modifier.isPrivate(method2.getModifiers())) return false;
        if(!method1.getDeclaringClass().isAssignableFrom(method2.getDeclaringClass())) return false;
        if(!method2.getName().equals(method1.getName())) return false;
        if(method1.getDeclaringClass().isInterface()) return false;
        return Arrays.equals(method1.getParameterTypes(), method2.getParameterTypes());
    }

    /**
     * Returns all methods in <code>methods</code> that are overridden in
     * the specified class hierarchy. The returned <code>Set</code> contains
     * all overridden methods and all overriding methods.
     * @param clazz the class hierarchy
     * @param methods the <code>Set</code> of methods
     * @return all overridden and overriding methods.
     */
    public static Set<Method> getOverriddenMethods(Class<?> clazz, Method[] methods)
    {
        Method[][] declaredMethods = MethodUtil.getMethodsSortedByInheritanceHierarchy(clazz);
        Set<Method> overridingMethods = new HashSet<>();
        for (Method currentAroundInvokeMethod : methods) {
            Set<Method> currentOverridingMethods = new HashSet<>();
            for (Method[] declaredMethod : declaredMethods) {
                for (Method aDeclaredMethod : declaredMethod) {
                    if (MethodUtil.overrides(currentAroundInvokeMethod, aDeclaredMethod)) {
                        currentOverridingMethods.add(aDeclaredMethod);
                    }
                }
            }
            if (!currentOverridingMethods.isEmpty()) {
                overridingMethods.add(currentAroundInvokeMethod);
                overridingMethods.addAll(currentOverridingMethods);
            }
        }
        return overridingMethods;
    }

    /**
     * Returns the declared methods of the specified class whose names are matching
     * the specified regular expression.
     * @param theClass the class whose methods are examined
     * @param expr the regular expression
     * @return the matching methods
     */
    public static Method[] getMatchingDeclaredMethods(Class<?> theClass, String expr)
    {
        Method[] methods = theClass.getDeclaredMethods();
        List<Method> resultList = new ArrayList<Method>();
        for (Method method : methods) {
            if (StringUtil.matchesPerl5(method.getName(), expr, true)) {
                resultList.add(method);
            }
        }
        return resultList.toArray(new Method[resultList.size()]);
    }

    /**
     * Returns all non-static methods declared by the specified class and its
     * superclasses. The returned array contains the methods of all classes
     * in the inheritance hierarchy, starting with the methods of the
     * most general superclass, which is <code>java.lang.Object</code>.
     * @param theClass the class whose methods are examined
     * @return the array of method arrays
     */
    public static Method[][] getMethodsSortedByInheritanceHierarchy(Class<?> theClass)
    {
        List<Method[]> hierarchyList = new ArrayList<>();
        Class<?>[] hierarchyClasses = ClassUtil.getInheritanceHierarchy(theClass);
        for (Class<?> hierarchyClass : hierarchyClasses) {
            addMethodsForClass(hierarchyList, hierarchyClass);
        }
        return hierarchyList.toArray(new Method[hierarchyList.size()][]);
    }

    private static void addMethodsForClass(List<Method[]> hierarchyList, Class<?> clazz)
    {
        List<Method> methodList = new ArrayList<>();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (!Modifier.isStatic(method.getModifiers())) {
                methodList.add(method);
            }
        }
        hierarchyList.add(methodList.toArray(new Method[methodList.size()]));
    }
}
