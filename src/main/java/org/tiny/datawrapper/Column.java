package org.tiny.datawrapper;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * カラム.
 *
 * @author dtmoyaji
 * @param <T>
 */
public class Column<T> extends ArrayList<RelationInfo> {

    public static final int UNDEFINED_LENGTH = -1;

    public static final int DEFAULT_VARCHAR_SIZE = 32;

    public static final int DEFAULT_DECIMAL_INT_SIZE = 18;

    public static final int DEFAULT_DECIMAL_FLOAT_SIZE = 9;

    public static final String DEFAULT_TIMESTAMP = "CURRENT_TIMESTAMP";

    public static final String DEFAULT_DATETIME_ORIGIN = "'1900-01-01 00:00:00'";

    public static final int SIZE_2 = 2;

    public static final int SIZE_4 = 4;

    public static final int SIZE_8 = 8;

    public static final int SIZE_16 = 16;

    public static final int SIZE_32 = 32;

    public static final int SIZE_64 = 64;

    public static final int SIZE_128 = 128;

    public static final int SIZE_256 = 256;

    public static final int SIZE_512 = 512;

    public static final int SIZE_1024 = 1024;

    public static final int SIZE_2048 = 2048;

    public static final int SIZE_4096 = 4096;

    /**
     * 見せない
     */
    public static final int VISIBLE_TYPE_HIDDEN = 1;

    /**
     * ラベル
     */
    public static final int VISIBLE_TYPE_LABEL = 2;

    /**
     * テキストフィールド
     */
    public static final int VISIBLE_TYPE_TEXT = 4;

    /**
     * チェックボックス
     */
    public static final int VISIBLE_TYPE_CHECKBOX = 8;

    /**
     * パスワード
     */
    public static final int VISIBLE_TYPE_PASSWORD = 16;

    /**
     * パスワードnull可
     */
    public static final int VISIBLE_TYPE_PASSWORD_NOREQ = 32;

    /**
     * 長いテキスト
     */
    public static final int VISIBLE_TYPE_TEXTAREA = 64;

    /**
     * 長いリッチテキスト
     */
    public static final int VISIBLE_TYPE_RICHTEXTAREA = 128;


    /**
     * カラム名
     */
    private String name;

    /**
     * カラムが属するテーブル
     */
    private Table myTable;

    /**
     * カラムの値
     */
    private T value = null;

    /**
     * カラムの型（Javaのクラス名表現を格納する)
     */
    private String typeName;

    /**
     * 可変長指定
     */
    private boolean variable = false;

    /**
     * カラムの長さ
     */
    private int length = UNDEFINED_LENGTH;

    /**
     * カラムが浮動小数のときの、小数部の長さ
     */
    private int floatlength = UNDEFINED_LENGTH;

    /**
     * 主キーかどうか
     */
    private boolean primaryKey = false;

    /**
     * NULLの許容
     */
    private boolean nullable = true;

    /**
     * 自動更新
     */
    private boolean autoIncrement = false;

    /**
     * 他のテーブルを参照するかどうか
     */
    private boolean relation = false;

    /**
     * レコード発生時の初期値
     */
    private String defaultValue = "";

    /**
     * 値をもつかどうか.
     */
    private boolean existValue = false;

    /**
     * Select文で、カラムを選択するかどうか.
     */
    private boolean selectable = true;

    /**
     * マージ対象に入れるかどうか
     */
    private boolean margeTarget = true;

    /**
     * カラムの表示用表現（別名).
     * <p>
     * select Table.Column1 alias1 from Table のalias1
     */
    private String alias = "";

    /**
     * RecordEditor 上のフィールド表示タイプ.
     */
    private int visibleType = Column.VISIBLE_TYPE_LABEL;

    /**
     * MD5フィールドの値計算に含めるかどうか
     */
    private boolean includeMd5 = true;

    public Column() {

    }

