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
 * Transition code
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class TransitionCode extends AbstractPart
{
    /** If positioning is relative */
    private boolean relative;
    
    /** If transition must be confirmed by the user */
    private boolean confirm;
    
    /** The message to print*/
    private int message;
    
    /** The target x position (relative or absolute) */
    private int targetX;
    
    /** The y position (relative or absolute) */
    private int targetY;
    
    /** The target map (255 means previous map) */
    private int targetMap;
    
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

    public TransitionCode(byte[] bytes, int offset)
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
            this.relative = (b & 0x80) != 0;
            this.confirm = (b & 0x40) != 0;
            this.message = b & 0x3f;
            
            // Read the X position
            this.targetX = bitStream.readSignedByte();
            this.size++;
            
            // Read the Y position
            this.targetY = bitStream.readSignedByte();
            this.size++;
            
            // Read the map
            this.targetMap = bitStream.readByte();
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
    public TransitionCode(Element element)
    {
        super();
        
        this.relative = Boolean.parseBoolean(element.attributeValue("relative", "false"));
        this.confirm = Boolean.parseBoolean(element.attributeValue("confirm", "false"));
        this.message = Integer.parseInt(element.attributeValue("message", "0"));
        this.targetX = Integer.parseInt(element.attributeValue("x", "0"));
        this.targetY = Integer.parseInt(element.attributeValue("y", "0"));
        this.targetMap = Integer.parseInt(element.attributeValue("map", "255"));
        this.actionClass = Integer.parseInt(element.attributeValue("class", "255"));
        this.actionSelector = Integer.parseInt(element.attributeValue("selector", "255"));
    }


    /**
     * @see de.ailis.wlandsuite.rawgame.parts.Part#toXml()
     */

    public Element toXml()
    {
        Element element;

        element = XmlUtils.createElement("transition");
        element.addAttribute("offset", Integer.toString(this.offset));
        element.addAttribute("relative", this.relative ? "true" : "false");
        element.addAttribute("confirm", this.confirm ? "true" : "false");
        element.addAttribute("message", Integer.toString(this.message));
        element.addAttribute("x", Integer.toString(this.targetX));
        element.addAttribute("y", Integer.toString(this.targetY));
        element.addAttribute("map", Integer.toString(this.targetMap));
        element.addAttribute("class", Integer.toString(this.actionClass));
        element.addAttribute("selector", Integer.toString(this.actionSelector));
        return element;
    }


    /**
     * @see de.ailis.wlandsuite.rawgame.parts.Part#write(java.io.OutputStream, int)
     */
    
    public void write(OutputStream stream, int offset) throws IOException
    {
        BitOutputStreamWrapper bitStream;
        int b;

        bitStream = new BitOutputStreamWrapper(stream);
        b = this.relative ? 0x80 : 0;
        b |= this.confirm ? 0x40 : 0;
        b |= this.message & 0x3f;
        bitStream.writeByte(b);
        
        bitStream.writeSignedByte(this.targetX);
        bitStream.writeSignedByte(this.targetY);
        bitStream.writeByte(this.targetMap);
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
     * Returns the confirm flag.
     *
     * @return The confirm flag
     */
    
    public boolean isConfirm()
    {
        return this.confirm;
    }


    /**
     * Sets the confirm flag.
     *
     * @param confirm 
     *            The confirm flag to set
     */
    
    public void setConfirm(boolean confirm)
    {
        this.confirm = confirm;
    }


    /**
     * Returns the relative flag.
     *
     * @return The relative flag
     */
    
    public boolean isRelative()
    {
        return this.relative;
    }


    /**
     * Sets the relative flag.
     *
     * @param relative 
     *            The relative flag to set
     */
    
    public void setRelative(boolean relative)
    {
        this.relative = relative;
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
     * Returns the target map.
     *
     * @return The target map
     */
    
    public int getTargetMap()
    {
        return this.targetMap;
    }


    /**
     * Sets the target map.
     *
     * @param targetMap 
     *            The target map to set
     */
    
    public void setTargetMap(int targetMap)
    {
        this.targetMap = targetMap;
    }


    /**
     * Returns the target X position.
     *
     * @return The target X position
     */
    
    public int getTargetX()
    {
        return this.targetX;
    }


    /**
     * Sets the target X position.
     *
     * @param targetX 
     *            The target X postion to set
     */
    
    public void setTargetX(int targetX)
    {
        this.targetX = targetX;
    }


    /**
     * Returns the target Y position.
     *
     * @return The target Y position
     */
    
    public int getTargetY()
    {
        return this.targetY;
    }


    /**
     * Sets the target Y position.
     *
     * @param targetY 
     *            The target Y position to set
     */
    
    public void setTargetY(int targetY)
    {
        this.targetY = targetY;
    }
}
