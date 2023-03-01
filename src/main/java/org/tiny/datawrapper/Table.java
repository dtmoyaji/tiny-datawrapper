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
 * テーブル
 *
 * @author Takahiro MURAKAMI
 */
@TinyTable
public abstract class Table extends ArrayList<Column> {

    public static final int JOIN_TYPE_INNER = 0;
    public static final int JOIN_TYPE_LEFT = 1;
    public static final int JOIN_TYPE_RIGHT = 2;

    /**
     * テーブル名.
     */
    protected String name;

    /**
     * テーブルの別名.
     */
    protected String alias;

    /**
     * JDBCの供給クラス.
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

        // テーブル名の補正
        this.name = NameDescriptor.toSqlName(
                this.getJavaName(),
                this.getServerType()
        );

        //　エイリアスの補正
        this.alias = this.name;

        // カラム名の補正
        this.forEach((col) -> {
            col.modifyName(this.getServerType());
        });
    }

    /**
     * getSelectionStringで使用される内部フラグ.
     */
    protected boolean matched = false;

    protected int joinType = Table.JOIN_TYPE_INNER;

    /**
     * デバッグモード
     */
    protected boolean debugMode = false;

    /**
     * コンストラクタ.
     * <p>
     * Tableの派生クラスはインスタンス化の際にすべて以下の手続きを実行する.
     * </p>
     * <pre>
     * 1. テーブル名の格納(SQL用の名称で格納する。)
     * 2. リフレクションを利用して、カラムを初期化する.
     * 3. カラムの追加定義を登録する。
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
     * テーブル名を取得する
     *
     * @return テーブルの物理名称を返す．
     */
    public String getName() {
        return this.name;
    }

    /**
     * テーブルのJava表現名称を取得する.
     *
     * @return
     */
    public String getJavaName() {
        return NameDescriptor.toJavaName(this.name);
    }

    /**
     * サーバーの種類を取得する
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
     * テーブルの別名を取得する.
     *
     * @return
     */
    public String getAlias() {
        return this.alias;
    }

    /**
     * テーブルの別名を登録する.
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
     * テーブルの別名を削除する.
     *
     * @return
     */
    public Table removeAlias() {
        this.alias = "";
        return this;
    }

    /**
     * テーブルが別名を持つかどうかを取得する.
     *
     * @return true - 別名あり false - 別名なし
     */
    public boolean hasAlias() {
        return !this.alias.equals(this.name);
    }

    /**
     * 別名を伴った状態でテーブル名を取得する.
     * <p>
     * <p>
     * このメソッドは、SQLのテーブル定義で使用される.
     * </p>
     * <pre>
     * 例: テーブル名 MYTABLE が別名ALIASを持つとき、Tableクラスによって
     * 自動生成されるSELECT文は,
     *
     * select colA, colB from MYTABLE ALIAS
     *
     * のように、別名ALIASを伴って生成される。
     * </pre>
     *
     * @return 別名を伴ったテーブル名
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
     * フィールド宣言したColumnを自動的にインスタンス化して登録する.
     * <p>
     * このメソッドは、データベースラッパーの最も重要なメソッドの１つ。
     * このメソッドにより、各テーブルクラスのカラム定義を簡単に記述できるようになっている。
     * </p>
     * <p>
     * このメソッドの動作概要は以下の通り。
     * </p>
     * <pre>
     * 1. リフレクションで定義されたフィールドを呼び出す。
     * 2. 呼び出されたフィールドが、ColumnまたはColumの子クラスかどうか
     *   を判別し、いずれかに該当する場合はインスタンス化を行う。
     * 3. カラムの名称、型情報を格納する。
     * </pre> なお、カラムのサイズやnullの可否、規定値などは、各クラスのdefineColumnsメソッドで行う。
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

                    //GUIの表示種別を初期化する。
                    newColumn.setVisibleType(Column.VISIBLE_TYPE_TEXT);

                    newColumn.setTable(this);
                    this.add(newColumn);
                    field.set(this, newColumn);

                    Class ccls = newColumn.getClass();
                    Field clmField; //カラム内のフィールド

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
     * カラムの定義を記述する.
     * <p>
     * テーブルの初期化の最終段階でこのメソッドが呼び出される。 各クラスでオーバライドされたこのメソッドには、カラムの属性情報と
     * 他のテーブルへのリレーション定義を記述する。
     * </p>
     * <p>
     * このメソッドをオーバライドし SQLの create table文と同様の定義を
     * 記述するだけで、データベース上にテーブルを作成し、レコードを登録し、 また削除ことが可能となる。
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
     * 指定したレコードを消す。 参照整合が壊れやすいので、使わないほうがいいよね。
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

        //カラムがIntのとき
        if (rvalue.equals(Integer.class.getSimpleName())) {
            rvalue = "int";
        }

        //カラムがSmallintのとき
        if (rvalue.equals(Short.class.getSimpleName())) {
            rvalue = "smallint";
        }

        //日付時刻型のとき
        if (rvalue.equals(Timestamp.class.getSimpleName())) {
            rvalue = "timestamp";
        }

        //日付型のとき
        if (rvalue.equals(Date.class.getSimpleName())) {
            rvalue = "date";
        }

        //時刻型のとき
        if (rvalue.equals(Time.class.getSimpleName())) {
            rvalue = "time";
        }

        //カラムがStringのとき
        if (rvalue.equals(String.class.getSimpleName())) {
            rvalue = "varchar%s";
            String variableSize = col.getVariableSize();
            rvalue = String.format(rvalue, variableSize);
        }

        //カラムがchar[]のとき
        if (rvalue.equals(char[].class.getSimpleName())) {
            rvalue = "char%s";
            String variableSize = col.getVariableSize();
            rvalue = String.format(rvalue, variableSize);
        }

        //カラムがBigDecimalのとき
        if (rvalue.equals(BigDecimal.class.getSimpleName())) {
            rvalue = "decimal%s";
            String variableSize = col.getVariableSize();
            rvalue = String.format(rvalue, variableSize);
        }

        return rvalue;

    }

    /**
     * テーブル生成用SQLの取得
     *
     * @return create文
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
     * レコードをselectする際にすべてのカラムを取得するよう設定を変更する.
     * <p>
     * <p>
     * なお、各テーブル派生クラスは、インスタンス化する際に すべてのカラムをselect対象としているので、このメソッドを呼び出すことは
     * あまりないと思われる。
     * </p>
     */
    public void selectAllColumn() {
        this.forEach((col) -> {
            col.setSelectable(true);
        });
    }

