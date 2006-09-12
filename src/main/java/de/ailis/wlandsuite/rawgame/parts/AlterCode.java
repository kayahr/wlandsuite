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
import java.util.ArrayList;
import java.util.List;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.ailis.wlandsuite.io.BitInputStreamWrapper;
import de.ailis.wlandsuite.io.BitOutputStreamWrapper;
import de.ailis.wlandsuite.rawgame.GameException;


/**
 * Alter code
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class AlterCode extends AbstractPart
{
    /** The message to print when player enters the square */
    private int message; 
    
    /** The alterations */
    private List<Alteration> alterations = new ArrayList<Alteration>();
    
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

    public AlterCode(byte[] bytes, int offset)
    {
        int b;
        BitInputStreamWrapper bitStream;
        
        this.size = 0;
        this.offset = offset;
        try
        {
            bitStream = new BitInputStreamWrapper(new ByteArrayInputStream(bytes,
                offset, bytes.length - offset));

            // Read the message
            this.message = bitStream.readByte();
            this.size++;

            // Read alterations
            do
            {
                b = bitStream.readByte();
                this.size++;
                this.alterations.add(new Alteration(bitStream, b & 0x7f));
                this.size += 4;
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
    public AlterCode(Element element)
    {
        super();
        
        this.message = Integer.parseInt(element.attributeValue("message"));
        this.actionClass = Integer.parseInt(element.attributeValue("class", "255"));
        this.actionSelector = Integer.parseInt(element.attributeValue("selector", "255"));
        for (Element subElement: (List<Element>) element.elements("alteration"))
        {
            this.alterations.add(new Alteration(subElement));
        }
    }


    /**
     * @see de.ailis.wlandsuite.rawgame.parts.Part#toXml()
     */

    public Element toXml()
    {
        Element element;

        element = DocumentHelper.createElement("alter");
        element.addAttribute("offset", Integer.toString(this.offset));
        element.addAttribute("message", Integer.toString(this.message));
        element.addAttribute("class", Integer.toString(this.actionClass));
        element.addAttribute("selector", Integer.toString(this.actionSelector));
        for (Alteration alteration: this.alterations)
        {
            element.add(alteration.toXml());
        }
        
        return element;
    }


    /**
     * @see de.ailis.wlandsuite.rawgame.parts.Part#write(java.io.OutputStream, int)
     */
    
    public void write(OutputStream stream, int offset) throws IOException
    {
        BitOutputStreamWrapper bitStream;

        bitStream = new BitOutputStreamWrapper(stream);
        bitStream.writeByte(this.message);
        for (int i = 0; i < this.alterations.size(); i++)
        {
            this.alterations.get(i).write(bitStream, i == this.alterations.size() - 1);
        }
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
     * Returns the alterations.
     *
     * @return The alterations
     */
    
    public List<Alteration> getAlterations()
    {
        return this.alterations;
    }


    /**
     * Sets the alterations.
     *
     * @param alterations 
     *            The alterations to set
     */
    
    public void setAlterations(List<Alteration> alterations)
    {
        this.alterations = alterations;
    }


    /**
     * Returns the message.
     *
     * @return The message
     */
    
    public int getMessage()
    {
        return this.message;
    }


    /**
     * Sets the message.
     *
     * @param message 
     *            The message to set
     */
    
    public void setMessage(int message)
    {
        this.message = message;
    }
}
