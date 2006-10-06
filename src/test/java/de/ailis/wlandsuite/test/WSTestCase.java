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

package de.ailis.wlandsuite.test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import junit.framework.TestCase;


/**
 * Utility methods for making testing easier
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public abstract class WSTestCase extends TestCase
{
    /**
     * Compares a byte array with the contents of a file.
     * 
     * @param file
     *            The file
     * @param data
     *            The byte array
     * @throws IOException
     */

    public static void assertEquals(File file, byte[] data) throws IOException
    {
        InputStream stream1;
        InputStream stream2;
        int b;

        stream1 = new ByteArrayInputStream(data);
        stream2 = new FileInputStream(file);
        try
        {
            while ((b = stream2.read()) != -1)
            {
                if (b != stream1.read())
                {
                    fail("Byte data doesn't match the file: " + file.getPath());
                }
            }
            if (stream1.read() != -1)
            {
                fail("Byte data is longer than the file: " + file.getPath());
            }
        }
        finally
        {
            stream2.close();
        }
    }


    /**
     * Compares two files.
     * 
     * @param file1
     *            The first file
     * @param file2
     *            The second file
     * @throws IOException
     */

    public static void assertEquals(File file1, File file2) throws IOException
    {
        InputStream stream1;
        InputStream stream2;
        int b;

        stream1 = new FileInputStream(file2);
        stream2 = new FileInputStream(file1);
        try
        {
            while ((b = stream2.read()) != -1)
            {
                if (b != stream1.read())
                {
                    fail("Content of " + file2.getPath() + " does not match the content of " + file1.getPath());
                }
            }
            if (stream1.read() != -1)
            {
                fail(file2.getPath() + " is longer than " + file1.getPath());
            }
        }
        finally
        {
            stream2.close();
            stream1.close();
        }
    }


    /**
     * Compares an image with an image file.
     * 
     * @param file
     *            The image file
     * @param image
     *            The image
     * @throws IOException
     */

    public static void assertEquals(File file, BufferedImage image)
        throws IOException
    {
        BufferedImage other;
        int w, h, x, y;

        other = ImageIO.read(file);

        w = image.getWidth();
        h = image.getHeight();
        if (other.getWidth() != w || other.getHeight() != h)
        {
            fail("Image has not the same size as the file: " + file.getPath());
        }
        for (y = 0; y < h; y++)
        {
            for (x = 0; x < w; x++)
            {
                if (image.getRGB(x, y) != other.getRGB(x, y))
                {
                    fail("Image does not match the image in the file: "
                        + file.getPath());
                }
            }
        }
    }


    /**
     * Reads the complete data from the specified input stream into a string and
     * returns it.
     * 
     * @param stream
     *            The input stream
     * @return The string
     * @throws IOException
     */

    public static String readString(InputStream stream) throws IOException
    {
        ByteArrayOutputStream bytes;
        byte[] buffer;
        int read;

        buffer = new byte[8192];
        bytes = new ByteArrayOutputStream();
        while ((read = stream.read(buffer)) != -1)
        {
            bytes.write(buffer, 0, read);
        }
        return new String(bytes.toByteArray(), "ISO-8859-1");
    }


    /**
     * Checks if a text matches a regular expression
     * 
     * @param regex
     *            The regular expression
     * @param text
     *            The test text
     */

    public static void assertRegex(String regex, String text)
    {
        Pattern pattern;
        
        pattern = Pattern.compile(regex, Pattern.DOTALL);
        if (!pattern.matcher(text).matches())
        {
            fail("Text '" + text + "' does not match regex '" + regex + "'");
        }
    }

    
    /**
     * Executes a Wasteland Suite program and checks its output.
     * 
     * @param command
     *            The command to execute
     * @param statusCode
     *            The correct status code
     * @param outExpr
     *            The correct output text regular expression
     * @param errExpr
     *            The correct error text regular expression
     * @throws IOException
     */

    public static void testExec(String command, int statusCode, String outExpr,
        String errExpr) throws IOException
    {
        String[] env;
        Process process;
        String out, err;

        env = new String[1];
        env[0] = "CLASSPATH=target/classes" + File.pathSeparatorChar
            + "src/test/lib/java-getopt.jar" + File.pathSeparatorChar
            + "src/test/lib/commons-logging.jar" + File.pathSeparatorChar
            + "src/test/lib/dom4j.jar";

        if (command.startsWith("wlandsuite"))
        {
            command = command.substring(10);
        }
        else
        {
            command = "-- " + command;
        }
        process = Runtime.getRuntime().exec(
            "java de.ailis.wlandsuite.Launcher " + command, env);
        process.getOutputStream().close();
        err = readString(process.getErrorStream());
        out = readString(process.getInputStream());
        assertRegex(errExpr, err);
        assertRegex(outExpr, out);
        try
        {
            assertEquals(statusCode, process.waitFor());
        }
        catch (InterruptedException e)
        {
            fail(e.getMessage());
        }
    }
}
