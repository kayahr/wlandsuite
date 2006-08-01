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

import java.io.IOException;
import java.io.InputStream;

import de.ailis.wlandsuite.pic.VerticalXorInputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the VerticalXorInputStream class
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class VerticalXorInputStreamTest extends TestCase
{
    /**
     * Returns the test suite.
     * 
     * @return The test suite
     */

    public static Test suite()
    {
        return new TestSuite(VerticalXorInputStreamTest.class);
    }


    /**
     * Tests reading VXor encoded data.
     * 
     * @throws IOException
     */

    public void testReading() throws IOException
    {
        int count;
        int b1, b2;
        InputStream picStream;
        InputStream vxorStream;
        InputStream testStream;

        picStream = getClass().getClassLoader().getResourceAsStream(
            "vxor/encoded.pic");
        vxorStream = new VerticalXorInputStream(picStream, 288);
        try
        {
            testStream = getClass().getClassLoader().getResourceAsStream(
                "vxor/decoded.dat");
            try
            {
                count = 0;
                while ((b1 = vxorStream.read()) != -1)
                {
                    count++;
                    b2 = testStream.read();
                    assertEquals(b2, b1);
                }
                assertEquals(18432, count);
            }
            finally
            {
                testStream.close();
            }
        }
        finally
        {
            vxorStream.close();
        }
    }
}
