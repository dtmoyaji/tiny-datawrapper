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

import java.util.ArrayList;

/**
 * Select文を生成するクラス.
 *
 * @author Takahiro MURAKAMI
 */
public class SelectionDescriptor {

    public static final int MODE_ENABLE_ONLY_AUTOMATIC = 0;
    public static final int MODE_INCLUDE_DISABLE = 1;

    //テーブル名を収集するリスト
    ArrayList<Table> tables = new ArrayList<>();

    private int[][] tableReference;
    private String[][] joinPattern;

    private int mode = MODE_ENABLE_ONLY_AUTOMATIC;

    private int serverType = Jdbc.SERVER_TYPE_H2DB;

    public SelectionDescriptor() {

    }

    public void setServerType(int type) {
        this.serverType = type;
    }

    public int getServerType() {
        return this.serverType;
    }

    /**
     *
     * @param baseTable
     * @param conditions
     *
     * @return
     *
     * @throws TinyDatabaseException
     */
    public String getSelectSentence(Table baseTable, Condition[] conditions)
            throws TinyDatabaseException {

        baseTable.postConstruct();

        String rvalue = "select distinct %s from %s %s %s";
        String columns = "";
        String fromAndJoins = " |" + baseTable.getNameWithAlias() + "| ";
        String wheres = "";
        String orders = "";

        //選択対象のカラムを列挙し、columnsに格納する。別名があれば別名を用いる.
        for (Condition condition : conditions) {
            if (!(condition instanceof ConditionForOrder)) {
                if (condition.getColumn()
                        .getSelectable()) {
                    Column column = condition.getColumn();
                    String colName = column.getFullName();
                    if (condition.getColumn()
                            .hasAlias()) {
                        colName += " " + condition.getColumn()
                                .getAlias();
                    }
                    //Select の抽出フィールドは同じ名称で複数選択させない。
                    if (!columns.contains(colName)) {
                        columns += ", " + colName;
                    }
                }
            }
        }

        //baseTableの抽出条件を反映する。
        for (Column col : baseTable) {
            if (col.getSelectable()) {
                String colName = col.getFullName();
                if (col.hasAlias()) {
                    colName += " " + col.getAlias();
                }
                if (!columns.contains(colName)) {
                    columns += ", " + colName;
                }
            }
        }

        if (columns.length() < 1) {
            throw new TinyDatabaseException("no columns for selection.");
        }
        columns = columns.substring(1);

        //自分の名前を追加する。
        tables.add(baseTable);

        //conditionsに含まれる他のテーブルを追加する
        String stackName = baseTable.getAlias();
        String tableNameStack = "<" + stackName + ">";
        for (Condition condition : conditions) {
            Table t = condition.getColumn()
                    .getTable();
            stackName = t.getAlias();
            if (!tableNameStack.contains("<" + stackName + ">")) {
                tables.add(t);
                tableNameStack += "<" + stackName + ">";
            }
        }

        //テーブルの依存関係に基づいて、Select生成に必要なテーブルがすべてリストにあるかどうかをチェックする。
        this.mapTableRelation();
        boolean res = this.checkReleation();

        //テーブルが不足するときの処理。
        if (!res) {
            this.mapOut();
            throw new TinyDatabaseException(
                    "Unlinkd tables used at select database.");
        }

        //テーブルが全部そろったとき
        if (res) {

            // inner joinの追加
            for (int i = 0; i < this.tableReference.length; i++) {
                for (int j = 0; j < this.tableReference[i].length; j++) {
                    if (this.tableReference[i][j] > 0 && i != j) {
                        Table table = tables.get(i);
                        //table.setJdbcSupplier(baseTable.getJdbcSupplier());
                        String joinTable = table.getNameWithAlias();
                        if (fromAndJoins.contains("|" + joinTable + "|")) {
                            joinTable = table.getNameWithAlias();
                        }
                        if (!fromAndJoins.contains("|" + joinTable + "|")) {
                            switch (table.getJoinType()) {
                                case Table.JOIN_TYPE_LEFT:
                                    fromAndJoins += "\n left join |" + joinTable + "| on \n";
                                    break;
                                case Table.JOIN_TYPE_RIGHT:
                                    fromAndJoins += "\n right join |" + joinTable + "| on \n";
                                    break;
                                default:
                                    fromAndJoins += "\n inner join |" + joinTable + "| on \n";
                                    break;
                            }
                            fromAndJoins += "\t" + this.joinPattern[i][j]
                                    .substring(4) + "";
                        }
                    }
                }
            }

        }

        while (fromAndJoins.contains("$")) {
            fromAndJoins = fromAndJoins.replace("$", "");
        }

        while (fromAndJoins.contains("|")) {
            fromAndJoins = fromAndJoins.replace("|", "");
        }

        // whereとorder byの処理
        for (Condition condition : conditions) {
            if (condition instanceof ConditionForOrder) {
                String o = ((ConditionForOrder) condition).getOrder();
                if (o.trim()
                        .length() > 0) {
                    orders += ", " + o + "\n";
                }
            } else {
                String w = condition.getWhere();
                if (w.trim()
                        .length() > 0) {
                    wheres += " and " + w + "\n";
                }
            }
        }

        // 全テーブルの有効レコードに自動的に絞り込む
        if (this.mode == MODE_ENABLE_ONLY_AUTOMATIC) {
            String disable = "DISABLE";
            switch (baseTable.getServerType()) {
                case Jdbc.SERVER_TYPE_MYSQL:
                    disable = "`" + disable + "`";
                    break;
                case Jdbc.SERVER_TYPE_PGSQL:
                    disable = "\"" + disable + "\"";
                    break;
            }
            for (Table table : this.tables) {
                if (table.isMyColumn(disable)) {
                    wheres += " and " + table.getAlias() + "." + disable + " = 0 \n";
                }
            }
        }

        if (wheres.length() > 0) {
            wheres = "\nwhere " + wheres.substring(" and".length());
        }

        if (orders.length() > 0) {
            orders = "\n order by " + orders.trim()
                    .substring(",".length());
        }

        rvalue = String.format(rvalue, columns, fromAndJoins, wheres, orders);
        //System.out.println(rvalue);
        return rvalue;
    }

