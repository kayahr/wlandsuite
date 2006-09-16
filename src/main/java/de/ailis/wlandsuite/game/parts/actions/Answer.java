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

package de.ailis.wlandsuite.game.parts.actions;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;


/**
 * A dialogue item used in the Dialogue Action
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Answer
{
    /** The answer message */
    private int message;

    /** The new action class to set when this answer is selected */
    private int newActionClass;

    /** The new action to set when this answer is selected */
    private int newAction;


    /**
     * Constructor
     * 
     * @param answer
     *            The answer
     * @param newActionClass
     *            The new action class
     * @param newAction
     *            The new action
     */

    public Answer(int answer, int newActionClass, int newAction)
    {
        this.message = answer;
        this.newActionClass = newActionClass;
        this.newAction = newAction;
    }
    
    
    /**
     * Constructor
     */
    
    public Answer()
    {
        super();
    }


    /**
     * Returns the check data as XML.
     * 
     * @return The check data as XML
     */

    public Element toXml()
    {
        Element element;

        element = DocumentHelper.createElement("answer");
        element.addAttribute("message", Integer.toString(this.message));
        element.addAttribute("newActionClass", Integer.toString(this.newActionClass));
        element.addAttribute("newAction", Integer.toString(this.newAction));
        return element;
    }


    /**
     * Creates and returns a new Check object by reading its data from XML.
     * 
     * @param element
     *            The XML element
     * @return The check data
     */

    public static Answer read(Element element)
    {
        int message, newActionClass, newAction;

        message = Integer.parseInt(element.attributeValue("message"));
        newActionClass = Integer.parseInt(element.attributeValue("newActionClass"));
        newAction = Integer.parseInt(element.attributeValue("newAction"));
        return new Answer(message, newActionClass, newAction);
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
     * Returns the message.
     *
     * @return The message
     */
    
    public int getMessage()
    {
        return this.message;
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
     * Returns the newActionClass.
     *
     * @return The newActionClass
     */
    
    public int getNewActionClass()
    {
        return this.newActionClass;
    }      
}
