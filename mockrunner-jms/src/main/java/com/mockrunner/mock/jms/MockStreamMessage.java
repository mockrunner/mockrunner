package com.mockrunner.mock.jms;

import java.util.Arrays;
import java.util.Collections;
import java.util.Stack;
import java.util.Vector;

import javax.jms.JMSException;
import javax.jms.MessageEOFException;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotReadableException;
import javax.jms.MessageNotWriteableException;
import javax.jms.StreamMessage;

import com.mockrunner.util.common.ArrayUtil;

/**
 * Mock implementation of JMS <code>StreamMessage</code>.
 */
public class MockStreamMessage extends MockMessage implements StreamMessage
{
    private Stack data;
    private boolean remainingBytesPushed;
    
    public MockStreamMessage()
    {
        data = new Stack();
        remainingBytesPushed = false;
    }

    public boolean readBoolean() throws JMSException
    {
        if(isInWriteMode())
        {
            throw new MessageNotReadableException("Message is in write mode");
        }
        if(data.empty())
        {
            throw new MessageEOFException("No more data");
        }
        Object value = readObject();
        if(null == value) {
            throw new MessageFormatException(getNullDatumMessage("boolean"));
        }
        if(value instanceof Boolean)
        {
            return (Boolean) value;
        }
        if(value instanceof String)
        {
            return Boolean.valueOf((String) value);
        }
        throw new MessageFormatException(value.getClass().getName() + " cannot be converted to boolean");
    }

    public byte readByte() throws JMSException
    {
        if(isInWriteMode())
        {
            throw new MessageNotReadableException("Message is in write mode");
        }
        if(data.empty())
        {
            throw new MessageEOFException("No more data");
        }
        Object value = readObject();
        if(null == value) {
            throw new MessageFormatException(getNullDatumMessage("byte"));
        }
        if(value instanceof Byte)
        {
            return (Byte) value;
        }
        if(value instanceof String)
        {
            return Byte.valueOf((String) value);
        }
        throw new MessageFormatException(value.getClass().getName() + " cannot be converted to byte");
    }

    public short readShort() throws JMSException
    {
        if(isInWriteMode())
        {
            throw new MessageNotReadableException("Message is in write mode");
        }
        if(data.empty())
        {
            throw new MessageEOFException("No more data");
        }
        Object value = readObject();
        if(null == value) {
            throw new MessageFormatException(getNullDatumMessage("short"));
        }
        if((value instanceof Byte) || (value instanceof Short))
        {
            return ((Number)value).shortValue();
        }
        if(value instanceof String)
        {
            return Short.valueOf((String) value);
        }
        throw new MessageFormatException(value.getClass().getName() + " cannot be converted to short");
    }

    public char readChar() throws JMSException
    {
        if(isInWriteMode())
        {
            throw new MessageNotReadableException("Message is in write mode");
        }
        if(data.empty())
        {
            throw new MessageEOFException("No more data");
        }
        Object value = readObject();
        if(null == value)
        {
            throw new NullPointerException();
        }
        if(value instanceof Character)
        {
            return (Character) value;
        }
        throw new MessageFormatException(value.getClass().getName() + " cannot be converted to char");
    }

    public int readInt() throws JMSException
    {
        if(isInWriteMode())
        {
            throw new MessageNotReadableException("Message is in write mode");
        }
        if(data.empty())
        {
            throw new MessageEOFException("No more data");
        }
        Object value = readObject();
        if(null == value) {
            throw new MessageFormatException(getNullDatumMessage("int"));
        }
        if((value instanceof Byte) || (value instanceof Short) || (value instanceof Integer))
        {
            return ((Number)value).intValue();
        }
        if(value instanceof String)
        {
            return Integer.valueOf((String) value);
        }
        throw new MessageFormatException(value.getClass().getName() + " cannot be converted to int");
    }

    public long readLong() throws JMSException
    {
        if(isInWriteMode())
        {
            throw new MessageNotReadableException("Message is in write mode");
        }
        if(data.empty())
        {
            throw new MessageEOFException("No more data");
        }
        Object value = readObject();
        if(null == value) {
            throw new MessageFormatException(getNullDatumMessage("long"));
        }
        if((value instanceof Byte) || (value instanceof Short) || (value instanceof Integer) || (value instanceof Long))
        {
            return ((Number)value).longValue();
        }
        if(value instanceof String)
        {
            return Long.valueOf((String) value);
        }
        throw new MessageFormatException(value.getClass().getName() + " cannot be converted to long");
    }

