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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.ailis.wlandsuite.game.GameException;
import de.ailis.wlandsuite.io.BitInputStreamWrapper;
import de.ailis.wlandsuite.io.BitOutputStreamWrapper;


/**
 * Monster data
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class MonsterData extends AbstractPart
{
    /** The monster data */
    private List<byte[]> data = new ArrayList<byte[]>();


    /**
     * Constructor
     * 
     * @param bytes
     *            The game block data
     * @param offset
     *            The offset of the part in the game block
     * @param monsters
     *            The number of monsters 
     */

    public MonsterData(byte[] bytes, int offset, int monsters)
    {
        BitInputStreamWrapper bitStream;
        
        this.size = monsters * 8;
        this.offset = offset;
        try
        {            
            bitStream = new BitInputStreamWrapper(new ByteArrayInputStream(bytes,
                offset, bytes.length - offset));

            for (int i = 0; i < monsters; i++)
            {
                byte[] data = new byte[8];
                bitStream.read(data);
                this.data.add(data);
            }
        }
        catch (IOException e)
        {
            throw new GameException(e.toString(), e);
        }
    }


    /**
     * Creates the central directory from XML.
     * 
     * @param element
     *            The XML element
     */

    @SuppressWarnings("unchecked")
    public MonsterData(Element element)
    {
        super();

        for (Element subElement: (List<Element>) element.elements("data"))
        {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            String data = subElement.getTextTrim();
            for (String c: data.split("\\s"))
            {
                int b = Integer.valueOf(c, 16);
                stream.write(b);
            }
            this.data.add(stream.toByteArray());
        }
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.Part#toXml()
     */

    public Element toXml()
    {
        Element element, subElement;

        element = DocumentHelper.createElement("monsterData");
        element.addAttribute("offset", Integer.toString(this.offset));
        
        for (byte[] data: this.data)
        {
            subElement = DocumentHelper.createElement("data");
            StringBuilder builder = new StringBuilder();
            for (byte b: data)
            {
                builder.append(String.format("%02x ", new Object[] { b }));
            }
            subElement.setText(builder.toString().trim());
            element.add(subElement);
        }
        return element;
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.Part#write(java.io.OutputStream)
     */

    public void write(OutputStream stream) throws IOException
    {
        BitOutputStreamWrapper bitStream;

        bitStream = new BitOutputStreamWrapper(stream);
        for (byte[] data: this.data)
        {
            bitStream.write(data);
        }
        bitStream.flush();
    }


    /**
     * Returns the data.
     *
     * @return The data
     */
    
    public List<byte[]> getData()
    {
        return this.data;
    }


    /**
     * Sets the data.
     *
     * @param data 
     *            The data to set
     */
    
    public void setData(List<byte[]> data)
    {
        this.data = data;
    }
}
