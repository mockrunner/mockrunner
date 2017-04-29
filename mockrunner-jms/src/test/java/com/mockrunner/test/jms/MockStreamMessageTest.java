package com.mockrunner.test.jms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import javax.jms.JMSException;
import javax.jms.MessageEOFException;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotReadableException;
import javax.jms.MessageNotWriteableException;

import org.junit.Test;

import com.mockrunner.mock.jms.MockStreamMessage;

public class MockStreamMessageTest
{
	@Test
    public void testReadWrite() throws Exception
    {
        MockStreamMessage message = new MockStreamMessage();
        message.writeString("test");
        try
        {
            message.readString();
            fail();
        }
        catch(MessageNotReadableException exc)
        {
            //should throw exception
        }
        message.reset();
        try
        {
            message.writeByte((byte)1);
            fail();
        }
        catch(MessageNotWriteableException exc)
        {
            //should throw exception
        }
        assertEquals("test", message.readString());
        try
        {
            message.readInt();
            fail();
        }
        catch(MessageEOFException exc)
        {
            //should throw exception
        }
        message = new MockStreamMessage();
        message.writeString("123");
        message.writeString("456");
        message.writeString("789");
        message.writeString("111");
        message.writeString("222");
        message.writeString("333");
        message.reset();
        assertEquals(123, message.readByte());
        assertEquals(456, message.readShort());
        assertEquals(789, message.readInt());
        assertEquals(111, message.readLong());
        assertEquals(222, message.readFloat(), 0);
        assertEquals(333, message.readDouble(), 0);
        try
        {
            message.readBoolean();
            fail();
        }
        catch(MessageEOFException exc)
        {
            //should throw exception
        }
        message = new MockStreamMessage();
        message.writeLong(1);
        message.reset();
        try
        {
            message.readInt();
            fail();
        }
        catch(MessageFormatException exc)
        {
            //should throw exception
        }
        message = new MockStreamMessage();
        message.writeByte((byte)1);
        message.writeByte((byte)1);
        message.writeByte((byte)1);
        message.writeByte((byte)1);
        message.reset();
        assertEquals(1, message.readByte());
        assertEquals(1, message.readShort());
        assertEquals(1, message.readInt());
        assertEquals(1, message.readLong());
        message = new MockStreamMessage();
        message.writeBoolean(false);
        message.reset();
        try
        {
            message.readFloat();
            fail();
        }
        catch(MessageFormatException exc)
        {
            //should throw exception
        }
        message = new MockStreamMessage();
        message.writeBoolean(false);
        message.writeBoolean(true);
        message.reset();
        assertEquals(false, message.readBoolean());
        assertEquals("true", message.readString());
        message.clearBody();
        message.writeDouble(123.4);
        message.reset();
        assertEquals("123.4", message.readString());
        message.clearBody();
        message.writeString("a");
        message.reset();
        try
        {
            message.readChar();
            fail();
        }
        catch(MessageFormatException exc)
        {
            //should throw exception
        }
        message.clearBody();
        message.writeChar('a');
        message.reset();
        assertEquals("a", message.readString());
        message = new MockStreamMessage();
        byte[] data = new byte[] {1, 2, 3, 4};
        message.writeBytes(data);
        message.writeBytes(data);
        message.reset();
        data = new byte[4];
        int number = message.readBytes(data);
        assertEquals(4, number);
        assertTrue(Arrays.equals(data, new byte[] {1, 2, 3, 4}));
        message.readBytes(data);
        data = (byte[])message.readObject();
        assertTrue(Arrays.equals(data, new byte[] {1, 2, 3, 4}));
        message = new MockStreamMessage();
        data = new byte[] {1, 2, 3, 4};
        message.writeBytes(data);
        message.reset();
        data = new byte[5];
        number = message.readBytes(data);
        assertEquals(4, number);
        assertTrue(Arrays.equals(data, new byte[] {1, 2, 3, 4, 0}));
        data = new byte[] {1, 2, 3, 4};
        message.clearBody();
        message.writeBytes(data, 3, 1);
        message.reset();
        data = new byte[5];
        number = message.readBytes(data);
        assertEquals(1, number);
        assertTrue(Arrays.equals(data, new byte[] {4, 0, 0, 0, 0}));
        data = new byte[] {1, 2, 3, 4};
        message.clearBody();
        message.writeBytes(data);
        message.reset();
        data = new byte[1]; 
        number = message.readBytes(data);
        assertEquals(1, number);
        assertTrue(Arrays.equals(data, new byte[] { 1 }));
        data = new byte[] {1, 2, 3, 4};
        message.clearBody();
        message.writeBytes(data);
        message.reset();
        data = new byte[0];
        number = message.readBytes(data);
        assertEquals(0, number);
        data = new byte[] {1, 2, 3, 4};
        message.clearBody();
        message.writeBytes(data);
        message.reset(); 
        message = new MockStreamMessage();
        message.writeObject(null);
        message.writeObject(null);
        message.writeObject(null);
        message.writeObject(3);
        message.writeObject("4");
        message.writeObject((byte) 5);
        message.reset();
        try
        {
            message.readChar();
            fail();
        }
        catch(NullPointerException exc)
        {
            //should throw exception
        }
        try
        {
            message.readDouble();
            fail();
        }
        catch(MessageFormatException exc)
        {
            //should throw exception
        }
        assertNull(message.readObject());
        assertEquals(3, message.readLong());
        assertEquals(4, message.readByte());
        try
        {
            message.readFloat();
            fail();
        }
        catch(MessageFormatException exc)
        {
            //should throw exception
        }
        try
        {
            message.readFloat();
            fail();
        }
        catch(MessageEOFException exc)
        {
            //should throw exception
        }
    }
    
