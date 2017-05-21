package com.mockrunner.test.gen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.mockrunner.gen.proc.PackageImportSorter;

import junit.framework.TestCase;

public class PackageImportSorterTest extends TestCase
{
    private PackageImportSorter sorter;

    @Override
    protected void setUp() throws Exception
    {
        sorter = new PackageImportSorter();
    }

    private List<String> getMatchingTestList()
    {
        List<String> imports = new ArrayList<>();
        imports.add("com.abc.base.Base");
        imports.add("java.util.List");
        imports.add("com.mockrunner.struts.ActionTestCaseAdapter");
        imports.add("java.util.Enumeration");
        imports.add("javax.servlet.jsp.JspException");
        imports.add("org.apache.commons.beanutils.BeanUtils");
        imports.add("javax.sql.DataSource");
        imports.add("java.io.BufferedWriter");
        imports.add("org.apache.struts.action.DynaActionFormClass");
        imports.add("org.mypack.TestClass");
        imports.add("javax.servlet.ServletRequest");
        imports.add("javax.swing.Box");
        imports.add("java.net.URL");
        imports.add("com.mockrunner.test.gen.PackageImportSorterTest");
        return imports;
    }

    private List<String> getNonMatchingTestList()
    {
        List<String> imports = new ArrayList<>();
        imports.add("co.bc.DEF");
        imports.add("co.ac.ABC");
        imports.add("abc.def.Base");
        imports.add("zzzz.yyy.YYY");
        imports.add("uuu.ooo.PPP");
        imports.add("coy.ooo.PPP");
        imports.add("mem.abc.ABC");
        return imports;
    }

    public void testMatchingGroups()
    {
        List<Set<String>> result = sorter.sortBlocks(getMatchingTestList());
        assertEquals(4, result.size());
        Iterator<String> block1 = (result.get(0)).iterator();
        assertEquals("java.io.BufferedWriter", block1.next());
        assertEquals("java.net.URL", block1.next());
        assertEquals("java.util.Enumeration", block1.next());
        assertEquals("java.util.List", block1.next());
        Iterator<String> block2 = (result.get(1)).iterator();
        assertEquals("javax.servlet.ServletRequest", block2.next());
        assertEquals("javax.servlet.jsp.JspException", block2.next());
        assertEquals("javax.sql.DataSource", block2.next());
        assertEquals("javax.swing.Box", block2.next());
        Iterator<String> block3 = (result.get(2)).iterator();
        assertEquals("org.apache.commons.beanutils.BeanUtils", block3.next());
        assertEquals("org.apache.struts.action.DynaActionFormClass", block3.next());
        assertEquals("org.mypack.TestClass", block3.next());
        Iterator<String> block4 = (result.get(3)).iterator();
        assertEquals("com.abc.base.Base", block4.next());
        assertEquals("com.mockrunner.struts.ActionTestCaseAdapter", block4.next());
        assertEquals("com.mockrunner.test.gen.PackageImportSorterTest", block4.next());
    }

    public void testNonMatchingGroups()
    {
        List<Set<String>> result = sorter.sortBlocks(getNonMatchingTestList());
        assertEquals(1, result.size());
        Iterator<String> block = (result.get(0)).iterator();
        assertEquals("abc.def.Base", block.next());
        assertEquals("co.ac.ABC", block.next());
        assertEquals("co.bc.DEF", block.next());
        assertEquals("coy.ooo.PPP", block.next());
        assertEquals("mem.abc.ABC", block.next());
        assertEquals("uuu.ooo.PPP", block.next());
        assertEquals("zzzz.yyy.YYY", block.next());
    }

