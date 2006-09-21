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

package de.ailis.wlandsuite.rawgame.parts;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import de.ailis.wlandsuite.utils.XmlUtils;
import org.dom4j.Element;

import de.ailis.wlandsuite.io.BitInputStreamWrapper;
import de.ailis.wlandsuite.io.BitOutputStreamWrapper;
import de.ailis.wlandsuite.rawgame.GameException;
import de.ailis.wlandsuite.utils.StringUtils;


/**
 * Monster names
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class MonsterNames extends AbstractPart
{
    /** The monster names */
    private List<String> names = new ArrayList<String>();


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

    public MonsterNames(byte[] bytes, int offset, int monsters)
    {
        int b;
        BitInputStreamWrapper bitStream;
        
        this.size = 0;
        this.offset = offset;
        try
        {
            bitStream = new BitInputStreamWrapper(new ByteArrayInputStream(bytes,
                offset, bytes.length - offset));

            for (int i = 0; i < monsters; i++)
            {
                StringBuilder name = new StringBuilder();
                while (true)
                {
                    String s;
                    
                    b = bitStream.readByte();
                    this.size++;
                    offset++;
                    if (b == 0) break;
                    s = new String(new byte[] { (byte) b }, "ASCII");
                    name.append(s);
                }
                this.names.add(name.toString());
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
    public MonsterNames(Element element)
    {
        super();

        for (Element subElement: (List<Element>) element.elements("name"))
        {
            this.names.add(StringUtils.unescape(subElement.getText(), "ASCII"));
        }
    }


    /**
     * @see de.ailis.wlandsuite.rawgame.parts.Part#toXml()
     */

    public Element toXml()
    {
        Element element, subElement;

        element = XmlUtils.createElement("monsterNames");
        element.addAttribute("offset", Integer.toString(this.offset));
        
        for (String name: this.names)
        {
            subElement = XmlUtils.createElement("name");
            subElement.setText(StringUtils.escape(name, "ASCII"));
            element.add(subElement);
        }
        return element;
    }


    /**
     * @see de.ailis.wlandsuite.rawgame.parts.Part#write(java.io.OutputStream, int)
     */
    
    public void write(OutputStream stream, int offset) throws IOException
    {
        BitOutputStreamWrapper bitStream;

        bitStream = new BitOutputStreamWrapper(stream);
        for (String name: this.names)
        {
            bitStream.write(name.getBytes("ASCII"));
            bitStream.write(0);
        }
        bitStream.flush();
    }


    /**
     * Returns the monsterNames.
     *
     * @return The monsterNames
     */
    
    public List<String> getNames()
    {
        return this.names;
    }


    /**
     * Sets the monsterNames.
     *
     * @param monsterNames 
     *            The monsterNames to set
     */
    
    public void setNames(List<String> monsterNames)
    {
        this.names = monsterNames;
    }       
}
