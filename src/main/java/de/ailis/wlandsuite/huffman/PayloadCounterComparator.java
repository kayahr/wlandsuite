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

package de.ailis.wlandsuite.huffman;

import java.util.Comparator;


/**
 * A comparator used for sorting the payloads by count.
 *
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class PayloadCounterComparator implements Comparator<Integer>
{
    /** The payload counter */
    private final int[] counter;

    /**
     * Constructor
     *
     * @param counter
     *            The payload counter
     */

    public PayloadCounterComparator(final int[] counter)
    {
        this.counter = counter;
    }


    /**
     * Compares the counters of the two given payloads.
     *
     * @param o1
     *            The payload 1
     * @param o2
     *            The payload 2
     * @return The compare result
     */

    @Override
    public int compare(final Integer o1, final Integer o2)
    {
        int v1, v2;

        v1 = this.counter[o1.intValue()];
        v2 = this.counter[o2.intValue()];
        if (v1 < v2)
        {
            return 1;
        }
        else if (v1 > v2)
        {
            return -1;
        }
        else
        {
            return 0;
        }
    }
}
