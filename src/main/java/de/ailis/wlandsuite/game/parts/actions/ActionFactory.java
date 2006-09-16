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
 * IMPLIED, INCLUDING BUT NOT LIMIED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package de.ailis.wlandsuite.game.parts.actions;

import java.io.IOException;

import org.dom4j.Element;

import de.ailis.wlandsuite.game.parts.SpecialActionTable;
import de.ailis.wlandsuite.io.SeekableInputStream;
import de.ailis.wlandsuite.rawgame.GameException;


/**
 * ActionFactory
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class ActionFactory
{
    /**
     * Private constructor
     */

    private ActionFactory()
    {
        // Empty. Static class.
    }


    /**
     * Reads an action from the specified input stream and returns it. The
     * action class must be specified so this factory method knows which action
     * implementation must be used.
     * 
     * @param actionClass
     *            The action class
     * @param stream
     *            The input stream
     * @param specialActionTable
     *            The special action table
     * @return The action
     * @throws IOException
     */

    public static Action read(int actionClass, SeekableInputStream stream,
        SpecialActionTable specialActionTable) throws IOException
    {
        switch (actionClass)
        {
            case 1:
                return PrintAction.read(stream);

            case 2:
                return CheckAction.read(stream);

            case 3:
                return EncounterAction.read(stream);

            case 4:
                return MaskAction.read(stream);

            case 5:
                return LootAction.read(stream);

            case 6:
                return SpecialActionFactory.read(stream, specialActionTable);

            case 8:
                return DialogueAction.read(stream);

            case 9:
                return RadiationAction.read(stream);

            case 0xa:
                return TransitionAction.read(stream);

            case 0xb:
                return ImpassableAction.read(stream);

            case 0xc:
                return AlterAction.read(stream);

            case 0xf:
                return EncounterAction.read(stream);

            default:
                throw new GameException("Unknown action class: " + actionClass);
        }
    }


    /**
     * Reads an action from the specified XML element and returns it. The action
     * class must be specified so this factory method knows which action
     * implementation must be used.
     * 
     * @param actionClass
     *            The action class
     * @param element
     *            The XML element
     * @return The action
     */

    public static Action read(int actionClass, Element element)
    {
        switch (actionClass)
        {
            case 1:
                return PrintAction.read(element);

            case 2:
                return CheckAction.read(element);

            case 3:
                return EncounterAction.read(element);

            case 4:
                return MaskAction.read(element);

            case 5:
                return LootAction.read(element);

            case 6:
                return SpecialActionFactory.read(element);

            case 8:
                return DialogueAction.read(element);

            case 9:
                return RadiationAction.read(element);

            case 0xa:
                return TransitionAction.read(element);

            case 0xb:
                return ImpassableAction.read(element);

            case 0xc:
                return AlterAction.read(element);

            case 0xf:
                return EncounterAction.read(element);

            default:
                throw new GameException("Unknown action class: " + actionClass);
        }
    }
}
