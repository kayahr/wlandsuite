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

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import de.ailis.wlandsuite.huffman.HuffmanInputStream;
import de.ailis.wlandsuite.huffman.HuffmanOutputStream;
import de.ailis.wlandsuite.huffman.HuffmanTree;
import de.ailis.wlandsuite.msq.MsqHeader;
import de.ailis.wlandsuite.msq.MsqType;
import de.ailis.wlandsuite.pic.Pic;


/**
 * HtdsTileset
 *
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class HtdsTileset
{
    /** The tiles */
    private final List<Pic> tiles;


    /**
     * Constructor
     *
     * @param tiles
     *            The tiles
     */

    public HtdsTileset(final List<Pic> tiles)
    {
        this.tiles = tiles;
    }


    /**
     * Reads HTDS tileset with the default tile size of 16x16 from the given
     * file.
     *
     * @param file
     *            The input file
     * @return The HTDS tileset
     * @throws IOException
     *             When file operation fails.
     */

    public static HtdsTileset read(final File file) throws IOException
    {
        return read(file, 16, 16);
    }


    /**
     * Reads HTDS tileset from the given file. Width and height of the tiles
     * must be specified because no image dimensions can be read from the
     * stream.
     *
     * @param file
     *            The input file
     * @param width
     *            The tile width
     * @param height
     *            The tile height
     * @return The HTDS tileset
     * @throws IOException
     *             When file operation fails.
     */

    public static HtdsTileset read(final File file, final int width, final int height)
        throws IOException
    {
        InputStream stream;

        stream = new FileInputStream(file);
        try
        {
            return read(stream, width, height);
        }
        finally
        {
            stream.close();
        }
    }


    /**
     * Reads HTDS tileset with the default tile size of 16x16 from the given
     * stream.
     *
     * This method returns null if no more tilesets are found on the stream.
     *
     * @param stream
     *            The input stream
     * @return The HTDS tileset
     * @throws IOException
     *             When file operation fails.
     */

    public static HtdsTileset read(final InputStream stream) throws IOException
    {
        return read(stream, 16, 16);
    }


    /**
     * Reads a HTDS tileset from a stream. Width and height of the tiles must be
     * specified because no image dimensions can be read from the stream.
     *
     * This method returns null if no more tilesets are found on the stream.
     *
     * @param stream
     *            The input stream
     * @param width
     *            The tile width
     * @param height
     *            The tile height
     * @return The tileset or null if no more tilesets are found
     * @throws IOException
     *             When file operation fails.
     */

    public static HtdsTileset read(final InputStream stream, final int width, final int height)
        throws IOException
    {
        MsqHeader header;
        HuffmanInputStream huffmanStream;
        List<Pic> tiles;
        int quantity;

        // Read the next MSQ header and validate it
        header = MsqHeader.read(stream);
        if (header == null)
        {
            // No more tilesets, abort.
            return null;
        }
        if (header.getType() != MsqType.Compressed)
        {
            throw new IOException(
                "Expected MSQ block of HTDS stream to be compressed");
        }

        // Calculate the number of tiles
        quantity = header.getSize() * 2 / width / height;

        // Read the tiles
        tiles = new ArrayList<Pic>(quantity);
        huffmanStream = new HuffmanInputStream(stream);
        for (int i = 0; i < quantity; i++)
        {
            tiles.add(Pic.read(huffmanStream, width, height));
        }
        return new HtdsTileset(tiles);
    }


    /**
     * Reads a tileset from an external tileset file for Displacer's hacked EXE.
     * Width and height of the tiles must be specified because no image
     * dimensions can be read from the stream.
     *
     * @param stream
     *            The input stream
     * @param width
     *            The tile width
     * @param height
     *            The tile height
     * @return The tileset
     * @throws IOException
     *             When file operation fails.
     */

    public static HtdsTileset readHacked(final InputStream stream, final int width,
        final int height) throws IOException
    {
        List<Pic> tiles;

        // Read the tiles
        tiles = new ArrayList<Pic>(163);
        while (true)
        {
            try
            {
                tiles.add(Pic.read(stream, width, height, false));
            }
            catch (final EOFException e)
            {
                break;
            }
        }
        return new HtdsTileset(tiles);
    }


    /**
     * Writes a HTDS tileset to a file.
     *
     * @param file
     *            The output file
     * @param disk
     *            The disk index
     * @throws IOException
     *             When file operation fails.
     */

    public void write(final File file, final int disk) throws IOException
    {
        OutputStream stream;

        stream = new FileOutputStream(file);
        try
        {
            write(stream, disk);
        }
        finally
        {
            stream.close();
        }
    }


    /**
     * Writes a HTDS tileset to a stream.
     *
     * @param stream
     *            The output stream
     * @param disk
     *            The disk index
     * @throws IOException
     *             When file operation fails.
     */

    public void write(final OutputStream stream, final int disk) throws IOException
    {
        MsqHeader header;
        HuffmanOutputStream huffmanStream;
        HuffmanTree tree;
        int size;
        byte[] bytes;

        // Calculate the size of the tileset
        size = 0;
        for (final Pic tile: this.tiles)
        {
            size += tile.getWidth() * tile.getHeight() / 2;
        }

        // Write the MSQ header
        header = new MsqHeader(MsqType.Compressed, disk, size);
        header.write(stream);

        // Write the tiles
        bytes = getBytes();
        tree = HuffmanTree.create(bytes);
        huffmanStream = new HuffmanOutputStream(stream, tree);
        huffmanStream.write(bytes);
        huffmanStream.flush();
    }


    /**
     * Writes a tileset to an external tileset file compatibly to Displacer's
     * hacked EXE.
     *
     * @param stream
     *            The output stream
     * @throws IOException
     *             When file operation fails.
     */

    public void writeHacked(final OutputStream stream) throws IOException
    {
        // Write the tiles
        stream.write(getBytes(false));
    }


    /**
     * Returns the xor encoded bytes of the tileset.
     *
     * @return The bytes
     */

    public byte[] getBytes()
    {
        return getBytes(true);
    }


    /**
     * Returns the bytes of the tileset.
     *
     * @param encoded
     *            If the pics should be xor encoded
     * @return The bytes
     */

    public byte[] getBytes(final boolean encoded)
    {
        ByteArrayOutputStream stream;

        try
        {
            stream = new ByteArrayOutputStream();
            try
            {
                for (final Pic tile: this.tiles)
                {
                    tile.write(stream, encoded);
                }
                return stream.toByteArray();
            }
            finally
            {
                stream.close();
            }
        }
        catch (final IOException e)
        {
            // Ignored, can't happen
            return null;
        }
    }

    /**
     * Returns the tiles.
     *
     * @return The tiles
     */

    public List<Pic> getTiles()
    {
        return this.tiles;
    }
}
