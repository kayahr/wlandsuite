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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.ailis.wlandsuite.game.GameException;


/**
 * Action class map part
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class ActionClassMapPart extends AbstractPart
{
    /** The map size */
    private int mapSize;

    /** The action class map. First index is y, second is x */
    private byte[][] map;


    /**
     * Creates an action class part from XML.
     * 
     * @param offset
     *            The offset
     * @param element
     *            The XML element
     * @param mapSize
     *            The map size
     */

    public ActionClassMapPart(int offset, Element element, int mapSize)
    {
        this(mapSize);

        String data;
        char c;
        int i;
        byte b;

        // Validate the offset
        if (offset != this.offset)
        {
            throw new GameException(String.format(
                "Action class map is at the wrong offset. Should "
                    + "be at 0x%x but is at 0x%x", new Object[] { this.offset,
                    offset }));
        }

        data = element.getTextTrim();
        i = 0;
        for (int y = 0; y < mapSize; y++)
        {
            for (int x = 0; x < mapSize; x++)
            {
                try
                {
                    c = data.charAt(i);
                }
                catch (StringIndexOutOfBoundsException e)
                {
                    throw new GameException("Action class map is corrupt: "
                        + (mapSize * mapSize - (y * mapSize + x))
                        + " bytes missing");
                }
                if (c == '.') c = '0';
                try
                {
                    b = Byte.valueOf(Character.toString(c), 16);
                }
                catch (NumberFormatException e)
                {
                    throw new GameException(
                        "Illegal character in action class map at y=" + y
                            + " x=" + x);
                }
                this.map[y][x] = b;
                i++;
            }
            i++;
        }
    }


    /**
     * Constructor
     * 
     * @param mapSize
     *            The map size
     */

    public ActionClassMapPart(int mapSize)
    {
        super(0, mapSize * mapSize / 2);
        this.mapSize = mapSize;
        this.map = new byte[mapSize][mapSize];
    }


    /**
     * Constructor
     * 
     * @param bytes
     *            The bytes of the game block
     * @param mapSize
     *            The map size
     */

    public ActionClassMapPart(byte[] bytes, int mapSize)
    {
        this(mapSize);
        parse(bytes, mapSize);
    }


    /**
     * Parses the action class map
     * 
     * @param bytes
     *            The block bytes
     * @param mapSize
     *            The map size
     */

    private void parse(byte[] bytes, int mapSize)
    {

        for (int y = 0; y < mapSize; y++)
        {
            for (int x = 0; x < mapSize; x += 2)
            {
                byte b;

                b = bytes[(y * mapSize + x) / 2];
                this.map[y][x] = (byte) ((b & 0xff) >> 4);
                this.map[y][x + 1] = (byte) (b & 0x0f);
            }
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

        element = DocumentHelper.createElement("actionClassMap");

        text = new StringWriter();
        writer = new PrintWriter(text);

        writer.println();
        for (int y = 0; y < this.mapSize; y++)
        {
            writer.print("    ");
            for (int x = 0; x < this.mapSize; x++)
            {
                byte b = this.map[y][x];
                if (b == 0)
                {
                    writer.print('.');
                }
                else
                {
                    writer.print(Integer.toHexString(b));
                }
            }
            writer.println();
        }
        writer.print("  ");

        element.add(DocumentHelper.createText(text.toString()));
        return element;
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.Part#write(java.io.OutputStream)
     */

    public void write(OutputStream stream) throws IOException
    {
        for (int y = 0; y < this.mapSize; y++)
        {
            for (int x = 0; x < this.mapSize; x += 2)
            {
                stream.write(((this.map[y][x] & 0x0f) << 4)
                    | (this.map[y][x + 1] & 0x0f));
            }
        }
    }


    /**
     * Returns the action class for the specified position
     * 
     * @param x
     *            The X position
     * @param y
     *            The Y position
     * @return The action class
     */

    public byte getActionClass(int x, int y)
    {
        return this.map[y][x];
    }
}
