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
import java.util.ArrayList;

import org.dom4j.Element;

import de.ailis.wlandsuite.common.exceptions.GameException;
import de.ailis.wlandsuite.io.SeekableInputStream;
import de.ailis.wlandsuite.io.SeekableOutputStream;
import de.ailis.wlandsuite.utils.XmlUtils;


/**
 * The skills of a character.
 *
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Skills extends ArrayList<Skill>
{
    /** Serial version UID */
    private static final long serialVersionUID = 1111498078578501152L;


    /**
     * Constructor
     */

    public Skills()
    {
        super();
    }


    /**
     * Constructor
     *
     * @param capacity
     *            The initial capacity
     */

    public Skills(final int capacity)
    {
        super(capacity);
    }


    /**
     * Creates and returns a new Skills object by reading all the skill from the
     * specified stream. The stream must be positioned at the beginning of the
     * skill list.
     *
     * @param stream
     *            The stream to read the skills from
     * @return The skills
     * @throws IOException
     *             When file operation fails.
     */

    public static Skills read(final SeekableInputStream stream) throws IOException
    {
        Skills skills;
        boolean special;

        skills = new Skills(30);

        special = false;
        for (int i = 0; i < 30; i++)
        {
            Skill skill;

            skill = Skill.read(stream);
            if (skill.getId() == 0)
            {
                special = true;
            }
            else
            {
                skill.setSpecial(special);
                skills.add(skill);
            }
        }

        // Return the skills
        return skills;
    }


    /**
     * Creates and returns a new Skills object from XML.
     *
     * @param element
     *            The XML element
     * @return The skills
     */

    public static Skills read(final Element element)
    {
        Skills skills;

        skills = new Skills(30);
        for (final Object item: element.elements("skill"))
        {
            final Element subElement = (Element) item;

            skills.add(Skill.read(subElement));
        }
        return skills;
    }


    /**
     * Writes the skills to the specified output stream.
     *
     * @param stream
     *            The output stream
     * @throws IOException
     *             When file operation fails.
     */

    public void write(final SeekableOutputStream stream) throws IOException
    {
        int specials, i;

        if (size() > 30)
        {
            throw new GameException("Character has to many skills: " + size());
        }

        // Count the special skills
        specials = 0;
        for (final Skill skill: this)
        {
            if (skill.isSpecial())
            {
                specials++;
            }
        }

        // Write the normal skills
        i = 0;
        for (final Skill skill: this)
        {
            if (!skill.isSpecial())
            {
                skill.write(stream);
                i++;
            }
        }

        // Write the unused skills
        while (i < 30 - specials)
        {
            stream.write(0);
            stream.write(0);
            i++;
        }

        // Write the special skills
        for (final Skill skill: this)
        {
            if (skill.isSpecial())
            {
                skill.write(stream);
            }
        }
    }


    /**
     * Returns the monsters as XML.
     *
     * @return The monsters as XML
     */

    public Element toXml()
    {
        Element element;

        // Create the root XML element
        element = XmlUtils.createElement("skills");

        // Add all the skills
        for (final Skill skill: this)
        {
            element.add(skill.toXml());
        }

        // Return the XML element
        return element;
    }
}
