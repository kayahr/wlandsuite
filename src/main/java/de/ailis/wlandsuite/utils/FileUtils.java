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

package de.ailis.wlandsuite.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * File utility methods
 *
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class FileUtils
{
    /**
     * Returns the file extension of the filename. If the filename has no
     * extension then an empty string is returned.
     *
     * @param filename
     *            The filename
     * @return The file extension
     */

    public static String getFileExtension(final String filename)
    {
        int pos;

        pos = filename.lastIndexOf('.');
        if (pos >= 0)
        {
            return filename.substring(pos + 1);
        }
        else
        {
            return "";
        }
    }


    /**
     * Reads all the available bytes from a file and returns them as a byte
     * array.
     *
     * @param file
     *            The file to read
     * @return The read data
     * @throws IOException
     *             When file operation fails.
     */

    public static byte[] readBytes(final File file) throws IOException
    {
        FileInputStream stream;

        stream = new FileInputStream(file);
        try
        {
            return readBytes(stream);
        }
        finally
        {
            stream.close();
        }
    }


    /**
     * Reads all the available bytes from the input stream and returns them as a
     * byte array.
     *
     * @param stream
     *            The input stream
     * @return The read data
     * @throws IOException
     *             When file operation fails.
     */

    public static byte[] readBytes(final InputStream stream) throws IOException
    {
        ByteArrayOutputStream output;
        byte[] buffer;
        int read;

        output = new ByteArrayOutputStream();
        try
        {
            buffer = new byte[8192];
            while ((read = stream.read(buffer)) != -1)
            {
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        }
        finally
        {
            output.close();
        }
    }


    /**
     * Writes bytes to a file.
     *
     * @param file
     *            The file
     * @param bytes
     *            The bytes
     * @throws IOException
     *             When file operation fails.
     */

    public static void writeBytes(final File file, final byte[] bytes) throws IOException
    {
        FileOutputStream stream;

        stream = new FileOutputStream(file);
        try
        {
            stream.write(bytes);
        }
        finally
        {
            stream.close();
        }
    }
}
