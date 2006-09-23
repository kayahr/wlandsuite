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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ailis.wlandsuite.cli.PackProg;
import de.ailis.wlandsuite.pic.Pic;
import de.ailis.wlandsuite.pics.PicsAnimation;
import de.ailis.wlandsuite.pics.PicsAnimationFrameSet;
import de.ailis.wlandsuite.pics.Pics;
import de.ailis.wlandsuite.pics.PicsAnimationInstruction;


/**
 * Packs a PICS file
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class PackPics extends PackProg
{
    /** The logger */
    private static final Log log = LogFactory.getLog(PackPics.class);
    
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
        List<PicsAnimation> animations;
        List<PicsAnimationFrameSet> frameSet;
        List<PicsAnimationInstruction> instructions;
        List<Pic> frames;
        Pic baseFrame, frame;
        String line;
        String[] parts;
        int delay;
        int picNo, frameSetNo, frameNo;
        BufferedReader reader;
        File file;
        File picDirectory, setDirectory;
        Pics pics;
        int lineNo;

        picNo = 0;
        animations = new ArrayList<PicsAnimation>();
        while (true)
        {
            picDirectory = new File(String.format("%s%c%03d", new Object[] {
                directory.getPath(), File.separatorChar, picNo }));
            if (!picDirectory.exists())
            {
                break;
            }

            log.info("Reading pic " + picNo);

            // Read the base frame
            baseFrame = new Pic(ImageIO.read(new File(picDirectory.getPath()
                + File.separatorChar + "000.png")));

            // Read the frame sets
            frameSetNo = 0;
            frameSet = new ArrayList<PicsAnimationFrameSet>();
            while (true)
            {
                setDirectory = new File(String.format("%s%c%03d", new Object[] {
                    picDirectory.getPath(), File.separatorChar, frameSetNo }));
                if (!setDirectory.exists())
                {
                    break;
                }

                // Read the instructions
                instructions = new ArrayList<PicsAnimationInstruction>();
                file = new File(setDirectory.getPath() + File.separatorChar
                    + "animation.txt");
                if (!file.exists())
                {
                    log.error("Animation file '" + file.getPath() + "' not found");
                }
                reader = new BufferedReader(new FileReader(file));
                lineNo = 0;
                while ((line = reader.readLine()) != null)
                {
                    lineNo++;
                    line = line.split("#")[0].trim();
                    if (line.length() == 0) continue;
                    parts = line.split("[ \\t]+");
                    try
                    {
                        delay = Integer.parseInt(parts[0]);
                        frameNo = Integer.parseInt(parts[1]);
                        instructions.add(new PicsAnimationInstruction(delay,
                            frameNo));
                    }
                    catch (Exception e)
                    {
                        log.error("Syntax error in animation file '"
                            + file.getPath() + "' line " + lineNo);
                    }
                }

                // Read the frames
                frameNo = 0;
                frames = new ArrayList<Pic>();
                while (true)
                {
                    file = new File(String.format("%s%c%03d.png",
                        new Object[] { setDirectory.getPath(),
                            File.separatorChar, frameNo + 1 }));
                    if (!file.exists())
                    {
                        break;
                    }
                    frame = new Pic(ImageIO.read(file));
                    frames.add(frame);

                    frameNo++;
                }
                frameSet.add(new PicsAnimationFrameSet(frames, instructions));
                frameSetNo++;
            }
            animations.add(new PicsAnimation(baseFrame, frameSet));

            picNo++;
        }

        pics = new Pics(animations);
        if (this.disk == -1)
        {
            pics.write(output);
        }
        else
        {
            pics.write(output, this.disk);
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
        PackPics app;
        LongOpt[] longOpts;

        longOpts = new LongOpt[1];
        longOpts[0] = new LongOpt("disk", LongOpt.REQUIRED_ARGUMENT, null, 'D');

        app = new PackPics();
        app.setHelp("help/packpics.txt");
        app.setProgName("packpics");
        app.setLongOpts(longOpts);
        app.start(args);
    }
}
