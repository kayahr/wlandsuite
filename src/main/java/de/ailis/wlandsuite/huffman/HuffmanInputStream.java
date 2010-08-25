/*
 * $Id$
 * Copyright (C) 2006 Klaus Reimer <k@ailis.de>
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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import de.ailis.wlandsuite.io.BitInputStream;
import de.ailis.wlandsuite.io.BitInputStreamWrapper;


/**
 * The HuffmanInputStream allows reading huffman compressed data from an input
 * stream.
 *
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class HuffmanInputStream extends BitInputStream
{
    /** The huffman tree */
    private final HuffmanTree tree;

    /** The bit reader */
    private final BitInputStream bitStream;


    /**
     * Constructor
     *
     * @param stream
     *            The input stream
     * @throws IOException
     *             When file operation fails.
     */

    public HuffmanInputStream(final InputStream stream) throws IOException
    {
        this.bitStream = new BitInputStreamWrapper(stream);
        this.tree = HuffmanTree.load(this.bitStream);
    }


    /**
     * @see java.io.InputStream#read()
     */

    @Override
    public int read() throws IOException
    {
        byte bit;
        HuffmanNode node;
        int payload;

        node = this.tree.getRootNode();
        while ((payload = node.getPayload()) == -1)
        {
            bit = this.bitStream.readBit();
            if (bit == -1)
            {
                throw new EOFException(
                    "Unexpected end of stream while reading huffman data");
            }
            if (bit == 0)
            {
                node = node.getLeft();
            }
            else
            {
                node = node.getRight();
            }
        }
        return payload;
    }
}
