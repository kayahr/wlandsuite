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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.ailis.wlandsuite.io.SeekableInputStream;
import de.ailis.wlandsuite.io.SeekableOutputStream;
import de.ailis.wlandsuite.rawgame.GameException;
import de.ailis.wlandsuite.utils.StringUtils;


/**
 * Character data
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Char
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
     */

    public Char()
    {
        super();
    }


    /**
     * Creates and returns a new Char object by reading the data from the
     * specified input stream.
     * 
     * @param stream
     *            The stream to read the character data from
     * @return The newly created character
     * @throws IOException
     */

    public static Char read(SeekableInputStream stream) throws IOException
    {
        byte[] tmp;
        Char character;

        // Create new character
        character = new Char();

        tmp = new byte[14];
        stream.read(tmp);
        character.name = getString(tmp);
        character.strength = stream.read();
        character.iq = stream.read();
        character.luck = stream.read();
        character.speed = stream.read();
        character.agility = stream.read();
        character.dexterity = stream.read();
        character.charisma = stream.read();
        character.money = stream.readInt3();
        character.gender = stream.read();
        character.nationality = stream.read();
        character.ac = stream.read();
        character.maxCon = stream.readWord();
        character.con = stream.readWord();
        character.weapon = stream.read();
        character.skillPoints = stream.read();
        character.experience = stream.readInt3();
        character.level = stream.read();
        character.armor = stream.read();
        character.lastCon = stream.readWord();
        character.afflictions = stream.read();
        character.npc = stream.read() == 1;
        character.unknown2A = stream.read();
        character.itemRefuse = stream.read();
        character.skillRefuse = stream.read();
        character.attribRefuse = stream.read();
        character.tradeRefuse = stream.read();
        character.unknown2F = stream.read();
        character.joinString = stream.read();
        character.willingness = stream.read();

        tmp = new byte[25];
        stream.read(tmp);
        character.rank = getString(tmp);

        stream.skip(53);

        character.skills = new byte[60];
        stream.read(character.skills);

        stream.skip(1);

        character.items = new byte[60];
        stream.read(character.items);

        stream.skip(7);

        // Return the newly creates character
        return character;
    }


    /**
     * Converts a null-terminated string into a normal string.
     * 
     * @param data
     *            The null-terminated data
     * @return The string
     */

    private static String getString(byte[] data)
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
     * Creates and returns a new character read from the specified XML element.
     * 
     * @param element
     *            The XML element to read the charactger from
     * @return The character
     */

    public static Char read(Element element)
    {
        Char character;
        ByteArrayOutputStream stream;

        // Create new character;
        character = new Char();
        character.name = StringUtils.unescape(element.attributeValue("name"),
            "ASCII");
        character.strength = Integer.parseInt(element
            .attributeValue("strength"));
        character.iq = Integer.parseInt(element.attributeValue("iq"));
        character.luck = Integer.parseInt(element.attributeValue("luck"));
        character.speed = Integer.parseInt(element.attributeValue("speed"));
        character.agility = Integer.parseInt(element.attributeValue("agility"));
        character.dexterity = Integer.parseInt(element
            .attributeValue("dexterity"));
        character.charisma = Integer.parseInt(element
            .attributeValue("charisma"));
        character.money = Integer.parseInt(element.attributeValue("money"));
        character.gender = Integer.parseInt(element.attributeValue("gender"));
        character.nationality = Integer.parseInt(element
            .attributeValue("nationality"));
        character.ac = Integer.parseInt(element.attributeValue("ac"));
        character.maxCon = Integer.parseInt(element.attributeValue("maxCon"));
        character.con = Integer.parseInt(element.attributeValue("con"));
        character.weapon = Integer.parseInt(element.attributeValue("weapon"));
        character.skillPoints = Integer.parseInt(element
            .attributeValue("skillPoints"));
        character.experience = Integer.parseInt(element
            .attributeValue("experience"));
        character.level = Integer.parseInt(element.attributeValue("level"));
        character.armor = Integer.parseInt(element.attributeValue("armor"));
        character.lastCon = Integer.parseInt(element.attributeValue("lastCon"));
        character.afflictions = Integer.parseInt(element
            .attributeValue("afflictions"));
        character.npc = Boolean.parseBoolean(element.attributeValue("npc"));
        character.unknown2A = Integer.parseInt(element
            .attributeValue("unknown2A"));
        character.itemRefuse = Integer.parseInt(element
            .attributeValue("itemRefuse"));
        character.skillRefuse = Integer.parseInt(element
            .attributeValue("skillRefuse"));
        character.attribRefuse = Integer.parseInt(element
            .attributeValue("attribRefuse"));
        character.tradeRefuse = Integer.parseInt(element
            .attributeValue("tradeRefuse"));
        character.unknown2F = Integer.parseInt(element
            .attributeValue("unknown2F"));
        character.joinString = Integer.parseInt(element
            .attributeValue("joinString"));
        character.willingness = Integer.parseInt(element
            .attributeValue("willingness"));
        character.rank = StringUtils.unescape(element.attributeValue("rank"),
            "ASCII");

        // Read skills
        stream = new ByteArrayOutputStream();
        for (String c: element.element("skills").getTextTrim().split("\\s"))
        {
            int b = Integer.valueOf(c, 16);
            stream.write(b);
        }
        character.skills = stream.toByteArray();
        if (character.skills.length != 60)
        {
            throw new GameException("Skill list of character " + character.name
                + " to large");
        }

        // Read items
        stream = new ByteArrayOutputStream();
        for (String c: element.element("items").getTextTrim().split("\\s"))
        {
            int b = Integer.valueOf(c, 16);
            stream.write(b);
        }
        character.items = stream.toByteArray();
        if (character.items.length != 60)
        {
            throw new GameException("Item list of character " + character.name
                + " to large");
        }

        // Return the newly created character
        return character;
    }


    /**
     * Returns the character as XML.
     * 
     * @param id
     *            The character id
     * @return The character as XML
     */

    public Element toXml(int id)
    {
        Element element, subElement;

        element = DocumentHelper.createElement("character");
        element.addAttribute("id", Integer.toString(id));
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

        subElement = DocumentHelper.createElement("skills");
        subElement.setText(toByteString(this.skills));
        element.add(subElement);

        subElement = DocumentHelper.createElement("items");
        subElement.setText(toByteString(this.items));
        element.add(subElement);

        return element;
    }


    /**
     * Writes the character to the specified stream.
     * 
     * @param stream
     *            The stream to write the character to
     * @throws IOException
     */

    public void write(SeekableOutputStream stream) throws IOException
    {
        stream.write(getBytes(this.name, 14));
        stream.write(this.strength);
        stream.write(this.iq);
        stream.write(this.luck);
        stream.write(this.speed);
        stream.write(this.agility);
        stream.write(this.dexterity);
        stream.write(this.charisma);
        stream.writeInt3(this.money);
        stream.write(this.gender);
        stream.write(this.nationality);
        stream.write(this.ac);
        stream.writeWord(this.maxCon);
        stream.writeWord(this.con);
        stream.write(this.weapon);
        stream.write(this.skillPoints);
        stream.writeInt3(this.experience);
        stream.write(this.level);
        stream.write(this.armor);
        stream.writeWord(this.lastCon);
        stream.write(this.afflictions);
        stream.write(this.npc ? 1 : 0);
        stream.write(this.unknown2A);
        stream.write(this.itemRefuse);
        stream.write(this.skillRefuse);
        stream.write(this.attribRefuse);
        stream.write(this.tradeRefuse);
        stream.write(this.unknown2F);
        stream.write(this.joinString);
        stream.write(this.willingness);
        stream.write(getBytes(this.rank, 25));

        for (int i = 0; i < 53; i++)
        {
            stream.write(0);
        }

        stream.write(this.skills);

        stream.write(0);

        stream.write(this.items);

        for (int i = 0; i < 7; i++)
        {
            stream.write(0);
        }
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


    /**
     * Returns the ac.
     * 
     * @return The ac
     */

    public int getAc()
    {
        return this.ac;
    }


    /**
     * Sets the ac.
     * 
     * @param ac
     *            The ac to set
     */

    public void setAc(int ac)
    {
        this.ac = ac;
    }


    /**
     * Returns the afflictions.
     * 
     * @return The afflictions
     */

    public int getAfflictions()
    {
        return this.afflictions;
    }


    /**
     * Sets the afflictions.
     * 
     * @param afflictions
     *            The afflictions to set
     */

    public void setAfflictions(int afflictions)
    {
        this.afflictions = afflictions;
    }


    /**
     * Returns the agility.
     * 
     * @return The agility
     */

    public int getAgility()
    {
        return this.agility;
    }


    /**
     * Sets the agility.
     * 
     * @param agility
     *            The agility to set
     */

    public void setAgility(int agility)
    {
        this.agility = agility;
    }


    /**
     * Returns the armor.
     * 
     * @return The armor
     */

    public int getArmor()
    {
        return this.armor;
    }


    /**
     * Sets the armor.
     * 
     * @param armor
     *            The armor to set
     */

    public void setArmor(int armor)
    {
        this.armor = armor;
    }


    /**
     * Returns the attribRefuse.
     * 
     * @return The attribRefuse
     */

    public int getAttribRefuse()
    {
        return this.attribRefuse;
    }


    /**
     * Sets the attribRefuse.
     * 
     * @param attribRefuse
     *            The attribRefuse to set
     */

    public void setAttribRefuse(int attribRefuse)
    {
        this.attribRefuse = attribRefuse;
    }


    /**
     * Returns the charisma.
     * 
     * @return The charisma
     */

    public int getCharisma()
    {
        return this.charisma;
    }


    /**
     * Sets the charisma.
     * 
     * @param charisma
     *            The charisma to set
     */

    public void setCharisma(int charisma)
    {
        this.charisma = charisma;
    }


    /**
     * Returns the con.
     * 
     * @return The con
     */

    public int getCon()
    {
        return this.con;
    }


    /**
     * Sets the con.
     * 
     * @param con
     *            The con to set
     */

    public void setCon(int con)
    {
        this.con = con;
    }


    /**
     * Returns the dexterity.
     * 
     * @return The dexterity
     */

    public int getDexterity()
    {
        return this.dexterity;
    }


    /**
     * Sets the dexterity.
     * 
     * @param dexterity
     *            The dexterity to set
     */

    public void setDexterity(int dexterity)
    {
        this.dexterity = dexterity;
    }


    /**
     * Returns the experience.
     * 
     * @return The experience
     */

    public int getExperience()
    {
        return this.experience;
    }


    /**
     * Sets the experience.
     * 
     * @param experience
     *            The experience to set
     */

    public void setExperience(int experience)
    {
        this.experience = experience;
    }


    /**
     * Returns the gender.
     * 
     * @return The gender
     */

    public int getGender()
    {
        return this.gender;
    }


    /**
     * Sets the gender.
     * 
     * @param gender
     *            The gender to set
     */

    public void setGender(int gender)
    {
        this.gender = gender;
    }


    /**
     * Returns the iq.
     * 
     * @return The iq
     */

    public int getIq()
    {
        return this.iq;
    }


    /**
     * Sets the iq.
     * 
     * @param iq
     *            The iq to set
     */

    public void setIq(int iq)
    {
        this.iq = iq;
    }


    /**
     * Returns the itemRefuse.
     * 
     * @return The itemRefuse
     */

    public int getItemRefuse()
    {
        return this.itemRefuse;
    }


    /**
     * Sets the itemRefuse.
     * 
     * @param itemRefuse
     *            The itemRefuse to set
     */

    public void setItemRefuse(int itemRefuse)
    {
        this.itemRefuse = itemRefuse;
    }


    /**
     * Returns the items.
     * 
     * @return The items
     */

    public byte[] getItems()
    {
        return this.items;
    }


    /**
     * Sets the items.
     * 
     * @param items
     *            The items to set
     */

    public void setItems(byte[] items)
    {
        this.items = items;
    }


    /**
     * Returns the joinString.
     * 
     * @return The joinString
     */

    public int getJoinString()
    {
        return this.joinString;
    }


    /**
     * Sets the joinString.
     * 
     * @param joinString
     *            The joinString to set
     */

    public void setJoinString(int joinString)
    {
        this.joinString = joinString;
    }


    /**
     * Returns the lastCon.
     * 
     * @return The lastCon
     */

    public int getLastCon()
    {
        return this.lastCon;
    }


    /**
     * Sets the lastCon.
     * 
     * @param lastCon
     *            The lastCon to set
     */

    public void setLastCon(int lastCon)
    {
        this.lastCon = lastCon;
    }


    /**
     * Returns the level.
     * 
     * @return The level
     */

    public int getLevel()
    {
        return this.level;
    }


    /**
     * Sets the level.
     * 
     * @param level
     *            The level to set
     */

    public void setLevel(int level)
    {
        this.level = level;
    }


    /**
     * Returns the luck.
     * 
     * @return The luck
     */

    public int getLuck()
    {
        return this.luck;
    }


    /**
     * Sets the luck.
     * 
     * @param luck
     *            The luck to set
     */

    public void setLuck(int luck)
    {
        this.luck = luck;
    }


    /**
     * Returns the maxCon.
     * 
     * @return The maxCon
     */

    public int getMaxCon()
    {
        return this.maxCon;
    }


    /**
     * Sets the maxCon.
     * 
     * @param maxCon
     *            The maxCon to set
     */

    public void setMaxCon(int maxCon)
    {
        this.maxCon = maxCon;
    }


    /**
     * Returns the money.
     * 
     * @return The money
     */

    public int getMoney()
    {
        return this.money;
    }


    /**
     * Sets the money.
     * 
     * @param money
     *            The money to set
     */

    public void setMoney(int money)
    {
        this.money = money;
    }


    /**
     * Returns the name.
     * 
     * @return The name
     */

    public String getName()
    {
        return this.name;
    }


    /**
     * Sets the name.
     * 
     * @param name
     *            The name to set
     */

    public void setName(String name)
    {
        this.name = name;
    }


    /**
     * Returns the nationality.
     * 
     * @return The nationality
     */

    public int getNationality()
    {
        return this.nationality;
    }


    /**
     * Sets the nationality.
     * 
     * @param nationality
     *            The nationality to set
     */

    public void setNationality(int nationality)
    {
        this.nationality = nationality;
    }


    /**
     * Returns the npc.
     * 
     * @return The npc
     */

    public boolean isNpc()
    {
        return this.npc;
    }


    /**
     * Sets the npc.
     * 
     * @param npc
     *            The npc to set
     */

    public void setNpc(boolean npc)
    {
        this.npc = npc;
    }


    /**
     * Returns the rank.
     * 
     * @return The rank
     */

    public String getRank()
    {
        return this.rank;
    }


    /**
     * Sets the rank.
     * 
     * @param rank
     *            The rank to set
     */

    public void setRank(String rank)
    {
        this.rank = rank;
    }


    /**
     * Returns the skillPoints.
     * 
     * @return The skillPoints
     */

    public int getSkillPoints()
    {
        return this.skillPoints;
    }


    /**
     * Sets the skillPoints.
     * 
     * @param skillPoints
     *            The skillPoints to set
     */

    public void setSkillPoints(int skillPoints)
    {
        this.skillPoints = skillPoints;
    }


    /**
     * Returns the skillRefuse.
     * 
     * @return The skillRefuse
     */

    public int getSkillRefuse()
    {
        return this.skillRefuse;
    }


    /**
     * Sets the skillRefuse.
     * 
     * @param skillRefuse
     *            The skillRefuse to set
     */

    public void setSkillRefuse(int skillRefuse)
    {
        this.skillRefuse = skillRefuse;
    }


    /**
     * Returns the skills.
     * 
     * @return The skills
     */

    public byte[] getSkills()
    {
        return this.skills;
    }


    /**
     * Sets the skills.
     * 
     * @param skills
     *            The skills to set
     */

    public void setSkills(byte[] skills)
    {
        this.skills = skills;
    }


    /**
     * Returns the speed.
     * 
     * @return The speed
     */

    public int getSpeed()
    {
        return this.speed;
    }


    /**
     * Sets the speed.
     * 
     * @param speed
     *            The speed to set
     */

    public void setSpeed(int speed)
    {
        this.speed = speed;
    }


    /**
     * Returns the strength.
     * 
     * @return The strength
     */

    public int getStrength()
    {
        return this.strength;
    }


    /**
     * Sets the strength.
     * 
     * @param strength
     *            The strength to set
     */

    public void setStrength(int strength)
    {
        this.strength = strength;
    }


    /**
     * Returns the tradeRefuse.
     * 
     * @return The tradeRefuse
     */

    public int getTradeRefuse()
    {
        return this.tradeRefuse;
    }


    /**
     * Sets the tradeRefuse.
     * 
     * @param tradeRefuse
     *            The tradeRefuse to set
     */

    public void setTradeRefuse(int tradeRefuse)
    {
        this.tradeRefuse = tradeRefuse;
    }


    /**
     * Returns the unknown2A.
     * 
     * @return The unknown2A
     */

    public int getUnknown2A()
    {
        return this.unknown2A;
    }


    /**
     * Sets the unknown2A.
     * 
     * @param unknown2A
     *            The unknown2A to set
     */

    public void setUnknown2A(int unknown2A)
    {
        this.unknown2A = unknown2A;
    }


    /**
     * Returns the unknown2F.
     * 
     * @return The unknown2F
     */

    public int getUnknown2F()
    {
        return this.unknown2F;
    }


    /**
     * Sets the unknown2F.
     * 
     * @param unknown2F
     *            The unknown2F to set
     */

    public void setUnknown2F(int unknown2F)
    {
        this.unknown2F = unknown2F;
    }


    /**
     * Returns the weapon.
     * 
     * @return The weapon
     */

    public int getWeapon()
    {
        return this.weapon;
    }


    /**
     * Sets the weapon.
     * 
     * @param weapon
     *            The weapon to set
     */

    public void setWeapon(int weapon)
    {
        this.weapon = weapon;
    }


    /**
     * Returns the willingness.
     * 
     * @return The willingness
     */

    public int getWillingness()
    {
        return this.willingness;
    }


    /**
     * Sets the willingness.
     * 
     * @param willingness
     *            The willingness to set
     */

    public void setWillingness(int willingness)
    {
        this.willingness = willingness;
    }
}