    /**
     * 全てのカラムをマージ対象にする.
     * <p>
     * なお、各テーブル派生クラスは、インスタンス化する際に すべてのカラムをマージ対象としているので、このメソッドを呼び出すことは
     * あまりないと思われる。
     * </p>
     */
    public void setMargeTargetAll() {
        this.forEach((col) -> {
            col.setMargeTarget(true);
        });
    }

    /**
     * レコードをselectする際にすべてのカラムを選択対象から除外するよう設定を変更する.
     * <p>
     * <p>
     * このメソッドは、レコードをselectする際に、一部のカラムを取得する場合に、 Column.setSelectableと同時に利用される。
     * </p>
     * <pre>
     * 例： テーブル MYTABLEのカラムが、COL1, COL2, COL3 ... COL100 とあり、
     * この中で COL1 と COL25をレコード抽出する場合。
     *
     * MyTable mytable = new MyTable();
     * mytable.setJdbcSupplier(jdbc); //supplierはIJdbcSupplierをインプリメントしたクラス
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
     * 自動生成したselect文を取得する.
     *
     * @param conditions 抽出条件
     *
     * @return select文
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
     * 指定条件で取得されるレコードの数を取得する.
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
     * 直接SQL構文渡してデータを取得する.
     * <p>
     * このライブラリを使ってテーブルを実装すると、DBサーバ上のテーブルと Table派生クラスのソースコードでカラムの名称やデータ型などの定義が、
     * 同期することになる。
     * </p>
     * <p>
     * 一方、このメソッドはソースコードのテーブル定義とは無関係に文字列を 渡しているため、このメソッドを直接外部クラスから利用するとソースコード
     * 上の定義変更に追随できなくなり、想定外の副作用を引き起こす可能性がある。
     * </p>
     * <p>
     * したがって非常に複雑なSQL構文の使用が避けられないとき以外は使用しないこと。
     * </p>
     *
     * @param cmd select文
     *
     * @return 抽出結果
     */
    public ResultSet select(String cmd) {
        ResultSet rs = this.getJdbc()
                .select(cmd);
        return rs;
    }

    /**
     * テーブルを作成する
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

                //テーブル情報の初期化まで情報提供してうっとおしいので制限つけた
                if (!this.getClass().getName().equals(TableInfo.class.getName())
                        && !this.getClass().getName().equals(ColumnInfo.class.getName())) {
                    Logger.getLogger(this.getClass().getName())
                            .log(Level.INFO, this.getName() + " created.");
                }
            }
        }
    }

    /**
     * SQLを実行する.特殊な例を除いて使用すべきではない。
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
     * カラムの名称変更.
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
     * データベースにテーブルがあるかどうかを確認する。
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
     * データを更新する。該当するレコードが存在しない場合は追加する。
     * <p>
     * <p>
     * DBサーバーによってはmergeに対応していないものがあるので、 H2DB以外で使用する場合は、事前にサーバーの仕様を確認すること。
     * </p>
     *
     * PGSQLはgetcount->insertで結合する
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

            // キーフィールドを抽出
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
     * レコードを追加する。確実にレコードが存在しないときのみ使用可。
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
     * カラムに登録された値で、データをマージする。
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
     * SQL文をコンソールに出力する. 主にデバッグ用
     *
     * @param sql
     */
    public void monitorSqlCommand(String sql) {
        System.out.println(sql);
    }