    /**
     *
     * @param start
     * @param goal
     *
     * @return
     */
    public boolean findRoute(Table current, Table start, Table goal) {
        boolean rvalue = false;

        ArrayList<Column> cols = current.getRelationColumns();

        for (Column col : cols) {

        }

        return rvalue;
    }

    /**
     * テーブルの依存マトリックスを展開し、依存状態を確認する。 各列、各行の合計がそれぞれ2以上の値をとる場合は、チェックにパスする。
     *
     * @return
     */
    public boolean checkReleation() {
        boolean rvalue = true;

        //テーブルが1つのときはjoinしていないので、そのままtrueを返す。
        if (this.tables.size() == 1) {
            return rvalue;
        }

        //縦横のチェック
        int[] horiz = new int[this.tables.size()];
        int[] vert = new int[this.tables.size()];
        for (int i = 0; i < this.tables.size(); i++) {
            horiz[i] = 0;
            vert[i] = 0;
        }
        for (int i = 0; i < this.tables.size(); i++) {
            for (int j = 0; j < this.tables.size(); j++) {
                horiz[i] += this.tableReference[i][j];
                vert[i] += this.tableReference[j][i];
            }
        }
        for (int i : horiz) {
            if (i < 2) {
                rvalue = false;
                break;
            }
        }
        if (rvalue) {
            for (int i : vert) {
                if (i < 2) {
                    rvalue = false;
                    break;
                }
            }
        }

        return rvalue;
    }

    public void joinOut() {
        System.out.println();
        for (int i = 0; i < this.tables.size(); i++) {
            for (int j = 0; j < this.tables.size(); j++) {
                System.out.print("[" + this.joinPattern[i][j] + "]");
            }
            System.out.println("\t" + this.tables.get(i)
                    .getName());
        }
    }

