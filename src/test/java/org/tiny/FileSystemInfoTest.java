package org.tiny;

import org.tiny.datawrapper.FileSystemInfo;
import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 *
 * @author dtmoyaji
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MiniServer.class)
public class FileSystemInfoTest {

    @Autowired
    FileSystemInfo fileSystemInfo;

    public FileSystemInfoTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getCurrentPath method, of class FileSystemInfo.
     * @throws java.io.IOException
     */
    @Test
    public void testGetCurrentPath() throws IOException {
        String path = this.fileSystemInfo.getCurrentPath();
        File f = new File("./");
        String cpath = f.getCanonicalPath();
        System.out.println(cpath);
        System.out.println(path);
        assertEquals(path, cpath);
    }

}
