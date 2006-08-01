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

package de.ailis.wlandsuite.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * A base class for packing programs.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public abstract class PackProg extends CLIProg
{
    /** The output filename (or null for stdout) */
    protected String output = null;
    
    /** The input directory */
    protected File input;

    
    /**
     * Returns the output stream where the data will be written to.
     * 
     * @param output
     *            The output file name or null for stdout
     * @return The output stream
     * @throws FileNotFoundException
     */

    private OutputStream getOutputStream(String output)
        throws FileNotFoundException
    {
        if (output == null)
        {
            return System.out;
        }
        else
        {
            return new FileOutputStream(new File(output));
        }
    }
    

    /**
     * Runs the program
     * 
     * @param params
     *            The command line arguments.
     * @throws IOException
     */

    @Override
    public void run(String[] params) throws IOException
    {
        OutputStream outputStream;
        
        if (params.length == 0)
        {
            wrongUsage("No input directory specified");
        }
        this.input = new File(params[0]);

        // Read output filename
        if (params.length > 1)
        {
            this.output = params[1];
            if ("-".equals(this.output))
            {
                this.output= null;
            }
        }

        // Too many parameters?
        if (params.length > 2)
        {
            wrongUsage("Too many parameters");
        }

        // Read the input file
        outputStream = getOutputStream(this.output);
        try
        {
            pack(this.input, outputStream);
        }
        finally
        {
            outputStream.close();
        }
    }


    /**
     * Packs the data in the input directory to the output stream.
     *
     * @param directory
     *            The input directory
     * @param output
     *            The output stream
     * @throws IOException 
     */

    protected abstract void pack(File directory, OutputStream output) throws IOException;
}
