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
    private byte[] charTable = new byte[60];
    
    /** The strings */
    private List<String> strings = new ArrayList<String>();


    /**
     * Constructor
     * 
     * @param bytes
     *            The game block data
     * @param offset
     *            The offset of the part in the game block
     */

    public Strings(byte[] bytes, int offset)
    {
        BitInputStreamWrapper bitStream;

        this.size = 0;
        this.offset = offset;
        bitStream = new BitInputStreamWrapper(new ByteArrayInputStream(bytes,
            offset, bytes.length - offset));
        try
        {
            bitStream.read(this.charTable);
            /*for (byte b: this.charTable)
            {
                if (b > 0x20)
                {
                    System.out.print(String.format("%c", new Object[] { b }));
                }
                else
                {
                    System.out.print(String.format("\\%02x", new Object[] { b }));
                }
            }*/
            //System.out.println();
            this.size += this.charTable.length;
            int tmp = bitStream.readWord();
            this.size += 2;
            int strings = tmp / 2;
            int[] stringOffsets = new int[strings];
            //System.out.println("Number of strings: " + strings);
            stringOffsets[0] = tmp;
            //System.out.println("String offset 0: " + String.format("%x", new Object[] {(offset + stringOffsets[0] + 60)}));
            for (int i = 1; i < strings; i++)
            {
                stringOffsets[i] = bitStream.readWord();
                this.size += 2;
              //  System.out.println("String offset " + i + ": " + String.format("%x", new Object[] {(offset + stringOffsets[i] + 60)}));
            }
            //System.out.println("Current offset: " + String.format("%x", new Object[] { offset + this.size }));
            outer: for (int j = 0; j < strings; j++)
            {
                int bits = 0;
                boolean upper = false;
                boolean high = false;
                int len;
                if (j >= strings - 1)
                    len = 127;
                else
                    len = (stringOffsets[j + 1] - stringOffsets[j]) * 8 / 5;
                StringBuilder string = new StringBuilder(); 
                for (int i = 0; i < len; i++)
                {
                    int index = bitStream.readBits(5, true);
                    if (index == -1) break outer;
                    switch (index)
                    {
                        case 0x1f:
                            high = true;
                            break;

                        case 0x1e:
                            upper = true;
                            break;

                        default:
                            int character = this.charTable[index + (high ? 0x1e : 0)];
                            String s;
                            if (character >= 0x20)
                            {
                                s = new String(new byte[] { (byte) character });
                            }
                            else
                            {
                                s = String.format("\\%02x",
                                    new Object[] { character });
                            }
                            if (upper) s = s.toUpperCase();
               //             System.out.print(s);
                            string.append(s);
                            upper = false;
                            high = false;
                    }
                    bits += 5;
                }
                this.size += bits / 8;
                if (bits % 8 > 0)
                {
                    bitStream.readBits(8 - (bits % 8), true);
                    this.size++;
                }
                this.strings.add(string.toString());
            }
        }
        catch (IOException e)
        {
            throw new GameException(e.toString(), e);
        }
        //System.exit(0);
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
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.Part#toXml()
     */

    public Element toXml()
    {
        Element element, subElement;

        element = DocumentHelper.createElement("strings");
        for (String string: this.strings)
        {
            subElement = DocumentHelper.createElement("string");
            subElement.addText(string);
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
        bitStream.flush();
    }
}
