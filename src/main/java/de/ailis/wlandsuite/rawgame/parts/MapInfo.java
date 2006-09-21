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

package de.ailis.wlandsuite.rawgame.parts;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import de.ailis.wlandsuite.utils.XmlUtils;
import org.dom4j.Element;

import de.ailis.wlandsuite.io.BitInputStreamWrapper;
import de.ailis.wlandsuite.io.BitOutputStreamWrapper;
import de.ailis.wlandsuite.rawgame.GameException;


/**
 * Map Info
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class MapInfo extends AbstractPart
{
    /** The map size */
    private int mapSize;
    
    /** The unknown value at offset 1 */ 
    private int unknown1;
    
    /** The unknown value at offset 1 */ 
    private int unknown2;
    
    /** The random encounter */
    private int encounterFrequency;
    
    /** The tileset to use */
    private int tileset;
    
    /** The last monster to use in random encounters */ 
    private int lastMonster;
    
    /** The unknown value at offset 6 */ 
    private int unknown6;
    
    /** The unknown value at offset 7 */ 
    private int unknown7;
    
    /** The time factor of the map (1 Minute = 256) */
    private int timeFactor;


    /**
     * Constructor
     * 
     * @param bytes
     *            The game block data
     * @param offset
     *            The offset of the part in the game block
     */

    public MapInfo(byte[] bytes, int offset)
    {
        BitInputStreamWrapper bitStream;

        this.size = 10;
        this.offset = offset;
        bitStream = new BitInputStreamWrapper(new ByteArrayInputStream(bytes,
            offset, this.size));
        try
        {
            this.mapSize = bitStream.read();
            this.unknown1 = bitStream.read();
            this.unknown2 = bitStream.read();
            this.encounterFrequency = bitStream.read();
            this.tileset = bitStream.read();
            this.lastMonster = bitStream.read();
            this.unknown6 = bitStream.read();
            this.unknown7 = bitStream.read();
            this.timeFactor = bitStream.readWord();            
        }
        catch (IOException e)
        {
            throw new GameException(e.toString(), e);
        }
    }


    /**
     * Creates the central directory from XML.
     * 
     * @param element
     *            The XML element
     */

    @SuppressWarnings("unchecked")
    public MapInfo(Element element)
    {
        super();

        this.mapSize = Integer.parseInt(element
            .attributeValue("mapSize"));
        this.unknown1 = Integer.parseInt(element
            .attributeValue("unknown1"));
        this.unknown2 = Integer.parseInt(element
            .attributeValue("unknown2"));
        this.encounterFrequency = Integer.parseInt(element
            .attributeValue("encounterFrequency"));
        this.tileset = Integer.parseInt(element
            .attributeValue("tileset"));
        this.lastMonster = Integer.parseInt(element
            .attributeValue("lastMonster"));
        this.unknown6 = Integer.parseInt(element
            .attributeValue("unknown6"));
        this.unknown7 = Integer.parseInt(element
            .attributeValue("unknown7"));
        this.timeFactor = Integer.parseInt(element
            .attributeValue("timeFactor"));
    }


    /**
     * @see de.ailis.wlandsuite.rawgame.parts.Part#toXml()
     */

    public Element toXml()
    {
        Element element;

        element = XmlUtils.createElement("mapInfo");
        element.addAttribute("offset", Integer.toString(this.offset));
        element.addAttribute("size", Integer.toString(this.size));
        element.addAttribute("mapSize", Integer
            .toString(this.mapSize));
        element.addAttribute("unknown1", Integer
            .toString(this.unknown1));
        element.addAttribute("unknown2", Integer
            .toString(this.unknown2));
        element.addAttribute("encounterFrequency", Integer
            .toString(this.encounterFrequency));
        element.addAttribute("tileset", Integer
            .toString(this.tileset));
        element.addAttribute("lastMonster", Integer
            .toString(this.lastMonster));
        element.addAttribute("unknown6", Integer
            .toString(this.unknown6));
        element.addAttribute("unknown7", Integer
            .toString(this.unknown7));
        element.addAttribute("timeFactor", Integer
            .toString(this.timeFactor));
        return element;
    }


    /**
     * @see de.ailis.wlandsuite.rawgame.parts.Part#write(java.io.OutputStream, int)
     */
    
    public void write(OutputStream stream, int offset) throws IOException
    {
        BitOutputStreamWrapper bitStream;

        bitStream = new BitOutputStreamWrapper(stream);
        bitStream.write(this.mapSize);
        bitStream.write(this.unknown1);
        bitStream.write(this.unknown2);
        bitStream.write(this.encounterFrequency);
        bitStream.write(this.tileset);
        bitStream.write(this.lastMonster);
        bitStream.write(this.unknown6);
        bitStream.write(this.unknown7);
        bitStream.writeWord(this.timeFactor);
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
     * Returns the mapSize.
     *
     * @return The mapSize
     */
    
    public int getMapSize()
    {
        return this.mapSize;
    }


    /**
     * Sets the mapSize.
     *
     * @param mapSize 
     *            The mapSize to set
     */
    
    public void setMapSize(int mapSize)
    {
        this.mapSize = mapSize;
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
     * Returns the unknown2.
     *
     * @return The unknown2
     */
    
    public int getUnknown2()
    {
        return this.unknown2;
    }


    /**
     * Sets the unknown2.
     *
     * @param unknown2 
     *            The unknown2 to set
     */
    
    public void setUnknown2(int unknown2)
    {
        this.unknown2 = unknown2;
    }


    /**
     * Returns the unknown6.
     *
     * @return The unknown6
     */
    
    public int getUnknown6()
    {
        return this.unknown6;
    }


    /**
     * Sets the unknown6.
     *
     * @param unknown6 
     *            The unknown6 to set
     */
    
    public void setUnknown6(int unknown6)
    {
        this.unknown6 = unknown6;
    }


    /**
     * Returns the unknown7.
     *
     * @return The unknown7
     */
    
    public int getUnknown7()
    {
        return this.unknown7;
    }


    /**
     * Sets the unknown7.
     *
     * @param unknown7 
     *            The unknown7 to set
     */
    
    public void setUnknown7(int unknown7)
    {
        this.unknown7 = unknown7;
    }
}
