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

package de.ailis.wlandsuite.game.blocks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import de.ailis.wlandsuite.game.GameBlockType;
import de.ailis.wlandsuite.game.GameException;
import de.ailis.wlandsuite.game.parts.ActionClassMap;
import de.ailis.wlandsuite.game.parts.ActionSelectorMap;
import de.ailis.wlandsuite.game.parts.Part;
import de.ailis.wlandsuite.game.parts.UnknownPart;


/**
 * An abstract Game Block which implements common stuff for all real game block
 * types.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public abstract class AbstractGameBlock implements GameBlock
{
    /** The block type */
    protected GameBlockType type;

    /** The list of block parts */
    protected List<Part> parts;


    /**
     * Constructor
     * 
     * @param type
     *            The game block type
     */

    protected AbstractGameBlock(GameBlockType type)
    {
        this.parts = new ArrayList<Part>();
        this.type = type;
    }


    /**
     * Processes the children of an XML element.
     * 
     * @param element
     *            The XML element
     */

    @SuppressWarnings("unchecked")
    protected void processChildren(Element element)
    {
        String tagName;
        Part part;
        ActionClassMap actionClassMap = null;

        for (Element child: (List<Element>) element.elements())
        {
            tagName = child.getName();

            if (tagName.equals("unknown"))
            {
                part = new UnknownPart(child);
            }
            else if (tagName.equals("actionClassMap"))
            {
                part = new ActionClassMap(child, ((GameMap) this).getMapSize());
                actionClassMap = (ActionClassMap) part;
            }
            else if (tagName.equals("actionSelectorMap"))
            {
                part = new ActionSelectorMap(child, ((GameMap) this)
                    .getMapSize(), actionClassMap);
            }
            else
            {
                throw new GameException("Unknown game part type: " + tagName);
            }

            this.parts.add(part);
        }
    }


    /**
     * Creates unknown parts to cover all the bytes which are not covered by
     * known parts.
     * 
     * @param bytes
     *            The bytes of the block
     */

    protected void createUnknownParts(byte[] bytes)
    {
        int start, end;

        Collections.sort(this.parts);
        start = 0;
        for (int i = 0, max = this.parts.size(); i < max; i++)
        {
            Part part = this.parts.get(i);
            end = part.getOffset();
            if (start > end)
            {
                throw new GameException("Part " + part + " overlaps part "
                    + this.parts.get(i - 1));
            }
            if (start != end)
            {
                this.parts.add(new UnknownPart(bytes, start, end - start));
            }
            start = end + part.getSize();
        }
        end = bytes.length;
        if (start != end)
        {
            this.parts.add(new UnknownPart(bytes, start, end - start));
        }
        Collections.sort(this.parts);
    }


    /**
     * Writes the block to a stream as XML
     * 
     * @param stream
     *            The output stream
     * @throws IOException
     */

    public void writeXml(OutputStream stream) throws IOException
    {
        XMLWriter writer;
        Document document;
        OutputFormat format;

        format = OutputFormat.createPrettyPrint();
        format.setTrimText(false);

        writer = new XMLWriter(stream, format);
        try
        {
            document = DocumentHelper.createDocument(toXml());
            writer.write(document);
        }
        finally
        {
            writer.close();
        }
    }


    /**
     * @see de.ailis.wlandsuite.game.blocks.GameBlock#getType()
     */

    public GameBlockType getType()
    {
        return this.type;
    }


    /**
     * Creates the game block data by writing all parts to a byte array and
     * returning it.
     * 
     * @return The game block data
     */

    protected byte[] createBlockData()
    {
        ByteArrayOutputStream byteStream;

        // Create the byte array
        byteStream = new ByteArrayOutputStream();
        for (Part part: this.parts)
        {
            // Verify offset of unknown block
            if (part instanceof UnknownPart)
            {
                int realOffset, targetOffset;

                realOffset = byteStream.size();
                targetOffset = part.getOffset();

                // Pad with zeros to match target offset
                /*
                 * for (int i = realOffset; i < targetOffset; i++) {
                 * byteStream.write(0); }
                 */

                if (realOffset != targetOffset)
                {
                    System.err.println("oho, offset mismatch: " + realOffset
                        + " != " + targetOffset);
                }
            }

            try
            {
                part.write(byteStream);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e.toString(), e);
            }
        }
        return byteStream.toByteArray();
    }
}
