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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.ailis.wlandsuite.game.GameException;


/**
 * Unknown part
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class UnknownPart extends AbstractPart
{
    /** The tag name */
    protected String tagName = "unknown";
    
    /** The bytes of this unknown part */
    private byte[] bytes;


    /**
     * Constructor
     * 
     * @param bytes
     *            The unknown bytes of the game block
     * @param offset
     *            The offset of the part in the game block
     */

    public UnknownPart(byte[] bytes, int offset)
    {
        this.bytes = bytes;
        this.offset = offset;
        this.size = bytes.length;
    }


    /**
     * Constructor
     * 
     * @param bytes
     *            The complete block data
     * @param start
     *            The start of the unknown part in the block
     * @param size
     *            The size of the unknoen part
     */

    public UnknownPart(byte[] bytes, int start, int size)
    {
        this.bytes = new byte[size];
        System.arraycopy(bytes, start, this.bytes, 0, size);
        this.offset = start;
        this.size = size;
    }


    /**
     * Creates an unknown part from XML.
     * 
     * @param element
     *            The XML element
     */

    public UnknownPart(Element element)
    {
        super();

        ByteArrayOutputStream stream;
        String data;
        int b;
        int size;

        this.offset = Integer.parseInt(element.attributeValue("offset"));

        size = Integer.parseInt(element.attributeValue("size"));
        stream = new ByteArrayOutputStream();
        data = element.getTextTrim();
        for (String c: data.split("\\s"))
        {
            b = Integer.valueOf(c, 16);
            stream.write(b);
        }
        this.bytes = stream.toByteArray();
        if (this.bytes.length != size)
        {
            throw new GameException("Unknown part has wrong size. Should be "
                + size + " bytes but is " + this.bytes.length + " bytes");
        }
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.Part#toXml()
     */

    public Element toXml()
    {
        Element element;
        StringWriter text;
        PrintWriter writer;
        int size;

        size = this.bytes.length;

        element = DocumentHelper.createElement(this.tagName);
        element.addAttribute("offset", Integer.toString(this.offset));
        element.addAttribute("size", Integer.toString(size));

        text = new StringWriter();
        writer = new PrintWriter(text);

        if (size > 9)
        {
            writer.println();
            writer.print("    ");
        }
        for (int i = 0; i < size; i++)
        {
            if (i > 0)
            {
                if (i % 16 == 0)
                {
                    writer.println();
                }
                if ((i < size) && (size > 9) && (i % 4 == 0))
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
        if (size > 9)
        {
            writer.println();
            writer.print("  ");
        }

        element.add(DocumentHelper.createText(text.toString()));

        return element;
    }


    /**
     * @throws IOException
     * @see de.ailis.wlandsuite.game.parts.Part#write(java.io.OutputStream)
     */

    public void write(OutputStream stream) throws IOException
    {
        stream.write(this.bytes);
    }


    /**
     * Returns the unknown bytes of the this block part
     * 
     * @return The unknown bytes
     */

    public byte[] getBytes()
    {
        return this.bytes;
    }
}
