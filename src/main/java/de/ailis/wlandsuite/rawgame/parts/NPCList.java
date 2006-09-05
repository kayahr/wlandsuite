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

package de.ailis.wlandsuite.rawgame.parts;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.ailis.wlandsuite.io.BitInputStream;
import de.ailis.wlandsuite.io.BitInputStreamWrapper;
import de.ailis.wlandsuite.io.BitOutputStreamWrapper;
import de.ailis.wlandsuite.rawgame.GameException;


/**
 * The NPC list
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class NPCList extends AbstractPart
{
    /** The npcs */
    private List<Char> npcs = new ArrayList<Char>();


    /**
     * Constructor
     * 
     * @param bytes
     *            The game block data
     * @param offset
     *            The offset of the part in the game block
     */

    public NPCList(byte[] bytes, int offset)
    {
        ByteArrayInputStream byteStream;
        BitInputStreamWrapper bitStream;

        this.offset = offset;
        byteStream = new ByteArrayInputStream(bytes, offset, bytes.length
            - offset);
        bitStream = new BitInputStreamWrapper(byteStream);
        try
        {
            // Skip first offset. It's always 0
            bitStream.readWord();
            offset += 2;

            int quantity = (bitStream.readWord() - offset) / 2;

            this.size = 2 + quantity * 0x102;

            for (int i = 0; i < quantity; i++)
            {
                this.npcs.add(new Char(bytes, offset + quantity * 2));
                offset += 0x100;
            }
        }
        catch (IOException e)
        {
            throw new GameException(e.toString(), e);
        }
    }


    /**
     * Creates the central directory from XML.
     * 
     * @param element
     *            The XML element
     */

    @SuppressWarnings("unchecked")
    public NPCList(Element element)
    {
        super();

        for (Element subElement: (List<Element>) element.elements("character"))
        {
            this.npcs.add(new Char(subElement));
        }
    }


    /**
     * @see de.ailis.wlandsuite.rawgame.parts.Part#toXml()
     */

    public Element toXml()
    {
        Element element, subElement;

        element = DocumentHelper.createElement("npcList");
        element.addAttribute("offset", Integer.toString(this.offset));
        element.addAttribute("size", Integer.toString(this.size));
        for (Char character: this.npcs)
        {
            subElement = character.toXml();
            element.add(subElement);
        }
        return element;
    }


    /**
     * @see de.ailis.wlandsuite.rawgame.parts.Part#write(java.io.OutputStream, int)
     */

    public void write(OutputStream stream, int blockOffset) throws IOException
    {
        BitOutputStreamWrapper bitStream;

        bitStream = new BitOutputStreamWrapper(stream);
        bitStream.writeWord(0);
        for (int i = 0; i < this.npcs.size(); i++)
        {
            bitStream.writeWord(blockOffset + 2 + this.npcs.size() * 2 + i
                * 0x100);
        }

        for (Char npc: this.npcs)
        {
            npc.write(bitStream, 0);
        }
        bitStream.flush();
    }


    /**
     * Checks if the specified block is a NPC list. This is needed because the
     * central directory sometimes have the same offsets for different parts.
     * This check can be used to ensure that the NPC list offset is correct.
     * 
     * @param bytes
     *            The bytes array
     * @param offset
     *            The offset in the bytes array
     * @return If block is a NPC list
     */

    public static boolean isNPCList(byte[] bytes, int offset)
    {
        BitInputStream bitStream = new BitInputStreamWrapper(
            new ByteArrayInputStream(bytes, offset, bytes.length - offset));

        try
        {
            // First offset must be 0
            if (bitStream.readWord() != 0) return false;

            // Maximum quantity is 255
            offset += 2;
            int quantity = (bitStream.readWord() - offset) / 2;
            if (quantity > 255) return false;

            // All offsets of the characters must be 0x100 bytes apart
            offset += quantity * 2;
            for (int i = 1; i < quantity; i++)
            {
                int tmp = bitStream.readWord();
                if (tmp != (offset + i * 0x100)) return false;
            }

            return true;
        }
        catch (IOException e)
        {
            return false;
        }
    }
}
