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

package de.ailis.wlandsuite.huffman;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the HuffmanOutputStream class
 *
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class HuffmanOutputStreamTest extends TestCase
{
    /**
     * Returns the test suite.
     *
     * @return The test suite
     */

    public static Test suite()
    {
        return new TestSuite(HuffmanOutputStreamTest.class);
    }


    /**
     * Tests reading of huffman compressed data.
     *
     * @throws IOException When file operation fails.
     */

    public void testReading() throws IOException
    {
        int count;
        int b, b1, b2;
        HuffmanTree tree;
        InputStream compressedStream;
        OutputStream huffmanStream;
        InputStream inputStream;
        ByteArrayOutputStream dataStream;
        InputStream testStream1;
        InputStream testStream2;
        byte[] testData;

        // Read the huffman tree from already encoded data
        compressedStream = getClass().getClassLoader().getResourceAsStream(
            "huffman/encoded.dat");
        try
        {
            tree = HuffmanTree.load(compressedStream);
        }
        finally
        {
            compressedStream.close();
        }

        // Compress and write encoded data to a byte array
        dataStream = new ByteArrayOutputStream();
        try
        {
            huffmanStream = new HuffmanOutputStream(dataStream, tree);
            inputStream = getClass().getResourceAsStream(
                "/huffman/decoded.dat");
            try
            {
                count = 0;
                while ((b = inputStream.read()) != -1)
                {
                    count++;
                    huffmanStream.write(b);
                }
                huffmanStream.flush();
                testData = dataStream.toByteArray();
                assertEquals(18432, count);
            }
            finally
            {
                inputStream.close();
            }
        }
        finally
        {
            dataStream.close();
        }


        testStream1 = new ByteArrayInputStream(testData);
        try
        {
            testStream2 = getClass().getResourceAsStream(
                "/huffman/encoded.dat");
            try
            {
                count = 0;
                while ((b1 = testStream1.read()) != -1)
                {
                    b2 = testStream2.read();
                    assertEquals(b2, b1);
                    count++;
                }
                assertEquals(9522, count);
            }
            finally
            {
                testStream2.close();
            }
        }
        finally
        {
            testStream1.close();
        }
    }
}
