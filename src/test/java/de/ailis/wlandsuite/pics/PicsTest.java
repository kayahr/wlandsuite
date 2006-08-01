/*
 * $Id$
 * Copyright (c) 2006 Klaus Reimer
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import junit.framework.Test;
import junit.framework.TestSuite;
import de.ailis.wlandsuite.pic.Pic;
import de.ailis.wlandsuite.test.WSTestCase;


/**
 * Tests the Pics class
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class PicsTest extends WSTestCase
{
    /**
     * Returns the test suite.
     * 
     * @return The test suite
     */

    public static Test suite()
    {
        return new TestSuite(PicsTest.class);
    }


    /**
     * Tests reading a PICS
     * 
     * @throws IOException
     */

    public void testRead() throws IOException
    {
        File file;
        Pics pics;
        PicsAnimation animation;
        PicsAnimationFrameSet frameSet;

        file = new File("src/test/resources/pics/test.pics");
        pics = Pics.read(new FileInputStream(file));        
        assertNotNull(pics);
        assertEquals(2, pics.getAnimations().size());
        animation = pics.getAnimations().get(0);
        assertEquals(new File("src/test/resources/pics/test/000/000.png"),
            animation.getBaseFrame());
        assertEquals(2, animation.getFrameSets().size());
        frameSet = animation.getFrameSets().get(0);
        assertEquals(1, frameSet.getFrames().size());
        assertEquals(new File("src/test/resources/pics/test/000/000/001.png"),
            frameSet.getFrames().get(0));
        frameSet = animation.getFrameSets().get(1);
        assertEquals(2, frameSet.getFrames().size());
        assertEquals(new File("src/test/resources/pics/test/000/001/001.png"),
            frameSet.getFrames().get(0));
        assertEquals(new File("src/test/resources/pics/test/000/001/002.png"),
            frameSet.getFrames().get(1));

        animation = pics.getAnimations().get(1);
        assertEquals(new File("src/test/resources/pics/test/001/000.png"),
            animation.getBaseFrame());
        assertEquals(1, animation.getFrameSets().size());
        frameSet = animation.getFrameSets().get(0);
        assertEquals(1, frameSet.getFrames().size());
        assertEquals(new File("src/test/resources/pics/test/001/000/001.png"),
            frameSet.getFrames().get(0));
    }


    /**
     * Tests writing a PICS.
     * 
     * @throws IOException
     */

    public void testWrite() throws IOException
    {
        Pics pics;
        List<PicsAnimation> animations;
        List<PicsAnimationFrameSet> frameSets;
        Pic baseFrame;
        List<Pic> frames;
        List<PicsAnimationInstruction> instructions;
        ByteArrayOutputStream stream;
        
        animations = new ArrayList<PicsAnimation>();
        
        baseFrame = new Pic(ImageIO.read(new File("src/test/resources/pics/test/000/000.png")));
        frameSets = new ArrayList<PicsAnimationFrameSet>();
        frames = new ArrayList<Pic>();
        frames.add(new Pic(ImageIO.read(new File("src/test/resources/pics/test/000/000/001.png"))));
        instructions = new ArrayList<PicsAnimationInstruction>();
        instructions.add(new PicsAnimationInstruction(10, 1));
        instructions.add(new PicsAnimationInstruction(10, 0));
        instructions.add(new PicsAnimationInstruction(1, 1));
        instructions.add(new PicsAnimationInstruction(1, 0));
        frameSets.add(new PicsAnimationFrameSet(frames, instructions));
        frames = new ArrayList<Pic>();
        frames.add(new Pic(ImageIO.read(new File("src/test/resources/pics/test/000/001/001.png"))));
        frames.add(new Pic(ImageIO.read(new File("src/test/resources/pics/test/000/001/002.png"))));
        instructions = new ArrayList<PicsAnimationInstruction>();
        instructions.add(new PicsAnimationInstruction(10, 1));
        instructions.add(new PicsAnimationInstruction(10, 2));
        instructions.add(new PicsAnimationInstruction(10, 0));
        frameSets.add(new PicsAnimationFrameSet(frames, instructions));
        animations.add(new PicsAnimation(baseFrame, frameSets));
        
        baseFrame = new Pic(ImageIO.read(new File("src/test/resources/pics/test/001/000.png")));
        frameSets = new ArrayList<PicsAnimationFrameSet>();
        frames = new ArrayList<Pic>();
        frames.add(new Pic(ImageIO.read(new File("src/test/resources/pics/test/001/000/001.png"))));
        instructions = new ArrayList<PicsAnimationInstruction>();
        instructions.add(new PicsAnimationInstruction(10, 1));
        instructions.add(new PicsAnimationInstruction(10, 0));
        frameSets.add(new PicsAnimationFrameSet(frames, instructions));
        animations.add(new PicsAnimation(baseFrame, frameSets));
        
        pics = new Pics(animations);
        stream = new ByteArrayOutputStream();
        pics.write(stream);

        assertEquals(new File("src/test/resources/pics/test.pics"), stream
            .toByteArray());
    }
}
