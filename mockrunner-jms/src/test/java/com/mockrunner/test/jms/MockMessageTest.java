package com.mockrunner.test.jms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotWriteableException;

import org.junit.Test;

import com.mockrunner.mock.jms.MockMessage;
import com.mockrunner.mock.jms.MockQueue;
import com.mockrunner.mock.jms.MockTopic;

public class MockMessageTest
{
	@Test
    public void testPropertyTypes() throws Exception
    {
        MockMessage message = new MockMessage();
        message.setStringProperty("string1", "123.4");
        assertEquals("123.4", message.getObjectProperty("string1"));
        assertEquals("123.4", message.getStringProperty("string1"));
        assertEquals(123.4, message.getDoubleProperty("string1"), 0.01);
        assertEquals(123.4, message.getFloatProperty("string1"), 0.01);
        try
        {
            message.getLongProperty("string1");
            fail();
        }
        catch(NumberFormatException exc)
        {
            //should throw Exception
        }
        message.setDoubleProperty("double1", 123.4);
        assertEquals(123.4, message.getObjectProperty("double1"));
        assertEquals("123.4", message.getStringProperty("double1"));
        assertEquals(123.4, message.getDoubleProperty("double1"), 0.01);
        message.setFloatProperty("float1", 123.4f);
        assertEquals(123.4f, message.getObjectProperty("float1"));
        assertEquals("123.4", message.getStringProperty("float1"));
        assertEquals(123.4, message.getFloatProperty("float1"), 0.01);
        try
        {
            message.getLongProperty("float1");
            fail();
        }
        catch(MessageFormatException exc)
        {
            //should throw Exception
        }
        try
        {
            message.getFloatProperty("double1");
            fail();
        }
        catch(MessageFormatException exc)
        {
            //should throw Exception
        }
        message.setByteProperty("byte1", (byte)123);
        assertEquals((byte) 123, message.getObjectProperty("byte1"));
        assertEquals("123", message.getStringProperty("byte1"));
        assertEquals((byte)123, message.getByteProperty("byte1"));
        assertEquals(123, message.getLongProperty("byte1"));
        assertEquals(123, message.getIntProperty("byte1"));
        assertEquals(123, message.getShortProperty("byte1"));
        assertEquals(123, message.getLongProperty("byte1"));
        try
        {
            message.getBooleanProperty("byte1");
            fail();
        }
        catch(MessageFormatException exc)
        {
            //should throw Exception
        }
        message.setIntProperty("int1", 12345);
        assertEquals(12345, message.getObjectProperty("int1"));
        assertEquals("12345", message.getStringProperty("int1"));
        assertEquals(12345, message.getLongProperty("int1"));
        assertEquals(12345, message.getIntProperty("int1"));
        try
        {
            message.getShortProperty("int1");
            fail();
        }
        catch(MessageFormatException exc)
        {
            //should throw Exception
        }
        message.setBooleanProperty("boolean1", true);
        assertEquals(Boolean.TRUE, message.getObjectProperty("boolean1"));
        assertEquals("true", message.getStringProperty("boolean1"));
        assertEquals(true, message.getBooleanProperty("boolean1"));
        try
        {
            message.getDoubleProperty("boolean1");
            fail();
        }
        catch(MessageFormatException exc)
        {
            //should throw Exception
        }
        Enumeration names = message.getPropertyNames();
        ArrayList nameList = new ArrayList();
        while(names.hasMoreElements())
        {
            nameList.add(names.nextElement());
        }
        assertTrue(nameList.size() == 6);
        assertTrue(nameList.contains("string1"));
        assertTrue(nameList.contains("double1"));
        assertTrue(nameList.contains("float1"));
        assertTrue(nameList.contains("int1"));
        assertTrue(nameList.contains("byte1"));
        assertTrue(nameList.contains("boolean1"));
    }

    @Test
    public void testNullPropertyName() throws Exception
    {
        MockMessage message = new MockMessage();
        try
        {
            message.setDoubleProperty(null, 123.4);
            fail();
        }
        catch(IllegalArgumentException exc)
        {
            //should throw exception
        }
        try
        {
            message.setObjectProperty("", "test");
            fail();
        }
        catch(IllegalArgumentException exc)
        {
            //should throw exception
        }
        try
        {
            message.setByteProperty(null, (byte)1);
            fail();
        }
        catch(IllegalArgumentException exc)
        {
            //should throw exception
        }
    }

    @Test(expected=MessageFormatException.class)
    public void testNullFloatProperty() throws JMSException {
        MockMessage m = new MockMessage();
        m.setObjectProperty("null", null);
        m.getFloatProperty(null);
    }

    @Test(expected=MessageFormatException.class)
    public void testNullIntProperty() throws JMSException {
        MockMessage m = new MockMessage();
        m.setObjectProperty("null", null);
        m.getIntProperty(null);
    }

