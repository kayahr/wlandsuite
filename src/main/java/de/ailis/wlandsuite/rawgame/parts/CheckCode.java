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

import de.ailis.wlandsuite.utils.XMLUtils;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.ailis.wlandsuite.io.BitInputStreamWrapper;
import de.ailis.wlandsuite.io.BitOutputStreamWrapper;
import de.ailis.wlandsuite.rawgame.GameException;


/**
 * Check code
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class CheckCode extends AbstractPart
{
    /** The flags */
    private int flags;    
    
    /** The string to display when entering the square */
    private int enterString;
    
    /** The string to display if the check passed */
    private int passString;
    
    /** The string to display if the check failed */
    private int failString;
    
    /** The new action class to set when the check passed (255 means setting no new action) */
    private int passActionClass;
    
    /** The new action selector to set when the check passed (255 means setting no new selector) */
    private int passActionSelector;

    /** The new action class to set when the check failed (255 means setting no new action) */
    private int failActionClass;
    
    /** The new action selector to set when the check failed (255 means setting no new selector) */
    private int failActionSelector;

    /** The unknown byte at position 08 */
    private int unknown08;
    
    /** The unknown byte at position 09 */
    private int unknown09;
    
    /** The check bytes */
    private byte[] checks;
    
    /** The bugfix id */
    private int bugfix;


    /**
     * Constructor
     * 
     * @param bytes
     *            The game block data
     * @param offset
     *            The offset of the part in the game block
     */

    public CheckCode(byte[] bytes, int offset)
    {
        int b;
        BitInputStreamWrapper bitStream;
        
        this.offset = offset;
        try
        {
            bitStream = new BitInputStreamWrapper(new ByteArrayInputStream(bytes,
                offset, bytes.length - offset));

            this.flags = bitStream.readByte();
            this.enterString = bitStream.readByte();
            this.passString = bitStream.readByte();
            this.failString = bitStream.readByte();
            this.passActionClass = bitStream.readByte();
            this.passActionSelector = bitStream.readByte();
            this.failActionClass = bitStream.readByte();
            this.failActionSelector = bitStream.readByte();
            this.unknown08 = bitStream.readByte();
            this.unknown09 = bitStream.readByte();
            
            ByteArrayOutputStream checks = new ByteArrayOutputStream();
            b = bitStream.readByte(); 
            while (b != 255)
            {
                checks.write(b);
                checks.write(bitStream.readByte());
                b = bitStream.readByte();
            }
            this.checks = checks.toByteArray();
            
            this.size = 11 + this.checks.length;
            
            
            // Bugfix for Safe-check on map 3 of game1
            if (this.offset == 2159 && this.size == 19)
            {
                System.out.println("Patching safe-check on map 3");
                byte[] newChecks = new byte[4];
                newChecks[0] = this.checks[0];
                newChecks[1] = this.checks[1];
                newChecks[2] = this.checks[2];
                newChecks[3] = this.checks[3];
                this.checks = newChecks;
                this.size = 14;
                this.bugfix = 1;
            }
            
            // Bugfix for Safe-Check on map 4 of game1
            if (this.offset == 2237 && this.size == 31)
            {
                System.out.println("Patching safe-check on map 4");
                byte[] newChecks = new byte[4];
                newChecks[0] = (byte) this.unknown09;
                newChecks[1] = this.checks[0];
                newChecks[2] = this.checks[1];
                newChecks[3] = this.checks[2];
                this.checks = newChecks;
                this.unknown09 = 0;
                this.failActionSelector = 0xff;
                this.size = 14;
                this.bugfix = 2;
            }
            
            // Bugfix for barrier check on map 7 of game2
            if (this.offset == 1893 && this.size == 107)
            {
                System.out.println("Patching barrier check on map 7");
                byte[] newChecks = new byte[] { 0, 0 };
                this.checks = newChecks;
                this.size = 12;
                this.bugfix = 3;
            }
        }
        catch (IOException e)
        {
            throw new GameException(e.toString(), e);
        }
    }


    /**
     * Creates the central directory from XML.
     * 
     * @param element
     *            The XML element
     */

    @SuppressWarnings("unchecked")
    public CheckCode(Element element)
    {
        super();

        this.flags = Integer.parseInt(element.attributeValue("flags")); 
        this.enterString = Integer.parseInt(element.attributeValue("enterString")); 
        this.passString = Integer.parseInt(element.attributeValue("passString")); 
        this.failString = Integer.parseInt(element.attributeValue("failString")); 
        this.passActionClass = Integer.parseInt(element.attributeValue("passClass"));
        this.passActionSelector = Integer.parseInt(element.attributeValue("passSelector"));
        this.failActionClass = Integer.parseInt(element.attributeValue("failClass"));
        this.failActionSelector = Integer.parseInt(element.attributeValue("failSelector"));
        this.unknown08 = Integer.parseInt(element.attributeValue("unknown08"));
        this.unknown09 = Integer.parseInt(element.attributeValue("unknown09"));
        this.bugfix = Integer.parseInt(element.attributeValue("bugfix", "0"));
        
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        String data = element.getTextTrim();
        for (String c: data.split("\\s"))
        {
            int b = Integer.valueOf(c, 16);
            stream.write(b);
        }
        this.checks = stream.toByteArray();
    }


    /**
     * @see de.ailis.wlandsuite.rawgame.parts.Part#toXml()
     */

    public Element toXml()
    {
        Element element;

        element = XMLUtils.createElement("check");
        element.addAttribute("offset", Integer.toString(this.offset));
        element.addAttribute("flags", Integer.toString(this.flags));
        element.addAttribute("enterString", Integer.toString(this.enterString));
        element.addAttribute("passString", Integer.toString(this.passString));
        element.addAttribute("failString", Integer.toString(this.failString));
        element.addAttribute("passClass", Integer.toString(this.passActionClass));
        element.addAttribute("passSelector", Integer.toString(this.passActionSelector));
        element.addAttribute("failClass", Integer.toString(this.failActionClass));
        element.addAttribute("failSelector", Integer.toString(this.failActionSelector));
        element.addAttribute("unknown08", Integer.toString(this.unknown08));
        element.addAttribute("unknown09", Integer.toString(this.unknown09));
        if (this.bugfix != 0)
        {
            element.addAttribute("bugfix", Integer.toString(this.bugfix));
        }

        StringWriter text = new StringWriter();
        PrintWriter writer = new PrintWriter(text);

        int size = this.checks.length;
        if (size > 9)
        {
            writer.println();
            writer.print("    ");
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
                    writer.print("    ");
                }
                else
                {
                    writer.print(" ");
                }
            }
            writer.format("%02x", new Object[] { this.checks[i] });
        }
        if (size > 9)
        {
            writer.println();
            writer.print("  ");
        }

        element.add(DocumentHelper.createText(text.toString()));
                
        return element;
    }


    /**
     * @see de.ailis.wlandsuite.rawgame.parts.Part#write(java.io.OutputStream, int)
     */
    
    public void write(OutputStream stream, int offset) throws IOException
    {
        BitOutputStreamWrapper bitStream;

        bitStream = new BitOutputStreamWrapper(stream);
        bitStream.writeByte(this.flags);
        bitStream.writeByte(this.enterString);
        bitStream.writeByte(this.passString);
        bitStream.writeByte(this.failString);
        bitStream.writeByte(this.passActionClass);
        bitStream.writeByte(this.passActionSelector);
        bitStream.writeByte(this.failActionClass);
        if (this.bugfix != 2)
        {
            bitStream.writeByte(this.failActionSelector);
        }
        bitStream.writeByte(this.unknown08);
        bitStream.writeByte(this.unknown09);
        
        if (this.bugfix != 3)
        {
            for (byte b: this.checks)
            {
                bitStream.writeByte(b & 0xff);
            }
        }
        else
        {
            bitStream.writeByte(0);
        }
        if (this.bugfix != 1)
        {
            bitStream.writeByte(0xff);
        }
        
        bitStream.flush();
    }


    /**
     * Returns the checks.
     *
     * @return The checks
     */
    
    public byte[] getChecks()
    {
        return this.checks;
    }


    /**
     * Sets the checks.
     *
     * @param checks 
     *            The checks to set
     */
    
    public void setChecks(byte[] checks)
    {
        this.checks = checks;
    }


    /**
     * Returns the enterString.
     *
     * @return The enterString
     */
    
    public int getEnterString()
    {
        return this.enterString;
    }


    /**
     * Sets the enterString.
     *
     * @param enterString 
     *            The enterString to set
     */
    
    public void setEnterString(int enterString)
    {
        this.enterString = enterString;
    }


    /**
     * Returns the failActionClass.
     *
     * @return The failActionClass
     */
    
    public int getFailActionClass()
    {
        return this.failActionClass;
    }


    /**
     * Sets the failActionClass.
     *
     * @param failActionClass 
     *            The failActionClass to set
     */
    
    public void setFailActionClass(int failActionClass)
    {
        this.failActionClass = failActionClass;
    }


    /**
     * Returns the failActionSelector.
     *
     * @return The failActionSelector
     */
    
    public int getFailActionSelector()
    {
        return this.failActionSelector;
    }


    /**
     * Sets the failActionSelector.
     *
     * @param failActionSelector 
     *            The failActionSelector to set
     */
    
    public void setFailActionSelector(int failActionSelector)
    {
        this.failActionSelector = failActionSelector;
    }


    /**
     * Returns the failString.
     *
     * @return The failString
     */
    
    public int getFailString()
    {
        return this.failString;
    }


    /**
     * Sets the failString.
     *
     * @param failString 
     *            The failString to set
     */
    
    public void setFailString(int failString)
    {
        this.failString = failString;
    }


    /**
     * Returns the flags.
     *
     * @return The flags
     */
    
    public int getFlags()
    {
        return this.flags;
    }


    /**
     * Sets the flags.
     *
     * @param flags 
     *            The flags to set
     */
    
    public void setFlags(int flags)
    {
        this.flags = flags;
    }


    /**
     * Returns the passActionClass.
     *
     * @return The passActionClass
     */
    
    public int getPassActionClass()
    {
        return this.passActionClass;
    }


    /**
     * Sets the passActionClass.
     *
     * @param passActionClass 
     *            The passActionClass to set
     */
    
    public void setPassActionClass(int passActionClass)
    {
        this.passActionClass = passActionClass;
    }


    /**
     * Returns the passActionSelector.
     *
     * @return The passActionSelector
     */
    
    public int getPassActionSelector()
    {
        return this.passActionSelector;
    }


    /**
     * Sets the passActionSelector.
     *
     * @param passActionSelector 
     *            The passActionSelector to set
     */
    
    public void setPassActionSelector(int passActionSelector)
    {
        this.passActionSelector = passActionSelector;
    }


    /**
     * Returns the passString.
     *
     * @return The passString
     */
    
    public int getPassString()
    {
        return this.passString;
    }


    /**
     * Sets the passString.
     *
     * @param passString 
     *            The passString to set
     */
    
    public void setPassString(int passString)
    {
        this.passString = passString;
    }


    /**
     * Returns the unknown08.
     *
     * @return The unknown08
     */
    
    public int getUnknown08()
    {
        return this.unknown08;
    }


    /**
     * Sets the unknown08.
     *
     * @param unknown08 
     *            The unknown08 to set
     */
    
    public void setUnknown08(int unknown08)
    {
        this.unknown08 = unknown08;
    }


    /**
     * Returns the unknown09.
     *
     * @return The unknown09
     */
    
    public int getUnknown09()
    {
        return this.unknown09;
    }


    /**
     * Sets the unknown09.
     *
     * @param unknown09 
     *            The unknown09 to set
     */
    
    public void setUnknown09(int unknown09)
    {
        this.unknown09 = unknown09;
    }
}
