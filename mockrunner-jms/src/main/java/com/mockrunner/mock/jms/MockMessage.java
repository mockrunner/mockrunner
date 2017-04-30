package com.mockrunner.mock.jms;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotWriteableException;

import com.mockrunner.base.NestedApplicationException;

/**
 * Mock implementation of JMS <code>Message</code>.
 */
public class MockMessage implements Message, Cloneable, Serializable
{
    private String messageId;
    private long timestamp;
    private String correlationId;
    private Destination replyTo;
    private Destination destination;
    private int deliveryMode;
    private boolean redelivered;
    private String type;
    private long expiration;
    private int priority;
    private boolean acknowledged;
    private Map properties;
    private boolean isInWriteMode;
    private boolean isInWriteModeProperties;
    
    public MockMessage()
    {
        messageId = null;
        timestamp = System.currentTimeMillis();
        deliveryMode = DeliveryMode.PERSISTENT;
        redelivered = false;
        expiration = 0;
        priority = 4;
        acknowledged = false;
        properties = new HashMap();
        isInWriteMode = true;
        isInWriteModeProperties = true;
    }
    
    public boolean isAcknowledged()
    {
        return acknowledged;
    }
    
    public String getJMSMessageID() throws JMSException
    {
        return messageId;
    }

    public void setJMSMessageID(String messageId) throws JMSException
    {
        this.messageId = messageId;
    }

    public long getJMSTimestamp() throws JMSException
    {
        return timestamp;
    }

    public void setJMSTimestamp(long timestamp) throws JMSException
    {
        this.timestamp = timestamp;
    }

    public byte[] getJMSCorrelationIDAsBytes() throws JMSException
    {
        if(null == correlationId) return null;
        try
        {
            return correlationId.getBytes("ISO-8859-1");
        }
        catch(UnsupportedEncodingException exc)
        {
            throw new JMSException(exc.getMessage());
        }
    }

    public void setJMSCorrelationIDAsBytes(byte[] correlationId) throws JMSException
    {
        try
        {
            if(null == correlationId)
            {
                this.correlationId = null;
            }
            else
            {
                this.correlationId = new String(correlationId, "ISO-8859-1");
            }
        }
        catch(UnsupportedEncodingException exc)
        {
            throw new JMSException(exc.getMessage());
        }
    }

    public void setJMSCorrelationID(String correlationId) throws JMSException
    {
        this.correlationId = correlationId;
    }

    public String getJMSCorrelationID() throws JMSException
    {
        return correlationId;
    }

    public Destination getJMSReplyTo() throws JMSException
    {
        return replyTo;
    }

    public void setJMSReplyTo(Destination replyTo) throws JMSException
    {
        this.replyTo = replyTo;
    }

    public Destination getJMSDestination() throws JMSException
    {
        return destination;
    }

    public void setJMSDestination(Destination destination) throws JMSException
    {
        this.destination = destination;
    }

    public int getJMSDeliveryMode() throws JMSException
    {
        return deliveryMode;
    }

    public void setJMSDeliveryMode(int deliveryMode) throws JMSException
    {
        this.deliveryMode = deliveryMode;
    }

    public boolean getJMSRedelivered() throws JMSException
    {
        return redelivered;
    }

    public void setJMSRedelivered(boolean redelivered) throws JMSException
    {
        this.redelivered = redelivered;
    }

    public String getJMSType() throws JMSException
    {
        return type;
    }

    public void setJMSType(String type) throws JMSException
    {
        this.type = type;
    }

    public long getJMSExpiration() throws JMSException
    {
        return expiration;
    }

    public void setJMSExpiration(long expiration) throws JMSException
    {
        this.expiration = expiration;
    }

    public int getJMSPriority() throws JMSException
    {
        return priority;
    }

    public void setJMSPriority(int priority) throws JMSException
    {
        this.priority = priority;
    }

    public void clearProperties() throws JMSException
    {
        isInWriteModeProperties = true;
        properties.clear();
    }

    public boolean propertyExists(String name) throws JMSException
    {
        return properties.containsKey(name);
    }

    public boolean getBooleanProperty(String name) throws JMSException
    {
        Object value = getObjectProperty(name);
        if(value == null)
        {
            return false;
        }
        if(value instanceof String)
        {
            return Boolean.valueOf((String) value);
        }
        if(value instanceof Boolean)
        {
            return (Boolean) value;
        }
        throw new MessageFormatException("Cannot convert property " + name + " of type " + value.getClass().getName() + " to boolean");
    }

    public byte getByteProperty(String name) throws JMSException
    {
        Object value = getObjectProperty(name);
        if(value == null)
        {
            throw new MessageFormatException(getNullPropertyMessage(name));
        }
        if(value instanceof String)
        {
            return Byte.valueOf((String) value);
        }
        if(value instanceof Byte)
        {
            return ((Number)value).byteValue();
        }
        throw new MessageFormatException("Cannot convert property " + name + " of type " + value.getClass().getName() + " to byte");
    }

