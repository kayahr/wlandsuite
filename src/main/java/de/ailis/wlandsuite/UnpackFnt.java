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

import javax.imageio.ImageIO;

import de.ailis.wlandsuite.cli.UnpackProg;
import de.ailis.wlandsuite.fnt.Fnt;
import de.ailis.wlandsuite.fnt.FntChar;


/**
 * Unpacks a FNT file into a directory.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class UnpackFnt extends UnpackProg
{
    /** The number of characters to read */
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
    public void unpack(InputStream input, File output) throws IOException
    {
        Fnt fnt;
        int charNo;
        File file;

        // Set number of cursors if not set via parameter
        if (this.quantity == 0)
        {
            if (this.input != null)
            {
                this.quantity = Fnt.getQuantity(new File(this.input).length());
            }
            else
            {
                this.quantity = 172;
            }
        }

        // Read the cursors
        fnt = Fnt.read(input, this.quantity);

        // Write the cursor images
        charNo = 0;
        for (FntChar fntChar: fnt.getChars())
        {
            file = new File(String.format("%s%c%03d.png", new Object[] {
                output.getPath(), File.separatorChar, charNo }));
            ImageIO.write(fntChar, "PNG", file);

            charNo++;
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
        UnpackFnt app;
        LongOpt[] longOpts;

        longOpts = new LongOpt[1];
        longOpts[0] = new LongOpt("quantity", LongOpt.REQUIRED_ARGUMENT, null,
            'q');

        app = new UnpackFnt();
        app.setHelp("help/unpackfnt.txt");
        app.setProgName("unpackfnt");
        app.setLongOpts(longOpts);
        app.start(args);
    }
}
