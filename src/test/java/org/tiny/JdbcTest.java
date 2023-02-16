/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tiny;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.tiny.datawrapper.Jdbc;

/**
 *
 * @author dtmoyaji
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes=MiniServer.class)
public class JdbcTest {

    @Autowired
    Jdbc jdbc;
    
    @Autowired
    MiniServer miniserver;

    @Value("${tiny.db.driver:org.h2.Driver}")
    String driver;
    
    @Value("${tiny.db.user:sa}")
    String user;
    
    @Value("${tiny.db.password:password}")
    String password;
    
    @Value("${tiny.db.port:9900}")
    String port;
    
    @Value("${tiny.db.url:./target/minisrv}")
    String url;
    
    @Before
    public void setUp(){
       
    }

    @After
    public void tearDown() {

    }

    /**
     * Test of getTcpServer method, of class Jdbc.
     */
    @Test
    public void testGetTcpServer() {
    }

    @Test
    public void testGetDriver() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "TEST getDriver");
        String drv = this.jdbc.getDriver();
        assertEquals(this.driver, drv);
    }

    @Test
    public void testGetJdbc() throws SQLException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "TEST getJdbc");
        Jdbc localJdbc = this.jdbc.getJdbc();
        assertNotNull(localJdbc);
        String url = localJdbc.getUrl();
        assertEquals(this.jdbc.getUrl(), url);
    }
    
    @Test
    public void createJdbcNonSpring(){
    }

}
