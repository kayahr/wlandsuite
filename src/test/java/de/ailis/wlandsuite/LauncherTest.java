/*
 * $Id$
 * Copyright (c) 2006 Klaus Reimer
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

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;
import de.ailis.wlandsuite.test.WSTestCase;


/**
 * Tests the Launcher program. Also is the base for all the other program tests.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class LauncherTest extends WSTestCase
{
    /** The program name */
    protected String progName;

    /** The program version */
    protected String version = "1.1.0";


    /**
     * Returns the test suite.
     * 
     * @return The test suite
     */

    public static Test suite()
    {
        return new TestSuite(LauncherTest.class);
    }


    /**
     * @see junit.framework.TestCase#setUp()
     */

    @Override
    public void setUp()
    {
        this.progName = "wlandsuite";
    }


    /**
     * Tests empty call
     * 
     * @throws IOException
     */

    public void testEmpty() throws IOException
    {
        testExec(this.progName, 2, "", "^" + this.progName
            + ": ERROR! No command specified\n" + "Try '" + this.progName
            + " --help' for more information\\.\n$");
    }


    /**
     * Tests version display
     * 
     * @throws IOException
     */

    public void testVersion() throws IOException
    {
        testExec(
            this.progName + " --version",
            0,
            "^wlandsuite "
                + this.version
                + "\n\nCopyright \\(C\\) 2006 by Klaus Reimer\n\nPermission.*SOFTWARE\\.\n$",
            "");
    }


    /**
     * Tests help display
     * 
     * @throws IOException
     */

    public void testHelp() throws IOException
    {
        testExec(
            this.progName + " --help",
            0,
            "^Usage: "
                + this.progName
                + " \\[OPTION\\]\\.\\.\\..*\nReport bugs to Klaus Reimer <k@ailis.de>\n$",
            "");
    }
}
