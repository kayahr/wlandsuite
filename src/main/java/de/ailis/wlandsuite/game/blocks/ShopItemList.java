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

package de.ailis.wlandsuite.game.blocks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import de.ailis.wlandsuite.game.RotatingXorInputStream;
import de.ailis.wlandsuite.io.SeekableInputStream;
import de.ailis.wlandsuite.io.SeekableOutputStream;
import de.ailis.wlandsuite.rawgame.RotatingXorOutputStream;


/**
 * Shop items
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class ShopItemList extends GameBlock implements Serializable
{
    /** Serial version UID */
    private static final long serialVersionUID = 1004046023232273188L;

    /** The data */
    private byte[] data;


    /**
     * Constructs a shop items object by reading it from a wasteland gameX file
     * stream. The stream must point at the beginning of the MSQ block (which is
     * at the "m" of the "msq" header string.
     * 
     * @param stream
     *            The input stream
     * @return The newly constructed shop items object
     * @throws IOException
     */

    public static ShopItemList read(SeekableInputStream stream) throws IOException
    {
        byte[] headerBytes;
        String header;
        SeekableInputStream xorStream;
        ShopItemList shopItems;

        // Read the MSQ block header and validate it
        headerBytes = new byte[4];
        stream.read(headerBytes);
        header = new String(headerBytes, "ASCII");
        if (!header.equals("msq0") && !header.equals("msq1"))
        {
            throw new IOException("No MSQ block header found at stream");
        }

        // Read/Decrypt the MSQ block
        xorStream = new SeekableInputStream(new RotatingXorInputStream(stream));

        shopItems = new ShopItemList();

        // Read the unknown block
        shopItems.data = new byte[760];
        xorStream.read(shopItems.data);

        // Return the newly created shop items object
        return shopItems;
    }


    /**
     * Writes the shop items to the specified output stream.
     * 
     * @param stream
     *            The output stream
     * @param disk
     *            The disk id (0 or 1)
     * @throws IOException
     */

    public void write(OutputStream stream, int disk) throws IOException
    {
        SeekableOutputStream seekStream;

        // Write the MSQ header
        stream.write("msq".getBytes());
        stream.write('0' + disk);

        // Write the header and the characters
        seekStream = new SeekableOutputStream(new RotatingXorOutputStream(
            stream));
        seekStream.write(this.data);
        seekStream.flush();
    }


    /**
     * Creates and returns a new shop items object from XML.
     * 
     * @param element
     *            The XML root element
     * @return The shop items object
     */

    public static ShopItemList read(Element element)
    {
        ShopItemList shopItems;
        String data;
        ByteArrayOutputStream byteStream;

        shopItems = new ShopItemList();

        // Read the header
        data = element.getTextTrim();
        byteStream = new ByteArrayOutputStream();
        for (String b: data.split("\\s"))
        {
            byteStream.write(Integer.parseInt(b, 16));
        }
        shopItems.data = byteStream.toByteArray();

        return shopItems;
    }


    /**
     * Reads a shop items object from the specified XML stream.
     * 
     * @param stream
     *            The input stream
     * @return The shop items object
     * @throws IOException
     */

    public static ShopItemList readXml(InputStream stream) throws IOException
    {
        SAXReader reader;
        Document document;
        Element element;

        reader = new SAXReader();
        try
        {
            document = reader.read(stream);
            element = document.getRootElement();

            return read(element);
        }
        catch (DocumentException e)
        {
            throw new IOException("Unable to parse game map from XML: "
                + e.getMessage());
        }
    }


    /**
     * @see de.ailis.wlandsuite.game.blocks.GameBlock#toXml()
     */

    @Override
    public Element toXml()
    {
        Element element;
        StringWriter text;
        PrintWriter writer;

        // Create the root element
        element = DocumentHelper.createElement("shopItemList");

        text = new StringWriter();
        writer = new PrintWriter(text);

        writer.println();
        writer.print("  ");
        for (int i = 0; i < this.data.length; i++)
        {
            if (i > 0)
            {
                if (i % 16 == 0)
                {
                    writer.println();
                }
                if ((i < this.data.length) && (i % 4 == 0))
                {
                    if (i % 16 == 0)
                    {
                        writer.print("  ");
                    }
                    else
                    {
                        writer.print("    ");
                    }
                }
                else
                {
                    writer.print(" ");
                }
            }
            writer.format("%02x", new Object[] { this.data[i] });
        }
        writer.println();

        element.add(DocumentHelper.createText(text.toString()));

        // Return the XMl element
        return element;
    }
}
