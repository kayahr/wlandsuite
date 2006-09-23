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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ailis.wlandsuite.cli.PackProg;
import de.ailis.wlandsuite.game.Game;
import de.ailis.wlandsuite.game.blocks.GameMap;
import de.ailis.wlandsuite.game.blocks.Savegame;
import de.ailis.wlandsuite.game.blocks.ShopItemList;


/**
 * Packs a GAME file.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class PackGame extends PackProg
{
    /** The logger */
    private static final Log log = LogFactory.getLog(PackGame.class);
    
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
        int mapNo, listNo;
        InputStream stream;
        File file;
        
        game = new Game();
        mapNo = 0;
        while (true)
        {
            file = new File(String.format("%s%cmap%02d.xml", new Object[] {
                directory.getPath(), File.separatorChar, mapNo }));
            if (!file.exists())
            {
                break;
            }
            stream = new FileInputStream(file);
            try
            {
                log.info("Reading map " + mapNo);
                game.addMap(GameMap.readXml(stream));
            }
            finally
            {
                stream.close();
            }
            mapNo++;
        }
        
        file = new File(String.format("%s%csavegame.xml", new Object[] {
            directory.getPath(), File.separatorChar }));
        if (file.exists())
        {
            stream = new FileInputStream(file);
            try
            {
                log.info("Reading savegame");
                game.setSavegame(Savegame.readXml(stream));
            }
            finally
            {
                stream.close();
            }
        }

        listNo = 0;
        while (true)
        {
            file = new File(String.format("%s%cshopitems%d.xml", new Object[] {
                directory.getPath(), File.separatorChar, listNo }));
            if (!file.exists())
            {
                break;
            }
            stream = new FileInputStream(file);
            try
            {
                log.info("Reading shop list " + listNo);
                game.addShopItemList(ShopItemList.readXml(stream));
            }
            finally
            {
                stream.close();
            }
            listNo++;
        }

        game.write(output, this.disk == -1 ? 0 : this.disk);
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
