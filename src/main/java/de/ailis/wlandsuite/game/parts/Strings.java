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
import de.ailis.wlandsuite.game.chartable.CharTable;
import de.ailis.wlandsuite.io.BitInputStreamWrapper;
import de.ailis.wlandsuite.io.BitOutputStreamWrapper;


/**
 * The strings.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Strings extends AbstractPart
{
    /** The character table */
    private CharTable charTable;

    /** The strings */
    private List<StringGroup> stringGroups = new ArrayList<StringGroup>();


    /**
     * Constructor
     * 
     * @param bytes
     *            The game block data
     * @param offset
     *            The offset of the part in the game block
     * @param endOffset
     *            The end offset of the part in the game block
     */

    public Strings(byte[] bytes, int offset, int endOffset)
    {
        ByteArrayInputStream byteStream;
        BitInputStreamWrapper bitStream;

        this.size = endOffset - offset;
        this.offset = offset;
        byteStream = new ByteArrayInputStream(bytes, offset, endOffset - offset);
        bitStream = new BitInputStreamWrapper(byteStream);
        try
        {
            this.charTable = new CharTable(bitStream);
            
            int tmp = bitStream.readWord();
            int strings = tmp / 2;
            List<Integer> stringOffsets = new ArrayList<Integer>(strings);
            stringOffsets.add(tmp);
            for (int i = 1; i < strings; i++)
            {
                tmp = bitStream.readWord();
                if ((tmp + offset + 60 >= endOffset) || (tmp < stringOffsets.get(i - 1)))
                {
                    // The last offset may be corrupt. That's ok. If it's not
                    // the last offset then throw an exception.
                    if (i == strings - 1)
                    {
                        continue;                        
                    }
                    else
                    {
                        throw new GameException("Error parsing strings");
                    }
                }
                stringOffsets.add(tmp);
            }
            
            for (int i = 0, max = stringOffsets.size(); i < max; i++)
            {
                StringGroup group = new StringGroup(bytes, offset
                    + stringOffsets.get(i) + 60, endOffset, this.charTable);

                // The last offset may be corrupt. That's ok. If it's not
                // the last offset then throw an exception.
                if (group.getStrings().size() > 0)
                {
                    this.stringGroups.add(group);
                }
                else
                {
                    if (i != strings - 1)
                    {
                        throw new GameException("Error parsing strings");
                    }
                }
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
    public Strings(Element element)
    {
        super();

        this.size = Integer.parseInt(element.attributeValue("size"));
        for (Element subElement: (List<Element>) element.elements("stringGroup"))
        {
            this.stringGroups.add(new StringGroup(subElement));
        }
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.Part#toXml()
     */

    public Element toXml()
    {
        Element element, subElement;

        element = DocumentHelper.createElement("strings");
        element.addAttribute("offset", Integer.toString(this.offset));
        element.addAttribute("size", Integer.toString(this.size));
        for (StringGroup stringGroup: this.stringGroups)
        {
            subElement = stringGroup.toXml();
            element.add(subElement);
        }
        return element;
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.Part#write(java.io.OutputStream, int)
     */
    
    public void write(OutputStream stream, int blockOffset) throws IOException
    {
        BitOutputStreamWrapper bitStream;
        CharTable charTable;
        ByteArrayOutputStream stringStream;
        byte[] strings;
        List<Integer> offsets;
        int oldSize;
        int offset;
        
        bitStream = new BitOutputStreamWrapper(stream);

        // Create and write the char table
        charTable = new CharTable();
        for (StringGroup group: this.stringGroups)
        {
            for (String string: group.getStrings())
            {
                charTable.add(string);
                charTable.add(0);
            }
        }
        charTable.write(bitStream);

        // Write the strings to temporary buffer and build the offset table
        stringStream = new ByteArrayOutputStream();
        offsets = new ArrayList<Integer>(this.stringGroups.size() * 4);
        offset = 0;
        oldSize = 0;
        for (StringGroup group: this.stringGroups)
        {
            group.write(stringStream, charTable);
            offsets.add(offset);
            offset += stringStream.size() - oldSize;
            oldSize = stringStream.size();
        }
        strings = stringStream.toByteArray();
        
        // Write the correct offsets to the stream
        for (int i = 0, max = offsets.size(); i < max; i++)
        {
            bitStream.writeWord(offsets.get(i) + max * 2);
        }
        
        // Write the strings to the stream
        bitStream.write(strings);
        
        // Write padding bytes
        int padding = this.size - (60 + offsets.size() * 2 + strings.length);
        if (padding < 0)
        {
            throw new GameException("String section is to large");
        }
        for (int i = 0; i < padding; i++)
        {
            bitStream.write(0);
        }

        // Flush the stream to make sure everything has been written
        bitStream.flush();
    }
}