    @Test(expected=MessageFormatException.class)
    public void testNullByteProperty() throws JMSException {
        MockMessage m = new MockMessage();
        m.setObjectProperty("null", null);
        m.getByteProperty(null);
    }

    @Test(expected=MessageFormatException.class)
    public void testNullShortProperty() throws JMSException {
        MockMessage m = new MockMessage();
        m.setObjectProperty("null", null);
        m.getShortProperty(null);
    }

    @Test(expected=MessageFormatException.class)
    public void testNullLongProperty() throws JMSException {
        MockMessage m = new MockMessage();
        m.setObjectProperty("null", null);
        m.getLongProperty(null);
    }

    @Test(expected=MessageFormatException.class)
    public void testNullDoubleProperty() throws JMSException {
        MockMessage m = new MockMessage();
        m.setObjectProperty("null", null);
        m.getDoubleProperty(null);
    }

    @Test
    public void testNullProperties() throws Exception
    {
        MockMessage message = new MockMessage();
        message.setObjectProperty("null", null);
        assertFalse(message.propertyExists("null"));
        assertNull(message.getObjectProperty("null"));
        assertNull(message.getStringProperty("test"));
        assertFalse(message.getBooleanProperty("null"));
        assertFalse(message.getBooleanProperty("test"));
    }

    @Test
    public void testReadOnlyProperties() throws Exception
    {
        MockMessage message = new MockMessage();
        message.setStringProperty("string", "test");
        message.setReadOnly(true);
        message.setDoubleProperty("double", 123);
        message.setReadOnlyProperties(true);
        try
        {
            message.setStringProperty("string", "anothertest");
            fail();
        }
        catch(MessageNotWriteableException exc)
        {
            //should throw exception
        }
        try
        {
            message.setDoubleProperty("double", 456);
            fail();
        }
        catch(MessageNotWriteableException exc)
        {
            //should throw exception
        }
        message.clearBody();
        try
        {
            message.setBooleanProperty("boolean", true);
            fail();
        }
        catch(MessageNotWriteableException exc)
        {
            //should throw exception
        }
        assertEquals("test", message.getStringProperty("string"));
        assertEquals(123, message.getDoubleProperty("double"), 0);
        assertFalse(message.getBooleanProperty("boolean"));
        assertFalse(message.propertyExists("boolean"));
        message.clearProperties();
        message.setBooleanProperty("boolean", true);
        assertTrue(message.getBooleanProperty("boolean"));
        assertTrue(message.propertyExists("boolean"));
    }

	@Test
    public void testSetAndGetCorrelationID() throws Exception
    {
        MockMessage message = new MockMessage();
        assertNull(message.getJMSCorrelationID());
        assertNull(message.getJMSCorrelationIDAsBytes());
        message.setJMSCorrelationIDAsBytes("ABC".getBytes("ISO-8859-1"));
        assertEquals("ABC", message.getJMSCorrelationID());
        assertTrue(Arrays.equals("ABC".getBytes("ISO-8859-1"), message.getJMSCorrelationIDAsBytes()));
        message.setJMSCorrelationIDAsBytes(null);
        assertNull(message.getJMSCorrelationID());
        assertNull(message.getJMSCorrelationIDAsBytes());
        message.setJMSCorrelationID("test");
        assertEquals("test", message.getJMSCorrelationID());
        assertTrue(Arrays.equals("test".getBytes("ISO-8859-1"), message.getJMSCorrelationIDAsBytes()));
    }

	@Test
    public void testClone() throws Exception
    {
        MockQueue queue = new MockQueue("MyQueue");
        MockTopic topic = new MockTopic("MyTopic");
        MockMessage message = new MockMessage();
        message.setStringProperty("string", "test");
        message.setIntProperty("int", 12345);
        message.setBooleanProperty("boolean", true);
        message.setJMSCorrelationID("testID");
        message.setJMSPriority(3);
        message.setJMSDestination(queue);
        message.setJMSReplyTo(topic);
        message.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
        MockMessage newMessage = (MockMessage)message.clone();
        assertNotSame(message, newMessage);
        assertEquals("test", newMessage.getStringProperty("string"));
        assertEquals(12345, newMessage.getIntProperty("int"));
        assertEquals(true, newMessage.getBooleanProperty("boolean"));
        assertEquals("testID", newMessage.getJMSCorrelationID());
        assertEquals(3, newMessage.getJMSPriority());
        assertEquals(DeliveryMode.NON_PERSISTENT, newMessage.getJMSDeliveryMode());
        assertSame(queue, newMessage.getJMSDestination());
        assertSame(topic, newMessage.getJMSReplyTo());
    }
}