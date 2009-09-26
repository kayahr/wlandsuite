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

import org.dom4j.Element;

import de.ailis.wlandsuite.common.exceptions.GameException;
import de.ailis.wlandsuite.io.SeekableInputStream;
import de.ailis.wlandsuite.io.SeekableOutputStream;
import de.ailis.wlandsuite.utils.StringUtils;
import de.ailis.wlandsuite.utils.XmlUtils;


/**
 * Shop item
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class ShopItem
{
    /** The price */
    private int price;

    /** The stock */
    private int stock;

    /** The item type */
    private ItemType type;

    /**
     * If weapon is a demolition weapon (Affects splash-damage on auto-miss
     * roles and playing a sound effect
     */
    private boolean demolition;

    /** The ammo capacity */
    private int capacity;

    /** The skill which is used for this item */
    private int skill;

    /** The base damage (1D6) */
    private int damage;

    /** The item used for ammunition */
    private int ammo;


    /**
     * Creates a new shop item by reading its data from the specified stream.
     * 
     * @param stream
     *            The input stream
     * @return The new shop item
     * @throws IOException
     */

    public static ShopItem read(final SeekableInputStream stream)
        throws IOException
    {
        ShopItem item;
        int b;

        item = new ShopItem();

        item.price = stream.readWord();
        item.stock = stream.read();
        b = stream.read();
        item.type = ItemType.valueOf(b >> 3);
        if ((b & 7) > 1) throw new GameException("Demolition flag is higher than 1");
        item.demolition = (b & 7) == 1;
        item.capacity = stream.read();
        item.skill = stream.read();
        item.damage = stream.read();
        item.ammo = stream.read();

        return item;
    }


    /**
     * Returns the XML representation of the shop item.
     * 
     * @param id
     *            The shop item id
     * @return The shop item
     */

    public Element toXml(final int id)
    {
        Element element;

        element = XmlUtils.createElement("shopItem");
        element.addAttribute("id", Integer.toString(id));
        element.addAttribute("price", Integer.toString(this.price));
        element.addAttribute("stock", Integer.toString(this.stock));
        element.addAttribute("type", Integer.toString(this.type.getId()));
        element.addAttribute("capacity", Integer.toString(this.capacity));
        element.addAttribute("skill", Integer.toString(this.skill));
        element.addAttribute("damage", Integer.toString(this.damage));
        element.addAttribute("ammo", Integer.toString(this.ammo));
        element.addAttribute("demolition", Boolean.toString(this.demolition));
        return element;
    }


    /**
     * Creates a new shop item by reading the data from XML.
     * 
     * @param element
     *            The XML element
     * @return The new shop item
     */

    public static ShopItem read(final Element element)
    {
        ShopItem item;

        item = new ShopItem();

        item.price = StringUtils.toInt(element.attributeValue("price", "0"));
        item.stock = StringUtils.toInt(element.attributeValue("stock", "0"));
        item.type = ItemType.valueOf(StringUtils.toInt(element
            .attributeValue("type", "0")));
        item.capacity = StringUtils.toInt(element.attributeValue("capacity",
            "0"));
        item.skill = StringUtils.toInt(element.attributeValue("skill", "0"));
        item.damage = StringUtils.toInt(element.attributeValue("damage", "0"));
        item.ammo = StringUtils.toInt(element.attributeValue("ammo", "0"));
        item.demolition = Boolean.parseBoolean(element.attributeValue(
            "demolition", "false"));

        return item;
    }


    /**
     * Writes the shop item to the specified output stream.
     * 
     * @param stream
     *            The output stream
     */

    public void write(final SeekableOutputStream stream)
    {
        stream.writeWord(this.price);
        stream.write(this.stock);
        stream.write((this.type.getId() << 3) | (this.demolition ? 1 : 0));
        stream.write(this.capacity);
        stream.write(this.skill);
        stream.write(this.damage);
        stream.write(this.ammo);
    }


    /**
     * Returns the ammo.
     * 
     * @return The ammo
     */

    public int getAmmo()
    {
        return this.ammo;
    }


    /**
     * Sets the ammo.
     * 
     * @param ammo
     *            The ammo to set
     */

    public void setAmmo(final int ammo)
    {
        this.ammo = ammo;
    }


    /**
     * Returns the capacity.
     * 
     * @return The capacity
     */

    public int getCapacity()
    {
        return this.capacity;
    }


    /**
     * Sets the capacity.
     * 
     * @param capacity
     *            The capacity to set
     */

    public void setCapacity(final int capacity)
    {
        this.capacity = capacity;
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

    public void setDamage(final int damage)
    {
        this.damage = damage;
    }


    /**
     * Returns the price.
     * 
     * @return The price
     */

    public int getPrice()
    {
        return this.price;
    }


    /**
     * Sets the price.
     * 
     * @param price
     *            The price to set
     */

    public void setPrice(final int price)
    {
        this.price = price;
    }


    /**
     * Returns the skill.
     * 
     * @return The skill
     */

    public int getSkill()
    {
        return this.skill;
    }


    /**
     * Sets the skill.
     * 
     * @param skill
     *            The skill to set
     */

    public void setSkill(final int skill)
    {
        this.skill = skill;
    }


    /**
     * Returns the stock.
     * 
     * @return The stock
     */

    public int getStock()
    {
        return this.stock;
    }


    /**
     * Sets the stock.
     * 
     * @param stock
     *            The stock to set
     */

    public void setStock(final int stock)
    {
        this.stock = stock;
    }


    /**
     * Returns the type.
     * 
     * @return The type
     */

    public ItemType getType()
    {
        return this.type;
    }


    /**
     * Sets the type.
     * 
     * @param type
     *            The type to set
     */

    public void setType(final ItemType type)
    {
        this.type = type;
    }


    /**
     * Returns true if item is a demolition weapon. This affects for example
     * the playing of a sound effect when the weapon is fired.
     * 
     * @return The demolition flag
     */

    public boolean isDemolution()
    {
        return this.demolition;
    }


    /**
     * Sets the demolition flag. This affects for example the playing of a
     * sound effect when the weapon is fired.
     * 
     * @param demolition
     *            The demolition flag to set
     */

    public void setDemolition(final boolean demolition)
    {
        this.demolition = demolition;
    }
}
