package com.mockrunner.test.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.kirkk.analyzer.framework.Jar;
import com.kirkk.analyzer.framework.bcelbundle.JarImpl;
import com.mockrunner.gen.jar.JarFileExtractor;

public class JarFileExtractorTest extends TestCase
{
    private JarFileExtractor getJarFileExtractor(List<String> mainJars, List<String> exceptionJars)
    {
        return new JarFileExtractor(mainJars, exceptionJars);
    }

    private List<String> getMainJars()
    {
        List<String> jars = new ArrayList<>();
        jars.add("test1.jar");
        jars.add("test2.jar");
        jars.add("test3.jar");
        return jars;
    }

    private List<String> getExceptionJars()
    {
        List<String> jars = new ArrayList<>();
        jars.add("nested1.jar");
        return jars;
    }

    private JarImpl createEmptyJar(String name)
    {
        return new JarImpl(name);
    }

    private JarImpl createJarWithDependencies(String name, String[] dependencies)
    {
        JarImpl jar = createEmptyJar(name);
        for (String dependency : dependencies) {
            jar.addOutgoingDependency(createEmptyJar(dependency));
        }
        return jar;
    }

    public void testFilter()
    {
        JarFileExtractor extractor = getJarFileExtractor(getMainJars(), new ArrayList<>());
        Jar[] jars = new Jar[5];
        jars[0] = createEmptyJar("test1.jar");
        jars[1] = createEmptyJar("test2.jar");
        jars[2] = createEmptyJar("test3.jar");
        jars[3] = createEmptyJar("test4.jar");
        jars[4] = createEmptyJar("test5.jar");
        jars = extractor.filter(jars);
        assertEquals(3, jars.length);
        assertTrue(jars[0].getJarFileName().equals("test1.jar"));
        assertTrue(jars[1].getJarFileName().equals("test2.jar"));
        assertTrue(jars[2].getJarFileName().equals("test3.jar"));
    }

    public void testEmpty()
    {
        JarFileExtractor extractor = getJarFileExtractor(getMainJars(), new ArrayList<>());
        Map<String, Set<String>> dependencies = extractor.createDependencies(new Jar[0]);
        assertEquals(0, dependencies.size());
    }

    public void testNoDependencies()
    {
        JarFileExtractor extractor = getJarFileExtractor(getMainJars(), new ArrayList<>());
        Jar[] jars = new Jar[3];
        jars[0] = createEmptyJar("test1.jar");
        jars[1] = createEmptyJar("test2.jar");
        jars[2] = createEmptyJar("test3.jar");
        Map<String,Set<String>> dependencies = extractor.createDependencies(jars);
        assertEquals(3, dependencies.size());
        assertTrue(dependencies.get("test1.jar").isEmpty());
        assertTrue(dependencies.get("test2.jar").isEmpty());
        assertTrue(dependencies.get("test3.jar").isEmpty());
    }

    public void testDependencies()
    {
        JarFileExtractor extractor = getJarFileExtractor(getMainJars(), new ArrayList<>());
        Jar[] jars = new Jar[1];
        jars[0] = createJarWithDependencies("test1.jar", new String[] {"1", "2", "3"});
        Map<String, Set<String>> dependencies = extractor.createDependencies(jars);
        assertEquals(1, dependencies.size());
        Set<String> jarSet = dependencies.get("test1.jar");
        assertEquals(3, jarSet.size());
        assertTrue(jarSet.contains("1"));
        assertTrue(jarSet.contains("2"));
        assertTrue(jarSet.contains("3"));
    }

    public void testRecursiveDependencies()
    {
        JarFileExtractor extractor = getJarFileExtractor(getMainJars(), new ArrayList<>());
        JarImpl jar1 = createEmptyJar("test1.jar");
        JarImpl nested1 = createJarWithDependencies("nested1.jar", new String[] {"1", "2", "3"});
        JarImpl nested2 = createJarWithDependencies("nested2.jar", new String[] {"4", "5", "6"});
        jar1.addOutgoingDependency(nested1);
        jar1.addOutgoingDependency(nested2);
        JarImpl jar2 = createJarWithDependencies("test2.jar", new String[] {"7", "8", "9"});
        Jar[] jars = new Jar[] {jar1, jar2};
        Map<String, Set<String>> dependencies = extractor.createDependencies(jars);
        assertEquals(2, dependencies.size());
        Set<String> jars1 = dependencies.get("test1.jar");
        assertEquals(8, jars1.size());
        assertTrue(jars1.contains("1"));
        assertTrue(jars1.contains("2"));
        assertTrue(jars1.contains("3"));
        assertTrue(jars1.contains("4"));
        assertTrue(jars1.contains("5"));
        assertTrue(jars1.contains("6"));
        assertTrue(jars1.contains("nested1.jar"));
        assertTrue(jars1.contains("nested2.jar"));
        Set<String> jars2 = dependencies.get("test2.jar");
        assertEquals(3, jars2.size());
        assertTrue(jars2.contains("7"));
        assertTrue(jars2.contains("8"));
        assertTrue(jars2.contains("9"));
        extractor = getJarFileExtractor(getMainJars(), getExceptionJars());
        dependencies = extractor.createDependencies(jars);
        assertEquals(2, dependencies.size());
        jars1 = dependencies.get("test1.jar");
        assertEquals(5, jars1.size());
        assertTrue(jars1.contains("4"));
        assertTrue(jars1.contains("5"));
        assertTrue(jars1.contains("6"));
        assertTrue(jars1.contains("nested1.jar"));
        assertTrue(jars1.contains("nested2.jar"));
        jars2 = dependencies.get("test2.jar");
        assertEquals(3, jars2.size());
        assertTrue(jars2.contains("7"));
        assertTrue(jars2.contains("8"));
        assertTrue(jars2.contains("9"));
    }
}
