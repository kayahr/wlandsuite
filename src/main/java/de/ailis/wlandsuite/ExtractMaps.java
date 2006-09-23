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

import java.awt.Graphics2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;

import de.ailis.wlandsuite.cli.ExtractProg;
import de.ailis.wlandsuite.game.Game;
import de.ailis.wlandsuite.game.blocks.GameMap;
import de.ailis.wlandsuite.game.parts.TileMap;
import de.ailis.wlandsuite.htds.Htds;
import de.ailis.wlandsuite.image.EgaImage;
import de.ailis.wlandsuite.pic.Pic;
import de.ailis.wlandsuite.sprites.Sprites;


/**
 * Extract all the maps of the game into a directory.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class ExtractMaps extends ExtractProg
{
    /**
     * @see de.ailis.wlandsuite.cli.ExtractProg#extract(java.io.File,
     *      java.io.File)
     */

    @Override
    public void extract(File input, File output) throws IOException
    {
        Game game1, game2;
        Htds htds1, htds2;
        Sprites sprites;
        InputStream stream;

        // Read game 1
        stream = new FileInputStream(new File(input.getAbsolutePath()
            + File.separatorChar + "game1"));
        try
        {
            game1 = Game.read(stream);
        }
        finally
        {
            stream.close();
        }

        // Read game 2
        stream = new FileInputStream(new File(input.getAbsolutePath()
            + File.separatorChar + "game2"));
        try
        {
            game2 = Game.read(stream);
        }
        finally
        {
            stream.close();
        }

        // Read tileset 1
        stream = new FileInputStream(new File(input.getAbsolutePath()
            + File.separatorChar + "allhtds1"));
        try
        {
            htds1 = Htds.read(stream);
        }
        finally
        {
            stream.close();
        }

        // Read tileset 2
        stream = new FileInputStream(new File(input.getAbsolutePath()
            + File.separatorChar + "allhtds2"));
        try
        {
            htds2 = Htds.read(stream);
        }
        finally
        {
            stream.close();
        }

        // Read sprites
        stream = new FileInputStream(new File(input.getAbsolutePath()
            + File.separatorChar + "ic0_9.wlf"));
        try
        {
            sprites = Sprites.read(stream);
        }
        finally
        {
            stream.close();
        }

        // Iterate over both game files
        int fileNo = 100;
        for (Game game: new Game[] { game1, game2 })
        {
            // Iterate over all maps of the current game file
            for (GameMap gameMap: game.getMaps())
            {
                int tilesetId = gameMap.getInfo().getTileset();
                int size = gameMap.getMapSize();
                EgaImage image = new EgaImage(size * 16, size * 16);
                Graphics2D graphics = image.createGraphics();
                List<Pic> tiles;

                if (tilesetId < 4)
                {
                    tiles = htds1.getTilesets().get(tilesetId).getTiles();
                }
                else
                {
                    tiles = htds2.getTilesets().get(tilesetId - 4).getTiles();
                }
                TileMap map = gameMap.getTileMap();

                for (int y = 0; y < size; y++)
                {
                    for (int x = 0; x < size; x++)
                    {
                        int tile = map.getTile(x, y);

                        if (tile >= 10)
                        {
                            graphics.drawImage(tiles.get(tile - 10), x * 16,
                                y * 16, null);
                        }
                        else
                        {
                            graphics.drawImage(sprites.getSprites().get(tile),
                                x * 16, y * 16, null);
                        }
                    }
                }

                // Write the image to disk
                File file = new File(String.format("%s%c%03d.png",
                    new Object[] { output, File.separatorChar, fileNo }));
                ImageIO.write(image, "PNG", file);

                fileNo++;
            }
            fileNo = 200;
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
        ExtractMaps app;
        app = new ExtractMaps();
        app.setHelp("help/extractmaps.txt");
        app.setProgName("extractmaps");
        app.start(args);
    }
}