    public float readFloat() throws JMSException
    {
        if(isInWriteMode())
        {
            throw new MessageNotReadableException("Message is in write mode");
        }
        if(data.empty())
        {
            throw new MessageEOFException("No more data");
        }
        Object value = readObject();
        if(null == value) {
            throw new MessageFormatException(getNullDatumMessage("float"));
        }
        if(value instanceof Float)
        {
            return (Float) value;
        }
        if(value instanceof String)
        {
            return Float.valueOf((String) value);
        }
        throw new MessageFormatException(value.getClass().getName() + " cannot be converted to float");
    }

    public double readDouble() throws JMSException
    {
        if(isInWriteMode())
        {
            throw new MessageNotReadableException("Message is in write mode");
        }
        if(data.empty())
        {
            throw new MessageEOFException("No more data");
        }
        Object value = readObject();
        if(null == value) {
            throw new MessageFormatException(getNullDatumMessage("double"));
        }
        if((value instanceof Float) || (value instanceof Double))
        {
            return ((Number)value).doubleValue();
        }
        if(value instanceof String)
        {
            return Double.valueOf((String) value);
        }
        throw new MessageFormatException(value.getClass().getName() + " cannot be converted to double");
    }

    public String readString() throws JMSException
    {
        if(isInWriteMode())
        {
            throw new MessageNotReadableException("Message is in write mode");
        }
        if(data.empty())
        {
            throw new MessageEOFException("No more data");
        }
        Object value = readObject();
        if(null == value) return null;
        if(value instanceof byte[])
        {
            throw new MessageFormatException(value.getClass().getName() + " cannot be converted to String");
        }
        return value.toString();
    }

    public int readBytes(byte[] byteData) throws JMSException
    {
        if(isInWriteMode())
        {
            throw new MessageNotReadableException("Message is in write mode");
        }
        if(data.empty())
        {
            throw new MessageEOFException("No more data");
        }
        if(null == byteData)
        {
            throw new NullPointerException();
        }
        Object value = readObject();
        if(null == value)
        {
            remainingBytesPushed = false;
            return -1;
        }
        if(!(value instanceof byte[]))
        {
            remainingBytesPushed = false;
            throw new MessageFormatException(value.getClass().getName() + " cannot be converted to byte[]");
        }
        int fieldLength = ((byte[])value).length;
        if(0 == fieldLength)
        {
            if(remainingBytesPushed)
            {
                remainingBytesPushed = false;
                return -1;
            }
            return 0;
        }
        if(0 == byteData.length && remainingBytesPushed)
        {
            remainingBytesPushed = false;
            return -1;
        }
        remainingBytesPushed = false;
        if(fieldLength < byteData.length)
        {
            System.arraycopy(value, 0, byteData, 0, fieldLength);
            return fieldLength;
        }
        System.arraycopy(value, 0, byteData, 0, byteData.length);
        byte[] remaining = new byte[fieldLength - byteData.length];
        System.arraycopy(value, byteData.length, remaining, 0, remaining.length);
        data.push(remaining);
        remainingBytesPushed = true;
        return byteData.length;
    }

    public Object readObject() throws JMSException
    {
        if(isInWriteMode())
        {
            throw new MessageNotReadableException("Message is in write mode");
        }
        if(data.empty())
        {
            throw new MessageEOFException("No more data");
        }
        return data.pop();
    }

    public void writeBoolean(boolean value) throws JMSException
    {
        if(!isInWriteMode())
        {
            throw new MessageNotWriteableException("Message is in read mode");
        }
        writeObject(value);
    }

    public void writeByte(byte value) throws JMSException
    {
        if(!isInWriteMode())
        {
            throw new MessageNotWriteableException("Message is in read mode");
        }
        writeObject(value);
    }

    public void writeShort(short value) throws JMSException
    {
        if(!isInWriteMode())
        {
            throw new MessageNotWriteableException("Message is in read mode");
        }
        writeObject(value);
    }

    public void writeChar(char value) throws JMSException
    {
        if(!isInWriteMode())
        {
            throw new MessageNotWriteableException("Message is in read mode");
        }
        writeObject(value);
    }

    public void writeInt(int value) throws JMSException
    {
        if(!isInWriteMode())
        {
            throw new MessageNotWriteableException("Message is in read mode");
        }
        writeObject(value);
    }

