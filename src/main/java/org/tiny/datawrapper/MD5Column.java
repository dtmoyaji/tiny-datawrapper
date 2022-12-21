/*
 * The MIT License
 *
 * Copyright 2017 Takahiro MURAKAMI.
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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;

/**
 * 他のフィールドからMD5を生成して登録するためのカラム
 *
 * @author Takahiro MURAKAMI
 */
public class MD5Column extends Column<String> {

    public MD5Column() {
        this
            .setAllowNull(true)
            .setLength(SIZE_32)
            .setInculdeMd5(false); //自分自身は含めない
    }

    /**
     * 計算に使用するフィールド値を用いて、MD5を生成する.
     * CSVレコードを生成した後、ダイジェストの計算を行い値を格納する.
     * 全てのカラムに必要な値を登録した後で呼び出す必要があるので注意。
     *
     * @return
     */
    public Column calculateMD5() {

        Table table = this.getTable();

        String record = "";
        for (Column col : table) {
            if (col.isInculdeMd5() && col.hasValue()) {
                record += ",\"" + String.valueOf(col.getValue()) + "\"";
            }
        }
        record = record.substring(1);
        try {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            byte[] array = record.getBytes("UTF-8");
            md5.update(array);
            byte[] bdigest = md5.digest();
            String digest = DatatypeConverter.printHexBinary(bdigest);
            this.setValue(digest);
            System.out.println(record + "\n=>" + digest);

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            Logger.getLogger(MD5Column.class.getName()).log(Level.SEVERE, null, ex);
        }

        return this;
    }
}
