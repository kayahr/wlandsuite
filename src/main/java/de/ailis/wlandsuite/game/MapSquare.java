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

package de.ailis.wlandsuite.game;


/**
 * A map square. Defines the action class and the action which should be
 * executed on this square and also defines the tile which should be drawn.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class MapSquare
{
    /** The action class */
    private int actionClass;

    /** The action number */
    private int action;

    /** The tile number */
    private int tile;


    /**
     * Empty constructor. Constructs a map square with all infos set to 0.
     */

    public MapSquare()
    {
        // Empty
    }


    /**
     * Full Constructor.
     * 
     * @param actionClass
     *            The action class
     * @param action
     *            The action number
     * @param tile
     *            The tile number
     */

    public MapSquare(int actionClass, int action, int tile)
    {
        this.actionClass = actionClass;
        this.action = action;
        this.tile = tile;
    }


    /**
     * Returns the action class. The action class defines the type of the 
     * action which should be executed on this square.
     * 
     * @return The action class
     */

    public int getActionClass()
    {
        return this.actionClass;
    }


    /**
     * Sets the action class. The action class defines the type of the 
     * action which should be executed on this square.
     * 
     * @param actionClass
     *            The action class to set
     */

    public void setActionClass(int actionClass)
    {
        this.actionClass = actionClass;
    }


    /**
     * Returns the action number. The action number defines which action in  
     * the action class of this square should be executed on this square.
     * 
     * @return The action number
     */

    public int getAction()
    {
        return this.action;
    }


    /**
     * Sets the action number. The action number defines which action in 
     * the action class of this square should be executed on this square.    
     * 
     * @param action
     *            The action number to set
     */

    public void setAction(int action)
    {
        this.action = action;
    }


    /**
     * Returns the tile number. This is the tile which is drawn in this square.
     * The tile numbers 0-9 references the 10 sprites in ic0_9.wlf. All other
     * numbers (subtract 10 from it) references a tile in the selected tileset.
     * 
     * @return The tile number
     */

    public int getTile()
    {
        return this.tile;
    }


    /**
     * Sets the tile number. This is the tile which is drawn in this square. The
     * tile numbers 0-9 references the 10 sprites in ic0_9.wlf. All other
     * numbers (subtract 10 from it) references a tile in the selected tileset.
     * 
     * @param tile
     *            The tile number to set
     */

    public void setTile(int tile)
    {
        this.tile = tile;
    }
}
