import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

import de.ailis.wlandsuite.exe.WlExe;
import de.ailis.wlandsuite.game.chartable.CharTable;
import de.ailis.wlandsuite.game.parts.Strings;
import de.ailis.wlandsuite.io.SeekableInputStream;
import de.ailis.wlandsuite.utils.StringUtils;

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


/**
 * Test
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Test
{

    /**
     * TODO Document me!
     * 
     * @param args
     * @throws IOException
     */

    public static void main(String[] args) throws IOException
    {
        File file;
        WlExe exe;

        file = new File("/home/k.reimer/.dosemu/msdos/games/wland/wl.exe");
        exe = new WlExe(file);
        SeekableInputStream stream = new SeekableInputStream(
            new FileInputStream(file));

        int offsets[] = { 0xa703, 0xab3e, 0xb270, 0xce4b, 0xd622, 0xdacc,
            0xdbf8, 0xdced };
        int quantity[] = { 18, 106, 170, 7, 63, 11, 10, 25 };

        int si = 0;
        for (int segoffset: offsets)
        {
            stream.seek(exe.getSeg2Offset() + segoffset);
            String filename = String.format("seg2_%04x", new Object[] { segoffset});
            System.out.println("Processing " + filename);
            
            Strings strings = Strings.read(stream, (int) (file.length()));
            while (strings.size() > quantity[si])
            {
                strings.remove(strings.size() - 1);
            }
            
            PrintWriter writer = new PrintWriter(new FileOutputStream(filename + ".txt"));
            for (String string: strings)
            {
                writer.println(StringUtils.escape(string, "ASCII"));
            }
            writer.close();            
            ByteArrayOutputStream index = new ByteArrayOutputStream();
            ByteArrayOutputStream stringBuffer = new ByteArrayOutputStream();
            int i = 0;
            for (String string: strings)
            {
                if (i % 4 == 0)
                {
                    int offset = stringBuffer.size() + (strings.size() * 2 / 4);
                    index.write(offset & 0xff);
                    index.write(offset >> 8);
                }
                stringBuffer.write(string.getBytes("ASCII"));
                stringBuffer.write(0);
                i++;
            }
            FileOutputStream fstream = new FileOutputStream(new File(filename + ".dat"));
            fstream.write(index.toByteArray());
            fstream.write(stringBuffer.toByteArray());
            
            si++;
        }
    }

}
