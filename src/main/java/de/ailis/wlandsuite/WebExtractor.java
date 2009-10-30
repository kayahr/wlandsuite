/*
 * $Id: UnpackTileset.java 126 2006-10-07 09:05:26Z k $
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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ailis.wlandsuite.cli.ExtractProg;
import de.ailis.wlandsuite.htds.Htds;
import de.ailis.wlandsuite.htds.HtdsTileset;
import de.ailis.wlandsuite.image.GifAnimWriter;
import de.ailis.wlandsuite.image.TransparentEgaImage;
import de.ailis.wlandsuite.masks.Mask;
import de.ailis.wlandsuite.masks.Masks;
import de.ailis.wlandsuite.pic.Pic;
import de.ailis.wlandsuite.pics.Pics;
import de.ailis.wlandsuite.pics.PicsAnimation;
import de.ailis.wlandsuite.pics.PicsAnimationFrameSet;
import de.ailis.wlandsuite.pics.PicsAnimationInstruction;
import de.ailis.wlandsuite.sprites.Sprite;
import de.ailis.wlandsuite.sprites.Sprites;


/**
 * Extracts all game data to web compatible files (JSON objects and PNG
 * graphics).
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision: 126 $
 */

public class WebExtractor extends ExtractProg
{
    /** The logger */
    private static final Log log = LogFactory.getLog(ExtractMaps.class);


    /**
     * Extracts the tilesets.
     * 
     * @param sourceDirectory
     *            The input directory
     * @param targetDirectory
     *            The output directory
     * @throws IOException
     */

    private void extractTilesets(final File sourceDirectory,
        final File targetDirectory) throws IOException
    {
        // Extract tilesets
        final File imagesDirectory = new File(targetDirectory, "images");
        imagesDirectory.mkdirs();
        for (int gameId = 1; gameId <= 2; gameId++)
        {
            final String filename = "allhtds" + gameId;

            log.info("Reading " + filename);
            final InputStream stream = new FileInputStream(new File(
                sourceDirectory, filename));
            try
            {
                final Htds htds = Htds.read(stream);

                int tilesetId = 0;
                for (final HtdsTileset tileset: htds.getTilesets())
                {
                    final List<Pic> tiles = tileset.getTiles();
                    final BufferedImage out = new BufferedImage(10 * 16,
                        (int) Math.ceil((double) tiles.size() / 10) * 16,
                        BufferedImage.TYPE_INT_RGB);
                    final Graphics2D g = out.createGraphics();
                    int i = 0;
                    for (final Pic tile: tileset.getTiles())
                    {
                        g.drawImage(tile, i % 10 * 16, i / 10 * 16, null);
                        i++;
                    }
                    ImageIO.write(out, "png", new File(imagesDirectory,
                        "tileset" + gameId + tilesetId + ".png"));
                    tilesetId++;
                }
            }
            finally
            {
                stream.close();
            }
        }
    }


    /**
     * Extracts the sprites.
     * 
     * @param sourceDirectory
     *            The input directory
     * @param targetDirectory
     *            The output directory
     * @throws IOException
     */

    private void extractSprites(final File sourceDirectory,
        final File targetDirectory) throws IOException
    {
        // Extract tilesets
        final File imagesDirectory = new File(targetDirectory, "images");
        imagesDirectory.mkdirs();

        String filename = "ic0_9.wlf";
        log.info("Reading " + filename);
        final Sprites sprites;
        InputStream stream = new FileInputStream(new File(sourceDirectory,
            filename));
        try
        {
            sprites = Sprites.read(stream);
        }
        finally
        {
            stream.close();
        }

        filename = "masks.wlf";
        log.info("Reading " + filename);
        final Masks masks;
        stream = new FileInputStream(new File(sourceDirectory, filename));
        try
        {
            masks = Masks.read(stream, 10);
        }
        finally
        {
            stream.close();
        }

        final BufferedImage out = new BufferedImage(10 * 16, 16,
            BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < 10; i++)
        {
            final Sprite sprite = sprites.getSprites().get(i);
            final Mask mask = masks.getMasks().get(i);
            for (int x = 0; x < 16; x++)
            {
                for (int y = 0; y < 16; y++)
                {
                    if (mask.getRGB(x, y) == Color.BLACK.getRGB())
                        out.setRGB(i * 16 + x, y, sprite.getRGB(x, y));
                }
            }
        }
        ImageIO.write(out, "png", new File(imagesDirectory, "sprites.png"));
    }


