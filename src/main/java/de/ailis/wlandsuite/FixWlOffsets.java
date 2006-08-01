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
import java.util.List;

import de.ailis.wlandsuite.cli.CLIProg;
import de.ailis.wlandsuite.exe.WlExe;
import de.ailis.wlandsuite.htds.Htds;
import de.ailis.wlandsuite.pics.Pics;


/**
 * Fixes the offsets in the WL.EXE. The unpacked EXE is needed.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class FixWlOffsets extends CLIProg
{
    /** The wasteland directory */
    private String wlDir;


    /**
     * @see de.ailis.wlandsuite.cli.CLIProg#run(java.lang.String[])
     */

    @Override
    protected void run(String[] params) throws IOException
    {
        WlExe wl;
        List<Integer> oldOffsets, newOffsets;
        File file;
        int oldOffset, newOffset;
        boolean changed = false;

        // Read wasteland directory parameter
        if (params.length > 1)
        {
            wrongUsage("Too many parameters");
        }
        if (params.length == 0)
        {
            wrongUsage("No wasteland directory specified");
        }
        this.wlDir = new File(params[0]).getPath() + File.separatorChar;

        // Open the wl.exe
        wl = new WlExe(new File(this.wlDir + "wl.exe"));
        try
        {
            // Fix HTDS1 offsets
            file = new File(this.wlDir + "allhtds1");
            if (file.length() > 34307)
            {
                warn("allhtds1 file is larger then 34307 bytes. This can cause trouble");
            }
            oldOffsets = wl.getHtds1Offsets();
            newOffsets = Htds.getMsqOffsets(file);
            if (!oldOffsets.equals(newOffsets))
            {
                for (int i = 0; i < oldOffsets.size(); i++)
                {
                    oldOffset = oldOffsets.get(i).intValue();
                    newOffset = newOffsets.get(i).intValue();
                    if (oldOffset != newOffset)
                    {
                        info("Adjusting HTDS1 bank " + i
                            + " offset from " + oldOffset + " to " + newOffset);
                    }
                    wl.setHtds1Offsets(newOffsets);
                    changed = true;
                }
            }

            // Fix HTDS2 offsets
            file = new File(this.wlDir + "allhtds2");
            if (file.length() > 39230)
            {
                warn("allhtds2 file is larger then 39230 bytes. This can cause trouble");
            }
            oldOffsets = wl.getHtds2Offsets();
            newOffsets = Htds.getMsqOffsets(new File(this.wlDir + "allhtds2"));
            if (!oldOffsets.equals(newOffsets))
            {
                for (int i = 0; i < oldOffsets.size(); i++)
                {
                    oldOffset = oldOffsets.get(i).intValue();
                    newOffset = newOffsets.get(i).intValue();
                    if (oldOffset != newOffset)
                    {
                        info("Adjusting HTDS2 bank " + i
                            + " offset from " + oldOffset + " to " + newOffset);
                    }
                    wl.setHtds2Offsets(newOffsets);
                    changed = true;
                }
            }

            // Fix PICS1 offsets
            file = new File(this.wlDir + "allpics1");
            if (file.length() > 105866)
            {
                warn("allpics1 file is larger then 105866 bytes. This can cause trouble");
            }
            oldOffsets = wl.getPics1Offsets();
            newOffsets = Pics.getMsqOffsets(file);
            if (!oldOffsets.equals(newOffsets))
            {
                for (int i = 0; i < oldOffsets.size(); i++)
                {
                    oldOffset = oldOffsets.get(i).intValue();
                    newOffset = newOffsets.get(i).intValue();
                    if (oldOffset != newOffset)
                    {
                        info("Adjusting PICS1 offset " + i
                            + " from " + oldOffset + " to " + newOffset);
                    }
                    wl.setPics1Offsets(newOffsets);
                    changed = true;
                }
            }

            // Fix PICS2 offsets
            file = new File(this.wlDir + "allpics2");
            oldOffsets = wl.getPics2Offsets();
            newOffsets = Pics.getMsqOffsets(file);
            if (!oldOffsets.equals(newOffsets))
            {
                for (int i = 0; i < oldOffsets.size(); i++)
                {
                    oldOffset = oldOffsets.get(i).intValue();
                    newOffset = newOffsets.get(i).intValue();
                    if (oldOffset != newOffset)
                    {
                        info("Adjusting PICS2 offset " + i
                            + " from " + oldOffset + " to " + newOffset);
                    }
                    wl.setPics2Offsets(newOffsets);
                    changed = true;
                }
            }
        }
        finally
        {
            wl.close();
        }
        
        if (!changed)
        {
            info("No offsets need to be fixed");
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
        FixWlOffsets app;

        app = new FixWlOffsets();
        app.setHelp("help/fixwloffsets.txt");
        app.setProgName("fixwloffsets");
        app.start(args);
    }
}
