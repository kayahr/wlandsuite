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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import de.ailis.wlandsuite.cli.UnpackProg;
import de.ailis.wlandsuite.game.Game;
import de.ailis.wlandsuite.game.GameBlock;


/**
 * Unpacks a game file into a directory.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class UnpackGame extends UnpackProg
{
    /** The wasteland directory */
    private File wlDir;


    /**
     * @see de.ailis.wlandsuite.cli.CLIProg#processOption(int,
     *      gnu.getopt.Getopt)
     */

    @Override
    protected void processOption(int opt, Getopt getopt)
    {
        switch (opt)
        {
            case 'w':
                this.wlDir = new File(getopt.getOptarg());
                break;
        }
    }


    /**
     * @see de.ailis.wlandsuite.cli.UnpackProg#unpack(java.io.InputStream,
     *      java.io.File)
     */

    @Override
    public void unpack(InputStream input, File output) throws IOException
    {
        Game game;
        List<GameBlock> blocks;
        File wl;
        int blockNo;
        File blockFile;

        if (this.wlDir == null)
        {
            wl = new File("wl.exe");
        }
        else
        {
            wl = new File(this.wlDir.getPath() + File.separatorChar + "wl.exe");
        }

        game = Game.read(input, wl);
        blocks = game.getBlocks();
        blockNo = 0;
        for (GameBlock block: blocks)
        {
            OutputStream stream;
            
            blockFile = new File(String.format("%s%c%03d.xml", new Object[] {
                output, File.separatorChar, blockNo }));
            stream = new FileOutputStream(blockFile);
            try
            {
                block.writeXml(stream);
            }
            finally
            {
                stream.close();
            }
            blockNo++;
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
        UnpackGame app;
        app = new UnpackGame();
        app.setHelp("help/unpackgame.txt");
        app.setProgName("unpackgame");
        app.start(args);
    }
}