    /**
     * Extracts the animations.
     * 
     * @param sourceDirectory
     *            The input directory
     * @param targetDirectory
     *            The output directory
     * @throws IOException
     */

    private void extractAnimations(final File sourceDirectory,
        final File targetDirectory) throws IOException
    {
        // Extract tilesets
        final File animsDirectory = new File(
            new File(targetDirectory, "images"), "animations");
        animsDirectory.mkdirs();

        for (int gameId = 1; gameId <= 2; gameId++)
        {
            final String filename = "allpics" + gameId;

            log.info("Reading " + filename);
            final Pics pics;
            final InputStream stream = new FileInputStream(new File(
                sourceDirectory, filename));
            try
            {
                pics = Pics.read(stream);
            }
            finally
            {
                stream.close();
            }

            int i = 0;
            for (final PicsAnimation animation: pics.getAnimations())
            {
                final File animDirectory = new File(animsDirectory, String
                    .format("%d%02d", gameId, i));
                animDirectory.mkdirs();

                final Pic baseFrame = animation.getBaseFrame();

                int layerId = 1;
                for (final PicsAnimationFrameSet frameSet: animation
                    .getFrameSets())
                {
                    final List<Pic> frames = frameSet.getFrames();
                    final List<PicsAnimationInstruction> instructions = frameSet
                        .getInstructions();
                    final GifAnimWriter gif = new GifAnimWriter(new File(
                        animDirectory, "layer" + layerId + ".gif"), 0);
                    try
                    {
                        gif.setTransparentIndex(16);
                        gif.setDelay(instructions.get(0).getDelay() * 50);
                        Pic current = baseFrame;
                        if (layerId == 1)
                            gif.addFrame(current);
                        else
                            gif.addFrame(new TransparentEgaImage(baseFrame
                                .getWidth(), baseFrame.getHeight()));
                        for (int j = 0; j < instructions.size(); j++)
                        {
                            final PicsAnimationInstruction instruction = instructions
                                .get(j);
                            final int frameIndex = instruction.getFrame();
                            final int delay = instructions.get(
                                (j + 1) % instructions.size()).getDelay();
                            final Pic frame = frameIndex == 0 ? baseFrame
                                : frames.get(frameIndex - 1);
                            gif.setDelay(delay * 50);
                            gif.addFrame(current.getDiff(frame));
                            current = frame;
                        }
                    }
                    finally
                    {
                        gif.close();
                    }
                    layerId++;
                }

                final File htmlFile = new File(animDirectory, "index.html");
                final PrintStream html = new PrintStream(htmlFile);
                html.println("<html>");
                html.println("<body>");
                html.println("<div style=\"position:relative\">");
                html.println("<img src=\"layer1.gif\" />");
                for (int j = 2; j < layerId; j++)
                {
                    html.println("<img src=\"layer" + j
                        + ".gif\" style=\"position:absolute;left:0;top:0\" />");
                }
                html.println("</div>");
                html.println("</body>");
                html.println("</html>");
                html.close();

                i++;
            }
        }
    }

    
    /**
     * @see de.ailis.wlandsuite.cli.ExtractProg#extract(java.io.File,
     *      java.io.File)
     */

    @Override
    protected void extract(final File sourceDirectory,
        final File targetDirectory) throws IOException
    {
        extractTilesets(sourceDirectory, targetDirectory);
        extractSprites(sourceDirectory, targetDirectory);
        extractAnimations(sourceDirectory, targetDirectory);
    }


    /**
     * Main method
     * 
     * @param args
     *            Command line arguments
     */

    public static void main(final String[] args)
    {
        final WebExtractor app = new WebExtractor();
        app.setHelp("help/webextractor.txt");
        app.setProgName("webextractor");
        app.start(args);
    }
}
