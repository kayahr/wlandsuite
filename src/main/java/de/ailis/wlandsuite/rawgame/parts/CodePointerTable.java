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

import de.ailis.wlandsuite.utils.XMLUtils;
import org.dom4j.Element;

import de.ailis.wlandsuite.io.BitInputStreamWrapper;
import de.ailis.wlandsuite.io.BitOutputStreamWrapper;
import de.ailis.wlandsuite.rawgame.GameException;


/**
 * Code pointer table
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class CodePointerTable extends AbstractPart
{
    /** The action classes which uses this code pointer table */
    private List<Integer> actionClasses;

    /** The code pointers */
    private List<Integer> codePointers;


    /**
     * Constructor
     * 
     * @param bytes
     *            The game block data
     * @param offset
     *            The offset of the part in the game block
     * @param actionClass
     *            The action class using this pointer table
     */

    public CodePointerTable(byte[] bytes, int offset, int actionClass)
    {
        BitInputStreamWrapper bitStream;
        int firstOffset;
        int currentOffset;

        this.actionClasses = new ArrayList<Integer>();
        this.actionClasses.add(actionClass);

        try
        {
            bitStream = new BitInputStreamWrapper(new ByteArrayInputStream(bytes,
                offset, bytes.length - offset));
            this.codePointers = new ArrayList<Integer>();
            firstOffset = -1;
            currentOffset = offset;
            this.offset = offset;
            while (firstOffset < 0 || currentOffset < firstOffset)
            {
                offset = bitStream.readWord();
                if (offset != 0 && (firstOffset == -1 || offset < firstOffset))
                {
                    firstOffset = offset;
                }
                this.codePointers.add(offset);
                currentOffset += 2;
            }            
            this.size = this.codePointers.size() * 2;
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
    public CodePointerTable(Element element)
    {
        super();
        
        int[] codePointers;
        
        codePointers = new int[element.elements().size()];
        this.actionClasses = new ArrayList<Integer>();
        for (String actionClass: element.attributeValue("actionClasses").split(
            ","))
        {
            this.actionClasses.add(Integer.valueOf(actionClass));
        }
        for (Element subElement: (List<Element>) element.elements())
        {
            int codeId;
            int offset;

            codeId = Integer.parseInt(subElement.attributeValue("codeId"));
            offset = Integer.parseInt(subElement.attributeValue("offset"));
            codePointers[codeId] = offset;
        }
        
        this.codePointers = new ArrayList<Integer>();
        for (int i: codePointers)
        {
            this.codePointers.add(i);
        }
    }


    /**
     * @see de.ailis.wlandsuite.rawgame.parts.Part#toXml()
     */

    public Element toXml()
    {
        Element element, subElement;
        StringBuilder builder;

        element = XMLUtils.createElement("codePointers");

        builder = new StringBuilder();
        for (int i = 0, max = this.actionClasses.size(); i < max; i++)
        {
            if (i > 0)
            {
                builder.append(",");
            }
            builder.append(Integer.toString(this.actionClasses.get(i)));
        }
        element.addAttribute("actionClasses", builder.toString());

        for (int i = 0; i < this.codePointers.size(); i++)
        {
            subElement = XMLUtils.createElement("codePointer");
            subElement.addAttribute("codeId", Integer.toString(i));
            subElement.addAttribute("offset", Integer.toString(this.codePointers.get(i)));
            element.add(subElement);
        }

        return element;
    }


    /**
     * @see de.ailis.wlandsuite.rawgame.parts.Part#write(java.io.OutputStream, int)
     */
    
    public void write(OutputStream stream, int blockOffset) throws IOException
    {
        BitOutputStreamWrapper bitStream;

        bitStream = new BitOutputStreamWrapper(stream);
        for (int offset: this.codePointers)
        {
            bitStream.writeWord(offset);
        }
        bitStream.flush();
    }


    /**
     * Returns the code pointer.
     * 
     * @param codeId
     *            The code id
     * @return The offset
     */

    public int getCodePointer(int codeId)
    {
        return this.codePointers.get(codeId);
    }


    /**
     * Sets a code pointer
     * 
     * @param codeId
     *            The code id
     * @param offset
     *            The offset
     */

    public void setCodePointer(byte codeId, int offset)
    {
        this.codePointers.set(codeId, offset);
    }


    /**
     * Returns the number of code pointers in this table.
     * 
     * @return The number of code pointers
     */

    public int getCodePointers()
    {
        return this.codePointers.size();
    }


    /**
     * Returns the action classes.
     * 
     * @return The action classes
     */

    public Integer[] getActionClasses()
    {
        return this.actionClasses.toArray(new Integer[0]);
    }
    
    
    /**
     * Adds an action class.
     *
     * @param actionClass The action class to add
     */
    
    public void addActionClass(int actionClass)
    {
        this.actionClasses.add(actionClass);
    }
}
