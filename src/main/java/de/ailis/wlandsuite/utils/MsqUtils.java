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

package de.ailis.wlandsuite.utils;



/**
 * MsqUtils
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class MsqUtils
{
    /**
     * Decodes encrypted map data. Returns a newly allocates byte array with the
     * unencrypted map data.
     * 
     * @param data
     *            The encrypted map data
     * @return The unecnrypted map data
     */

    public static byte[] decodeMapData(byte[] data)
    {
        byte[] result;
        int offset;
        int e1, e2;
        byte enc;
        int checksum;
        int endChecksum;
        byte crypted;

        result = new byte[data.length - 2];
        offset = 0;
        while (offset < data.length)
        {
            // Get encryption byte and checksum end marker
            e1 = NumberUtils.getUnsigned(data[offset]);
            e2 = NumberUtils.getUnsigned(data[offset + 1]);
            enc = (byte) (e1 ^ e2);
            endChecksum = e1 | (e2 << 8);
            offset += 2;
            byte b;

            // Initialize checksum
            checksum = 0;

            // Repeat until end of msq block
            while (checksum != endChecksum)
            {
                // Read crypted byte
                crypted = data[offset];

                // Decrypt the byte
                b = (byte) (crypted ^ enc);
                result[offset - 2] = b;

                offset++;

                // Update checksum
                checksum = (checksum - NumberUtils.getUnsigned(b)) & 0xffff;

                // Updated encryption byte
                enc += 0x1f;
            }

            // Repeat reading of unencrypted data until next msq header
            while (offset < data.length)
            {
                b = data[offset];
                result[offset - 2] = b;
                offset++;
            }
        }
        return result;
    }
}
