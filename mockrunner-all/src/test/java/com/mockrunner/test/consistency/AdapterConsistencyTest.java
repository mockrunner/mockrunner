package com.mockrunner.test.consistency;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.mockrunner.base.BasicHTMLOutputTestCase;
import com.mockrunner.base.BasicWebTestCase;
import com.mockrunner.base.HTMLOutputModule;
import com.mockrunner.base.HTMLOutputTestCase;
import com.mockrunner.base.WebTestCase;
import com.mockrunner.base.WebTestModule;
import com.mockrunner.connector.BasicConnectorTestCaseAdapter;
import com.mockrunner.connector.ConnectorTestCaseAdapter;
import com.mockrunner.connector.ConnectorTestModule;
import com.mockrunner.ejb.BasicEJBTestCaseAdapter;
import com.mockrunner.ejb.EJBTestCaseAdapter;
import com.mockrunner.ejb.EJBTestModule;
import com.mockrunner.jdbc.BasicJDBCTestCaseAdapter;
import com.mockrunner.jdbc.JDBCTestCaseAdapter;
import com.mockrunner.jdbc.JDBCTestModule;
//import com.mockrunner.jms.BasicJMSTestCaseAdapter;
//import com.mockrunner.jms.JMSTestCaseAdapter;
//import com.mockrunner.jms.JMSTestModule;
import com.mockrunner.servlet.BasicServletTestCaseAdapter;
import com.mockrunner.servlet.ServletTestCaseAdapter;
import com.mockrunner.servlet.ServletTestModule;
import com.mockrunner.struts.ActionTestCaseAdapter;
import com.mockrunner.struts.ActionTestModule;
import com.mockrunner.struts.BasicActionTestCaseAdapter;
import com.mockrunner.tag.BasicTagTestCaseAdapter;
import com.mockrunner.tag.TagTestCaseAdapter;
import com.mockrunner.tag.TagTestModule;

public class AdapterConsistencyTest extends TestCase
{
    private Map<Class<?>, List<Class<?>>> adapterMap;
    private Map<Class<?>, List<String>> exceptionMap;
    private List<FailureEntry> failureList;

    @Override
  protected void setUp() throws Exception
    {
        super.setUp();
        failureList = new ArrayList<>();
        adapterMap = new HashMap<>();
        exceptionMap = new HashMap<>();
        initializeTestModules();
    }

    protected void initializeTestModules()
    {
        addAdapter(ServletTestModule.class, ServletTestCaseAdapter.class);
        addAdapter(ServletTestModule.class, BasicServletTestCaseAdapter.class);
        addExceptionMethod(ServletTestModule.class, "getOutput");

        addAdapter(TagTestModule.class, TagTestCaseAdapter.class);
        addAdapter(TagTestModule.class, BasicTagTestCaseAdapter.class);
        addExceptionMethod(TagTestModule.class, "getOutput");

        addAdapter(ActionTestModule.class, ActionTestCaseAdapter.class);
        addAdapter(ActionTestModule.class, BasicActionTestCaseAdapter.class);
        addExceptionMethod(ActionTestModule.class, "getOutput");

        addAdapter(EJBTestModule.class, EJBTestCaseAdapter.class);
        addAdapter(EJBTestModule.class, BasicEJBTestCaseAdapter.class);

        addAdapter(JDBCTestModule.class, JDBCTestCaseAdapter.class);
        addAdapter(JDBCTestModule.class, BasicJDBCTestCaseAdapter.class);

//        addAdapter(JMSTestModule.class, JMSTestCaseAdapter.class);
//        addAdapter(JMSTestModule.class, BasicJMSTestCaseAdapter.class);

        addAdapter(ConnectorTestModule.class, ConnectorTestCaseAdapter.class);
        addAdapter(ConnectorTestModule.class, BasicConnectorTestCaseAdapter.class);

        addAdapter(HTMLOutputModule.class, HTMLOutputTestCase.class);
        addAdapter(HTMLOutputModule.class, BasicHTMLOutputTestCase.class);

        addAdapter(WebTestModule.class, WebTestCase.class);
        addAdapter(WebTestModule.class, BasicWebTestCase.class);
    }

