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

package de.ailis.wlandsuite.exe;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;


/**
 * Provides methods for reading and writing special bytes from the unpacked
 * wl.exe file.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class WlExe
{
    /** The random access to wl.exe */
    private RandomAccessFile file;


    /**
     * Constructor. If you create this object then you must call the close()
     * method if you are finished with it.
     * 
     * @param file
     *            The wl.exe
     * @throws IOException
     */

    public WlExe(File file) throws IOException
    {
        if (file.length() != 169488 && file.length() != 169824)
        {
            throw new IOException(
                "wl.exe is not 169488 bytes long. Maybe it's still packed?");
        }
        this.file = new RandomAccessFile(file, "rw");
    }


    /**
     * Closes the random access file.
     * 
     * @throws IOException
     */

    public void close() throws IOException
    {
        this.file.close();
    }


    /**
     * Returns the MSQ offsets of the first tileset file.
     * 
     * @return The MSQ offsets
     * @throws IOException
     */

    public List<Integer> getHtds1Offsets() throws IOException
    {
        List<Integer> offsets;

        offsets = new ArrayList<Integer>(4);
        this.file.seek(0x18ccc);
        offsets.add(Integer.valueOf(readOffset()));
        offsets.add(Integer.valueOf(readOffset()));
        offsets.add(Integer.valueOf(readOffset()));
        offsets.add(Integer.valueOf(readOffset()));
        return offsets;
    }


    /**
     * Sets the MSQ offsets for the first tileset file.
     * 
     * @param offsets
     *            The MSQ offsets
     * @throws IOException
     */

    public void setHtds1Offsets(List<Integer> offsets) throws IOException
    {
        if (offsets.size() != 4)
        {
            throw new IOException("HTDS1 needs 4 offsets but tried to write "
                + offsets.size() + " offsets");
        }
        this.file.seek(0x18ccc);
        for (Integer offset: offsets)
        {
            writeOffset(offset.intValue());
        }
    }


    /**
     * Returns the MSQ offsets of the second tileset file.
     * 
     * @return The MSQ offsets
     * @throws IOException
     */

    public List<Integer> getHtds2Offsets() throws IOException
    {
        List<Integer> offsets;

        offsets = new ArrayList<Integer>(5);
        this.file.seek(0x18cdc);
        offsets.add(Integer.valueOf(readOffset() - 0x8603));
        offsets.add(Integer.valueOf(readOffset() - 0x8603));
        offsets.add(Integer.valueOf(readOffset() - 0x8603));
        offsets.add(Integer.valueOf(readOffset() - 0x8603));
        offsets.add(Integer.valueOf(readOffset() - 0x8603));
        return offsets;
    }


    /**
     * Sets the MSQ offsets for the second tileset file.
     * 
     * @param offsets
     *            The MSQ offsets
     * @throws IOException
     */

    public void setHtds2Offsets(List<Integer> offsets) throws IOException
    {
        if (offsets.size() != 5)
        {
            throw new IOException("HTDS2 needs 5 offsets but tried to write "
                + offsets.size() + " offsets");
        }
        this.file.seek(0x18cdc);
        for (Integer offset: offsets)
        {
            writeOffset(offset.intValue() + 0x8603);
        }
    }


    /**
     * Reads an offset from the current position.
     * 
     * @return The offset
     * @throws IOException
     */

    private int readOffset() throws IOException
    {
        int ch1 = this.file.read();
        int ch2 = this.file.read();
        int ch3 = this.file.read();
        int ch4 = this.file.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0) throw new EOFException();
        return ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
    }


    /**
     * Writes an offset to the current position
     * 
     * @param offset
     *            The offset to write
     * @throws IOException
     */

    private final void writeOffset(int offset) throws IOException
    {
        this.file.write((offset >>> 0) & 0xFF);
        this.file.write((offset >>> 8) & 0xFF);
        this.file.write((offset >>> 16) & 0xFF);
        this.file.write((offset >>> 24) & 0xFF);
    }
    
    
    /**
     * Returns the MSQ offsets of the first pics file.
     * 
     * @return The MSQ offsets
     * @throws IOException
     */

    public List<Integer> getPics1Offsets() throws IOException
    {
        List<Integer> offsets;
        int offset;

        offsets = new ArrayList<Integer>(33);
        this.file.seek(0x18960);
        for (int i = 0; i < 34; i++)
        {
            offset = readOffset();
            
            // Ignore the 26th offset. It's always the same as the 25th. 
            // Bug in the game?
            if (i == 26) continue;
            
            offsets.add(Integer.valueOf(offset));
        }
        return offsets;
    }


    /**
     * Sets the MSQ offsets for the first pics file.
     * 
     * @param offsets
     *            The MSQ offsets
     * @throws IOException
     */

    public void setPics1Offsets(List<Integer> offsets) throws IOException
    {
        int index;
        
        if (offsets.size() != 33)
        {
            throw new IOException("PICS1 needs 33 offsets but tried to write "
                + offsets.size() + " offsets");
        }
        this.file.seek(0x18960);
        index = 0;
        for (Integer offset: offsets)
        {
            // Write the 25th offset twice. Bug in the game? 
            if (index == 25)
            {
                writeOffset(offset.intValue());
            }
            writeOffset(offset.intValue());
            index++;
        }
    }
    
    
    /**
     * Returns the MSQ offsets of the second pics file.
     * 
     * @return The MSQ offsets
     * @throws IOException
     */

    public List<Integer> getPics2Offsets() throws IOException
    {
        List<Integer> offsets;

        offsets = new ArrayList<Integer>(49);
        this.file.seek(0x189e8);
        for (int i = 0; i < 49; i++)
        {
            offsets.add(Integer.valueOf(readOffset()));
        }
        return offsets;
    }


    /**
     * Sets the MSQ offsets for the second pics file.
     * 
     * @param offsets
     *            The MSQ offsets
     * @throws IOException
     */

    public void setPics2Offsets(List<Integer> offsets) throws IOException
    {
        if (offsets.size() != 49)
        {
            throw new IOException("PICS2 needs 49 offsets but tried to write "
                + offsets.size() + " offsets");
        }
        this.file.seek(0x189e8);
        for (Integer offset: offsets)
        {
            writeOffset(offset.intValue());
        }
    }
}
