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

import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;


/**
 * Simple Log handler which just logs the
 *
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class LogHandler extends Handler
{
    /** The program */
    private String program;
    
    
    /**
     * Constructor
     * 
     * @param program The program
     */
    
    public LogHandler(String program)
    {
        this.program = program;
    }
    
    /**
     * @see java.util.logging.Handler#close()
     */

    @Override
    public void close() throws SecurityException
    {
        // Empty
    }

    
    /**
     * @see java.util.logging.Handler#flush()
     */

    @Override
    public void flush()
    {
        // Empty
    }

    
    /**
     * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
     */

    @Override
    public void publish(LogRecord record)
    {
        StringBuilder builder;
        PrintStream stream;
        
        builder = new StringBuilder();
        builder.append(this.program);
        builder.append(": ");
        
        if (record.getLevel() == Level.WARNING)
        {
            stream = System.err;
            builder.append("WARNING! ");
        }
        else if (record.getLevel() == Level.SEVERE)
        {
            stream = System.err;
            builder.append("ERROR! ");
        }
        else
        {
            stream = System.out;
        }
        builder.append(record.getMessage());
        stream.println(builder.toString());
    }
}
