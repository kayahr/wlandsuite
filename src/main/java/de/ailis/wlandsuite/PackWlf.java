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

package de.ailis.wlandsuite;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import de.ailis.wlandsuite.cli.PackProg;
import de.ailis.wlandsuite.wlf.Wlf;
import de.ailis.wlandsuite.wlf.WlfMask;


/**
 * Packs bit mask files from a directory into a Wasteland WLF file.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class PackWlf extends PackProg
{
    /**
     * @see de.ailis.wlandsuite.cli.PackProg#pack(java.io.File, java.io.OutputStream)
     */
    
    @Override
    protected void pack(File directory, OutputStream output)
        throws IOException
    {
        List<WlfMask> masks;
        int maskNo;
        File maskFile;
        
        // Read the animation frames
        masks = new ArrayList<WlfMask>();
        maskNo = 0;
        while (true)
        {
            maskFile = new File(String.format("%s%c%03d.png", new Object[] {
                directory.getPath(), File.separatorChar, maskNo }));
            if (!maskFile.exists())
            {
                break;
            }
            masks.add(new WlfMask(ImageIO.read(maskFile)));
            maskNo++;
        }
        
        new Wlf(masks).write(output);
    }


    /**
     * Main method
     * 
     * @param args
     *            Command line arguments
     */

    public static void main(String[] args)
    {
        PackWlf app;

        app = new PackWlf();
        app.setHelp("help/packwlf.txt");
        app.setProgName("packwlf");
        app.start(args);
    }
}
