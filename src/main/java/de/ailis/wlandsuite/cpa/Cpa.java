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

package de.ailis.wlandsuite.cpa;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.ailis.wlandsuite.huffman.HuffmanInputStream;
import de.ailis.wlandsuite.huffman.HuffmanOutputStream;
import de.ailis.wlandsuite.huffman.HuffmanTree;
import de.ailis.wlandsuite.msq.MsqHeader;
import de.ailis.wlandsuite.msq.MsqType;
import de.ailis.wlandsuite.pic.Pic;


/**
 * A CPA (most likely a "Compressed Picture Animation") simply consists out of a
 * base frame and several animation frames with delay information.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Cpa implements Serializable
{
    /** Serial version UID */
    private static final long serialVersionUID = -8589288667220674619L;

    /** The base frame */
    private Pic baseFrame;

    /** The animation frames */
    private List<CpaFrame> frames;


    /**
     * Private constructor
     */

    private Cpa()
    {
        this.frames = new ArrayList<CpaFrame>();
    }


    /**
     * Constructor for building a new CPA.
     * 
     * @param baseFrame
     *            The base frame
     * @param frames
     *            The animation frames
     */

    public Cpa(Pic baseFrame, List<CpaFrame> frames)
    {
        this.baseFrame = baseFrame;
        this.frames = frames;
    }


    /**
     * Loads a CPA from a stream. A base frame width of 288 pixel is assumed
     * which is the size of Wasteland's end.cpa file. If you want to read a
     * picture with a different width then use the read method where you can
     * specify a custom width.
     * 
     * @param stream
     *            The input stream
     * @return The CPA
     * @throws IOException
     * 
     * @see Cpa#read(InputStream, int)
     */

    public static Cpa read(InputStream stream) throws IOException
    {
        return read(stream, 288);
    }


    /**
     * Loads a CPA from a stream. The width of the base frame must be specified
     * because image dimensions can't be read from a CPA. The height is
     * automatically calculated by using the data size and the width.
     * 
     * @param stream
     *            The input stream
     * @param width
     *            The width of the base frame
     * @return The CPA
     * @throws IOException
     */

    public static Cpa read(InputStream stream, int width) throws IOException
    {
        MsqHeader header;
        HuffmanInputStream huffmanStream;
        Cpa cpa;
        int height;
        CpaFrame frame;
        Pic basePic;

        // Read the first MSQ header of the base frame and validate it
        header = MsqHeader.read(stream);
        if (header == null)
        {
            throw new IOException("Did not find first CPA MSQ block");
        }
        if (header.getType() != MsqType.Compressed)
        {
            throw new IOException(
                "Expected first MSQ block of CPA stream to be compressed");
        }

        // Calculate the picture height
        height = header.getSize() * 2 / width;

        // Create the CPA
        cpa = new Cpa();

        // Read the base frame
        huffmanStream = new HuffmanInputStream(stream);
        cpa.baseFrame = Pic.read(huffmanStream, width, height);

        // Read the second MSQ header (The frames) and validate it
        header = MsqHeader.read(stream);
        if (header == null)
        {
            throw new IOException("Did not find second CPA MSQ block");
        }
        if (header.getType() != MsqType.CpaAnimation)
        {
            throw new IOException(
                "Expected second MSQ block of CPA stream to be a CPA animation block");
        }

        // Start a huffman input stream
        huffmanStream = new HuffmanInputStream(stream);

        // Read the animation size from the MSQ block (which is ignored)
        if (huffmanStream.readWord() == -1)
        {
            throw new EOFException(
                "Unexpected end of stream while reading animation size");
        }

        // Read all the frames
        basePic = cpa.baseFrame;
        while ((frame = CpaFrame.read(huffmanStream, basePic)) != null)
        {
            cpa.frames.add(frame);
            basePic = frame.getPic();
        }

        // Return the CPA
        return cpa;
    }


    /**
     * Writes a CPA to a stream.
     * 
     * @param stream
     *            The output stream
     * @throws IOException
     */

    public void write(OutputStream stream) throws IOException
    {
        MsqHeader header;
        HuffmanTree tree;
        HuffmanOutputStream huffmanStream;
        int size;
        byte[] animBlock;

        // Create and write the first MSQ header (base frame)
        size = this.baseFrame.getWidth() * this.baseFrame.getHeight() / 2;
        header = new MsqHeader(MsqType.Compressed, 0, size);
        header.write(stream);

        // Write the base frame
        tree = HuffmanTree.create(this.baseFrame.getBytes());
        huffmanStream = new HuffmanOutputStream(stream, tree);
        this.baseFrame.write(huffmanStream);
        huffmanStream.flush();

        // Write the second MSQ header (The animation frames)
        animBlock = buildAnimationBlock();
        header = new MsqHeader(MsqType.CpaAnimation, 0, animBlock.length + 2);
        header.write(stream);

        // Write the animation block
        tree = HuffmanTree.create(animBlock);
        huffmanStream = new HuffmanOutputStream(stream, tree);
        huffmanStream.write(animBlock);
        huffmanStream.flush();
    }


    /**
     * Builds the animation block. This consists of a size, the animation data
     * and a trailing 0x0000.
     * 
     * @return The animation block
     */

    private byte[] buildAnimationBlock()
    {
        ByteArrayOutputStream stream;
        byte[] animData;

        stream = new ByteArrayOutputStream();
        try
        {
            try
            {
                animData = buildAnimationData();
                stream.write(animData.length & 0xff);
                stream.write(animData.length >> 8);
                stream.write(animData);
                stream.write(0x00);
                stream.write(0x00);
                return stream.toByteArray();
            }
            finally
            {
                stream.close();
            }
        }
        catch (IOException e)
        {
            // Ignored. Can't happen
            return null;
        }
    }


    /**
     * Builds the animation data.
     * 
     * @return The animation data
     */

    private byte[] buildAnimationData()
    {
        ByteArrayOutputStream stream;
        Pic basePic;

        stream = new ByteArrayOutputStream();
        try
        {
            try
            {
                basePic = this.baseFrame;
                for (int i = 0; i < this.frames.size(); i++)
                {
                    CpaFrame frame = this.frames.get(i);
                    stream.write(frame.getDelay() & 0xff);
                    stream.write(frame.getDelay() >> 8);
                    stream.write(buildDiff(basePic, frame.getPic(),
                        i == this.frames.size() - 4 ? this.frames.get(
                            this.frames.size() - 1).getPic() : null));
                    stream.write(0xff);
                    stream.write(0xff);
                    basePic = frame.getPic();
                }
                stream.write(0xff);
                stream.write(0xff);
                return stream.toByteArray();
            }
            finally
            {
                stream.close();
            }
        }
        catch (IOException e)
        {
            // Ignored. Can't happen
            return null;
        }
    }


    /**
     * Builds a diff between two frames. For the 12th frame the last frame must
     * be specified, too so the diff also overwrites the changes made by tha
     * last frame. This is needed because Wasteland loops the frames 12-15.
     * 
     * @param base
     *            The base frame
     * @param frame
     *            The animation frame
     * @param last
     *            The optional last frame
     * @return The diff
     */

    private byte[] buildDiff(Pic base, Pic frame, Pic last)
    {
        ByteArrayOutputStream stream;
        byte[] bytes;
        int width, height;
        int x, y, i;
        int p1, p2;
        int offset;
        boolean changed;

        stream = new ByteArrayOutputStream();
        try
        {
            try
            {
                width = base.getWidth();
                height = base.getHeight();
                bytes = new byte[4];
                for (y = 0; y < height; y++)
                {
                    for (x = 0; x < width; x += 8)
                    {
                        changed = false;
                        for (i = 0; i < 4; i++)
                        {
                            p1 = frame.getPixel(x + i * 2, y);
                            p2 = frame.getPixel(x + i * 2 + 1, y);
                            if ((base.getPixel(x + i * 2, y) != p1)
                                || (base.getPixel(x + i * 2 + 1, y) != p2))
                            {
                                changed = true;
                            }
                            if (last != null)
                            {
                                if ((last.getPixel(x + i * 2, y) != p1)
                                    || (last.getPixel(x + i * 2 + 1, y) != p2))
                                {
                                    changed = true;
                                }
                            }

                            bytes[i] = (byte) ((p1 << 4) | p2);
                        }
                        if (changed)
                        {
                            offset = (y * 320 + x) / 8;
                            stream.write(offset & 0xff);
                            stream.write(offset >> 8);
                            stream.write(bytes);
                        }
                    }
                }
                return stream.toByteArray();
            }
            finally
            {
                stream.close();
            }
        }
        catch (IOException e)
        {
            // Ignored. Can't happen
            return null;
        }
    }


    /**
     * Returns the base frame.
     * 
     * @return The base frame
     */

    public Pic getBaseFrame()
    {
        return this.baseFrame;
    }


    /**
     * Returns the animation frames.
     * 
     * @return The animation frames
     */

    public List<CpaFrame> getFrames()
    {
        return this.frames;
    }
}