    /**
     * テーブルの参照をマトリクスに展開する.<br>
     *
     * Selectで指定された複数のTableの派生クラス A,B,Cがあり
     * <pre>
     * A⇔B　相互参照
     * B->C　BからのみCを参照
     * </pre> の参照を持つ場合、参照相手をマトリックスABCにマップすると、
     * <pre>
     *    A B C　計
     *  A 1 1 0  2
     *  B 1 1 1  3
     *  C 0 0 1  1
     * 計 2 2 2
     * </pre> となる。一方通行の参照を考慮して、対角線を対称にマトリックスを ORすると
     * <pre>
     *    A B C　計
     *  A 1 1 0  2
     *  B 1 1 1  3
     *  C 0 1 1  2
     * 計 2 3 2
     * </pre> となる。 このライブラリではこのマトリックスを縦又は横の計をテーブルごとに見ながら
     * 合計が2以上のときにjoinを生成可能であると判断する。<br>
     * 例では、横計を見ながら A->Bのjoin, B->Aのjoin, B->Cのjoin, C->Bのjoinを許可することを
     * 判定している。<br>
     * 実際のプログラムの動作は、
     * <pre>
     * 先頭テーブルAより : Select from A を作成
     * A  1 [1] 0 より Inner join B を作成
     * B [1] 1  1 より Aは既出のためスキップ
     * B  1  1 [1] より Inner join C を作成
     * C  0 [1] 1 より Bは既出のためスキップ
     * </pre> となるので、マトリックスの対角上部だけスキャンしている。
     * <br>
     * 逆に、B->Cの参照がない場合、
     * <pre>
     *    A B C　計
     *  A 1 1 0  2
     *  B 1 1 0  2
     *  C 0 0 1  1
     * 計 2 2 1
     * </pre> となり、合計を見るだけで孤立したテーブルが混入しており、自動的に結合できないことがわかる<br>
     */
    private void mapTableRelation() {
        this.tableReference = new int[tables.size()][tables.size()];
        this.joinPattern = new String[tables.size()][tables.size()];

        if (this.tables.size() > 0) {

            for (int i = 0; i < this.tables.size(); i++) {
                this.tables.get(i)
                        .setMatched(false);

                for (int j = 0; j < this.tables.size(); j++) {
                    tableReference[i][j] = 0;
                    joinPattern[i][j] = "";
                }
                tableReference[i][i] = 1; // diagonal cell
            }

            for (int i = 0; i < this.tables.size(); i++) {
                Table refFrom = this.tables.get(i);
                for (int j = 0; j < refFrom.size(); j++) {

                    ArrayList<RelationInfo> col = refFrom.get(j);
                    for (int k = 0; k < col.size(); k++) {
                        String refName
                                = NameDescriptor.toSqlName(
                                        col.get(k).getTable().getSimpleName(),
                                        this.getServerType()
                                );

                        int l = this.getMatchedTableNumber(refName);
                        if (l > -1) {
                            this.tableReference[i][l] += 1;
                            if (i != l) {

                                for (Table tbl : this.tables) {
                                    //if (!tbl.isMatched()) {
                                    if (tbl.getName()
                                            .equals(refName)) {
                                        refName = tbl.getAlias();
                                        tbl.setMatched(true);
                                        break;
                                    }
                                    //}
                                }

                                Column c = (Column) col;
                                joinPattern[i][l] = " and $" + refFrom
                                        .getAlias() + "$." + c
                                                .getName()
                                        + " = $"
                                        + refName + "$." + c.getName();
                            }
                        }
                    }

                }
            }

            // プラグインなどの後から追加したパッケージに含まれるテーブルから既存のテーブル
            // に参照がある場合は、参照情報を事前に既存のテーブルに登録できないので、
            // 以下の処理でマトリックスを操作して対角を基軸に線対象のコピーを作る。
            // これで、追加パッケージ内のテーブルに参照を書くだけで自動でjoinできるようになる。
            for (int i = 0; i < tableReference.length; i++) {
                for (int j = 0; j < tableReference[i].length; j++) {
                    if (i != j) {
                        if (tableReference[i][j] > 0 && tableReference[j][i] == 0) {
                            tableReference[j][i] = tableReference[i][j];
                            joinPattern[j][i] = joinPattern[i][j];
                        }
                    }
                }
            }

        } else {
            tableReference[0][0] = 0;
            joinPattern[0][0] = "";
        }
        //this.mapOut();
        //this.joinOut();

    }

    private int getMatchedTableNumber(String tableName) {
        int rvalue = -1;
        for (int i = 0; i < this.tables.size(); i++) {
            Table target = this.tables.get(i);
            if (target.isNameEquals(tableName)) {
                rvalue = i;
                break;
            }
        }
        return rvalue;
    }

    public void mapOut() {
        System.out.println();
        for (int i = 0; i < this.tables.size(); i++) {
            for (int j = 0; j < this.tables.size(); j++) {
                System.out.print(this.tableReference[i][j]);
            }
            System.out.println("\t" + this.tables.get(i)
                    .getName());
        }
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return this.mode;
    }

}
