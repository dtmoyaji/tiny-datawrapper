package org.tiny.datawrapper;

/**
 * Javaの命名規則とデータベースの命名規則を相互変換 するためのクラス。内部で利用するので、外部には出てこない と思います。
 *
 * @author dtmoyaji
 */
public class NameDescriptor {

    /**
     * TableName, ColumnNameを TABLE_NAME, COLUMN_NAMEに変換する.
     *
     * @param name
     * @return
     */
    public static String toSqlName(String name) {
        String rvalue = "";

        //SpringBean対応で修飾子をカット
        if (name.contains("$$")) {
            name = name.substring(0, name.indexOf("$$"));
        }

        char[] leaf = name.toCharArray();

        for (int i = 0; i < leaf.length; i++) {
            if (leaf[i] >= 'A' && leaf[i] <= 'Z') {
                char newleaf = (char) ('a' + (leaf[i] - 'A'));
                if (i > 0) {
                    rvalue += '_';
                }
                rvalue += newleaf;
            } else {
                rvalue += leaf[i];
            }
        }

        rvalue = rvalue.toUpperCase();
        return rvalue;
    }

    public static String toSqlName(String name, int serverType) {
        String rvalue = NameDescriptor.toSqlName(name);
        switch (serverType) {
            case Jdbc.SERVER_TYPE_MYSQL:
                rvalue = "`" + rvalue + "`";
                break;
            case Jdbc.SERVER_TYPE_H2DB:
            case Jdbc.SERVER_TYPE_PGSQL:
                rvalue = "\"" + rvalue + "\"";
                break;
        }
        return rvalue;
    }

    /**
     * SQLのテーブル表示形式 table_nameをJavaの文字列表記TableNameに変換する.
     *
     * @param name
     * @return
     */
    public static String toJavaName(String name) {
        String rvalue = "";

        name = name.replaceAll("`", "");
        name = name.replaceAll("\"", "");

        char[] leaf = name.toLowerCase().toCharArray();

        for (int i = 0; i < leaf.length; i++) {
            if (i == 0) {
                rvalue += (char) ('A' + (leaf[i] - 'a'));
            } else if (leaf[i] == '_') {
                i++;
                if (i < leaf.length) {
                    rvalue += (char) ('A' + (leaf[i] - 'a'));
                }
            } else {
                rvalue += leaf[i];
            }
        }

        return rvalue;
    }

    /**
     * 名前に付随するクオートを外す
     *
     * @param name
     * @return クオートをカットした名前
     */
    public static String getSplitedName(String name) {
        String rvalue = name.replaceAll("\"", "");
        rvalue = rvalue.replaceAll("`", "");
        return rvalue;
    }

}
