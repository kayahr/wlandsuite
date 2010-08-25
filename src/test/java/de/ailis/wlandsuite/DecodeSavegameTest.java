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

package de.ailis.wlandsuite;

import java.io.File;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Tests the DecodeSavegame program.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class DecodeSavegameTest extends LauncherTest
{
    /**
     * Returns the test suite.
     * 
     * @return The test suite
     */

    public static Test suite()
    {
        return new TestSuite(DecodeSavegameTest.class);
    }


    /**
     * @see junit.framework.TestCase#setUp()
     */

    @Override
    public void setUp()
    {
        this.progName = "decodesavegame";
    }


    /**
     * Tests empty call
     * 
     * @throws IOException When file operation fails.
     */

    @Override
    public void testEmpty() throws IOException
    {
        testExec(this.progName, 1, "",
            "^decodesavegame: ERROR! Unexpected end of stream while reading savegame\n$");
    }


    /**
     * Tests converting from file to file.
     * 
     * @throws IOException When file operation fails.
     */

    public void testFileFile() throws IOException
    {
        File file;

        file = File.createTempFile("decodesavegame", ".xml");
        testExec("decodesavegame src/test/resources/savegame/test " + file.getPath(),
            0, "^decodesavegame: Success\n$", "");
        assertEquals(new File("src/test/resources/savegame/test.xml"), file);
        file.delete();
    }
}
