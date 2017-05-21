package com.mockrunner.test.gen;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.mockrunner.gen.proc.JavaLineAssembler;

public class JavaLineAssemblerTest extends TestCase
{
    private final static String NL = System.getProperty("line.separator");

    private JavaLineAssembler assembler;

    @Override
  protected void setUp() throws Exception
    {
        super.setUp();
        assembler = new JavaLineAssembler();
    }

    public void testReset()
    {
        assembler.appendLine("xyz");
        assembler.reset();
        assertEquals("", assembler.getResult());
    }

    public void testAppendBlankAndNewline()
    {
        assembler.appendBlank(3);
        assembler.appendBlank();
        assembler.appendNewLine(3);
        assembler.appendNewLine();
        assertEquals("    " + NL + NL + NL + NL , assembler.getResult());
    }

    public void testAppendLine()
    {
        assembler.appendLine(null);
        assertEquals("", assembler.getResult());
        assembler.appendLine("");
        assertEquals("", assembler.getResult());
        assembler.appendLine("testLine");
        assertEquals("testLine" + NL, assembler.getResult());
    }

    public void testAppendCodeLines()
    {
        assembler.appendCodeLines(null);
        assertEquals(NL, assembler.getResult());
        assembler.reset();
        assembler.appendCodeLines(new String[0]);
        assertEquals(NL, assembler.getResult());
        assembler.reset();
        String[] lines = new String[] {"Line1", "Line2", "Line3", "Line4", "Line5"};
        assembler.appendCodeLines(lines);
        String expected = "Line1" + NL +
                          "Line2" + NL +
                          "Line3" + NL +
                          "Line4" + NL +
                          "Line5" + NL;
        assertEquals(expected, assembler.getResult());
    }

    public void testAppendIndent()
    {
        assembler.appendIndent();
        assertEquals("", assembler.getResult());
        assembler.setIndentLevel(2);
        assembler.appendIndent();
        assertEquals("        ", assembler.getResult());
        assembler.reset();
        assembler.setIndentLevel(1);
        assembler.appendLine("testLine");
        assertEquals("    testLine" + NL, assembler.getResult());
    }

    public void testAppendPackageInfo()
    {
        assembler.appendPackageInfo(null);
        assertEquals("", assembler.getResult());
        assembler.appendPackageInfo("");
        assertEquals("", assembler.getResult());
        assembler.appendPackageInfo("myPackage");
        assertEquals("package myPackage;" + NL + NL, assembler.getResult());
    }

    public void testAppendImports()
    {
        assembler.appendImports(null);
        assertEquals("", assembler.getResult());
        assembler.appendImports(new ArrayList<>());
        assertEquals("", assembler.getResult());
        assembler.appendImport("com.MyClass");
        assertEquals("import com.MyClass;" + NL, assembler.getResult());
        assembler.reset();
        List<String> importList = new ArrayList<>();
        importList.add("com.MyClass1");
        importList.add("com.MyClass2");
        importList.add("com.MyClass3");
        assembler.appendImports(importList);
        assertEquals("import com.MyClass1;" + NL + "import com.MyClass2;" + NL + "import com.MyClass3;" + NL, assembler.getResult());
    }

    public void testAppendClassDefintion()
    {
        assembler.appendClassDefintion(null);
        assertEquals("", assembler.getResult());
        assembler.appendClassDefintion("");
        assertEquals("", assembler.getResult());
        assembler.appendClassDefintion("MyClass");
        assertEquals("public class MyClass" + NL, assembler.getResult());
        assembler.reset();
        assembler.appendClassDefintion("MyClass", "");
        assertEquals("public class MyClass" + NL, assembler.getResult());
        assembler.reset();
        assembler.appendClassDefintion("MyClass", new String[0]);
        assertEquals("public class MyClass" + NL, assembler.getResult());
        assembler.reset();
        assembler.appendClassDefintion("MyClass", "", "", null);
        assertEquals("public class MyClass" + NL, assembler.getResult());
        assembler.reset();
        assembler.appendClassDefintion("MyClass", "abstract", "", null);
        assertEquals("public abstract class MyClass" + NL, assembler.getResult());
    }

