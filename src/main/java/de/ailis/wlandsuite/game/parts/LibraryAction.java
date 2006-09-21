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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.ailis.wlandsuite.utils.StringUtils;
import de.ailis.wlandsuite.utils.XmlUtils;
import org.dom4j.Element;

import de.ailis.wlandsuite.io.SeekableOutputStream;


/**
 * The library data used in the Special Building Actions.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class LibraryAction implements Action
{
    /** The new action class to set when leaving the library */
    private int newActionClass;

    /** The new action to set when leaving the library */
    private int newAction;

    /** The welcome string */
    private int message;

    /** The library name */
    private String name;

    /** The skills that can be learned at this library */
    private int[] skills;


    /**
     * Creates and returns a new Library object by reading its data from the
     * specified stream.
     * 
     * @param stream
     *            The input stream
     * @return The LIbrary
     * @throws IOException
     */

    public static LibraryAction read(InputStream stream) throws IOException
    {
        LibraryAction library;
        byte[] bytes;
        int p, b;
        List<Integer> skills;

        library = new LibraryAction();

        library.newActionClass = stream.read();
        library.newAction = stream.read();
        library.message = stream.read();

        // Read the name
        bytes = new byte[13];
        stream.read(bytes);
        p = 0;
        while (bytes[p] != 0 && p < 13)
            p++;
        library.name = new String(bytes, 0, p, "ASCII");

        // Read the item types
        b = stream.read();
        skills = new ArrayList<Integer>();
        while (b != 255)
        {
            skills.add(b);
            b = stream.read();
        }
        int max = skills.size();
        library.skills = new int[max];
        for (int i = 0; i < max; i++)
        {
            library.skills[i] = skills.get(i);
        }

        return library;
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.Action#write(de.ailis.wlandsuite.io.SeekableOutputStream,
     *      de.ailis.wlandsuite.game.parts.SpecialActionTable)
     */

    public void write(SeekableOutputStream stream,
        SpecialActionTable specialActionTable) throws IOException
    {
        stream.write(0x82);

        stream.write(this.newActionClass);
        stream.write(this.newAction);
        stream.write(this.message);

        stream.write(this.name.getBytes("ASCII"), 0, Math.min(this.name
            .length(), 13));
        for (int i = this.name.length(); i < 13; i++)
        {
            stream.write(0);
        }

        for (int itemType: this.skills)
        {
            stream.write(itemType);
        }
        stream.write(255);
    }


    /**
     * Returns the library data as XML.
     * 
     * @param id
     *            The action id
     * @return The library data as XML
     */

    public Element toXml(int id)
    {
        Element element;

        element = XmlUtils.createElement("library");

        element.addAttribute("id", StringUtils.toHex(id));
        if (this.name != null && this.name.length() > 0)
        {
            element.addAttribute("name", this.name);
        }
        if (this.message != 0)
        {
            element.addAttribute("message", Integer.toString(this.message));
        }
        if (this.newActionClass != 255)
        {
            element.addAttribute("newActionClass", StringUtils.toHex
                (this.newActionClass));
        }
        if (this.newAction != 255)
        {
            element.addAttribute("newAction", StringUtils.toHex(this.newAction));
        }

        for (int skill: this.skills)
        {
            Element subElement;

            subElement = XmlUtils.createElement("skill");
            subElement.addText(Integer.toString(skill));

            element.add(subElement);
        }
        return element;
    }


    /**
     * Creates and returns a new Library object by reading its data from XML.
     * 
     * @param element
     *            The XML element
     * @return The library data
     */

    public static LibraryAction read(Element element)
    {
        LibraryAction library;

        library = new LibraryAction();
        library.name = element.attributeValue("name", "");
        library.message = StringUtils.toInt(element.attributeValue("message",
            "0"));
        library.newActionClass = StringUtils.toInt(element.attributeValue(
            "newActionClass", "255"));
        library.newAction = StringUtils.toInt(element.attributeValue(
            "newAction", "255"));

        List<?> elements = element.elements("skill");
        library.skills = new int[elements.size()];
        int i = 0;
        for (Object item: elements)
        {
            Element subElement = (Element) item;
            library.skills[i] = StringUtils.toInt(subElement.getText());
            i++;
        }

        return library;
    }


    /**
     * Returns the message.
     * 
     * @return The message
     */

    public int getMessage()
    {
        return this.message;
    }


    /**
     * Sets the message.
     * 
     * @param message
     *            The message to set
     */

    public void setMessage(int message)
    {
        this.message = message;
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
     * Returns the newAction.
     * 
     * @return The newAction
     */

    public int getNewAction()
    {
        return this.newAction;
    }


    /**
     * Sets the newAction.
     * 
     * @param newAction
     *            The newAction to set
     */

    public void setNewAction(int newAction)
    {
        this.newAction = newAction;
    }


    /**
     * Returns the newActionClass.
     * 
     * @return The newActionClass
     */

    public int getNewActionClass()
    {
        return this.newActionClass;
    }


    /**
     * Sets the newActionClass.
     * 
     * @param newActionClass
     *            The newActionClass to set
     */

    public void setNewActionClass(int newActionClass)
    {
        this.newActionClass = newActionClass;
    }


    /**
     * Returns the skills.
     * 
     * @return The skills
     */

    public int[] getSkills()
    {
        return this.skills;
    }


    /**
     * Sets the skills.
     * 
     * @param skills
     *            The skills to set
     */

    public void setSkills(int[] skills)
    {
        this.skills = skills;
    }
}
