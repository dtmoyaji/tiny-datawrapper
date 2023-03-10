/*
 * The MIT License
 *
 * Copyright 2017 tmworks.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.tiny.datawrapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.tiny.datawrapper.annotations.Comment;
import org.tiny.datawrapper.annotations.LogicalName;
import org.tiny.datawrapper.annotations.TinyTable;
import org.tiny.datawrapper.entity.ColumnInfo;
import org.tiny.datawrapper.entity.TableInfo;
import org.tiny.datawrapper.entity.Translate;

/**
 * γγΌγγ«
 *
 * @author Takahiro MURAKAMI
 */
@TinyTable
public abstract class Table extends ArrayList<Column> {

    public static final int JOIN_TYPE_INNER = 0;
    public static final int JOIN_TYPE_LEFT = 1;
    public static final int JOIN_TYPE_RIGHT = 2;

    /**
     * γγΌγγ«ε.
     */
    protected String name;

    /**
     * γγΌγγ«γ?ε₯ε.
     */
    protected String alias;

    /**
     * JDBCγ?δΎη΅¦γ―γ©γΉ.
     */
    @Autowired
    private Jdbc jdbc;

    /**
     * @return the jdbc
     */
    public Jdbc getJdbc() {
        return jdbc;
    }

    /**
     * @param jdbc the jdbc to set
     */
    public void setJdbc(Jdbc jdbc) {
        this.jdbc = jdbc;

        // γγΌγγ«εγ?θ£ζ­£
        this.name = NameDescriptor.toSqlName(
                this.getJavaName(),
                this.getServerType()
        );

        //γγ¨γ€γͺγ’γΉγ?θ£ζ­£
        this.alias = this.name;

        // γ«γ©γ εγ?θ£ζ­£
        this.forEach((col) -> {
            col.modifyName(this.getServerType());
        });
    }

    /**
     * getSelectionStringγ§δ½Ώη¨γγγει¨γγ©γ°.
     */
    protected boolean matched = false;

    protected int joinType = Table.JOIN_TYPE_INNER;

    /**
     * γγγγ°γ’γΌγ
     */
    protected boolean debugMode = false;

    /**
     * γ³γ³γΉγγ©γ―γΏ.
     * <p>
     * Tableγ?ζ΄Ύηγ―γ©γΉγ―γ€γ³γΉγΏγ³γΉεγ?ιγ«γγΉγ¦δ»₯δΈγ?ζηΆγγε?θ‘γγ.
     * </p>
     * <pre>
     * 1. γγΌγγ«εγ?ζ Όη΄(SQLη¨γ?εη§°γ§ζ Όη΄γγγ)
     * 2. γͺγγ¬γ―γ·γ§γ³γε©η¨γγ¦γγ«γ©γ γεζεγγ.
     * 3. γ«γ©γ γ?θΏ½ε ε?ηΎ©γη»ι²γγγ
     * </pre>
     */
    public Table() {
        this.initialize();
    }

    private void initialize() {

        this.name = NameDescriptor.toSqlName(this.getClass()
                .getSimpleName());
        this.alias = this.name;

        try {
            this.generateColumns();
            this.defineColumns();
        } catch (TinyDatabaseException ex) {
            Logger.getLogger(Table.class.getName())
                    .log(Level.SEVERE, null, ex);
        }

    }

    /**
     * γγΌγγ«εγεεΎγγ
     *
     * @return γγΌγγ«γ?η©ηεη§°γθΏγοΌ
     */
    public String getName() {
        return this.name;
    }

    /**
     * γγΌγγ«γ?Javaθ‘¨ηΎεη§°γεεΎγγ.
     *
     * @return
     */
    public String getJavaName() {
        return NameDescriptor.toJavaName(this.name);
    }

    /**
     * γ΅γΌγγΌγ?η¨?ι‘γεεΎγγ
     *
     * @return
     */
    public int getServerType() {
        if (this.getJdbc() != null) {
            return this.getJdbc().getServerType();
        } else {
            return Jdbc.SERVER_TYPE_UNKNOWN;
        }
    }

    /**
     * γγΌγγ«γ?ε₯εγεεΎγγ.
     *
     * @return
     */
    public String getAlias() {
        return this.alias;
    }

    /**
     * γγΌγγ«γ?ε₯εγη»ι²γγ.
     *
     * @param alias
     *
     * @return
     */
    public Table setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    /**
     * γγΌγγ«γ?ε₯εγει€γγ.
     *
     * @return
     */
    public Table removeAlias() {
        this.alias = "";
        return this;
    }

    /**
     * γγΌγγ«γε₯εγζγ€γγ©γγγεεΎγγ.
     *
     * @return true - ε₯εγγ false - ε₯εγͺγ
     */
    public boolean hasAlias() {
        return !this.alias.equals(this.name);
    }

    /**
     * ε₯εγδΌ΄γ£γηΆζγ§γγΌγγ«εγεεΎγγ.
     * <p>
     * <p>
     * γγ?γ‘γ½γγγ―γSQLγ?γγΌγγ«ε?ηΎ©γ§δ½Ώη¨γγγ.
     * </p>
     * <pre>
     * δΎ: γγΌγγ«ε MYTABLE γε₯εALIASγζγ€γ¨γγTableγ―γ©γΉγ«γγ£γ¦
     * θͺεηζγγγSELECTζγ―,
     *
     * select colA, colB from MYTABLE ALIAS
     *
     * γ?γγγ«γε₯εALIASγδΌ΄γ£γ¦ηζγγγγ
     * </pre>
     *
     * @return ε₯εγδΌ΄γ£γγγΌγγ«ε
     */
    public String getNameWithAlias() {
        String rvalue = this.getName();
        if (this.hasAlias()) {
            rvalue += " " + this.getAlias();
        }
        return rvalue;
    }

    public void setDebugMode(boolean mode) {
        this.debugMode = mode;
    }

    public boolean getDebugMode() {
        return this.debugMode;
    }

