package com.mockrunner.test.util;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

public class FileUtilTest extends TestCase
{
	//TODO breaks upon release

    public void testGetLinesFromFile()
    {
//        File file = new File("target/test-classes/com/mockrunner/test/util/testlines.txt");
//        List lineList = FileUtil.getLinesFromFile(file);
//        doTestLines(lineList);
    }

    public void testFindFile() throws IOException
    {
//        File file = FileUtil.findFile("target/test-classes/com/mockrunner/test/util/testlines.txt");
//        assertNotNull(file);
//        List lineList = FileUtil.getLinesFromFile(file);
//        doTestLines(lineList);
//        file = FileUtil.findFile("/com/mockrunner/test/util/testlines.txt");
//        assertNotNull(file);
//        lineList = FileUtil.getLinesFromFile(file);
//        doTestLines(lineList);
//        file = FileUtil.findFile("com/mockrunner/test/util/testlines.txt");
//        assertNotNull(file);
//        lineList = FileUtil.getLinesFromFile(file);
//        assertNotNull(FileUtil.findFile("FileUtil.class"));
//        assertNotNull(FileUtil.findFile("com/mockrunner/test/util/file name with blanks.txt"));
//        try
//        {
//            FileUtil.findFile("notfound");
//            fail();
//        }
//        catch(FileNotFoundException exc)
//        {
//            //should throw exception
//        }
    }

    private void doTestLines(List<String> lineList)
    {
        assertTrue(lineList.size() == 6);
        assertEquals("line1", lineList.get(0));
        assertEquals("line2", lineList.get(1));
        assertEquals("line3", lineList.get(2));
        assertEquals("", lineList.get(3));
        assertEquals("line4", lineList.get(4));
        assertEquals("line5", lineList.get(5));
    }
}
