package com.mockrunner.gen.proc;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.Deprecated;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;

public class BCELClassAnalyzer
{
    private Map<java.lang.reflect.Method,Method> methodMap;

    public BCELClassAnalyzer(Class<?> clazz)
    {
        methodMap = new HashMap<>();
        try
        {
            prepareMethodMap(clazz);
        }
        catch(ClassNotFoundException exc)
        {
            throw new RuntimeException(exc);
        }
    }

    public boolean isMethodDeprecated(java.lang.reflect.Method reflectMethod)
    {
        org.apache.bcel.classfile.Method bcelMethod = getBCELMethod(reflectMethod);
        if(null == bcelMethod) return false;
        Attribute[] attributes = bcelMethod.getAttributes();
        DeprecatedVisitor visitor = new DeprecatedVisitor();
        for (Attribute attribute : attributes) {
            attribute.accept(visitor);
            if (visitor.isDeprecated()) {
                return true;
            }
        }
        return false;
    }

    public String[] getArgumentNames(java.lang.reflect.Method reflectMethod)
    {
        org.apache.bcel.classfile.Method bcelMethod = getBCELMethod(reflectMethod);
        if(null == bcelMethod) return null;
        LocalVariableTable table = bcelMethod.getLocalVariableTable();
        if(null == table) return null;
        LocalVariable[] variables = table.getLocalVariableTable();
        if(null == variables) return null;
        int firstIndex = 0;
        while((firstIndex < variables.length) && (variables[firstIndex].getName().equals("this")))
        {
            firstIndex++;
        }
        Type[] types = bcelMethod.getArgumentTypes();
        if(null == types) return null;
        if((variables.length - firstIndex) < types.length) return null;
        String[] names = new String[types.length];
        for(int ii = 0; ii < types.length; ii++)
        {
            names[ii] = variables[firstIndex + ii].getName();
        }
        return names;
    }

    private org.apache.bcel.classfile.Method getBCELMethod(java.lang.reflect.Method reflectMethod)
    {
        return methodMap.get(reflectMethod);
    }

    private void prepareMethodMap(Class<?> clazz) throws ClassNotFoundException
    {
        java.lang.reflect.Method[] reflectMethods = clazz.getDeclaredMethods();
        JavaClass javaClass = Repository.lookupClass(clazz);
        org.apache.bcel.classfile.Method[] bcelMethods = javaClass.getMethods();
        List<java.lang.reflect.Method> reflectMethodList = new ArrayList<>(Arrays.asList(reflectMethods));
        List<Method> bcelMethodList = new ArrayList<>(Arrays.asList(bcelMethods));
        while(!reflectMethodList.isEmpty())
        {
            findAndAddBCELMethod(reflectMethodList, bcelMethodList);
        }
    }

    private void findAndAddBCELMethod(List<java.lang.reflect.Method> reflectMethodList, List<Method> bcelMethodList)
    {
        java.lang.reflect.Method reflectMethod = reflectMethodList.remove(0);
        for (Method bcelMethod : bcelMethodList) {
            if (areMethodsEqual(reflectMethod, bcelMethod)) {
                methodMap.put(reflectMethod, bcelMethod);
            }
        }
    }

    private boolean areMethodsEqual(java.lang.reflect.Method reflectMethod, org.apache.bcel.classfile.Method bcelMethod)
    {
        if(null == reflectMethod || null == bcelMethod) return false;
        if(!(reflectMethod.getName().equals(bcelMethod.getName()))) return false;
        Class<?> reflectReturnType = reflectMethod.getReturnType();
        Class<?> bcelReturnType = typeToClass(bcelMethod.getReturnType());
        if(!(reflectReturnType.equals(bcelReturnType))) return false;
        Class<?>[] reflectArgumentTypes = reflectMethod.getParameterTypes();
        Class<?>[] bcelArgumentTypes = getBCELArgumentList(bcelMethod);
        return Arrays.equals(reflectArgumentTypes, bcelArgumentTypes);
    }

    private Class<?>[] getBCELArgumentList(org.apache.bcel.classfile.Method bcelMethod)
    {
        Type[] bcelArguments = bcelMethod.getArgumentTypes();
        List<Class<?>> argumentList = new ArrayList<>();
        for (Type bcelArgument : bcelArguments) {
            Class<?> currentClass = typeToClass(bcelArgument);
            if (null != currentClass) {
                argumentList.add(currentClass);
            }
        }
        return argumentList.toArray(new Class<?>[argumentList.size()]);
    }

    private Class<?> typeToClass(Type type)
    {
        if(!(type instanceof ArrayType))
        {
            return simpleTypeToClass(type);
        }
        ArrayType arrayType = (ArrayType)type;
        Class<?> basicClass = simpleTypeToClass(arrayType.getBasicType());
        if(null == basicClass) return null;
        return Array.newInstance(basicClass, new int[arrayType.getDimensions()]).getClass();
    }

    private Class<?> simpleTypeToClass(Type type)
    {
        if(null == type) return null;
        if(type instanceof BasicType)
        {
            BasicType basicType = (BasicType)type;
            if(basicType.equals(Type.BOOLEAN))
            {
                return Boolean.TYPE;
            }
            if(basicType.equals(Type.BYTE))
            {
                return Byte.TYPE;
            }
            if(basicType.equals(Type.CHAR))
            {
                return Character.TYPE;
            }
            if(basicType.equals(Type.DOUBLE))
            {
                return Double.TYPE;
            }
            if(basicType.equals(Type.FLOAT))
            {
                return Float.TYPE;
            }
            if(basicType.equals(Type.INT))
            {
                return Integer.TYPE;
            }
            if(basicType.equals(Type.LONG))
            {
                return Long.TYPE;
            }
            if(basicType.equals(Type.SHORT))
            {
                return Short.TYPE;
            }
            if(basicType.equals(Type.VOID))
            {
                return Void.TYPE;
            }
        }
        else if(type instanceof ObjectType)
        {
            ObjectType objectType = (ObjectType)type;
            try
            {
                return Class.forName(objectType.getClassName());
            }
            catch(ClassNotFoundException exc)
            {
                throw new RuntimeException(exc);
            }
        }
        return null;
    }

    private class DeprecatedVisitor extends EmptyVisitor
    {
        private boolean deprecated = false;

        @Override
        public void visitDeprecated(Deprecated deprecatedValue)
        {
            deprecated = true;
        }

        public boolean isDeprecated()
        {
            return deprecated;
        }
    }
}