    public short getShortProperty(String name) throws JMSException
    {
        Object value = getObjectProperty(name);
        if(value == null)
        {
            throw new MessageFormatException(getNullPropertyMessage(name));
        }
        if(value instanceof String)
        {
            return Short.valueOf((String) value);
        }
        if((value instanceof Short) || (value instanceof Byte))
        {
            return ((Number)value).shortValue();
        }
        throw new MessageFormatException("Cannot convert property " + name + " of type " + value.getClass().getName() + " to short");
    }

    public int getIntProperty(String name) throws JMSException
    {
        Object value = getObjectProperty(name);
        if(value == null)
        {
            throw new MessageFormatException(getNullPropertyMessage(name));
        }
        if(value instanceof String)
        {
            return Integer.valueOf((String) value);
        }
        if((value instanceof Integer) || (value instanceof Short) || (value instanceof Byte))
        {
            return ((Number)value).intValue();
        }
        throw new MessageFormatException("Cannot convert property " + name + " of type " + value.getClass().getName() + " to int");
    }

    public long getLongProperty(String name) throws JMSException
    {
        Object value = getObjectProperty(name);
        if(value == null)
        {
            throw new MessageFormatException(getNullPropertyMessage(name));
        }
        if(value instanceof String)
        {
            return Long.valueOf((String) value);
        }
        if((value instanceof Long) || (value instanceof Integer) || (value instanceof Short) || (value instanceof Byte))
        {
            return ((Number)value).longValue();
        }
        throw new MessageFormatException("Cannot convert property " + name + " of type " + value.getClass().getName() + " to long");
    }

    public float getFloatProperty(String name) throws JMSException
    {
        Object value = getObjectProperty(name);
        if(value == null)
        {
            throw new MessageFormatException(getNullPropertyMessage(name));
        }
        if(value instanceof String)
        {
            return Float.valueOf((String) value);
        }
        if(value instanceof Float)
        {
            return ((Number)value).floatValue();
        }
        throw new MessageFormatException("Cannot convert property " + name + " of type " + value.getClass().getName() + " to float");
    }

    public double getDoubleProperty(String name) throws JMSException
    {
        Object value = getObjectProperty(name);
        if(value == null)
        {
            throw new MessageFormatException(getNullPropertyMessage(name));
        }
        if(value instanceof String)
        {
           return Double.valueOf((String) value);
        }
        if((value instanceof Double) || (value instanceof Float))
        {
            return ((Number)value).doubleValue();
        }
        throw new MessageFormatException("Cannot convert property " + name + " of type " + value.getClass().getName() + " to double");
    }

    public String getStringProperty(String name) throws JMSException
    {
        Object value = getObjectProperty(name);
        if(null == value) return null;
        return value.toString();
    }

    public Object getObjectProperty(String name) throws JMSException
    {
        return properties.get(name);
    }

    public Enumeration getPropertyNames() throws JMSException
    {
        return new Vector(properties.keySet()).elements();
    }

    public void setBooleanProperty(String name, boolean value) throws JMSException
    {
        setObjectProperty(name, value);
    }

    public void setByteProperty(String name, byte value) throws JMSException
    {
        setObjectProperty(name, value);
    }

    public void setShortProperty(String name, short value) throws JMSException
    {
        setObjectProperty(name, value);
    }

    public void setIntProperty(String name, int value) throws JMSException
    {
        setObjectProperty(name, value);
    }

    public void setLongProperty(String name, long value) throws JMSException
    {
        setObjectProperty(name, value);
    }

    public void setFloatProperty(String name, float value) throws JMSException
    {
        setObjectProperty(name, value);
    }

    public void setDoubleProperty(String name, double value) throws JMSException
    {
        setObjectProperty(name, value);
    }

    public void setStringProperty(String name, String value) throws JMSException
    {
        setObjectProperty(name, value);
    }

    public void setObjectProperty(String name, Object object) throws JMSException
    {
        if(!isInWriteModeProperties)
        {
            throw new MessageNotWriteableException("Message is in read mode");
        }
        if(null == name || name.length() <= 0)
        {
            throw new IllegalArgumentException("Property names must not be null or empty strings");
        }
        if(null == object) return;
        if((object instanceof String) || (object instanceof Number) || (object instanceof Boolean))
        {
            properties.put(name, object);
            return;
        }
        throw new MessageFormatException(object.getClass().getName() + " not a valid type");
    }

    public void acknowledge() throws JMSException
    {
        acknowledged = true;
    }

    public void clearBody() throws JMSException
    {
        isInWriteMode = true;
    }
    
    public void setReadOnly(boolean isReadOnly)
    {
        isInWriteMode = !isReadOnly;
    }
    
    public void setReadOnlyProperties(boolean isReadOnly)
    {
        isInWriteModeProperties = !isReadOnly;
    }
    
    public Object clone()
    {
        try
        {
            MockMessage clone = (MockMessage)super.clone();
            clone.properties = new HashMap(properties);
            return clone;
        }
        catch(CloneNotSupportedException exc)
        {
            throw new NestedApplicationException(exc);
        }
    }
    
    protected boolean isInWriteMode()
    {
        return isInWriteMode;
    }

    protected String getNullPropertyMessage(String name) {
      return String.format("Property %s was null", name);
    }
}
