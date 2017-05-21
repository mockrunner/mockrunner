package com.mockrunner.gen.jar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.kirkk.analyzer.framework.Jar;

public class JarFileExtractor
{
    private List<String> mainJars;
    private List<String> exceptionJars;

    public JarFileExtractor(List<String> mainJars, List<String> exceptionJars)
    {
        this.mainJars = new ArrayList<>(mainJars);
        this.exceptionJars = new ArrayList<>(exceptionJars);
    }

    public Jar[] filter(Jar jars[])
    {
        List<Jar> finalList = new ArrayList<>();
        for (Jar jar : jars) {
            if (mainJars.contains(jar.getJarFileName())) {
                finalList.add(jar);
            }
        }
        return finalList.toArray(new Jar[finalList.size()]);
    }

    public Map<String,Set<String>> createDependencies(Jar jars[])
    {
        Map<String,Set<String>> finalMap = new HashMap<>();
        for (Jar jar : jars) {
            if (mainJars.contains(jar.getJarFileName())) {
                Set<String> currentSet = createDependencySet(jar);
                finalMap.put(jar.getJarFileName(), currentSet);
            }
        }
        return finalMap;
    }

    private Set<String> createDependencySet(Jar jar)
    {
        Set<String> resultSet = new TreeSet<>();
        List<Jar> dependendJars = jar.getOutgoingDependencies();
        if(null == dependendJars) return resultSet;
        for (Jar currentJar : dependendJars) {
            String currentJarFileName = currentJar.getJarFileName();
            resultSet.add(currentJarFileName);
            if (!exceptionJars.contains(currentJarFileName)) {
                resultSet.addAll(createDependencySet(currentJar));
            }
        }
        return resultSet;
    }
}
