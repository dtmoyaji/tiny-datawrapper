/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tiny.h220;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.tiny.*;
import org.tiny.datawrapper.Jdbc;
import org.tiny.datawrapper.TinyDatabaseException;

/**
 * H2dbでマージの挙動が変わった事に対応したテスト。
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MiniServer.class)
public class MargeTest {

    @Autowired
    Jdbc jdbc;

    @Autowired
    @Qualifier("TABLE_TESTER")
    TableTester tt;

    @Before
    public void setUp() {
        tt.alterOrCreateTable();
    }

    @After
    public void tearDown() {

    }

    /**
     * Test of getTcpServer method, of class Jdbc.
     */
    @Test
    public void testMargeTest() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "TEST margeTest");

        tt.setDebugMode(true);
        
        tt.clearValues();
        tt.SzField1.setValue("A");
        tt.SzFiled2.setValue("B");
        tt.merge();

        try (ResultSet rs = tt.select(tt.SzField1.sameValueOf("A"))) {
            if (rs.next()) {
                tt.clearValues();
                tt.Pkey.setValue(tt.Pkey.of(rs));
                tt.SzFiled2.setValue("C");
                tt.update(
                        tt.Pkey.sameValueOf(tt.Pkey.of(rs))
                );
            }
            
            tt.SzFiled2.setValue("D");
            tt.merge();
        } catch (TinyDatabaseException | SQLException ex) {
            Logger.getLogger(MargeTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
