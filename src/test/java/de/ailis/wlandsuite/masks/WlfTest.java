/*
 * $Id:WlfTest.java 81 2006-09-02 12:10:44Z k $
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

package de.ailis.wlandsuite.masks;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import junit.framework.Test;
import junit.framework.TestSuite;
import de.ailis.wlandsuite.masks.Masks;
import de.ailis.wlandsuite.masks.Mask;
import de.ailis.wlandsuite.test.WSTestCase;


/**
 * Tests the Wlf class
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision:81 $
 */

public class WlfTest extends WSTestCase
{
    /**
     * Returns the test suite.
     * 
     * @return The test suite
     */

    public static Test suite()
    {
        return new TestSuite(WlfTest.class);
    }


    /**
     * Tests reading a WLF.
     * 
     * @throws IOException
     */

    public void testRead() throws IOException
    {
        Masks wlf;
        File file;
        List<Mask> images;

        file = new File("src/test/resources/wlf/test.wlf");
        wlf = Masks.read(new FileInputStream(file), 2);

        images = wlf.getMasks();
        assertNotNull(images);
        assertEquals(2, images.size());
        assertEquals(new File("src/test/resources/wlf/test/000.png"), images
            .get(0));
        assertEquals(new File("src/test/resources/wlf/test/001.png"), images
            .get(1));
    }


    /**
     * Tests writing a PIC to a stream.
     * 
     * @throws IOException
     */

    public void testWriteToStream() throws IOException
    {
        Masks wlf;
        List<Mask> masks;
        ByteArrayOutputStream stream;

        masks = new ArrayList<Mask>();
        masks.add(new Mask(ImageIO.read(new File(
            "src/test/resources/wlf/test/000.png"))));
        masks.add(new Mask(ImageIO.read(new File(
            "src/test/resources/wlf/test/001.png"))));
        wlf = new Masks(masks);
        stream = new ByteArrayOutputStream();
        wlf.write(stream);
        assertEquals(new File("src/test/resources/wlf/test.wlf"), stream
            .toByteArray());
    }
}