	@Test
    public void testReadBytes() throws Exception
    {
        MockStreamMessage message = new MockStreamMessage();
        message.writeBytes(new byte[] {1, 2, 3, 4, 5, 6, 7});
        byte[] buffer = new byte[10];
        message.reset();
        int number = message.readBytes(buffer);
        assertEquals(7, number);
        assertTrue(Arrays.equals(buffer, new byte[] {1, 2, 3, 4, 5, 6, 7, 0, 0, 0}));
        try
        {
            message.readBytes(buffer);
            fail();
        }
        catch(MessageEOFException exc)
        {
            //should throw exception
        }
        message.clearBody();
        message.writeBytes(new byte[] {1, 2, 3, 4, 5, 6, 7});
        buffer = new byte[5];
        message.reset();
        number = message.readBytes(buffer);
        assertEquals(5, number);
        assertTrue(Arrays.equals(buffer, new byte[] {1, 2, 3, 4, 5}));
        number = message.readBytes(buffer);
        assertEquals(2, number);
        assertTrue(Arrays.equals(buffer, new byte[] {6, 7, 3, 4, 5}));
        try
        {
            message.readBytes(buffer);
            fail();
        }
        catch(MessageEOFException exc)
        {
            //should throw exception
        }
        message.clearBody();
        message.writeBytes(new byte[] {1, 2, 3, 4, 5, 6, 7});
        buffer = new byte[3];
        message.reset();
        number = message.readBytes(buffer);
        assertEquals(3, number);
        assertTrue(Arrays.equals(buffer, new byte[] {1, 2, 3}));
        number = message.readBytes(buffer);
        assertEquals(3, number);
        assertTrue(Arrays.equals(buffer, new byte[] {4, 5, 6}));
        number = message.readBytes(buffer);
        assertEquals(1, number);
        assertTrue(Arrays.equals(buffer, new byte[] {7, 5, 6}));
        try
        {
            message.readBytes(buffer);
            fail();
        }
        catch(MessageEOFException exc)
        {
            //should throw exception
        }
        message.clearBody();
        message.writeBytes(new byte[] {1, 2, 3, 4, 5, 6, 7});
        buffer = new byte[0];
        message.reset();
        number = message.readBytes(buffer);
        assertEquals(0, number);
        number = message.readBytes(buffer);
        assertEquals(-1, number);
        message.clearBody();
        message.writeBytes(new byte[] {1, 2, 3, 4, 5, 6, 7});
        buffer = new byte[7];
        message.reset();
        number = message.readBytes(buffer);
        assertEquals(7, number);
        assertTrue(Arrays.equals(buffer, new byte[] {1, 2, 3, 4, 5, 6, 7}));
        number = message.readBytes(buffer);
        assertEquals(-1, number);
        message.clearBody();
        message.writeBytes(new byte[] {1, 2, 3, 4, 5, 6, 7});
        message.reset();
        try
        {
            number = message.readBytes(null);
            fail();
        }
        catch(NullPointerException exc)
        {
            //should throw exception
        }
        message.clearBody();
        message.writeBytes(new byte[0]);
        message.reset();
        number = message.readBytes(new byte[1]);
        assertEquals(0, number);
        try
        {
            message.readBytes(new byte[1]);
            fail();
        }
        catch(MessageEOFException exc)
        {
            //should throw exception
        }
        message.clearBody();
        message.writeBytes(new byte[] {1, 2, 3, 4, 5, 6, 7});
        buffer = new byte[7];
        message.reset();
        number = message.readBytes(buffer);
        try
        {
            message.readByte();
            fail();
        }
        catch(MessageFormatException exc)
        {
            //should throw exception
        }
    }
    
