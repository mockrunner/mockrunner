package com.mockrunner.test.gen;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.mockrunner.gen.proc.JavaLineParser;
import com.mockrunner.gen.proc.JavaLineProcessor;
import com.mockrunner.gen.proc.JavaLineParser.Block;
import com.mockrunner.gen.proc.JavaLineParser.Line;

public class JavaLineProcessorTest extends TestCase
{
    private final static String NL = System.getProperty("line.separator");

    private String getValidTestCode()
    {
        String testJava = "package com.mockrunner.test.consistency;\n" +
                "\n" +
                "import java.io.FileInputStream;\n" +
                "import java.io.FileReader;\n" +
                "\n" +
                "import junit.framework.TestCase;\n" +
                "\n" +
                "public class JavaLineParserTest extends TestCase\n" +
                "{\n" +
                "    private String test;\n" +
                "    protected int myInt;\n" +
                "    public test()\n" +
                "{\n" +
                " //do it\n" +
                " if(true)\n" +
                "{\n" +
                "  \t}\n" +
                "}\n" +
                "\n" +
                "public anotherMethod()\n" +
                "{\n" +
                "}  \n" +
                "}";
        return testJava;
    }

    private String getInvalidTestCode()
    {
        String testJava = "package com.mockrunner.test.consistency;\n" +
                "\n" +
                "import java.io.FileInputStream;\n" +
                "import java.io.FileReader;\n" +
                "" +
                "import junit.framework.TestCase;\n" +
                "\n" +
                "public class JavaLineParserTest extends TestCase\n" +
                "{\n" +
                "    private String test;\n" +
                "    protected int myInt;\n" +
                "    public test()\n" +
                "{\n" +
                " //do it\n" +
                " if(true)\n" +
                "{\n" +
                "{\n" +
                "  \t}\n" +
                "}}}}}}}}}}}}\n";
        return testJava;
    }

    private String getNestedTestCode()
    {
        String testJava = "test\n" +
                "\n" +
                "\n" +
                "{\n" +
                "{\n" +
                "ff{sfs\n" +
                "yxv{yxvx\n" +
                "\n" +
                "abc\n" +
                "}}\n" +
                "abc\n" +
                "\n" +
                "\n" +
                "}\n" +
                "}\n" +
                "}}}\n";
        return testJava;
    }

    public void testParseValid() throws Exception
    {
        JavaLineParser parser = new JavaLineParser();
        List<String> lineList = new ArrayList<>();
        lineList.add("import java.io.FileReader");
        lineList.add("java.io.FileInputStream");
        parser.addLines(lineList);
        parser.addLine("blic clas");
        List<String> blockList = new ArrayList<String>();
        blockList.add("public test()");
        parser.addBlocks(blockList);
        parser.addBlock("anotherMethod");
        List<Line> result = parser.parse(getValidTestCode());
        assertEquals(5, result.size());
        Line line1 = result.get(0);
        assertEquals("import java.io.FileInputStream;", line1.getLine());
        assertEquals(3, line1.getLineNumber());
        Line line2 = result.get(1);
        assertEquals("import java.io.FileReader;", line2.getLine());
        assertEquals(4, line2.getLineNumber());
        Line line3 = result.get(2);
        assertEquals("public class JavaLineParserTest extends TestCase", line3.getLine());
        assertEquals(8, line3.getLineNumber());
        Block block1 = (Block)result.get(3);
        assertEquals("    public test()", block1.getLine());
        assertEquals(12, block1.getLineNumber());
        assertEquals(18, block1.getEndLineNumber());
        Block block2 = (Block)result.get(4);
        assertEquals("public anotherMethod()", block2.getLine());
        assertEquals(20, block2.getLineNumber());
        assertEquals(22, block2.getEndLineNumber());
    }

    public void testParseInvalid() throws Exception
    {
        JavaLineParser parser = new JavaLineParser();
        parser.addLine("import java.io.FileReader");
        parser.addBlock("public test()");
        try
        {
            parser.parse(getInvalidTestCode());
            fail();
        }
        catch(RuntimeException exc)
        {
            assertTrue(exc.getMessage().contains("Blocks not found"));
            assertTrue(exc.getMessage().contains("public test()"));
            assertTrue(!exc.getMessage().contains("Lines not found"));
        }

    }

    public void testParseDeeplyNested() throws Exception
    {
        String testJava = getNestedTestCode();
        JavaLineParser parser = new JavaLineParser();
        parser.addBlock("test");
        List<Line> result = parser.parse(testJava);
        assertEquals(1, result.size());
        Block block1 = (Block)result.get(0);
        assertEquals("test", block1.getLine());
        assertEquals(1, block1.getLineNumber());
        assertEquals(16, block1.getEndLineNumber());
    }

