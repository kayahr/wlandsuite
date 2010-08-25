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

package de.ailis.wlandsuite.game.parts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.ailis.wlandsuite.common.exceptions.GameException;
import de.ailis.wlandsuite.utils.XmlUtils;


/**
 * A unknown block.
 *
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Unknown
{
    /** The unknown bytes */
    private byte[] bytes;


    /**
     * Creates a new unknown block by reading the specified number of bytes from
     * the specified stream.
     *
     * @param stream
     *            The input stream
     * @param size
     *            The number of bytes to read
     * @return The unknown block
     * @throws IOException
     *             When file operation fails.
     */

    public static Unknown read(final InputStream stream, final int size) throws IOException
    {
        Unknown unknown;

        unknown = new Unknown();
        unknown.bytes = new byte[size];
        stream.read(unknown.bytes);
        return unknown;
    }


    /**
     * Writes the unknown data to the specified stream.
     *
     * @param stream
     *            The output stream
     * @throws IOException
     *             When file operation fails.
     */

    public void write(final OutputStream stream) throws IOException
    {
        stream.write(this.bytes);
    }


    /**
     * Returns the unknown data as XML.
     *
     * @param elementName
     *            The element name
     * @return The unknown data as XML
     */

    public Element toXml(final String elementName)
    {
        Element element;
        StringWriter text;
        PrintWriter writer;

        element = XmlUtils.createElement(elementName);

        text = new StringWriter();
        writer = new PrintWriter(text);

        writer.println();
        writer.print("    ");
        for (int i = 0; i < this.bytes.length; i++)
        {
            if (i > 0)
            {
                if (i % 16 == 0)
                {
                    writer.println();
                }
                if ((i < this.bytes.length) && (i % 4 == 0))
                {
                    writer.print("    ");
                }
                else
                {
                    writer.print(" ");
                }
            }
            writer.format("%02x", new Object[] { this.bytes[i] });
        }
        writer.println();
        writer.print("  ");

        element.add(DocumentHelper.createText(text.toString()));

        return element;
    }


    /**
     * Creates and returns a new Unknown object by reading its data from XML.
     *
     * @param element
     *            The XML element
     * @param size
     *            The unknown block size.
     * @return The Unknown object
     */

    public static Unknown read(final Element element, final int size)
    {
        Unknown unknown;
        ByteArrayOutputStream byteStream;
        String data;

        unknown = new Unknown();
        data = element.getTextTrim();
        byteStream = new ByteArrayOutputStream();
        for (final String b: data.split("\\s"))
        {
            byteStream.write(Integer.parseInt(b, 16));
        }
        unknown.bytes = byteStream.toByteArray();
        if (unknown.bytes.length != size)
        {
            throw new GameException("Invalid unknown block size: "
                + unknown.bytes.length + ". Should be: " + size);
        }

        return unknown;
    }
}
