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

package de.ailis.wlandsuite.pics;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ailis.wlandsuite.huffman.HuffmanInputStream;
import de.ailis.wlandsuite.msq.MsqHeader;


/**
 * A PICS object contains the encounter animations from the files
 * allpics1 or allpics2.
 *
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Pics
{
    /** The logger */
    private static final Log log = LogFactory.getLog(Pics.class);

    /** The tilesets */
    private final List<PicsAnimation> animations;


    /**
     * Constructor
     *
     * @param animations
     *            The animations
     */

    public Pics(final List<PicsAnimation> animations)
    {
        this.animations = animations;
    }


    /**
     * Reads PICS animations with the default width of 96 pixels from the given
     * stream.
     *
     * @param stream
     *            The input stream
     * @return The PICS animations
     * @throws IOException
     *             When file operation fails.
     */

    public static Pics read(final InputStream stream) throws IOException
    {
        return read(stream, 96);
    }


    /**
     * Reads PICS animations from the given stream. Width
     * must be specified because no image dimensions can be read from the
     * stream.
     *
     * @param stream
     *            The input stream
     * @param width
     *            The tile width
     * @return The PICS animations
     * @throws IOException
     *             When file operation fails.
     */

    public static Pics read(final InputStream stream, final int width)
        throws IOException
    {
        List<PicsAnimation> animations;
        PicsAnimation animation;
        int picNo;

        animations = new ArrayList<PicsAnimation>();
        picNo = 0;
        log.info("Reading pic " + picNo);
        while ((animation = PicsAnimation.read(stream, width)) != null)
        {
            animations.add(animation);
            picNo++;
            log.info("Reading pic " + picNo);
        }
        return new Pics(animations);
    }


    /**
     * Writes PICS animations to the given stream. The disk index is determined
     * automatically by looking at the number of tilesets in the HTDS.
     *
     * @param stream
     *            The output stream
     * @throws IOException
     *             When file operation fails.
     */

    public void write(final OutputStream stream) throws IOException
    {
        write(stream, this.animations.size() == 33 ? 0 : 1);
    }


    /**
     * Writes PICS animations to the given stream.
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
        int picNo = 0;
        for (final PicsAnimation animation: this.animations)
        {
            log.info("Writing pic " + picNo);
            animation.write(stream, disk);
            picNo++;
        }
    }


    /**
     * Returns the offsets of the base frame MSQ blocks in the specified file.
     * The offsets are determined by reading the raw data of each block and
     * looking at the current position in the file.
     *
     * @param file
     *            The file
     * @return The offsets
     * @throws IOException
     *             When file operation fails.
     */

    public static List<Integer> getMsqOffsets(final File file) throws IOException
    {
        List<Integer> offsets;
        RandomAccessFile access;
        FileInputStream stream;
        MsqHeader header;
        long offset;
        byte[] dummy;
        boolean baseFrame = true;
        HuffmanInputStream huffmanStream;

        offsets = new ArrayList<Integer>();
        access = new RandomAccessFile(file, "r");
        try
        {
            stream = new FileInputStream(access.getFD());
            offset = 0;
            while ((header = MsqHeader.read(stream)) != null)
            {
                if (baseFrame)
                {
                    offsets.add(Integer.valueOf((int) offset));
                }
                baseFrame = !baseFrame;
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

    public List<PicsAnimation> getAnimations()
    {
        return this.animations;
    }
}
