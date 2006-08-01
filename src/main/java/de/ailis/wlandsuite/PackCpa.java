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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import de.ailis.wlandsuite.cli.PackProg;
import de.ailis.wlandsuite.cpa.Cpa;
import de.ailis.wlandsuite.cpa.CpaFrame;
import de.ailis.wlandsuite.pic.Pic;


/**
 * Packs an animation from a directory into a CPA file.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class PackCpa extends PackProg
{
    /**
     * @see de.ailis.wlandsuite.cli.PackProg#pack(java.io.File,
     *      java.io.OutputStream)
     */

    @Override
    protected void pack(File directory, OutputStream output) throws IOException
    {
        Cpa cpa;
        Pic baseFrame;
        Pic pic;
        List<CpaFrame> frames;
        int frameNo;
        File frameFile;
        BufferedReader reader;
        String line;
        List<Integer> delays;
        int delay;
        File file;

        // Read the base frame
        file = new File(directory.getPath() + File.separatorChar + "000.png");
        if (!file.exists())
        {
            error("Base frame PNG '" + file.getPath() + "' not found");
        }
        baseFrame = new Pic(ImageIO.read(file));

        // Read the animation delays
        delays = new ArrayList<Integer>();
        file = new File(directory.getPath() + File.separatorChar + "delays.txt");
        if (file.exists())
        {
            reader = new BufferedReader(new FileReader(file));
            try
            {
                while ((line = reader.readLine()) != null)
                {
                    line = line.trim();
                    if (line.length() == 0 || line.startsWith("#"))
                    {
                        continue;
                    }
                    delays.add(Integer.valueOf(line));
                }
            }
            finally
            {
                reader.close();
            }
        }
        else
        {
            warn("Delays file '" + file.getPath() + "' not found");
        }

        // Read the animation frames
        frames = new ArrayList<CpaFrame>();
        frameNo = 1;
        while (true)
        {
            frameFile = new File(String.format("%s%c%03d.png", new Object[] {
                directory.getPath(), File.separatorChar, frameNo }));
            if (!frameFile.exists())
            {
                break;
            }
            pic = new Pic(ImageIO.read(frameFile));
            if (frameNo > delays.size())
            {
                warn("No delay found for frame " + frameNo
                    + ". Using default delay 2");
                delay = 2;
            }
            else
            {
                delay = delays.get(frameNo - 1);
            }
            frames.add(new CpaFrame(delay, pic));
            frameNo++;
        }
        if (frameNo != 16)
        {
            warn("Wasteland needs 15 animation frames. But " + (frameNo - 1)
                + " frames were found. This may cause trouble.");
        }

        cpa = new Cpa(baseFrame, frames);
        cpa.write(output);
    }


    /**
     * Main method
     * 
     * @param args
     *            Command line arguments
     */

    public static void main(String[] args)
    {
        PackCpa app;

        app = new PackCpa();
        app.setHelp("help/packcpa.txt");
        app.setProgName("packcpa");
        app.start(args);
    }
}
