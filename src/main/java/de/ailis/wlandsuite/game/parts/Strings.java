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
import java.util.ArrayList;
import java.util.List;

import de.ailis.wlandsuite.utils.XMLUtils;
import org.dom4j.Element;

import de.ailis.wlandsuite.game.chartable.CharTable;
import de.ailis.wlandsuite.io.SeekableInputStream;
import de.ailis.wlandsuite.io.SeekableOutputStream;
import de.ailis.wlandsuite.rawgame.GameException;
import de.ailis.wlandsuite.utils.StringUtils;


/**
 * All the strings of a map. Be careful when you delete a string because other
 * strings will get a new index and you have to correct all references to these
 * strings.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Strings extends ArrayList<String>
{
    /** Serial version UID */
    private static final long serialVersionUID = 1697322917088367990L;


    /**
     * Constructs a new String list
     */

    public Strings()
    {
        super();
    }


    /**
     * Constructor
     * 
     * @param capacity
     *            The initial capacity
     */

    public Strings(int capacity)
    {
        super(capacity);
    }


    /**
     * Creates and returns a new Strings object by reading all the strings from
     * the specified stream.
     * 
     * @param stream
     *            The stream to read the strings from
     * @param endOffset
     *            The definite end of the strings block
     * @return The strings
     * @throws IOException
     */

    public static Strings read(SeekableInputStream stream, int endOffset)
        throws IOException
    {
        long startOffset;
        Strings strings;
        List<Integer> stringOffsets;
        int tmp, quantity;

        // Remember the start offset
        startOffset = stream.tell();

        // Create the strings object
        strings = new Strings();

        // Read the character table
        CharTable charTable = new CharTable(stream);

        // Read the string offsets
        tmp = stream.readWord();
        quantity = tmp / 2;
        stringOffsets = new ArrayList<Integer>(quantity);
        stringOffsets.add(tmp);
        for (int i = 1; i < quantity; i++)
        {
            tmp = stream.readWord();
            if ((tmp + startOffset + 60 >= endOffset)
                || (tmp < stringOffsets.get(i - 1)))
            {
                // The last offset may be corrupt. That's ok. If it's not
                // the last offset then throw an exception.
                if (i == quantity - 1)
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

        // Read the strings
        for (int i = 0, max = stringOffsets.size(); i < max; i++)
        {
            stream.seek(stringOffsets.get(i) + 60 + startOffset);
            readStringGroup(stream, charTable, strings, endOffset);
        }

        // Return the strings
        return strings;
    }


    /**
     * Reads a string group from the specified stream. The stream must be
     * positioned at the beginning of the string group. The strings are
     * completely decrypted by using the specified char table. Strings are added
     * to the specified strings array.
     * 
     * @param stream
     *            The stream to read the string group from
     * @param charTable
     *            The character table
     * @param strings
     *            The string list to add the strings to
     * @param endOffset
     *            The definite end of the strings block
     * @throws IOException
     */

    private static void readStringGroup(SeekableInputStream stream,
        CharTable charTable, List<String> strings, int endOffset)
        throws IOException
    {
        for (int j = 0; j < 4; j++)
        {
            boolean upper = false;
            boolean high = false;
            StringBuilder string = new StringBuilder();
            outer: while (true)
            {
                if (stream.tell() > endOffset)
                {
                    return;
                }
                int index = stream.readBits(5, true);
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
                        String s = new String(new byte[] { (byte) character },
                            "ASCII");
                        if (upper) s = s.toUpperCase();
                        string.append(s);
                        upper = false;
                        high = false;
                }
            }
            strings.add(string.toString());
        }
    }


    /**
     * Writes the strings to the specified stream.
     * 
     * @param stream
     *            The stream to write the strings to
     * @throws IOException
     */

    public void write(SeekableOutputStream stream) throws IOException
    {
        CharTable charTable;
        ByteArrayOutputStream stringStream;
        byte[] strings;
        List<Integer> offsets;
        int oldSize;
        int offset;
        int stringNo;
        SeekableOutputStream bitStream;

        // Create and write the char table
        charTable = new CharTable();
        for (String string: this)
        {
            charTable.add(string);
            charTable.add(0);
        }
        charTable.write(stream);

        // Write the strings to temporary buffer and build the offset table
        stringStream = new ByteArrayOutputStream();
        offsets = new ArrayList<Integer>(size() / 4 + 1);
        offset = 0;
        oldSize = 0;
        stringNo = 0;
        stringStream = new ByteArrayOutputStream();
        bitStream = new SeekableOutputStream(stringStream);
        for (String string: this)
        {
            writeString(bitStream, string, charTable);

            stringNo++;
            if (stringNo % 4 == 0)
            {
                bitStream.flush(true);
                offsets.add(offset);
                offset += stringStream.size() - oldSize;
                oldSize = stringStream.size();
                bitStream = new SeekableOutputStream(stringStream);
            }
        }
        if (stringNo % 4 != 0)
        {
            bitStream.flush(true);
            offsets.add(offset);
        }
        strings = stringStream.toByteArray();

        // Write the correct offsets to the stream
        for (int i = 0, max = offsets.size(); i < max; i++)
        {
            stream.writeWord(offsets.get(i) + max * 2);
        }

        // Write the strings to the stream
        stream.write(strings);
    }


    /**
     * Writes a string into the specified output stream.
     * 
     * @param stream
     *            The output stream
     * @param string
     *            The string to write
     * @param charTable
     *            The character table
     * @throws IOException
     */

    private void writeString(SeekableOutputStream stream, String string,
        CharTable charTable) throws IOException
    {
        for (byte b: string.getBytes("ASCII"))
        {
            // Handle upper case characters;
            if (b >= 65 && b <= 90)
            {
                stream.writeBits(0x1e, 5, true);
                b += 32;
            }

            // Get the character index in the char table
            int index = charTable.getIndex(b);
            if (index == -1)
            {
                throw new GameException("Unable to find index for character "
                    + b);
            }

            // Handle high characters
            if (index >= 0x1e)
            {
                stream.writeBits(0x1f, 5, true);
                index -= 0x1e;
            }

            // Write the character
            stream.writeBits(index, 5, true);
        }
        stream.writeBits(charTable.getIndex(0), 5, true);
    }


    /**
     * Creates and returns a new Strings object read from the specified XML
     * element.
     * 
     * @param element
     *            The XML element
     * @return The Strings object
     */

    public static Strings read(Element element)
    {
        Strings strings;

        // Create new strings object
        strings = new Strings(element.elements().size());

        // Read all the strings
        for (Object subElement: element.elements("string"))
        {
            Element string;
            String text;
            int id;

            string = (Element) subElement;
            text = StringUtils.unescape(string.getText(), "ASCII");
            id = Integer.parseInt(string.attributeValue("id"));
            if (id == strings.size())
            {
                strings.add(text);
            }
            else if (id < strings.size())
            {
                strings.set(id, text);
            }
            else
            {
                for (int i = strings.size(); i < id; i++)
                {
                    strings.add("");
                }
                strings.add(text);
            }
        }

        // Return the newly created Strings object
        return strings;
    }


    /**
     * Returns the strings as XML.
     * 
     * @return The strings as XML
     */

    public Element toXml()
    {
        Element element, subElement;
        int stringNo;

        // Create the root XML element
        element = XMLUtils.createElement("strings");

        // Add all the strings
        stringNo = 0;
        for (String string: this)
        {
            // Create and append string element
            subElement = XMLUtils.createElement("string");
            subElement.addAttribute("id", Integer.toString(stringNo));
            subElement.addText(StringUtils.escape(string, "ASCII"));
            element.add(subElement);
            stringNo++;
        }

        // Return the XML element
        return element;
    }
}
