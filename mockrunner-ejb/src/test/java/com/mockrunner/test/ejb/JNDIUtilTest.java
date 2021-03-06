package com.mockrunner.test.ejb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockejb.jndi.MockContextFactory;

import com.mockrunner.ejb.Configuration;
import com.mockrunner.ejb.JNDIUtil;
import com.mockrunner.mock.ejb.MockUserTransaction;
import com.mockrunner.test.ejb.TestJNDI.NullContext;

public class JNDIUtilTest
{
    private Properties savedProperties;
    private Context context;
    
    @Before
    public void setUp() throws Exception
    {
        savedProperties = new Properties();
        TestJNDI.saveProperties(savedProperties);
        MockContextFactory.setAsInitial();
        context = new InitialContext();
    }

    @After
    public void tearDown() throws Exception
    {
        MockContextFactory.revertSetAsInitial();
        TestJNDI.restoreProperties(savedProperties);
    }

    @Test
    public void testInitMockContextFactory() throws Exception
    {
        System.getProperties().remove(Context.INITIAL_CONTEXT_FACTORY);
        JNDIUtil.initMockContextFactory();
        assertEquals(MockContextFactory.class.getName(), System.getProperty(Context.INITIAL_CONTEXT_FACTORY));
        assertEquals("org.mockejb.jndi", System.getProperty(Context.URL_PKG_PREFIXES));
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "test");
        JNDIUtil.initMockContextFactory();
        assertEquals(MockContextFactory.class.getName(), System.getProperty(Context.INITIAL_CONTEXT_FACTORY));
        assertEquals("org.mockejb.jndi", System.getProperty(Context.URL_PKG_PREFIXES));
        System.setProperty(Context.URL_PKG_PREFIXES, "test");
        JNDIUtil.initMockContextFactory();
        assertEquals(MockContextFactory.class.getName(), System.getProperty(Context.INITIAL_CONTEXT_FACTORY));
        assertEquals("test", System.getProperty(Context.URL_PKG_PREFIXES));
    }
    
    @Test
    public void testResetMockContextFactory() throws Exception
    {
        System.getProperties().remove(Context.INITIAL_CONTEXT_FACTORY);
        JNDIUtil.resetMockContextFactory();
        assertNull(System.getProperty(Context.INITIAL_CONTEXT_FACTORY));
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "test");
        JNDIUtil.resetMockContextFactory();
        assertEquals("test", System.getProperty(Context.INITIAL_CONTEXT_FACTORY));
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, MockContextFactory.class.getName());
        JNDIUtil.resetMockContextFactory();
        assertNull(System.getProperty(Context.INITIAL_CONTEXT_FACTORY));
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "test");
        System.setProperty(Context.URL_PKG_PREFIXES, "testURL");
        JNDIUtil.initMockContextFactory();
        JNDIUtil.resetMockContextFactory();
        assertEquals("test", System.getProperty(Context.INITIAL_CONTEXT_FACTORY));
        assertEquals("testURL", System.getProperty(Context.URL_PKG_PREFIXES));
    }
    
    @Test
    public void testGetContext() throws Exception
    {
        Configuration configuration = new Configuration();
        NullContext testContext = new NullContext();
        configuration.setContext(testContext);
        Context context = JNDIUtil.getContext(configuration);
        assertSame(context, testContext);
        configuration.setContext(null);
        context = JNDIUtil.getContext(configuration);
        assertNotSame(context, testContext);
    }
    
    @Test
    public void testBindUserTransaction() throws Exception
    {
        Configuration configuration = new Configuration();
        MockUserTransaction transaction = new MockUserTransaction();
        configuration.setBindMockUserTransactionToJNDI(false);
        JNDIUtil.bindUserTransaction(configuration, context, transaction);
        try
        {
            context.lookup("javax.transaction.UserTransaction");
            fail();
        } 
        catch(NameNotFoundException exc)
        {
            //should throw exception
        }
        try
        {
            context.lookup("java:comp/UserTransaction");
            fail();
        } 
        catch(NameNotFoundException exc)
        {
            //should throw exception
        }
        configuration.setBindMockUserTransactionToJNDI(true);
        configuration.setUserTransactionJNDIName("myJNDIName");
        JNDIUtil.bindUserTransaction(configuration, context, transaction);
        assertSame(context.lookup("myJNDIName"), transaction);
        assertSame(context.lookup("javax.transaction.UserTransaction"), transaction);
        assertSame(context.lookup("java:comp/UserTransaction"), transaction);
    }
}
