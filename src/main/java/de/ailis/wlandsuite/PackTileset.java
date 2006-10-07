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
import de.ailis.wlandsuite.htds.HtdsTileset;
import de.ailis.wlandsuite.pic.Pic;


/**
 * Packs tiles from a directory into an external tileset file compatible to
 * Displacer's hacked EXE.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class PackTileset extends PackProg
{
    /**
     * @see de.ailis.wlandsuite.cli.PackProg#pack(java.io.File,
     *      java.io.OutputStream)
     */

    @Override
    public void pack(File directory, OutputStream output) throws IOException
    {
        List<Pic> tiles;
        HtdsTileset tileset;
        int tileNo;
        File file;

        tiles = new ArrayList<Pic>();
        tileNo = 0;
        while (true)
        {
            file = new File(String.format("%s%c%03d.png", new Object[] {
                directory.getPath(), File.separatorChar, tileNo }));
            if (!file.exists())
            {
                break;
            }
            tiles.add(new Pic(ImageIO.read(file)));
            tileNo++;
        }
        tileset = new HtdsTileset(tiles);
        tileset.writeHacked(output);
    }


    /**
     * Main method
     * 
     * @param args
     *            Command line arguments
     */

    public static void main(String[] args)
    {
        PackTileset app;

        app = new PackTileset();
        app.setHelp("help/packtileset.txt");
        app.setProgName("packtileset");
        app.start(args);
    }
}