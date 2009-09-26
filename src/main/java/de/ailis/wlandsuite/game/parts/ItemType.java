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

import de.ailis.wlandsuite.common.exceptions.GameException;


/**
 * The item type.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public enum ItemType
{
    /** Melee weapon */
    MELEE_WEAPON(1),

    /** Short range single shot weapon */
    SHORT_RANGE_WEAPON(2),

    /** Medium range single shot weapon */
    MEDIUM_RANGE_WEAPON(3),

    /** Long range single shot weapon */
    LONG_RANGE_WEAPON(4),

    /** Short range burst/auto weapon */
    SHORT_RANGE_AUTO_WEAPON(5),

    /** Medium range burst/auto weapon */
    MEDIUM_RANGE_AUTO_WEAPON(6),

    /** Long range burst/auto weapon */
    LONG_RANGE_AUTO_WEAPON(7),

    /** Medium range rocket */
    MEDIUM_RANGE_ROCKET_WEAPON(8),

    /** Long range rocket */
    LONG_RANGE_ROCKET_WEAPON(9),

    /** Short range energy weapon */
    SHORT_RANGE_ENERGY_WEAPON(10),

    /** Medium range energy weapon */
    MEDIUM_RANGE_ENERGY_WEAPON(11),

    /** Long range energy weapon */
    LONG_RANGE_ENERGY_WEAPON(12),

    /** Explosive weapon */
    EXPLOSIVE_WEAPON(13),
    
    /** Ammunition */
    AMMO(14),
    
    /** Armor */
    ARMOR(15),
    
    /** Standard item (Like ropes, books, ...) */
    STANDARD(16),
    
    /** Special items (Jewelry, Fruit, Claypot) */
    SPECIAL(17),
    
    /** Quest items (Keys, Android parts, ...) */
    QUEST(18);     
       

    /** The ID of the item type */
    private int id;


    /**
     * Constructor.
     * 
     * @param id
     *            The item type id
     */

    private ItemType(final int id)
    {
        this.id = id;
    }


    /**
     * Returns the ID of the item type.
     * 
     * @return The ID of the item type
     */

    public int getId()
    {
        return this.id;
    }


    /**
     * Returns the item type for the specified id. If Id is not a valid item
     * type then an exception is thrown.
     * 
     * @param id
     *            The item type id
     * @return The item type. Never null
     */

    public static ItemType valueOf(final int id)
    {
        for (final ItemType type: ItemType.values())
        {
            if (type.getId() == id) return type;
        }
        throw new GameException("Not a valid item type: " + id);
    }
}
