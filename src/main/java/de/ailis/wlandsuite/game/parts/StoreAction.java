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
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

import de.ailis.wlandsuite.io.SeekableOutputStream;
import de.ailis.wlandsuite.utils.StringUtils;
import de.ailis.wlandsuite.utils.XmlUtils;


/**
 * The store data used in the Special Building Actions.
 *
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class StoreAction implements Action
{
    /** The new action class to set when leaving the store */
    private int newActionClass;

    /** The new action to set when leaving the store */
    private int newAction;

    /** The buy factor */
    private int buyFactor;

    /** The sell factor */
    private int sellFactor;

    /** The welcome string */
    private int message;

    /** The item list to use */
    private int itemList;

    /** The doctor name */
    private String name;

    /** The item types this shop buys */
    private int[] itemTypes;


    /**
     * Creates and returns a new Store object by reading its data from the
     * specified stream.
     *
     * @param stream
     *            The input stream
     * @return The Store
     * @throws IOException
     *             When file operation fails.
     */

    public static StoreAction read(final InputStream stream) throws IOException
    {
        StoreAction store;
        byte[] bytes;
        int p, b;
        List<Integer> itemTypes;

        store = new StoreAction();

        store.newActionClass = stream.read();
        store.newAction = stream.read();
        store.buyFactor = stream.read();
        store.sellFactor = stream.read();
        store.message = stream.read();
        store.itemList = stream.read();

        // Read the name
        bytes = new byte[13];
        stream.read(bytes);
        p = 0;
        while (bytes[p] != 0 && p < 13)
            p++;
        store.name = new String(bytes, 0, p, "ASCII");

        // Read the item types
        b = stream.read();
        itemTypes = new ArrayList<Integer>();
        while (b != 255)
        {
            itemTypes.add(b);
            b = stream.read();
        }
        final int max = itemTypes.size();
        store.itemTypes = new int[max];
        for (int i = 0; i < max; i++)
        {
            store.itemTypes[i] = itemTypes.get(i);
        }

        return store;
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.Action#write(de.ailis.wlandsuite.io.SeekableOutputStream,
     *      de.ailis.wlandsuite.game.parts.SpecialActionTable)
     */

    @Override
    public void write(final SeekableOutputStream stream,
        final SpecialActionTable specialActionTable) throws IOException
    {
        stream.write(0x81);

        stream.write(this.newActionClass);
        stream.write(this.newAction);
        stream.write(this.buyFactor);
        stream.write(this.sellFactor);
        stream.write(this.message);
        stream.write(this.itemList);

        stream.write(this.name.getBytes("ASCII"), 0, Math.min(this.name
            .length(), 13));
        for (int i = this.name.length(); i < 13; i++)
        {
            stream.write(0);
        }

        for (final int itemType: this.itemTypes)
        {
            stream.write(itemType);
        }
        stream.write(255);
    }


    /**
     * Returns the store data as XML.
     *
     * @param id
     *            The action id
     * @return The store data as XML
     */

    @Override
    public Element toXml(final int id)
    {
        Element element;

        element = XmlUtils.createElement("store");

        element.addAttribute("id", StringUtils.toHex(id));
        if (this.name != null && this.name.length() > 0)
        {
            element.addAttribute("name", this.name);
        }
        if (this.message != 0)
        {
            element.addAttribute("message", Integer.toString(this.message));
        }
        if (this.buyFactor != 0)
        {
            element.addAttribute("buyFactor", Integer.toString(this.buyFactor));
        }
        if (this.sellFactor != 0)
        {
            element.addAttribute("sellFactor", Integer.toString(this.sellFactor));
        }
        if (this.itemList != 0)
        {
            element.addAttribute("itemList", Integer.toString(this.itemList));
        }
        if (this.newActionClass != 255)
        {
            element.addAttribute("newActionClass", StringUtils.toHex(this.newActionClass));
        }
        if (this.newActionClass != 255)
        {
            element.addAttribute("newAction", StringUtils.toHex(this.newAction));
        }

        for (final int itemType: this.itemTypes)
        {
            Element subElement;

            subElement = XmlUtils.createElement("itemType");
            subElement.addText(Integer.toString(itemType));

            element.add(subElement);
        }
        return element;
    }


    /**
     * Creates and returns a new Store object by reading its data from XML.
     *
     * @param element
     *            The XML element
     * @return The store data
     */

    public static StoreAction read(final Element element)
    {
        StoreAction store;

        store = new StoreAction();
        store.name = element.attributeValue("name", "");
        store.message = StringUtils.toInt(element.attributeValue("message", "0"));
        store.buyFactor = StringUtils.toInt(element.attributeValue("buyFactor", "0"));
        store.sellFactor = StringUtils.toInt(element
            .attributeValue("sellFactor", "1"));
        store.itemList = StringUtils.toInt(element.attributeValue("itemList", "0"));
        store.newActionClass = StringUtils.toInt(element
            .attributeValue("newActionClass", "255"));
        store.newAction = StringUtils.toInt(element.attributeValue("newAction", "255"));

        final List<?> elements = element.elements("itemType");
        store.itemTypes = new int[elements.size()];
        int i = 0;
        for (final Object item: elements)
        {
            final Element subElement = (Element) item;
            store.itemTypes[i] = StringUtils.toInt(subElement.getText());
            i++;
        }

        return store;
    }


    /**
     * Returns the buyFactor.
     *
     * @return The buyFactor
     */

    public int getBuyFactor()
    {
        return this.buyFactor;
    }


    /**
     * Sets the buyFactor.
     *
     * @param buyFactor
     *            The buyFactor to set
     */

    public void setBuyFactor(final int buyFactor)
    {
        this.buyFactor = buyFactor;
    }


    /**
     * Returns the itemList.
     *
     * @return The itemList
     */

    public int getItemList()
    {
        return this.itemList;
    }


    /**
     * Sets the itemList.
     *
     * @param itemList
     *            The itemList to set
     */

    public void setItemList(final int itemList)
    {
        this.itemList = itemList;
    }


    /**
     * Returns the itemTypes.
     *
     * @return The itemTypes
     */

    public int[] getItemTypes()
    {
        return this.itemTypes;
    }


    /**
     * Sets the itemTypes.
     *
     * @param itemTypes
     *            The itemTypes to set
     */

    public void setItemTypes(final int[] itemTypes)
    {
        this.itemTypes = itemTypes;
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

    public void setMessage(final int message)
    {
        this.message = message;
    }


    /**
     * Returns the name.
     *
     * @return The name
     */

    public String getName()
    {
        return this.name;
    }


    /**
     * Sets the name.
     *
     * @param name
     *            The name to set
     */

    public void setName(final String name)
    {
        this.name = name;
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
     * Returns the sellFactor.
     *
     * @return The sellFactor
     */

    public int getSellFactor()
    {
        return this.sellFactor;
    }


    /**
     * Sets the sellFactor.
     *
     * @param sellFactor
     *            The sellFactor to set
     */

    public void setSellFactor(final int sellFactor)
    {
        this.sellFactor = sellFactor;
    }
}
