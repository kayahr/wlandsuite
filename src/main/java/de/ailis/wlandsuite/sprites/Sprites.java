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

package de.ailis.wlandsuite.sprites;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * A Sprites object contains the sprites from Wasteland's ic0_9.wlf file.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Sprites implements Serializable
{
    /** Serial version UID */
    private static final long serialVersionUID = -2736830301271481828L;
    
    /** The sprites */
    private List<Sprite> sprites;


    /**
     * Private constructor
     */

    private Sprites()
    {
        this.sprites = new ArrayList<Sprite>();
    }


    /**
     * Constructor for building new sprites.
     * 
     * @param cursors
     *            The cursors
     */

    public Sprites(List<Sprite> cursors)
    {
        this.sprites = cursors;
    }


    /**
     * Calculates and returns the number of sprites for the given file size,
     * cursor width and height
     * 
     * @param size
     *            The file size
     * @param width
     *            The sprite width
     * @param height
     *            The sprite height
     * @return The number of cursors
     */

    public static int getQuantity(long size, int width, int height)
    {
        return (int) (size / (height * (width / 2)));
    }


    /**
     * Loads sprites from a stream. A sprite size of 16x16 and a quantity of 10
     * is assumed.
     * 
     * @param stream
     *            The input stream
     * @return The sprites
     * @throws IOException
     */

    public static Sprites read(InputStream stream) throws IOException
    {
        return read(stream, 16, 16, 10);
    }


    /**
     * Loads sprites from a stream. A sprite size of 16x16 is assumed.
     * 
     * @param stream
     *            The input stream
     * @param quantity
     *            The number of sprites to read
     * @return The sprites
     * @throws IOException
     */

    public static Sprites read(InputStream stream, int quantity)
        throws IOException
    {
        return read(stream, 16, 16, quantity);
    }


    /**
     * Loads sprites from a stream.
     * 
     * @param stream
     *            The input stream
     * @param width
     *            The width of a sprite
     * @param height
     *            The height of a sprite
     * @param quantity
     *            The number of sprites
     * @return The sprites
     * @throws IOException
     */

    public static Sprites read(InputStream stream, int width, int height,
        int quantity) throws IOException
    {
        Sprites sprites;

        // Create the sprites object
        sprites = new Sprites();

        // Read the sprites
        for (int i = 0; i < quantity; i++)
        {
            sprites.sprites.add(Sprite.read(stream, width, height));
        }

        // Return the sprites
        return sprites;
    }


    /**
     * Writes sprites to a stream.
     * 
     * @param stream
     *            The output stream
     * @throws IOException
     */

    public void write(OutputStream stream) throws IOException
    {
        // Read the cursors
        for (Sprite sprite: this.sprites)
        {
            sprite.write(stream);
        }
    }


    /**
     * Returns the sprites.
     * 
     * @return The sprites
     */

    public List<Sprite> getSprites()
    {
        return this.sprites;
    }
}
