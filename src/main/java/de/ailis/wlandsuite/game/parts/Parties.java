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
import java.util.ArrayList;

import org.dom4j.Element;

import de.ailis.wlandsuite.rawgame.GameException;
import de.ailis.wlandsuite.utils.StringUtils;
import de.ailis.wlandsuite.utils.XmlUtils;


/**
 * Parties
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Parties extends ArrayList<Party>
{
    /** Serial version UID */
    private static final long serialVersionUID = -1762204965578700760L;

    /** The currently active party */
    private int currentParty;


    /**
     * Constructor
     */

    public Parties()
    {
        super(4);
    }


    /**
     * Reads the parties from the specified input stream.
     * 
     * @param stream
     *            The input stream
     * @return The parties
     * @throws IOException
     */

    public static Parties read(InputStream stream) throws IOException
    {
        Parties parties;

        parties = new Parties();
        for (int i = 0; i < 4; i++)
        {
            parties.add(Party.read(stream));
        }
        return parties;
    }


    /**
     * Reads the parties from the specified XML element.
     * 
     * @param element
     *            The XML element
     * @return The parties
     */

    public static Parties read(Element element)
    {
        Parties parties;

        parties = new Parties();
        parties.currentParty = StringUtils.toInt(element
            .attributeValue("currentParty", "0"));
        for (Object item: element.elements("party"))
        {
            Element party = (Element) item;

            parties.add(Party.read(party));
        }
        return parties;
    }


    /**
     * Writes the parties to the specified output stream.
     * 
     * @param stream
     *            The output stream
     * @throws IOException
     */

    public void write(OutputStream stream) throws IOException
    {
        if (size() > 4)
        {
            throw new GameException(
                "There can be only 4 parties in the save game but here we have "
                    + size());
        }

        // Write the parties
        for (Party party: this)
        {
            party.write(stream);
        }

        // Fill with empty parties
        Party empty = null;
        for (int i = size(); i < 4; i++)
        {
            if (empty == null) empty = new Party();
            empty.write(stream);
        }
    }


    /**
     * Returns the XML representation of the parties.
     * 
     * @return The XML element
     */

    public Element toXml()
    {
        int id;
        Element element;

        if (size() > 4)
        {
            throw new GameException(
                "There can be only 4 parties in the save game but here we have "
                    + size());
        }

        element = XmlUtils.createElement("parties");
        element.addAttribute("currentParty", Integer
            .toString(this.currentParty));
        id = 0;
        for (Party party: this)
        {
            element.add(party.toXml(id));
            id++;
        }
        return element;
    }


    /**
     * Returns the currentParty.
     * 
     * @return The currentParty
     */

    public int getCurrentParty()
    {
        return this.currentParty;
    }


    /**
     * Sets the currentParty.
     * 
     * @param currentParty
     *            The currentParty to set
     */

    public void setCurrentParty(int currentParty)
    {
        this.currentParty = currentParty;
    }


    /**
     * Returns the total number of party members
     * 
     * @return The total number of party members
     */

    public int getTotalMembers()
    {
        int members = 0;

        for (Party party: this)
        {
            for (int member: party)
            {
                if (member > members) members = member;
            }
        }
        return members;
    }
}
