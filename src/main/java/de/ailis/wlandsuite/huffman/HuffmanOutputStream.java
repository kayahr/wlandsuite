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

import java.io.IOException;
import java.io.OutputStream;

import de.ailis.wlandsuite.io.BitOutputStream;
import de.ailis.wlandsuite.io.BitOutputStreamWrapper;


/**
 * The HuffmanOutputStream allows writing huffman compressed data to an output
 * stream. The Huffman Tree must have already been calculated and given to the
 * constructor of this class.
 * 
 * It's very important that you close or flush the stream before you rely on the
 * written data because this stream uses a BitWriter which may cache bits until
 * a full byte is written.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class HuffmanOutputStream extends OutputStream
{
    /** The bit stream */
    private final BitOutputStream bitStream;

    /** The huffman tree */
    private final HuffmanTree tree;


    /**
     * Constructor
     * 
     * @param stream
     *            The output stream
     * @param tree
     *            The huffman tree
     * @throws IOException
     */

    public HuffmanOutputStream(final OutputStream stream, final HuffmanTree tree)
        throws IOException
    {
        this.bitStream = new BitOutputStreamWrapper(stream);
        this.tree = tree;
        this.tree.save(this.bitStream);
    }


    /**
     * @see java.io.OutputStream#write(int)
     */

    @Override
    public void write(final int b) throws IOException
    {

        final int payload = b < 0 ? b + 256 : b;

        final HuffmanNode node = this.tree.getNode(payload);
        if (node == null)
        {
            throw new IOException("No huffman node found for payload "
                + payload);
        }
        final boolean[] fullKey = node.getFullKey();
        for (int i = fullKey.length - 1; i >= 0; i--)
        {
            this.bitStream.writeBit(fullKey[i]);
        }
    }


    /**
     * @see java.io.OutputStream#flush()
     */

    @Override
    public void flush() throws IOException
    {
        this.bitStream.flush();
    }

    /**
     * @see java.io.OutputStream#close()
     */

    @Override
    public void close() throws IOException
    {
        this.bitStream.close();
    }
}
