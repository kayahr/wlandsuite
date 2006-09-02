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
import de.ailis.wlandsuite.utils.StringUtils;


/**
 * A string group
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class StringGroup
{
    /** The offset of the string in the strings block */
    private int offset;

    /** The strings */
    private List<String> strings = new ArrayList<String>();


    /**
     * Constructor
     * 
     * @param bytes
     *            The game block data
     * @param offset
     *            The offset of the part in the game block
     * @param endOffset
     *            The endoffset of the part in the game block
     * @param charTable
     *            The character table
     */

    public StringGroup(byte[] bytes, int offset, int endOffset,
        CharTable charTable)
    {
        ByteArrayInputStream byteStream;
        BitInputStreamWrapper bitStream;

        this.offset = offset;
        byteStream = new ByteArrayInputStream(bytes, offset, endOffset - offset);
        bitStream = new BitInputStreamWrapper(byteStream);
        try
        {
            for (int j = 0; j < 4; j++)
            {
                boolean upper = false;
                boolean high = false;
                StringBuilder string = new StringBuilder();
                outer: while (true)
                {
                    int index = bitStream.readBits(5, true);
                    if (index == -1)
                    {
                        return;
                    }
                    switch (index)
                    {
                        case 0x1f:
                            high = true;
                            break;

                        case 0x1e:
                            upper = true;
                            break;

                        default:
                            int character = charTable.getCharacter(index
                                + (high ? 0x1e : 0));
                            if (character == 0)
                            {
                                break outer;
                            }
                            String s = new String(
                                new byte[] { (byte) character }, "ASCII");
                            if (upper) s = s.toUpperCase();
                            string.append(s);
                            upper = false;
                            high = false;
                    }
                }
                this.strings.add(string.toString());
            }
        }
        catch (IOException e)
        {
            throw new GameException(e.toString(), e);
        }
    }


    /**
     * Creates the strings group from XML.
     * 
     * @param element
     *            The XML element
     */

    @SuppressWarnings("unchecked")
    public StringGroup(Element element)
    {
        super();

        for (Element subElement: (List<Element>) element.elements("string"))
        {
            this.strings.add(StringUtils
                .unescape(subElement.getText(), "ASCII"));
        }
    }


    /**
     * Creates and returns and XML element containing the strings of this
     * stringgroup.
     * 
     * @return The XML element
     */

    public Element toXml()
    {
        Element element, subElement;

        element = DocumentHelper.createElement("stringGroup");
        element.addAttribute("offset", Integer.toString(this.offset));
        for (String string: this.strings)
        {
            subElement = DocumentHelper.createElement("string");
            subElement.addText(StringUtils.escape(string, "ASCII"));
            element.add(subElement);
        }
        return element;
    }


    /**
     * Writes the string group to the specified stream.
     * 
     * @param stream
     *            The output stream
     * @param charTable
     *            The char table for character compression
     * @throws IOException
     */

    public void write(OutputStream stream, CharTable charTable)
        throws IOException
    {
        BitOutputStreamWrapper bitStream;

        bitStream = new BitOutputStreamWrapper(stream);
        for (String string: this.strings)
        {
            for (byte b: string.getBytes("ASCII"))
            {
                // Handle upper case characters;
                if (b >= 65 && b <= 90)
                {
                    bitStream.writeBits(0x1e, 5, true);
                    b += 32;
                }

                // Get the character index in the char table
                int index = charTable.getIndex(b);
                if (index == -1)
                {
                    throw new GameException(
                        "Unable to find index for character " + b);
                }

                // Handle high characters
                if (index >= 0x1e)
                {
                    bitStream.writeBits(0x1f, 5, true);
                    index -= 0x1e;
                }

                // Write the character
                bitStream.writeBits(index, 5, true);
            }
            bitStream.writeBits(charTable.getIndex(0), 5, true);
        }
        bitStream.flush(true);
    }


    /**
     * Returns the strings.
     * 
     * @return The strings
     */

    public List<String> getStrings()
    {
        return this.strings;
    }
}
