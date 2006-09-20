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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

import de.ailis.wlandsuite.io.SeekableInputStream;
import de.ailis.wlandsuite.io.SeekableOutputStream;
import de.ailis.wlandsuite.utils.XMLUtils;


/**
 * A container for actions.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Actions
{
    /** The actions */
    private List<Action> actions;


    /**
     * Constructor
     */

    public Actions()
    {
        this.actions = new ArrayList<Action>();
    }


    /**
     * Creates and returns a new Actions object.
     * 
     * @param actionClass
     *            The action class
     * @param stream
     *            The input stream
     * @param specialActionTable
     *            The special action table
     * @return The Actions object
     * @throws IOException
     */

    public static Actions read(int actionClass, SeekableInputStream stream,
        SpecialActionTable specialActionTable) throws IOException
    {
        Action action;
        int quantity;
        List<Integer> offsets;
        int endOffset;
        int pos;
        int offset;
        Actions actions;

        // Create the actions object
        actions = new Actions();

        // Read the action offsets
        pos = (int) stream.tell();
        offsets = new ArrayList<Integer>();
        endOffset = 0;
        while (pos != endOffset)
        {
            offset = stream.readWord();
            offsets.add(offset);
            pos += 2;
            if (offset != 0 && (endOffset == 0 || offset < endOffset))
            {
                endOffset = offset;
            }
        }

        // Count the actions
        quantity = offsets.size();

        // Read the actions
        actions.actions = new ArrayList<Action>(quantity);
        for (int i = 0; i < quantity; i++)
        {
            int actionOffset = offsets.get(i).intValue();
            if (actionOffset != 0)
            {
                stream.seek(actionOffset);
                action = ActionFactory.read(actionClass, stream,
                    specialActionTable);
            }
            else
            {
                action = null;
            }
            actions.actions.add(action);
        }

        // Return the actions
        return actions;
    }


    /**
     * Writes the actions to the specified output stream.
     * 
     * @param stream
     *            The output stream
     * @param specialActionTable
     *            The special action table
     * @throws IOException
     */

    public void write(SeekableOutputStream stream,
        SpecialActionTable specialActionTable) throws IOException
    {
        List<Integer> offsets;
        ByteArrayOutputStream byteStream;
        SeekableOutputStream actionStream;
        int startOffset;

        // Write the actions into a temporary buffer and calculate the offsets
        byteStream = new ByteArrayOutputStream();
        offsets = new ArrayList<Integer>(this.actions.size());
        startOffset = (int) stream.tell() + this.actions.size() * 2;
        for (Action action: this.actions)
        {
            if (action == null)
            {
                offsets.add(0);
                continue;
            }
            offsets.add(startOffset + byteStream.size());

            actionStream = new SeekableOutputStream(byteStream);
            action.write(actionStream, specialActionTable);
            actionStream.flush();
        }

        // Write the offset table
        for (Integer offset: offsets)
        {
            stream.writeWord(offset);
        }

        // Write the actions
        stream.write(byteStream.toByteArray());
    }


    /**
     * Returns the actions as XML.
     * 
     * @param actionClass
     *            The action class (1-15)
     * @return The actions as XML
     */

    public Element toXml(int actionClass)
    {
        Element element;
        int id;

        element = XMLUtils.createElement("actions");
        element.addAttribute("actionClass", Integer.toString(actionClass));
        id = 0;
        for (Action action: this.actions)
        {
            if (action != null)
            {
                element.add(action.toXml(id));
            }
            id++;
        }
        return element;
    }


    /**
     * Creates a new Actions object by reading its data from the specified XML
     * element.
     * 
     * @param element
     *            The XML element
     * @return The new Actions object
     */

    public static Actions read(Element element)
    {
        int actionClass;
        Actions actions;

        actions = new Actions();

        actionClass = Integer.parseInt(element.attributeValue("actionClass"));
        for (Object item: element.elements())
        {
            Element subElement = (Element) item;
            int id;

            id = Integer.parseInt(subElement.attributeValue("id"));
            while (id >= actions.actions.size())
            {
                actions.actions.add(null);
            }
            actions.actions
                .set(id, ActionFactory.read(actionClass, subElement));
        }

        return actions;
    }


    /**
     * Returns the number of actions.
     * 
     * @return The number of actions
     */

    public int countActions()
    {
        return this.actions.size();
    }


    /**
     * Returns the action with the specified index.
     * 
     * @param index
     *            The index
     * @return The action
     */
    
    public Action getAction(int index)
    {
        return this.actions.get(index);
    }
}
