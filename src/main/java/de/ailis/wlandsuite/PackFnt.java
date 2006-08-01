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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import de.ailis.wlandsuite.cli.PackProg;
import de.ailis.wlandsuite.fnt.Fnt;
import de.ailis.wlandsuite.fnt.FntChar;


/**
 * Packs font characters from a directory into a fnt file.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class PackFnt extends PackProg
{
    /**
     * @see de.ailis.wlandsuite.cli.PackProg#pack(java.io.File,
     *      java.io.OutputStream)
     */

    @Override
    protected void pack(File directory, OutputStream output) throws IOException
    {
        List<FntChar> chars;
        FntChar fntChar;
        File file;
        int charNo;

        // Read the chars
        chars = new ArrayList<FntChar>();
        charNo = 0;
        while (true)
        {
            file = new File(String.format("%s%c%03d.png", new Object[] {
                directory.getPath(), File.separatorChar, charNo }));
            if (!file.exists())
            {
                break;
            }
            fntChar = new FntChar(ImageIO.read(file));
            
            chars.add(fntChar);
            charNo++;
        }

        new Fnt(chars).write(output);
    }


    /**
     * Main method
     * 
     * @param args
     *            Command line arguments
     */

    public static void main(String[] args)
    {
        PackFnt app;

        app = new PackFnt();
        app.setHelp("help/packfnt.txt");
        app.setProgName("packfnt");
        app.start(args);
    }
}
