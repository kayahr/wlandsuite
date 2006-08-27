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
import java.util.ArrayList;
import java.util.List;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.ailis.wlandsuite.game.GameException;
import de.ailis.wlandsuite.io.BitInputStreamWrapper;
import de.ailis.wlandsuite.io.BitOutputStreamWrapper;


/**
 * Transition code
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class SimpleCode extends AbstractPart
{
    /** The messages to print*/
    private List<Integer> messages = new ArrayList<Integer>();
    
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

    public SimpleCode(byte[] bytes, int offset)
    {
        int b;
        BitInputStreamWrapper bitStream;
        
        this.size = 0;
        this.offset = offset;
        try
        {
            bitStream = new BitInputStreamWrapper(new ByteArrayInputStream(bytes,
                offset, bytes.length - offset));

            // Read messages
            do
            {
                b = bitStream.readByte();
                this.size++;
                this.messages.add(b & 0x7f);
            }
            while ((b & 0x80) == 0);
            
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
    public SimpleCode(Element element)
    {
        super();
        
        String[] parts = element.attributeValue("messages", "0").split(",");
        for (String part: parts)
        {
            this.messages.add(Integer.valueOf(part.trim()));
        }
        this.actionClass = Integer.parseInt(element.attributeValue("class", "255"));
        this.actionSelector = Integer.parseInt(element.attributeValue("selector", "255"));
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.Part#toXml()
     */

    public Element toXml()
    {
        Element element;

        element = DocumentHelper.createElement("simple");
        element.addAttribute("offset", Integer.toString(this.offset));
        StringBuilder messages = new StringBuilder();
        messages.append(this.messages.get(0));
        for (int i = 1; i < this.messages.size(); i++)
        {
            messages.append(", ").append(this.messages.get(i));
        }
        element.addAttribute("messages", messages.toString());
        element.addAttribute("class", Integer.toString(this.actionClass));
        element.addAttribute("selector", Integer.toString(this.actionSelector));
        return element;
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.Part#write(java.io.OutputStream)
     */

    public void write(OutputStream stream) throws IOException
    {
        BitOutputStreamWrapper bitStream;

        bitStream = new BitOutputStreamWrapper(stream);
        for (int i = 0; i < this.messages.size() - 1; i++)
        {
            bitStream.writeByte(this.messages.get(i));
        }
        bitStream.writeByte(this.messages.get(this.messages.size() - 1).intValue() | 0x80);
        
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
     * Returns the messages.
     *
     * @return The messages
     */
    
    public List<Integer> getMessages()
    {
        return this.messages;
    }


    /**
     * Sets the messages.
     *
     * @param messages
     *            The messages to set
     */
    
    public void setMessages(List<Integer> messages)
    {
        this.messages = messages;
    }
}
