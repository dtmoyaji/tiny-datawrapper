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

/**
 * 並べ替え専用のCondition
 * @author Takahiro MURAKAMI
 */
public class ConditionForOrder extends Condition {

    public static final int ORDER_ASC = 100;

    public static final int ORDER_DESC = 101;

    public ConditionForOrder(Column col, int operation) {
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
