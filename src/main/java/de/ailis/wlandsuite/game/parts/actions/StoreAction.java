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

package de.ailis.wlandsuite.game.parts.actions;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.ailis.wlandsuite.game.parts.SpecialActionTable;
import de.ailis.wlandsuite.io.SeekableOutputStream;


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
     */

    public static StoreAction read(InputStream stream) throws IOException
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
        int max = itemTypes.size();
        store.itemTypes = new int[max];
        for (int i = 0; i < max; i++)
        {
            store.itemTypes[i] = itemTypes.get(i);
        }

        return store;
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.actions.Action#write(de.ailis.wlandsuite.io.SeekableOutputStream,
     *      de.ailis.wlandsuite.game.parts.SpecialActionTable)
     */

    public void write(SeekableOutputStream stream,
        SpecialActionTable specialActionTable) throws IOException
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

        for (int itemType: this.itemTypes)
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

    public Element toXml(int id)
    {
        Element element;

        element = DocumentHelper.createElement("store");

        element.addAttribute("id", Integer.toString(id));
        element.addAttribute("name", this.name);
        element.addAttribute("message", Integer.toString(this.message));
        element.addAttribute("buyFactor", Integer.toString(this.buyFactor));
        element.addAttribute("sellFactor", Integer.toString(this.sellFactor));
        element.addAttribute("itemList", Integer.toString(this.itemList));
        element.addAttribute("newActionClass", Integer
            .toString(this.newActionClass));
        element.addAttribute("newAction", Integer.toString(this.newAction));

        for (int itemType: this.itemTypes)
        {
            Element subElement;

            subElement = DocumentHelper.createElement("itemType");
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

    public static StoreAction read(Element element)
    {
        StoreAction store;

        store = new StoreAction();
        store.name = element.attributeValue("name");
        store.message = Integer.parseInt(element.attributeValue("message"));
        store.buyFactor = Integer.parseInt(element.attributeValue("buyFactor"));
        store.sellFactor = Integer.parseInt(element
            .attributeValue("sellFactor"));
        store.itemList = Integer.parseInt(element.attributeValue("itemList"));
        store.newActionClass = Integer.parseInt(element
            .attributeValue("newActionClass"));
        store.newAction = Integer.parseInt(element.attributeValue("newAction"));

        List<?> elements = element.elements("itemType");
        store.itemTypes = new int[elements.size()];
        int i = 0;
        for (Object item: elements)
        {
            Element subElement = (Element) item;
            store.itemTypes[i] = Integer.parseInt(subElement.getText());
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

    public void setBuyFactor(int buyFactor)
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

    public void setItemList(int itemList)
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

    public void setItemTypes(int[] itemTypes)
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

    public void setMessage(int message)
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

    public void setName(String name)
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

    public void setNewAction(int newAction)
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

    public void setNewActionClass(int newActionClass)
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

    public void setSellFactor(int sellFactor)
    {
        this.sellFactor = sellFactor;
    }
}
