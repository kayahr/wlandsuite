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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import de.ailis.wlandsuite.utils.XMLUtils;
import org.dom4j.Element;

import de.ailis.wlandsuite.io.BitInputStreamWrapper;
import de.ailis.wlandsuite.io.BitOutputStreamWrapper;
import de.ailis.wlandsuite.rawgame.GameException;
import de.ailis.wlandsuite.utils.StringUtils;


/**
 * Character data
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Char extends AbstractPart
{
    /** The character name */
    private String name;

    /** The strength */
    private int strength;

    /** The iq */
    private int iq;

    /** The luck */
    private int luck;

    /** The speed */
    private int speed;

    /** The agility */
    private int agility;

    /** The dexterity */
    private int dexterity;

    /** The charisma */
    private int charisma;

    /** The current money */
    private int money;

    /** The gender (0 = male, 1 = female) */
    private int gender;

    /**
     * The nationality (0 = US, 1 = Russian, 2 = Mexican, 3 = Indian, 4 =
     * Chinese
     */
    private int nationality;

    /** The current armor class */
    private int ac;

    /** The maximum constitution */
    private int maxCon;

    /** The current constitution */
    private int con;

    /** The equipped weapon (Index in item list) */
    private int weapon;

    /** The skill points */
    private int skillPoints;

    /** The experience points */
    private int experience;

    /** The character level */
    private int level;

    /** The equipped armor (Index in item list) */
    private int armor;

    /** The last con before character got unconscious */
    private int lastCon;

    /** Bitmap with afflictions */
    private int afflictions;

    /** If character is an NPC */
    private boolean npc;

    /** Unknown */
    private int unknown2A;

    /** Chance of refusing to use an item */
    private int itemRefuse;

    /** Chance of refusing to use a skill */
    private int skillRefuse;

    /** Chance of refusing to use an attribute */
    private int attribRefuse;

    /** Chance of refusing a trade */
    private int tradeRefuse;

    /** Unknown */
    private int unknown2F;

    /** String to print when NPC joins the party */
    private int joinString;

    /**
     * Willingness to carry out a Use or Trade command. Checked against the
     * target characters charisma
     */
    private int willingness;

    /** The rank */
    private String rank;

    /** The skill list */
    private byte[] skills;

    /** The item list */
    private byte[] items;


    /**
     * Constructor
     * 
     * @param bytes
     *            The game block data
     * @param offset
     *            The offset of the part in the game block
     */

    public Char(byte[] bytes, int offset)
    {
        BitInputStreamWrapper bitStream;
        byte[] tmp;

        this.size = 0x100;
        this.offset = offset;
        bitStream = new BitInputStreamWrapper(new ByteArrayInputStream(bytes,
            offset, this.size));
        try
        {
            tmp = new byte[14];
            bitStream.read(tmp);
            this.name = getString(tmp);
            this.strength = bitStream.read();
            this.iq = bitStream.read();
            this.luck = bitStream.read();
            this.speed = bitStream.read();
            this.agility = bitStream.read();
            this.dexterity = bitStream.read();
            this.charisma = bitStream.read();
            this.money = bitStream.readInt3();
            this.gender = bitStream.read();
            this.nationality = bitStream.read();
            this.ac = bitStream.read();
            this.maxCon = bitStream.readWord();
            this.con = bitStream.readWord();
            this.weapon = bitStream.read();
            this.skillPoints = bitStream.read();
            this.experience = bitStream.readInt3();
            this.level = bitStream.read();
            this.armor = bitStream.read();
            this.lastCon = bitStream.readWord();
            this.afflictions = bitStream.read();
            this.npc = bitStream.read() == 1;
            this.unknown2A = bitStream.read();
            this.itemRefuse = bitStream.read();
            this.skillRefuse = bitStream.read();
            this.attribRefuse = bitStream.read();
            this.tradeRefuse = bitStream.read();
            this.unknown2F = bitStream.read();
            this.joinString = bitStream.read();
            this.willingness = bitStream.read();

            tmp = new byte[25];
            bitStream.read(tmp);
            this.rank = getString(tmp);

            bitStream.skip(53);

            this.skills = new byte[60];
            bitStream.read(this.skills);

            bitStream.skip(1);

            this.items = new byte[60];
            bitStream.read(this.items);

            bitStream.skip(7);
        }
        catch (IOException e)
        {
            throw new GameException(e.toString(), e);
        }
    }


    /**
     * Converts a null-terminated string into a normal string.
     * 
     * @param data
     *            The null-terminated data
     * @return The string
     */

    private String getString(byte[] data)
    {
        int pos = 0;

        while (pos < data.length && data[pos] != 0)
        {
            pos++;
        }
        try
        {
            return new String(data, 0, pos, "ASCII");
        }
        catch (UnsupportedEncodingException e)
        {
            // Can't happen
            return null;
        }
    }


    /**
     * Creates the character from XML.
     * 
     * @param element
     *            The XML element
     */

    @SuppressWarnings("unchecked")
    public Char(Element element)
    {
        super();

        ByteArrayOutputStream stream;

        this.name = StringUtils.unescape(element.attributeValue("name"),
            "ASCII");
        this.strength = Integer.parseInt(element.attributeValue("strength"));
        this.iq = Integer.parseInt(element.attributeValue("iq"));
        this.luck = Integer.parseInt(element.attributeValue("luck"));
        this.speed = Integer.parseInt(element.attributeValue("speed"));
        this.agility = Integer.parseInt(element.attributeValue("agility"));
        this.dexterity = Integer.parseInt(element.attributeValue("dexterity"));
        this.charisma = Integer.parseInt(element.attributeValue("charisma"));
        this.money = Integer.parseInt(element.attributeValue("money"));
        this.gender = Integer.parseInt(element.attributeValue("gender"));
        this.nationality = Integer.parseInt(element
            .attributeValue("nationality"));
        this.ac = Integer.parseInt(element.attributeValue("ac"));
        this.maxCon = Integer.parseInt(element.attributeValue("maxCon"));
        this.con = Integer.parseInt(element.attributeValue("con"));
        this.weapon = Integer.parseInt(element.attributeValue("weapon"));
        this.skillPoints = Integer.parseInt(element
            .attributeValue("skillPoints"));
        this.experience = Integer
            .parseInt(element.attributeValue("experience"));
        this.level = Integer.parseInt(element.attributeValue("level"));
        this.armor = Integer.parseInt(element.attributeValue("armor"));
        this.lastCon = Integer.parseInt(element.attributeValue("lastCon"));
        this.afflictions = Integer.parseInt(element
            .attributeValue("afflictions"));
        this.npc = Boolean.parseBoolean(element.attributeValue("npc"));
        this.unknown2A = Integer.parseInt(element.attributeValue("unknown2A"));
        this.itemRefuse = Integer
            .parseInt(element.attributeValue("itemRefuse"));
        this.skillRefuse = Integer.parseInt(element
            .attributeValue("skillRefuse"));
        this.attribRefuse = Integer.parseInt(element
            .attributeValue("attribRefuse"));
        this.tradeRefuse = Integer.parseInt(element
            .attributeValue("tradeRefuse"));
        this.unknown2F = Integer.parseInt(element.attributeValue("unknown2F"));
        this.joinString = Integer
            .parseInt(element.attributeValue("joinString"));
        this.willingness = Integer.parseInt(element
            .attributeValue("willingness"));
        this.rank = StringUtils.unescape(element.attributeValue("rank"),
            "ASCII");

        // Read skills
        stream = new ByteArrayOutputStream();
        for (String c: element.element("skills").getTextTrim().split("\\s"))
        {
            int b = Integer.valueOf(c, 16);
            stream.write(b);
        }
        this.skills = stream.toByteArray();
        if (this.skills.length != 60)
        {
            throw new GameException("Skill list of character " + this.name
                + " to large");
        }

        // Read items
        stream = new ByteArrayOutputStream();
        for (String c: element.element("items").getTextTrim().split("\\s"))
        {
            int b = Integer.valueOf(c, 16);
            stream.write(b);
        }
        this.items = stream.toByteArray();
        if (this.items.length != 60)
        {
            throw new GameException("Item list of character " + this.name
                + " to large");
        }

    }


    /**
     * @see de.ailis.wlandsuite.rawgame.parts.Part#toXml()
     */

    public Element toXml()
    {
        Element element, subElement;

        element = XMLUtils.createElement("character");
        element.addAttribute("name", StringUtils.escape(this.name, "ASCII"));
        element.addAttribute("strength", Integer.toString(this.strength));
        element.addAttribute("iq", Integer.toString(this.iq));
        element.addAttribute("luck", Integer.toString(this.luck));
        element.addAttribute("speed", Integer.toString(this.speed));
        element.addAttribute("agility", Integer.toString(this.agility));
        element.addAttribute("dexterity", Integer.toString(this.dexterity));
        element.addAttribute("charisma", Integer.toString(this.charisma));
        element.addAttribute("money", Integer.toString(this.money));
        element.addAttribute("gender", Integer.toString(this.gender));
        element.addAttribute("nationality", Integer.toString(this.nationality));
        element.addAttribute("ac", Integer.toString(this.ac));
        element.addAttribute("maxCon", Integer.toString(this.maxCon));
        element.addAttribute("con", Integer.toString(this.con));
        element.addAttribute("weapon", Integer.toString(this.weapon));
        element.addAttribute("skillPoints", Integer.toString(this.skillPoints));
        element.addAttribute("experience", Integer.toString(this.experience));
        element.addAttribute("level", Integer.toString(this.level));
        element.addAttribute("armor", Integer.toString(this.armor));
        element.addAttribute("lastCon", Integer.toString(this.lastCon));
        element.addAttribute("afflictions", Integer.toString(this.afflictions));
        element.addAttribute("npc", Boolean.toString(this.npc));
        element.addAttribute("afflictions", Integer.toString(this.afflictions));
        element.addAttribute("unknown2A", Integer.toString(this.unknown2A));
        element.addAttribute("itemRefuse", Integer.toString(this.itemRefuse));
        element.addAttribute("skillRefuse", Integer.toString(this.skillRefuse));
        element.addAttribute("attribRefuse", Integer
            .toString(this.attribRefuse));
        element.addAttribute("tradeRefuse", Integer.toString(this.tradeRefuse));
        element.addAttribute("unknown2F", Integer.toString(this.unknown2F));
        element.addAttribute("joinString", Integer.toString(this.joinString));
        element.addAttribute("willingness", Integer.toString(this.willingness));
        element.addAttribute("rank", StringUtils.escape(this.rank, "ASCII"));

        subElement = XMLUtils.createElement("skills");
        subElement.setText(toByteString(this.skills));
        element.add(subElement);

        subElement = XMLUtils.createElement("items");
        subElement.setText(toByteString(this.items));
        element.add(subElement);

        return element;
    }


    /**
     * @see de.ailis.wlandsuite.rawgame.parts.Part#write(java.io.OutputStream, int)
     */

    public void write(OutputStream stream, int offset) throws IOException
    {
        BitOutputStreamWrapper bitStream;

        bitStream = new BitOutputStreamWrapper(stream);
        bitStream.write(getBytes(this.name, 14));
        bitStream.write(this.strength);
        bitStream.write(this.iq);
        bitStream.write(this.luck);
        bitStream.write(this.speed);
        bitStream.write(this.agility);
        bitStream.write(this.dexterity);
        bitStream.write(this.charisma);
        bitStream.writeInt3(this.money);
        bitStream.write(this.gender);
        bitStream.write(this.nationality);
        bitStream.write(this.ac);
        bitStream.writeWord(this.maxCon);
        bitStream.writeWord(this.con);
        bitStream.write(this.weapon);
        bitStream.write(this.skillPoints);
        bitStream.writeInt3(this.experience);
        bitStream.write(this.level);
        bitStream.write(this.armor);
        bitStream.writeWord(this.lastCon);
        bitStream.write(this.afflictions);
        bitStream.write(this.npc ? 1 : 0);
        bitStream.write(this.unknown2A);
        bitStream.write(this.itemRefuse);
        bitStream.write(this.skillRefuse);
        bitStream.write(this.attribRefuse);
        bitStream.write(this.tradeRefuse);
        bitStream.write(this.unknown2F);
        bitStream.write(this.joinString);
        bitStream.write(this.willingness);
        bitStream.write(getBytes(this.rank, 25));

        for (int i = 0; i < 53; i++)
        {
            bitStream.write(0);
        }

        bitStream.write(this.skills);

        bitStream.write(0);

        bitStream.write(this.items);

        for (int i = 0; i < 7; i++)
        {
            bitStream.write(0);
        }


        bitStream.flush();
    }


    /**
     * Converts a string to null-terminated bytes array with a specific length.
     * 
     * @param string
     *            The string
     * @param length
     *            The length of the byte array
     * @return The byte array
     * @throws IOException
     */

    private byte[] getBytes(String string, int length) throws IOException
    {
        ByteArrayOutputStream stream;

        stream = new ByteArrayOutputStream();
        stream.write(string.getBytes("ASCII"));
        if (stream.size() > length)
        {
            throw new GameException("String is longer than " + length
                + " bytes: " + string);
        }
        for (int i = stream.size(); i < length; i++)
        {
            stream.write(0);
        }
        return stream.toByteArray();
    }


    /**
     * Creates a bytes list block.
     * 
     * @param bytes
     *            The bytes
     * @return The bytes list block
     */

    private String toByteString(byte[] bytes)
    {
        StringWriter text = new StringWriter();
        PrintWriter writer = new PrintWriter(text);
        int size = bytes.length;

        if (size > 9)
        {
            writer.println();
            writer.print("        ");
        }
        for (int i = 0; i < size; i++)
        {
            if (i > 0)
            {
                if (i % 16 == 0)
                {
                    writer.println();
                }
                if ((i < size) && (size > 9) && (i % 4 == 0))
                {
                    writer.print("        ");
                }
                else
                {
                    writer.print(" ");
                }
            }
            writer.format("%02x", new Object[] { bytes[i] });
        }
        if (size > 9)
        {
            writer.println();
            writer.print("      ");
        }

        return text.toString();
    }
}
