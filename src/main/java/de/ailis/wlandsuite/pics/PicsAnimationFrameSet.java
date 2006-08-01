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

package de.ailis.wlandsuite.pics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ailis.wlandsuite.pic.Pic;


/**
 * An Animation Frame Set contains a list of animation frames and the playing
 * instructions of the frames which defines the delay and play order of the
 * frames.
 * 
 * Each animation frame is just a transparent BufferedImage where only the part
 * which is changed in this animation frame is opaque. So to get the real
 * complete frame you have to overlay this transparent image over the base frame
 * image.
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class PicsAnimationFrameSet
{
    /** The animation frames */
    private List<Pic> frames;

    /** The animation instructions */
    private List<PicsAnimationInstruction> instructions;


    /**
     * Constructor
     */

    PicsAnimationFrameSet()
    {
        this.frames = new ArrayList<Pic>();
        this.instructions = new ArrayList<PicsAnimationInstruction>();
    }


    /**
     * Constructor
     * 
     * @param frames
     *            The frames
     * @param instructions
     *            The instructions
     */

    public PicsAnimationFrameSet(List<Pic> frames,
        List<PicsAnimationInstruction> instructions)
    {
        this.frames = frames;
        this.instructions = instructions;
    }


    /**
     * Adds a frame to the frame set.
     * 
     * @param delay
     *            The delay
     * @param frameNo
     *            The raw frame to display in this frame
     * @param baseFrame
     *            The base frame
     * @param workingFrame
     *            The current frame which acts as a base for this frame
     * @param rawFrames
     *            The raw frames
     */

    void addFrame(int delay, int frameNo, Pic baseFrame,
        Pic workingFrame, List<RawAnimationFrame> rawFrames)
    {
        int index;

        // Apply the raw animation to the current working frame
        rawFrames.get(frameNo).apply(workingFrame);

        // Get frame index. Store new frame if not already present
        index = getFrameIndex(workingFrame, baseFrame);
        if (index == -1)
        {
            this.frames.add(workingFrame.clone());
            index = this.frames.size();
        }

        // Add the animation instruction
        this.instructions.add(new PicsAnimationInstruction(delay, index));
    }


    /**
     * Returns the raw animation frames for this frame set. Map key is a string
     * in to format "fromFrame-toFrame". Example: "0-1" to indicate the raw
     * frame is updating frame 0 to get to frame 1.
     * 
     * @param baseFrame
     *            The base frame
     * @return The raw animation frames.
     */

    public Map<String, RawAnimationFrame> getRawFrames(Pic baseFrame)
    {
        Map<String, RawAnimationFrame> rawFrames;
        String key;
        int currentFrame, nextFrame;

        rawFrames = new HashMap<String, RawAnimationFrame>();
        currentFrame = 0;
        for (PicsAnimationInstruction instruction: this.instructions)
        {
            nextFrame = instruction.getFrame();
            key = getRawFrameKey(currentFrame, nextFrame);

            // Do nothing if a raw frame for this animation frame combination
            // has already been calculated.
            if (!rawFrames.containsKey(key))
            {
                // Create the raw frame and add it to the raw frames map
                rawFrames.put(key, getRawFrame(currentFrame, nextFrame,
                    baseFrame));
            }

            currentFrame = nextFrame;
        }
        return rawFrames;
    }


    /**
     * Calculated and returns a raw animation frame.
     * 
     * @param currentFrame
     *            The current frame number (0=base frame)
     * @param nextFrame
     *            The next frame number (0=base frame)
     * @param baseFrame
     *            The base frame
     * @return The raw animation frame
     */

    private RawAnimationFrame getRawFrame(int currentFrame, int nextFrame,
        Pic baseFrame)
    {
        Pic current, next;
        List<RawAnimationFramePart> parts;

        current = currentFrame == 0 ? baseFrame : this.frames
            .get(currentFrame - 1);
        next = nextFrame == 0 ? baseFrame : this.frames.get(nextFrame - 1);

        parts = getDiff(current, next);

        return new RawAnimationFrame(parts, 0);
    }


    /**
     * Returns an array with the diff values of two frames. The array is two
     * dimensional array. The first index is the y coordinate and the second is
     * the x corrdinate. Each array entry holds the byte for two pixels.
     * 
     * @param frame1
     *            The first frame to compare
     * @param frame2
     *            The second frame to compare
     * @return The diff
     */

    private List<RawAnimationFramePart> getDiff(Pic frame1, Pic frame2)
    {
        int x, y;
        int w, h;
        List<RawAnimationFramePart> diff;
        RawAnimationFramePart last, current;
        int xor;
        
        w = frame1.getWidth();
        h = frame1.getHeight();
        diff = new ArrayList<RawAnimationFramePart>();
        last = null;
        for (y = 0; y < h; y++)
        {
            for (x = 0; x < w; x += 2)
            {
                xor = (((frame1.getPixel(x, y) ^ frame2.getPixel(x,
                    y))) << 4)
                    | (frame1.getPixel(x + 1, y) ^ frame2.getPixel(x + 1, y));
                if (xor != 0)
                {
                    current = new RawAnimationFramePart((y * w +x) / 2, new int[] { xor }, 0);
                    if (last != null && last.isMergable(current))
                    {
                        last.merge(current);
                    }
                    else
                    {
                        diff.add(current);
                        last = current;
                    }
                }
            }
        }
        return diff;
    }


    /**
     * Returns the raw animation frame map key.
     * 
     * @param frame1
     *            The first frame
     * @param frame2
     *            The second frame
     * @return The map key
     */

    public static String getRawFrameKey(int frame1, int frame2)
    {
        int tmp;

        if (frame1 > frame2)
        {
            tmp = frame1;
            frame1 = frame2;
            frame2 = tmp;
        }
        return frame1 + "-" + frame2;
    }


    /**
     * Checks if the specified frame already exists and returns the frame index.
     * 0 is the base frame index while indices > 0 are the indexes in the frame
     * list -1. If the frame is unknown then -1 is returned so the caller can
     * add the new frame to the list of frames.
     * 
     * @param workingFrame
     *            The working frmae
     * @param baseFrame
     *            The base frame
     * @return The frame index or -1 if frame was not found
     */

    private int getFrameIndex(Pic workingFrame, Pic baseFrame)
    {
        if (workingFrame.equals(baseFrame))
        {
            return 0;
        }
        for (int i = 0; i < this.frames.size(); i++)
        {
            if (this.frames.get(i).equals(workingFrame))
            {
                return i + 1;
            }
        }
        return -1;
    }


    /**
     * Returns the frames.
     * 
     * @return The frames
     */

    public List<Pic> getFrames()
    {
        return this.frames;
    }


    /**
     * Returns the instructions.
     * 
     * @return The instructions
     */

    public List<PicsAnimationInstruction> getInstructions()
    {
        return this.instructions;
    }
}