    public void testAppendClassDefintionWithSuperClass()
    {
        assembler.appendClassDefintion(null, "MySuperClass");
        assertEquals("", assembler.getResult());
        assembler.appendClassDefintion("", "MySuperClass");
        assertEquals("", assembler.getResult());
        assembler.appendClassDefintion("MyClass", "MySuperClass");
        assertEquals("public class MyClass extends MySuperClass" + NL, assembler.getResult());
        assembler.reset();
        assembler.appendClassDefintion("MyClass", "", "MySuperClass", null);
        assertEquals("public class MyClass extends MySuperClass" + NL, assembler.getResult());
    }

    public void testAppendClassDefintionWithInterfaces()
    {
        assembler.appendClassDefintion(null, new String[] {"1"});
        assertEquals("", assembler.getResult());
        assembler.appendClassDefintion("", new String[] {"1"});
        assertEquals("", assembler.getResult());
        assembler.appendClassDefintion("MyClass", new String[] {"1"});
        assertEquals("public class MyClass implements 1" + NL, assembler.getResult());
        assembler.reset();
        assembler.appendClassDefintion("MyClass", new String[] {"Interface1", "Interface2", "Interface3"});
        assertEquals("public class MyClass implements Interface1, Interface2, Interface3" + NL, assembler.getResult());
        assembler.reset();
        assembler.appendClassDefintion("MyClass", "", "", new String[] {"1", "2"});
        assertEquals("public class MyClass implements 1, 2" + NL, assembler.getResult());
    }

    public void testAppendClassDefintionWithSuperClassAndInterfaces()
    {
        assembler.setIndentLevel(2);
        assembler.appendClassDefintion("MyClass", "", "MySuperClass", new String[] {"1", "2"});
        assertEquals("        public class MyClass extends MySuperClass implements 1, 2" + NL, assembler.getResult());
    }

    public void testAppendMemberDeclaration()
    {
        assembler.setIndentLevel(1);
        assembler.appendMemberDeclaration("", "myName");
        assertEquals("", assembler.getResult());
        assembler.appendMemberDeclaration("MyType", null);
        assertEquals("", assembler.getResult());
        assembler.appendMemberDeclaration("MyType", "myName");
        assertEquals("    private MyType myName;" + NL, assembler.getResult());
    }

    public void testAppendMethodDeclaration()
    {
        assembler.setIndentLevel(2);
        assembler.appendMethodDeclaration("");
        assertEquals("", assembler.getResult());
        assembler.appendMethodDeclaration(null);
        assertEquals("", assembler.getResult());
        assembler.appendMethodDeclaration("myMethod");
        assertEquals("        void myMethod()" + NL, assembler.getResult());
        assembler.reset();
        assembler.setDefaultMethodModifier("protected");
        assembler.setIndentLevel(1);
        assembler.appendMethodDeclaration("myMethod");
        assertEquals("    protected void myMethod()" + NL, assembler.getResult());
    }

    public void testAppendMethodDeclarationWithReturnType()
    {
        assembler.setIndentLevel(2);
        assembler.appendMethodDeclaration("MyReturnType", "");
        assertEquals("", assembler.getResult());
        assembler.appendMethodDeclaration("MyReturnType", null);
        assertEquals("", assembler.getResult());
        assembler.setDefaultMethodModifier("private");
        assembler.appendMethodDeclaration("", "myMethod");
        assertEquals("        private void myMethod()" + NL, assembler.getResult());
        assembler.reset();
        assembler.setDefaultMethodModifier("");
        assembler.setIndentLevel(0);
        assembler.appendMethodDeclaration("MyReturnType", "myMethod");
        assertEquals("MyReturnType myMethod()" + NL, assembler.getResult());
    }