    public void testMixedGroups()
    {
        List<String> testList = new ArrayList<String>();
        testList.addAll(getNonMatchingTestList());
        testList.addAll(getMatchingTestList());
        List<Set<String>> result = sorter.sortBlocks(testList);
        assertEquals(8, result.size());
        Iterator<String> block1 = (result.get(0)).iterator();
        assertEquals("java.io.BufferedWriter", block1.next());
        assertEquals("java.net.URL", block1.next());
        assertEquals("java.util.Enumeration", block1.next());
        assertEquals("java.util.List", block1.next());
        Iterator<String> block2 = (result.get(1)).iterator();
        assertEquals("javax.servlet.ServletRequest", block2.next());
        assertEquals("javax.servlet.jsp.JspException", block2.next());
        assertEquals("javax.sql.DataSource", block2.next());
        assertEquals("javax.swing.Box", block2.next());
        Iterator<String> block3 = (result.get(2)).iterator();
        assertEquals("mem.abc.ABC", block3.next());
        Iterator<String> block4 = (result.get(3)).iterator();
        assertEquals("org.apache.commons.beanutils.BeanUtils", block4.next());
        assertEquals("org.apache.struts.action.DynaActionFormClass", block4.next());
        assertEquals("org.mypack.TestClass", block4.next());
        Iterator<String> block5 = (result.get(4)).iterator();
        assertEquals("uuu.ooo.PPP", block5.next());
        assertEquals("zzzz.yyy.YYY", block5.next());
        Iterator<String> block6 = (result.get(5)).iterator();
        assertEquals("abc.def.Base", block6.next());
        assertEquals("co.ac.ABC", block6.next());
        assertEquals("co.bc.DEF", block6.next());
        Iterator<String> block7 = (result.get(6)).iterator();
        assertEquals("com.abc.base.Base", block7.next());
        assertEquals("com.mockrunner.struts.ActionTestCaseAdapter", block7.next());
        assertEquals("com.mockrunner.test.gen.PackageImportSorterTest", block7.next());
        Iterator<String> block8 = (result.get(7)).iterator();
        assertEquals("coy.ooo.PPP", block8.next());
    }

    public void testMixedWithSingleGroup()
    {
        List<String> testList = new ArrayList<>();
        testList.addAll(getNonMatchingTestList());
        testList.add("java.util.Enumeration");
        List<Set<String>> result = sorter.sortBlocks(testList);
        assertEquals(3, result.size());
        Iterator<String> block1 = (result.get(0)).iterator();
        assertEquals("abc.def.Base", block1.next());
        assertEquals("co.ac.ABC", block1.next());
        assertEquals("co.bc.DEF", block1.next());
        assertEquals("coy.ooo.PPP", block1.next());
        Iterator<String> block2 = (result.get(1)).iterator();
        assertEquals("java.util.Enumeration", block2.next());
        Iterator<String> block3 = (result.get(2)).iterator();
        assertEquals("mem.abc.ABC", block3.next());
        assertEquals("uuu.ooo.PPP", block3.next());
        assertEquals("zzzz.yyy.YYY", block3.next());
    }

    public void testMixedWithTwoGroups()
    {
        List<String> testList = new ArrayList<>();
        testList.addAll(getNonMatchingTestList());
        testList.add("java.util.Enumeration");
        testList.add("org.mypack.TestClass");
        List<Set<String>> result = sorter.sortBlocks(testList);
        assertEquals(5, result.size());
        Iterator<String> block1 = (result.get(0)).iterator();
        assertEquals("abc.def.Base", block1.next());
        assertEquals("co.ac.ABC", block1.next());
        assertEquals("co.bc.DEF", block1.next());
        assertEquals("coy.ooo.PPP", block1.next());
        Iterator<String> block2 = (result.get(1)).iterator();
        assertEquals("java.util.Enumeration", block2.next());
        Iterator<String> block3 = (result.get(2)).iterator();
        assertEquals("mem.abc.ABC", block3.next());
        Iterator<String> block4 = (result.get(3)).iterator();
        assertEquals("org.mypack.TestClass", block4.next());
        Iterator<String> block5 = (result.get(4)).iterator();
        assertEquals("uuu.ooo.PPP", block5.next());
        assertEquals("zzzz.yyy.YYY", block5.next());
    }

    public void testMixedWithThreeGroups()
    {
        List<String> testList = new ArrayList<>();
        testList.addAll(getNonMatchingTestList());
        testList.add("java.util.Enumeration");
        testList.add("org.mypack.TestClass");
        testList.add("javax.sql.DataSource");
        List<Set<String>> result = sorter.sortBlocks(testList);
        assertEquals(6, result.size());
        Iterator<String> block1 = (result.get(0)).iterator();
        assertEquals("abc.def.Base", block1.next());
        assertEquals("co.ac.ABC", block1.next());
        assertEquals("co.bc.DEF", block1.next());
        assertEquals("coy.ooo.PPP", block1.next());
        Iterator<String> block2 = (result.get(1)).iterator();
        assertEquals("java.util.Enumeration", block2.next());
        Iterator<String> block3 = (result.get(2)).iterator();
        assertEquals("javax.sql.DataSource", block3.next());
        Iterator<String> block4 = (result.get(3)).iterator();
        assertEquals("mem.abc.ABC", block4.next());
        Iterator<String> block5 = (result.get(4)).iterator();
        assertEquals("org.mypack.TestClass", block5.next());
        Iterator<String> block6 = (result.get(5)).iterator();
        assertEquals("uuu.ooo.PPP", block6.next());
        assertEquals("zzzz.yyy.YYY", block6.next());
    }
}
