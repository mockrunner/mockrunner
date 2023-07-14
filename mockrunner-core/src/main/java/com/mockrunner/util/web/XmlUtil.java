package com.mockrunner.util.web;

import java.io.StringReader;
import java.util.List;

import org.apache.xerces.parsers.DOMParser;
import org.cyberneko.html.HTMLConfiguration;
import org.jdom2.Element;
import org.jdom2.input.DOMBuilder;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.InputSource;

import com.mockrunner.base.NestedApplicationException;

/**
 * Util class for HTML and XML parsing.
 */
public class XmlUtil
{ 
    /**
     * Convinience method for HTML fragments. Returns the body
     * as JDOM <code>Element</code>.
     * 
     * If an HTML documents looks like this:
     * <pre>
     * &lt;html&gt;
     * &lt;head&gt;
     * &lt;/head&gt;
     * &lt;body&gt;
     * &lt;h1&gt;
     * &lt;/h1&gt;
     * &lt;/body&gt;
     * &lt;/html&gt;
     * </pre>
     * 
     * the method returns the h1 tag as <code>Element</code>.
     * @param document the <code>org.jdom2.Document</code>
     * @return the body <code>Element</code>
     */
    public static Element getBodyFragmentFromJDOMDocument(org.jdom2.Document document)
    {
        Element element = document.getRootElement().getChild("BODY");
        if(null == element)
        {
            element = document.getRootElement().getChild("body");
        }
        if(null != element)
        {
            List childs = element.getChildren();
            if(null != childs && childs.size() > 0) return (Element)childs.get(0);
        }
        return null;
    }
    
    /**
     * @param document the document to get the body from
     * @return the body element
     * @deprecated use {@link #getBodyFragmentFromJDOMDocument}
     */
    public static Element getBodyFragmentJDOMDocument(org.jdom2.Document document)
    {
        return getBodyFragmentFromJDOMDocument(document);
    }
    
    /**
     * Returns the documents XML content as a string.
     * @param document the <code>org.jdom2.Document</code>
     * @return the output as string
     */
    public static String createStringFromJDOMDocument(org.jdom2.Document document)
    {
        try
        {
            return new XMLOutputter().outputString(document);
        }
        catch(Exception exc)
        {
            throw new NestedApplicationException(exc);
        }
    }
    
    /**
     * Creates a JDOM <code>Document</code> from a specified
     * W3C <code>Document</code>.
     * @param document the <code>org.w3c.dom.Document</code>
     * @return the <code>org.jdom2.Document</code>
     */
    public static org.jdom2.Document createJDOMDocument(org.w3c.dom.Document document)
    {
        return new DOMBuilder().build(document);
    }
    
    /**
     * Returns a parser suitable for parsing HTML documents.
     * The NekoHTML parser is used with some settings to
     * preserve case of tag names and disable namespace processing. 
     * This method is used by {@link #parseHTML}.
     * @return instance of <code>org.apache.xerces.parsers.DOMParser</code>
     *         with Neko configuration
     */
    public static DOMParser getHTMLParser()
    {
        try
        {
            HTMLConfiguration config = new HTMLConfiguration();
            config.setProperty("http://cyberneko.org/html/properties/names/elems", "match");
            config.setProperty("http://cyberneko.org/html/properties/names/attrs", "no-change");
            DOMParser parser = new DOMParser(config);
            return parser;
        }
        catch(Exception exc)
        {
            throw new NestedApplicationException(exc);
        }
    }
    
    /**
     * Parses the specified HTML with the NekoHTML parser.
     * If you want to use another HTML parser or configure
     * the NekoHTML parser with special features, you can use
     * the <code>parse</code> method.
     * @param source the HTML as String
     * @return the parsed document as org.w3c.dom.Document
     */
    public static org.w3c.dom.Document parseHTML(String source)
    {
        try
        {
            return parse(getHTMLParser(), source);
        }
        catch(Exception exc)
        {
            throw new NestedApplicationException(exc);
        }
    }
    
    /**
     * Parses the specified XML with the specified parser.
     * The main purpose of this method is to use the NekoHTML 
     * parser with custom features and properties. If you can live
     * with the settings provided by Mockrunner, you can use 
     * {@link #parseHTML}.
     * @param parser the parser (must extend 
     *               <code>org.apache.xerces.parsers.DOMParser</code>), 
     *               e.g. the one returned by {@link #getHTMLParser}
     * @param source the XML as String
     * @return the parsed document as org.w3c.dom.Document
     */
    public static org.w3c.dom.Document parse(DOMParser parser, String source)
    {
        try
        {
            parser.parse(new InputSource(new StringReader(source)));
            return parser.getDocument();
        }
        catch(Exception exc)
        {
            throw new NestedApplicationException(exc);
        }
    }
}
