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

import de.ailis.wlandsuite.utils.XMLUtils;
import org.dom4j.Element;

import de.ailis.wlandsuite.io.SeekableInputStream;
import de.ailis.wlandsuite.io.SeekableOutputStream;


/**
 * Additional map information.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Info
{
    /** The unknown byte at offset 0 */
    private int unknown0;

    /** The unknown byte at offset 1 */
    private int unknown1;

    /** The encounter frequency */
    private int encounterFrequency;

    /** The tileset to use */
    private int tileset;

    /** The last monster to use in random encounters */
    private int lastMonster;

    /** The maximum number of simultaneous encounters */
    private int maxEncounters;

    /** The background tile */
    private int backgroundTile;

    /** The time factor of the map (1 Minute = 256) */
    private int timeFactor;

    /** The unknown byte at offset 9 */
    private int unknown9;


    /**
     * Constructor
     */

    public Info()
    {
        super();
    }


    /**
     * Creates and returns a new Info object by reading it from the specified
     * stream.
     * 
     * @param stream
     *            The stream to read the central directory from.
     * @return The central directory
     * @throws IOException
     */

    public static Info read(SeekableInputStream stream) throws IOException
    {
        Info info;

        // Create new Info object
        info = new Info();

        info.unknown0 = stream.readByte();
        info.unknown1 = stream.readByte();
        info.encounterFrequency = stream.readByte();
        info.tileset = stream.readByte();
        info.lastMonster = stream.readByte();
        info.maxEncounters = stream.readByte();
        info.backgroundTile = stream.readByte();
        info.timeFactor = stream.readWord();
        info.unknown9 = stream.readByte();

        // Return the newly created Info object
        return info;
    }


    /**
     * Writes the info object to the specified output stream.
     * 
     * @param stream
     *            The stream to write the info object to
     */

    public void write(SeekableOutputStream stream)
    {
        stream.writeByte(this.unknown0);
        stream.writeByte(this.unknown1);
        stream.writeByte(this.encounterFrequency);
        stream.writeByte(this.tileset);
        stream.writeByte(this.lastMonster);
        stream.writeByte(this.maxEncounters);
        stream.writeByte(this.backgroundTile);
        stream.writeWord(this.timeFactor);
        stream.writeByte(this.unknown9);
    }


    /**
     * Creates and returns a new Info object from XML.
     * 
     * @param element
     *            The XML element
     * @return The new Info object
     */

    public static Info read(Element element)
    {
        Info info;

        info = new Info();

        info.tileset = Integer.parseInt(element.attributeValue("tileset"));
        info.backgroundTile = Integer.parseInt(element
            .attributeValue("backgroundTile", "0"));
        info.timeFactor = Integer
            .parseInt(element.attributeValue("timeFactor", "256"));
        info.encounterFrequency = Integer.parseInt(element
            .attributeValue("encounterFrequency", "0"));
        info.lastMonster = Integer.parseInt(element
            .attributeValue("lastMonster", "1"));
        info.maxEncounters = Integer.parseInt(element
            .attributeValue("maxEncounters"));
        info.unknown0 = Integer.parseInt(element.attributeValue("unknown0", "0"));
        info.unknown1 = Integer.parseInt(element.attributeValue("unknown1", "0"));
        info.unknown9 = Integer.parseInt(element.attributeValue("unknown9", "0"));

        return info;
    }

    /**
     * Returns the map info as XML.
     * 
     * @return The map info as XML
     */

    public Element toXml()
    {
        Element element;

        element = XMLUtils.createElement("info");
        element.addAttribute("tileset", Integer.toString(this.tileset));
        if (this.backgroundTile != 0)
        {
            element.addAttribute("backgroundTile", Integer
                .toString(this.backgroundTile));
        }
        if (this.timeFactor != 256)
        {
            element.addAttribute("timeFactor", Integer.toString(this.timeFactor));
        }
        if (this.encounterFrequency != 0)
        {
            element.addAttribute("encounterFrequency", Integer
                .toString(this.encounterFrequency));
        }
        if (this.lastMonster != 1)
        {
            element.addAttribute("lastMonster", Integer.toString(this.lastMonster));
        }
        if (this.maxEncounters != 1)
        {
            element.addAttribute("maxEncounters", Integer
                .toString(this.maxEncounters));
        }
        if (this.unknown0 != 0)
        {
            element.addAttribute("unknown0", Integer.toString(this.unknown0));
        }
        if (this.unknown1 != 0)
        {
            element.addAttribute("unknown1", Integer.toString(this.unknown1));
        }
        if (this.unknown9 != 0)
        {
            element.addAttribute("unknown9", Integer.toString(this.unknown9));
        }

        return element;
    }


    /**
     * Returns the backgroundTile.
     *
     * @return The backgroundTile
     */
    
    public int getBackgroundTile()
    {
        return this.backgroundTile;
    }


    /**
     * Sets the backgroundTile.
     *
     * @param backgroundTile 
     *            The backgroundTile to set
     */
    
    public void setBackgroundTile(int backgroundTile)
    {
        this.backgroundTile = backgroundTile;
    }


    /**
     * Returns the encounterFrequency.
     *
     * @return The encounterFrequency
     */
    
    public int getEncounterFrequency()
    {
        return this.encounterFrequency;
    }


    /**
     * Sets the encounterFrequency.
     *
     * @param encounterFrequency 
     *            The encounterFrequency to set
     */
    
    public void setEncounterFrequency(int encounterFrequency)
    {
        this.encounterFrequency = encounterFrequency;
    }


    /**
     * Returns the lastMonster.
     *
     * @return The lastMonster
     */
    
    public int getLastMonster()
    {
        return this.lastMonster;
    }


    /**
     * Sets the lastMonster.
     *
     * @param lastMonster 
     *            The lastMonster to set
     */
    
    public void setLastMonster(int lastMonster)
    {
        this.lastMonster = lastMonster;
    }


    /**
     * Returns the maxEncounters.
     *
     * @return The maxEncounters
     */
    
    public int getMaxEncounters()
    {
        return this.maxEncounters;
    }


    /**
     * Sets the maxEncounters.
     *
     * @param maxEncounters 
     *            The maxEncounters to set
     */
    
    public void setMaxEncounters(int maxEncounters)
    {
        this.maxEncounters = maxEncounters;
    }


    /**
     * Returns the tileset.
     *
     * @return The tileset
     */
    
    public int getTileset()
    {
        return this.tileset;
    }


    /**
     * Sets the tileset.
     *
     * @param tileset 
     *            The tileset to set
     */
    
    public void setTileset(int tileset)
    {
        this.tileset = tileset;
    }


    /**
     * Returns the timeFactor.
     *
     * @return The timeFactor
     */
    
    public int getTimeFactor()
    {
        return this.timeFactor;
    }


    /**
     * Sets the timeFactor.
     *
     * @param timeFactor 
     *            The timeFactor to set
     */
    
    public void setTimeFactor(int timeFactor)
    {
        this.timeFactor = timeFactor;
    }


    /**
     * Returns the unknown0.
     *
     * @return The unknown0
     */
    
    public int getUnknown0()
    {
        return this.unknown0;
    }


    /**
     * Sets the unknown0.
     *
     * @param unknown0 
     *            The unknown0 to set
     */
    
    public void setUnknown0(int unknown0)
    {
        this.unknown0 = unknown0;
    }


    /**
     * Returns the unknown1.
     *
     * @return The unknown1
     */
    
    public int getUnknown1()
    {
        return this.unknown1;
    }


    /**
     * Sets the unknown1.
     *
     * @param unknown1 
     *            The unknown1 to set
     */
    
    public void setUnknown1(int unknown1)
    {
        this.unknown1 = unknown1;
    }


    /**
     * Returns the unknown9.
     *
     * @return The unknown9
     */
    
    public int getUnknown9()
    {
        return this.unknown9;
    }


    /**
     * Sets the unknown9.
     *
     * @param unknown9 
     *            The unknown9 to set
     */
    
    public void setUnknown9(int unknown9)
    {
        this.unknown9 = unknown9;
    }
}
