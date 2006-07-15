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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import de.ailis.wlandsuite.cli.ConvertProg;
import de.ailis.wlandsuite.io.WlfReader;
import de.ailis.wlandsuite.utils.FileUtils;
import de.ailis.wlandsuite.utils.WlfUtils;


/**
 * Converts Wasteland WLF file into a standard image format file.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class DecodeWlf extends ConvertProg
{
    /** The width */
    private int width = 16;

    /** The height */
    private int height = 16;

    /** The number of masks to read */
    private int masks = 0;

    /** The output format */
    private String format = null;


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
            case 'H':
                this.height = Integer.parseInt(getopt.getOptarg());
                break;
            case 'm':
                this.masks = Integer.parseInt(getopt.getOptarg());
                break;
            case 'f':
                this.format = getopt.getOptarg();
                break;
        }
    }


    /**
     * @see de.ailis.wlandsuite.cli.ConvertProg#convert(java.io.InputStream,
     *      java.io.OutputStream)
     */

    @Override
    public void convert(InputStream input, OutputStream output)
        throws IOException
    {
        BufferedImage image;

        // Set format if not set via parameter
        if (this.format == null)
        {
            if (this.output != null)
            {
                this.format = FileUtils.getFileExtension(this.output);
            }
            else
            {
                this.format = "png";
            }
        }

        // Set number of masks if not set via parameter
        if (this.masks == 0)
        {
            if (this.input != null)
            {
                this.masks = (int) new File(this.input).length() * 8
                    / this.width / this.height;
            }
            else
            {
                this.masks = 10;
            }
        }

        image = WlfUtils.join(WlfReader.getInstance().readWlf(input,
            this.width, this.height, this.masks));
        ImageIO.write(image, this.format, output);
    }


    /**
     * Main method
     * 
     * @param args
     *            Command line arguments
     */

    public static void main(String[] args)
    {
        DecodeWlf app;
        LongOpt[] longOpts;

        longOpts = new LongOpt[4];
        longOpts[0] = new LongOpt("width", LongOpt.REQUIRED_ARGUMENT, null, 'W');
        longOpts[1] = new LongOpt("height", LongOpt.REQUIRED_ARGUMENT, null,
            'H');
        longOpts[2] = new LongOpt("format", LongOpt.REQUIRED_ARGUMENT, null,
            'f');
        longOpts[3] = new LongOpt("masks", LongOpt.REQUIRED_ARGUMENT, null, 'm');

        app = new DecodeWlf();
        app.setHelp("help/decodewlf.txt");
        app.setProgName("decodewlf");
        app.setLongOpts(longOpts);
        app.start(args);
    }
}
