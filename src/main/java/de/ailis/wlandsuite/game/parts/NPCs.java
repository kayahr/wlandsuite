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
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

import de.ailis.wlandsuite.io.SeekableInputStream;
import de.ailis.wlandsuite.io.SeekableOutputStream;
import de.ailis.wlandsuite.utils.XmlUtils;


/**
 * All the NPCs of a map. Be careful when you delete an NPC because other NPCs
 * will get a new index and you have to correct all references to these NPCs.
 *
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class NPCs extends ArrayList<Char>
{
    /** Serial version UID */
    private static final long serialVersionUID = 1362069659860711472L;


    /**
     * Constructs a new String list
     */

    public NPCs()
    {
        super();
    }


    /**
     * Constructor
     *
     * @param capacity
     *            The initial capacity
     */

    public NPCs(final int capacity)
    {
        super(capacity);
    }


    /**
     * Creates and returns a new NPCs object by reading all the NPCs from the
     * specified stream.
     *
     * @param stream
     *            The stream to read the strings from
     * @return The NPCs
     * @throws IOException
     *             When file operation fails.
     */

    public static NPCs read(final SeekableInputStream stream) throws IOException
    {
        long offset;
        NPCs npcs;
        int tmp;
        long quantity;

        offset = stream.tell();

        // Create the NPCs object
        npcs = new NPCs();

        // Read the first offset. Must be 0. If not, abort
        if (stream.readWord() != 0)
        {
            return npcs;
        }

        offset += 2;

        // Maximum number of characters is 255. If it's higher, abort
        quantity = (stream.readWord() - offset) / 2;
        if (quantity < 1 || quantity > 255)
        {
            return npcs;
        }

        // All offsets of the characters must be 0x100 bytes apart
        offset += quantity * 2;
        for (int i = 1; i < quantity; i++)
        {
            tmp = stream.readWord();
            if (tmp != (offset + i * 0x100))
            {
                return npcs;
            }
        }

        // Read all characters
        for (int i = 0; i < quantity; i++)
        {
            npcs.add(Char.read(stream));
        }

        // Return the newly created NPCs object
        return npcs;
    }


    /**
     * Creates and returns a new NPCs object from XML.
     *
     * @param element
     *            The XML element
     * @return The NPCs
     */

    @SuppressWarnings("unchecked")
    public static NPCs read(final Element element)
    {
        NPCs npcs;
        List<Element> subElements;

        npcs = new NPCs();
        if (element != null)
        {
            subElements = element.elements("character");
            for (final Element subElement: subElements)
            {
                npcs.add(Char.read(subElement));
            }
        }
        return npcs;
    }


    /**
     * Returns the NPCs as XML.
     *
     * @return The NPCs as XML
     */

    public Element toXml()
    {
        Element element, subElement;
        int npcNo;

        // Create the root XML element
        element = XmlUtils.createElement("npcs");

        // Add all the npcs
        npcNo = 1;
        for (final Char character: this)
        {
            // Create and append string element
            subElement = character.toXml(npcNo);
            element.add(subElement);
            npcNo++;
        }

        // Return the XML element
        return element;
    }


    /**
     * Writes the NPCs to the specified output stream.
     *
     * @param stream
     *            The output stream
     * @throws IOException
     *             When file operation fails.
     */

    public void write(final SeekableOutputStream stream) throws IOException
    {
        int offset;

        offset = (int) stream.tell();

        // Write the offsets
        stream.writeWord(0);
        for (int i = 0; i < size(); i++)
        {
            stream.writeWord(2 + size() * 2 + i * 256 + offset);
        }

        // Write the characters.
        for (final Char character: this)
        {
            character.write(stream);
        }
    }
}