    public void testAppendMethodDeclarationWithReturnTypeAndParameters()
    {
        assembler.appendMethodDeclaration("MyReturnType", "", new String[] {"1"}, new String[] {"2"});
        assertEquals("", assembler.getResult());
        assembler.appendMethodDeclaration("MyReturnType", null,  new String[] {"1"}, new String[] {"2"});
        assertEquals("", assembler.getResult());
        assembler.setDefaultMethodModifier("public");
        assembler.appendMethodDeclaration("", "myMethod",  new String[] {"String"}, new String[] {"myString"});
        assertEquals("public void myMethod(String myString)" + NL, assembler.getResult());
        assembler.reset();
        assembler.setIndentLevel(1);
        String[] types = new String[] {"String", "int", "Integer", "double"};
        assembler.appendMethodDeclaration("MyReturnType", "myMethod",  types, null);
        assertEquals("    MyReturnType myMethod(String param0, int param1, Integer param2, double param3)" + NL, assembler.getResult());
        assembler.reset();
        String[] names = new String[] {"string0", "int1", "int2"};
        assembler.appendMethodDeclaration("MyReturnType", "myMethod",  types, names);
        assertEquals("MyReturnType myMethod(String string0, int int1, Integer int2, double param3)" + NL, assembler.getResult());
        assembler.reset();
        assembler.setDefaultMethodModifier("protected");
        names = new String[] {"string0", "int1", "int2", "double3"};
        assembler.appendMethodDeclaration("MyReturnType", "myMethod",  types, names);
        assertEquals("protected MyReturnType myMethod(String string0, int int1, Integer int2, double double3)" + NL, assembler.getResult());
        assembler.reset();
        assembler.setDefaultMethodModifier("private");
        names = new String[] {"string0", "int1", "int2", "double3", "xyz"};
        assembler.appendMethodDeclaration("MyReturnType", "myMethod",  types, names);
        assertEquals("private MyReturnType myMethod(String string0, int int1, Integer int2, double double3)" + NL, assembler.getResult());
    }

    public void testAppendMethodDeclarationWithReturnTypeParametersAndModifiers()
    {
        assembler.appendMethodDeclaration(new String[] {"abstract"}, "MyReturnType", "", new String[] {"1"}, new String[] {"2"});
        assertEquals("", assembler.getResult());
        assembler.appendMethodDeclaration(new String[] {"abstract"}, "MyReturnType", null,  new String[] {"1"}, new String[] {"2"});
        assertEquals("", assembler.getResult());
        assembler.setDefaultMethodModifier("public");
        assembler.appendMethodDeclaration(new String[] {"abstract"}, "", "myMethod",  new String[] {"String"}, new String[] {"myString"});
        assertEquals("public abstract void myMethod(String myString)" + NL, assembler.getResult());
        assembler.reset();
        String[] types = new String[] {"String", "int", "Integer", "double"};
        String[] names = new String[] {"string0", "int1", "int2"};
        String[] modifiers = new String[] {"abstract", "synchronized"};
        assembler.appendMethodDeclaration(modifiers, "MyReturnType", "myMethod",  types, names);
        assertEquals("abstract synchronized MyReturnType myMethod(String string0, int int1, Integer int2, double param3)" + NL, assembler.getResult());
    }

    public void testAppendMethodDeclarationWithReturnTypeParametersModifiersAndExceptions()
    {
        assembler.setDefaultMethodModifier("public");
        assembler.appendMethodDeclaration(new String[] {"abstract"}, "", "myMethod",  new String[] {"String"}, new String[] {"myString"}, new String[] {"FirstException", "SecondException"});
        assertEquals("public abstract void myMethod(String myString) throws FirstException, SecondException" + NL, assembler.getResult());
        assembler.reset();
        String[] types = new String[] {"String", "int", "Integer", "double"};
        String[] names = new String[] {"string0", "int1", "int2"};
        String[] modifiers = new String[] {"abstract", "synchronized"};
        assembler.appendMethodDeclaration(modifiers, "MyReturnType", "myMethod",  types, names, null);
        assertEquals("abstract synchronized MyReturnType myMethod(String string0, int int1, Integer int2, double param3)" + NL, assembler.getResult());
        assembler.reset();
        assembler.appendMethodDeclaration(modifiers, null, "myMethod",  null, null, new String[] {"Exception"});
        assertEquals("abstract synchronized void myMethod() throws Exception" + NL, assembler.getResult());
    }

