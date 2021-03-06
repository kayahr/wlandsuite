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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.ailis.wlandsuite.huffman.HuffmanInputStream;
import de.ailis.wlandsuite.huffman.HuffmanOutputStream;
import de.ailis.wlandsuite.huffman.HuffmanTree;
import de.ailis.wlandsuite.io.BitInputStream;
import de.ailis.wlandsuite.io.BitInputStreamWrapper;
import de.ailis.wlandsuite.msq.MsqHeader;
import de.ailis.wlandsuite.msq.MsqType;
import de.ailis.wlandsuite.pic.Pic;


/**
 * An animation consists of a base frame (which is the compressed pic found in
 * the beginning of a Wasteland animation structure) and a number of animation
 * frame sets (which can be used to play multiple animations at once). Simple
 * animations (like the rotating dish of the Ranger Center) only has one frame
 * set.
 *
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class PicsAnimation
{
    /** The base frame */
    private final Pic baseFrame;

    /** The animation frame sets */
    private List<PicsAnimationFrameSet> frameSets = new ArrayList<PicsAnimationFrameSet>();


    /**
     * Constructor
     *
     * @param baseFrame
     *            The base frame;
     * @param frameSets
     *            The frame sets
     */

    public PicsAnimation(final Pic baseFrame, final List<PicsAnimationFrameSet> frameSets)
    {
        this.baseFrame = baseFrame;
        this.frameSets = frameSets;
    }


    /**
     * Reads an animation from the given input stream. If no more animations are
     * found then NULL is returned. The width of the picture must be specified
     * because no image dimensions can be read from the stream.
     *
     * @param stream
     *            The PICS animation stream
     * @param width
     *            The width of the picture
     * @return The picture animation
     * @throws IOException
     *             When file operation fails.
     */

    public static PicsAnimation read(final InputStream stream, final int width)
        throws IOException
    {
        MsqHeader header;
        int height;
        HuffmanInputStream huffmanStream;
        Pic baseFrame;

        // Read the next MSQ header and validate it
        header = MsqHeader.read(stream);
        if (header == null)
        {
            // No more animations, abort.
            return null;
        }
        if (header.getType() != MsqType.Compressed)
        {
            throw new IOException(
                "Expected base frame block of PICS stream to be compressed");
        }

        // Calculate the height
        height = header.getSize() * 2 / width;

        // Read the base frame
        huffmanStream = new HuffmanInputStream(stream);
        baseFrame = Pic.read(huffmanStream, width, height);

        // Read the second MSQ header (The animation frames) and validate it
        header = MsqHeader.read(stream);
        if (header == null)
        {
            throw new EOFException(
                "Unexpected end of stream while reading PICS animation block");
        }
        if (header.getType() != MsqType.Compressed)
        {
            throw new IOException(
                "Expected animation block of PICS stream to be compressed");
        }

        // Start a huffman input stream
        huffmanStream = new HuffmanInputStream(stream);

        return new PicsAnimation(baseFrame, readAnimationData(huffmanStream,
            baseFrame));
    }


    /**
     * Reads the animation data from the stream.
     *
     * @param stream
     *            The input stream
     * @param baseFrame
     *            The base frame
     * @return The animation data
     * @throws IOException
     *             When file operation fails.
     */

    private static List<PicsAnimationFrameSet> readAnimationData(
        final BitInputStream stream, final Pic baseFrame) throws IOException
    {
        List<PicsAnimationFrameSet> frameSets;
        PicsAnimationFrameSet frameSet;
        List<RawAnimationFrame> rawFrames;
        RawAnimationFrame rawFrame;
        int dataSize;
        int pos;
        byte[] instructions;
        Pic workingFrame;
        int headerSize;
        InputStream byteStream;
        int delay;
        int frameNo;

        // Read the header size from the MSQ block
        headerSize = stream.readWord();
        if (headerSize == -1)
        {
            throw new EOFException(
                "Unexpected end of stream while reading animation header size");
        }

        // Read the raw animation instructions
        instructions = new byte[headerSize];
        if (stream.read(instructions) != headerSize)
        {
            throw new EOFException(
                "Unexpected end of stream while reading animation header");
        }

        // Read the raw animation frames
        rawFrames = new ArrayList<RawAnimationFrame>();
        dataSize = stream.readWord();
        if (dataSize == -1)
        {
            throw new IOException(
                "Unexpected end of stream while reading animation data size");
        }
        pos = 0;
        while (pos < dataSize)
        {
            rawFrame = RawAnimationFrame.read(stream);
            rawFrames.add(rawFrame);
            pos += rawFrame.getSize();
        }

        // Cycle through the animation instructions and build the frame sets
        frameSets = new ArrayList<PicsAnimationFrameSet>();
        byteStream = new ByteArrayInputStream(instructions);
        frameSet = new PicsAnimationFrameSet();
        workingFrame = baseFrame.clone();
        try
        {
            while ((delay = byteStream.read()) != -1)
            {
                if (delay == 255)
                {
                    frameSets.add(frameSet);
                    frameSet = new PicsAnimationFrameSet();
                    workingFrame = baseFrame.clone();
                    continue;
                }
                frameNo = byteStream.read();
                if (frameNo == -1)
                {
                    throw new EOFException(
                        "Unexpected end of animation frame header stream");
                }
                frameSet.addFrame(delay, frameNo, baseFrame, workingFrame,
                    rawFrames);
            }
        }
        finally
        {
            byteStream.close();
        }

        return frameSets;
    }


    /**
     * Reads an external encounter animation file as used by Displacer's hacked
     * EXE.
     *
     * @param stream
     *            The input stream
     * @param width
     *            The animation width
     * @param height
     *            The animation height
     * @return The animation
     * @throws IOException
     *             When file operation fails.
     */

    public static PicsAnimation readHacked(final InputStream stream, final int width,
        final int height) throws IOException
    {
        Pic baseFrame;
        List<PicsAnimationFrameSet> frames;

        baseFrame = Pic.read(stream, width, height, false);
        frames = readAnimationData(new BitInputStreamWrapper(stream), baseFrame);
        return new PicsAnimation(baseFrame, frames);
    }


    /**
     * Writes the picture animation to the given output stream.
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
        HuffmanTree huffmanTree;
        HuffmanOutputStream huffmanStream;
        byte bytes[];

        // Write the base frame MSQ header
        bytes = this.baseFrame.getBytes();
        header = new MsqHeader(MsqType.Compressed, disk, bytes.length);
        header.write(stream);

        // Write the base frame MSQ data
        huffmanTree = HuffmanTree.create(bytes);
        huffmanStream = new HuffmanOutputStream(stream, huffmanTree);
        huffmanStream.write(bytes);
        huffmanStream.flush();

        // Write the animation MSQ header
        header = new MsqHeader(MsqType.Compressed, disk, bytes.length);
        header.write(stream);

        // Write the animation MSQ data
        bytes = getAnimationData();
        huffmanTree = HuffmanTree.create(bytes);
        huffmanStream = new HuffmanOutputStream(stream, huffmanTree);
        huffmanStream.write(bytes);
        huffmanStream.flush();
    }


    /**
     * Returns the animation data as byte array. Used internally by the
     * write() and writeHacked() methods.
     *
     * @return The animation data as byte array
     * @throws IOException
     *             When file operation fails.
     */

    private byte[] getAnimationData() throws IOException
    {
        List<RawAnimationFrame> seenFrames;
        boolean seen;
        byte bytes[];
        int currentFrame, nextFrame;
        int delay;
        int frameId, newFrameId;
        ByteArrayOutputStream headerStream;
        ByteArrayOutputStream dataStream;
        ByteArrayOutputStream animStream;
        RawAnimationFrame frame;
        Map<String, RawAnimationFrame> rawFrames;
        int size;
        String key;

        // Create animation data
        seenFrames = new ArrayList<RawAnimationFrame>();
        headerStream = new ByteArrayOutputStream();
        dataStream = new ByteArrayOutputStream();
        newFrameId = 0;
        for (final PicsAnimationFrameSet frameSet: this.frameSets)
        {
            // Get the map with raw frames from the frame set
            rawFrames = frameSet.getRawFrames(this.baseFrame);

            // Cycle through instructions
            currentFrame = 0;
            for (final PicsAnimationInstruction instruction: frameSet
                .getInstructions())
            {
                delay = instruction.getDelay();
                nextFrame = instruction.getFrame();
                key = PicsAnimationFrameSet.getRawFrameKey(currentFrame,
                    nextFrame);

                frame = rawFrames.get(key);

                frameId = seenFrames.indexOf(frame);
                seen = frameId != -1;
                if (!seen)
                {
                    frameId = newFrameId;
                    newFrameId++;
                }

                // Add instructions to header stream
                headerStream.write(delay);
                headerStream.write(frameId);

                // Write the rawframe to the data stream
                if (!seen)
                {
                    frame.write(dataStream);
                    seenFrames.add(frame);
                }

                // Process next frame
                currentFrame = nextFrame;
            }

            // Write the end-of-instructions maerker
            headerStream.write(0xff);
        }

        // Append header and data to get the bytes we need to compress
        animStream = new ByteArrayOutputStream();
        size = headerStream.size();
        animStream.write(size & 0xff);
        animStream.write((size >> 8) & 0xff);
        animStream.write(headerStream.toByteArray());
        size = dataStream.size();
        animStream.write(size & 0xff);
        animStream.write((size >> 8) & 0xff);
        animStream.write(dataStream.toByteArray());
        bytes = animStream.toByteArray();

        // Close the working streams
        dataStream.close();
        headerStream.close();
        animStream.close();

        return bytes;
    }


    /**
     * Writes an external animation file as used by Displacer's hacked EXE.
     *
     * @param stream
     *            The output stream
     * @throws IOException
     *             When file operation fails.
     */

    public void writeHacked(final OutputStream stream) throws IOException
    {
        // Write the base frame MSQ header
        stream.write(this.baseFrame.getBytes(false));

        // Write the animation data
        stream.write(getAnimationData());
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
     * Returns the frame sets.
     *
     * @return The frame sets
     */

    public List<PicsAnimationFrameSet> getFrameSets()
    {
        return this.frameSets;
    }
}