    public void testParseError() throws Exception
    {
        JavaLineParser parser = new JavaLineParser();
        List<String> lineList = new ArrayList<>();
        lineList.add("imprt java.io.FileReader");
        lineList.add("java.io.FileInputStream");
        parser.addLines(lineList);
        parser.addLine("blic clas");
        List<String> blockList = new ArrayList<>();
        blockList.add("public test)");
        parser.addBlocks(blockList);
        parser.addBlock("anotherethod");
        try
        {
            parser.parse(getValidTestCode());
            fail();
        }
        catch(RuntimeException exc)
        {
            assertTrue(exc.getMessage().contains("Lines not found"));
            assertTrue(exc.getMessage().contains("imprt java.io.FileReader"));
            assertTrue(exc.getMessage().contains("Blocks not found"));
            assertTrue(exc.getMessage().contains("public test)"));
            assertTrue(exc.getMessage().contains("anotherethod"));
        }
    }

    public void testProcessValid() throws Exception
    {
        String testCode = getValidTestCode();
        JavaLineProcessor processor = new JavaLineProcessor();
        List<String> lineList = new ArrayList<>();
        lineList.add("import java.io.FileReader");
        processor.addLines(lineList);
        processor.addLine("java.io.FileInputStream");
        List<String> blockList = new ArrayList<>();
        blockList.add("public test()");
        processor.addBlocks(blockList);
        processor.addBlock("anotherMethod");
        String result = processor.process(testCode);
        assertTrue(result.contains("//import java.io.FileReader"));
        assertTrue(result.contains("//import java.io.FileInputStream"));
        assertTrue(result.contains("    /*public test()" + NL + "{" + NL + " //do it" + NL + " if(true)" + NL + "{" +
                NL + "  \t}" + NL + "}*/"));
        assertTrue(result.contains("/*public anotherMethod()" + NL + "{" + NL + "}*/"));
        assertEquals(stripChars(testCode), stripChars(result));
    }

    public void testProcessDeeplyNested() throws Exception
    {
        String testJava = getNestedTestCode();
        JavaLineProcessor processor = new JavaLineProcessor();
        List<String> blockList = new ArrayList<>();
        blockList.add("test");
        processor.addBlocks(blockList);
        String result = processor.process(testJava);
        assertTrue(result.trim().startsWith("/*"));
        assertTrue(result.trim().endsWith("*/"));
    }

    public void testProcessCommentAll() throws Exception
    {
        String testCode = getValidTestCode();
        JavaLineProcessor processor = new JavaLineProcessor();
        List<String> lineList = new ArrayList<>();
        lineList.add("");
        processor.addLines(lineList);
        String result = processor.process(testCode);
        BufferedReader reader = new BufferedReader(new StringReader(result));
        String currentLine = null;
        while((currentLine = reader.readLine()) != null)
        {
            assertTrue(currentLine.trim().startsWith("//"));
        }
    }

    public void testProcessDoubleLine() throws Exception
    {
        String testCode = "First line\nSecond line\nFirst line";
        JavaLineProcessor processor = new JavaLineProcessor();
        List<String> lineList = new ArrayList<>();
        lineList.add("First line");
        processor.addLines(lineList);
        String result = processor.process(testCode);
        BufferedReader reader = new BufferedReader(new StringReader(result));
        assertTrue(reader.readLine().trim().startsWith("//"));
        assertFalse(reader.readLine().trim().startsWith("//"));
        assertTrue(reader.readLine().trim().startsWith("//"));
    }

    public void testProcessDoubleBlock() throws Exception
    {
        String testCode = "Block\n{\n}\notherBlock\n{\n}\notherBlock\n{\n}\n";
        JavaLineProcessor processor = new JavaLineProcessor();
        List<String> blockList = new ArrayList<>();
        blockList.add("otherBlock");
        processor.addBlocks(blockList);
        String result = processor.process(testCode);
        String expected = "Block" + NL + "{" + NL + "}" + NL + "/*otherBlock" + NL + "{" + NL + "}*/" + NL + "/*otherBlock" + NL + "{" + NL + "}*/" + NL;
        assertEquals(result, expected);
    }

    private String stripChars(String theString)
    {
        StringBuilder buffer = new StringBuilder(theString);
        int ii = 0;
        while(ii < buffer.length())
        {
            char currentChar = buffer.charAt(ii);
            if(Character.isWhitespace(currentChar) || currentChar == '/' || currentChar == '*')
            {
                buffer.deleteCharAt(ii);
            }
            else
            {
                ii++;
            }
        }
        return buffer.toString();
    }
}
