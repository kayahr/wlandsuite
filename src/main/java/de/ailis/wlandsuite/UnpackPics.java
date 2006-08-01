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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.imageio.ImageIO;

import de.ailis.wlandsuite.cli.UnpackProg;
import de.ailis.wlandsuite.pics.PicsAnimation;
import de.ailis.wlandsuite.pics.PicsAnimationFrameSet;
import de.ailis.wlandsuite.pics.PicsAnimationInstruction;
import de.ailis.wlandsuite.pics.Pics;


/**
 * Unpacks a PICS file
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class UnpackPics extends UnpackProg
{
    /** The width */
    private int width = 96;


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
        }

    }


    /**
     * @see de.ailis.wlandsuite.cli.UnpackProg#unpack(java.io.InputStream,
     *      java.io.File)
     */

    @Override
    public void unpack(InputStream input, File directory) throws IOException
    {
        int picNo;
        int frameNo;
        int frameSetNo;
        File picDirectory;
        File setDirectory;
        PrintWriter writer;
        Pics pics;
        
        pics = Pics.read(input, this.width);
        picNo = 0;
        for (PicsAnimation pic: pics.getAnimations())
        {
            picDirectory = new File(String.format("%s%c%03d", new Object[] {
                directory.getPath(), File.separatorChar, picNo }));
            picDirectory.mkdirs();

            // Write the base frame
            ImageIO.write(pic.getBaseFrame(), "PNG", new File(picDirectory
                .getPath()
                + File.separatorChar + "000.png"));

            frameSetNo = 0;
            for (PicsAnimationFrameSet set: pic.getFrameSets())
            {
                setDirectory = new File(String.format("%s%c%03d", new Object[] {
                    picDirectory.getPath(), File.separatorChar, frameSetNo }));
                setDirectory.mkdirs();

                frameNo = 0;
                for (BufferedImage frame: set.getFrames())
                {
                    ImageIO.write(frame, "PNG", new File(String.format(
                        "%s%c%03d.png", new Object[] { setDirectory.getPath(),
                            File.separatorChar, frameNo + 1 })));

                    frameNo++;
                }
                frameSetNo++;

                // Write the animation data
                writer = new PrintWriter(new FileWriter(new File(String.format(
                    "%s%canimation.txt", new Object[] { setDirectory.getPath(),
                        File.separatorChar }))));
                try
                {
                    writer.println("# First number is the delay value (0-254)");
                    writer
                        .println("# Second number is the frame to display (000 = Base frame)");
                    writer.println();
                    for (PicsAnimationInstruction instruction: set
                        .getInstructions())
                    {
                        writer.println(String.format("%3d %03d", new Object[] {
                            instruction.getDelay(), instruction.getFrame() }));
                    }
                }
                finally
                {
                    writer.close();
                }
            }
            picNo++;
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
        UnpackPics app;
        LongOpt[] longOpts;

        longOpts = new LongOpt[1];
        longOpts[0] = new LongOpt("width", LongOpt.REQUIRED_ARGUMENT, null, 'W');

        app = new UnpackPics();
        app.setHelp("help/unpackpics.txt");
        app.setProgName("unpackpics");
        app.setLongOpts(longOpts);
        app.start(args);
    }
}
