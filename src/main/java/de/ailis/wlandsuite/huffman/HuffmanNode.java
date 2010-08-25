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


/**
 * A Node in the Huffman tree.
 *
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class HuffmanNode implements Comparable<HuffmanNode>
{
    /** The left sub node */
    private HuffmanNode left;

    /** The right sub node */
    private HuffmanNode right;

    /** The parent node */
    private HuffmanNode parent;

    /** The node key (false = left, true = right) */
    private boolean key;

    /** The full node key */
    private boolean[] fullKey;

    /** The payload */
    private final int payload;

    /** The propability */
    private int probability;

    /** The id generator */
    private static int nextId = 0;

    /** The serial id */
    private final int id = nextId++;


    /**
     * Constructor
     *
     * @param left
     *            The left sub node
     * @param right
     *            The right sub node
     */

    public HuffmanNode(final HuffmanNode left, final HuffmanNode right)
    {
        this.left = left;
        if (this.left != null)
        {
            this.left.parent = this;
            this.left.key = false;
        }
        this.right = right;
        if (this.right != null)
        {
            this.right.parent = this;
            this.right.key = true;
        }
        this.payload = -1;
        this.probability = this.left.probability + this.right.probability;
    }


    /**
     * Constructor
     *
     * @param payload
     *            The payload
     */

    public HuffmanNode(final int payload)
    {
        this.payload = payload;
    }


    /**
     * Constructor
     *
     * @param payload
     *            The payload
     * @param probability
     *            The probability
     */

    public HuffmanNode(final int payload, final int probability)
    {
        this.payload = payload;
        this.probability = probability;
    }


    /**
     * Returns the left.
     *
     * @return The left
     */

    public HuffmanNode getLeft()
    {
        return this.left;
    }


    /**
     * Returns the payload.
     *
     * @return The payload
     */

    public int getPayload()
    {
        return this.payload;
    }


    /**
     * Returns the right sub node.
     *
     * @return The right sub node
     */

    public HuffmanNode getRight()
    {
        return this.right;
    }


    /**
     * Returns the node key.
     *
     * @return The node key
     */

    public boolean getKey()
    {
        return this.key;
    }


    /**
     * Returns the parent node.
     *
     * @return The parent node
     */

    public HuffmanNode getParent()
    {
        return this.parent;
    }


    /**
     * Returns the full key of this node (Beginning from the root).
     *
     * @return The full key
     */

    public boolean[] getFullKey()
    {
        HuffmanNode current;
        int count;
        int index;

        // If there is already a cached full key then use it
        if (this.fullKey == null)
        {
            // Find out how many key bits are needed for the full key
            count = 0;
            current = this;
            while (current.parent != null)
            {
                count++;
                current = current.parent;
            }

            // Build the full key
            this.fullKey = new boolean[count];
            current = this;
            index = 0;
            while (current.parent != null)
            {
                this.fullKey[index] = current.key;
                current = current.parent;
                index++;
            }
        }
        return this.fullKey;
    }


    /**
     * Returns the string representation of the node key. For the root node
     * "ROOT" is returned
     *
     * @return The node key as a string
     */

    public String getFullKeyName()
    {
        boolean[] fullKey;
        StringBuilder builder;

        if (this.parent == null)
        {
            return "ROOT";
        }

        fullKey = getFullKey();
        builder = new StringBuilder(fullKey.length);
        for (int i = fullKey.length - 1; i >= 0; i--)
        {
            builder.append(fullKey[i] ? '1' : '0');
        }
        return builder.toString();
    }


    /**
     * Returns the probability.
     *
     * @return The probability
     */

    public int getProbability()
    {
        return this.probability;
    }


    /**
     * Compares to Huffman Nodes.
     *
     * @param other
     *            The other Huffman Node.
     * @return The compare result
     */

    @Override
    public int compareTo(final HuffmanNode other)
    {
        if (this.probability < other.probability)
        {
            return 1;
        }
        else if (this.probability > other.probability)
        {
            return -1;
        }
        else
        {
            return (Integer.valueOf(this.id).compareTo(Integer
                .valueOf(other.id)));
        }
    }


    /**
     * Dumps the current node into the given string builder. Output is indented
     * by level*2 space characters. This method is used by toString() to
     * recursively build a nice tree output.
     *
     * @param builder
     *            The string builder
     * @param level
     *            The indent level
     */

    private void dumpNode(final StringBuilder builder, final int level)
    {
        char[] indent;

        indent = new char[level * 2];
        for (int i = 0; i < level * 2; i++)
        {
            indent[i] = ' ';
        }
        builder.append(getFullKeyName());
        if (this.payload == -1)
        {
            builder.append(":\n");
            builder.append(indent);
            builder.append("  Left -> ");
            this.left.dumpNode(builder, level + 1);
            builder.append('\n');
            builder.append(indent);
            builder.append("  Right -> ");
            this.right.dumpNode(builder, level + 1);
        }
        else
        {
            builder.append(" = ");
            builder.append(this.payload);
        }
    }


    /**
     * @see java.lang.Object#toString()
     */

    @Override
    public String toString()
    {
        StringBuilder builder;

        builder = new StringBuilder();
        dumpNode(builder, 0);
        return builder.toString();
    }
}
