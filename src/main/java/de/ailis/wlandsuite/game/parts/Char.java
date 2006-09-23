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
import java.io.UnsupportedEncodingException;

import de.ailis.wlandsuite.utils.XmlUtils;
import org.dom4j.Element;

import de.ailis.wlandsuite.common.exceptions.GameException;
import de.ailis.wlandsuite.io.SeekableInputStream;
import de.ailis.wlandsuite.io.SeekableOutputStream;
import de.ailis.wlandsuite.utils.StringUtils;


/**
 * Character data
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Char
{
    /** The nationalities */
    private static final String[] nationalities = { "US", "Russian", "Mexican",
        "Indian", "Chinese" };

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
    private Skills skills;

    /** The item list */
    private Items items;


    /**
     * Private Constructor
     */

    private Char()
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

        // Skip unknown bytes which are always 0
        stream.skip(53);

        // Read the skills
        character.skills = Skills.read(stream);

        stream.skip(1);

        // Read the items
        character.items = Items.read(stream);

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

        // Create new character;
        character = new Char();
        character.name = StringUtils.unescape(element.attributeValue("name"),
            "ASCII");
        character.strength = StringUtils.toInt(element
            .attributeValue("strength"));
        character.iq = StringUtils.toInt(element.attributeValue("iq"));
        character.luck = StringUtils.toInt(element.attributeValue("luck"));
        character.speed = StringUtils.toInt(element.attributeValue("speed"));
        character.agility = StringUtils.toInt(element.attributeValue("agility"));
        character.dexterity = StringUtils.toInt(element
            .attributeValue("dexterity"));
        character.charisma = StringUtils.toInt(element
            .attributeValue("charisma"));
        character.money = StringUtils.toInt(element.attributeValue("money", "0"));
        character.gender = element.attributeValue("gender", "male").equals(
            "male") ? 0 : 1;
        character.nationality = getNationality(element
            .attributeValue("nationality", "US"));
        character.ac = StringUtils.toInt(element.attributeValue("ac", "0"));
        character.maxCon = StringUtils.toInt(element.attributeValue("maxCon"));
        character.con = StringUtils.toInt(element.attributeValue("con"));
        character.weapon = StringUtils.toInt(element.attributeValue("weapon", "0"));
        character.skillPoints = StringUtils.toInt(element
            .attributeValue("skillPoints", "0"));
        character.experience = StringUtils.toInt(element
            .attributeValue("experience", "0"));
        character.level = StringUtils.toInt(element.attributeValue("level", "1"));
        character.armor = StringUtils.toInt(element.attributeValue("armor", "0"));
        character.lastCon = StringUtils.toInt(element.attributeValue("lastCon", "0"));
        character.afflictions = StringUtils.toInt(element
            .attributeValue("afflictions", "0"));
        character.npc = Boolean.parseBoolean(element.attributeValue("npc", "false"));
        character.unknown2A = StringUtils.toInt(element
            .attributeValue("unknown2A", "0"));
        character.itemRefuse = StringUtils.toInt(element
            .attributeValue("itemRefuse", "0"));
        character.skillRefuse = StringUtils.toInt(element
            .attributeValue("skillRefuse", "0"));
        character.attribRefuse = StringUtils.toInt(element
            .attributeValue("attribRefuse", "0"));
        character.tradeRefuse = StringUtils.toInt(element
            .attributeValue("tradeRefuse", "0"));
        character.unknown2F = StringUtils.toInt(element
            .attributeValue("unknown2F", "0"));
        character.joinString = StringUtils.toInt(element
            .attributeValue("joinString", "0"));
        character.willingness = StringUtils.toInt(element
            .attributeValue("willingness", "0"));
        character.rank = StringUtils.unescape(element.attributeValue("rank", "Private"),
            "ASCII");

        // Read skills
        character.skills = Skills.read(element.element("skills"));

        // Read items
        character.items = Items.read(element.element("items"));

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
        Element element;

        element = XmlUtils.createElement("character");
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
        element.addAttribute("gender", this.gender == 0 ? "male" : "female");
        element.addAttribute("nationality", getNationality(this.nationality));
        element.addAttribute("ac", Integer.toString(this.ac));
        element.addAttribute("maxCon", Integer.toString(this.maxCon));
        element.addAttribute("con", Integer.toString(this.con));
        if (this.weapon != 0)
        {
            element.addAttribute("weapon", Integer.toString(this.weapon));
        }
        if (this.skillPoints != 0)
        {
            element.addAttribute("skillPoints", Integer.toString(this.skillPoints));
        }
        if (this.experience != 0)
        {
            element.addAttribute("experience", Integer.toString(this.experience));
        }
        if (this.level != 1)
        {
            element.addAttribute("level", Integer.toString(this.level));
        }
        if (this.armor != 0)
        {
            element.addAttribute("armor", Integer.toString(this.armor));
        }
        if (this.lastCon != 0)
        {
            element.addAttribute("lastCon", Integer.toString(this.lastCon));
        }
        if (this.afflictions != 0)
        {
            element.addAttribute("afflictions", Integer.toString(this.afflictions));
        }
        if (this.npc)
        {
            element.addAttribute("npc", "true");
        }
        if (this.unknown2A != 0)
        {
            element.addAttribute("unknown2A", StringUtils.toHex(this.unknown2A));
        }
        if (this.itemRefuse != 0)
        {
            element.addAttribute("itemRefuse", Integer.toString(this.itemRefuse));
        }
        if (this.skillRefuse != 0)
        {
            element.addAttribute("skillRefuse", Integer.toString(this.skillRefuse));
        }
        if (this.attribRefuse != 0)
        {
            element.addAttribute("attribRefuse", Integer
                .toString(this.attribRefuse));
        }
        if (this.tradeRefuse != 0)
        {
            element.addAttribute("tradeRefuse", Integer.toString(this.tradeRefuse));
        }
        if (this.unknown2F != 0)
        {
            element.addAttribute("unknown2F", StringUtils.toHex(this.unknown2F));
        }
        if (this.joinString != 0)
        {
            element.addAttribute("joinString", Integer.toString(this.joinString));
        }
        if (this.willingness != 0)
        {
            element.addAttribute("willingness", Integer.toString(this.willingness));
        }
        if (!this.rank.equals("Private"))
        {
            element.addAttribute("rank", StringUtils.escape(this.rank, "ASCII"));
        }

        // Add the skills
        element.add(this.skills.toXml());

        // Add the items
        element.add(this.items.toXml());

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

        // Write the skills
        this.skills.write(stream);

        stream.write(0);

        // Write the items
        this.items.write(stream);

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

    public Items getItems()
    {
        return this.items;
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

    public Skills getSkills()
    {
        return this.skills;
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


    /**
     * Returns the text represenation of a numeric nationality.
     * 
     * @param nationality
     *            The numeric nationality
     * @return The text nationality
     */

    public static String getNationality(int nationality)
    {
        return nationalities[nationality];
    }


    /**
     * Returns the numeric represenation of a textual nationality.
     * 
     * @param nationality
     *            The textual nationality
     * @return The numeric nationality
     */

    public static int getNationality(String nationality)
    {
        for (int i = 0; i < nationalities.length; i++)
        {
            if (nationalities[i].equals(nationality))
            {
                return i;
            }
        }
        throw new GameException("Unknown nationality: " + nationality);
    }
}
