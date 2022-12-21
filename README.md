## SQLマイグレータ

カラムの論理名と物理名、データ構造、リレーションをプロセスごとに考えながらコーディングするのが
かったるいので、まとめて扱えるようなラッパークラスを作っている。

#### 機能1: Javaでテーブルを定義したら、SQLのクリエイト文を書かなくてもよくなる。

<pre>
//テーブル定義
class ExtTable extends Table {

  // カラムはJavaのコーディングルールを逸脱してpublicで実装し、カプセル化しない。
  // カラムの初期化はクラスをインスタンス化するときに自動で行うので、宣言だけ行う。

  @LogicalName("論理名")　//論理名を宣言すると、カラムを論理名としてデータ取得出来るようになる。
  public IncrementalKey tableColumn1; // 事前定義済みのColumn (IncrementalKey は自動ナンバリング)

  public Column<Integer> tableColumn2;

  public Column<Stirng> tableColumn3;

  //defineColumnでテーブルの属性（Primary Key, サイズ、Null許可、規定値、リレーション）を定義する。
  @Override
  public defineColumns(){

    this.tableColumn3.setSize(1024)
      .allowNull(false)
      .defualt("NOT ENTRIED");

    this.tableColumn2.addRelathionWith(OTEHR_TABLECLASS); // 外部参照の定義

  }

}
</pre>

以下のメソッドで、DB上にテーブルを作成する

<pre>
ExtTable extTable = new ExtTable();
extTable.alterOrCreateTable(supplier);　// JdbcSupplierを登録
</pre>

alterOrCreateTableで、supplierに格納されたDbのアクセス情報を使ってテーブルをサーバー上に作成する。

なお、すでにサーバー上にテーブルがある場合は、テーブル定義とサーバーのテーブルを比較し、カラムの追加、サイズの拡大のみ行う。

このとき、危険回避のためカラムがクラス定義で削除されていても、サーバー上のカラムは削除しない。

カラムの型が変更されていた場合のチェックは未実装（将来課題）。


#### 機能2: 以下のように、SQL構文を可能な限り使わないでデータ抽出を可能にする。ただし、必要とあれば超絶技巧を凝らしたＳＱＬも投げれる。

<pre>
// selectの条件はandのみをサポートする。

TableExtends table = new TableExtends();
table.setJdbcSupplier(supplier);

ResultSet rs = table.select(
    tableColumn1.sameValueOf(valueOf1), //抽出条件を複数指定できる
    tableColumn2.sameValueOf(valueOf2)
);

String colValue3 = table.TableColumne3.of(rs);
</pre>

外部参照を定義してあれば、外部テーブルを抽出キーに利用可能

<pre>
ResultSet rs = table.select(
    tableColumn1.sameValueOf(valueOf1), //抽出条件を複数指定できる
    otehrTable.tableColumn2.sameValueOf(valueOf2),
);
</pre>

マージやインサート、アップデートもサポートする

以下の処理で、tableColumn1 = SOMETHING1 の値をとるレコードのtableColumn2, tableColumn3を更新する。

<pre>
table.clearValues(); //カラムに保存した値をクリア
table.tableColumn2.setValue(SOMETHING2);
table.tableColumn3.setValue("FOO VAR");

table.update(
 table.tableColumn1.sameValueOf(SOMETHING1) //ここの引数は、update文のwhere句に該当する。
);
</pre>

複雑なSQLも以下のように構文を直接投げることが可能。
<pre>
ResultSet rs = table.select(
  "-- 複雑なSQL" +
  " select foo, var from table " +
  " left join otertable on key1 = key2 " +
  " right join othertable on key3 = key4 " +
  " where fieldA like 'where status' "" 
);
</pre>
ただし、テーブル定義を変更した際に、このような処理があると全部追従して訂正しないといけないので、
多用しないことを推奨する。

#### 機能3: H2DBの起動、終了、テーブル情報、カラム情報を自動格納する仕組み

TableInfoテーブルとColumnInfoテーブルを自動的に生成するので、Table.alterOrCreate()でSQLサーバに作成する際に、
自動的にテーブル情報と絡む情報を記録する。

記録する際に、アノテーションで記述した情報をそれぞれ抽出して登録するので、テーブル情報の管理が簡単になる。（カラムを廃止した際の記録の取扱いは将来課題）

<pre>
@TinyTable("TRANSLATE") //Springの @Autowired +  @Qualifier("TRANSLATE")でインジェクションするためにつける。
@LogicalName("翻訳") // TableInfoに格納される論理名称
@Comment("言語別にテキスト文を格納する.") // TableInfoに格納される備考
public class Translate extends Table {
 :
 :
}
</pre>

#### 機能4: SpringBoot内で利用可能
##### テーブル

新規にテーブルを定義する際に、アノテーション　@ClearfyTable("テーブル名称")をつけることで、Injectionに対応する。
Injectionでマッピングする変数には、変数に@Autowiredと@Qualifier("テーブル名称")をつける。

##### JDBC
JDBCをSpringBootで使用する際は、@Autowiredでマッピングを行う。