    protected void addAdapter(Class<?> module, Class<?> adapter)
    {
        List<Class<?>> adapterList = adapterMap.get(module);
        if(null == adapterList)
        {
            adapterList = new ArrayList<>();
            adapterMap.put(module, adapterList);
            exceptionMap.put(module, new ArrayList<>());
        }
        adapterList.add(adapter);
    }

    protected void addExceptionMethod(Class<?> module, String method)
    {
        List<String> exceptionList = exceptionMap.get(module);
        if(null == exceptionList)
        {
            exceptionList = new ArrayList<>();
            exceptionMap.put(module, exceptionList);
        }
        exceptionList.add(method);
    }

    public void testAdapterConsistency()
    {
        for (Class<?> currentModule : adapterMap.keySet()) {
            List<Class<?>> adapterList = adapterMap.get(currentModule);
            for (Class<?> currentAdapter : adapterList) {
                doTestAdapter(currentModule, currentAdapter);
            }
        }
        dumpFailures();
    }

    private void doTestAdapter(Class<?> currentModule, Class<?> currentAdapter)
    {
        Method[] adapterMethods = currentAdapter.getDeclaredMethods();
        Method[] moduleMethods = currentModule.getDeclaredMethods();
        for (Method currentMethod : moduleMethods) {
            if (shouldMethodBeChecked(currentMethod, currentModule)) {
                if (!isMethodDeclared(currentMethod, adapterMethods)) {
                    addFailureEntry(currentModule, currentAdapter, currentMethod);
                }
            }
        }
    }

    private boolean shouldMethodBeChecked(Method theMethod, Class<?> module)
    {
        if(!Modifier.isPublic(theMethod.getModifiers())) return false;
        List<String> exceptionMethods = exceptionMap.get(module);
        return !(null != exceptionMethods && exceptionMethods.contains(theMethod.getName()));
    }

    private boolean isMethodDeclared(Method theMethod, Method[] otherClassMethods)
    {
        for (Method currentMethod : otherClassMethods) {
            if (areMethodsEqual(theMethod, currentMethod)) {
                return true;
            }
        }
        return false;
    }

    private boolean areMethodsEqual(Method aMethod, Method anotherMethod)
    {
        if(Modifier.isPrivate(anotherMethod.getModifiers())) return false;
        if(!aMethod.getName().equals(anotherMethod.getName())) return false;
        if(!aMethod.getReturnType().equals(anotherMethod.getReturnType())) return false;
        Class<?>[] aMethodParams = aMethod.getParameterTypes();
        Class<?>[] anotherMethodParams = anotherMethod.getParameterTypes();
        return Arrays.equals(aMethodParams, anotherMethodParams);
    }

    private void addFailureEntry(Class<?> currentModule, Class<?> currentAdapter, Method missingMethod)
    {
        failureList.add(new FailureEntry(currentModule, currentAdapter, missingMethod));
    }

    private void dumpFailures()
    {
        for (FailureEntry currentEntry : failureList) {
            Class<?> currentModule = currentEntry.getModule();
            Class<?> currentAdapter = currentEntry.getAdapter();
            Method currentMethod = currentEntry.getMethod();
            System.out.println("Failure in " + currentAdapter.getName() + " (Module: " + currentModule.getName() + ")");
            System.out.println("The following method is missing:");
            System.out.println();
            Class<?>[] params = currentMethod.getParameterTypes();
            System.out.println("Name: " + currentMethod.getName());
            System.out.println("Return type: " + currentMethod.getReturnType().getName());
            if (null != params && params.length > 0) {
                System.out.print("Parameter: ");
                for (Class<?> currentParam : params) {
                    System.out.print(currentParam.getName() + "; ");
                }
                System.out.println();
            }
            System.out.println();
        }
        assertTrue(failureList.isEmpty());
    }

    private class FailureEntry
    {
        private Class<?> module;
        private Class<?> adapter;
        private Method method;

        public FailureEntry(Class<?> module, Class<?> adapter, Method method)
        {
            this.module = module;
            this.adapter = adapter;
            this.method = method;
        }

        public Class<?> getAdapter()
        {
            return adapter;
        }

        public Method getMethod()
        {
            return method;
        }

        public Class<?> getModule()
        {
            return module;
        }
    }
}
