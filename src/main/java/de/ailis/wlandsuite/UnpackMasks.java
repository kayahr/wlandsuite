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
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;

import de.ailis.wlandsuite.cli.UnpackProg;
import de.ailis.wlandsuite.masks.Masks;
import de.ailis.wlandsuite.masks.Mask;


/**
 * Unpacks the bit masks from a Wasteland masks.wlf file into a directory.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class UnpackMasks extends UnpackProg
{
    /** The width */
    private int width = 16;

    /** The height */
    private int height = 16;

    /** The number of masks to read */
    private int quantity = 0;


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
            case 'q':
                this.quantity = Integer.parseInt(getopt.getOptarg());
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
        Masks wlf;
        List<Mask> masks;
        File file;

        // Set number of masks if not set via parameter
        if (this.quantity == 0)
        {
            if (this.input != null)
            {
                this.quantity = Masks.getNumberOfMasks(this.width, this.height,
                    new File(this.input).length());
            }
            else
            {
                this.quantity = 10;
            }
        }

        wlf = Masks.read(input, this.width, this.height, this.quantity);
        masks = wlf.getMasks();
        for (int i = 0; i < masks.size(); i++)
        {
            file = new File(String.format("%s%c%03d.png", new Object[] {
                directory.getPath(), File.separatorChar, i }));
            ImageIO.write(masks.get(i), "PNG", file);
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
        UnpackMasks app;
        LongOpt[] longOpts;

        longOpts = new LongOpt[3];
        longOpts[0] = new LongOpt("width", LongOpt.REQUIRED_ARGUMENT, null, 'W');
        longOpts[1] = new LongOpt("height", LongOpt.REQUIRED_ARGUMENT, null,
            'H');
        longOpts[2] = new LongOpt("quantity", LongOpt.REQUIRED_ARGUMENT, null, 'q');

        app = new UnpackMasks();
        app.setHelp("help/unpackmasks.txt");
        app.setProgName("unpackmasks");
        app.setLongOpts(longOpts);
        app.start(args);
    }
}
