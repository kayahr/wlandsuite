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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import de.ailis.wlandsuite.cli.PackProg;
import de.ailis.wlandsuite.rawgame.BlockFactory;
import de.ailis.wlandsuite.rawgame.Game;
import de.ailis.wlandsuite.rawgame.blocks.GameBlock;


/**
 * Packs a GAME file.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class PackGame extends PackProg
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
            case 'D':
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
        Game game;
        List<GameBlock> blocks;
        int blockNo;
        File blockFile;
        InputStream stream;

        blocks = new ArrayList<GameBlock>();
        blockNo = 0;
        while (true)
        {
            blockFile = new File(String.format("%s%c%03d.xml", new Object[] {
                directory.getPath(), File.separatorChar, blockNo }));
            if (!blockFile.exists())
            {
                break;
            }
            stream = new FileInputStream(blockFile);
            try
            {
                blocks.add(BlockFactory.readXml(stream));
            }
            finally
            {
                stream.close();
            }
            blockNo++;
        }

        game = new Game(blocks, this.disk == -1 ? 0 : this.disk);
        game.write(output);
    }


    /**
     * Main method
     * 
     * @param args
     *            Command line arguments
     */

    public static void main(String[] args)
    {
        PackGame app;
        LongOpt[] longOpts;

        longOpts = new LongOpt[1];
        longOpts[0] = new LongOpt("disk", LongOpt.REQUIRED_ARGUMENT, null, 'D');

        app = new PackGame();
        app.setHelp("help/packgame.txt");
        app.setProgName("packgame");
        app.setLongOpts(longOpts);
        app.start(args);
    }
}
