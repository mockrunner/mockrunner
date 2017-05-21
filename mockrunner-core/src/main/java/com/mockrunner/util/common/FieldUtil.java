package com.mockrunner.util.common;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class FieldUtil
{
    /**
     * Returns all non-static fields declared by the specified class and its
     * superclasses. The returned array contains the methods of all classes
     * in the inheritance hierarchy, starting with the methods of the
     * most general superclass, which is <code>java.lang.Object</code>.
     * @param theClass the class whose methods are examined
     * @return the array of field arrays
     */
    public static Field[][] getFieldsSortedByInheritanceHierarchy(Class<?> theClass)
    {
        List<Field[]> hierarchyList = new ArrayList<>();
        Class<?>[] hierarchyClasses = ClassUtil.getInheritanceHierarchy(theClass);
        for (Class<?> hierarchyClass : hierarchyClasses) {
            addFieldsForClass(hierarchyList, hierarchyClass);
        }
        return hierarchyList.toArray(new Field[hierarchyList.size()][]);
    }

    private static void addFieldsForClass(List<Field[]> hierarchyList, Class<?> clazz)
    {
        List<Field> methodList = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                methodList.add(field);
            }
        }
        hierarchyList.add(methodList.toArray(new Field[methodList.size()]));
    }
}