    /**
     * カラムの別名を登録する。 別名はselect時にフィールドの別名表現として使用される。
     * この値を登録すると、そのカラムは自動的にSelectableとなる。
     *
     * @param text 別名
     *
     * @return 続けて設定できるように、コンディションを返す。
     */
    public Condition<T> setAlias(String text) {
        this.alias = text;
        return new Condition<T>(this, Condition.SELECTABLE);
    }

    /**
     * カラムの別名を取得する。
     *
     * @return 別名
     */
    public String getAlias() {
        return this.alias;
    }

    /**
     * 別名を持つかどうかを取得する。
     *
     * @return true: あり false: なし
     */
    public boolean hasAlias() {
        return this.alias.length() > 0;
    }

    /**
     * 別名を消去する。
     *
     * @return このカラム
     */
    public Column<T> removeAlias() {
        this.alias = "";
        return this;
    }

    /**
     * カラムの名称を取得する。 テーブル生成時に、カラムの変数名より自動的に 生成された名称を格納する。
     *
     * @return テーブル名
     */
    public String getName() {
        return this.name;
    }

    /**
     * *
     * クオートを外した名前
     *
     * @return
     */
    public String getSplitedName() {
        return NameDescriptor.getSplitedName(this.name);
    }

    public void modifyName(int ServerType) {
        switch (this.getTable().getServerType()) {
            case Jdbc.SERVER_TYPE_MYSQL:
                if (!this.name.contains("`")) {
                    this.name = NameDescriptor.toSqlName(
                            this.getJavaName(),
                            Jdbc.SERVER_TYPE_MYSQL
                    );
                }
                break;
            case Jdbc.SERVER_TYPE_H2DB: //H2dbはv2.0でPGSQLに文法を寄せてったので、まとめた
            case Jdbc.SERVER_TYPE_PGSQL:
                if (!this.name.contains("\"")) {
                    this.name = NameDescriptor.toSqlName(
                            this.getJavaName(),
                            Jdbc.SERVER_TYPE_PGSQL
                    );
                }
                break;
        }
    }

    /**
     * Javaの表記形式で、カラム名称を取得する.
     * <p>
     * カラムの物理名がMY_COLUMNのときは、MyColumnを返す.
     * </p>
     *
     * @return Javaの表記名.
     */
    public String getJavaName() {
        return NameDescriptor.toJavaName(this.getName());
    }

    /**
     * カラムを TableName.ColumnName の表記で取得する。
     * Select文を自動生成する際に内部的に使用されている。
     *
     * @return カラム名
     */
    public String getFullName() {
        String rvalue = "%s.%s";
        String colName = this.getName();
        String tableName = (this.getTable().hasAlias())
                ? this.getTable().getAlias() : this.getTable().getName();
        rvalue = String.format(rvalue, tableName, colName);
        return rvalue;
    }

