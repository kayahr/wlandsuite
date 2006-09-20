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
import java.util.ArrayList;
import java.util.List;

import de.ailis.wlandsuite.utils.XMLUtils;
import org.dom4j.Element;

import de.ailis.wlandsuite.io.SeekableInputStream;
import de.ailis.wlandsuite.io.SeekableOutputStream;


/**
 * All the Monsters of a map. Be careful when you delete a Monster because other
 * Monsters will get a new index and you have to correct all references to these
 * Monsters.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Monsters extends ArrayList<Monster>
{
    /** Serial version UID */
    private static final long serialVersionUID = -3991988772308292906L;


    /**
     * Constructor
     */

    public Monsters()
    {
        super();
    }


    /**
     * Constructor
     * 
     * @param capacity
     */

    public Monsters(int capacity)
    {
        super(capacity);
    }


    /**
     * Creates and returns a new Monsters object by reading all the Monsters
     * from the specified stream. The stream must be positioned at the beginning
     * of the monster names table and the offset to the monster data must be
     * specified together with the number of monsters to read.
     * 
     * @param stream
     *            The stream to read the Monsters from
     * @param dataOffset
     *            The offset of the monster data block
     * @param quantity
     *            The number of monsters to read
     * @return The Monsters
     * @throws IOException
     */

    public static Monsters read(SeekableInputStream stream, int dataOffset,
        int quantity) throws IOException
    {
        Monsters monsters;
        List<String> names;

        // Read the monster names
        names = new ArrayList<String>();
        for (int i = 0; i < quantity; i++)
        {
            StringBuilder name = new StringBuilder();
            while (true)
            {
                String s;
                int b;

                b = stream.readByte();
                if (b == 0) break;
                s = new String(new byte[] { (byte) b }, "ASCII");
                name.append(s);
            }
            names.add(name.toString());
        }

        // Create the monsters object
        monsters = new Monsters(quantity);

        // Seek to monster data
        stream.seek(dataOffset);
        for (String name: names)
        {
            monsters.add(Monster.read(stream, name));
        }

        // Return the monsters object
        return monsters;
    }
    
    
    /**
     * Creates and returns a new Monsters object from XML.
     *
     * @param element The XML element
     * @return The Monsters
     */
    
    @SuppressWarnings("unchecked")
    public static Monsters read(Element element)
    {
        Monsters monsters;
        List<Element> subElements;
        
        subElements = element.elements("monster");
        monsters = new Monsters(subElements.size());
        for (Element subElement: subElements)
        {
            monsters.add(Monster.read(subElement));
        }
        return monsters;
    }


    /**
     * Writes the monster data to the specified output stream.
     * 
     * @param stream
     *            The output stream
     */

    public void writeData(SeekableOutputStream stream)
    {
        for (Monster monster: this)
        {
            monster.write(stream);
        }
    }
    

    /**
     * Writes the monster names to the specified output stream.
     * 
     * @param stream
     *            The output stream
     * @throws IOException
     */

    public void writeNames(OutputStream stream)
        throws IOException
    {
        for (Monster monster: this)
        {
            stream.write(monster.getName().getBytes("ASCII"));
            stream.write(0);
        }
    }


    /**
     * Returns the monsters as XML.
     * 
     * @return The monsters as XML
     */

    public Element toXml()
    {
        Element element, subElement;
        int monsterNo;

        // Create the root XML element
        element = XMLUtils.createElement("monsters");

        // Add all the monsters
        monsterNo = 0;
        for (Monster monster: this)
        {
            // Create and append string element
            subElement = monster.toXml(monsterNo);
            element.add(subElement);
            monsterNo++;
        }

        // Return the XML element
        return element;
    }
}
