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

import java.io.IOException;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.ailis.wlandsuite.io.SeekableInputStream;
import de.ailis.wlandsuite.io.SeekableOutputStream;
import de.ailis.wlandsuite.utils.StringUtils;


/**
 * The data of a monster which the player can fight in random or fixed
 * encounter.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Monster
{
    /** Monster type "animal" */
    public static final int MONSTER_TYPE_ANIMAL = 1;

    /** Monster type "mutant" */
    public static final int MONSTER_TYPE_MUTANT = 2;

    /** Monster type "humanoid" */
    public static final int MONSTER_TYPE_HUMANOID = 3;

    /** Monster type "cyborg" */
    public static final int MONSTER_TYPE_CYBORGL = 4;

    /** Monster type "robot" */
    public static final int MONSTER_TYPE_ROBOT = 5;

    /** The monster name (Plural and singular) */
    private String name;

    /**
     * The experience points the killer get. Also used for calculating hit
     * points
     */
    private int experience;

    /** How good the monster is skilled. The higher the value the better it is */
    private int skill;

    /** The random amount of additional damage in D6 */
    private int randomDamage;

    /** The maximum group size (0-15) */
    private int maxGroupSize;

    /** Multiplier for experience points. Also affects the AC of the monster */
    private int ac;

    /** The fixed amount of damage (0-15) */
    private int fixedDamage;

    /** The weapon type (0-15) */
    private int weaponType;

    /** The monster type (TYPE_* constant) */
    private int monsterType;

    /** The picture to use for this monster */
    private int picture;


    /**
     * Creates and returns a new monster by reading it's data from the specified
     * stream. The monster name must be specified because it's read from a
     * different part of the game file. The stream must be positioned at the
     * beginning of the monster data block.
     * 
     * @param stream
     *            The input stream
     * @param monsterName
     *            The monster name
     * @return The monster
     * @throws IOException
     *             If the monster data could not be read from the stream
     */

    public static Monster read(SeekableInputStream stream, String monsterName)
        throws IOException
    {
        Monster monster;
        int b;

        monster = new Monster();
        monster.name = monsterName;

        monster.experience = stream.readWord();
        monster.skill = stream.readByte();
        monster.randomDamage = stream.readByte();

        b = stream.readByte();
        monster.maxGroupSize = b >> 4;
        monster.ac = b & 15;

        b = stream.readByte();
        monster.fixedDamage = b >> 4;
        monster.weaponType = b & 15;

        monster.monsterType = stream.readByte();
        monster.picture = stream.readByte();

        return monster;
    }


    /**
     * Creates and returns a new monster by reading it's data from the specified
     * XML element.
     * 
     * @param element
     *            The XML element
     * @return The monster
     */

    public static Monster read(Element element)
    {
        Monster monster;

        monster = new Monster();

        monster.name = StringUtils.unescape(element.attributeValue("name"),
            "ASCII");
        monster.experience = Integer.parseInt(element
            .attributeValue("experience"));
        monster.ac = Integer.parseInt(element.attributeValue("ac"));
        monster.skill = Integer.parseInt(element.attributeValue("skill"));
        monster.fixedDamage = Integer.parseInt(element
            .attributeValue("fixedDamage"));
        monster.randomDamage = Integer.parseInt(element
            .attributeValue("randomDamage"));
        monster.maxGroupSize = Integer.parseInt(element
            .attributeValue("maxGroupSize"));
        monster.weaponType = Integer.parseInt(element
            .attributeValue("weaponType"));
        monster.monsterType = Integer.parseInt(element
            .attributeValue("monsterType"));
        monster.picture = Integer.parseInt(element.attributeValue("picture"));

        return monster;
    }


    /**
     * Writes the monster data (not the name, this is done elsewhere) to the
     * specified output stream
     * 
     * @param stream
     *            The stream to write the monster data to
     */

    public void write(SeekableOutputStream stream)
    {
        stream.writeWord(this.experience);
        stream.writeByte(this.skill);
        stream.writeByte(this.randomDamage);
        stream.writeByte(this.maxGroupSize << 4 | this.ac);
        stream.writeByte(this.fixedDamage << 4 | this.weaponType);
        stream.writeByte(this.monsterType);
        stream.writeByte(this.picture);
    }


    /**
     * Returns the monster data as XML.
     * 
     * @param id
     *            The monster id
     * @return The monster data as XML
     */

    public Element toXml(int id)
    {
        Element element;

        element = DocumentHelper.createElement("monster");
        element.addAttribute("id", Integer.toString(id));
        element.addAttribute("name", StringUtils.escape(this.name, "ASCII"));
        element.addAttribute("experience", Integer.toString(this.experience));
        element.addAttribute("ac", Integer.toString(this.ac));
        element.addAttribute("skill", Integer.toString(this.skill));
        element.addAttribute("fixedDamage", Integer.toString(this.fixedDamage));
        element.addAttribute("randomDamage", Integer
            .toString(this.randomDamage));
        element.addAttribute("maxGroupSize", Integer
            .toString(this.maxGroupSize));
        element.addAttribute("weaponType", Integer.toString(this.weaponType));
        element.addAttribute("monsterType", Integer.toString(this.monsterType));
        element.addAttribute("picture", Integer.toString(this.picture));

        return element;
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
     * Returns the fixedDamage.
     * 
     * @return The fixedDamage
     */

    public int getFixedDamage()
    {
        return this.fixedDamage;
    }


    /**
     * Sets the fixedDamage.
     * 
     * @param fixedDamage
     *            The fixedDamage to set
     */

    public void setFixedDamage(int fixedDamage)
    {
        this.fixedDamage = fixedDamage;
    }


    /**
     * Returns the maxGroupSize.
     * 
     * @return The maxGroupSize
     */

    public int getMaxGroupSize()
    {
        return this.maxGroupSize;
    }


    /**
     * Sets the maxGroupSize.
     * 
     * @param maxGroupSize
     *            The maxGroupSize to set
     */

    public void setMaxGroupSize(int maxGroupSize)
    {
        this.maxGroupSize = maxGroupSize;
    }


    /**
     * Returns the monsterType.
     * 
     * @return The monsterType
     */

    public int getMonsterType()
    {
        return this.monsterType;
    }


    /**
     * Sets the monsterType.
     * 
     * @param monsterType
     *            The monsterType to set
     */

    public void setMonsterType(int monsterType)
    {
        this.monsterType = monsterType;
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
     * Returns the picture.
     * 
     * @return The picture
     */

    public int getPicture()
    {
        return this.picture;
    }


    /**
     * Sets the picture.
     * 
     * @param picture
     *            The picture to set
     */

    public void setPicture(int picture)
    {
        this.picture = picture;
    }


    /**
     * Returns the randomDamage.
     * 
     * @return The randomDamage
     */

    public int getRandomDamage()
    {
        return this.randomDamage;
    }


    /**
     * Sets the randomDamage.
     * 
     * @param randomDamage
     *            The randomDamage to set
     */

    public void setRandomDamage(int randomDamage)
    {
        this.randomDamage = randomDamage;
    }


    /**
     * Returns the skill.
     * 
     * @return The skill
     */

    public int getSkill()
    {
        return this.skill;
    }


    /**
     * Sets the skill.
     * 
     * @param skill
     *            The skill to set
     */

    public void setSkill(int skill)
    {
        this.skill = skill;
    }


    /**
     * Returns the weaponType.
     * 
     * @return The weaponType
     */

    public int getWeaponType()
    {
        return this.weaponType;
    }


    /**
     * Sets the weaponType.
     * 
     * @param weaponType
     *            The weaponType to set
     */

    public void setWeaponType(int weaponType)
    {
        this.weaponType = weaponType;
    }
}
