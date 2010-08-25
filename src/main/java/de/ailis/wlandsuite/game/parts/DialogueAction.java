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
import java.util.List;

import org.dom4j.Element;

import de.ailis.wlandsuite.io.SeekableInputStream;
import de.ailis.wlandsuite.io.SeekableOutputStream;
import de.ailis.wlandsuite.utils.StringUtils;
import de.ailis.wlandsuite.utils.XmlUtils;


/**
 * The dialogue action is used to interact with the player. The player can
 * choose answers and each answer triggers a different action.
 *
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class DialogueAction implements Action
{
    /** If conversation is done by menus */
    private boolean menu;

    /** The starting message */
    private int message;

    /**
     * The new action class to set when the dialogue is canceled (255 means
     * setting no new action class)
     */
    private int cancelNewActionClass;

    /**
     * The new action to set when the dialogue is canceled (255 means setting no
     * new action)
     */
    private int cancelNewAction;

    /**
     * The new action class to set when an other answer is selected (255 means
     * setting no new action class)
     */
    private int otherNewActionClass;

    /**
     * The new action to set when an other answer is selected (255 means setting
     * no new action)
     */
    private int otherNewAction;

    /** The answers */
    private final List<Answer> answers = new ArrayList<Answer>();


    /**
     * Creates and returns a new Dialogue Action by reading its data from the
     * specified stream.
     *
     * @param stream
     *            The input stream
     * @return The new Dialogue Action
     * @throws IOException
     *             When file operation fails.
     */

    public static DialogueAction read(final SeekableInputStream stream)
        throws IOException
    {
        DialogueAction action;
        int b;

        action = new DialogueAction();

        b = stream.read();
        action.menu = (b & 128) == 128;
        action.message = b & 127;
        action.cancelNewActionClass = stream.read();
        action.cancelNewAction = stream.read();
        do
        {
            Answer answer;

            b = stream.read();
            answer = new Answer();
            answer.setMessage(b & 127);
            action.answers.add(answer);
        }
        while ((b & 128) == 0);

        for (final Answer answer: action.answers)
        {
            answer.setNewActionClass(stream.read());
            answer.setNewAction(stream.read());
        }

        action.otherNewActionClass = stream.read();
        if (action.otherNewActionClass < 253)
        {
            action.otherNewAction = stream.read();
        }
        else
        {
            action.otherNewAction = 255;
        }

        return action;
    }


    /**
     * Creates and returns a Dialogue Action by reading its data from XML.
     *
     * @param element
     *            The XML element
     * @return The Dialogue Action
     */

    public static DialogueAction read(final Element element)
    {
        DialogueAction action;

        action = new DialogueAction();

        action.menu = Boolean.parseBoolean(element.attributeValue("menu",
            "false"));
        action.message = StringUtils.toInt(element.attributeValue("message",
            "0"));
        action.cancelNewActionClass = StringUtils.toInt(element.attributeValue(
            "cancelNewActionClass", "255"));
        action.cancelNewAction = StringUtils.toInt(element.attributeValue(
            "cancelNewAction", "255"));
        action.otherNewActionClass = StringUtils.toInt(element.attributeValue(
            "otherNewActionClass", "255"));
        action.otherNewAction = StringUtils.toInt(element.attributeValue(
            "otherNewAction", "255"));

        // Read the checks
        for (final Object answer: element.elements())
        {
            Element subElement;

            subElement = (Element) answer;
            action.answers.add(Answer.read(subElement));
        }

        // Return the check action
        return action;
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.Action#toXml(int)
     */

    public Element toXml(final int id)
    {
        Element element;

        element = XmlUtils.createElement("dialogue");
        element.addAttribute("id", StringUtils.toHex(id));
        if (this.menu)
        {
            element.addAttribute("menu", "true");
        }
        if (this.message != 0)
        {
            element.addAttribute("message", Integer.toString(this.message));
        }
        if (this.cancelNewActionClass != 255)
        {
            element.addAttribute("cancelNewActionClass", StringUtils
                .toHex(this.cancelNewActionClass));
        }
        if (this.cancelNewAction != 255)
        {
            element.addAttribute("cancelNewAction", StringUtils
                .toHex(this.cancelNewAction));
        }
        if (this.otherNewActionClass != 255)
        {
            element.addAttribute("otherNewActionClass", StringUtils
                .toHex(this.otherNewActionClass));
        }
        if (this.otherNewAction != 255)
        {
            element.addAttribute("otherNewAction", StringUtils
                .toHex(this.otherNewAction));
        }

        for (final Answer answer: this.answers)
        {
            element.add(answer.toXml());
        }

        return element;
    }


    /**
     * @see de.ailis.wlandsuite.game.parts.Action#write(de.ailis.wlandsuite.io.SeekableOutputStream,
     *      de.ailis.wlandsuite.game.parts.SpecialActionTable)
     */

    public void write(final SeekableOutputStream stream,
        final SpecialActionTable specialActionTable)
    {
        stream.write(this.message & 127 | (this.menu ? 128 : 0));
        stream.write(this.cancelNewActionClass);
        stream.write(this.cancelNewAction);

        for (int i = 0, max = this.answers.size(); i < max; i++)
        {
            final Answer answer = this.answers.get(i);
            stream.write((answer.getMessage() & 127)
                | ((i == max - 1) ? 128 : 0));
        }
        for (final Answer answer: this.answers)
        {
            stream.write(answer.getNewActionClass());
            stream.write(answer.getNewAction());
        }

        stream.write(this.otherNewActionClass);
        if (this.otherNewActionClass < 253)
        {
            stream.write(this.otherNewAction);
        }
    }


    /**
     * Returns the cancelNewAction.
     *
     * @return The cancelNewAction
     */

    public int getCancelNewAction()
    {
        return this.cancelNewAction;
    }


    /**
     * Sets the cancelNewAction.
     *
     * @param cancelNewAction
     *            The cancelNewAction to set
     */

    public void setCancelNewAction(final int cancelNewAction)
    {
        this.cancelNewAction = cancelNewAction;
    }


    /**
     * Returns the cancelNewActionClass.
     *
     * @return The cancelNewActionClass
     */

    public int getCancelNewActionClass()
    {
        return this.cancelNewActionClass;
    }


    /**
     * Sets the cancelNewActionClass.
     *
     * @param cancelNewActionClass
     *            The cancelNewActionClass to set
     */

    public void setCancelNewActionClass(final int cancelNewActionClass)
    {
        this.cancelNewActionClass = cancelNewActionClass;
    }


    /**
     * Returns the menu.
     *
     * @return The menu
     */

    public boolean isMenu()
    {
        return this.menu;
    }


    /**
     * Sets the menu.
     *
     * @param menu
     *            The menu to set
     */

    public void setMenu(final boolean menu)
    {
        this.menu = menu;
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

    public void setMessage(final int message)
    {
        this.message = message;
    }


    /**
     * Returns the otherNewAction.
     *
     * @return The otherNewAction
     */

    public int getOtherNewAction()
    {
        return this.otherNewAction;
    }


    /**
     * Sets the otherNewAction.
     *
     * @param otherNewAction
     *            The otherNewAction to set
     */

    public void setOtherNewAction(final int otherNewAction)
    {
        this.otherNewAction = otherNewAction;
    }


    /**
     * Returns the otherNewActionClass.
     *
     * @return The otherNewActionClass
     */

    public int getOtherNewActionClass()
    {
        return this.otherNewActionClass;
    }


    /**
     * Sets the otherNewActionClass.
     *
     * @param otherNewActionClass
     *            The otherNewActionClass to set
     */

    public void setOtherNewActionClass(final int otherNewActionClass)
    {
        this.otherNewActionClass = otherNewActionClass;
    }


    /**
     * Adds a new answer
     *
     * @param answer
     *            The answer to add
     */

    public void addAnswer(final Answer answer)
    {
        this.answers.add(answer);
    }


    /**
     * Returns the answer with the specified index.
     *
     * @param index
     *            The index
     * @return The answer
     */

    public Answer getAnswer(final int index)
    {
        return this.answers.get(index);
    }


    /**
     * Sets a answer at a specific index.
     *
     * @param index
     *            The index
     * @param answer
     *            The answer
     */

    public void setAnswer(final int index, final Answer answer)
    {
        this.answers.set(index, answer);
    }


    /**
     * Deletes all answers
     */

    public void clearAnswers()
    {
        this.answers.clear();
    }


    /**
     * Removes the answer at the specified index.
     *
     * @param index
     *            The index
     */

    public void removeAnswer(final int index)
    {
        this.answers.remove(index);
    }


    /**
     * Returns the number of answers.
     *
     * @return The number of answers
     */

    public int countAnswers()
    {
        return this.answers.size();
    }


    /**
     * Inserts a answer at a specific index.
     *
     * @param index
     *            The index
     * @param answer
     *            The answer
     */

    public void addAnswer(final int index, final Answer answer)
    {
        this.answers.add(index, answer);
    }
}
