package org.tiny;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.tiny.datawrapper.Jdbc;
import org.tiny.datawrapper.TinyDatabaseException;

/**
 *
 * @author Takahiro MURAKAMI
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MiniServer.class)
public class TableGenerateTest {

    @Autowired
    @Qualifier("TABLE_TESTER")
    TableTester tableTester;

    @Autowired
    @Qualifier("TABLE_TESTER_NEXT")
    TableTesterNext tableTesterNext;

    @Autowired
    @Qualifier("Jdbc")
    private Jdbc jdbc;

    public TableGenerateTest() {
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

    @Test
    public void testNormal() {
        this.jdbc.setUrl("jdbc:h2");
        this.tableTester.setJdbc(jdbc);

        String planeName = this.tableTester.getName();
        System.out.println("\nTEST NORMAL");

        String fieldType = this.tableTester.Pkey.getType();
        assertEquals(fieldType, Integer.class.getSimpleName());

        fieldType = this.tableTester.TimeField.getType();
        assertEquals(fieldType, java.sql.Time.class.getSimpleName());
        System.out.println(fieldType);

        fieldType = this.tableTester.DateField.getType();
        assertEquals(fieldType, java.sql.Date.class.getSimpleName());

        fieldType = this.tableTester.Curtimestamp.getType();
        assertEquals(fieldType, java.sql.Timestamp.class.getSimpleName());

        assertTrue(this.tableTester.isNameEquals(planeName));

        String createSentence = this.tableTester.getCreateSentence();
        System.out.println(createSentence);

        try {
            String selectSentence = this.tableTester.getSelectSentence(this.tableTester.Pkey.asc());
            System.out.println(selectSentence);
        } catch (TinyDatabaseException ex) {
            Logger.getLogger(TableGenerateTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            String cmd = tableTester.getSelectSentence(
                    tableTesterNext.NextKey.sameValueOf("ACHO")
            );
            System.out.println(cmd);
        } catch (TinyDatabaseException ex) {
            Logger.getLogger(TableGenerateTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void testMysql() {
        System.out.println("\nTEST MYSQL");

        // this.tableTester = new TableTester();
        this.jdbc.setUrl("jdbc:mysql");
        this.tableTester.setJdbc(jdbc);

        String planeName = this.tableTester.getName();

        String fieldType = this.tableTester.Pkey.getType();
        assertEquals(fieldType, Integer.class.getSimpleName());

        fieldType = this.tableTester.TimeField.getType();
        assertEquals(fieldType, java.sql.Time.class.getSimpleName());
        System.out.println(fieldType);

        fieldType = this.tableTester.DateField.getType();
        assertEquals(fieldType, java.sql.Date.class.getSimpleName());

        fieldType = this.tableTester.Curtimestamp.getType();
        assertEquals(fieldType, java.sql.Timestamp.class.getSimpleName());

        assertTrue(this.tableTester.isNameEquals(planeName));

        String createSentence = this.tableTester.getCreateSentence();
        System.out.println(createSentence);

        try {
            String selectSentence = this.tableTester.getSelectSentence(this.tableTester.Pkey.asc());
            System.out.println(selectSentence);
        } catch (TinyDatabaseException ex) {
            Logger.getLogger(TableGenerateTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Test
    public void testPgsql() {
        System.out.println("\nTEST PGSQL\n\n");

        this.jdbc.setUrl("jdbc:postgresql");
        this.tableTester.setJdbc(jdbc);

        String planeName = this.tableTester.getName();
        this.tableTester.setDebugMode(true);
        /*        this.tableTester.alterOrCreateTable(() -> {
            Jdbc jdbc = new Jdbc("jdbc:postgresql://vmpossrv:5432/datesv");
            jdbc.setAuthInfo("datesv", "datesv");
            return jdbc;
        });
         */
        String fieldType = this.tableTester.Pkey.getType();
        assertEquals(fieldType, Integer.class.getSimpleName());

        fieldType = this.tableTester.TimeField.getType();
        assertEquals(fieldType, java.sql.Time.class.getSimpleName());
        System.out.println(fieldType);

        fieldType = this.tableTester.DateField.getType();
        assertEquals(fieldType, java.sql.Date.class.getSimpleName());

        fieldType = this.tableTester.Curtimestamp.getType();
        assertEquals(fieldType, java.sql.Timestamp.class.getSimpleName());

        boolean result = this.tableTester.isNameEquals(planeName);
        assertTrue(result);

        String createSentence = this.tableTester.getCreateSentence();
        System.out.println(createSentence);

        /*
        try {
            this.tableTester.clearValues();
            this.tableTester.TimeField.setValue(Time.valueOf("10:00:00"));
            this.tableTester.merge();

            this.tableTester.clearValues();
            this.tableTester.DateField.setValue(Date.valueOf("2000-01-01"));
            this.tableTester.merge();

            this.tableTester.getCount(this.tableTester.Pkey.isNull());

            this.tableTester.getCount(this.tableTester.Pkey.isNotNull());

            this.tableTester.drop();

        } catch (TinyDatabaseException ex) {
            Logger.getLogger(TableGenerateTest.class.getName()).log(Level.SEVERE, null, ex);
        }
         */
    }
}
