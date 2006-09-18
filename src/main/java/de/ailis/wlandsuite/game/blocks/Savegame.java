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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import de.ailis.wlandsuite.game.RotatingXorInputStream;
import de.ailis.wlandsuite.game.parts.Char;
import de.ailis.wlandsuite.io.SeekableInputStream;
import de.ailis.wlandsuite.io.SeekableOutputStream;
import de.ailis.wlandsuite.rawgame.RotatingXorOutputStream;


/**
 * Save game.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Savegame extends GameBlock implements Serializable
{
    /** Serial version UID */
    private static final long serialVersionUID = -7700742752700695254L;
    
    /** The header at the beginning of the savegame */
    private byte[] header = new byte[0x100];
    
    /** The characters */
    private List<Char> characters = new ArrayList<Char>(7);


    /**
     * Constructs a savegame
     */

    public Savegame()
    {
        super();
    }


    /**
     * Constructs a savegame by reading it from a wasteland gameX file stream.
     * The stream must point at the beginning of the MSQ block (which is at the
     * "m" of the "msq" header string.
     * 
     * @param stream
     *            The input stream
     * @return The newly constructed save game
     * @throws IOException
     */

    public static Savegame read(SeekableInputStream stream)
        throws IOException
    {
        byte[] headerBytes;
        String header;
        SeekableInputStream xorStream;
        Savegame savegame;
        
        // Read the MSQ block header and validate it
        headerBytes = new byte[4];
        stream.read(headerBytes);
        header = new String(headerBytes, "ASCII");
        if (!header.equals("msq0") && !header.equals("msq1"))
        {
            throw new IOException("No MSQ block header found at stream");
        }

        // Read/Decrypt the MSQ block
        xorStream = new SeekableInputStream(new RotatingXorInputStream(stream));
        
        savegame = new Savegame();
        
        // Read the unknown block
        xorStream.read(savegame.header);
        
        // Read the seven characters
        for (int i = 0; i < 7; i++)
        {
            savegame.characters.add(Char.read(xorStream));
        }
        
        // Skip the padding 
        stream.skip(2560);

        // Return the newly created Game Map
        return savegame;
    }


    /**
     * Writes the savegame to the specified output stream.
     * 
     * @param stream
     *            The output stream
     * @param disk
     *            The disk id (0 or 1)
     * @throws IOException
     */

    public void write(OutputStream stream, int disk) throws IOException
    {
        SeekableOutputStream seekStream;

        // Write the MSQ header
        stream.write("msq".getBytes());
        stream.write('0' + disk);

        // Write the header and the characters
        seekStream = new SeekableOutputStream(new RotatingXorOutputStream(stream));
        seekStream.write(this.header);
        for (Char character: this.characters)
        {
            character.write(seekStream);
        }
        seekStream.flush();
        for (int i = 0; i < 2560; i++)
        {
            stream.write(0);
        }
    }


    /**
     * Creates and returns a new savegame from XML.
     * 
     * @param element
     *            The XML root element
     * @return The savegame
     */

    public static Savegame read(Element element)
    {
        Savegame savegame;
        String data;
        ByteArrayOutputStream byteStream;
        
        savegame = new Savegame();
        
        // Read the header
        data = element.element("header").getTextTrim();
        byteStream = new ByteArrayOutputStream();
        for (String b: data.split("\\s"))
        {
            byteStream.write(Integer.parseInt(b, 16));
        }
        savegame.header = byteStream.toByteArray();

        // Read the characters
        for (Object item: element.element("characters").elements("character"))
        {
            Element subElement = (Element) item;
            
            savegame.characters.add(Char.read(subElement));
        }

        return savegame;
    }


    /**
     * Reads a savegame from the specified XML stream.
     * 
     * @param stream
     *            The input stream
     * @return The savegame
     * @throws IOException
     */

    public static Savegame readXml(InputStream stream) throws IOException
    {
        SAXReader reader;
        Document document;
        Element element;

        reader = new SAXReader();
        try
        {
            document = reader.read(stream);
            element = document.getRootElement();

            return read(element);
        }
        catch (DocumentException e)
        {
            throw new IOException("Unable to parse game map from XML: "
                + e.getMessage());
        }
    }


    /**
     * @see de.ailis.wlandsuite.game.blocks.GameBlock#toXml()
     */

    @Override
    public Element toXml()
    {
        Element element, subElement;
        StringWriter text;
        PrintWriter writer;

        // Create the root element
        element = DocumentHelper.createElement("savegame");

        // Create the header element
        subElement = DocumentHelper.createElement("header");

        text = new StringWriter();
        writer = new PrintWriter(text);

        writer.println();
        writer.print("    ");
        for (int i = 0; i < this.header.length; i++)
        {
            if (i > 0)
            {
                if (i % 16 == 0)
                {
                    writer.println();
                }
                if ((i < this.header.length) && (i % 4 == 0))
                {
                    writer.print("    ");
                }
                else
                {
                    writer.print(" ");
                }
            }
            writer.format("%02x", new Object[] { this.header[i] });
        }
        writer.println();
        writer.print("  ");

        subElement.add(DocumentHelper.createText(text.toString()));
        element.add(subElement);
        
        // Write the characters
        subElement = DocumentHelper.createElement("characters");
        int id = 0;
        for (Char characters: this.characters)
        {
            subElement.add(characters.toXml(id));
            id++;
        }
        element.add(subElement);

        // Return the XMl element
        return element;
    }
}