    /**
     * カラムに登録された値で、データをマージする。
     *
     * @return true - 成功, false - 失敗
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
     * テーブルがサーバに存在するかどうか確認し、なければ作成する.
     * <p>
     * また、テーブルがすでに存在するときは、クラス上のカラム定義とサーバ上の定義を比較し サーバー上の定義を更新する。
     * </p>
     * <p>
     * <p>
     * ただし、安全性を考慮して、以下の更新のみを行う。
     * </p>
     * <pre>
     * ・カラムの追加は行うが、削除はしない。
     * ・型変換はサイズの拡大のみ（varcharのサイズのみ、short to int to longは未実装)
     * ※初期値は変更を行う。
     * ※Nullは不許可→許可は行う。
     * ※プライマリキーの追加、削除は行う。
     * (※は将来実装予定)
     * </pre>
     * <p>
     * 上記以外は無視する。
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
                //サーバーにあるが、クラスに無い
                String tblName = this.getName().replaceAll("\"", ""); // pgsql対応
                ResultSet columns = jdbc
                        .getConnection()
                        .getMetaData()
                        .getColumns(null, null, this.getName().toUpperCase(), "%");
                while (columns.next()) {
                    String colName = columns.getString("COLUMN_NAME");
                    if (this.isMyColumn(colName.toUpperCase())) {
                        this.checkColumnAndUpdate(columns);
                    }
                }
                //クラスにあるがサーバーに無い
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
     * TableInfoとColumnInfoに情報を記録する.
     *
     * @param supplier
     */
    public void recordInfo(Jdbc supplier) {

        //テーブル情報テーブルの初期化.getConectionで作られるので、alterしない。
        TableInfo tableInfo = new TableInfo();
        tableInfo.setJdbc(supplier);

        //カラム情報テーブルの初期化
        ColumnInfo columnInfo = new ColumnInfo();
        columnInfo.setJdbc(supplier);

        //テーブル情報の記録.
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

        //カラム情報の記録.
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
     * テーブルの論理名称を取得する.
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
     * カラムの論理名を取得する.
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
     * テーブルを定義に基づいて追加する。
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
     * テーブルの定義をHtml形式で返す。
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
     * 事前にColumn.setValueで値をセットしておき、更新条件を conditionsで指定することで値を書き換える。
     *
     * @param conditions 更新条件
     *
     * @return 成功するとtrue;
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
     * カラムに格納されている値をクリアする.
     * <p>
     * 追加、更新、削除の呼び出し操作をする前に、このメソッドを呼び出して、 事前に使用した値をうっかり使わないようにすることを推奨している。
     * </p>
     */
    public void clearValues() {
        this.forEach((column) -> {
            column.clearValue();
        });
    }

    /**
     * 指定条件で取得されるレコードの数を取得する.
     * <p>
     * javaのResultSetには、データ抽出時の件数を取得するメソッドがないため、 事前に件数を把握しておきたいときにこのメソッドを用いる。
     * </p>
     * <p>
     * なお、件数取得対象のSelect文をそのままSQL文のサブクエリーとして組み込んで いるだけである。
     * このメソッドとselectメソッドを両方利用するときは、サーバーとやり取りを2回
     * することになるので、なるべく件数依存しないで済むように実装するほうがお行儀 のよいプログラムといえる。
     * </p>
     *
     * @param conditions 抽出条件
     *
     * @return レコード数
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
     * 指定したカラムが、テーブルに所属するかどうかを取得する。
     *
     * @param column カラム
     *
     * @return true:所属する false:所属しない
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
     * 指定したカラム名に該当するカラムが、テーブルに所属するか どうかを取得する。
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
     * データベース上に無い、後で追加されたカラムかどうかを調べる.
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
     * カラムを取得する。
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
     * 参照先を持つカラムを取得する。
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
     * データをselectする際に、参照用のテーブルとしてふるまうための コンディションを生成する.
     * <p>
     * テーブルの参照関係が、
     * </p>
     * <pre>
     * TableA.ColumnA → TableB.ColumnA
     * TableC.ColumnB → TableB.ColumnB
     * </pre> のときに、3つのテーブルを結合するSQLは
     * <pre>
     * Select TableA.ColumnA1, TableC.ColumnC2 from TableA
     * inner join TableB on
     * TableB.ColumnA = TableA.ColumnA
     * inner join TableC on
     * TableC.ColumnB = TableB.ColumnB
     * </pre>
     * <p>
     * となるが、ここでテーブルBは単純に結合の橋渡しをする。
     * </p>
     * <p>
     * このような、橋渡しをするテーブルをselectで指定する際の コンディションを生成することを目的としている。
     * </p>
     *
     * @return
     */
    public Condition useJoinPath() {
        Condition rvalue = new Condition(this.get(0), Condition.RELATION_PATH);
        return rvalue;
    }

    /**
     * アノテーションで定義した論理名をカラムの別名に使用する. 翻訳データがあれば、別名は翻訳データで上書きする.
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
     * テーブルをトランケートする。 テーブルが存在しないときは、新規作成を行う。
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
