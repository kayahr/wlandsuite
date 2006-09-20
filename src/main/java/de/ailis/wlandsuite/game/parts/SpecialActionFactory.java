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

package de.ailis.wlandsuite.game.parts;

import java.io.IOException;

import org.dom4j.Element;

import de.ailis.wlandsuite.io.SeekableInputStream;
import de.ailis.wlandsuite.rawgame.GameException;


/**
 * Special actions are doctors, shops, libraries, ranger centers and special EXE
 * actions.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class SpecialActionFactory
{
    /**
     * Private constructor
     */

    private SpecialActionFactory()
    {
        super();
    }


    /**
     * Creates and returns a new Special Action by reading its data from the
     * specified stream.
     * 
     * @param stream
     *            The input stream
     * @param specialActionTable The special action table
     * @return The new Dialogue Action
     * @throws IOException
     */

    public static Action read(SeekableInputStream stream,
        SpecialActionTable specialActionTable) throws IOException
    {
        int type;

        type = stream.read();
        switch (type)
        {
            case 0x80:
                return DoctorAction.read(stream);

            case 0x81:
                return StoreAction.read(stream);

            case 0x82:
                return LibraryAction.read(stream);

            case 0x83:
                return RangerCenterAction.read(stream);

            default:
                if (type > 127)
                {
                    throw new GameException("Unknown special action type: "
                        + type);
                }
                return SpecialAction.read(stream, type, specialActionTable);
        }
    }


    /**
     * Creates and returns a Special Action by reading its data from XML.
     * 
     * @param element
     *            The XML element
     * @return The Dialogue Action
     */

    public static Action read(Element element)
    {
        String type;

        type = element.getName();

        if (type.equals("doctor"))
        {
            return DoctorAction.read(element);
        }
        else if (type.equals("store"))
        {
            return StoreAction.read(element);
        }
        else if (type.equals("library"))
        {
            return LibraryAction.read(element);
        }
        else if (type.equals("rangerCenter"))
        {
            return RangerCenterAction.read(element);
        }
        else
        {
            return SpecialAction.read(element);
        }
    }
}
