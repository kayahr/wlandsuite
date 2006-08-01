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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the HuffmanTree class
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class HuffmanTreeTest extends TestCase
{
    /**
     * Returns the test suite.
     * 
     * @return The test suite
     */

    public static Test suite()
    {
        return new TestSuite(HuffmanTreeTest.class);
    }


    /**
     * Tests creating a new huffman tree
     * 
     * @throws IOException
     */

    public void testCreateTree() throws IOException
    {
        InputStream dataStream;
        InputStream inputStream;
        ByteArrayOutputStream dataOutputStream;
        HuffmanOutputStream huffmanOutputStream;
        HuffmanInputStream huffmanInputStream;
        HuffmanTree tree;
        byte[] data;
        int b;

        // Build the tree
        dataStream = getClass().getClassLoader().getResourceAsStream(
            "huffman/decoded.dat");
        tree = HuffmanTree.create(dataStream);
        dataStream.close();

        // Encode the data
        inputStream = getClass().getClassLoader().getResourceAsStream(
            "huffman/decoded.dat");
        dataOutputStream = new ByteArrayOutputStream();
        huffmanOutputStream = new HuffmanOutputStream(dataOutputStream, tree);
        while ((b = inputStream.read()) != -1)
        {
            huffmanOutputStream.write(b);
        }
        huffmanOutputStream.flush();
        assertEquals(9522, dataOutputStream.size());
        data = dataOutputStream.toByteArray();
        inputStream.close();
        huffmanOutputStream.close();

        // Decode the data and verify the encoded data
        dataStream = new ByteArrayInputStream(data);
        huffmanInputStream = new HuffmanInputStream(dataStream);
        inputStream = getClass().getClassLoader().getResourceAsStream(
            "huffman/decoded.dat");
        while ((b = inputStream.read()) != -1)
        {
            assertEquals(b, huffmanInputStream.read());
        }
        inputStream.close();
        huffmanInputStream.close();
    }
}
