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

import java.awt.image.BufferedImage;
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
import de.ailis.wlandsuite.msq.MsqHeader;
import de.ailis.wlandsuite.msq.MsqType;
import de.ailis.wlandsuite.pic.Pic;


/**
 * Animation
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */
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
    private Pic baseFrame;

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

    public PicsAnimation(Pic baseFrame, List<PicsAnimationFrameSet> frameSets)
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
     */

    public static PicsAnimation read(InputStream stream, int width)
        throws IOException
    {
        int headerSize;
        int dataSize;
        int pos;
        int delay;
        int frameNo;
        byte[] instructions;
        List<PicsAnimationFrameSet> frameSets;
        PicsAnimationFrameSet frameSet;
        List<RawAnimationFrame> rawFrames;
        RawAnimationFrame rawFrame;
        Pic workingFrame;
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

        // Read the header size from the MSQ block
        headerSize = huffmanStream.readWord();
        if (headerSize == -1)
        {
            throw new EOFException(
                "Unexpected end of stream while reading animation header size");
        }

        // Read the raw animation instructions
        instructions = new byte[headerSize];
        if (huffmanStream.read(instructions) != headerSize)
        {
            throw new EOFException(
                "Unexpected end of stream while reading animation header");
        }

        // Read the raw animation frames
        rawFrames = new ArrayList<RawAnimationFrame>();
        dataSize = huffmanStream.readWord();
        if (dataSize == -1)
        {
            throw new IOException(
                "Unexpected end of stream while reading animation data size");
        }
        pos = 0;
        while (pos < dataSize)
        {
            rawFrame = RawAnimationFrame.read(huffmanStream);
            rawFrames.add(rawFrame);
            pos += rawFrame.getSize();
        }

        // Cycle through the animation instructions and build the frame sets
        frameSets = new ArrayList<PicsAnimationFrameSet>();
        stream = new ByteArrayInputStream(instructions);
        frameSet = new PicsAnimationFrameSet();
        workingFrame = baseFrame.clone();
        try
        {
            while ((delay = stream.read()) != -1)
            {
                if (delay == 255)
                {
                    frameSets.add(frameSet);
                    frameSet = new PicsAnimationFrameSet();
                    workingFrame = baseFrame.clone();
                    continue;
                }
                frameNo = stream.read();
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
            stream.close();
        }

        return new PicsAnimation(baseFrame, frameSets);
    }


    /**
     * Writes the picture animation to the given output stream.
     * 
     * @param stream
     *            The output stream
     * @param disk
     *            The disk index
     * @throws IOException
     */

    public void write(OutputStream stream, int disk) throws IOException
    {
        MsqHeader header;
        HuffmanTree huffmanTree;
        HuffmanOutputStream huffmanStream;
        RawAnimationFrame frame;
        Map<String, RawAnimationFrame> rawFrames;
        byte bytes[];
        int size;
        String key;
        List<RawAnimationFrame> seenFrames;
        boolean seen;
        int currentFrame, nextFrame;
        int delay;
        int frameId, newFrameId;        
        ByteArrayOutputStream headerStream;
        ByteArrayOutputStream dataStream;
        ByteArrayOutputStream animStream;

        // Write the base frame MSQ header
        bytes = this.baseFrame.getBytes();
        header = new MsqHeader(MsqType.Compressed, disk, bytes.length);
        header.write(stream);

        // Write the base frame MSQ data
        huffmanTree = HuffmanTree.create(bytes);
        huffmanStream = new HuffmanOutputStream(stream, huffmanTree);
        huffmanStream.write(bytes);
        huffmanStream.flush();

        // Create animation data
        seenFrames = new ArrayList<RawAnimationFrame>();
        headerStream = new ByteArrayOutputStream();
        dataStream = new ByteArrayOutputStream();
        newFrameId = 0;
        for (PicsAnimationFrameSet frameSet: this.frameSets)
        {
            // Get the map with raw frames from the frame set
            rawFrames = frameSet.getRawFrames(this.baseFrame);

            // Cycle through instructions
            currentFrame = 0;
            for (PicsAnimationInstruction instruction: frameSet
                .getInstructions())
            {
                delay = instruction.getDelay();
                nextFrame = instruction.getFrame();
                key = PicsAnimationFrameSet.getRawFrameKey(currentFrame, nextFrame);
                
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

        // Write the animation MSQ header
        header = new MsqHeader(MsqType.Compressed, disk, bytes.length);
        header.write(stream);

        // Write the animation MSQ data
        huffmanTree = HuffmanTree.create(bytes);
        huffmanStream = new HuffmanOutputStream(stream, huffmanTree);
        huffmanStream.write(bytes);
        huffmanStream.flush();
    }


    /**
     * Returns the base frame.
     * 
     * @return The base frame
     */

    public BufferedImage getBaseFrame()
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
