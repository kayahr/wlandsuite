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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import de.ailis.wlandsuite.io.BitInputStream;
import de.ailis.wlandsuite.io.BitInputStreamWrapper;
import de.ailis.wlandsuite.io.BitOutputStream;


/**
 * Huffman Tree
 *
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class HuffmanTree
{
    /** The root node of the huffman tree */
    private HuffmanNode rootNode;

    /** The index. Mapping payloads to nodes */
    private final Map<Integer, HuffmanNode> index = new HashMap<Integer, HuffmanNode>();


    /**
     * Private constructor.
     */

    private HuffmanTree()
    {
        super();
    }


    /**
     * Constructs a new huffman tree. The tree is created for the data which can
     * be read from the stream.
     *
     * @param stream
     *            The stream to read the data from
     * @return The Huffman tree
     * @throws IOException
     *             When file operation fails.
     */

    public static HuffmanTree create(final InputStream stream) throws IOException
    {
        HuffmanTree tree;

        tree = new HuffmanTree();
        tree.createTree(stream);
        return tree;
    }


    /**
     * Creates a huffman tree from a byte array.
     *
     * @param bytes The bytes to build the huffman tree for
     * @return The huffman tree
     */

    public static HuffmanTree create(final byte[] bytes)
    {
        ByteArrayInputStream stream;

        try
        {
            stream = new ByteArrayInputStream(bytes);
            try
            {
                return create(stream);
            }
            finally
            {
                stream.close();
            }
        }
        catch (final IOException e)
        {
            // Ignored. Can't happen
            return null;
        }
    }


    /**
     * Loads a Huffman tree from an already encoded data stream
     *
     * @param stream
     *            The data stream
     * @return The Huffman Tree
     * @throws IOException
     *             When file operation fails.
     */

    public static HuffmanTree load(final InputStream stream) throws IOException
    {
        HuffmanTree tree;

        tree = new HuffmanTree();
        if (stream instanceof BitInputStream)
        {
            tree.loadTree((BitInputStream) stream);
        }
        else
        {
            tree.loadTree(new BitInputStreamWrapper(stream));
        }
        return tree;
    }


    /**
     * Creates the root node of the Huffman Tree.
     *
     * @param stream
     *            The input stream to build the root node for.
     * @throws IOException
     *             When file operation fails.
     */

    private void createTree(final InputStream stream) throws IOException
    {
        int[] counter;
        int b;
        HuffmanNode node, left, right;
        SortedSet<HuffmanNode> nodes;

        // Read the stream and build the counter array
        counter = new int[256];
        while ((b = stream.read()) != -1)
        {
            counter[b]++;
        }

        // Initialize the nodes with it's payloads and probabilities
        nodes = new TreeSet<HuffmanNode>();
        for (int i = 0; i < 256; i++)
        {
            if (counter[i] == 0) continue;
            node = new HuffmanNode(i, counter[i]);
            nodes.add(node);
            this.index.put(i, node);
        }

        // Repeat until only one node (the root node) is left
        while (nodes.size() > 1)
        {
            // Take the last two nodes from the list
            left = nodes.last();
            nodes.remove(left);
            right = nodes.last();
            nodes.remove(right);

            // Create the parent node for them and add it to the list
            node = new HuffmanNode(left, right);
            nodes.add(node);
        }

        // Save the root node
        this.rootNode = nodes.first();
    }


    /**
     * Loads the Huffman tree from the specified bit stream.
     *
     * @param stream
     *            The stream to read the tree from
     * @throws IOException
     *             When file operation fails.
     */

    private void loadTree(final BitInputStream stream) throws IOException
    {
        this.rootNode = loadNode(stream);
    }


    /**
     * Saves the huffman tree.
     *
     * @param bitStream
     *            The stream to save the tree to
     * @throws IOException
     *             When file operation fails.
     */

    void save(final BitOutputStream bitStream) throws IOException
    {
        saveNode(this.rootNode, bitStream);
    }


    /**
     * Saves a huffman node to a bit stream.
     *
     * @param node
     *            The huffman node to save
     * @param bitStream
     *            The bit stream to write the node to
     * @throws IOException
     *             When file operation fails.
     */

    private void saveNode(final HuffmanNode node, final BitOutputStream bitStream)
        throws IOException
    {
        int payload;

        payload = node.getPayload();
        if (payload == -1)
        {
            bitStream.writeBit(false);
            saveNode(node.getLeft(), bitStream);
            bitStream.writeBit(false);
            saveNode(node.getRight(), bitStream);
        }
        else
        {
            bitStream.writeBit(true);
            bitStream.writeByte(payload);
        }
    }


    /**
     * Builds a huffman node and returns it.
     *
     * @param stream
     *            The bit stream to read data from
     * @return The huffman node
     * @throws IOException
     *             When file operation fails.
     */

    private HuffmanNode loadNode(final BitInputStream stream) throws IOException
    {
        int b;
        HuffmanNode node;

        if (stream.readBit() == 0)
        {
            HuffmanNode left, right;

            left = loadNode(stream);
            stream.readBit();
            right = loadNode(stream);
            return new HuffmanNode(left, right);
        }
        else
        {
            b = stream.readByte();
            node = new HuffmanNode(b);
            this.index.put(Integer.valueOf(b), node);
            return node;
        }
    }


    /**
     * Returns the root node of the huffman tree.
     *
     * @return The root node
     */

    public HuffmanNode getRootNode()
    {
        return this.rootNode;
    }


    /**
     * Returns the huffman node for the given payload.
     *
     * @param payload
     *            The payload
     * @return The Huffman node
     */

    public HuffmanNode getNode(final int payload)
    {
        return this.index.get(Integer.valueOf(payload));
    }


    /**
     * @see java.lang.Object#toString()
     */

    @Override
    public String toString()
    {
        StringBuilder builder;

        builder = new StringBuilder();
        for (final HuffmanNode node: this.index.values())
        {
            builder.append(node.getFullKeyName());
            builder.append("=");
            builder.append(node.getPayload());
            builder.append("\n");
        }
        return builder.toString();
    }
}
