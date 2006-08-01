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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


/**
 * A base class for extract programs.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public abstract class UnpackProg extends CLIProg
{
    /** The input filename (or null for stdin) */
    protected String input = null;
    
    /** The output directory */
    protected File output;

    
    /**
     * Returns the input stream where the input data can be read from.
     * 
     * @param input
     *            The input file name or null for stdin
     * @return The input stream
     * @throws FileNotFoundException
     */

    private InputStream getInputStream(String input)
        throws FileNotFoundException
    {
        if (input == null)
        {
            return System.in;
        }
        else
        {
            return new FileInputStream(new File(input));
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
        InputStream inputStream;
        
        if (params.length == 0)
        {
            wrongUsage("No output directory specified");
        }
        this.output = new File(params[0]);
        this.output.mkdirs();

        // Read input filename
        if (params.length > 1)
        {
            this.input = params[1];
            if ("-".equals(this.input))
            {
                this.input = null;
            }
        }

        // Too many parameters?
        if (params.length > 2)
        {
            wrongUsage("Too many parameters");
        }

        // Read the input file
        inputStream = getInputStream(this.input);
        try
        {
            unpack(inputStream, this.output);
        }
        finally
        {
            inputStream.close();
        }
    }


    /**
     * Converts some input into some output.
     * 
     * @param input
     *            The input stream
     * @param directory
     *            The output directory
     * @throws IOException 
     */

    protected abstract void unpack(InputStream input, File directory) throws IOException;
}
