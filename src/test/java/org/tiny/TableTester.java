package org.tiny;


import java.sql.Date;
import java.sql.Time;
import org.tiny.datawrapper.Column;
import org.tiny.datawrapper.CurrentTimestamp;
import org.tiny.datawrapper.IncrementalKey;
import org.tiny.datawrapper.StampAtCreation;
import org.tiny.datawrapper.Table;
import org.tiny.datawrapper.TinyDatabaseException;
import org.tiny.datawrapper.annotations.TinyTable;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author dtmoyaji
 */
@TinyTable("TABLE_TESTER")
public class TableTester extends Table{
    
    public IncrementalKey Pkey;
    
    public StampAtCreation Stamp;
  
    public CurrentTimestamp Curtimestamp;
    
    public Column<Date> DateField;
    
    public Column<Time> TimeField;
    
    public Column<String> SzField1;
    
    public Column<String> SzFiled2;

    @Override
    public void defineColumns() throws TinyDatabaseException {
        this.DateField.setDefault("'1900-01-01'");
        
        this.SzField1.setLength(Column.SIZE_1024);
        
        this.SzFiled2.setLength(Column.SIZE_256);
    }
    
}
