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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import de.ailis.wlandsuite.common.exceptions.GameException;
import de.ailis.wlandsuite.game.RotatingXorInputStream;
import de.ailis.wlandsuite.game.RotatingXorOutputStream;
import de.ailis.wlandsuite.game.parts.ShopItem;
import de.ailis.wlandsuite.io.SeekableInputStream;
import de.ailis.wlandsuite.io.SeekableOutputStream;
import de.ailis.wlandsuite.utils.XmlUtils;


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

    /** The shop items */
    private List<ShopItem> items = new ArrayList<ShopItem>();


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

    public static ShopItemList read(SeekableInputStream stream)
        throws IOException
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

        // Skip first 8 bytes. They are always the same
        xorStream.skip(8);

        // Read the shop items
        for (int i = 0; i < 94; i++)
        {
            shopItems.items.add(ShopItem.read(xorStream));
        }

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

        // Write the header
        seekStream = new SeekableOutputStream(new RotatingXorOutputStream(
            stream));
        seekStream.write(0x60);
        seekStream.write(0x60);
        seekStream.write(0x60);
        seekStream.write(0x00);
        seekStream.write(0x37);
        seekStream.write(0x08);
        seekStream.write(0xf8);
        seekStream.write(0x39);

        // Make sure there are not too much shop items
        if (this.items.size() > 94)
        {
            throw new GameException("To many shop items");
        }

        // Write the shop items
        for (ShopItem item: this.items)
        {
            item.write(seekStream);
        }

        // Write padding
        for (int i = this.items.size(); i < 94; i++)
        {
            seekStream.writeInt(0);
            seekStream.writeInt(0);
        }

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

        shopItems = new ShopItemList();

        // Read the shop items
        for (Object item: element.elements("shopItem"))
        {
            Element subElement = (Element) item;
            shopItems.items.add(ShopItem.read(subElement));
        }

        return shopItems;
    }


    /**
     * Reads a shop items object from the specified XML stream.
     * 
     * @param stream
     *            The input stream
     * @return The shop items object
     */

    public static ShopItemList readXml(InputStream stream)
    {
        Document document;
        Element element;
        document = XmlUtils.readDocument(stream);
        element = document.getRootElement();

        return read(element);
    }


    /**
     * @see de.ailis.wlandsuite.game.blocks.GameBlock#toXml()
     */

    @Override
    public Element toXml()
    {
        Element element;
        int id;

        // Create the root element
        element = XmlUtils.createElement("shopItems");

        id = 0;
        for (ShopItem item: this.items)
        {
            element.add(item.toXml(id));
            id++;
        }

        // Return the XMl element
        return element;
    }
}