    public void testAppendConstructorDeclaration()
    {
        assembler.setIndentLevel(1);
        assembler.appendConstructorDeclaration("");
        assertEquals("", assembler.getResult());
        assembler.appendConstructorDeclaration(null);
        assertEquals("", assembler.getResult());
        assembler.appendConstructorDeclaration("MyConstructor");
        assertEquals("    public MyConstructor()" + NL, assembler.getResult());
    }

    public void testAppendConstructorDeclarationWithParameters()
    {
        assembler.setIndentLevel(1);
        assembler.appendConstructorDeclaration("", new String[0], new String[0]);
        assertEquals("", assembler.getResult());
        assembler.appendConstructorDeclaration(null, new String[] {"1"}, new String[] {"2"});
        assertEquals("", assembler.getResult());
        String[] types = new String[] {"String", "int", "Integer", "double"};
        String[] names = new String[] {"string0", "int1", "int2"};
        assembler.appendConstructorDeclaration("MyConstructor", types, names);
        assertEquals("    public MyConstructor(String string0, int int1, Integer int2, double param3)" + NL, assembler.getResult());
    }

    public void testAppendConstructorDeclarationWithParametersAndExceptions()
    {
        assembler.setIndentLevel(1);
        assembler.appendConstructorDeclaration("", new String[0], new String[0], null);
        assertEquals("", assembler.getResult());
        String[] types = new String[] {"String", "int", "Integer", "double"};
        String[] names = new String[] {"string0", "int1", "int2"};
        String[] exceptions = new String[] {"Exception1", "Exception2", "Exception3"};
        assembler.appendConstructorDeclaration("MyConstructor", types, names, exceptions);
        assertEquals("    public MyConstructor(String string0, int int1, Integer int2, double param3) throws Exception1, Exception2, Exception3" + NL, assembler.getResult());
    }

    public void testAppendComment()
    {
        assembler.setIndentLevel(1);
        assembler.appendComment("");
        assertEquals("", assembler.getResult());
        assembler.appendComment(null);
        assertEquals("", assembler.getResult());
        assembler.appendComment("this is a comment");
        assertEquals("    //this is a comment" + NL, assembler.getResult());
    }

    public void testAppendBlockComment()
    {
        assembler.setIndentLevel(1);
        assembler.appendBlockComment(new String[0]);
        assertEquals("", assembler.getResult());
        assembler.appendBlockComment(null);
        assertEquals("", assembler.getResult());
        String[] lines = new String[] {"This is a", "multiline block", "comment"};
        assembler.appendBlockComment(lines);
        String expected = "    /*" + NL +
                          "     * This is a" + NL +
                          "     * multiline block" + NL +
                          "     * comment" + NL +
                          "     */" + NL;
        assertEquals(expected, assembler.getResult());
        assembler.reset();
        lines = new String[] {"Comment"};
        assembler.appendBlockComment(lines);
        expected = "/*" + NL +
                   " * Comment" + NL +
                   " */" + NL;
        assertEquals(expected, assembler.getResult());
    }

    public void testAppendJavaDocComment()
    {
        assembler.setIndentLevel(1);
        assembler.appendJavaDocComment(new String[0]);
        assertEquals("", assembler.getResult());
        assembler.appendJavaDocComment(null);
        assertEquals("", assembler.getResult());
        String[] lines = new String[] {"This is a", "multiline block", "comment"};
        assembler.appendJavaDocComment(lines);
        String expected = "    /**" + NL +
                          "     * This is a" + NL +
                          "     * multiline block" + NL +
                          "     * comment" + NL +
                          "     */" + NL;
        assertEquals(expected, assembler.getResult());
    }
}