package com.mockrunner.gen.proc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.mockrunner.base.BaseTestCase;
import com.mockrunner.base.HTMLOutputModule;
import com.mockrunner.base.HTMLOutputTestCase;
import com.mockrunner.gen.proc.JavaClassGenerator.ConstructorDeclaration;
import com.mockrunner.gen.proc.JavaClassGenerator.MethodDeclaration;
import com.mockrunner.util.common.ArrayUtil;
import com.mockrunner.util.common.ClassUtil;

public class StandardAdapterProcessor implements AdapterProcessor
{
    private String name;
    private String output;

    @Override
    public void process(Class<?> module, List<String> excludedMethods)
    {
        JavaClassGenerator classGenerator = new JavaClassGenerator();
        BCELClassAnalyzer analyzer = new BCELClassAnalyzer(module);
        classGenerator.setCreateJavaDocComments(true);
        classGenerator.setPackage(module.getPackage());
        String className = getClassNameFromBaseName(getBaseName(module));
        name = getJavaFileName(module, className);
        classGenerator.setClassName(className);
        classGenerator.setAbstract(true);
        Class<?> superClass = getSuperClass(module);
        if(null != superClass)
        {
            classGenerator.setSuperClass(getSuperClass(module));
        }
        classGenerator.setClassComment(getClassComment(module));
        Class<?> factoryClass = determineFactoryClass(module);
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setModule(module);
        memberInfo.setFactory(factoryClass);
        addMemberDeclarations(classGenerator, memberInfo);
        addConstructors(classGenerator);
        addTearDownMethod(classGenerator, memberInfo);
        addSetUpMethod(classGenerator, memberInfo);
        addAdditionalControlMethods(classGenerator, memberInfo);
        addDelegatorMethods(classGenerator, analyzer, excludedMethods, memberInfo);
        output = classGenerator.generate();
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getOutput()
    {
        return output;
    }

    private Class<?> determineFactoryClass(Class<?> module)
    {
        Constructor<?>[] constructors = module.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterTypes().length == 1) {
                return constructor.getParameterTypes()[0];
            }
        }
        throw new RuntimeException("Module " + module.getName() + " has no constructor with mock object factory argument");
    }

    private void addTearDownMethod(JavaClassGenerator classGenerator, MemberInfo memberInfo)
    {
        MethodDeclaration method = createProtectedMethod();
        method.setName("tearDown");
        method.setExceptions(new Class[] {Exception.class});
        method.setCodeLines(getTearDownMethodCodeLines(memberInfo));
        classGenerator.addMethodDeclaration(method);
    }

    private void addSetUpMethod(JavaClassGenerator classGenerator, MemberInfo memberInfo)
    {
        MethodDeclaration method = createProtectedMethod();
        method.setName("setUp");
        method.setExceptions(new Class[] {Exception.class});
        String[] comment = new String[2];
        comment[0] = "Creates the " + getJavaDocLink(memberInfo.getModule()) + ". If you";
        comment[1] = "overwrite this method, you must call <code>super.setUp()</code>.";
        method.setCommentLines(comment);
        method.setCodeLines(getSetUpMethodCodeLines(memberInfo));
        classGenerator.addMethodDeclaration(method);
    }

    private void addHTMLOutputMethods(JavaClassGenerator classGenerator, MemberInfo memberInfo)
    {
        Class<?> module = memberInfo.getModule();
        if(!HTMLOutputModule.class.isAssignableFrom(module)) return;
        MethodDeclaration htmlOutputMethod = createProtectedMethod();
        htmlOutputMethod.setName("getHTMLOutputModule");
        htmlOutputMethod.setReturnType(HTMLOutputModule.class);
        String[] comment = new String[3];
        comment[0] = "Returns the " + getJavaDocLink(module) + " as";
        comment[1] = getJavaDocLink(HTMLOutputModule.class) + ".";
        comment[2] = "@return the " + getJavaDocLink(HTMLOutputModule.class);
        htmlOutputMethod.setCommentLines(comment);
        String[] codeLines = new String[] {"return " + memberInfo.getModuleMember() + ";"};
        htmlOutputMethod.setCodeLines(codeLines);
        classGenerator.addMethodDeclaration(htmlOutputMethod);
    }

    private void addDelegatorMethods(JavaClassGenerator classGenerator, BCELClassAnalyzer analyzer, List<String> excludedMethods, MemberInfo memberInfo)
    {
        Method[] moduleMethods = getDelegateMethods(memberInfo.getModule(), excludedMethods);
        for (Method method : moduleMethods) {
            MethodDeclaration delegationMethod = createProtectedMethod();
            delegationMethod.setName(method.getName());
            delegationMethod.setReturnType(method.getReturnType());
            Class<?>[] exceptions = method.getExceptionTypes();
            if (exceptions.length > 0) {
                delegationMethod.setExceptions(exceptions);
            }
            Class<?>[] parameters = method.getParameterTypes();
            String[] argumentNames = null;
            if (parameters.length > 0) {
                delegationMethod.setArguments(parameters);
                argumentNames = analyzer.getArgumentNames(method);
                if (null == argumentNames || argumentNames.length <= 0) {
                    argumentNames = prepareSuitableArgumentNames(parameters);
                }
                delegationMethod.setArgumentNames(argumentNames);
            }
            String delegationCodeLine = createDelegationCodeLine(method, memberInfo.getModuleMember(), argumentNames);
            delegationMethod.setCodeLines(new String[]{delegationCodeLine});
            String[] delegationMethodComment = createDelegationMethodComment(analyzer, memberInfo.getModule(), method);
            delegationMethod.setCommentLines(delegationMethodComment);
            classGenerator.addMethodDeclaration(delegationMethod);
        }
    }

    private void addConstructors(JavaClassGenerator classGenerator)
    {
        classGenerator.addConstructorDeclaration();
        ConstructorDeclaration constructor = new ConstructorDeclaration();
        constructor.setArguments(new Class[] {String.class});
        constructor.setArgumentNames(new String[] {"name"});
        constructor.setCodeLines(new String[] {"super(name);"});
        classGenerator.addConstructorDeclaration(constructor);
    }

    private String[] prepareSuitableArgumentNames(Class<?>[] arguments)
    {
        String[] names = new String[arguments.length];
        for(int ii = 0; ii < arguments.length; ii++)
        {
            names[ii] = ClassUtil.getArgumentName(arguments[ii]);
        }
        ArrayUtil.ensureUnique(names);
        return names;
    }

    private Method[] getDelegateMethods(Class<?> module, List<String> excludedMethods)
    {
        Method[] moduleMethods = module.getDeclaredMethods();
        List<Method> delegateMethods = new ArrayList<>();
        for (Method currentMethod : moduleMethods) {
            if (shouldMethodBeAdded(currentMethod, excludedMethods)) {
                delegateMethods.add(currentMethod);
            }
        }
        return delegateMethods.toArray(new Method[delegateMethods.size()]);
    }

    private boolean shouldMethodBeAdded(Method currentMethod, List<String> excludedMethods)
    {
        if(!Modifier.isPublic(currentMethod.getModifiers())) return false;
        if(null == excludedMethods) return true;
        return !excludedMethods.contains(currentMethod.getName());
    }

    private String[] createDelegationMethodComment(BCELClassAnalyzer analyzer, Class<?> module, Method method)
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Delegates to {@link ");
        buffer.append(module.getName());
        buffer.append("#");
        buffer.append(method.getName());
        Class<?>[] argumentTypes = method.getParameterTypes();
        if(argumentTypes.length > 0)
        {
            buffer.append("(");
            for(int ii = 0; ii < argumentTypes.length; ii++)
            {
                buffer.append(ClassUtil.getClassName(argumentTypes[ii]));
                if(ii < argumentTypes.length-1)
                {
                    buffer.append(", ");
                }
            }
            buffer.append(")");
        }
        buffer.append("}");
        if(analyzer.isMethodDeprecated(method))
        {
            return new String[] {buffer.toString(), "@deprecated"};
        }
        return new String[] {buffer.toString()};
    }

    private String createDelegationCodeLine(Method method, String memberName, String[] argumentNames)
    {
        StringBuilder buffer = new StringBuilder();
        if(!Void.TYPE.equals(method.getReturnType()))
        {
            buffer.append("return ");
        }
        buffer.append(memberName);
        buffer.append(".");
        buffer.append(method.getName());
        buffer.append("(");
        if(null != argumentNames)
        {
            for(int ii = 0; ii < argumentNames.length; ii++)
            {
                buffer.append(argumentNames[ii]);
                if(ii < argumentNames.length-1)
                {
                    buffer.append(", ");
                }
            }
        }
        buffer.append(");");
        return buffer.toString();
    }

    protected Class<?>[] getAdditionalImports()
    {
        return new Class[0];
    }

    protected String getBaseName(Class<?> module)
    {
        String name = ClassUtil.getClassName(module);
        int moduleIndex = name.indexOf("Module");
        if(moduleIndex > -1)
        {
            name = name.substring(0, moduleIndex);
        }
        return name;
    }

    protected void addGetAndSetMethodPair(JavaClassGenerator classGenerator, Class<?> clazz, String memberName)
    {
        addGetMethod(classGenerator, clazz, memberName);
        addSetMethod(classGenerator, clazz, memberName);
    }

    protected void addGetMethod(JavaClassGenerator classGenerator, Class<?> clazz, String memberName)
    {
        addGetMethod(classGenerator, clazz, new String[] {"return " + memberName + ";"});
    }

    protected void addGetMethod(JavaClassGenerator classGenerator, Class<?> clazz, String[] codeLines)
    {
        MethodDeclaration getMethod = createProtectedMethod();
        getMethod.setName("get" + ClassUtil.getClassName(clazz));
        getMethod.setReturnType(clazz);
        String[] comment = new String[2];
        comment[0] = "Gets the " + getJavaDocLink(clazz) + ".";
        comment[1] = "@return the " + getJavaDocLink(clazz);
        getMethod.setCommentLines(comment);
        getMethod.setCodeLines(codeLines);
        classGenerator.addMethodDeclaration(getMethod);
    }

    protected void addSetMethod(JavaClassGenerator classGenerator, Class<?> clazz, String memberName)
    {
        String[] comment;
        MethodDeclaration setMethod = createProtectedMethod();
        setMethod.setName("set" + ClassUtil.getClassName(clazz));
        comment = new String[2];
        comment[0] = "Sets the " + getJavaDocLink(clazz) + ".";
        comment[1] = "@param " + memberName + " the " + getJavaDocLink(clazz);
        setMethod.setCommentLines(comment);
        setMethod.setArguments(new Class[] {clazz});
        setMethod.setArgumentNames(new String[] {memberName});
        setMethod.setCodeLines(new String[] {"this." + memberName + " = " + memberName + ";"});
        classGenerator.addMethodDeclaration(setMethod);
    }

    protected String getJavaDocLink(Class<?> clazz)
    {
        return "{@link " + clazz.getName() + "}";
    }

    protected MethodDeclaration createProtectedMethod()
    {
        MethodDeclaration method = new MethodDeclaration();
        method.setModifier(Modifier.PROTECTED);
        return method;
    }

    protected void addMemberDeclarations(JavaClassGenerator classGenerator, MemberInfo memberInfo)
    {
        String memberName = ClassUtil.getArgumentName(memberInfo.getModule());
        memberInfo.setModuleMember(memberName);
        classGenerator.addMemberDeclaration(memberInfo.getModule(), memberName);
    }

    protected String[] getSetUpMethodCodeLines(MemberInfo memberInfo)
    {
        String[] codeLines = new String[2];
        codeLines[0] = "super.setUp();";
        String factoryCall = "get" + ClassUtil.getClassName(memberInfo.getFactory());
        codeLines[1] = memberInfo.getModuleMember() + " = create" + ClassUtil.getClassName(memberInfo.getModule()) + "(" + factoryCall +"());";
        return codeLines;
    }

    protected String[] getTearDownMethodCodeLines(MemberInfo memberInfo)
    {
        return new String[] {"super.tearDown();", memberInfo.getModuleMember() + " = null;"};
    }

    protected void addAdditionalControlMethods(JavaClassGenerator classGenerator, MemberInfo memberInfo)
    {
        addHTMLOutputMethods(classGenerator, memberInfo);
        addGetAndSetMethodPair(classGenerator, memberInfo.getModule(), memberInfo.getModuleMember());
    }

    protected String[] getClassComment(Class<?> module)
    {
        String link = getJavaDocLink(module);
        String[] comment = new String[7];
        comment[0] = "Delegator for " + link + ". You can";
        comment[1] = "subclass this adapter or use " + link;
        comment[2] = "directly (so your test case can use another base class).";
        comment[3] = "This adapter extends BaseTestCase.class.";
        comment[4] = "It can be used if you want to use several modules in conjunction.";
        comment[5] = "<b>This class is generated from the " + link;
        comment[6] = "and should not be edited directly</b>.";
        return comment;
    }

    protected Class<?> getSuperClass(Class<?> module)
    {
        if(HTMLOutputModule.class.isAssignableFrom(module))
        {
            return HTMLOutputTestCase.class;
        }
        return BaseTestCase.class;
    }

    protected String getJavaFileName(Class<?> module, String className)
    {
        return ClassUtil.getPackageName(module).replace('.', '/') + "/" + className + ".java";
    }

    protected String getClassNameFromBaseName(String className)
    {
        return className + "CaseAdapter";
    }

    protected class MemberInfo
    {
        private Class<?> module;
        private String moduleMember;
        private Class<?> factory;
        private String factoryMember;

        public Class<?> getFactory()
        {
            return factory;
        }

        public void setFactory(Class<?> factory)
        {
            this.factory = factory;
        }

        public String getFactoryMember()
        {
            return factoryMember;
        }

        public void setFactoryMember(String factoryMember)
        {
            this.factoryMember = factoryMember;
        }

        public Class<?> getModule()
        {
            return module;
        }

        public void setModule(Class<?> module)
        {
            this.module = module;
        }

        public String getModuleMember()
        {
            return moduleMember;
        }

        public void setModuleMember(String moduleMember)
        {
            this.moduleMember = moduleMember;
        }
    }
}
