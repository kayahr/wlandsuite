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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import de.ailis.wlandsuite.utils.StringUtils;
import de.ailis.wlandsuite.utils.XmlUtils;
import org.dom4j.Element;

import de.ailis.wlandsuite.io.SeekableOutputStream;
import de.ailis.wlandsuite.rawgame.GameException;


/**
 * The special action.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class SpecialAction implements Action
{
    /** The special action to perform */
    private int action;

    /** The data bytes of the action */
    private byte[] data;


    /**
     * Creates and returns a new action by reading the data it from the
     * specified input stream.
     * 
     * @param stream
     *            The input stream to read the action data from
     * @param action
     *            The action
     * @param specialActionTable
     *            The special action table
     * @return The new action
     * @throws IOException
     */

    public static SpecialAction read(InputStream stream, int action,
        SpecialActionTable specialActionTable) throws IOException
    {
        int len, b;
        SpecialAction specialAction;
        boolean replace;
        boolean ffterm;
        ByteArrayOutputStream byteStream;

        // Create new action object
        specialAction = new SpecialAction();

        if (action >= specialActionTable.size())
        {
            throw new GameException("Action is not in the action table: "
                + action);
        }
        specialAction.action = specialActionTable.get(action);

        len = getDataLength(specialAction.action);
        ffterm = (len & 512) == 512;
        replace = (len & 256) == 256;
        len = len & 255;
        byteStream = new ByteArrayOutputStream();
        for (int i = 0; i < len; i++)
        {
            byteStream.write(stream.read());
        }
        if (ffterm)
        {
            b = stream.read();
            while (b != 255)
            {
                byteStream.write(b);
                b = stream.read();
            }
            byteStream.write(b);
        }            
        if (replace)
        {
            b = stream.read();
            byteStream.write(b);
            if (b < 253)
            {
                byteStream.write(stream.read());
            }
        }
        specialAction.data = byteStream.toByteArray();

        // Return the action
        return specialAction;
    }


    /**
     * Creates and returns a new action by reading the data from XML.
     * 
     * @param element
     *            The XML element to read
     * @return The new action
     */

    public static SpecialAction read(Element element)
    {
        SpecialAction action;
        ByteArrayOutputStream stream;

        // Create new message action
        action = new SpecialAction();

        action.action = StringUtils.toInt(element.attributeValue("action"));

        stream = new ByteArrayOutputStream();
        for (String c: element.getTextTrim().split("\\s"))
        {
            stream.write(Integer.valueOf(c, 16));
        }
        action.data = stream.toByteArray();

        // Return the new action
        return action;
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.Action#toXml(int)
     */

    public Element toXml(int id)
    {
        Element element;
        StringBuilder data;

        element = XmlUtils.createElement("special");
        element.addAttribute("id", StringUtils.toHex(id));
        element.addAttribute("action", StringUtils.toHex(this.action));
        data = new StringBuilder();
        for (byte b: this.data)
        {
            data.append(String.format("%02x ", new Object[] { b & 0xff }));
        }
        element.setText(data.toString().trim());

        return element;
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.Action#write(de.ailis.wlandsuite.io.SeekableOutputStream,
     *      de.ailis.wlandsuite.game.parts.SpecialActionTable)
     */

    public void write(SeekableOutputStream stream,
        SpecialActionTable specialActionTable) throws IOException
    {
        int action = -1;

        for (int i = 0; i < specialActionTable.size(); i++)
        {
            if (specialActionTable.get(i).intValue() == this.action)
            {
                action = i;
            }
        }
        if (action == -1)
        {
            throw new GameException("Action is not in action table: "
                + this.action);
        }
        stream.write(action);
        stream.write(this.data);
    }


    /**
     * Returns the data length for the specified action. If Bit 8 is set then it
     * means the typical action class/action byte pair (Where the action is not
     * present when action class is >= 253). If Bit 9 is set then this means
     * it needs to read the data until FF byte is found.
     * 
     * @param action
     *            The action
     * @return The data length
     */

    private static int getDataLength(int action)
    {
        switch (action)
        {
            case 0x00:
                return 0 + 512;
                
            case 0x01:
                return 6;
                
            case 0x02:
                return 4;

            case 0x03:
                return 6;
                
            case 0x04:
                return 3;
                
            case 0x06:
                return 4;
                
            case 0x07:
                return 6;
                
            case 0x09:
                return 3;
               
            case 0x0a:
                return 3;

            case 0x0b:
                return 3;
              
            case 0x0c:
                return 3;
                
            case 0x0e:
                return 1;
                
            case 0x0f:
                return 8;

            case 0x10:
                return 2;
                
            case 0x11:
                return 3;

            case 0x12:
                return 0 + 256;
                
            case 0x13:
                return 0 + 256;
                
            case 0x14:
                return 2;
                
            case 0x15:
                return 0 + 256;
                
            case 0x16:
                return 2;
                
            case 0x17:
                return 2;
                
            case 0x18:
                return 0 + 256;
                
            case 0x19:
                return 2; 

            case 0x1a:
                return 2;
                
            case 0x1b:
                return 2;
                
            case 0x1c:
                return 2;
                
            case 0x1d:
                return 3;
                
            case 0x1e:
                return 3;

            case 0x20:
                return 4;

            case 0x21:
                return 1;
                
            case 0x22:
                return 4;
                
            case 0x23:
                return 2;
                
            case 0x24:
                return 2;
                
            case 0x25:
                return 4;
                
            case 0x26:
                return 3;
                
            case 0x27:
                return 9; 
               
            case 0x28:
                return 2;
                
            case 0x29:
                return 1;
                
            case 0x2a:
                return 1;
               
            case 0x2b:
                return 4 + 512;

            default:
                throw new GameException(String.format(
                    "Don't know the data length of action: %02x",
                    new Object[] { action }));
        }
    }


    /**
     * Returns the action.
     * 
     * @return The action
     */

    public int getAction()
    {
        return this.action;
    }


    /**
     * Sets the action.
     * 
     * @param action
     *            The action to set
     */

    public void setAction(int action)
    {
        this.action = action;
    }


    /**
     * Returns the data.
     * 
     * @return The data
     */

    public byte[] getData()
    {
        return this.data;
    }


    /**
     * Sets the data.
     * 
     * @param data
     *            The data to set
     */

    public void setData(byte[] data)
    {
        this.data = data;
    }
}
