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


/**
 * Game Block
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class GameBlock
{
    /** The block bytes */
    protected byte[] bytes;

    /** The block type */
    protected GameBlockType type;

    /** The map size. Only when block type is "map" */
    protected int mapSize;


    /**
     * Constructor
     * 
     * @param bytes
     *            The block bytes
     */

    public GameBlock(byte[] bytes)
    {
        this.bytes = bytes;
        this.type = getType(this.bytes);
        if (this.type == GameBlockType.map)
        {
            this.mapSize = getMapSize(this.bytes);
        }
        else
        {
            this.mapSize = 0;
        }
    }


    /**
     * Constructor
     * 
     * @param bytes
     *            The block bytes
     * @param type
     *            The block type
     * @param mapSize
     *            The map size
     */

    public GameBlock(byte[] bytes, GameBlockType type, int mapSize)
    {
        this.bytes = bytes;
        this.type = type;
        this.mapSize = mapSize;
    }


    /**
     * Reads a game block from the specified input stream.
     * 
     * @param stream
     *            The input stream
     * @param disk
     *            The disk id
     * @param blockId
     *            The block id
     * @param size
     *            The block size
     * @return The game block
     * @throws IOException
     */

    public static GameBlock read(InputStream stream, int disk, int blockId,
        int size) throws IOException
    {
        RotatingXorInputStream gameStream;
        byte[] rawBytes;
        byte[] bytes;
        int read;
        int checksum, endChecksum;
        GameBlockType type;
        int mapSize = 0;
        int offset;
        int encSize;

        // Read the raw block
        rawBytes = new byte[size];
        read = stream.read(rawBytes);
        if (read != size)
        {
            throw new IOException("Unable to read block " + blockId
                + " of game file " + disk + ". Need to read " + size
                + " bytes but only got " + read);
        }

        // Decrypt the whole block from the stream
        stream = new ByteArrayInputStream(rawBytes);
        gameStream = new RotatingXorInputStream(stream);
        size -= 2;
        bytes = new byte[size];
        gameStream.read(bytes);

        // Determine the block type
        type = getType(bytes);

        // And if the block is a map then we determine the size of the
        // encrypted block and read the data again
        if (type == GameBlockType.map)
        {
            // Get map size, calculate string offset position and use it
            // as the size of the encrypted block
            mapSize = getMapSize(bytes);
            offset = mapSize * mapSize * 3 / 2;
            encSize = ((bytes[offset] & 0xff) | ((bytes[offset + 1] & 0xff) << 8));

            stream = new ByteArrayInputStream(rawBytes);

            // Read the encrypted part of the data
            gameStream = new RotatingXorInputStream(stream);
            gameStream.read(bytes, 0, encSize);

            // Read the unecrypted part of the data
            stream.read(bytes, encSize, bytes.length - encSize);
        }
        else if (type == GameBlockType.savegame)
        {
            encSize = 0x800;

            stream = new ByteArrayInputStream(rawBytes);

            // Read the encrypted part of the data
            gameStream = new RotatingXorInputStream(stream);
            gameStream.read(bytes, 0, encSize);

            // Read the unecrypted part of the data
            stream.read(bytes, encSize, bytes.length - encSize);
        }

        // Check the checksum
        checksum = gameStream.getChecksum();
        endChecksum = gameStream.getEndChecksum();
        System.out.println(endChecksum);
        if (endChecksum != checksum)
        {
            throw new IOException("Checksum error! Expected " + endChecksum
                + " but got " + checksum);
        }
        return new GameBlock(bytes, type, mapSize);
    }


    /**
     * Writes the game block to the specified output stream.
     * 
     * @param stream
     *            The output stream
     * @throws IOException
     */

    public void write(OutputStream stream) throws IOException
    {
        OutputStream gameStream;
        int encSize;
        int mapSize;
        int offset;

        // Determine the size of the encrypted part of the block
        if (this.type == GameBlockType.map)
        {
            // Only the stuff before the strings is encrypted. So we get
            // the string offset and use it as the size.
            mapSize = this.mapSize;
            offset = mapSize * mapSize * 3 / 2;
            encSize = ((this.bytes[offset] & 0xff) | ((this.bytes[offset + 1] & 0xff) << 8));
        }
        else if (this.type == GameBlockType.savegame)
        {
            // Only the first 0x800 bytes of the savegame block is encoded
            encSize = 0x800;
        }
        else
        {
            // Other blocks are completely encrypted
            encSize = this.bytes.length;
        }

        // Write the encrypted data
        gameStream = new RotatingXorOutputStream(stream);
        gameStream.write(this.bytes, 0, encSize);
        gameStream.flush();

        // Write the unencrypted data
        if (encSize < this.bytes.length)
        {
            stream.write(this.bytes, encSize, this.bytes.length - encSize);
        }
    }


    /**
     * Checks if the specified byte array represents a Wasteland save game. Save
     * games are discovered by the block size and by the byte offsets 1-8 which
     * represents the character order and must contain values between 0 and 7
     * while all non-zero numbers can only occur once.
     * 
     * @param bytes
     *            The byte array to check
     * @return If it's a save game or not
     */

    private static boolean isSaveGame(byte[] bytes)
    {
        List<Integer> seen;
        byte b;

        seen = new ArrayList<Integer>(7);
        if (bytes.length == 4608)
        {
            for (int i = 1; i < 8; i++)
            {
                b = bytes[i];
                if (b > 7) return false;
                if (b != 0 && seen.contains(Integer.valueOf(b))) return false;
                seen.add(Integer.valueOf(b));
            }
            return true;
        }
        return false;
    }

    /**
     * Checks if the specified byte array represents an unknown block which is
     * one of the blocks following the save game block. Purpose of these blocks
     * is currently unknown.
     * 
     * @param bytes
     *            The byte array to check
     * @return If it's an unknwon block or not
     */

    private static boolean isUnknown(byte[] bytes)
    {
        if (bytes.length == 760)
        {
            if (bytes[0] == 0x60 && bytes[1] == 0x60 && bytes[2] == 0x60)
            {
                return true;
            }
            return false;
        }
        return false;
    }


    /**
     * Returns the game block type of the specified block data.
     * 
     * @param bytes
     *            The block data
     * @return The game block type
     */

    private static GameBlockType getType(byte[] bytes)
    {
        if (isSaveGame(bytes))
        {
            return GameBlockType.savegame;
        }
        else if (isUnknown(bytes))
        {
            return GameBlockType.unknown;
        }
        else
        {
            return GameBlockType.map;
        }
    }


    /**
     * Returns the map size. This method does not look for the map size in the
     * EXE file. Instead it tries to "guess" it by looking at some
     * characteristics of the byte array. This is not totaly safe but it works
     * fine.
     * 
     * If the map size could not be determined then a GameException is thrown.
     * 
     * @param bytes
     *            The bytes of the map block
     * @return The map size
     * @throws GameException
     *             If size could not be determined
     */

    private static int getMapSize(byte[] bytes) throws GameException
    {
        int start;
        int offset;

        // Cycle over possible map sizes
        size: for (int size = 32; size <= 64; size *= 2)
        {
            // Calculate start of central directory
            start = size * size * 3 / 2;

            // Read 19 offsets of the central directory and validate them
            for (int i = 0; i < 19; i++)
            {
                // Read offset
                try
                {
                    offset = (bytes[start + i * 2] & 0xff)
                        | ((bytes[start + i * 2 + 1] & 0xff) << 8);
                }
                catch (IndexOutOfBoundsException e)
                {
                    // Out of bounds? Size must be wrong
                    continue size;
                }

                // Validate offset
                if (offset != 0 && (offset < start || offset > bytes.length))
                {
                    continue size;
                }
            }

            // Everything looks fine. This size is correct
            return size;
        }

        // Found no valid size? Strange. Throw exception
        throw new GameException("Unable to determine map size");
    }


    /**
     * Returns the block bytes.
     * 
     * @return The block bytes
     */

    public byte[] getBytes()
    {
        return this.bytes;
    }
}
