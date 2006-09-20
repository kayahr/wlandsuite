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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.ailis.wlandsuite.rawgame.GameException;
import de.ailis.wlandsuite.utils.XMLUtils;


/**
 * The Action Class Map describes the action classes of all the map squares.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class ActionClassMap
{
    /** The action classes */
    private int[][] actionClasses;


    /**
     * Constructs a new action classes map with the specified map size. Map size
     * must be 32 or 64. Any other value throws an IllegalArgumentException.
     * 
     * @param mapSize
     *            The map size
     * @throws IllegalArgumentException
     *             If specified size is not 32 and not 64.
     */

    public ActionClassMap(int mapSize)
    {
        // Validate the map size
        if (mapSize != 32 && mapSize != 64)
        {
            throw new IllegalArgumentException("Illegal map size specified: "
                + mapSize);
        }

        // Initialize the internal array
        this.actionClasses = new int[mapSize][mapSize];
    }


    /**
     * Creates a new Action Classes Map by reading it from the specified input
     * stream. The map size must be specified because it can't be read from the
     * stream automatically. Map size must be 32 or 64. Any other value throws
     * an IllegalArgumentException.
     * 
     * @param stream
     *            The input stream to read the action classes map from
     * @param mapSize
     *            The map size (Must be 32 or 64)
     * @return The newly constructed Action Classes Map
     * @throws IOException
     * @throws IllegalArgumentException
     *             If specified size is not 32 and not 64.
     */

    public static ActionClassMap read(InputStream stream, int mapSize)
        throws IOException
    {
        ActionClassMap actionClassMap;

        // Create the action map
        actionClassMap = new ActionClassMap(mapSize);

        // Parse the action classes from the stream
        for (int y = 0; y < mapSize; y++)
        {
            for (int x = 0; x < mapSize; x += 2)
            {
                int b;

                // Read the byte from the stream (Contains two classes)
                b = stream.read();

                // Set the action classes for two squares
                actionClassMap.actionClasses[y][x + 0] = (b >> 4) & 0x0f;
                actionClassMap.actionClasses[y][x + 1] = (b >> 0) & 0x0f;
            }
        }

        // Return the newly constructed action map
        return actionClassMap;
    }


    /**
     * Writes the action classes map to the specified output stream.
     * 
     * @param stream
     *            The output stream to write the action classes map to
     * @throws IOException
     */

    public void write(OutputStream stream) throws IOException
    {
        int mapSize;

        // Determine the map size;
        mapSize = this.actionClasses.length;

        // Write the action classes
        for (int y = 0; y < mapSize; y++)
        {
            for (int x = 0; x < mapSize; x += 2)
            {
                int b;

                b = (this.actionClasses[y][x] << 4)
                    | this.actionClasses[y][x + 1];
                stream.write(b);
            }
        }
    }


    /**
     * Creates and returns a new action classes map read from XML.
     * 
     * @param element
     *            The XML element
     * @param mapSize
     *            The map size
     * @return The action classes map
     */

    public static ActionClassMap read(Element element, int mapSize)
    {
        ActionClassMap actionClassMap;
        String data;
        char c;
        int i;
        int b;

        // Create the new action map
        actionClassMap = new ActionClassMap(mapSize);

        // Parse the action classes
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
                actionClassMap.actionClasses[y][x] = b;
                i++;
            }
            i++;
        }

        // Return the newly created action map
        return actionClassMap;
    }


    /**
     * Returns the action classes map in XML format.
     * 
     * @return The action classes map in XML
     */

    public Element toXml()
    {
        Element element;
        StringWriter text;
        PrintWriter writer;
        int mapSize;

        // Determine the map size
        mapSize = this.actionClasses.length;

        // Create the root XML element
        element = XMLUtils.createElement("actionClassMap");

        // Write the actionClasses content
        text = new StringWriter();
        writer = new PrintWriter(text);
        writer.println();
        for (int y = 0; y < mapSize; y++)
        {
            // Write indentation
            writer.print("    ");

            for (int x = 0; x < mapSize; x++)
            {
                int b;

                b = this.actionClasses[y][x];
                if (b == 0)
                {
                    // For action class 0 it's ok to write a dot instead of 0.
                    writer.print('.');
                }
                else
                {
                    // For all other action classes write the hex character
                    writer.print(Integer.toHexString(b));
                }
            }
            writer.println();
        }
        writer.print("  ");
        element.add(DocumentHelper.createText(text.toString()));

        // Return the XML element
        return element;
    }


    /**
     * Checks if the specified action class exists in the action class map.
     * 
     * @param actionClass
     *            The action class to search
     * @return If the action class exists or not
     */

    public boolean hasActionClass(int actionClass)
    {
        int mapSize = this.actionClasses.length;

        for (int y = 0; y < mapSize; y++)
        {
            for (int x = 0; x < mapSize; x++)
            {
                if (this.actionClasses[y][x] == actionClass)
                {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Returns the action class at the specified position
     * 
     * @param x
     *            The x position
     * @param y
     *            The y position
     * @return The action class
     */

    public int getActionClass(int x, int y)
    {
        return this.actionClasses[y][x];
    }


    /**
     * Sets the action class at the specified position.
     * 
     * @param x
     *            The x position
     * @param y
     *            The y position
     * @param actionClass
     *            The action class to set
     */

    public void setActionClass(int x, int y, int actionClass)
    {
        this.actionClasses[y][x] = actionClass;
    }
}
