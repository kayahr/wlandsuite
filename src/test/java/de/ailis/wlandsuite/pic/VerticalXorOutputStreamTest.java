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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.ailis.wlandsuite.pic.VerticalXorOutputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the VerticalXorOutputStream class
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class VerticalXorOutputStreamTest extends TestCase
{
    /**
     * Returns the test suite.
     * 
     * @return The test suite
     */

    public static Test suite()
    {
        return new TestSuite(VerticalXorOutputStreamTest.class);
    }


    /**
     * Tests writing VXor endoced data.
     * 
     * @throws IOException
     */

    public void testWriting() throws IOException
    {
        int count;
        int b, b1, b2;
        byte[] buffer;
        ByteArrayOutputStream picStream;
        OutputStream vxorStream;
        InputStream testStream;
        InputStream testStream2;

        picStream = new ByteArrayOutputStream();
        vxorStream = new VerticalXorOutputStream(picStream, 288);
        try
        {
            testStream = getClass().getClassLoader().getResourceAsStream(
                "vxor/decoded.dat");
            try
            {
                count = 0;
                while ((b = testStream.read()) != -1)
                {
                    vxorStream.write(b);
                    count++;
                }
                buffer = picStream.toByteArray();
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


        testStream = getClass().getClassLoader().getResourceAsStream(
            "vxor/encoded.pic");
        try
        {
            testStream2 = new ByteArrayInputStream(buffer);
            try
            {
                count = 0;
                while ((b1 = testStream2.read()) != -1)
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
            testStream.close();
        }
    }
}