    /**
     * カラムの定義文を取得する Create や Alter で使用する定義
     *
     * @return
     */
    public String getColumnDef() {
        String fieldLine = " %s %s %s %s %s ";
        String fieldDef = "";

        String pkeydef = ""; //primary key

        String fieldType = this.getType();
        String autoIncrement = "";
        String nullable = "";
        String defaultValue = "";

        //カラムがIntのとき
        if (fieldType.equals(Integer.class.getSimpleName())) {
            fieldType = "int";
        }

        //カラムがSmallintのとき
        if (fieldType.equals(Short.class.getSimpleName())) {
            fieldType = "smallint";
        }

        //時刻型のとき
        if (fieldType.equals(Timestamp.class.getSimpleName())) {
            switch (this.getTable().getServerType()) {
                case Jdbc.SERVER_TYPE_H2DB:
                    fieldType = "timestamp";
                    break;
                case Jdbc.SERVER_TYPE_MYSQL:
                    fieldType = "datetime";
                    break;
                case Jdbc.SERVER_TYPE_PGSQL:
                    fieldType = "timestamp";
                    break;
                default:
                    fieldType = "timestamp";
                    break;
            }
        }

        //日付型のとき
        if (fieldType.equals(Date.class.getSimpleName())) {
            fieldType = "date";
        }

        //時刻型のとき
        if (fieldType.equals(Time.class.getSimpleName())) {
            fieldType = "time";
        }

        //カラムがStringのとき
        if (fieldType.equals(String.class.getSimpleName())) {
            fieldType = "varchar%s";
            String variableSize = this.getVariableSize();
            fieldType = String.format(fieldType, variableSize);
        }

        //カラムがchar[]のとき
        if (fieldType.equals(char[].class.getSimpleName())) {
            fieldType = "char%s";
            String variableSize = this.getVariableSize();
            fieldType = String.format(fieldType, variableSize);
        }

        //カラムがBigDecimalのとき
        if (fieldType.equals(BigDecimal.class.getSimpleName())) {
            fieldType = "decimal%s";
            String variableSize = this.getVariableSize();
            fieldType = String.format(fieldType, variableSize);
        }

        if (this.isPrimaryKey()) {
            pkeydef += this.getName() + ",";
        }

        if (this.isAutoIncrement()) {
            autoIncrement = "AUTO_INCREMENT";

            // PGSQLはauto_incrementできないのでserialに差し替え
            if (this.getTable().getServerType() == Jdbc.SERVER_TYPE_PGSQL) {
                String buf = "%s serial not null";
                buf = String.format(buf, this.getName());
                return buf;
            }
        }

        if (!this.isNullable()) {
            nullable = "not null";
        }

        if (this.getDefault()
                .length() > 0) {
            defaultValue = "default " + this.getDefault();
        }

        fieldDef = String.format(fieldLine,
                this.getName(),
                fieldType,
                nullable,
                autoIncrement,
                defaultValue);

        return fieldDef;
    }

    /**
     * カラムの型を文字列表現で格納する。
     *
     * @return カラムの型
     */
    public String getType() {
        return this.typeName;
    }
    
    public void setType(String typeName){
        this.typeName = typeName;
    }

    /**
     * カラムの値を取得する。
     *
     * @return
     */
    public T getValue() {
        return value;
    }

    public Column<T> setValue(T value) {
        if (value != null
                && this.getType().equals(String.class.getSimpleName())) {
            String vvalue = (String) value;
            vvalue = vvalue.replaceAll("'", "''");
            this.value = (T) vvalue;
        } else {
            this.value = value;
        }
        this.existValue = true;
        return this;
    }

    public Column<T> setPrimaryKey(boolean key) {
        this.primaryKey = key;
        return this;
    }

    public boolean isPrimaryKey() {
        return this.primaryKey;
    }

    public Column<T> setAllowNull(boolean nullable) {
        this.nullable = nullable;
        return this;
    }

    public boolean isNullable() {
        return this.nullable;
    }

    public Column<T> setDefault(String defaultvalue) {
        this.defaultValue = defaultvalue;
        return this;
    }

    public String getDefault() {
        return this.defaultValue;
    }

    public Column<T> setLength(int intLength, int floatLength) {
        this.length = intLength;
        this.floatlength = floatLength;
        this.variable = true;
        return this;
    }

    public Column<T> setLength(int length) {
        this.length = length;
        this.floatlength = -1;
        this.variable = true;
        return this;
    }

    public int getLength() {
        return this.length;
    }

