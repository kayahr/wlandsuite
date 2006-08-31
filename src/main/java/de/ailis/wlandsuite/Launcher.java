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

import de.ailis.wlandsuite.cli.CLIProg;



/**
 * The general application launcher.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Launcher extends CLIProg
{
    /**
     * @see de.ailis.wlandsuite.cli.CLIProg#run(java.lang.String[])
     */
    
    @Override
    protected void run(String[] params)
    {
        String[] newArgs;
        String command;
        
        if (params.length == 0)
        {
            wrongUsage("No command specified");
        }
        command = params[0];        
        
        newArgs = new String[params.length - 1];
        for (int i = 0; i < params.length - 1; i++)
        {
            newArgs[i] = params[i + 1];
        }
        
        if ("decodepic".equals(command))
        {
            DecryptGame.main(newArgs);
            System.exit(0);
        }
        else if ("encodepic".equals(command))
        {
            EncodePic.main(newArgs);
            System.exit(0);
        }
        else if ("unpackwlf".equals(command))
        {
            UnpackWlf.main(newArgs);
            System.exit(0);
        }
        else if ("packwlf".equals(command))
        {
            PackWlf.main(newArgs);
            System.exit(0);
        }
        else if ("unpackcpa".equals(command))
        {
            UnpackCpa.main(newArgs);
            System.exit(0);
        }
        else if ("packcpa".equals(command))
        {
            PackCpa.main(newArgs);
            System.exit(0);
        }
        else if ("unpackcurs".equals(command))
        {
            UnpackCurs.main(newArgs);
            System.exit(0);
        }
        else if ("packcurs".equals(command))
        {
            PackCurs.main(newArgs);
            System.exit(0);
        }
        else if ("unpackfnt".equals(command))
        {
            UnpackFnt.main(newArgs);
            System.exit(0);
        }
        else if ("packfnt".equals(command))
        {
            PackFnt.main(newArgs);
            System.exit(0);
        }
        else if ("packhtds".equals(command))
        {
            PackHtds.main(newArgs);
            System.exit(0);
        }
        else if ("unpackhtds".equals(command))
        {
            UnpackHtds.main(newArgs);
            System.exit(0);
        }
        else if ("packpics".equals(command))
        {
            PackPics.main(newArgs);
            System.exit(0);
        }
        else if ("unpackpics".equals(command))
        {
            UnpackPics.main(newArgs);
            System.exit(0);
        }
        else if ("fixwloffsets".equals(command))
        {
            FixWlOffsets.main(newArgs);
            System.exit(0);
        }
        else
        {
            wrongUsage("Unknown command: " + command);
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
        Launcher launcher;
        
        launcher = new Launcher();
        launcher.start(args);
    }
}
