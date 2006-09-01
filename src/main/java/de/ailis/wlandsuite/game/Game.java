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

package de.ailis.wlandsuite.game;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import de.ailis.wlandsuite.game.blocks.GameBlock;
import de.ailis.wlandsuite.utils.FileUtils;


/**
 * Game
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Game
{
    /** The game blocks */
    private List<GameBlock> blocks;

    /** The disk number */
    private int disk;


    /**
     * Constructor
     * 
     * @param blocks
     *            The game blocks
     * @param disk
     *            The disk number
     */

    public Game(List<GameBlock> blocks, int disk)
    {
        this.blocks = blocks;
        this.disk = disk;
    }


    /**
     * Reads all game blocks from the specified input stream. Data is
     * automatically decrypted.
     * 
     * @param stream
     *            The input stream
     * @return The game
     * @throws IOException
     */

    public static Game read(InputStream stream) throws IOException
    {
        return read(stream, true);        
    }
    
    
    /**
     * Reads all game blocks from the specified input stream. A second 
     * parameter can be used to control if the data should be decrypted
     * or not.
     * 
     * @param stream
     *            The input stream
     * @param decrypt
     *            If data should be decrypted
     * @return The game
     * @throws IOException
     */

    public static Game read(InputStream stream, boolean decrypt) throws IOException
    {
        byte[] bytes;
        List<GameBlock> blocks;
        GameBlock block;
        InputStream byteStream;
        int pos, end;
        int disk;
        boolean eof = false;

        // Read the whole file into a byte array
        bytes = FileUtils.readBytes(stream);

        // Check if it's a game file and get disk index
        if (bytes[0] != 'm' || bytes[1] != 's' || bytes[2] != 'q'
            || (bytes[3] != '0' && bytes[3] != '1'))
        {
            throw new IOException("Stream is not a game file");
        }
        disk = bytes[3] - '0';

        // Set the byte stream to the bytes array and skip the msq header
        byteStream = new ByteArrayInputStream(bytes);
        byteStream.skip(4);

        // Read until the end of the stream has been reached
        pos = 4;
        blocks = new ArrayList<GameBlock>();
        while (!eof)
        {
            // Find end of MSQ block
            for (end = pos; end < bytes.length; end++)
            {
                if (bytes[end] == 'm' && bytes[end + 1] == 's'
                    && bytes[end + 2] == 'q' && bytes[end + 3] == '0' + disk)
                {
                    break;
                }
            }

            // Read MSQ block
            block = BlockFactory.read(byteStream, end - pos, decrypt);
            blocks.add(block);

            // Check if end has been reached
            if (end == bytes.length)
            {
                eof = true;
            }
            else
            {
                // If it's not the end then skip the next MSQ block
                pos = end + 4;
                byteStream.skip(4);
            }
        }
        return new Game(blocks, disk);
    }


    /**
     * Writes the encrypted game data to a stream.
     * 
     * @param stream
     *            The output stream
     * @throws IOException
     */

    public void write(OutputStream stream) throws IOException
    {
        write(stream, true);
    }


    /**
     * Writes the game data to a stream. If the data should be encrypted or not
     * can be selected with a parameter.
     * 
     * @param stream
     *            The output stream
     * @param encrypt
     *            If the writtendata should be encrypted
     * @throws IOException
     */

    public void write(OutputStream stream, boolean encrypt)
        throws IOException
    {
        for (GameBlock block: this.blocks)
        {
            stream.write('m');
            stream.write('s');
            stream.write('q');
            stream.write('0' + this.disk);
            block.write(stream, encrypt);
        }
    }


    /**
     * Returns the game blocks.
     * 
     * @return The game blocks
     */

    public List<GameBlock> getBlocks()
    {
        return this.blocks;
    }


    /**
     * Returns the disk number.
     *
     * @return The disk number
     */
    
    public int getDisk()
    {
        return this.disk;
    }
}
