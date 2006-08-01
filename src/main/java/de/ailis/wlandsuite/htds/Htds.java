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

package de.ailis.wlandsuite.htds;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import de.ailis.wlandsuite.huffman.HuffmanInputStream;
import de.ailis.wlandsuite.msq.MsqHeader;


/**
 * A HTDS object contains the tile sets from the allhtds1 or allhtds2 files of
 * Wasteland.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Htds
{
    /** The tilesets */
    private List<HtdsTileset> tilesets;


    /**
     * Constructor
     * 
     * @param tilesets
     *            The tilesets
     */

    public Htds(List<HtdsTileset> tilesets)
    {
        this.tilesets = tilesets;
    }


    /**
     * Reads HTDS tilesets with the default tile size of 16x16 from the given
     * stream.
     * 
     * @param stream
     *            The input stream
     * @return The HTDS tilesets
     * @throws IOException
     */

    public static Htds read(InputStream stream) throws IOException
    {
        return read(stream, 16, 16);
    }


    /**
     * Reads HTDS tilesets from the given stream. Width and height of the tiles
     * must be specified because no image dimensions can be read from the
     * stream.
     * 
     * @param stream
     *            The input stream
     * @param width
     *            The tile width
     * @param height
     *            The tile height
     * @return The HTDS tilesets
     * @throws IOException
     */

    public static Htds read(InputStream stream, int width, int height)
        throws IOException
    {
        List<HtdsTileset> tilesets;
        HtdsTileset tileset;

        tilesets = new ArrayList<HtdsTileset>();
        while ((tileset = HtdsTileset.read(stream, width, height)) != null)
        {
            tilesets.add(tileset);
        }
        return new Htds(tilesets);
    }


    /**
     * Writes HTDS tilesets to the given stream. The disk index is determined
     * automatically by looking at the number of tilesets in the HTDS.
     * 
     * @param stream
     *            The output stream
     * @throws IOException
     */

    public void write(OutputStream stream) throws IOException
    {
        write(stream, this.tilesets.size() == 4 ? 0 : 1);
    }


    /**
     * Writes HTDS tilesets to the given stream.
     * 
     * @param stream
     *            The output stream
     * @param disk
     *            The disk index
     * @throws IOException
     */

    public void write(OutputStream stream, int disk) throws IOException
    {
        for (HtdsTileset tileset: this.tilesets)
        {
            tileset.write(stream, disk);
        }
    }


    /**
     * Returns the offsets of the tileset MSQ blocks in the specified file.
     * The offsets are determined by reading the raw data of each block and
     * looking at the position in the file.
     * 
     * @param file
     *            The file
     * @return The offsets
     * @throws IOException
     */

    public static List<Integer> getMsqOffsets(File file) throws IOException
    {
        List<Integer> offsets;
        RandomAccessFile access;
        FileInputStream stream;
        MsqHeader header;
        long offset;
        byte[] dummy;
        HuffmanInputStream huffmanStream;

        offsets = new ArrayList<Integer>();
        access = new RandomAccessFile(file, "r");
        try
        {
            stream = new FileInputStream(access.getFD());
            offset = 0;
            while ((header = MsqHeader.read(stream)) != null)
            {
                offsets.add(Integer.valueOf((int) offset));
                huffmanStream = new HuffmanInputStream(stream);
                dummy = new byte[header.getSize()];
                huffmanStream.read(dummy);
                offset = access.getFilePointer();
            }
        }
        finally
        {
            access.close();
        }
        return offsets;
    }


    /**
     * Returns the tilesets.
     * 
     * @return The tilesets
     */

    public List<HtdsTileset> getTilesets()
    {
        return this.tilesets;
    }
}
