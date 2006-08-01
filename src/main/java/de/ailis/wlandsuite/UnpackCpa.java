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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.imageio.ImageIO;

import de.ailis.wlandsuite.cli.UnpackProg;
import de.ailis.wlandsuite.cpa.Cpa;
import de.ailis.wlandsuite.cpa.CpaFrame;


/**
 * Unpacks a CPA file into a directory. The base frame gets 000.png. Each frame
 * in the animation is saved in its own PNG with a filename like 001.png,
 * 002.png and so on. A text file named animation.txt is also written to the
 * directory. This file contains the delay values used between the frames.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class UnpackCpa extends UnpackProg
{
    /** The width */
    private int width = 288;


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
    public void unpack(InputStream input, File output) throws IOException
    {
        Cpa cpa;
        int frameNo;
        PrintWriter writer;

        // Read the CPA
        cpa = Cpa.read(input, this.width);

        // Save the base frame
        ImageIO.write(cpa.getBaseFrame(), "PNG", new File(String.format(
            "%s%c000.png",
            new Object[] { output.getPath(), File.separatorChar })));

        // Save the animation frames and the animation.txt file
        writer = new PrintWriter(new FileWriter(output.getPath()
            + File.separatorChar + "delays.txt"));
        try
        {
            writer
                .println("# The delays between the animation frames (0-65534)");
            writer.println();
            frameNo = 1;
            for (CpaFrame frame: cpa.getFrames())
            {
                ImageIO.write(frame.getPic(), "PNG", new File(String.format(
                    "%s%c%03d.png", new Object[] { output.getPath(),
                        File.separatorChar, frameNo })));
                writer.println(String.format("%5d", new Object[] { frame
                    .getDelay() }));
                frameNo++;
            }
        }
        finally
        {
            writer.close();
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
        UnpackCpa app;
        LongOpt[] longOpts;

        longOpts = new LongOpt[1];
        longOpts[0] = new LongOpt("width", LongOpt.REQUIRED_ARGUMENT, null, 'W');

        app = new UnpackCpa();
        app.setHelp("help/unpackcpa.txt");
        app.setProgName("unpackcpa");
        app.setLongOpts(longOpts);
        app.start(args);
    }
}
