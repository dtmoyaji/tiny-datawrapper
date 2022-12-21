package org.tiny;


import java.sql.Date;
import java.sql.Time;
import org.tiny.datawrapper.annotations.ClearfyTable;
import org.tiny.datawrapper.ClearfyDatabaseException;
import org.tiny.datawrapper.Column;
import org.tiny.datawrapper.CurrentTimestamp;
import org.tiny.datawrapper.IncrementalKey;
import org.tiny.datawrapper.StampAtCreation;
import org.tiny.datawrapper.Table;
import org.springframework.stereotype.Component;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Takahiro MURAKAMI
 */
@ClearfyTable("TABLE_TESTER")
public class TableTester extends Table{
    
    public IncrementalKey Pkey;
    
    public StampAtCreation Stamp;
  
    public CurrentTimestamp Curtimestamp;
    
    public Column<Date> DateField;
    
    public Column<Time> TimeField;
    
    public Column<String> SzField;

    @Override
    public void defineColumns() throws ClearfyDatabaseException {
        this.DateField.setDefault("'1900-01-01'");
        
        this.SzField.setLength(Column.SIZE_1024);
    }
    
}
