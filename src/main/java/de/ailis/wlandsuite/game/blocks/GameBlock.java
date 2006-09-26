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

import java.io.IOException;
import java.io.OutputStream;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;


/**
 * A base class for all game blocks providing common stuff.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public abstract class GameBlock
{
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
            Element rootElement = toXml();
            rootElement.addAttribute("xmlns:xsi",
                "http://www.w3.org/2001/XMLSchema-instance");
            rootElement
                .addAttribute(
                    "xsi:schemaLocation",
                    "http://ailis.de/wlandsuite http://www.ailis.de/~k/software/projects/wlandsuite/schemas/wlandsuite.xsd");
            document = DocumentHelper.createDocument(rootElement);
            writer.write(document);
        }
        finally
        {
            writer.close();
        }
    }


    /**
     * Returns the game map as XML.
     * 
     * @return The map as XML
     */

    public abstract Element toXml();
}
