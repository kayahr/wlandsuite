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

package de.ailis.wlandsuite.cpa;

import java.awt.image.BufferedImage;
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
 * Tests the Cpa class
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class CpaTest extends WSTestCase
{
    /**
     * Returns the test suite.
     * 
     * @return The test suite
     */

    public static Test suite()
    {
        return new TestSuite(CpaTest.class);
    }


    /**
     * Tests reading a CPA
     * 
     * @throws IOException When file operation fails.
     */

    public void testRead() throws IOException
    {
        File file;
        Cpa cpa;
        BufferedImage image;

        file = new File("src/test/resources/cpa/test.cpa");
        cpa = Cpa.read(new FileInputStream(file));
        image = cpa.getBaseFrame();
        assertEquals(new File("src/test/resources/cpa/test/000.png"), image);
        assertEquals(15, cpa.getFrames().size());
        assertEquals(5, cpa.getFrames().get(0).getDelay());
        image = cpa.getFrames().get(0).getPic();
        assertEquals(new File("src/test/resources/cpa/test/001.png"), image);
    }


    /**
     * Tests writing a CPA.
     * 
     * @throws IOException When file operation fails.
     */

    public void testWrite() throws IOException
    {
        Cpa cpa;
        Pic baseFrame;
        List<CpaFrame> frames;
        ByteArrayOutputStream stream;

        baseFrame = new Pic(ImageIO.read(new File(
            "src/test/resources/cpa/test/000.png")));
        frames = new ArrayList<CpaFrame>();
        for (int i = 1; i <= 15; i++)
        {
            frames.add(new CpaFrame(5, new Pic(ImageIO.read(new File(String
                .format("src/test/resources/cpa/test/%03d.png",
                    new Object[] { i }))))));
        }
        cpa = new Cpa(baseFrame, frames);

        stream = new ByteArrayOutputStream();
        cpa.write(stream);

        assertEquals(new File("src/test/resources/cpa/test.cpa"), stream
            .toByteArray());
    }
}
