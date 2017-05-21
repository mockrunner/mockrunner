package com.mockrunner.gen.proc;

import junit.framework.TestCase;

import com.mockrunner.base.BasicHTMLOutputTestCase;
import com.mockrunner.base.HTMLOutputModule;
import com.mockrunner.gen.proc.JavaClassGenerator.MethodDeclaration;
import com.mockrunner.mock.web.WebMockObjectFactory;
import com.mockrunner.util.common.ClassUtil;

public class BasicAdapterProcessor extends StandardAdapterProcessor
{
    @Override
    protected void addMemberDeclarations(JavaClassGenerator classGenerator, MemberInfo memberInfo)
    {
        super.addMemberDeclarations(classGenerator, memberInfo);
        String factoryName = ClassUtil.getArgumentName(memberInfo.getFactory());
        memberInfo.setFactoryMember(factoryName);
        classGenerator.addMemberDeclaration(memberInfo.getFactory(), factoryName);
    }

    @Override
    protected String[] getSetUpMethodCodeLines(MemberInfo memberInfo)
    {
        String[] codeLines = new String[2];
        codeLines[0] = "super.setUp();";
        String getFactoryCall = "get" + ClassUtil.getClassName(memberInfo.getFactory());
        codeLines[1] = memberInfo.getModuleMember() + " = create" + ClassUtil.getClassName(memberInfo.getModule()) + "(" + getFactoryCall +"());";
        return codeLines;
    }

    @Override
    protected String[] getTearDownMethodCodeLines(MemberInfo memberInfo)
    {
        String[] codeLines = new String[3];
        codeLines[0] = "super.tearDown();";
        codeLines[1] = memberInfo.getModuleMember() + " = null;";
        codeLines[2] = memberInfo.getFactoryMember() + " = null;";
        return codeLines;
    }

    @Override
    protected void addAdditionalControlMethods(JavaClassGenerator classGenerator, MemberInfo memberInfo)
    {
        addCreateFactoryMethods(classGenerator, memberInfo);
        addGetFactoryLazyMethod(classGenerator, memberInfo);
        addSetMethod(classGenerator, memberInfo.getFactory(), memberInfo.getFactoryMember());
        addCreateModuleMethods(classGenerator, memberInfo);
        super.addAdditionalControlMethods(classGenerator, memberInfo);
    }

    private void addGetFactoryLazyMethod(JavaClassGenerator classGenerator, MemberInfo memberInfo)
    {
        String[] codeLines = new String[8];
        codeLines[0] = "synchronized(" + ClassUtil.getClassName(memberInfo.getFactory()) + ".class)";
        codeLines[1] = "{";
        codeLines[2] = "    if(" + memberInfo.getFactoryMember() + " == null)";
        codeLines[3] = "    {";
        codeLines[4] = "        " + memberInfo.getFactoryMember() + " = create" + ClassUtil.getClassName(memberInfo.getFactory()) + "();";
        codeLines[5] = "    }";
        codeLines[6] = "}";
        codeLines[7] = "return " + memberInfo.getFactoryMember() + ";";
        addGetMethod(classGenerator, memberInfo.getFactory(), codeLines);
    }

    @Override
    protected String[] getClassComment(Class<?> module)
    {
        String link = getJavaDocLink(module);
        String[] comment = new String[9];
        String standardClassName = ClassUtil.getPackageName(module) + "." + super.getClassNameFromBaseName(getBaseName(module));
        comment[0] = "Delegator for " + link + ". You can";
        comment[1] = "subclass this adapter or use " + link;
        comment[2] = "directly (so your test case can use another base class).";
        comment[3] = "This basic adapter can be used if you don't need any other modules. It";
        comment[4] = "does not extend BaseTestCase.class. If you want";
        comment[5] = "to use several modules in conjunction, consider subclassing";
        comment[6] = "{@link " + standardClassName + "}.";
        comment[7] = "<b>This class is generated from the " + link;
        comment[8] = "and should not be edited directly</b>.";
        return comment;
    }

    @Override
    protected Class<?> getSuperClass(Class<?> module)
    {
        if(HTMLOutputModule.class.isAssignableFrom(module))
        {
            return BasicHTMLOutputTestCase.class;
        }
        return TestCase.class;
    }

    @Override
    protected String getClassNameFromBaseName(String className)
    {
        return "Basic" + className + "CaseAdapter";
    }

