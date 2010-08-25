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

package de.ailis.wlandsuite.pic;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import junit.framework.Test;
import junit.framework.TestSuite;
import de.ailis.wlandsuite.test.WSTestCase;


/**
 * Tests the Pic class
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class PicTest extends WSTestCase
{
    /**
     * Returns the test suite.
     * 
     * @return The test suite
     */

    public static Test suite()
    {
        return new TestSuite(PicTest.class);
    }


    /**
     * Tests reading a PIC
     * 
     * @throws IOException When file operation fails.
     */

    public void testRead() throws IOException
    {
        File file;
        Pic pic;

        file = new File("src/test/resources/pic/test.pic");
        pic = Pic.read(new FileInputStream(file), 288, 128);
        assertNotNull(pic);
        assertEquals(new File("src/test/resources/pic/test.png"), pic);
    }


    /**
     * Tests writing a PIC.
     * 
     * @throws IOException When file operation fails.
     */

    public void testWrite() throws IOException
    {
        Pic pic;
        ByteArrayOutputStream stream;

        pic = new Pic(ImageIO.read(new File("src/test/resources/pic/test.png")));
        stream = new ByteArrayOutputStream();
        pic.write(stream);

        assertEquals(new File("src/test/resources/pic/test.pic"), stream
            .toByteArray());
    }
}
