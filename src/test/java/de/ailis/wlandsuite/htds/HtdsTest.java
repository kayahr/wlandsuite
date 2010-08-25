/*
 * $Id$
 * Copyright (c) 2006 Klaus Reimer
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

package de.ailis.wlandsuite.htds;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import junit.framework.Test;
import junit.framework.TestSuite;
import de.ailis.wlandsuite.pic.Pic;
import de.ailis.wlandsuite.test.WSTestCase;


/**
 * Tests the Htds class
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class HtdsTest extends WSTestCase
{
    /**
     * Returns the test suite.
     * 
     * @return The test suite
     */

    public static Test suite()
    {
        return new TestSuite(HtdsTest.class);
    }


    /**
     * Tests reading a HTDS
     * 
     * @throws IOException When file operation fails.
     */

    public void testRead() throws IOException
    {
        File file;
        Htds htds;
        HtdsTileset tileset;

        file = new File("src/test/resources/htds/test.htds");
        htds = Htds.read(new FileInputStream(file));
        assertNotNull(htds);
        assertEquals(3, htds.getTilesets().size());
        tileset = htds.getTilesets().get(0);
        assertEquals(2, tileset.getTiles().size());
        assertEquals(new File("src/test/resources/htds/test/000/000.png"),
            tileset.getTiles().get(0));
        assertEquals(new File("src/test/resources/htds/test/000/001.png"),
            tileset.getTiles().get(1));
        tileset = htds.getTilesets().get(1);
        assertEquals(1, tileset.getTiles().size());
        assertEquals(new File("src/test/resources/htds/test/001/000.png"),
            tileset.getTiles().get(0));
        tileset = htds.getTilesets().get(2);
        assertEquals(3, tileset.getTiles().size());
        assertEquals(new File("src/test/resources/htds/test/002/000.png"),
            tileset.getTiles().get(0));
        assertEquals(new File("src/test/resources/htds/test/002/001.png"),
            tileset.getTiles().get(1));
        assertEquals(new File("src/test/resources/htds/test/002/002.png"),
            tileset.getTiles().get(2));
    }


    /**
     * Tests writing a HTDS.
     * 
     * @throws IOException When file operation fails.
     */

    public void testWrite() throws IOException
    {
        Htds htds;
        List<HtdsTileset> tilesets;
        List<Pic> tiles;
        ByteArrayOutputStream stream;

        tilesets = new ArrayList<HtdsTileset>();

        tiles = new ArrayList<Pic>();
        tiles.add(new Pic(ImageIO.read(new File(
            "src/test/resources/htds/test/000/000.png"))));
        tiles.add(new Pic(ImageIO.read(new File(
            "src/test/resources/htds/test/000/001.png"))));
        tilesets.add(new HtdsTileset(tiles));

        tiles = new ArrayList<Pic>();
        tiles.add(new Pic(ImageIO.read(new File(
            "src/test/resources/htds/test/001/000.png"))));
        tilesets.add(new HtdsTileset(tiles));

        tiles = new ArrayList<Pic>();
        tiles.add(new Pic(ImageIO.read(new File(
            "src/test/resources/htds/test/002/000.png"))));
        tiles.add(new Pic(ImageIO.read(new File(
            "src/test/resources/htds/test/002/001.png"))));
        tiles.add(new Pic(ImageIO.read(new File(
            "src/test/resources/htds/test/002/002.png"))));
        tilesets.add(new HtdsTileset(tiles));


        htds = new Htds(tilesets);
        stream = new ByteArrayOutputStream();
        htds.write(stream);

        assertEquals(new File("src/test/resources/htds/test.htds"), stream
            .toByteArray());
    }
}
