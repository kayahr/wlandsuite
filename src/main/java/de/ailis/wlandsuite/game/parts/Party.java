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

import de.ailis.wlandsuite.common.exceptions.GameException;
import de.ailis.wlandsuite.utils.StringUtils;
import de.ailis.wlandsuite.utils.XmlUtils;


/**
 * Party is a container for party members (Which are just the member ids).
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Party extends ArrayList<Integer>
{
    /** Serial version UID */
    private static final long serialVersionUID = -3080130103326593294L;

    /** X position on the map */
    private int x;

    /** Y position on the map */
    private int y;

    /** The current map */
    private int map;

    /** The previous X position on the previous map (For FF transitions) */
    private int prevX;

    /** The previous Y position on the previous map (For FF transitions) */
    private int prevY;

    /** The previous map (For FF transitions) */
    private int prevMap;


    /**
     * Constructor
     */

    public Party()
    {
        super(7);
    }


    /**
     * Creates a new party be reading its data from the specified stream.
     * 
     * @param stream
     *            The input stream
     * @return The new party
     * @throws IOException
     */

    public static Party read(InputStream stream) throws IOException
    {
        Party party;
        int member;

        party = new Party();
        stream.skip(1);
        for (int i = 0; i < 7; i++)
        {
            member = stream.read();
            if (member > 0) party.add(member);
        }
        party.x = stream.read();
        party.y = stream.read();
        party.map = stream.read();
        party.prevX = stream.read();
        party.prevY = stream.read();
        party.prevMap = stream.read();

        return party;
    }


    /**
     * Writes the party to the specified stream.
     * 
     * @param stream
     *            The output stream
     * @throws IOException
     */

    public void write(OutputStream stream) throws IOException
    {
        if (size() > 7)
        {
            throw new GameException(
                "Party cannot have more than 7 members but this party has "
                    + size());
        }

        stream.write(0);
        for (int member: this)
        {
            stream.write(member);
        }
        for (int i = size(); i < 7; i++)
        {
            stream.write(0);
        }
        stream.write(this.x);
        stream.write(this.y);
        stream.write(this.map);
        stream.write(this.prevX);
        stream.write(this.prevY);
        stream.write(this.prevMap);
    }


    /**
     * Creates a new party by reading its data from the specified XML element.
     * 
     * @param element
     *            The XML element
     * @return The new party
     */

    public static Party read(Element element)
    {
        Party party;

        party = new Party();
        party.x = StringUtils.toInt(element.attributeValue("x"));
        party.y = StringUtils.toInt(element.attributeValue("y"));
        party.map = StringUtils.toInt(element.attributeValue("map"));
        party.prevX = StringUtils.toInt(element.attributeValue("prevX", "0"));
        party.prevY = StringUtils.toInt(element.attributeValue("prevY", "0"));
        party.prevMap = StringUtils.toInt(element
            .attributeValue("prevMap", "0"));
        for (Object item: element.elements("member"))
        {
            Element member = (Element) item;

            party.add(StringUtils.toInt(member.getTextTrim()));
        }
        return party;
    }


    /**
     * Returns the XML representation of the party.
     * 
     * @param id
     *            The party id
     * @return The XML element
     */

    public Element toXml(int id)
    {
        Element element;

        if (size() > 7)
        {
            throw new GameException(
                "Party cannot have more than 7 members but this party has "
                    + size());
        }

        element = XmlUtils.createElement("party");
        element.addAttribute("id", Integer.toString(id));
        element.addAttribute("x", Integer.toString(this.x));
        element.addAttribute("y", Integer.toString(this.y));
        element.addAttribute("map", Integer.toString(this.map));
        element.addAttribute("prevX", Integer.toString(this.prevX));
        element.addAttribute("prevY", Integer.toString(this.prevY));
        element.addAttribute("prevMap", Integer.toString(this.prevMap));
        for (int member: this)
        {
            Element subElement;

            subElement = XmlUtils.createElement("member");
            subElement.addText(Integer.toString(member));
            element.add(subElement);
        }
        return element;
    }


    /**
     * Returns the map.
     * 
     * @return The map
     */

    public int getMap()
    {
        return this.map;
    }


    /**
     * Sets the map.
     * 
     * @param map
     *            The map to set
     */

    public void setMap(int map)
    {
        this.map = map;
    }


    /**
     * Returns the previous map (For FF transitions):
     * 
     * @return The map
     */

    public int getPrevMap()
    {
        return this.prevMap;
    }


    /**
     * Sets the previous map (For FF transitions).
     * 
     * @param prevMap
     *            The map to set
     */

    public void setPrevMap(int prevMap)
    {
        this.prevMap = prevMap;
    }


    /**
     * Returns the previous X position on the previous map (For FF transitions).
     * 
     * @return The X position
     */

    public int getPrevX()
    {
        return this.prevX;
    }


    /**
     * Sets the previous X position on the previous map (For FF transitions).
     * 
     * @param prevX
     *            The X position to set
     */

    public void setPrevX(int prevX)
    {
        this.prevX = prevX;
    }


    /**
     * Returns the previous Y position on the previous map (For FF transitions).
     * 
     * @return The Y position
     */

    public int getPrevY()
    {
        return this.prevY;
    }


    /**
     * Sets the previous Y position on the previous map (For FF transitions).
     * 
     * @param prevY
     *            The Y position to set
     */

    public void setPrevY(int prevY)
    {
        this.prevY = prevY;
    }


    /**
     * Returns the x position on the map.
     * 
     * @return The x position
     */

    public int getX()
    {
        return this.x;
    }


    /**
     * Sets the x position on the map.
     * 
     * @param x
     *            The x position to set
     */

    public void setX(int x)
    {
        this.x = x;
    }


    /**
     * Returns the y position on the map.
     * 
     * @return The y position
     */

    public int getY()
    {
        return this.y;
    }


    /**
     * Sets the y position on the map.
     * 
     * @param y
     *            The y position to set
     */

    public void setY(int y)
    {
        this.y = y;
    }
}
