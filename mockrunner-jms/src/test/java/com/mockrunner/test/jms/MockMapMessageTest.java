package com.mockrunner.test.jms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import javax.jms.MessageFormatException;
import javax.jms.MessageNotWriteableException;

import org.junit.Test;

import com.mockrunner.mock.jms.MockMapMessage;

public class MockMapMessageTest
{
	@Test
    public void testReadWrite() throws Exception
    {
        MockMapMessage message = new MockMapMessage();
        message.setBoolean("boolean1", true);
        assertTrue(message.itemExists("boolean1"));
        assertEquals("true", message.getString("boolean1"));
        assertEquals(true, message.getBoolean("boolean1"));
        message.setString("string1", "12.3");
        assertEquals(12.3, message.getFloat("string1"), 0.01);
        assertEquals(12.3, message.getDouble("string1"), 0.01);
        assertEquals("12.3", message.getString("string1"));
        assertEquals("12.3", message.getObject("string1"));
        try
        {
            message.getLong("string1");
            fail();
        }
        catch(NumberFormatException exc)
        {
            //should throw exception
        }
        message.setObject("byteObject1", (byte) 1);
        assertEquals("1", message.getString("byteObject1"));
        assertEquals(1, message.getLong("byteObject1"));
        assertEquals(1, message.getInt("byteObject1"));
        assertEquals(1, message.getShort("byteObject1"));
        assertEquals(1, message.getByte("byteObject1"));
        try
        {
            message.getDouble("byteObject1");
            fail();
        }
        catch(MessageFormatException exc)
        {
            //should throw exception
        }
        message.setInt("int1", 123);
        assertEquals(123, message.getLong("int1")); 
        try
        {
            message.getShort("int1");
            fail();
        }
        catch(MessageFormatException exc)
        {
            //should throw exception
        }
        byte[] data = new byte[] {1, 2, 3};
        message.setBytes("bytes1", data);
        assertTrue(Arrays.equals(message.getBytes("bytes1"), new byte[] {1, 2, 3}));
        message.setBytes("bytes2", data, 1, 2);
        assertTrue(Arrays.equals(message.getBytes("bytes2"), new byte[] {2, 3}));
        try
        {
            message.getString("bytes1");
            fail();
        }
        catch(MessageFormatException exc)
        {
            //should throw exception
        }
        assertTrue(Arrays.equals((byte[])message.getObject("bytes1"), new byte[] {1, 2, 3}));
        message.setObject("null", null);
        assertTrue(message.itemExists("null"));
        try
        {
            message.getInt("null");
            fail();
        }
        catch(MessageFormatException exc)
        {
            //should throw exception
        }
        assertNull(message.getString("null"));
    }
    
	@Test
    public void testReadOnly() throws Exception
    {
        MockMapMessage message = new MockMapMessage();
        message.setString("test", "test");
        message.setBoolean("boolean", true);
        message.setReadOnly(true);
        try
        {
            message.setString("test", "anothertest");
            fail();
        } 
        catch(MessageNotWriteableException exc)
        {
            //should throw exception
        }
        try
        {
            message.setBoolean("boolean", false);
            fail();
        } 
        catch(MessageNotWriteableException exc)
        {
            //should throw exception
        }
        try
        {
            message.setInt("int", 1);
            fail();
        } 
        catch(MessageNotWriteableException exc)
        {
            //should throw exception
        }
        assertEquals("test", message.getString("test"));
        assertTrue(message.getBoolean("boolean"));
        assertFalse(message.itemExists("int"));
        message.clearBody();
        message.setInt("int", 1);
        assertTrue(message.itemExists("int"));
    }
    
	@Test
    public void testNullName() throws Exception
    {
        MockMapMessage message = new MockMapMessage();
        try
        {
            message.setDouble(null, 123.4);
            fail();
        } 
        catch(IllegalArgumentException exc)
        {
            //should throw exception
        }
        try
        {
            message.setBytes("", new byte[0]);
            fail();
        } 
        catch(IllegalArgumentException exc)
        {
            //should throw exception
        }
        try
        {
            message.setString(null, "test");
            fail();
        } 
        catch(IllegalArgumentException exc)
        {
            //should throw exception
        }
    }
    
	@Test
    public void testEquals() throws Exception
    {
        MockMapMessage message1 = new MockMapMessage();
        message1.setInt("name1", 1);
        message1.setString("name2", "text");
        message1.setBytes("name3", new byte[] {1, 2, 3});
        assertTrue(message1.equals(message1));
        MockMapMessage message2 = null;
        assertFalse(message1.equals(message2));
        message2 = new MockMapMessage();
        assertFalse(message1.equals(message2));
        assertTrue(message2.equals(new MockMapMessage()));
        assertEquals(message2.hashCode(), new MockMapMessage().hashCode());
        message2.setInt("name1", 1);
        message2.setString("name2", "text");
        message2.setBytes("name3", new byte[] {1, 2, 1});
        assertFalse(message1.equals(message2));
        assertFalse(message2.equals(message1));
        message2.setBytes("name3", new byte[] {1, 2, 3});
        assertTrue(message1.equals(message2));
        assertTrue(message2.equals(message1));
        assertEquals(message1.hashCode(), message2.hashCode());
        message2.setString("name4", "text");
        assertFalse(message1.equals(message2));
        message2.setString("name4", null);
        assertFalse(message1.equals(message2));
        assertFalse(message2.equals(message1));
        message1.setString("name4", null);
        assertTrue(message1.equals(message2));
        assertTrue(message2.equals(message1));
        assertEquals(message1.hashCode(), message2.hashCode());
        message2.setBytes("name3", new byte[] {});
        assertFalse(message1.equals(message2));
        assertFalse(message2.equals(message1));
    }
    
	@Test
    public void testGetMap() throws Exception
    {
        MockMapMessage message = new MockMapMessage();
        assertTrue(message.getMap().isEmpty());
        message.setString("1", "value1");
        message.setInt("2", 2);
        assertEquals(2, message.getMap().size());
        assertEquals("value1", message.getMap().get("1"));
        assertEquals(2, message.getMap().get("2"));
        message.getMap().put("3", "3");
        assertEquals(2, message.getMap().size());
        assertEquals("value1", message.getMap().get("1"));
        assertEquals(2, message.getMap().get("2"));
        MockMapMessage otherMessage = new MockMapMessage();
        otherMessage.setString("1", "value1");
        otherMessage.setInt("2", 2);
        assertTrue(message.equals(otherMessage));
    }
    
	@Test
    public void testClone() throws Exception
    {
        MockMapMessage message = new MockMapMessage();
        MockMapMessage newMessage = (MockMapMessage)message.clone();
        assertNotSame(message, newMessage);
        assertEquals(message, newMessage);
        message.setFloat("float", 2.0f);
        message.setString("string", "text");
        byte[] myArray = new byte[]{1, 2, 3};
        message.setBytes("bytes", myArray);
        newMessage = (MockMapMessage)message.clone();
        assertNotSame(message, newMessage);
        assertEquals(message, newMessage);
        assertEquals(2.0f, message.getFloat("float"), 0);
        assertEquals("text", message.getString("string"));
        assertNotSame(myArray, message.getBytes("bytes"));
        assertTrue(Arrays.equals(myArray, message.getBytes("bytes")));
    }
    
	@Test
    public void testToString() throws Exception
    {
        MockMapMessage message = new MockMapMessage();
        assertEquals(MockMapMessage.class.getName() + ": {}", message.toString());
        message.setInt("name", 3);
        assertEquals(MockMapMessage.class.getName() + ": {name=3}", message.toString());
    }
}
