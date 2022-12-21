/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tiny;

import java.sql.Date;
import org.tiny.datawrapper.TinyDatabaseException;
import org.tiny.datawrapper.Column;
import org.tiny.datawrapper.CurrentTimestamp;
import org.tiny.datawrapper.IncrementalKey;
import org.tiny.datawrapper.StampAtCreation;
import org.tiny.datawrapper.Table;
import org.tiny.datawrapper.annotations.TinyTable;

/**
 *
 * @author Takahiro MURAKAMI
 */
@TinyTable("TABLE_TESTER_NEXT")
public class TableTesterNext extends Table {

    public IncrementalKey NextKey;

    public StampAtCreation Stamp;

    public Column<Integer> Pkey;

    public CurrentTimestamp Curtimestamp;

    public Column<Date> DateField;

    @Override
    public void defineColumns() throws TinyDatabaseException {
        this.Pkey.addRelationWith(TableTester.class, Pkey);
    }

}