	@Test
    public void testEquals() throws Exception
    {
        MockStreamMessage message1 = new MockStreamMessage();
        message1.writeString("test");
        message1.writeObject(1L);
        message1.writeBytes(new byte[] {1, 2, 3});
        assertTrue(message1.equals(message1));
        MockStreamMessage message2 = null;
        assertFalse(message1.equals(message2));
        message2 = new MockStreamMessage();
        assertFalse(message1.equals(message2));
        assertFalse(message2.equals(message1));
        message2.writeString("test");
        message2.writeBytes(new byte[] {1, 2, 3});
        message2.writeObject(1L);
        assertFalse(message1.equals(message2));
        assertFalse(message2.equals(message1));
        message2 = new MockStreamMessage();
        message2.writeString("test");
        message2.writeObject(1L);
        message2.writeBytes(new byte[] {});
        assertFalse(message1.equals(message2));
        assertFalse(message2.equals(message1));
        message2 = new MockStreamMessage();
        message2.writeString("test");
        message2.writeObject(1L);
        message2.writeBytes(new byte[] {1, 2, 3});
        assertTrue(message1.equals(message2));
        assertTrue(message2.equals(message1));
        assertEquals(message1.hashCode(), message2.hashCode());
        message2.writeString(null);
        assertFalse(message1.equals(message2));
        assertFalse(message2.equals(message1));
        message1.writeString(null);
        assertTrue(message1.equals(message2));
        assertTrue(message2.equals(message1));
        assertEquals(message1.hashCode(), message2.hashCode());
        message1.reset();
        assertTrue(message1.equals(message2));
        assertTrue(message2.equals(message1));
        assertEquals(message1.hashCode(), message2.hashCode());
    }
    
	@Test
    public void testClone() throws Exception
    {
        MockStreamMessage message = new MockStreamMessage();
        MockStreamMessage newMessage = (MockStreamMessage)message.clone();
        assertNotSame(message, newMessage);
        assertEquals(message, newMessage);
        message = new MockStreamMessage();
        message.writeByte((byte)1);
        message.writeBytes(new byte[]{1, 2, 3, 4, 5});
        message.writeBoolean(true);
        newMessage = (MockStreamMessage)message.clone();
        assertNotSame(message, newMessage);
        assertEquals(message, newMessage);
        try
        {
            newMessage.readBoolean();
            fail();
        }
        catch(JMSException exc)
        {
            //should throw exception
        }
        newMessage.reset();
        assertEquals(1, newMessage.readByte());
        byte[] myArray = new byte[6];
        newMessage.readBytes(myArray);
        assertTrue(Arrays.equals(new byte[]{1, 2, 3, 4, 5, 0}, myArray));
        assertTrue(newMessage.readBoolean());
        message = new MockStreamMessage();
        message.writeString("test1");
        message.writeString("test2");
        message.reset();
        newMessage = (MockStreamMessage)message.clone();
        assertEquals("test1", message.readString());
        assertEquals("test2", message.readString());
    }
    
	@Test
    public void testToString() throws Exception
    {
        MockStreamMessage message = new MockStreamMessage();
        assertEquals(MockStreamMessage.class.getName() + ": []", message.toString());
        message.writeInt(12);
        message.writeString("abc");
        assertEquals(MockStreamMessage.class.getName() + ": [12, abc]", message.toString());
    }
}
