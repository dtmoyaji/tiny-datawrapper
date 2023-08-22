package org.tiny.datawrapper;

/**
 * データの処理を一括して行うためのユーティリティクラス。
 *
 * @author dtmoyaji
 */
public class TableModifier {

    /**
     * 指定した複数のテーブルに対して、すべてのカラムをselect候補から
     * 外す処理を行う。
     *
     * @param tables テーブル
     */
    public static final void unselectAllColumn(Table... tables) {
        for (Table table : tables) {
            table.unselectAllColumn();
        }
    }

}
