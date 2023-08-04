package org.tiny.datawrapper;

/**
 * 並べ替え専用のCondition
 * @author dtmoyaji
 */
public class ConditionForOrder<T> extends Condition<T> {

    public static final int ORDER_ASC = 100;

    public static final int ORDER_DESC = 101;

    public ConditionForOrder(Column<T> col, int operation) {
        super(col, operation);
    }

    /**
     * 並ぶ方向を取得する
     * @return ORDER_ASC:昇順, ORDER_DESK:降順
     */
    public String getOrder() {
        String rvalue = "";

        switch (this.operation) {
            case ORDER_ASC:
                rvalue = this.column.getFullName() + " asc";
                break;
            case ORDER_DESC:
                rvalue = this.column.getFullName() + " desc";
                break;
        }

        return rvalue;
    }

}
