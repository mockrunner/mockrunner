package com.mockrunner.gen.proc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class PackageImportSorter
{
    private String[] order;

    public PackageImportSorter()
    {
        order = new String[4];
        order[0] = "java";
        order[1] = "javax";
        order[2] = "org";
        order[3] = "com";
    }

    public List<Set<String>> sortBlocks(List<String> imports)
    {
        Map<String,Group> groups = new HashMap<>();
        Set<String> sortedGroups = new TreeSet<>();
        List<Set<String>> resultList = new ArrayList<>();
        initializeGroups(imports, groups, sortedGroups);
        if(groups.isEmpty())
        {
            prepareSingleBlock(imports, resultList);
            return resultList;
        }
        classifyImports(groups, sortedGroups);
        prepareResultList(groups, resultList);
        return resultList;
    }

    private void prepareSingleBlock(List<String> imports, List<Set<String>> resultList)
    {
        Set<String> block = new TreeSet<String>(imports);
        resultList.add(block);
    }

    private List<String> getGroupsAsSortedList()
    {
        Set<String> allGroupSet = new TreeSet<>();
        Collections.addAll(allGroupSet, order);
        List<String> allGroups = new ArrayList<>(allGroupSet);
        Collections.reverse(allGroups);
        return allGroups;
    }

    private void prepareResultList(Map<String,Group> groups, List<Set<String>> resultList)
    {
        for (String anOrder : order) {
            Group currentGroup = groups.get(anOrder);
            if (null != currentGroup) {
                addIfNotEmpty(resultList, currentGroup.getBeforeGroup());
                addIfNotEmpty(resultList, currentGroup.getActualGroup());
                addIfNotEmpty(resultList, currentGroup.getAfterGroup());
            }
        }
    }

    private void addIfNotEmpty(List<Set<String>> list, Set<String> set)
    {
        if(null == set || set.isEmpty()) return;
        list.add(set);
    }

    private void initializeGroups(List<String> imports, Map<String, Group> groups, Set<String> sortedGroups)
    {
        List<String> allGroups = getGroupsAsSortedList();
        sortedGroups.addAll(imports);
        for (Object anImport : imports) {
            String currentImport = (String) anImport;
            createGroupIfMatching(allGroups, groups, sortedGroups, currentImport);
        }
        sortedGroups.addAll(groups.keySet());
    }

    private void createGroupIfMatching(List<String> allGroups, Map<String, Group> groups, Set<String> sortedGroups, String currentImport)
    {
        for (String groupName : allGroups) {
            if (currentImport.startsWith(groupName)) {
                Group group = getGroupByName(groups, groupName);
                group.addToActualGroup(currentImport);
                sortedGroups.remove(currentImport);
                return;
            }
        }
    }

    private Group getGroupByName(Map<String, Group> groups, String groupName)
    {
        Group group = groups.get(groupName);
        if(null == group)
        {
            group = new Group(groupName);
            groups.put(groupName, group);
        }
        return group;
    }

    private void classifyImports(Map<String,Group> groups, Set<String> sortedGroups)
    {
        Group currentGroup = null;
        Set<String> tempBeforeGroup = new TreeSet<>();
        for (String currentImport : sortedGroups) {
            Group tempGroup = groups.get(currentImport);
            currentGroup = handleTempGroup(currentGroup, tempBeforeGroup, currentImport, tempGroup);
        }
    }

    private Group handleTempGroup(Group currentGroup, Set<String> tempBeforeGroup, String currentImport, Group tempGroup)
    {
        if(null != tempGroup)
        {
            if(null == currentGroup)
            {
                tempGroup.addAllToBeforeGroup(tempBeforeGroup);
            }
            currentGroup = tempGroup;
        }
        else
        {
            if(null == currentGroup)
            {
                tempBeforeGroup.add(currentImport);
            }
            else
            {
                currentGroup.addToAfterGroup(currentImport);
            }
        }
        return currentGroup;
    }

    private class Group
    {
        private String groupName;
        private Set<String> beforeGroup;
        private Set<String> actualGroup;
        private Set<String> afterGroup;

        public Group(String groupName)
        {
            this.groupName = groupName;
            beforeGroup = new TreeSet<>();
            actualGroup = new TreeSet<>();
            afterGroup = new TreeSet<>();
        }

        public String getGroupName()
        {
            return groupName;
        }

        public Set<String> getActualGroup()
        {
            return actualGroup;
        }

        public Set<String> getAfterGroup()
        {
            return afterGroup;
        }

        public Set<String> getBeforeGroup()
        {
            return beforeGroup;
        }

        public void addAllToBeforeGroup(Set<String> importSet)
        {
            beforeGroup.addAll(importSet);
        }

        public void addAllToActualGroup(Set<String> importSet)
        {
            actualGroup.addAll(importSet);
        }

        public void addAllToAfterGroup(Set<String> importSet)
        {
            afterGroup.addAll(importSet);
        }

        public void addToBeforeGroup(String importString)
        {
            beforeGroup.add(importString);
        }

        public void addToActualGroup(String importString)
        {
            actualGroup.add(importString);
        }

        public void addToAfterGroup(String importString)
        {
            afterGroup.add(importString);
        }
    }
}
