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

import java.io.InputStream;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;


/**
 * This Entity Resolver resolves XSD files on the classpath.
 *
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class ClasspathEntityResolver implements EntityResolver
{
    /** The singleton instance */
    private static ClasspathEntityResolver instance = new ClasspathEntityResolver();


    /**
     * Private constructor
     */

    private ClasspathEntityResolver()
    {
        super();
    }


    /**
     * Returns the singleton instance of the Resolver.
     *
     * @return The singleton resolver
     */

    public static ClasspathEntityResolver getInstance()
    {
        return instance;
    }


    /**
     * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
     */

    @Override
    public InputSource resolveEntity(final String publicId, final String systemId)
    {
        InputStream stream;

        // Return null if we are not responsible
        if (!systemId.startsWith("classpath://"))
        {
            return null;
        }

        // Return the input source if the file was found on the class path
        stream = getClass().getClassLoader().getResourceAsStream(systemId.substring(12));
        if (stream != null)
        {
            return new InputSource(stream);
        }

        // Return null if file was not found on the class path
        return null;
    }
}
