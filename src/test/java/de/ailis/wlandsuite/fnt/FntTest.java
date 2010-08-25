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

package de.ailis.wlandsuite.fnt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import junit.framework.Test;
import junit.framework.TestSuite;
import de.ailis.wlandsuite.image.EgaImage;
import de.ailis.wlandsuite.test.WSTestCase;


/**
 * Tests the Fnt class
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class FntTest extends WSTestCase
{
    /**
     * Returns the test suite.
     * 
     * @return The test suite
     */

    public static Test suite()
    {
        return new TestSuite(FntTest.class);
    }


    /**
     * Tests reading a FNT
     * 
     * @throws IOException When file operation fails.
     */

    public void testRead() throws IOException
    {
        File file;
        Fnt fnt;

        file = new File("src/test/resources/fnt/test.fnt");
        fnt = Fnt.read(new FileInputStream(file), 3);
        assertNotNull(fnt);
        assertEquals(3, fnt.getChars().size());
        assertEquals(new File("src/test/resources/fnt/test/000.png"), fnt
            .getChars().get(0));
        assertEquals(new File("src/test/resources/fnt/test/001.png"), fnt
            .getChars().get(1));
        assertEquals(new File("src/test/resources/fnt/test/002.png"), fnt
            .getChars().get(2));
    }


    /**
     * Tests writing a FNT.
     * 
     * @throws IOException When file operation fails.
     */

    public void testWrite() throws IOException
    {
        Fnt fnt;
        List<FntChar> chars;
        ByteArrayOutputStream stream;
        EgaImage fntChar;

        chars = new ArrayList<FntChar>();
        fntChar = new EgaImage(ImageIO.read(new File(
            "src/test/resources/fnt/test/000.png")));
        chars.add(new FntChar(fntChar));
        fntChar = new EgaImage(ImageIO.read(new File(
            "src/test/resources/fnt/test/001.png")));
        chars.add(new FntChar(fntChar));
        fntChar = new EgaImage(ImageIO.read(new File(
            "src/test/resources/fnt/test/002.png")));
        chars.add(new FntChar(fntChar));

        fnt = new Fnt(chars);
        stream = new ByteArrayOutputStream();
        fnt.write(stream);

        assertEquals(new File("src/test/resources/fnt/test.fnt"), stream
            .toByteArray());
    }
}
