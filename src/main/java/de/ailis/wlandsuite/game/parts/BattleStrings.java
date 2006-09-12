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

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.ailis.wlandsuite.io.SeekableInputStream;
import de.ailis.wlandsuite.io.SeekableOutputStream;


/**
 * The battle strings.
 * 
 * TODO Find out how the battle strings work in the game and rename all the
 * attributes in something meaningful.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class BattleStrings
{
    /** Battle strings */
    private int[] strings = new int[37];


    /**
     * Constructor
     */

    public BattleStrings()
    {
        super();
    }


    /**
     * Creates and returns a new BattleStrings object by reading it from the
     * specified stream.
     * 
     * @param stream
     *            The stream to read the battle strings from.
     * @return The battle strings
     * @throws IOException
     */

    public static BattleStrings read(SeekableInputStream stream)
        throws IOException
    {
        BattleStrings info;

        // Create new Info object
        info = new BattleStrings();

        for (int i = 0; i < 37; i++)
        {
            info.strings[i] = stream.readByte();
        }

        // Return the newly created Info object
        return info;
    }


    /**
     * Writes the batte strings object to the specified output stream.
     * 
     * @param stream
     *            The stream to write the battle strings object to
     */

    public void write(SeekableOutputStream stream)
    {
        for (int i = 0; i < 37; i++)
        {
            stream.writeByte(this.strings[i]);
        }
    }


    /**
     * Creates and returns a new battel strings object from XML.
     * 
     * @param element
     *            The XML element
     * @return The new battle strings object
     */

    public static BattleStrings read(Element element)
    {
        BattleStrings info;

        info = new BattleStrings();

        for (int i = 0; i < 37; i++)
        {
            info.strings[i] = Integer.parseInt(element.attributeValue("s" + i));
        }

        return info;
    }

    /**
     * Returns the battle strings as XML.
     * 
     * @return The battle strings as XML
     */

    public Element toXml()
    {
        Element element;

        element = DocumentHelper.createElement("battleStrings");
        for (int i = 0; i < 37; i++)
        {
            element.addAttribute("s" + i, Integer.toString(this.strings[i]));
        }

        return element;
    }
}
