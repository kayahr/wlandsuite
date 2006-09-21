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

package de.ailis.wlandsuite.utils;

import java.io.UnsupportedEncodingException;


/**
 * String utility methods
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class StringUtils
{
    /**
     * Escapes special characters in a string. Returns null if the encoding is
     * unknown.
     * 
     * @param string
     *            The raw string
     * @param encoding
     *            The encoding of the string
     * @return The escaped string
     */

    public static String escape(String string, String encoding)
    {
        StringBuilder result;

        result = new StringBuilder(string.length());
        try
        {
            for (byte c: string.getBytes(encoding))
            {
                switch (c)
                {
                    case '\n':
                        result.append("\\n");
                        break;
                    case '\r':
                        result.append("\\r");
                        break;
                    case '\t':
                        result.append("\\t");
                        break;
                    case '\b':
                        result.append("\\b");
                        break;
                    case '\\':
                        result.append("\\\\");
                        break;
                    case '\f':
                        result.append("\\f");
                        break;
                    default:
                        if (c < 32 || c > 126)
                        {
                            result.append(String.format("\\x%02x",
                                new Object[] { c }));
                        }
                        else
                        {
                            result
                                .append(new String(new byte[] { c }, encoding));
                        }
                }
            }
            return result.toString();
        }
        catch (UnsupportedEncodingException e)
        {
            return null;
        }
    }


    /**
     * Unescaped special characters in a string. Returns null if the encoding is
     * unknown.
     * 
     * @param string
     *            The escaped string
     * @param encoding
     *            The encoding of the string
     * @return The raw string
     */

    public static String unescape(String string, String encoding)
    {
        StringBuilder result;
        int mode = 0;
        int value = 0;

        result = new StringBuilder(string.length());
        try
        {
            for (byte c: string.getBytes(encoding))
            {
                switch (mode)
                {
                    case 0:
                        switch (c)
                        {
                            case '\\':
                                mode = 1;
                                break;
                            default:
                                result.append(new String(new byte[] { c },
                                    encoding));
                        }
                        break;

                    case 1:
                        switch (c)
                        {
                            case '\\':
                                result.append('\\');
                                mode = 0;
                                break;
                            case 'n':
                                result.append('\n');
                                mode = 0;
                                break;
                            case 'r':
                                result.append('\r');
                                mode = 0;
                                break;
                            case 't':
                                result.append('\t');
                                mode = 0;
                                break;
                            case 'b':
                                result.append('\b');
                                mode = 0;
                                break;
                            case 'f':
                                result.append('\f');
                                mode = 0;
                                break;
                            case 'x':
                                mode = 2;
                                break;
                        }
                        break;
                    case 2:
                        value = Integer.parseInt(new String(new byte[] { c },
                            encoding), 16) << 4;
                        mode = 3;
                        break;

                    case 3:
                        value |= Integer.parseInt(new String(new byte[] { c },
                            encoding), 16);
                        result.append(new String(new byte[] { (byte) value },
                            encoding));

                        value = 0;
                        mode = 0;
                        break;
                }
            }
            return result.toString();
        }
        catch (UnsupportedEncodingException e)
        {
            return null;
        }
    }


    /**
     * Converts a string into an int. The string can be written in hexadecimal
     * (0x prefix), binary (b suffix) or decimal. If value is null then 0 is
     * returned. If value is not parsable then a NumberFormatException is
     * thrown.
     * 
     * @param value
     *            The value as string
     * @return The int value
     */

    public static int toInt(String value)
    {
        // NULL is 0
        if (value == null)
        {
            return 0;
        }

        // Parse hexadecimal value
        if (value.startsWith("0x"))
        {
            return Integer.parseInt(value.substring(2), 16);
        }

        // Parse binary value
        if (value.endsWith("b"))
        {
            return Integer.parseInt(value.substring(0, value.length() - 1), 2);
        }

        // Parse decimal value
        return Integer.parseInt(value);
    }


    /**
     * Converts the specified int to a hex string with a 0x prefix.
     * 
     * @param value
     *            The decimal value
     * @return The hex string with 0x prefix
     */

    public static String toHex(int value)
    {
        return "0x" + Integer.toString(value, 16);
    }
}
