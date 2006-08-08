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

import java.io.IOException;
import java.io.InputStream;

import de.ailis.wlandsuite.msq.MsqHeader;
import de.ailis.wlandsuite.msq.MsqType;


/**
 * Game Map
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class GameMap
{
    /**
     * Reads a game map from the specified input stream.
     * 
     * @param stream
     *            The input stream
     * @return The game map
     * @throws IOException
     */

    public static GameMap read(InputStream stream) throws IOException
    {
        GameMap map;
        MsqHeader header;
        RotatingXorInputStream gameStream;
        int stringsOffset;
        int monsterNamesOffset;
        int monsterDataOffset;
        int[] actionClassOffsets;
        int offset;
        int unknown;
        int tmp;

        map = new GameMap();

        stream.skip(0x55c3);

        // Read and validate MSQ header
        header = MsqHeader.read(stream);
        if (header.getType() != MsqType.Uncompressed)
        {
            throw new IOException("Expected game MSQ block to be uncompressed");
        }

        // Wrap the stream into a game stream for Rotating XOR decoding
        gameStream = new RotatingXorInputStream(stream);
        offset = 0;

        // Read tile action classes
        for (int y = 0; y < 64 / 2; y++)
        {
            for (int x = 0; x < 32 / 2; x++)
            {
                String c = String.format("%02x", new Object[] { gameStream
                    .read() });
                for (int i = 0; i < 2; i++)
                {
                    String c2 = c.substring(i, i + 1);
                    if (c2.equals("0"))
                    {
                        System.out.print("   ");
                    }
                    else
                    {
                        System.out.print(c2 + c2 + " ");
                    }
                }
                offset++;
            }
            System.out.println();
        }

        // Read tile action selectors
        for (int y = 0; y < 64 / 2; y++)
        {
            for (int x = 0; x < 64 / 2; x++)
            {
                String c = String.format("%02x", new Object[] { gameStream
                    .read() });
                if (c.equals("00"))
                {
                    System.out.print("   ");
                }
                else
                {
                    System.out.print(c + " ");
                }
                offset++;
            }
            System.out.println();
        }

        // Read string block offset
        stringsOffset = gameStream.readWord();
        System.out.println("Strings offset: " + stringsOffset);
        offset += 2;

        // Read monster names offset
        monsterNamesOffset = gameStream.readWord();
        System.out.println("Monster names offset: " + monsterNamesOffset);
        offset += 2;

        // Read monster data offset
        monsterDataOffset = gameStream.readWord();
        System.out.println("Monster data offset: " + monsterDataOffset);
        offset += 2;

        // Read action class offsets
        actionClassOffsets = new int[16];
        for (int i = 0; i < 16; i++)
        {
            actionClassOffsets[i] = gameStream.readWord();
            System.out.println("Action class " + i + " offset: "
                + actionClassOffsets[i]);
            offset += 2;
        }

        System.out.println();

        // Read action class data
        for (int i = 0; i < 16; i++)
        {
            // Ignore unused action classes
            if (actionClassOffsets[i] == 0) continue;

            // Read unknown bytes until we hit the next interesting offset
            System.out.print("Unkown bytes: ");
            while (offset < actionClassOffsets[i])
            {
                unknown = gameStream.read();
                System.out.format("%02x ", new Object[] { unknown });
                offset++;
            }
            System.out.println();

            tmp = gameStream.readWord();
            int quantity = (tmp - offset) / 2;
            System.out.println("Action class " + i + " (Offset "
                + String.format("%04x", new Object[] { offset })
                + ", Actions: " + quantity + ")");
            offset += 2;
            /*
             * for (int j = 1; j < quantity; j++) { tmp = gameStream.readWord();
             * offset += 2; }
             */
        }

        while (offset != stringsOffset)
        {
            gameStream.read();
            offset++;
        }

        System.out.println();
        System.out.println("Current offset: " + offset);

        byte[] charTable = new byte[60];
        gameStream.read(charTable);
        offset += 60;

        tmp = gameStream.readWord();
        System.out.println(tmp);
        int strings = tmp / 2;
        System.out.println("Number of strings: " + strings);
        offset += 2;
        int[] stringOffsets = new int[strings];
        stringOffsets[0] = tmp;
        System.out.println("String offset 0: " + tmp);
        for (int i = 1; i < strings; i++)
        {
            stringOffsets[i] = gameStream.readWord();
            offset += 2;
            System.out.println("String offset " + i + ": " + stringOffsets[i]);
        }

        for (int j = 0; j < strings; j++)
        {
            System.out.print("String at offset " + offset + ": ");
            int bits = 0;
            boolean upper = false;
            boolean high = false;
            int len;
            if (j >= strings - 1)
                len = 127;
            else
                len = (stringOffsets[j + 1] - stringOffsets[j]) * 8 / 5;
            for (int i = 0; i < len; i++)
            {
                int index = gameStream.readBits(5, true);
                switch (index)
                {
                    case 0x1f:
                        high = true;
                        break;

                    case 0x1e:
                        upper = true;
                        break;

                    default:
                        int character = charTable[index + (high ? 0x1e : 0)];
                        String s;
                        if (character >= 0x20)
                        {
                            s = new String(new byte[] { (byte) character });
                        }
                        else
                        {
                            s = String.format("\\%02x",
                                new Object[] { character });
                        }
                        if (upper) s = s.toUpperCase();
                        System.out.print(s);
                        upper = false;
                        high = false;
                }
                bits += 5;
            }
            offset += bits / 8;
            if (bits % 8 > 0)
            {
                System.out.print(" / Remaining bits: " + (bits % 8) + " = ");
                System.out.println(gameStream.readBits(8 - (bits % 8), true));
                offset++;
            }
            System.out.println();
        }
        System.out.println("Current offset: " + offset);

        return true ? null : map;
    }
}
