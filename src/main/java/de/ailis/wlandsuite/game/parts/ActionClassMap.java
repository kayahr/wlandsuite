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
 * Action class map
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class ActionClassMap extends AbstractPart
{
    /** The action class map. First index is y, second is x */
    private byte[][] map;


    /**
     * Creates an action class part from XML.
     * 
     * @param element
     *            The XML element
     * @param mapSize
     *            The map size
     */

    public ActionClassMap(Element element, int mapSize)
    {
        this(mapSize);

        String data;
        char c;
        int i;
        byte b;

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

    public ActionClassMap(int mapSize)
    {
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

    public ActionClassMap(byte[] bytes, int mapSize)
    {
        this(mapSize);
        this.offset = 0;
        this.size = mapSize * mapSize / 2;
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
        for (int y = 0, yMax = this.map.length; y < yMax; y++)
        {
            writer.print("    ");
            for (int x = 0, xMax = this.map[y].length; x < xMax; x++)
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
     * @see de.ailis.wlandsuite.game.parts.Part#write(java.io.OutputStream, int)
     */
    
    public void write(OutputStream stream, int offset) throws IOException
    {
        for (int y = 0, yMax = this.map.length; y < yMax; y++)
        {
            for (int x = 0, xMax = this.map[y].length; x < xMax; x += 2)
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

    
    /**
     * Sets the action class for a specific position
     * 
     * @param x
     *            The X position
     * @param y
     *            The Y position
     * @param actionClass
     *            The action class
     */

    public void setActionClass(int x, int y, byte actionClass)
    {
        this.map[y][x] = actionClass;
    }
}
