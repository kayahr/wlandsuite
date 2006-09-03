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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.ailis.wlandsuite.game.GameException;
import de.ailis.wlandsuite.io.BitInputStreamWrapper;
import de.ailis.wlandsuite.io.BitOutputStreamWrapper;


/**
 * Radiation code
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class RadiationCode extends AbstractPart
{
    /** If armor is ignored */
    private boolean ignoreArmor;
    
    /** The id of the message to print */
    private int message;
    
    /** The damage (Number of d6 dices) */
    private int damage;
    
    /** The new action class to set (255 means setting no new action) */
    private int actionClass;
    
    /** The new action selector to set (255 means setting no new selector) */
    private int actionSelector;


    /**
     * Constructor
     * 
     * @param bytes
     *            The game block data
     * @param offset
     *            The offset of the part in the game block
     */

    public RadiationCode(byte[] bytes, int offset)
    {
        int b;
        BitInputStreamWrapper bitStream;
        
        this.size = 0;
        this.offset = offset;
        try
        {
            bitStream = new BitInputStreamWrapper(new ByteArrayInputStream(bytes,
                offset, bytes.length - offset));

            // Read first byte
            b = bitStream.readByte();
            this.size++;
            this.message = b;
            this.ignoreArmor = (b & 1) == 1;
            
            // Read the damage
            this.damage = bitStream.readByte();
            this.size++;
            
            // Read the action class
            this.actionClass = bitStream.readByte();
            this.size++;

            // Read the action selector
            if (this.actionClass < 253)
            {
                this.actionSelector = bitStream.readByte();
                this.size++;
            }
            else
            {
                this.actionSelector = 255;
            }
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
    public RadiationCode(Element element)
    {
        super();
        
        this.ignoreArmor = Boolean.parseBoolean(element.attributeValue("ignoreArmor", "false"));
        this.message = Integer.parseInt(element.attributeValue("message", "0"));
        this.damage = Integer.parseInt(element.attributeValue("damage", "0"));
        this.actionClass = Integer.parseInt(element.attributeValue("class", "255"));
        this.actionSelector = Integer.parseInt(element.attributeValue("selector", "255"));
        
        // Validate ignoreArmor flag
        if (this.ignoreArmor && (this.message & 1) == 0)
        {
            throw new GameException("Invalid radiation data: Ignore armor flag can only be true when an odd message id is used");
        }
        if (!this.ignoreArmor && (this.message & 1) != 0)
        {
            throw new GameException("Invalid radiation data: Ignore armor flag can only be false when an even message id is used");
        }
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.Part#toXml()
     */

    public Element toXml()
    {
        Element element;

        element = DocumentHelper.createElement("radiation");
        element.addAttribute("offset", Integer.toString(this.offset));
        element.addAttribute("ignoreArmor", this.ignoreArmor ? "true" : "false");
        element.addAttribute("message", Integer.toString(this.message));
        element.addAttribute("damage", Integer.toString(this.damage));
        element.addAttribute("class", Integer.toString(this.actionClass));
        element.addAttribute("selector", Integer.toString(this.actionSelector));
        return element;
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.Part#write(java.io.OutputStream, int)
     */
    
    public void write(OutputStream stream, int offset) throws IOException
    {
        BitOutputStreamWrapper bitStream;
        int b;

        bitStream = new BitOutputStreamWrapper(stream);
        b = this.message;
        bitStream.writeByte(b);
        
        bitStream.writeByte(this.damage);
        bitStream.writeByte(this.actionClass);
        if (this.actionClass < 253)
        {
            bitStream.writeByte(this.actionSelector);
        }
        
        bitStream.flush();
    }


    /**
     * Returns the action class.
     *
     * @return The action class
     */
    
    public int getActionClass()
    {
        return this.actionClass;
    }


    /**
     * Sets the action class.
     *
     * @param actionClass 
     *            The action class to set
     */
    
    public void setActionClass(int actionClass)
    {
        this.actionClass = actionClass;
    }


    /**
     * Returns the action selector.
     *
     * @return The action selector
     */
    
    public int getActionSelector()
    {
        return this.actionSelector;
    }


    /**
     * Sets the action selector.
     *
     * @param actionSelector 
     *            The action selector to set
     */
    
    public void setActionSelector(int actionSelector)
    {
        this.actionSelector = actionSelector;
    }


    /**
     * Returns the ignoreArmor flag
     *
     * @return The ignorArmor flag
     */
    
    public boolean isIgnoreArmor()
    {
        return this.ignoreArmor;
    }


    /**
     * Sets the ignoreArmor flag.
     *
     * @param ignoreArmor 
     *            The ignoreArmor flag to set
     */
    
    public void setIgnoreArmor(boolean ignoreArmor)
    {
        this.ignoreArmor = ignoreArmor;
    }


    /**
     * Returns the message id.
     *
     * @return The message id
     */
    
    public int getMessage()
    {
        return this.message;
    }


    /**
     * Sets the message id.
     *
     * @param message 
     *            The message id to set
     */
    
    public void setMessage(int message)
    {
        this.message = message;
    }


    /**
     * Returns the damage.
     *
     * @return The damage
     */
    
    public int getDamage()
    {
        return this.damage;
    }


    /**
     * Sets the damage.
     *
     * @param damage 
     *            The damage to set
     */
    
    public void setTargetMap(int damage)
    {
        this.damage = damage;
    }
}