    public void setLength(String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int getFloatLength() {
        return this.floatlength;
    }

    public boolean isVariable() {
        return this.variable;
    }

    public String getVariableSize() {
        String fmt = "";
        if (this.getFloatLength() == UNDEFINED_LENGTH) {
            fmt = "(%d)";
        } else {
            fmt = "(%d,%d)";
        }
        fmt = String.format(fmt, this.length, this.floatlength);
        return fmt;
    }

    public void setTable(Table table) {
        this.myTable = table;
    }

    public Table getTable() {
        return this.myTable;
    }

    /**
     * カラムの参照先を登録する. テーブルのPrimary Keyを参照することが明らかな場合は、このメソッドでテーブルのみを指定できる．
     *
     * @param table 参照先のテーブルクラス
     *
     * @return このカラム(続けて定義を記述するため.)
     */
    public Column<T> addRelationWith(Class<? extends Table> table) throws TinyDatabaseException {
        return this.addRelationWith(table, this);
    }

    /**
     * カラムの参照先を登録する. テーブルと参照先カラムを明示する必要がある場合は、このメソッドでテーブルとカラムを指定する．
     *
     * @param table 参照先のテーブルクラス
     * @param column 参照先のカラム
     *
     * @return このカラム(続けて定義を記述するため.)
     * @throws org.tiny.datawrapper.TinyDatabaseException 同一テーブル内のリレーションが宣言されたとき
     */
    public Column<T> addRelationWith(Class<? extends Table> table, Column<T> column) throws TinyDatabaseException {

        this.relation = false;

        //同一テーブル内の宣言は登録しない（自動処理できないため）。
        if (!table.equals(this.myTable.getClass())) {
            boolean exist = false;
            for (RelationInfo inf : this) {
                if (inf.equals(table, column)) {
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                this.add(new RelationInfo(table, column));
            }
            // SpringBeanでArrayListとpublicフィールドが分離してしまうので補正。
            this.bindFieldWithArray();
        } else {
            throw new TinyDatabaseException("同一テーブル内のリレーション宣言が行われています。");
        }
        return this;
    }

    /**
     * なんでかわからんが、SpringBeanするとフィールドとArrayのitemが別オブジェクトに なってしまうので、もっかい登録しなおし。
     */
    public void bindFieldWithArray() {
        Table tbl = this.getTable();
        for (int i = 0; i < tbl.size(); i++) {
            if (tbl.get(i).getJavaName().equals(this.getJavaName())) {
                tbl.set(i, this);
                break;
            }
        }
        this.relation = true;
    }

    public boolean hasRelation() {
        return this.relation;
    }

    public Condition<T> sameValueOf(String value) {
        Condition<T> rvalue = new Condition(this, Condition.EQUALS, value);
        return rvalue;
    }

    public Condition<T> sameValueOf(int value) {
        Condition<T> rvalue = new Condition(this, Condition.EQUALS, String.valueOf(
                value));
        return rvalue;
    }

    public Condition<T> sameValueOf(Column<T> col) {
        Condition<T> rvalue = new Condition(this, Condition.EQUALS, col);
        return rvalue;
    }

    public Condition<T> differentValueOf(String value) {
        Condition<T> rvalue = new Condition(this, Condition.NOT_EQUALS, value);
        return rvalue;
    }

    public Condition<T> differentValueOf(Column<T> col) {
        Condition<T> rvalue = new Condition(this, Condition.NOT_EQUALS, col);
        return rvalue;
    }

    public Condition<T> isNull() {
        Condition<T> rvalue = new Condition(this, Condition.IS_NULL);
        return rvalue;
    }

    public Condition<T> isNotNull() {
        Condition<T> rvalue = new Condition(this, Condition.IS_NOT_NULL);
        return rvalue;
    }

    public Condition<T> like(String value) {
        Condition<T> rvalue = new Condition(this, Condition.LIKE, value);
        return rvalue;
    }

    public Condition<T> greater(String value) {
        Condition<T> rvalue = new Condition(this, Condition.GREATER, value);
        return rvalue;
    }

    public Condition<T> greater(Column<T> col) {
        Condition<T> rvalue = new Condition(this, Condition.GREATER, col);
        return rvalue;
    }

    public Condition<T> greaterEqual(String value) {
        Condition<T> rvalue = new Condition(this, Condition.GREATER_EQUAL, value);
        return rvalue;
    }

    public Condition<T> greaterEqual(Column<T> col) {
        Condition<T> rvalue = new Condition(this, Condition.GREATER_EQUAL, col);
        return rvalue;
    }

    public Condition<T> lower(String value) {
        Condition<T> rvalue = new Condition(this, Condition.LOWER, value);
        return rvalue;
    }

    public Condition<T> lower(Column<T> col) {
        Condition<T> rvalue = new Condition(this, Condition.LOWER, col);
        return rvalue;
    }

    public Condition<T> lowerEqual(String value) {
        Condition<T> rvalue = new Condition(this, Condition.LOWER_EQUAL, value);
        return rvalue;
    }

    public Condition<T> lowerEqual(Column<T> col) {
        Condition<T> rvalue = new Condition(this, Condition.LOWER_EQUAL, col);
        return rvalue;
    }

    public Column<T> setAutoIncrement(boolean setting) {
        this.autoIncrement = setting;
        return this;
    }

    public boolean isAutoIncrement() {
        return this.autoIncrement;
    }

    public void clearValue() {
        this.existValue = false;
        this.value = null;
    }

    public boolean hasValue() {
        if (this.value == null) {
            this.existValue = false;
        }
        return this.existValue;
    }

    public Condition<T> setSelectable(boolean selectable) {
        this.selectable = selectable;
        return new Condition(this, Condition.SELECTABLE);
    }

    /**
     * 2つのテーブルをつなぐ中間テーブルのキーフィールドとして機能するときに指定する.
     *
     * @return
     */
    public Condition<T> asJoinBridge() {
        return new Condition(this, Condition.RELATION_PATH);
    }

    public boolean getSelectable() {
        return this.selectable;
    }

    public static Timestamp getCurrentTimestamp() {
        Timestamp rvalue = Timestamp.valueOf(LocalDateTime.now());
        return rvalue;
    }

    /**
     * ResulSetより、カラムを抽出してその値を返す。
     *
     * @param rs 抽出元のResultSet
     *
     * @return 抽出した値
     */
    public T of(ResultSet rs) {
        T rvalue = null;
        try {
            int colId = -1;
            String pullname = this.getName().replace("\"", "").replace("`", "");
            String aliasName = this.getAlias().replace("\"", "").replace("`", "");
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                if (rs.getMetaData().getColumnName(i).equalsIgnoreCase(pullname)
                        || rs.getMetaData().getColumnName(i).equalsIgnoreCase(aliasName)) {
                    colId = i;
                    break;
                }
            }
            if (this.getType().equals(char[].class.getSimpleName())) {
                String sz = rs.getString(colId);
                return (T) sz.toCharArray();
            } else if (this.getType().equals(Short.class.getSimpleName())) {
                Short srt = rs.getShort(colId);
                rvalue = (T) srt;
            } else {
                rvalue = (T) rs.getObject(colId);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Column.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        return rvalue;
    }

    /**
     * RecordEditor上の表示タイプを設定する.
     *
     * @param type
     *
     * @return this
     */
    public Column<T> setVisibleType(int type) {
        this.visibleType = type;
        return this;
    }

    /**
     * RecordEditor上の表示タイプを取得する.
     *
     * @return 表示種別
     */
    public int getVisibleType() {
        return this.visibleType;
    }

    public ConditionForOrder<T> asc() {
        ConditionForOrder<T> rvalue = new ConditionForOrder(this,
                ConditionForOrder.ORDER_ASC);
        return rvalue;
    }

    public ConditionForOrder<T> desc() {
        ConditionForOrder<T> rvalue = new ConditionForOrder(this,
                ConditionForOrder.ORDER_DESC);
        return rvalue;
    }

    /**
     * MD5の計算に値を含めるかどうか
     *
     * @return
     */
    public boolean isInculdeMd5() {
        return this.includeMd5;
    }

    /**
     * MD5の計算に値を含めるかどうかを設定する
     *
     * @param include true - 含める false - 含めない
     *
     * @return
     */
    public Column<T> setInculdeMd5(boolean include) {
        this.includeMd5 = include;
        return this;
    }

    /**
     * マージ対象にするかどうかを指定する。
     *
     * @param status
     */
    public Column<T> setMargeTarget(boolean status) {
        this.margeTarget = status;
        return this;
    }

    /**
     * マージ対象になっているかどうかを取得する。
     *
     * @return
     */
    public boolean isMargeTarget() {
        return this.margeTarget;
    }
    
    public boolean isMatchedVisibleType(int type){
        return (type & this.getVisibleType()) > 0;
    }

}
