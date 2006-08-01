/*
 * $Id$
 * Copyright (c) 2006 Klaus Reimer
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the WlExe class
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class WlExeTest extends TestCase
{
    /**
     * Returns the test suite.
     * 
     * @return The test suite
     */

    public static Test suite()
    {
        return new TestSuite(WlExeTest.class);
    }


    /**
     * Creates a temporary copy of the specified source file.
     * 
     * @param source
     *            The source file
     * @return The copy of the source file
     * @throws IOException
     */

    private File createCopy(File source) throws IOException
    {
        File dest;
        InputStream input;
        OutputStream output;
        byte[] buffer;
        int read;

        dest = File.createTempFile("wlandsuite", ".exe");
        buffer = new byte[8192];
        input = new FileInputStream(source);
        try
        {
            output = new FileOutputStream(dest);
            try
            {
                while ((read = input.read(buffer)) != -1)
                {
                    output.write(buffer, 0, read);
                }
            }
            finally
            {
                output.close();
            }
        }
        finally
        {
            input.close();
        }

        return dest;
    }


    /**
     * Tests getting MSQ offsets for HTDS1.
     * 
     * @throws IOException
     */

    public void testGetHTDS1Offsets() throws IOException
    {
        WlExe wl;
        List<Integer> offsets;

        wl = new WlExe(new File("src/test/resources/exe/wl.exe"));
        offsets = wl.getHtds1Offsets();
        assertEquals(4, offsets.size());
        assertEquals(0x0000, offsets.get(0).intValue());
        assertEquals(0x1402, offsets.get(1).intValue());
        assertEquals(0x3ee8, offsets.get(2).intValue());
        assertEquals(0x69fc, offsets.get(3).intValue());
        wl.close();
    }


    /**
     * Tests getting MSQ offsets for HTDS2.
     * 
     * @throws IOException
     */

    public void testGetHTDS2Offsets() throws IOException
    {
        WlExe wl;
        List<Integer> offsets;

        wl = new WlExe(new File("src/test/resources/exe/wl.exe"));
        offsets = wl.getHtds2Offsets();
        assertEquals(5, offsets.size());
        assertEquals(0x0000, offsets.get(0).intValue());
        assertEquals(0x222c, offsets.get(1).intValue());
        assertEquals(0x3c97, offsets.get(2).intValue());
        assertEquals(0x5676, offsets.get(3).intValue());
        assertEquals(0x70eb, offsets.get(4).intValue());
        wl.close();
    }


    /**
     * Tests setting MSQ offsets for HTDS1.
     * 
     * @throws IOException
     */

    public void testSetHTDS1Offsets() throws IOException
    {
        WlExe wl;
        File copy;
        File wlFile;
        List<Integer> offsets;

        wlFile = new File("src/test/resources/exe/wl.exe");
        copy = createCopy(wlFile);
        offsets = new ArrayList<Integer>(4);
        offsets.add(Integer.valueOf(0x01020304));
        offsets.add(Integer.valueOf(0x02030405));
        offsets.add(Integer.valueOf(0x03040506));
        offsets.add(Integer.valueOf(0x04050607));
        wl = new WlExe(copy);
        wl.setHtds1Offsets(offsets);
        wl.close();
        wl = new WlExe(copy);
        assertEquals(offsets, wl.getHtds1Offsets());
        wl.close();
        copy.delete();
    }


    /**
     * Tests setting MSQ offsets for HTDS2.
     * 
     * @throws IOException
     */

    public void testSetHTDS2Offsets() throws IOException
    {
        WlExe wl;
        File copy;
        File wlFile;
        List<Integer> offsets;

        wlFile = new File("src/test/resources/exe/wl.exe");
        copy = createCopy(wlFile);
        offsets = new ArrayList<Integer>(5);
        offsets.add(Integer.valueOf(0x01020304));
        offsets.add(Integer.valueOf(0x02030405));
        offsets.add(Integer.valueOf(0x03040506));
        offsets.add(Integer.valueOf(0x04050607));
        offsets.add(Integer.valueOf(0x05060708));
        wl = new WlExe(copy);
        wl.setHtds2Offsets(offsets);
        wl.close();
        wl = new WlExe(copy);
        assertEquals(offsets, wl.getHtds2Offsets());
        wl.close();
        copy.delete();
    }

    /**
     * Tests getting MSQ offsets for PICS1.
     * 
     * @throws IOException
     */

    public void testGetPics1Offsets() throws IOException
    {
        WlExe wl;
        List<Integer> offsets;

        wl = new WlExe(new File("src/test/resources/exe/wl.exe"));
        offsets = wl.getPics1Offsets();
        assertEquals(33, offsets.size());
        assertEquals(0x0, offsets.get(0).intValue());
        assertEquals(0x7e4, offsets.get(1).intValue());
        assertEquals(0x13c4, offsets.get(2).intValue());
        assertEquals(0x1e9c, offsets.get(3).intValue());
        assertEquals(0x2673, offsets.get(4).intValue());
        assertEquals(0x3390, offsets.get(5).intValue());
        assertEquals(0x3fe3, offsets.get(6).intValue());
        assertEquals(0x4e4d, offsets.get(7).intValue());
        assertEquals(0x5ebc, offsets.get(8).intValue());
        assertEquals(0x6a27, offsets.get(9).intValue());
        assertEquals(0x73b3, offsets.get(10).intValue());
        assertEquals(0x8684, offsets.get(11).intValue());
        assertEquals(0x8f64, offsets.get(12).intValue());
        assertEquals(0x9831, offsets.get(13).intValue());
        assertEquals(0xa7a7, offsets.get(14).intValue());
        assertEquals(0xb201, offsets.get(15).intValue());
        assertEquals(0xba94, offsets.get(16).intValue());
        assertEquals(0xc8fd, offsets.get(17).intValue());
        assertEquals(0xd2aa, offsets.get(18).intValue());
        assertEquals(0xde55, offsets.get(19).intValue());
        assertEquals(0xe926, offsets.get(20).intValue());
        assertEquals(0xfb9e, offsets.get(21).intValue());
        assertEquals(0x1060a, offsets.get(22).intValue());
        assertEquals(0x10eae, offsets.get(23).intValue());
        assertEquals(0x1199d, offsets.get(24).intValue());
        assertEquals(0x123af, offsets.get(25).intValue());
        assertEquals(0x15b06, offsets.get(26).intValue());
        assertEquals(0x162a1, offsets.get(27).intValue());
        assertEquals(0x16cd3, offsets.get(28).intValue());
        assertEquals(0x17544, offsets.get(29).intValue());
        assertEquals(0x17fa4, offsets.get(30).intValue());
        assertEquals(0x18a9d, offsets.get(31).intValue());
        assertEquals(0x19535, offsets.get(32).intValue());
        wl.close();
    }


    /**
     * Tests getting MSQ offsets for PICS2.
     * 
     * @throws IOException
     */

    public void testGetPics2Offsets() throws IOException
    {
        WlExe wl;
        List<Integer> offsets;

        wl = new WlExe(new File("src/test/resources/exe/wl.exe"));
        offsets = wl.getPics2Offsets();
        assertEquals(49, offsets.size());
        assertEquals(0x0, offsets.get(0).intValue());
        assertEquals(0x7d1, offsets.get(1).intValue());
        assertEquals(0x1394, offsets.get(2).intValue());
        assertEquals(0x1e6a, offsets.get(3).intValue());
        assertEquals(0x28be, offsets.get(4).intValue());
        assertEquals(0x38fe, offsets.get(5).intValue());
        assertEquals(0x41ad, offsets.get(6).intValue());
        assertEquals(0x4992, offsets.get(7).intValue());
        assertEquals(0x515e, offsets.get(8).intValue());
        assertEquals(0x5c9a, offsets.get(9).intValue());
        assertEquals(0x66f8, offsets.get(10).intValue());
        assertEquals(0x7287, offsets.get(11).intValue());
        assertEquals(0x7bbc, offsets.get(12).intValue());
        assertEquals(0x8871, offsets.get(13).intValue());
        assertEquals(0x8e3d, offsets.get(14).intValue());
        assertEquals(0x9927, offsets.get(15).intValue());
        assertEquals(0xa2d6, offsets.get(16).intValue());
        assertEquals(0xac0d, offsets.get(17).intValue());
        assertEquals(0xb758, offsets.get(18).intValue());
        assertEquals(0xc12e, offsets.get(19).intValue());
        assertEquals(0xcd0f, offsets.get(20).intValue());
        assertEquals(0xd6d4, offsets.get(21).intValue());
        assertEquals(0xe671, offsets.get(22).intValue());
        assertEquals(0xef36, offsets.get(23).intValue());
        assertEquals(0xf88d, offsets.get(24).intValue());
        assertEquals(0x1009b, offsets.get(25).intValue());
        assertEquals(0x10acf, offsets.get(26).intValue());
        assertEquals(0x11a37, offsets.get(27).intValue());
        assertEquals(0x122fe, offsets.get(28).intValue());
        assertEquals(0x12d71, offsets.get(29).intValue());
        assertEquals(0x13686, offsets.get(30).intValue());
        assertEquals(0x14282, offsets.get(31).intValue());
        assertEquals(0x14d49, offsets.get(32).intValue());
        assertEquals(0x15721, offsets.get(33).intValue());
        assertEquals(0x1653b, offsets.get(34).intValue());
        assertEquals(0x16f6f, offsets.get(35).intValue());
        assertEquals(0x1797a, offsets.get(36).intValue());
        assertEquals(0x18688, offsets.get(37).intValue());
        assertEquals(0x19451, offsets.get(38).intValue());
        assertEquals(0x19dd4, offsets.get(39).intValue());
        assertEquals(0x1a50d, offsets.get(40).intValue());
        assertEquals(0x1aefe, offsets.get(41).intValue());
        assertEquals(0x1bcc6, offsets.get(42).intValue());
        assertEquals(0x1c931, offsets.get(43).intValue());
        assertEquals(0x1d4e0, offsets.get(44).intValue());
        assertEquals(0x1dda9, offsets.get(45).intValue());
        assertEquals(0x1eadf, offsets.get(46).intValue());
        assertEquals(0x1f49f, offsets.get(47).intValue());
        assertEquals(0x1fe8f, offsets.get(48).intValue());
        wl.close();
    }


    /**
     * Tests setting MSQ offsets for PICS1.
     * 
     * @throws IOException
     */

    public void testSetPics1Offsets() throws IOException
    {
        WlExe wl;
        File copy;
        File wlFile;
        List<Integer> offsets;

        wlFile = new File("src/test/resources/exe/wl.exe");
        copy = createCopy(wlFile);
        offsets = new ArrayList<Integer>(33);
        offsets.add(Integer.valueOf(0x01020304));
        offsets.add(Integer.valueOf(0x02030405));
        offsets.add(Integer.valueOf(0x03040506));
        offsets.add(Integer.valueOf(0x04050607));
        offsets.add(Integer.valueOf(0x05060708));
        offsets.add(Integer.valueOf(0x01020304));
        offsets.add(Integer.valueOf(0x02030405));
        offsets.add(Integer.valueOf(0x03040506));
        offsets.add(Integer.valueOf(0x04050607));
        offsets.add(Integer.valueOf(0x05060708));
        offsets.add(Integer.valueOf(0x01020304));
        offsets.add(Integer.valueOf(0x02030405));
        offsets.add(Integer.valueOf(0x03040506));
        offsets.add(Integer.valueOf(0x04050607));
        offsets.add(Integer.valueOf(0x05060708));
        offsets.add(Integer.valueOf(0x01020304));
        offsets.add(Integer.valueOf(0x02030405));
        offsets.add(Integer.valueOf(0x03040506));
        offsets.add(Integer.valueOf(0x04050607));
        offsets.add(Integer.valueOf(0x05060708));
        offsets.add(Integer.valueOf(0x01020304));
        offsets.add(Integer.valueOf(0x02030405));
        offsets.add(Integer.valueOf(0x03040506));
        offsets.add(Integer.valueOf(0x04050607));
        offsets.add(Integer.valueOf(0x05060708));
        offsets.add(Integer.valueOf(0x01020304));
        offsets.add(Integer.valueOf(0x02030405));
        offsets.add(Integer.valueOf(0x03040506));
        offsets.add(Integer.valueOf(0x04050607));
        offsets.add(Integer.valueOf(0x05060708));
        offsets.add(Integer.valueOf(0x01020304));
        offsets.add(Integer.valueOf(0x02030405));
        offsets.add(Integer.valueOf(0x03040506));
        wl = new WlExe(copy);
        wl.setPics1Offsets(offsets);
        wl.close();
        wl = new WlExe(copy);
        assertEquals(offsets, wl.getPics1Offsets());
        wl.close();
        copy.delete();
    }


    /**
     * Tests setting MSQ offsets for PICS2.
     * 
     * @throws IOException
     */

    public void testSetPics2Offsets() throws IOException
    {
        WlExe wl;
        File copy;
        File wlFile;
        List<Integer> offsets;

        wlFile = new File("src/test/resources/exe/wl.exe");
        copy = createCopy(wlFile);
        offsets = new ArrayList<Integer>(49);
        offsets.add(Integer.valueOf(0x01020304));
        offsets.add(Integer.valueOf(0x02030405));
        offsets.add(Integer.valueOf(0x03040506));
        offsets.add(Integer.valueOf(0x04050607));
        offsets.add(Integer.valueOf(0x05060708));
        offsets.add(Integer.valueOf(0x01020304));
        offsets.add(Integer.valueOf(0x02030405));
        offsets.add(Integer.valueOf(0x03040506));
        offsets.add(Integer.valueOf(0x04050607));
        offsets.add(Integer.valueOf(0x05060708));
        offsets.add(Integer.valueOf(0x01020304));
        offsets.add(Integer.valueOf(0x02030405));
        offsets.add(Integer.valueOf(0x03040506));
        offsets.add(Integer.valueOf(0x04050607));
        offsets.add(Integer.valueOf(0x05060708));
        offsets.add(Integer.valueOf(0x01020304));
        offsets.add(Integer.valueOf(0x02030405));
        offsets.add(Integer.valueOf(0x03040506));
        offsets.add(Integer.valueOf(0x04050607));
        offsets.add(Integer.valueOf(0x05060708));
        offsets.add(Integer.valueOf(0x01020304));
        offsets.add(Integer.valueOf(0x02030405));
        offsets.add(Integer.valueOf(0x03040506));
        offsets.add(Integer.valueOf(0x04050607));
        offsets.add(Integer.valueOf(0x05060708));
        offsets.add(Integer.valueOf(0x01020304));
        offsets.add(Integer.valueOf(0x02030405));
        offsets.add(Integer.valueOf(0x03040506));
        offsets.add(Integer.valueOf(0x04050607));
        offsets.add(Integer.valueOf(0x05060708));
        offsets.add(Integer.valueOf(0x01020304));
        offsets.add(Integer.valueOf(0x02030405));
        offsets.add(Integer.valueOf(0x03040506));
        offsets.add(Integer.valueOf(0x04050607));
        offsets.add(Integer.valueOf(0x05060708));
        offsets.add(Integer.valueOf(0x01020304));
        offsets.add(Integer.valueOf(0x02030405));
        offsets.add(Integer.valueOf(0x03040506));
        offsets.add(Integer.valueOf(0x04050607));
        offsets.add(Integer.valueOf(0x05060708));
        offsets.add(Integer.valueOf(0x01020304));
        offsets.add(Integer.valueOf(0x02030405));
        offsets.add(Integer.valueOf(0x03040506));
        offsets.add(Integer.valueOf(0x04050607));
        offsets.add(Integer.valueOf(0x05060708));
        offsets.add(Integer.valueOf(0x01020304));
        offsets.add(Integer.valueOf(0x02030405));
        offsets.add(Integer.valueOf(0x03040506));
        offsets.add(Integer.valueOf(0x04050607));
        wl = new WlExe(copy);
        wl.setPics2Offsets(offsets);
        wl.close();
        wl = new WlExe(copy);
        assertEquals(offsets, wl.getPics2Offsets());
        wl.close();
        copy.delete();
    }
}
