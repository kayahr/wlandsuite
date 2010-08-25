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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import de.ailis.wlandsuite.common.exceptions.GameException;
import de.ailis.wlandsuite.game.RotatingXorInputStream;
import de.ailis.wlandsuite.game.RotatingXorOutputStream;
import de.ailis.wlandsuite.game.parts.Char;
import de.ailis.wlandsuite.game.parts.Parties;
import de.ailis.wlandsuite.game.parts.Unknown;
import de.ailis.wlandsuite.io.SeekableInputStream;
import de.ailis.wlandsuite.io.SeekableOutputStream;
import de.ailis.wlandsuite.utils.StringUtils;
import de.ailis.wlandsuite.utils.XmlUtils;


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

    /** The unknown data at position 0x38 */
    private Unknown unknown38;

    /** The unknown data at position 0x7A */
    private Unknown unknown7A;

    /** The unknown data at position 0x82 */
    private Unknown unknown82;

    /** The unknown data at position 0x85 */
    private Unknown unknown85;

    /** The unknown data at position 0xF9 */
    private Unknown unknownF9;

    /** The minute of the current time */
    private int minute;

    /** The hour of the current time */
    private int hour;

    /** The serial number of the savegame */
    private long serial;

    /** The parties */
    private Parties parties;

    /** The characters */
    private final List<Char> characters = new ArrayList<Char>(7);


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
     *             When file operation fails.
     */

    public static Savegame read(final SeekableInputStream stream) throws IOException
    {
        byte[] headerBytes;
        String header;
        SeekableInputStream xorStream;

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

        return readData(xorStream);
    }


    /**
     * Reads the savegame data. This method is used internally by the read() and
     * readHacked() methods.
     *
     * @param stream
     *            The input stream
     * @return The save game
     * @throws IOException
     *             When file operation fails.
     */

    private static Savegame readData(final SeekableInputStream stream)
        throws IOException
    {
        Savegame savegame;
        int viewportX, viewportY, currentMap;
        int currentMembers, currentParty, totalMembers, totalGroups;

        savegame = new Savegame();

        // Read the parties
        savegame.parties = Parties.read(stream);

        // Read the unknown block at position 0x38
        savegame.unknown38 = Unknown.read(stream, 64);

        // Read the viewport coordinates
        viewportX = stream.readSignedByte();
        viewportY = stream.readSignedByte();

        // Read the unknown block at position 0x7A
        savegame.unknown7A = Unknown.read(stream, 3);

        currentMembers = stream.read();
        currentParty = stream.read();

        currentMap = stream.read();

        totalMembers = stream.read();
        totalGroups = stream.read();

        savegame.unknown82 = Unknown.read(stream, 1);

        savegame.minute = stream.read();
        savegame.hour = stream.read();

        savegame.unknown85 = Unknown.read(stream, 112);

        savegame.serial = stream.readInt();

        savegame.unknownF9 = Unknown.read(stream, 7);

        // Correct the parties object
        savegame.parties.get(currentParty).setX(viewportX + 9);
        savegame.parties.get(currentParty).setY(viewportY + 4);
        savegame.parties.get(currentParty).setMap(currentMap);
        while (savegame.parties.size() > totalGroups + 1)
        {
            savegame.parties.remove(savegame.parties.size() - 1);
        }
        savegame.parties.setCurrentParty(currentParty);
        if (savegame.parties.getTotalMembers() != totalMembers)
        {
            throw new GameException(
                "Total members mismatch. Looks like we did not understand this field...");
        }
        if (currentMembers != savegame.parties.get(currentParty).size())
        {
            throw new GameException(
                "Current members mismatch. Looks like we did not understand this field...");
        }

        // Read the seven characters
        for (int i = 0; i < 7; i++)
        {
            savegame.characters.add(Char.read(stream));
        }

        // Skip the padding
        stream.skip(2560);

        // Return the newly created Game Map
        return savegame;
    }


    /**
     * Reads an external savegame file (for Displacer's hacked EXE file).
     *
     * @param stream
     *            The input stream
     * @return The savegame
     * @throws IOException
     *             When file operation fails.
     */

    public static Savegame readHacked(final InputStream stream) throws IOException
    {
        return readData(new SeekableInputStream(stream));
    }


    /**
     * Writes the savegame to the specified output stream.
     *
     * @param stream
     *            The output stream
     * @param disk
     *            The disk id (0 or 1)
     * @throws IOException
     *             When file operation fails.
     */

    public void write(final OutputStream stream, final int disk) throws IOException
    {
        SeekableOutputStream seekStream;

        // Write the MSQ header
        stream.write("msq".getBytes());
        stream.write('0' + disk);

        seekStream = new SeekableOutputStream(new RotatingXorOutputStream(
            stream));

        writeData(seekStream);

        for (int i = 0; i < 2560; i++)
        {
            stream.write(0);
        }
    }


    /**
     * Writes the savegame data to the specified stream. This method is used
     * internally by the write() and writeHacked() method.
     *
     * @param stream
     *            The output stream
     * @throws IOException
     *             When file operation fails.
     */

    private void writeData(final SeekableOutputStream stream) throws IOException
    {
        // Write the parties
        this.parties.write(stream);

        // Write the unknown data at position 0x38
        this.unknown38.write(stream);

        // Write the view port position
        stream
            .write(this.parties.get(this.parties.getCurrentParty()).getX() - 9);
        stream
            .write(this.parties.get(this.parties.getCurrentParty()).getY() - 4);

        // Write the unknown data at position 0x7A
        this.unknown7A.write(stream);

        stream.write(this.parties.get(this.parties.getCurrentParty()).size());
        stream.write(this.parties.getCurrentParty());

        stream.write(this.parties.get(this.parties.getCurrentParty()).getMap());

        stream.write(this.parties.getTotalMembers());
        stream.write(this.parties.size() - 1);

        this.unknown82.write(stream);

        stream.write(this.minute);
        stream.write(this.hour);

        this.unknown85.write(stream);

        stream.writeInt(this.serial);

        this.unknownF9.write(stream);

        // Write the characters
        for (final Char character: this.characters)
        {
            character.write(stream);
        }
        stream.flush();
    }


    /**
     * Writes the savegame to an external save file (Compatibly to Displacer's
     * hacked EXE file).
     *
     * @param stream
     *            The output stream
     * @throws IOException
     *             When file operation fails.
     */

    public void writeHacked(final OutputStream stream) throws IOException
    {
        writeData(new SeekableOutputStream(stream));
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

    public static Savegame read(final Element element)
    {
        Savegame savegame;
        String[] parts;

        savegame = new Savegame();

        parts = element.attributeValue("time", "12:00").split(":");
        savegame.hour = Integer.parseInt(parts[0]);
        savegame.minute = Integer.parseInt(parts[1]);
        savegame.serial = StringUtils.toInt(element.attributeValue("serial",
            "0"));

        // Read the parties
        savegame.parties = Parties.read(element.element("parties"));

        // Read the unknown data at position 0x38
        savegame.unknown38 = Unknown.read(element.element("unknown38"), 64);

        // Read the unknown data at position 0x7A
        savegame.unknown7A = Unknown.read(element.element("unknown7A"), 3);

        // Read the unknown data at position 0x82
        savegame.unknown82 = Unknown.read(element.element("unknown82"), 1);

        // Read the unknown data at position 0x85
        savegame.unknown85 = Unknown.read(element.element("unknown85"), 112);

        // Read the unknown data at position 0xF9
        savegame.unknownF9 = Unknown.read(element.element("unknownF9"), 7);

        // Read the characters
        for (final Object item: element.element("characters").elements("character"))
        {
            final Element subElement = (Element) item;

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
     */

    public static Savegame readXml(final InputStream stream)
    {
        Document document;
        Element element;

        document = XmlUtils.readDocument(stream);
        element = document.getRootElement();
        return read(element);
    }


    /**
     * @see de.ailis.wlandsuite.game.blocks.GameBlock#toXml()
     */

    @Override
    public Element toXml()
    {
        Element element, subElement;

        // Create the root element
        element = XmlUtils.createElement("savegame");
        element.addAttribute("time", String.format("%02d:%02d", new Object[] {
            this.hour, this.minute }));
        element.addAttribute("serial", Long.toString(this.serial));

        // Add the parties element
        element.add(this.parties.toXml());

        // Add the unknown block at position 0x38
        element.add(this.unknown38.toXml("unknown38"));

        // Add the unknown block at position 0x7A
        element.add(this.unknown7A.toXml("unknown7A"));

        // Add the unknown block at position 0x82
        element.add(this.unknown82.toXml("unknown82"));

        // Add the unknown block at position 0x85
        element.add(this.unknown85.toXml("unknown85"));

        // Add the unknown block at position 0xF9
        element.add(this.unknownF9.toXml("unknownF9"));

        // Write the characters
        subElement = XmlUtils.createElement("characters");
        int id = 1;
        for (final Char characters: this.characters)
        {
            subElement.add(characters.toXml(id));
            id++;
        }
        element.add(subElement);

        // Return the XMl element
        return element;
    }
}