    private void addCreateFactoryMethods(JavaClassGenerator classGenerator, MemberInfo memberInfo)
    {
        Class<?> factory = memberInfo.getFactory();
        MethodDeclaration createMethod = prepareCreateMethod(factory);
        String[] comment = new String[2];
        comment[0] = "Creates a " + getJavaDocLink(factory) + ".";
        comment[1] = "@return the created " + getJavaDocLink(factory);
        createMethod.setCommentLines(comment);
        String[] codeLines = new String[] {"return new " + ClassUtil.getClassName(factory) + "();"};
        createMethod.setCodeLines(codeLines);
        classGenerator.addMethodDeclaration(createMethod);
        if(HTMLOutputModule.class.isAssignableFrom(memberInfo.getModule()))
        {
            createMethod = prepareCreateMethod(factory);
            createMethod.setArguments(new Class[] {WebMockObjectFactory.class});
            createMethod.setArgumentNames(new String[] {"otherFactory"});
            codeLines = new String[] {"return new " + ClassUtil.getClassName(factory) + "(otherFactory);"};
            createMethod.setCodeLines(codeLines);
            comment = new String[] {"Same as <code>" + createMethod.getName() + "(otherFactory, true)</code>." };
            createMethod.setCommentLines(comment);
            classGenerator.addMethodDeclaration(createMethod);
            createMethod = prepareCreateMethod(factory);
            createMethod.setArguments(new Class[] {WebMockObjectFactory.class, Boolean.TYPE});
            createMethod.setArgumentNames(new String[] {"otherFactory", "createNewSession"});
            codeLines = new String[] {"return new " + ClassUtil.getClassName(factory) + "(otherFactory, createNewSession);"};
            createMethod.setCodeLines(codeLines);
            createMethod.setCommentLines(getCreateMethodComment(factory));
            classGenerator.addMethodDeclaration(createMethod);
        }
    }

    private void addCreateModuleMethods(JavaClassGenerator classGenerator, MemberInfo memberInfo)
    {
        Class<?> module = memberInfo.getModule();
        Class<?> factory = memberInfo.getFactory();
        MethodDeclaration createMethod = prepareCreateMethod(module);
        String[] comment = new String[4];
        comment[0] = "Creates a " + getJavaDocLink(module) + " based on the current";
        comment[1] = getJavaDocLink(factory) + ".";
        comment[2] = "Same as <code>" + createMethod.getName() + "(" + createGetFactoryMethodCall(factory) +  ")</code>.";
        comment[3] = "@return the created " + getJavaDocLink(module);
        createMethod.setCommentLines(comment);
        String[] codeLines = new String[] {"return new " + ClassUtil.getClassName(module) + "(" + createGetFactoryMethodCall(factory) + ");"};
        createMethod.setCodeLines(codeLines);
        classGenerator.addMethodDeclaration(createMethod);
        createMethod = prepareCreateMethod(module);
        comment = new String[3];
        comment[0] = "Creates a " + getJavaDocLink(module) + " with the specified";
        comment[1] = getJavaDocLink(factory) + ".";
        comment[2] = "@return the created " + getJavaDocLink(module);
        createMethod.setCommentLines(comment);
        createMethod.setArguments(new Class[] {factory});
        createMethod.setArgumentNames(new String[] {"mockFactory"});
        codeLines = new String[] {"return new " + ClassUtil.getClassName(module) + "(mockFactory);"};
        createMethod.setCodeLines(codeLines);
        classGenerator.addMethodDeclaration(createMethod);
    }

    private String createGetFactoryMethodCall(Class<?> factory)
    {
        return "get" + ClassUtil.getClassName(factory) + "()";
    }

    private MethodDeclaration prepareCreateMethod(Class<?> clazz)
    {
        String methodName = "create" + ClassUtil.getClassName(clazz);
        MethodDeclaration createMethod = createProtectedMethod();
        createMethod.setName(methodName);
        createMethod.setReturnType(clazz);
        return createMethod;
    }

    private String[] getCreateMethodComment(Class<?> factory)
    {
        String link = getJavaDocLink(factory);
        String[] comment = new String[13];
        comment[0] = "Creates a " + link + " based on another ";
        comment[1] = getJavaDocLink(WebMockObjectFactory.class) + ".";
        comment[2] = "The created " + link + " will have its own";
        comment[3] = "request and response objects. If you set <i>createNewSession</i>";
        comment[4] = "to <code>true</code> it will also have its own session object.";
        comment[5] = "The two factories will share one <code>ServletContext</code>.";
        comment[6] = "Especially important for multithreading tests.";
        comment[7] = "If you set <i>createNewSession</i> to false, the two factories";
        comment[8] = "will share one session. This setting simulates multiple requests";
        comment[9] = "from the same client.";
        comment[10] = "@param otherFactory the other factory";
        comment[11] = "@param createNewSession create a new session for the new factory";
        comment[12] = "@return the created " + link;
        return comment;
    }
}