    /**
     * γγ£γΌγ«γε?£θ¨γγColumnγθͺεηγ«γ€γ³γΉγΏγ³γΉεγγ¦η»ι²γγ.
     * <p>
     * γγ?γ‘γ½γγγ―γγγΌγΏγγΌγΉγ©γγγΌγ?ζγιθ¦γͺγ‘γ½γγγ?οΌγ€γ
     * γγ?γ‘γ½γγγ«γγγεγγΌγγ«γ―γ©γΉγ?γ«γ©γ ε?ηΎ©γη°‘εγ«θ¨θΏ°γ§γγγγγ«γͺγ£γ¦γγγ
     * </p>
     * <p>
     * γγ?γ‘γ½γγγ?εδ½ζ¦θ¦γ―δ»₯δΈγ?ιγγ
     * </p>
     * <pre>
     * 1. γͺγγ¬γ―γ·γ§γ³γ§ε?ηΎ©γγγγγ£γΌγ«γγεΌγ³εΊγγ
     * 2. εΌγ³εΊγγγγγ£γΌγ«γγγColumnγΎγγ―Columγ?ε­γ―γ©γΉγγ©γγ
     *   γε€ε₯γγγγγγγ«θ©²ε½γγε ΄εγ―γ€γ³γΉγΏγ³γΉεγθ‘γγ
     * 3. γ«γ©γ γ?εη§°γεζε ±γζ Όη΄γγγ
     * </pre> γͺγγγ«γ©γ γ?γ΅γ€γΊγnullγ?ε―ε¦γθ¦ε?ε€γͺγ©γ―γεγ―γ©γΉγ?defineColumnsγ‘γ½γγγ§θ‘γγ
     *
     * @see Table#defineColumns
     */
    private void generateColumns() throws TinyDatabaseException {
        ((ArrayList) this).clear();

        String mypackage = Table.class.getPackage()
                .getName();

        Field[] fields = this.getClass()
                .getFields();
        for (Field field : fields) {
            String fieldTypeName = field.getType().getTypeName();

            boolean pass = false;
            if (fieldTypeName.equals(Column.class.getTypeName())) {
                pass = true;
            } else if (field.getType().getSuperclass() != null) {
                if (field.getType().getSuperclass().equals(Column.class)) {
                    pass = true;
                }
            }

            if (pass) {
                try {
                    String columnName = NameDescriptor.toSqlName(field.getName());

                    String ClsType = field.getGenericType().toString();
                    Column newColumn = null;

                    if (ClsType.equals(mypackage + ".Column<java.lang.Integer>")) {
                        newColumn = new Column<String>();
                        ClsType = Integer.class.getSimpleName();

                    } else if (ClsType.equals(mypackage + ".Column<java.lang.Short>")) {
                        newColumn = new Column<Short>();
                        ClsType = Short.class.getSimpleName();

                    } else if (ClsType.equals(mypackage + ".Column<java.sql.Timestamp>")) {
                        newColumn = new Column<Timestamp>();
                        ClsType = Timestamp.class.getSimpleName();

                    } else if (ClsType.equals(mypackage + ".Column<java.sql.Date>")) {
                        newColumn = new Column<Date>();
                        ClsType = Date.class.getSimpleName();

                    } else if (ClsType.equals(mypackage + ".Column<java.sql.Time>")) {
                        newColumn = new Column<Time>();
                        ClsType = Time.class.getSimpleName();

                    } else if (ClsType.equals(mypackage + ".Column<java.lang.String>")) {
                        newColumn = new Column<String>();
                        newColumn.setLength(Column.DEFAULT_VARCHAR_SIZE);
                        ClsType = String.class.getSimpleName();

                    } else if (ClsType.equals(mypackage + ".Column<char[]>")) {
                        newColumn = new Column<char[]>();
                        newColumn.setLength(Column.DEFAULT_VARCHAR_SIZE);
                        ClsType = char[].class.getSimpleName();

                    } else if (ClsType.equals(mypackage + ".Column<java.math.BigDecimal>")) {
                        newColumn = new Column<BigDecimal>();
                        newColumn.setLength(Column.DEFAULT_DECIMAL_INT_SIZE,
                                Column.DEFAULT_DECIMAL_FLOAT_SIZE);
                        ClsType = BigDecimal.class.getSimpleName();

                    } else if (ClsType.equals("class " + mypackage + ".IncrementalKey")) {
                        newColumn = new IncrementalKey();
                        ClsType = Integer.class.getSimpleName();

                    } else if (ClsType.equals("class " + mypackage + ".StampAtCreation")) {
                        newColumn = new StampAtCreation();
                        ClsType = Timestamp.class.getSimpleName();
                    } else if (ClsType.equals("class " + mypackage + ".CurrentTimestamp")) {
                        newColumn = new CurrentTimestamp();
                        ClsType = Timestamp.class.getSimpleName();

                    } else if (ClsType.equals("class " + mypackage + ".ShortFlagZero")) {
                        newColumn = new ShortFlagZero();
                        ClsType = Short.class.getSimpleName();

                    } else if (ClsType.equals("class " + mypackage + ".MD5Column")) {
                        newColumn = new MD5Column();
                        ClsType = String.class.getSimpleName();

                    } else {
                        throw new TinyDatabaseException(
                                "unsupported type " + ClsType);
                    }

                    //GUIγ?θ‘¨η€Ίη¨?ε₯γεζεγγγ
                    newColumn.setVisibleType(Column.VISIBLE_TYPE_TEXT);

                    newColumn.setTable(this);
                    this.add(newColumn);
                    field.set(this, newColumn);

                    Class ccls = newColumn.getClass();
                    Field clmField; //γ«γ©γ εγ?γγ£γΌγ«γ

                    while (!ccls.equals(Column.class)) {
                        ccls = ccls.getSuperclass();
                    }
                    clmField = ccls.getDeclaredField("typeName");
                    clmField.setAccessible(true);
                    clmField.set(newColumn, ClsType);

                    clmField = ccls.getDeclaredField("name");
                    clmField.setAccessible(true);
                    clmField.set(newColumn, columnName);

                } catch (IllegalArgumentException | IllegalAccessException
                        | NoSuchFieldException | SecurityException ex) {
                    Logger.getLogger(Table.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
        }

        //System.out.println(this.getTableConstructionInfo());
    }

    /**
     * γ«γ©γ γ?ε?ηΎ©γθ¨θΏ°γγ.
     * <p>
     * γγΌγγ«γ?εζεγ?ζη΅ζ?΅ιγ§γγ?γ‘γ½γγγεΌγ³εΊγγγγ εγ―γ©γΉγ§γͺγΌγγ©γ€γγγγγγ?γ‘γ½γγγ«γ―γγ«γ©γ γ?ε±ζ§ζε ±γ¨
     * δ»γ?γγΌγγ«γΈγ?γͺγ¬γΌγ·γ§γ³ε?ηΎ©γθ¨θΏ°γγγ
     * </p>
     * <p>
     * γγ?γ‘γ½γγγγͺγΌγγ©γ€γγ SQLγ? create tableζγ¨εζ§γ?ε?ηΎ©γ
     * θ¨θΏ°γγγ γγ§γγγΌγΏγγΌγΉδΈγ«γγΌγγ«γδ½ζγγγ¬γ³γΌγγη»ι²γγ γΎγει€γγ¨γε―θ½γ¨γͺγγ
     * </p>
     *
     * @throws org.tiny.datawrapper.TinyDatabaseException
     */
    public abstract void defineColumns() throws TinyDatabaseException;

    public void drop() {
        String cmd = "drop table if exists %s ";
        cmd = String.format(cmd, this.getName());
        if (this.getDebugMode()) {
            Logger.getLogger(this.getName()).info(cmd);
        }
        this.execute(cmd);

        try {
            TableInfo tableInfo = new TableInfo();
            //tableInfo.setJdbcSupplier(this.jdbc);

            ColumnInfo columnInfo = new ColumnInfo();
            //columnInfo.setJdbcSupplier(this.jdbc);
            try (ResultSet rs = tableInfo.select(tableInfo.TablePhisicalName.sameValueOf(this.getName()))) {
                if (rs.next()) {
                    columnInfo.delete(columnInfo.TableInfoId.sameValueOf(tableInfo.TableInfoId.of(rs)));
                    tableInfo.delete(tableInfo.TableInfoId.sameValueOf(tableInfo.TableInfoId.of(rs)));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * ζε?γγγ¬γ³γΌγγζΆγγ εη§ζ΄εγε£γγγγγ?γ§γδ½Ώγγͺγγ»γγγγγγ­γ
     *
     * @param conditions
     * @return
     */
    public boolean delete(Condition... conditions) {
        String cmd = "delete from %s where %s ";
        String where = "";
        for (Condition cnd : conditions) {
            if (!(cnd instanceof ConditionForOrder)) {
                where += ", " + cnd.getWhere();
            }
        }
        where = where.substring(1);
        cmd = String.format(cmd, this.getName(), where);

        if (this.getDebugMode()) {
            Logger.getLogger(this.getName()).info(cmd);
        }

        return this.execute(cmd);
    }

    public String getColumnSqlType(Column col) {
        String rvalue = col.getType();

        //γ«γ©γ γIntγ?γ¨γ
        if (rvalue.equals(Integer.class.getSimpleName())) {
            rvalue = "int";
        }

        //γ«γ©γ γSmallintγ?γ¨γ
        if (rvalue.equals(Short.class.getSimpleName())) {
            rvalue = "smallint";
        }

        //ζ₯δ»ζε»εγ?γ¨γ
        if (rvalue.equals(Timestamp.class.getSimpleName())) {
            rvalue = "timestamp";
        }

        //ζ₯δ»εγ?γ¨γ
        if (rvalue.equals(Date.class.getSimpleName())) {
            rvalue = "date";
        }

        //ζε»εγ?γ¨γ
        if (rvalue.equals(Time.class.getSimpleName())) {
            rvalue = "time";
        }

        //γ«γ©γ γStringγ?γ¨γ
        if (rvalue.equals(String.class.getSimpleName())) {
            rvalue = "varchar%s";
            String variableSize = col.getVariableSize();
            rvalue = String.format(rvalue, variableSize);
        }

        //γ«γ©γ γchar[]γ?γ¨γ
        if (rvalue.equals(char[].class.getSimpleName())) {
            rvalue = "char%s";
            String variableSize = col.getVariableSize();
            rvalue = String.format(rvalue, variableSize);
        }

        //γ«γ©γ γBigDecimalγ?γ¨γ
        if (rvalue.equals(BigDecimal.class.getSimpleName())) {
            rvalue = "decimal%s";
            String variableSize = col.getVariableSize();
            rvalue = String.format(rvalue, variableSize);
        }

        return rvalue;

    }

    /**
     * γγΌγγ«ηζη¨SQLγ?εεΎ
     *
     * @return createζ
     */
    public String getCreateSentence() {
        String rvalue = "create table if not exists %s (\n %s\n)\n";

        String fieldDef = "";
        String pkeydef = ""; //primary key

        Field[] fields = this.getClass()
                .getFields();
        for (Field field : fields) {
            boolean usefield = false;
            if (field.getType()
                    .getTypeName()
                    .equals(Column.class.getTypeName())) {
                usefield = true;
            } else if (field.getType()
                    .getSuperclass() != null) {
                if (field.getType()
                        .getSuperclass()
                        .getTypeName()
                        .equals(Column.class.getTypeName())) {
                    usefield = true;
                }
            }

            if (usefield) {
                try {
                    Column col = (Column) field.get(this);
                    if (col.isPrimaryKey()) {
                        pkeydef += "," + col.getName();
                    }

                    fieldDef += ",\n" + col.getColumnDef();
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(Table.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
        }

        if (pkeydef.length() > 0) {
            pkeydef = pkeydef.substring(1);
            String fmt = "\n, primary key (%s) ";
            fmt = String.format(fmt, pkeydef);
            fieldDef += fmt;
        }

        fieldDef = fieldDef.substring(1);
        fieldDef = fieldDef.trim();

        rvalue = String.format(rvalue, this.getName(), fieldDef);

        return rvalue;
    }

    /**
     * γ¬γ³γΌγγselectγγιγ«γγΉγ¦γ?γ«γ©γ γεεΎγγγγθ¨­ε?γε€ζ΄γγ.
     * <p>
     * <p>
     * γͺγγεγγΌγγ«ζ΄Ύηγ―γ©γΉγ―γγ€γ³γΉγΏγ³γΉεγγιγ« γγΉγ¦γ?γ«γ©γ γselectε―Ύθ±‘γ¨γγ¦γγγ?γ§γγγ?γ‘γ½γγγεΌγ³εΊγγγ¨γ―
     * γγΎγγͺγγ¨ζγγγγ
     * </p>
     */
    public void selectAllColumn() {
        this.forEach((col) -> {
            col.setSelectable(true);
        });
    }

    /**
     * ε¨γ¦γ?γ«γ©γ γγγΌγΈε―Ύθ±‘γ«γγ.
     * <p>
     * γͺγγεγγΌγγ«ζ΄Ύηγ―γ©γΉγ―γγ€γ³γΉγΏγ³γΉεγγιγ« γγΉγ¦γ?γ«γ©γ γγγΌγΈε―Ύθ±‘γ¨γγ¦γγγ?γ§γγγ?γ‘γ½γγγεΌγ³εΊγγγ¨γ―
     * γγΎγγͺγγ¨ζγγγγ
     * </p>
     */
    public void setMargeTargetAll() {
        this.forEach((col) -> {
            col.setMargeTarget(true);
        });
    }

    /**
     * γ¬γ³γΌγγselectγγιγ«γγΉγ¦γ?γ«γ©γ γιΈζε―Ύθ±‘γγι€ε€γγγγθ¨­ε?γε€ζ΄γγ.
     * <p>
     * <p>
     * γγ?γ‘γ½γγγ―γγ¬γ³γΌγγselectγγιγ«γδΈι¨γ?γ«γ©γ γεεΎγγε ΄εγ«γ Column.setSelectableγ¨εζγ«ε©η¨γγγγ
     * </p>
     * <pre>
     * δΎοΌ γγΌγγ« MYTABLEγ?γ«γ©γ γγCOL1, COL2, COL3 ... COL100 γ¨γγγ
     * γγ?δΈ­γ§ COL1 γ¨ COL25γγ¬γ³γΌγζ½εΊγγε ΄εγ
     *
     * MyTable mytable = new MyTable();
     * mytable.setJdbcSupplier(jdbc); //supplierγ―IJdbcSupplierγγ€γ³γγͺγ‘γ³γγγγ―γ©γΉ
     *
     * mytable.unselectAllColumn();
     *
     * ResultSet rs = mytable.select(
     * mytable.Col1.setSelectable(true),
     * mytable.Col2.setSelectable(true)
     * );
     *
     * </pre>
     *
     * @see Column#setSelectable(boolean)
     */
    public void unselectAllColumn() {
        for (Column col : this) {
            col.setSelectable(false);
        }
    }

    /**
     * θͺεηζγγselectζγεεΎγγ.
     *
     * @param conditions ζ½εΊζ‘δ»Ά
     *
     * @return selectζ
     *
     * @throws TinyDatabaseException
     *
     * @see Condition
     * @see Column
     */
    public String getSelectSentence(Condition... conditions) throws
            TinyDatabaseException {

        SelectionDescriptor desc = new SelectionDescriptor();
        desc.setServerType(this.getServerType());

        String se = desc.getSelectSentence(this, conditions);

        if (this.getDebugMode()) {
            desc.mapOut();
            desc.joinOut();
        }

        return se;
    }

    /**
     * ζε?ζ‘δ»Άγ§εεΎγγγγ¬γ³γΌγγ?ζ°γεεΎγγ.
     *
     * @param conditions
     *
     * @return
     */
    public int getSelectCount(Condition... conditions) {
        int rvalue = -1;
        try {

            String cmd = this.getSelectSentence(conditions);
            cmd = "select count(*) rcount from (" + cmd + ") rcq";

            if (this.getDebugMode()) {
                Logger.getLogger(this.getName()).info("SQL \n" + cmd);
            }

            ResultSet rs = this.getJdbc().select(cmd);

            if (rs.next()) {
                rvalue = rs.getInt("rcount");
                rs.close();
            }
        } catch (TinyDatabaseException | SQLException ex) {
            Logger.getLogger(Table.class.getName())
                    .log(Level.SEVERE, null, ex);
        }

        return rvalue;
    }

    public ResultSet select(Condition... conditions) {
        ResultSet rvalue = null;
        try {

            String cmd = this.getSelectSentence(conditions);

            if (this.getDebugMode()) {
                Logger.getLogger(this.getName()).info(cmd);
            }

            rvalue = this.getJdbc().select(cmd);

        } catch (TinyDatabaseException ex) {
            Logger.getLogger(Table.class.getName())
                    .log(Level.SEVERE, null, ex);
        }

        return rvalue;
    }

    /**
     * η΄ζ₯SQLζ§ζζΈ‘γγ¦γγΌγΏγεεΎγγ.
     * <p>
     * γγ?γ©γ€γγ©γͺγδ½Ώγ£γ¦γγΌγγ«γε?θ£γγγ¨γDBγ΅γΌγδΈγ?γγΌγγ«γ¨ Tableζ΄Ύηγ―γ©γΉγ?γ½γΌγΉγ³γΌγγ§γ«γ©γ γ?εη§°γγγΌγΏεγͺγ©γ?ε?ηΎ©γγ
     * εζγγγγ¨γ«γͺγγ
     * </p>
     * <p>
     * δΈζΉγγγ?γ‘γ½γγγ―γ½γΌγΉγ³γΌγγ?γγΌγγ«ε?ηΎ©γ¨γ―η‘ι’δΏγ«ζε­εγ ζΈ‘γγ¦γγγγγγγ?γ‘γ½γγγη΄ζ₯ε€ι¨γ―γ©γΉγγε©η¨γγγ¨γ½γΌγΉγ³γΌγ
     * δΈγ?ε?ηΎ©ε€ζ΄γ«θΏ½ιγ§γγͺγγͺγγζ³ε?ε€γ?ε―δ½η¨γεΌγθ΅·γγε―θ½ζ§γγγγ
     * </p>
     * <p>
     * γγγγ£γ¦ιεΈΈγ«θ€ιγͺSQLζ§ζγ?δ½Ώη¨γιΏγγγγͺγγ¨γδ»₯ε€γ―δ½Ώη¨γγͺγγγ¨γ
     * </p>
     *
     * @param cmd selectζ
     *
     * @return ζ½εΊη΅ζ
     */
    public ResultSet select(String cmd) {
        ResultSet rs = this.getJdbc()
                .select(cmd);
        return rs;
    }

    /**
     * γγΌγγ«γδ½ζγγ
     */
    public void createTable() {
        if (this.getJdbc() != null) {
            if (!this.isExist()) {
                String cmd = this.getCreateSentence();

                if (this.getDebugMode()) {
                    Logger.getLogger(this.getName()).info(cmd);
                }

                Jdbc jdbc = this.getJdbc();
                jdbc.execute(cmd);

                //γγΌγγ«ζε ±γ?εζεγΎγ§ζε ±ζδΎγγ¦γγ£γ¨γγγγ?γ§εΆιγ€γγ
                if (!this.getClass().getName().equals(TableInfo.class.getName())
                        && !this.getClass().getName().equals(ColumnInfo.class.getName())) {
                    Logger.getLogger(this.getClass().getName())
                            .log(Level.INFO, this.getName() + " created.");
                }
            }
        }
    }

    /**
     * SQLγε?θ‘γγ.ηΉζ?γͺδΎγι€γγ¦δ½Ώη¨γγΉγγ§γ―γͺγγ
     *
     * @param cmd
     * @return
     */
    public boolean execute(String cmd) {
        boolean rvalue = false;
        Jdbc jdbc = this.getJdbc();
        rvalue = jdbc.execute(cmd);
        return rvalue;
    }

    /**
     * γ«γ©γ γ?εη§°ε€ζ΄.
     *
     * @param from
     * @param to
     */
    public void renameColumn(String from, String to) {

        try {
            ResultSet rs = this.getJdbc().getMetaData().
                    getColumns(null, null, this.getName(), from);
            if (rs.next()) {
                Column newColumn = this.get(to);
                String cmd = "alter table %s change %s %s";
                cmd = String.format(cmd, this.getName(), from,
                        newColumn.getColumnDef()
                );
                this.execute(cmd);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * γγΌγΏγγΌγΉγ«γγΌγγ«γγγγγ©γγγη’Ίθͺγγγ
     *
     * @return
     */
    public boolean isExist() {
        boolean rvalue = false;

        if (getJdbc() != null) {
            Jdbc jdbc = this.getJdbc();
            rvalue = jdbc.isExistTable(this.getName());
        }

        return rvalue;
    }

    /**
     * γγΌγΏγζ΄ζ°γγγθ©²ε½γγγ¬γ³γΌγγε­ε¨γγͺγε ΄εγ―θΏ½ε γγγ
     * <p>
     * <p>
     * DBγ΅γΌγγΌγ«γγ£γ¦γ―mergeγ«ε―ΎεΏγγ¦γγͺγγγ?γγγγ?γ§γ H2DBδ»₯ε€γ§δ½Ώη¨γγε ΄εγ―γδΊεγ«γ΅γΌγγΌγ?δ»ζ§γη’Ίθͺγγγγ¨γ
     * </p>
     *
     * PGSQLγ―getcount->insertγ§η΅εγγ
     *
     * @param columns
     *
     * @return
     */
    public boolean merge(Column... columns) {

        this.postConstruct();

        for (Column colm : columns) {
            Column c = this.get(colm.getName());
            c.setValue(colm.getValue());
        }

        if (this.getServerType() == Jdbc.SERVER_TYPE_PGSQL || this.getServerType() == Jdbc.SERVER_TYPE_H2DB) {
            boolean rvalue = false;

            // γ­γΌγγ£γΌγ«γγζ½εΊ
            ArrayList<Condition> pkeys = new ArrayList<>();
            for (Column col : this) {
                if (col.isPrimaryKey()) {
                    if (col.hasValue()) {
                        pkeys.add(new Condition(col, Condition.EQUALS, col.getValue()));
                    }
                }
            }

            try {
                if (!pkeys.isEmpty()) {
                    Condition[] pkeyarray = (Condition[]) pkeys.toArray(new Condition[]{});
                    if (this.getCount(pkeyarray) > 0) {
                        this.update(pkeyarray);
                    } else {
                        this.insert((Column[]) this.toArray());
                    }
                } else {
                    this.insert((Column[]) this.toArray());
                }
            } catch (TinyDatabaseException ex) {
                Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
            }

            return rvalue;
        } else {
            String cmd = "";

            cmd = MergeDescriptor.getMergeSentence(this.getJdbc().getServerType(),
                    name,
                    (Column[]) this.toArray()
            );

            if (this.getDebugMode()) {
                System.out.println(cmd);
            }
            return this.execute(cmd);
        }
    }

    /**
     * γ¬γ³γΌγγθΏ½ε γγγη’Ίε?γ«γ¬γ³γΌγγε­ε¨γγͺγγ¨γγ?γΏδ½Ώη¨ε―γ
     *
     * @param columns
     *
     * @return
     */
    public boolean insert(Column... columns) {

        String fields = "";
        String values = "";

        for (Column column : columns) {
            boolean ins = true;
            if (column.isPrimaryKey() && column.getValue() == null) {
                ins = false;
            } else if (column.isPrimaryKey() && column.getValue().toString().equals("")) {
                ins = false;
            } else if (!column.isNullable() && column.getValue() == null) {
                ins = false;
            }
            if (ins) {
                fields += "," + column.getName();
                if (column.getValue() == null) {
                    values += ", null";
                } else {
                    if (column.getType().equals(char[].class.getSimpleName())) {
                        char[] valueBuf = (char[]) column.getValue();
                        values += ",'" + String.valueOf(valueBuf) + "'";
                    } else {
                        values += ",'" + column.getValue()
                                .toString() + "'";
                    }
                }
            }
        }

        fields = fields.substring(1);
        values = values.substring(1);

        String cmd = "insert into %s (%s) values (%s)";
        cmd = String.format(cmd, this.getName(), fields, values);

        if (this.getDebugMode()) {
            Logger.getLogger(this.getName()).info("SQL INSERT --- \n" + cmd);
        }

        return this.execute(cmd);
    }

    /**
     * γ«γ©γ γ«η»ι²γγγε€γ§γγγΌγΏγγγΌγΈγγγ
     *
     * @return
     */
    public boolean merge() throws NullPointerException {

        Column[] columns = new Column[this.size()];

        for (int i = 0; i < this.size(); i++) {
            columns[i] = this.get(i);
        }

        return this.merge(columns);

    }

    /**
     * SQLζγγ³γ³γ½γΌγ«γ«εΊεγγ. δΈ»γ«γγγγ°η¨
     *
     * @param sql
     */
    public void monitorSqlCommand(String sql) {
        System.out.println(sql);
    }

    /**
     * γ«γ©γ γ«η»ι²γγγε€γ§γγγΌγΏγγγΌγΈγγγ
     *
     * @return true - ζε, false - ε€±ζ
     */
    public boolean insert() throws NullPointerException {

        String fields = "";
        String values = "";

        for (Column column : this) {
            if (column.hasValue()) {
                fields += "," + column.getName();
                values += ",'" + column.getValue()
                        .toString() + "'";
            }
        }

        fields = fields.substring(1)
                .trim();
        values = values.substring(1)
                .trim();

        if (fields.length() < 1) {
            throw new NullPointerException(
                    "At table [" + this.getName() + "], columns have not values on merge datas.");
        }

        String cmd = "insert into %s (%s) values (%s)";
        cmd = String.format(cmd, this.getName(), fields, values);
        return this.getJdbc().execute(cmd);

    }

    /**
     * γγΌγγ«γγ΅γΌγγ«ε­ε¨γγγγ©γγη’Ίθͺγγγͺγγγ°δ½ζγγ.
     * <p>
     * γΎγγγγΌγγ«γγγ§γ«ε­ε¨γγγ¨γγ―γγ―γ©γΉδΈγ?γ«γ©γ ε?ηΎ©γ¨γ΅γΌγδΈγ?ε?ηΎ©γζ―θΌγ γ΅γΌγγΌδΈγ?ε?ηΎ©γζ΄ζ°γγγ
     * </p>
     * <p>
     * <p>
     * γγ γγε?ε¨ζ§γθζ?γγ¦γδ»₯δΈγ?ζ΄ζ°γ?γΏγθ‘γγ
     * </p>
     * <pre>
     * γ»γ«γ©γ γ?θΏ½ε γ―θ‘γγγει€γ―γγͺγγ
     * γ»εε€ζγ―γ΅γ€γΊγ?ζ‘ε€§γ?γΏοΌvarcharγ?γ΅γ€γΊγ?γΏγshort to int to longγ―ζͺε?θ£)
     * β»εζε€γ―ε€ζ΄γθ‘γγ
     * β»Nullγ―δΈθ¨±ε―βθ¨±ε―γ―θ‘γγ
     * β»γγ©γ€γγͺγ­γΌγ?θΏ½ε γει€γ―θ‘γγ
     * (β»γ―ε°ζ₯ε?θ£δΊε?)
     * </pre>
     * <p>
     * δΈθ¨δ»₯ε€γ―η‘θ¦γγγ
     * </p>
     *
     * @param supplier
     *
     * @see Table#checkColumnAndUpdate(java.sql.ResultSet)
     */
    public void alterOrCreateTable(Jdbc supplier) {
        this.setJdbc(supplier);

        Jdbc jdbc = this.getJdbc();
        if (!this.isExist()) {
            this.createTable();
        } else {
            try {
                //γ΅γΌγγΌγ«γγγγγ―γ©γΉγ«η‘γ
                String tblName = this.getName().replaceAll("\"", ""); // pgsqlε―ΎεΏ
                ResultSet columns = jdbc
                        .getConnection()
                        .getMetaData()
                        .getColumns(null, null, tblName, "%");
                while (columns.next()) {
                    String colName = columns.getString("COLUMN_NAME");
                    if (this.isMyColumn(colName.toUpperCase())) {
                        this.checkColumnAndUpdate(columns);
                    }
                }
                //γ―γ©γΉγ«γγγγ΅γΌγγΌγ«η‘γ
                for (Column col : this) {
                    if (this.isAppendedColumn(col.getName())) {
                        this.alterTableAddColumn(col);
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(Table.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
        jdbc.registTableEntryCache(this.getName());
        if (!(this instanceof TableInfo)
                && !(this instanceof ColumnInfo)) {
            this.recordInfo(supplier);
        }

    }

    @PostConstruct
    public void postConstruct() {
        if (this.jdbc != null) {
            this.name = NameDescriptor.toSqlName(
                    this.getClass().getSimpleName(),
                    this.jdbc.getServerType()
            );
            this.forEach((column) -> {
                column.modifyName(this.jdbc.getServerType());
            });
        }

    }

    public void alterOrCreateTable() {
        if (this.getJdbc() != null) {
            this.alterOrCreateTable(this.getJdbc());
        }
    }

    /**
     * TableInfoγ¨ColumnInfoγ«ζε ±γθ¨ι²γγ.
     *
     * @param supplier
     */
    public void recordInfo(Jdbc supplier) {

        //γγΌγγ«ζε ±γγΌγγ«γ?εζε.getConectionγ§δ½γγγγ?γ§γalterγγͺγγ
        TableInfo tableInfo = new TableInfo();
        tableInfo.setJdbc(supplier);

        //γ«γ©γ ζε ±γγΌγγ«γ?εζε
        ColumnInfo columnInfo = new ColumnInfo();
        columnInfo.setJdbc(supplier);

        //γγΌγγ«ζε ±γ?θ¨ι².
        String tablePhisicalName = this.getName();
        String tableLogicalName = this.getLogicalName();
        String tableClassName = this.getClass()
                .getCanonicalName();

        ResultSet rs = tableInfo.select(
                tableInfo.TableInfoId.setSelectable(true),
                tableInfo.TablePhisicalName.sameValueOf(tablePhisicalName)
        );
        int infId = -1;
        try {
            if (rs.next()) {
                infId = tableInfo.TableInfoId.of(rs);
                rs.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }

        tableInfo.clearValues();

        if (infId > 0) {
            tableInfo.TableInfoId.setValue(infId);
        }

        tableInfo.TableClassName.setValue(tableClassName);
        tableInfo.TablePhisicalName.setValue(tablePhisicalName);
        tableInfo.TableLogicalName.setValue(tableLogicalName);

        tableInfo.merge();

        //γ«γ©γ ζε ±γ?θ¨ι².
        tableInfo.unselectAllColumn();
        tableInfo.TableInfoId.setSelectable(true);
        rs = tableInfo.select(
                tableInfo.TableClassName.sameValueOf(tableClassName)
        );

        try {
            if (rs.next()) {
                int tableInfoId = tableInfo.TableInfoId.of(rs);

                for (int i = 0; i < this.size(); i++) {

                    Column column = this.get(i);
                    String columnPhisicalName = column.getName();
                    String columnLogicalName = this.getColumnLogicalName(column);
                    String columnClassName = tableClassName
                            + "." + column.getJavaName();

                    columnInfo.clearValues();
                    columnInfo.ColumnClassName.setValue(columnClassName);
                    columnInfo.ColumnPhisicalName.setValue(columnPhisicalName);
                    columnInfo.ColumnLogicalName.setValue(columnLogicalName);
                    columnInfo.TableInfoId.setValue(tableInfoId);
                    columnInfo.Ordinal.setValue(i + 1);
                    columnInfo.ColumnType.setValue(column.getType());
                    columnInfo.ColumnLength.setValue(column.getVariableSize());
                    columnInfo.AllowNullable
                            .setValue(column.isNullable() ? (short) 1 : (short) 0);
                    columnInfo.Description.setValue(this
                            .getColumnComment(column));
                    columnInfo.merge();

                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName())
                    .log(Level.SEVERE, null, ex);
        }

    }

    /**
     * γγΌγγ«γ?θ«ηεη§°γεεΎγγ.
     *
     * @return
     */
    public String getLogicalName() {
        Annotation anno = this.getClass()
                .getAnnotation(LogicalName.class);
        String rvalue = TableInfo.MESSAGE_LOGICALNAME_LOST;

        if (anno != null) {
            rvalue = this.getClass()
                    .getAnnotation(LogicalName.class)
                    .value();
        }
        return rvalue;
    }

    /**
     * γ«γ©γ γ?θ«ηεγεεΎγγ.
     *
     * @param col
     *
     * @return
     */
    public String getColumnLogicalName(Column col) {
        String rvalue = null;
        try {
            Field f = this.getClass()
                    .getField(col.getJavaName());
            LogicalName lname = f.getAnnotation(LogicalName.class);
            rvalue = (lname == null) ? TableInfo.MESSAGE_LOGICALNAME_LOST : lname.
                    value();
        } catch (NoSuchFieldException | SecurityException ex) {
            Logger.getLogger(Table.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        return rvalue;
    }

    public String getColumnComment(Column column) {
        String rvalue = null;
        try {
            Field f = this.getClass()
                    .getField(column.getJavaName());
            Comment comment = f.getAnnotation(Comment.class);
            rvalue = (comment == null) ? null : comment.value();
        } catch (NoSuchFieldException | SecurityException ex) {
            Logger.getLogger(Table.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        return rvalue;
    }

    /**
     * γγΌγγ«γε?ηΎ©γ«εΊγ₯γγ¦θΏ½ε γγγ
     *
     * @param column
     */
    private void alterTableAddColumn(Column column) {
        String colType = this.getColumnSqlType(column);

        String addline = "alter table if exists %s add column if not exists %s %s %s %s";

        switch (this.getServerType()) {
            case Jdbc.SERVER_TYPE_MYSQL:
                addline = "alter table %s add %s %s %s %s";
                break;
            case Jdbc.SERVER_TYPE_PGSQL:
                addline = "alter table %s add column %s %s %s %s";
                break;
        }

        String nullable = "";
        if (!column.isNullable()) {
            nullable = "not null";
        }

        String def = "";
        if (column.getDefault()
                .length() > 0) {
            def = "default " + column.getDefault();
        }

        if (column.isAutoIncrement()) {
            def = "AUTO_INCREMENT";
        }

        addline = String.format(addline, this.getName(), column.getName(),
                colType, nullable, def);

        switch (this.getServerType()) {
            case Jdbc.SERVER_TYPE_PGSQL:
                if (column.isAutoIncrement()) {
                    addline = "alter table %s add column %s serial not null ";
                    addline = String.format(addline, this.getName(), column.getName());
                }
                break;

        }

        Jdbc jdbc = this.getJdbc();
        jdbc.execute(addline);
        Logger.getLogger(this.getClass().getName()).info("Column added.");
    }

    /**
     *
     * @param columnInfo
     */
    private void checkColumnAndUpdate(ResultSet columnInfo) {
        try {
            String colname = columnInfo.getString("COLUMN_NAME");
            colname = NameDescriptor.toJavaName(colname);
            colname = NameDescriptor.toSqlName(colname, this.getServerType());
            Column col = this.get(colname);
            String type = columnInfo.getString("TYPE_NAME");
            int size = columnInfo.getInt("COLUMN_SIZE");
            switch (type) {
                case "VARCHAR":
                case "CHAR":
                    if (col.getLength() > size) {
                        String fmt = "ALTER TABLE %s MODIFY COLUMN %s %s(%d) ";
                        fmt = String.format(
                                fmt,
                                this.getName(),
                                col.getName(),
                                type,
                                col.getLength());
                        Jdbc jdbc = this.getJdbc();
                        jdbc.execute(fmt);
                        Logger.getLogger(this.getClass().getName()).info("Column Modified.");
                    }

                    break;

            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    /**
     * γγΌγγ«γ?ε?ηΎ©γHtmlε½’εΌγ§θΏγγ
     *
     * @return
     */
    public String getTableConstructionInfo() {
        String rvalue = "<div>\n%s\n<div>\n<table>\n%s\n</table>\n</div>\n</div>";

        String columns = "";
        for (Column col : this) {
            String logicalName = col.getName();
            try {
                LogicalName a = this.getClass()
                        .getField(col.getJavaName())
                        .getDeclaredAnnotation(LogicalName.class);
                if (a != null) {
                    logicalName = a.value();
                }
            } catch (NoSuchFieldException | SecurityException ex) {
                Logger.getLogger(Table.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
            String column = "<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>\n";
            column = String.format(column,
                    logicalName,
                    col.getName(), col.getType(), col
                    .isNullable());
            columns += column;
        }

        rvalue = String.format(rvalue, this.getName(), columns);

        return rvalue;
    }

    /**
     * δΊεγ«Column.setValueγ§ε€γγ»γγγγ¦γγγζ΄ζ°ζ‘δ»Άγ conditionsγ§ζε?γγγγ¨γ§ε€γζΈγζγγγ
     *
     * @param conditions ζ΄ζ°ζ‘δ»Ά
     *
     * @return ζεγγγ¨true;
     * @throws org.tiny.datawrapper.TinyDatabaseException
     */
    public boolean update(Condition... conditions) throws TinyDatabaseException {

        this.postConstruct();

        String cmd = "update %s set %s where %s";
        String tableName = this.getName();
        String fields = "";
        String wheres = "";

        for (Column column : this) {
            if (column.hasValue() && column.isMargeTarget()) {

                String field = "%s = '%s'";

                String fieldName = column.getFullName();
                if (this.getServerType() == Jdbc.SERVER_TYPE_PGSQL) {
                    fieldName = this.getName();
                }

                if (column.getType().equals(char[].class.getTypeName())) {
                    char[] fieldvalue = (char[]) column.getValue();
                    field = String.format(field, fieldName, String.valueOf(fieldvalue));
                } else {
                    if (fieldName.equals(column.getTable().getName()) && this.getServerType() == Jdbc.SERVER_TYPE_PGSQL) {
                        field = String.format(field, column.getName(), column.getValue());
                    } else {
                        field = String.format(field, column.getFullName(), column.getValue());
                    }
                }

                fields += ", " + field;
            }
        }
        if (fields.length() > 0) {
            fields = fields.substring(1)
                    .trim();
        } else {
            throw new TinyDatabaseException(
                    "All Columns are not selectable on update.");
        }

        for (Condition condition : conditions) {
            String where = condition.getWhere();
            wheres += " and " + where;
        }
        if (wheres.length() > 4) {
            wheres = wheres.substring(4)
                    .trim();
        }

        cmd = String.format(cmd, tableName, fields, wheres);

        if (this.getDebugMode()) {
            Logger.getLogger(this.getName()).info(cmd);
        }

        //System.out.println(cmd);
        return this.getJdbc().execute(cmd);
    }

    /**
     * γ«γ©γ γ«ζ Όη΄γγγ¦γγε€γγ―γͺγ’γγ.
     * <p>
     * θΏ½ε γζ΄ζ°γει€γ?εΌγ³εΊγζδ½γγγεγ«γγγ?γ‘γ½γγγεΌγ³εΊγγ¦γ δΊεγ«δ½Ώη¨γγε€γγγ£γγδ½Ώγγͺγγγγ«γγγγ¨γζ¨ε₯¨γγ¦γγγ
     * </p>
     */
    public void clearValues() {
        this.forEach((column) -> {
            column.clearValue();
        });
    }

    /**
     * ζε?ζ‘δ»Άγ§εεΎγγγγ¬γ³γΌγγ?ζ°γεεΎγγ.
     * <p>
     * javaγ?ResultSetγ«γ―γγγΌγΏζ½εΊζγ?δ»Άζ°γεεΎγγγ‘γ½γγγγͺγγγγ δΊεγ«δ»Άζ°γζζ‘γγ¦γγγγγ¨γγ«γγ?γ‘γ½γγγη¨γγγ
     * </p>
     * <p>
     * γͺγγδ»Άζ°εεΎε―Ύθ±‘γ?Selectζγγγ?γΎγΎSQLζγ?γ΅γγ―γ¨γͺγΌγ¨γγ¦η΅γΏθΎΌγγ§ γγγ γγ§γγγ
     * γγ?γ‘γ½γγγ¨selectγ‘γ½γγγδΈ‘ζΉε©η¨γγγ¨γγ―γγ΅γΌγγΌγ¨γγεγγ2ε
     * γγγγ¨γ«γͺγγ?γ§γγͺγγΉγδ»Άζ°δΎε­γγͺγγ§ζΈγγγγ«ε?θ£γγγ»γγγθ‘ε γ?γγγγ­γ°γ©γ γ¨γγγγ
     * </p>
     *
     * @param conditions ζ½εΊζ‘δ»Ά
     *
     * @return γ¬γ³γΌγζ°
     */
    public int getCount(Condition... conditions) throws TinyDatabaseException {
        String cmdsrc = this.getSelectSentence(conditions);
        String cmd = "select count(*) reccount from (%s) countsql";
        cmd = String.format(cmd, cmdsrc);

        if (this.getDebugMode()) {
            Logger.getLogger(this.getClass().getCanonicalName()).info(cmd);
        }

        ResultSet rs = this.getJdbc()
                .select(cmd);
        int rvalue = -1;
        try {
            if (rs.next()) {
                rvalue = rs.getInt("reccount");
            }
            rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        return rvalue;
    }

    /**
     * ζε?γγγ«γ©γ γγγγΌγγ«γ«ζε±γγγγ©γγγεεΎγγγ
     *
     * @param column γ«γ©γ 
     *
     * @return true:ζε±γγ false:ζε±γγͺγ
     */
    public boolean isMyColumn(Column column) {
        boolean rvalue = false;
        for (Column col : this) {
            if (col.equals(column)) {
                rvalue = true;
                break;
            }
        }
        return rvalue;
    }

    /**
     * ζε?γγγ«γ©γ εγ«θ©²ε½γγγ«γ©γ γγγγΌγγ«γ«ζε±γγγ γ©γγγεεΎγγγ
     *
     * @param columnName
     *
     * @return
     */
    public boolean isMyColumn(String columnName) {
        columnName = columnName.toUpperCase();
        boolean rvalue = false;
        for (Column col : this) {
            if (col.getName().equals(columnName)
                    || col.getName().equals("\"" + columnName + "\"")
                    || col.getName().equals("`" + columnName + "`")) {
                rvalue = true;
                break;
            }
        }
        return rvalue;
    }

    /**
     * γγΌγΏγγΌγΉδΈγ«η‘γγεΎγ§θΏ½ε γγγγ«γ©γ γγ©γγγθͺΏγΉγ.
     *
     * @param columnName
     *
     * @return
     */
    private boolean isAppendedColumn(String columnName) {

        String tableName = this.getName();

        Jdbc jdbc = this.getJdbc();
        boolean rvalue = false;
        boolean cached = jdbc.hasColumnCacheEntry(tableName, columnName);
        if (!cached) {
            try {
                ResultSet columns = jdbc
                        .getConnection()
                        .getMetaData()
                        .getColumns(null, null, tableName, columnName);
                if (!columns.next()) {
                    rvalue = true;
                } else {
                    this.getJdbc().registColumnEntryCache(tableName, columnName);
                }
                columns.close();
            } catch (SQLException ex) {
                Logger.getLogger(Table.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
        return rvalue;
    }

    /**
     * γ«γ©γ γεεΎγγγ
     *
     * @param columnName
     *
     * @return
     */
    public Column get(String columnName) {
        Column rvalue = null;
        for (Column col : this) {
            if (col.getName().equals(columnName)) {
                rvalue = col;
                break;
            }
        }
        if (rvalue == null) {
            String ModName = NameDescriptor.toJavaName(columnName);
            ModName = NameDescriptor.toSqlName(ModName, this.getServerType());
            for (Column col : this) {
                if (col.getName().equals(ModName)) {
                    rvalue = col;
                    break;
                }
            }
        }

        return rvalue;
    }

    /**
     * εη§εγζγ€γ«γ©γ γεεΎγγγ
     *
     * @return
     */
    public ArrayList<Column> getRelationColumns() {

        ArrayList<Column> rvalue = new ArrayList<>();

        for (Column cols : this) {
            if (cols.hasRelation()) {
                rvalue.add(cols);
            }
        }

        return rvalue;
    }

    /**
     * γγΌγΏγselectγγιγ«γεη§η¨γ?γγΌγγ«γ¨γγ¦γ΅γγΎγγγγ? γ³γ³γγ£γ·γ§γ³γηζγγ.
     * <p>
     * γγΌγγ«γ?εη§ι’δΏγγ
     * </p>
     * <pre>
     * TableA.ColumnA β TableB.ColumnA
     * TableC.ColumnB β TableB.ColumnB
     * </pre> γ?γ¨γγ«γ3γ€γ?γγΌγγ«γη΅εγγSQLγ―
     * <pre>
     * Select TableA.ColumnA1, TableC.ColumnC2 from TableA
     * inner join TableB on
     * TableB.ColumnA = TableA.ColumnA
     * inner join TableC on
     * TableC.ColumnB = TableB.ColumnB
     * </pre>
     * <p>
     * γ¨γͺγγγγγγ§γγΌγγ«Bγ―εη΄γ«η΅εγ?ζ©ζΈ‘γγγγγ
     * </p>
     * <p>
     * γγ?γγγͺγζ©ζΈ‘γγγγγγΌγγ«γselectγ§ζε?γγιγ? γ³γ³γγ£γ·γ§γ³γηζγγγγ¨γη?ηγ¨γγ¦γγγ
     * </p>
     *
     * @return
     */
    public Condition useJoinPath() {
        Condition rvalue = new Condition(this.get(0), Condition.RELATION_PATH);
        return rvalue;
    }

    /**
     * γ’γγγΌγ·γ§γ³γ§ε?ηΎ©γγθ«ηεγγ«γ©γ γ?ε₯εγ«δ½Ώη¨γγ. ηΏ»θ¨³γγΌγΏγγγγ°γε₯εγ―ηΏ»θ¨³γγΌγΏγ§δΈζΈγγγ.
     */
    public void setAliasFromLogicalName(String Country) {

        if (this.getJdbc() == null) {
            TinyDatabaseException ex = new TinyDatabaseException("JDbcSupplier is null.");
            Logger.getLogger(Table.class.getName())
                    .log(Level.SEVERE, null, ex);
        }

        Translate translate = new Translate();
        //translate.setJdbcSupplier(jdbc);

        Class myclass = this.getClass();
        Field[] fz = myclass.getDeclaredFields();
        for (Field field : fz) {

            if (Column.class.isAssignableFrom(field.getType())) {

                LogicalName logicalName = field.getAnnotation(LogicalName.class);
                String fname = NameDescriptor.toSqlName(
                        field.getName(),
                        this.getJdbc().getServerType()
                );
                String aliasName = (logicalName == null) ? fname : logicalName
                        .value();

                translate.unselectAllColumn();
                ResultSet rs = translate.select(
                        translate.TranslateData.setSelectable(true),
                        translate.TranslateKey.sameValueOf(aliasName),
                        translate.Language.sameValueOf(Country)
                );
                boolean translated = false;
                try {
                    if (rs.next()) {
                        aliasName = translate.TranslateData.of(rs);
                        translated = true;
                    }
                } catch (NullPointerException ex) {
                } catch (SQLException ex) {
                    Logger.getLogger(Table.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
                if (!translated) {
                    translate.clearValues();
                    translate.merge(
                            translate.Language.setValue(Country),
                            translate.TranslateKey.setValue(fname),
                            translate.TranslateData.setValue(aliasName)
                    );
                }

                Column col = this.get(fname);
                col.setAlias(aliasName);
                //System.out.println(col.getFullName());
            }
        }
    }

    /**
     * @return the matched
     */
    public boolean isMatched() {
        return matched;
    }

    /**
     * @param matched the matched to set
     */
    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    /**
     * @return the joinType
     */
    public int getJoinType() {
        return joinType;
    }

    /**
     * @param joinType the joinType to set
     */
    public void setJoinType(int joinType) {
        this.joinType = joinType;
    }

    /**
     * γγΌγγ«γγγ©γ³γ±γΌγγγγ γγΌγγ«γε­ε¨γγͺγγ¨γγ―γζ°θ¦δ½ζγθ‘γγ
     */
    public void truncate() {
        if (this.isExist()) {
            String cmd = "truncate table %s";
            cmd = String.format(cmd, this.getName());
            this.getJdbc().execute(cmd);
            Logger.getLogger(this.getClass().getName()).info(this.getName() + " truncated.");
        } else {
            this.alterOrCreateTable(this.getJdbc());
        }
    }

    public void setColumnsVisibleType(int Type) {
        for (Column col : this) {
            col.setVisibleType(Type);
        }

    }

    public boolean isNameEquals(String name) {
        String myJavaName = this.getJavaName();
        name = NameDescriptor.getSplitedName(name);
        String cmpJavaName = NameDescriptor.toJavaName(name);
        return myJavaName.equals(cmpJavaName);
    }

    public Column[] toArray() {
        Column[] rvalue = new Column[this.size()];
        for (int i = 0; i < rvalue.length; i++) {
            rvalue[i] = this.get(i);
        }
        return rvalue;
    }

    public boolean contains(String tableName) {
        boolean rvalue = false;

        for (Column col : this) {
            if (col.getName().equals(tableName)) {
                rvalue = true;
                break;
            }
        }

        return rvalue;
    }

}
