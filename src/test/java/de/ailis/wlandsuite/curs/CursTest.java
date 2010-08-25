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

package de.ailis.wlandsuite.curs;

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
 * Tests the Curs class
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class CursTest extends WSTestCase
{
    /**
     * Returns the test suite.
     * 
     * @return The test suite
     */

    public static Test suite()
    {
        return new TestSuite(CursTest.class);
    }


    /**
     * Tests reading a Curs
     * 
     * @throws IOException When file operation fails.
     */

    public void testRead() throws IOException
    {
        File file;
        Curs curs;

        file = new File("src/test/resources/curs/test.curs");
        curs = Curs.read(new FileInputStream(file), 2);
        assertNotNull(curs);
        assertEquals(2, curs.getCursors().size());
        assertEquals(new File("src/test/resources/curs/test/000.png"), curs
            .getCursors().get(0).getCursor());
        assertEquals(new File("src/test/resources/curs/test/000_mask.png"), curs
            .getCursors().get(0).getMask());
        assertEquals(new File("src/test/resources/curs/test/001.png"), curs
            .getCursors().get(1).getCursor());
        assertEquals(new File("src/test/resources/curs/test/001_mask.png"), curs
            .getCursors().get(1).getMask());
    }


    /**
     * Tests writing a Curs.
     * 
     * @throws IOException When file operation fails.
     */

    public void testWrite() throws IOException
    {
        Curs curs;
        List<Cursor> cursors;
        ByteArrayOutputStream stream;
        EgaImage cursor, mask;

        cursors = new ArrayList<Cursor>();
        cursor = new EgaImage(ImageIO.read(new File(
            "src/test/resources/curs/test/000.png")));
        mask = new EgaImage(ImageIO.read(new File(
            "src/test/resources/curs/test/000_mask.png")));
        cursors.add(new Cursor(cursor, mask));
        cursor = new EgaImage(ImageIO.read(new File(
            "src/test/resources/curs/test/001.png")));
        mask = new EgaImage(ImageIO.read(new File(
            "src/test/resources/curs/test/001_mask.png")));
        cursors.add(new Cursor(cursor, mask));

        curs = new Curs(cursors);
        stream = new ByteArrayOutputStream();
        curs.write(stream);

        assertEquals(new File("src/test/resources/curs/test.curs"), stream
            .toByteArray());
    }
}
