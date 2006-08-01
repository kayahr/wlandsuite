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

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;

import de.ailis.wlandsuite.cli.UnpackProg;
import de.ailis.wlandsuite.htds.Htds;
import de.ailis.wlandsuite.htds.HtdsTileset;
import de.ailis.wlandsuite.pic.Pic;


/**
 * Unpacks tiles from a HTDS file into a directory.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class UnpackHtds extends UnpackProg
{
    /** The width */
    private int width = 16;

    /** The height */
    private int height = 16;


    /**
     * @see de.ailis.wlandsuite.cli.CLIProg#processOption(int,
     *      gnu.getopt.Getopt)
     */

    @Override
    protected void processOption(int opt, Getopt getopt)
    {
        switch (opt)
        {
            case 'W':
                this.width = Integer.parseInt(getopt.getOptarg());
                break;
            case 'H':
                this.height = Integer.parseInt(getopt.getOptarg());
                break;
        }

    }


    /**
     * @see de.ailis.wlandsuite.cli.UnpackProg#unpack(java.io.InputStream,
     *      java.io.File)
     */

    @Override
    public void unpack(InputStream input, File directory) throws IOException
    {
        Htds htds;
        List<HtdsTileset> tilesets;
        int tilesetNo, tileNo;
        File bankDir;
        
        htds = Htds.read(input, this.width, this.height);
        tilesets = htds.getTilesets();       
        tilesetNo = 0;
        for (HtdsTileset tileset: tilesets)
        {
            bankDir = new File(String.format("%s%c%03d", new Object[] { 
                directory.getPath(), File.separatorChar,  tilesetNo}));
            bankDir.mkdirs();
            tileNo = 0;
            for (Pic tile: tileset.getTiles())
            {
                ImageIO.write(tile, "PNG", new File(String.format("%s%c%03d.png",
                    new Object[] { bankDir.getPath(),
                    File.separatorChar, tileNo})));
                tileNo++;
            }
            tilesetNo++;
        }
    }


    /**
     * Main method
     * 
     * @param args
     *            Command line arguments
     */

    public static void main(String[] args)
    {
        UnpackHtds app;
        LongOpt[] longOpts;

        longOpts = new LongOpt[2];
        longOpts[0] = new LongOpt("width", LongOpt.REQUIRED_ARGUMENT, null, 'W');
        longOpts[1] = new LongOpt("height", LongOpt.REQUIRED_ARGUMENT, null,
            'H');

        app = new UnpackHtds();
        app.setHelp("help/unpackhtds.txt");
        app.setProgName("unpackhtds");
        app.setLongOpts(longOpts);
        app.start(args);
    }
}
