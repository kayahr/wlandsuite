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

import de.ailis.wlandsuite.common.exceptions.GameException;
import de.ailis.wlandsuite.utils.XmlUtils;


/**
 * The Action Map describes the actions of all the map squares.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class ActionMap
{
    /** The actions */
    private int[][] actions;


    /**
     * Constructs a new action map with the specified map size. Map size must be
     * 32 or 64. Any other value throws an IllegalArgumentException.
     * 
     * @param mapSize
     *            The map size
     * @throws IllegalArgumentException
     *             If specified size is not 32 and not 64.
     */

    public ActionMap(int mapSize)
    {
        // Validate the map size
        if (mapSize != 32 && mapSize != 64)
        {
            throw new IllegalArgumentException("Illegal map size specified: "
                + mapSize);
        }

        // Initialize the internal arrays
        this.actions = new int[mapSize][mapSize];
    }


    /**
     * Creates a new Action Map by reading it from the specified input stream.
     * The map size must be specified because it can't be read from the stream
     * automatically. Map size must be 32 or 64. Any other value throws an
     * IllegalArgumentException.
     * 
     * @param stream
     *            The input stream to read the action map from
     * @param mapSize
     *            The map size (Must be 32 or 64)
     * @return The newly constructed Action Map
     * @throws IOException
     * @throws IllegalArgumentException
     *             If specified size is not 32 and not 64.
     */

    public static ActionMap read(InputStream stream, int mapSize)
        throws IOException
    {
        ActionMap actionMap;

        // Create the action map
        actionMap = new ActionMap(mapSize);

        // Parse the actions from the stream
        for (int y = 0; y < mapSize; y++)
        {
            for (int x = 0; x < mapSize; x++)
            {
                // Read the action from the stream
                actionMap.actions[y][x] = stream.read();
            }
        }

        // Return the newly constructed action map
        return actionMap;
    }


    /**
     * Writes the action map to the specified output stream.
     * 
     * @param stream
     *            The output stream to write the action map to
     * @throws IOException
     */

    public void write(OutputStream stream) throws IOException
    {
        int mapSize;

        // Determine the map size;
        mapSize = this.actions.length;

        // Write the actions
        for (int y = 0; y < mapSize; y++)
        {
            for (int x = 0; x < mapSize; x++)
            {
                stream.write(this.actions[y][x]);
            }
        }
    }


    /**
     * Creates and returns a new action map read from XML.
     * 
     * @param element
     *            The XML element
     * @param mapSize
     *            The map size
     * @return The action map
     */

    public static ActionMap read(Element element, int mapSize)
    {
        ActionMap actionMap;
        String data;
        int i;
        int b;
        String s;

        // Create the new action map
        actionMap = new ActionMap(mapSize);

        // Parse the actions
        data = element.getTextTrim();
        i = 0;
        for (int y = 0; y < mapSize; y++)
        {
            for (int x = 0; x < mapSize; x++)
            {
                try
                {
                    s = data.substring(i * 3, i * 3 + 2);
                }
                catch (StringIndexOutOfBoundsException e)
                {
                    throw new GameException("Action selector map is corrupt: "
                        + (mapSize * mapSize - (y * mapSize + x))
                        + " bytes missing");
                }
                if (s.equals("..")) s = "00";
                try
                {
                    b = Integer.valueOf(s, 16);
                }
                catch (NumberFormatException e)
                {
                    throw new GameException(
                        "Illegal data in action selector map at y=" + y + " x="
                            + x);
                }
                actionMap.actions[y][x] = b;
                i++;
            }
        }

        // Return the newly created action map
        return actionMap;
    }


    /**
     * Returns the action map in XML format.
     * 
     * @param actionClassMap
     *            The action class map
     * @return The action map in XML
     */

    public Element toXml(ActionClassMap actionClassMap)
    {
        Element element;
        StringWriter text;
        PrintWriter writer;
        int mapSize;

        // Determine the map size
        mapSize = this.actions.length;

        // Create the root XML element
        element = XmlUtils.createElement("actionMap");

        // Write the actions content
        text = new StringWriter();
        writer = new PrintWriter(text);
        writer.println();
        for (int y = 0; y < mapSize; y++)
        {
            writer.print("    ");
            for (int x = 0; x < mapSize; x++)
            {
                int b;

                if (x > 0)
                {
                    writer.print(" ");
                }

                b = this.actions[y][x];
                if (b == 0 && (actionClassMap.getActionClass(x, y) == 0))
                {
                    writer.print("..");
                }
                else
                {
                    writer.format("%02x", new Object[] { b });
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
     * Returns the action at the specified position
     * 
     * @param x
     *            The x position
     * @param y
     *            The y position
     * @return The action
     */

    public int getAction(int x, int y)
    {
        return this.actions[y][x];
    }


    /**
     * Sets the action at the specified position.
     * 
     * @param x
     *            The x position
     * @param y
     *            The y position
     * @param action
     *            The action to set
     */

    public void setAction(int x, int y, int action)
    {
        this.actions[y][x] = action;
    }
}
