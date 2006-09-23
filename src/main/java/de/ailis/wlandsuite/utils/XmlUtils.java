/*
 * $Id$
 * Copyright (C) 2006 Klaus Reimer <k@ailis.de>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package de.ailis.wlandsuite.utils;

import java.io.InputStream;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import de.ailis.wlandsuite.common.exceptions.XmlException;


/**
 * XMLUtils
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class XmlUtils
{
    /** Tje wlandsuite XML namespace */
    private static final String namespace = "http://ailis.de/wlandsuite";


    /**
     * Returns a new XML element within the wlandsuite namespace.
     * 
     * @param name
     *            The element name
     * @return The new XML element
     */

    public static Element createElement(String name)
    {
        return DocumentFactory.getInstance().createElement(name, namespace);
    }


    /**
     * Reads a document from the specified input stream. The XML reader is fully
     * configured to validate the wlandsuite namespace.
     * 
     * @param stream
     *            The input stream
     * @return The validated document
     */

    public static Document readDocument(InputStream stream)
    {
        SAXReader reader;

        reader = new SAXReader(true);
        try
        {
            reader.setFeature("http://xml.org/sax/features/validation", true);
            reader.setFeature(
                "http://apache.org/xml/features/validation/schema", true);
            reader
                .setFeature(
                    "http://apache.org/xml/features/validation/schema-full-checking",
                    true);
            reader
                .setProperty(
                    "http://apache.org/xml/properties/schema/external-schemaLocation",
                    "http://ailis.de/wlandsuite classpath://de/ailis/wlandsuite/resource/wlandsuite.xsd");
            reader.setEntityResolver(ClasspathEntityResolver.getInstance());
            return reader.read(stream);
        }
        catch (SAXException e)
        {
            throw new XmlException("Unable to configure XML reader: "
                + e.toString(), e);
        }
        catch (DocumentException e)
        {
            throw new XmlException("Unable to read XML document: "
                + e.toString(), e);
        }
    }
}