    public void writeLong(long value) throws JMSException
    {
        if(!isInWriteMode())
        {
            throw new MessageNotWriteableException("Message is in read mode");
        }
        writeObject(value);
    }

    public void writeFloat(float value) throws JMSException
    {
        if(!isInWriteMode())
        {
            throw new MessageNotWriteableException("Message is in read mode");
        }
        writeObject(value);
    }

    public void writeDouble(double value) throws JMSException
    {
        if(!isInWriteMode())
        {
            throw new MessageNotWriteableException("Message is in read mode");
        }
        writeObject(value);
    }

    public void writeString(String value) throws JMSException
    {
        if(!isInWriteMode())
        {
            throw new MessageNotWriteableException("Message is in read mode");
        }
        writeObject(value);
    }

    public void writeBytes(byte[] data) throws JMSException
    {
        if(!isInWriteMode())
        {
            throw new MessageNotWriteableException("Message is in read mode");
        }
        writeObject(data);
    }

    public void writeBytes(byte[] data, int offset, int length) throws JMSException
    {
        if(!isInWriteMode())
        {
            throw new MessageNotWriteableException("Message is in read mode");
        }
        if(null == data)
        {
            writeObject(null);
            return;
        }
        writeObject(ArrayUtil.truncateArray(data, offset, length));
    }

    public void writeObject(Object object) throws JMSException
    {
        if(!isInWriteMode())
        {
            throw new MessageNotWriteableException("Message is in read mode");
        }
        if(null == object)
        {
            data.push(object);
            return;
        }
        if((object instanceof String) || (object instanceof Number) || (object instanceof Character) || (object instanceof Boolean))
        {
            data.push(object);
            return;
        }
        if(object instanceof byte[])
        {
            byte[] arrayData = ((byte[])object).clone();
            data.push(arrayData);
            return;
        }
        throw new MessageFormatException(object.getClass() + " not a valid type");
    }

    public void reset() throws JMSException
    {
        setReadOnly(true);
        Collections.reverse(data);
        remainingBytesPushed = false;
    }

    public void clearBody() throws JMSException
    {
        super.clearBody();
        data = new Stack();
        remainingBytesPushed = false;
    }
    
    /**
     * Compares the underlying stream data.
     */
    public boolean equals(Object otherObject)
    {
        if(null == otherObject) return false;
        if(!(otherObject instanceof MockStreamMessage)) return false;
        MockStreamMessage otherMessage = (MockStreamMessage)otherObject;
        if(data.size() != otherMessage.data.size()) return false;
        Vector otherData = otherMessage.data;
        if(isInWriteMode() != otherMessage.isInWriteMode())
        {
            otherData = new Vector(otherData);
            Collections.reverse(otherData);
        }
        for(int ii = 0; ii < data.size(); ii++)
        {
            Object nextValue = data.get(ii);
            Object otherValue = otherData.get(ii);
            if(null == nextValue)
            {
                if(null != otherValue) return false;
            }
            else if(nextValue instanceof byte[])
            {
                if(null == otherValue) return false;
                if(!(otherValue instanceof byte[])) return false;
                if(!Arrays.equals((byte[])nextValue, (byte[])otherValue)) return false;
            }
            else
            {
                if(!nextValue.equals(otherValue)) return false;
            }
        }
        return true;
    }

    public int hashCode()
    {
        int value = 17;
        Vector theData = new Vector(data);
        if(isInWriteMode())
        {
            Collections.reverse(theData);
        }
        for (Object nextValue : theData) {
            if (nextValue instanceof byte[]) {
                for (int yy = 0; yy < ((byte[]) nextValue).length; yy++) {
                    value = (31 * value) + ((byte[]) nextValue)[yy];
                }
            } else if (nextValue != null) {
                value = (31 * value) + nextValue.hashCode();
            }
        }
        return value;
    }
    
    public Object clone()
    {
        MockStreamMessage message = (MockStreamMessage)super.clone();
        message.data = new Stack();
        for(int ii = 0; ii < data.size(); ii++)
        {
            Object nextValue = data.get(ii);
            if(nextValue instanceof byte[])
            {
                message.data.add(((byte[])nextValue).clone());
            }
            else
            {
                message.data.add(nextValue);
            }
        }
        return message;
    }

    public String toString()
    {
        return this.getClass().getName() + ": " + data.toString();
    }

    private String getNullDatumMessage(String typename) {
      return String.format("Cannot convert null to a %s.", typename);
    }
}
