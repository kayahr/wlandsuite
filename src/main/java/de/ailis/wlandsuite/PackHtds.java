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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import de.ailis.wlandsuite.cli.PackProg;
import de.ailis.wlandsuite.htds.Htds;
import de.ailis.wlandsuite.htds.HtdsTileset;
import de.ailis.wlandsuite.pic.Pic;


/**
 * Packs tiles from a directory into a HTDS file.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class PackHtds extends PackProg
{
    /** The disk index */
    private byte disk = -1;


    /**
     * @see de.ailis.wlandsuite.cli.CLIProg#processOption(int,
     *      gnu.getopt.Getopt)
     */

    @Override
    protected void processOption(int opt, Getopt getopt)
    {
        switch (opt)
        {
            case 'f':
                this.disk = Byte.parseByte(getopt.getOptarg());
                break;
        }

    }


    /**
     * @see de.ailis.wlandsuite.cli.PackProg#pack(java.io.File,
     *      java.io.OutputStream)
     */

    @Override
    public void pack(File directory, OutputStream output) throws IOException
    {
        Htds htds;
        List<HtdsTileset> tilesets;
        List<Pic> tiles;
        int tilesetNo, tileNo;
        File file;
        File tilesetDir;

        tilesets = new ArrayList<HtdsTileset>();
        tilesetNo = 0;
        while (true)
        {
            tileNo = 0;
            tilesetDir = new File(String.format("%s%c%03d", new Object[] {
                directory.getPath(), File.separatorChar, tilesetNo}));
            if (!tilesetDir.exists())
            {
                break;
            }
            tiles = new ArrayList<Pic>();
            while (true)
            {
                file = new File(String.format("%s%c%03d.png", new Object[]
                                                                         { 
                    tilesetDir.getPath(), File.separatorChar, tileNo}));
                if (!file.exists())
                {
                    break;
                }
                tiles.add(new Pic(ImageIO.read(file)));
                tileNo++;
            }
            tilesets.add(new HtdsTileset(tiles));
            tilesetNo++;
        }
        
        htds = new Htds(tilesets);
        
        if (this.disk == -1)
        {
            htds.write(output);
        }
        else
        {
            htds.write(output, this.disk);
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
        PackHtds app;
        LongOpt[] longOpts;

        longOpts = new LongOpt[1];
        longOpts[0] = new LongOpt("disk", LongOpt.REQUIRED_ARGUMENT,
            null, 'D');

        app = new PackHtds();
        app.setHelp("help/packhtds.txt");
        app.setProgName("packhtds");
        app.setLongOpts(longOpts);
        app.start(args);
    }
}
