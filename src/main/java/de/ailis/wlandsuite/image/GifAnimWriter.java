package de.ailis.wlandsuite.image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

/*
 * $Id$
 * Copyright (C) 2009 Klaus Reimer <k@ailis.de>
 * See LICENSE.txt file for licensing information.
 */


/**
 * GIF animation writer
 *
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class GifAnimWriter
{
    /**
     * Disposal method
     */

    public enum DisposalMethod
    {
        /** Disposal method "none" */
        NONE("none"),

        /** Disposal method "doNotDispose" */
        DO_NOT_DISPOSE("doNotDispose"),

        /** Disposal method "restoreToBackgroundColor" */
        RESTORE_TO_BACKGROUND_COLOR("restoreToBackgroundColor"),

        /** Disposal method "restoreToPrevious" */
        RESTORE_TO_PREVIOUS("restoreToPrevious");

        /** The disposal method name */
        private String name;


        /**
         * Constructor
         *
         * @param name
         *            The disposal method name
         */
        private DisposalMethod(final String name)
        {
            this.name = name;
        }


        /**
         * Returns the disposal method name.
         *
         * @return The disposal method name
         */

        public String getName()
        {
            return this.name;
        }
    }

    /** The image output stream */
    private final ImageOutputStream stream;

    /** The image writer */
    private final ImageWriter writer;

    /** The number of loops (0 = Endless loop) */
    private final int loops;

    /** The time the next frame is displayed (in milliseconds) */
    private int delay = 0;

    /** The next disposal method */
    private DisposalMethod disposalMethod = DisposalMethod.NONE;

    /** The next transparent index (null = No transparency) */
    private Integer transparentIndex = null;


    /**
     * Constructor.
     *
     * @param output
     *            The output file
     * @param loops
     *            The number of loops (0 = Endless loop)
     * @throws IOException
     *             If GIF could not be written
     */

    public GifAnimWriter(final File output, final int loops)
        throws IOException
    {
        if (output.exists()) output.delete();
        this.stream = new FileImageOutputStream(output);
        this.writer = findWriter();
        this.writer.setOutput(this.stream);
        this.loops = loops;
        this.writer.prepareWriteSequence(null);
    }


    /**
     * Finds a writer for image/gif mime type and returns it.
     *
     * @return The writer to use
     * @throws IOException
     *             If no writer could be found
     */

    private ImageWriter findWriter() throws IOException
    {
        final Iterator<ImageWriter> writers =
            ImageIO.getImageWritersByMIMEType("image/gif");
        if (!writers.hasNext())
            throw new IOException("Can't find writer for image/gif");
        return writers.next();
    }


    /**
     * Creates and returns the metadata for the next frame.
     *
     * @param image
     *            The next frame
     * @return The metadata for the next frame
     * @throws IOException
     *             If metadata could not be created
     */

    private IIOMetadata createMetaData(final BufferedImage image)
        throws IOException
    {
        final ImageWriteParam imageWriteParam =
            this.writer.getDefaultWriteParam();
        final ImageTypeSpecifier imageTypeSpecifier =
            new ImageTypeSpecifier(image);
        final IIOMetadata metaData =
            this.writer.getDefaultImageMetadata(imageTypeSpecifier,
                imageWriteParam);
        final String metaFormatName = metaData.getNativeMetadataFormatName();
        final IIOMetadataNode root =
            (IIOMetadataNode) metaData.getAsTree(metaFormatName);
        IIOMetadataNode child = getNode(root, "GraphicControlExtension");
        child.setAttribute("disposalMethod", this.disposalMethod.getName());
        child.setAttribute("userInputFlag", "FALSE");
        child.setAttribute("transparentColorFlag",
            this.transparentIndex == null ? "FALSE" : "TRUE");
        child.setAttribute("transparentColorIndex",
            this.transparentIndex == null ? "0" : Integer
                .toString(this.transparentIndex));
        child.setAttribute("delayTime", Integer.toString(this.delay / 10));

        final IIOMetadataNode appEntensionsNode =
            getNode(root, "ApplicationExtensions");
        child = new IIOMetadataNode("ApplicationExtension");
        child.setAttribute("applicationID", "NETSCAPE");
        child.setAttribute("authenticationCode", "2.0");
        child.setUserObject(new byte[] { 0x1, (byte) (this.loops & 0xFF),
            (byte) ((this.loops >> 8) & 0xFF) });
        appEntensionsNode.appendChild(child);

        metaData.setFromTree(metaFormatName, root);
        return metaData;
    }


    /**
     * Returns the node with the given name.
     *
     * @param rootNode
     *            The root node
     * @param nodeName
     *            The name to search for
     * @return The found node
     */

    private static IIOMetadataNode getNode(final IIOMetadataNode rootNode,
        final String nodeName)
    {
        final int size = rootNode.getLength();
        for (int i = 0; i < size; i++)
        {
            if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName) == 0)
            {
                return ((IIOMetadataNode) rootNode.item(i));
            }
        }
        final IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return node;
    }


    /**
     * Sets the display time of the next frame to the specified number of
     * milliseconds. Default wait time is 0.
     *
     * @param delay
     *            The next display time in milliseconds
     */

    public void setDelay(final int delay)
    {
        this.delay = delay;
    }


    /**
     * Returns the display time of the next frame in milliseconds. Default is 0.
     *
     * @return The next display time in milliseconds.
     */

    public int getDelay()
    {
        return this.delay;
    }


    /**
     * Sets the index of the transparent color in the next frame. null means no
     * transparency.
     *
     * @param index
     *            The index of the transparent color or null for no transparency
     */

    public void setTransparentIndex(final Integer index)
    {
        this.transparentIndex = index;
    }


    /**
     * Returns the index of the transparent color in the next frame. May return
     * null if no transparency is used.
     *
     * @return The index of the transparent color or null if none
     */

    public Integer getTransparentIndex()
    {
        return this.transparentIndex;
    }


    /**
     * Sets the disposal method for the next frame.
     *
     * @param disposalMethod
     *            The disposal method for the next frame. Must not be null.
     */

    public void setDisposalMethod(final DisposalMethod disposalMethod)
    {
        if (disposalMethod == null)
            throw new IllegalArgumentException("method must be set");
        this.disposalMethod = disposalMethod;
    }


    /**
     * Returns the disposal method for the next frame.
     *
     * @return The disposal method for the next frame. Never null.
     */

    public DisposalMethod getDisposalMethod()
    {
        return this.disposalMethod;
    }


    /**
     * Adds a new frame.
     *
     * @param image
     *            The frame to add
     * @throws IOException
     *             When file operation fails.
     */

    public void addFrame(final BufferedImage image) throws IOException
    {
        final ImageWriteParam imageWriteParam =
            this.writer.getDefaultWriteParam();
        this.writer.writeToSequence(new IIOImage(image, null,
            createMetaData(image)), imageWriteParam);
    }


    /**
     * Closes all open resources.
     *
     * @throws IOException
     *             If resources could not be closed
     */

    public void close() throws IOException
    {
        this.writer.endWriteSequence();
        this.stream.close();
    }
}
