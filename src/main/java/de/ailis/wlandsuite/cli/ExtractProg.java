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
import java.io.IOException;


/**
 * A base class for extract programs. Extract programs are meant for extracting
 * information from the wasteland directory into a target directory.
 *
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public abstract class ExtractProg extends CLIProg
{
    /** The input filename (or null for current directory) */
    protected String input = null;

    /** The output directory */
    protected File output;


    /**
     * Returns the source directory where the input data can be read from.
     *
     * @param input
     *            The input file name or null for stdin
     * @return The input stream
     */

    private File getSourceDirectory(final String input)
    {
        if (input == null)
        {
            return new File(".");
        }
        else
        {
            return new File(input);
        }
    }


    /**
     * Runs the program
     *
     * @param params
     *            The command line arguments.
     * @throws IOException
     *             When some file could not be read or written.
     */

    @Override
    public void run(final String[] params) throws IOException
    {
        File sourceDirectory;

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
        }

        // Too many parameters?
        if (params.length > 2)
        {
            wrongUsage("Too many parameters");
        }

        // Read the source directory
        sourceDirectory = getSourceDirectory(this.input);
        extract(sourceDirectory, this.output);
    }


    /**
     * Extracts some data from input directory into some output.
     *
     * @param sourceDirectory
     *            The input directory
     * @param targetDirectory
     *            The output directory
     * @throws IOException
     *             When file operation fails.
     */

    protected abstract void extract(File sourceDirectory, File targetDirectory)
        throws IOException;
}
