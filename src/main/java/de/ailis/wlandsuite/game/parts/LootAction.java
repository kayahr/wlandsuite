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
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

import de.ailis.wlandsuite.io.SeekableInputStream;
import de.ailis.wlandsuite.io.SeekableOutputStream;
import de.ailis.wlandsuite.utils.StringUtils;
import de.ailis.wlandsuite.utils.XmlUtils;


/**
 * The loot action defines a loot back and a new action to set the square to
 * when all items from the loot bag have been retrieved.
 *
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class LootAction implements Action
{
    /** The new action class to set */
    private int newActionClass;

    /** The new action to set */
    private int newAction;

    /** The loot item */
    private final List<LootItem> items = new ArrayList<LootItem>();


    /**
     * Creates and returns a new Loot Action by reading its data from the
     * specified stream.
     *
     * @param stream
     *            The input stream
     * @return The new Loot Action
     * @throws IOException
     *             When file operation fails.
     */

    public static LootAction read(final SeekableInputStream stream)
        throws IOException
    {
        LootAction action;
        LootItem item;

        action = new LootAction();

        action.newActionClass = stream.readByte();
        action.newAction = stream.readByte();

        while ((item = LootItem.read(stream)) != null)
        {
            action.items.add(item);
        }

        return action;
    }


    /**
     * Creates and returns a Loot Action by reading its data from XML.
     *
     * @param element
     *            The XML element
     * @return The Loot Action
     */

    public static LootAction read(final Element element)
    {
        LootAction action;

        action = new LootAction();

        action.newActionClass = StringUtils.toInt(element
            .attributeValue("newActionClass"));
        action.newAction = StringUtils.toInt(element
            .attributeValue("newAction"));

        // Read the items
        for (final Object item: element.elements())
        {
            Element subElement;

            subElement = (Element) item;
            action.items.add(LootItem.read(subElement));
        }

        // Return the check action
        return action;
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.Action#toXml(int)
     */

    @Override
    public Element toXml(final int id)
    {
        Element element;

        element = XmlUtils.createElement("loot");
        element.addAttribute("id", StringUtils.toHex(id));
        element.addAttribute("newActionClass", StringUtils.toHex(this.newActionClass));
        element.addAttribute("newAction", StringUtils.toHex(this.newAction));

        for (final LootItem item: this.items)
        {
            element.add(item.toXml());
        }

        return element;
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.Action#write(de.ailis.wlandsuite.io.SeekableOutputStream,
     *      de.ailis.wlandsuite.game.parts.SpecialActionTable)
     */

    @Override
    public void write(final SeekableOutputStream stream,
        final SpecialActionTable specialActionTable)
    {
        stream.write(this.newActionClass);
        stream.write(this.newAction);

        for (final LootItem item: this.items)
        {
            item.write(stream);
        }
        stream.write(255);
    }


    /**
     * Returns the newAction.
     *
     * @return The newAction
     */

    public int getNewAction()
    {
        return this.newAction;
    }


    /**
     * Sets the newAction.
     *
     * @param newAction
     *            The newAction to set
     */

    public void setNewAction(final int newAction)
    {
        this.newAction = newAction;
    }


    /**
     * Returns the newActionClass.
     *
     * @return The newActionClass
     */

    public int getNewActionClass()
    {
        return this.newActionClass;
    }


    /**
     * Sets the newActionClass.
     *
     * @param newActionClass
     *            The newActionClass to set
     */

    public void setNewActionClass(final int newActionClass)
    {
        this.newActionClass = newActionClass;
    }


    /**
     * Adds a new item
     *
     * @param item
     *            The item to add
     */

    public void addItem(final LootItem item)
    {
        this.items.add(item);
    }


    /**
     * Returns the item with the specified index.
     *
     * @param index
     *            The index
     * @return The item
     */

    public LootItem getItem(final int index)
    {
        return this.items.get(index);
    }


    /**
     * Sets a item at a specific index.
     *
     * @param index
     *            The index
     * @param item
     *            The item
     */

    public void setItem(final int index, final LootItem item)
    {
        this.items.set(index, item);
    }


    /**
     * Deletes all items
     */

    public void clearItems()
    {
        this.items.clear();
    }


    /**
     * Removes the item at the specified index.
     *
     * @param index
     *            The index
     */

    public void removeItem(final int index)
    {
        this.items.remove(index);
    }


    /**
     * Returns the number of items.
     *
     * @return The number of items
     */

    public int countItems()
    {
        return this.items.size();
    }


    /**
     * Inserts a item at a specific index.
     *
     * @param index
     *            The index
     * @param item
     *            The item
     */

    public void addItem(final int index, final LootItem item)
    {
        this.items.add(index, item);
    }
}
