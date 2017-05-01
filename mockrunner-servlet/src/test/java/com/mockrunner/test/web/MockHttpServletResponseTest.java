package com.mockrunner.test.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockServletOutputStream;

public class MockHttpServletResponseTest extends TestCase
{
    private MockHttpServletResponse response;
    
    protected void setUp() throws Exception
    {
        super.setUp();
        response = new MockHttpServletResponse();
    }
    
    public void testResetAll() throws Exception
    {
        response.addHeader("header", "headervalue");
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        response.setBufferSize(10);
        response.resetAll();
        assertNull(response.getHeader("header"));
        assertEquals(8192, response.getBufferSize());
        assertFalse(response.wasErrorSent());
    }
    
    public void testHeaders()
    {
        response.addHeader("testHeader", "xyz");
        response.addHeader("testHeader", "abc");
        assertTrue(response.getHeaderList("testHeader").size() == 2);
        assertTrue(response.getHeaderList("testHeader").contains("xyz"));
        assertTrue(response.getHeaderList("testHeader").contains("abc"));
        response.setHeader("testHeader", "abc");
        assertTrue(response.getHeaderList("testHeader").size() == 1);
        assertTrue(response.getHeaderList("testHeader").contains("abc"));
        assertEquals("abc", response.getHeader("testHeader"));
        response.addDateHeader("dateHeader", 0);
        String expectedDateString = "Thu, 1 Jan 1970 00:00:00 GMT";
        assertEquals(expectedDateString, response.getHeader("dateHeader"));
        response.setDateHeader("dateHeader", 0);
        assertEquals(expectedDateString, response.getHeader("dateHeader"));
        response.addIntHeader("intHeader", 0);
        assertEquals("0", response.getHeader("intHeader"));
        response.clearHeaders();
        Collection headers = response.getHeaderNames();
        assertTrue(headers.isEmpty());
    }
    
    public void testGetHeaderNames()
    {
        response.addHeader("testHeader1", "xyz1");
        response.addHeader("testHeader1", "abc");
        response.addHeader("testHeader2", "xyz2");
        response.addHeader("testHeader3", "xyz3");
        Collection headerNames= response.getHeaderNames();
        assertEquals(3, headerNames.size());
        assertTrue(headerNames.contains("testHeader1"));
        assertTrue(headerNames.contains("testHeader2"));
        assertTrue(headerNames.contains("testHeader3"));
    }
    
    public void testGetHeaders()
    {
        response.addHeader("testHeader1", "xyz1");
        response.addHeader("testHeader1", "abc");
        response.addHeader("testHeader2", "xyz2");
        Collection headers = response.getHeaders("testHeader1");
        assertEquals(2, headers.size());
        assertTrue(headers.contains("xyz1"));
        assertTrue(headers.contains("abc"));
    }
    
    public void testHeadersCaseInsensitive()
    {
        response.addHeader("testHeader", "xyz");
        response.addHeader("TESTHeader", "abc");
        response.addHeader("MYHEADER1", "xyz");
        response.addHeader("myHeader2", "abc");
        assertEquals("xyz", response.getHeader("myheader1"));
        assertEquals("abc", response.getHeader("MYHEADER2"));
        Collection headerNames = response.getHeaderNames();
        assertEquals(3, headerNames.size());
        assertTrue(headerNames.contains("testHeader"));
        assertTrue(headerNames.contains("MYHEADER1"));
        assertTrue(headerNames.contains("myHeader2"));
    }
    
    public void testOutputStreams() throws IOException
    {
        response.getOutputStream().print("test");
        response.getWriter().print(true);
        response.getWriter().print("test");
        response.flushBuffer();
        assertEquals("testtruetest", response.getOutputStreamContent());
    }
    
    public void testGetSetContentType() throws IOException
    {
        assertNull(response.getContentType());
        response.setContentType("myType");
        assertEquals("myType", response.getContentType());
    }
    
    public void testFlush() throws IOException
    {
        response.getOutputStream().write('a');
        response.flushBuffer();
        assertEquals("a", ((MockServletOutputStream)response.getOutputStream()).getContent());
    }
    
    public void testReset() throws IOException
    {
        response.addHeader("testHeader", "xyz");
        assertTrue(response.getHeaderList("testHeader").size() == 1);
        response.getOutputStream().write('a');
        response.getWriter().write("xyz");
        response.resetBuffer();
        assertEquals("", ((MockServletOutputStream)response.getOutputStream()).getContent());
        assertTrue(response.getHeaderList("testHeader").size() == 1);
        response.reset();
        assertNull(response.getHeaderList("testHeader"));
    }
    
    public void testSetCharacterEncoding() throws IOException
    {
        response.setCharacterEncoding("ISO-8859-1");
        response.getWriter().write("?");
        response.getWriter().flush();
        response.getOutputStream().println("?");
        response.setCharacterEncoding("US-ASCII");
        assertFalse(response.getOutputStreamContent().startsWith("ºº"));
        response.setCharacterEncoding("ISO-8859-1");
        assertTrue(response.getOutputStreamContent().startsWith("??"));
        response.resetAll();
        response.setCharacterEncoding("UTF-8"); 
        String input = "\u00F8";
        PrintWriter writer = response.getWriter(); 
        writer.write(input);
        response.flushBuffer(); 
        String output = response.getOutputStreamContent();
        assertTrue(output.equals(input));
    }

    public void testRedirect() throws IOException
    {
        response.sendRedirect("/some-location");
        assertEquals("/some-location", response.getHeader("Location"));
        assertEquals(HttpServletResponse.SC_FOUND, response.getStatusCode());
    }

    public void testEncodeRedirectURL() throws IOException {
        final String encoded1 = response.encodeRedirectURL("page#/some-location?a=b&c=d");
        assertEquals("page%23%2Fsome-location%3Fa%3Db%26c%3Dd", encoded1);
        final String encoded2 = response.encodeRedirectURL("page");
        assertEquals("page", encoded2);
    }

    public void testEncodeRedirectUrl() throws IOException {
        final String encoded = response.encodeRedirectUrl("page#/some-location?a=b&c=d");
        assertEquals("page%23%2Fsome-location%3Fa%3Db%26c%3Dd", encoded);
        final String encoded2 = response.encodeRedirectUrl("page");
        assertEquals("page", encoded2);
    }
}
