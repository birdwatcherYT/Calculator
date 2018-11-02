package calculator;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Javaによる簡単な計算機
 *
 */
public class Calculator {

    static CalculatorFrame cf;

    public static void main(String args[]) {
        cf = new CalculatorFrame();// CalculatorFrameオブジェクトの作成
        cf.setVisible(true);// ウィンドウの表示
    }
}

/**
 * 画面に関するクラス
 *
 */
class CalculatorFrame extends Frame implements ActionListener, ChangeListener, KeyListener, ItemListener {

    private static final HashMap<String, String> hash = new HashMap<String, String>() {// 演算子を表示するための変換用
        {
            put("none", " ");
            put("plus", "+");
            put("minus", "-");
            put("star", "*");
            put("slash", "/");
        }
    };
    private static final String FILE_NAME = "history.txt";// 履歴を保存するファイル名
    private static final int HISTORY_MAX = 50;// 履歴を保存する最大数
    private static final Dimension BUTTON_DIM = new Dimension(45, 45);// ボタンの大きさ指定用
    private static final Font BUTTON_FONT = new Font("Dialog", Font.PLAIN, 22);// ボタンのフォント用
    private static final Font TEXT_FONT = new Font("Dialog", Font.PLAIN, 15);// ラベルのフォント用

    Label text;// 計算結果や入力値の出力をするためのラベル
    Button button[];// 数字0-9用のボタンの配列
    Panel numberPanel;// 数字のボタンを配置するパネル
    Button clear, plus, minus, equal, point, star, slash, delete, negate, root;// 各種ボタン
    Panel commandPanel;// 演算記号などを配置するパネル
    String buffer;// 表示する文字列
    double result;// 演算結果を保存
    String operator;// 現在の演算子を保存
    boolean append;// 数字入力中かどうか(数字をくっつけるかどうか)
    boolean decimalFlag;// 10進数モードかどうか
    Button[] alphabet;// a-fのボタン
    Panel alphabetPanel;// a-fのボタンを配置するパネル
    JRadioButton decimalMode, hexMode;// 10進数,16進数モード変更用のラジオボタン
    ButtonGroup radioGroup;// ラジオボタンのグループ(ON,OFFを排他的にするため)
    Panel modePanel;// 10,16進数モード選択パネル
    Panel infoPanel;// 情報を配置するパネル
    Choice choice;// 履歴を保存する
    Label opLabel;// 現在のオペレータを表示させるラベル

    /**
     * コンストラクタ
     */
    CalculatorFrame() {
        /*----------初期化関連----------*/
        setTitle("Calculator");// タイトルの設定
        opLabel = new Label("");// 現在のオペレータ表示用ラベル
        initBuffer();// 表示する文字列の初期化
        initOperator();// 現在の演算子を初期化
        append = false;// 入力中でない
        decimalFlag = true;// 10進数モードにする
        text = new Label(buffer, Label.RIGHT);// 数字を表示するためのラベルを作成(右寄せ)
        text.setBackground(Color.white);// ラベルの背景を白にする
        text.setFont(TEXT_FONT);
        showBuffer();// バッファの表示
        result = Double.parseDouble(buffer);// 演算結果を初期化

        /*----------チョイス関連----------*/
        choice = new Choice();// 履歴保存用チョイスの作成
        choice.add("History");// "History"を追加
        choice.addItemListener(this);// ItemListenerに登録
        load();// 履歴ロード

        /*----------ラジオボタン関連----------*/
        radioGroup = new ButtonGroup();// ラジオボタングループの作成
        decimalMode = new JRadioButton("10進数モード", decimalFlag);// 10進数モードのラジオボタン
        hexMode = new JRadioButton("16進数モード");// 16進数モードのラジオボタン
        radioGroup.add(decimalMode);// ボタングループに10進モード追加
        radioGroup.add(hexMode);// ボタングループに16進モード追加
        decimalMode.addChangeListener(this);// 10進数モードをChangeListenerに追加
        hexMode.addChangeListener(this);// 16進数モードをChangeListenerに追加

        modePanel = new Panel();// モード変更用パネルを作成
        modePanel.setLayout(new GridLayout(1, 2));// 1 x 2 のレイアウト
        modePanel.add(decimalMode);// 10進数モードのラジオボタン追加
        modePanel.add(hexMode);// 16進数モードのラジオボタン追加

        /*----------コマンドボタン関連----------*/
        clear = new Button("C");// Clearボタンの作成
        plus = new Button("+");// +ボタンの作成
        minus = new Button("-");// -ボタンの作成
        equal = new Button("=");// =ボタンの作成
        point = new Button(".");// 小数点ボタンの作成
        star = new Button("*");// *ボタンの作成
        slash = new Button("/");// /ボタンの作成
        delete = new Button("←");// 1文字削除ボタンの作成
        negate = new Button("±");// プラスマイナス反転
        root = new Button("√");// ルートボタン作成

        clear.addActionListener(this);// アクションリスナーに登録
        plus.addActionListener(this);
        minus.addActionListener(this);
        equal.addActionListener(this);
        point.addActionListener(this);
        star.addActionListener(this);
        slash.addActionListener(this);
        delete.addActionListener(this);
        negate.addActionListener(this);
        root.addActionListener(this);

        commandPanel = new Panel(); // コマンドを扱うパネルの作成
        commandPanel.setLayout(new GridLayout(4, 1));// 4 x 1の格子上に配置するようにする
        commandPanel.add(clear);// Clearボタンの追加
        commandPanel.add(equal);// =ボタンの追加
        commandPanel.add(plus);// +ボタンの追加
        commandPanel.add(minus);// -ボタンの追加
        commandPanel.add(star);// *ボタンの追加
        commandPanel.add(slash);// /ボタンの追加
        commandPanel.add(negate);// ±ボタンの追加
        commandPanel.add(root);// √ボタンの追加

        clear.setPreferredSize(BUTTON_DIM);// GridLayoutに置く要素1つのサイズを変更すれば、同じパネル上の他のサイズも同じサイズになる

        clear.setFont(BUTTON_FONT);// フォントの設定
        plus.setFont(BUTTON_FONT);
        minus.setFont(BUTTON_FONT);
        equal.setFont(BUTTON_FONT);
        point.setFont(BUTTON_FONT);
        star.setFont(BUTTON_FONT);
        slash.setFont(BUTTON_FONT);
        delete.setFont(BUTTON_FONT);
        negate.setFont(BUTTON_FONT);
        root.setFont(BUTTON_FONT);

        /*----------数字ボタン関連----------*/
        numberPanel = new Panel();// 数字を並べるパネルの作成
        numberPanel.setLayout(new GridLayout(4, 3));// 4 x 3 の格子上に配置するようにする
        button = new Button[10];// 数字0-9のボタン
        for (int i = 0; i < 10; i++) {
            button[i] = new Button((new Integer(i)).toString());// 数字iのボタンのオブジェクトを作成
            button[i].addActionListener(this);// アクションリスナーに登録
            button[i].setPreferredSize(BUTTON_DIM);// サイズ指定
            button[i].setFont(BUTTON_FONT);// フォント指定
        }
        for (int i = 1; i < 10; ++i) {
            numberPanel.add(button[i]);// パネルに1-9のボタンを追加
        }
        numberPanel.add(button[0]); // パネルに0ボタンの追加
        numberPanel.add(point);// 小数点ボタンの追加
        numberPanel.add(delete);// 1文字削除ボタンの追加

        /*----------アルファベットボタン関連----------*/
        alphabetPanel = new Panel();// a-fのアルファベットを配置するパネル
        alphabetPanel.setLayout(new GridLayout(3, 2));// 3 x 2 の格子状に配置
        alphabetPanel.setVisible(!decimalFlag);
        alphabet = new Button[6];// アルファベットa-fのボタン
        for (int i = 0; i < 6; i++) {
            alphabet[i] = new Button((char) ('a' + i) + "");// アルファベットのボタンの作成
            alphabet[i].addActionListener(this);// アクションリスナーに登録
            alphabet[i].setPreferredSize(BUTTON_DIM);// サイズ指定
            alphabet[i].setFont(BUTTON_FONT);// フォント指定
            alphabetPanel.add(alphabet[i]);// パネルa-fのボタンを追加
        }

        /*----------情報パネル関連----------*/
        infoPanel = new Panel();// 情報パネル作成
        infoPanel.setLayout(new BorderLayout());
        infoPanel.add("North", modePanel);// 上にモードパネル追加
        infoPanel.add("Center", choice);// 中心にチョイス追加
        infoPanel.add("East", opLabel);// 右にオペレータの状態追加

        /*----------全体の表示関連----------*/
        setLayout(new BorderLayout());// レイアウトの指定
        add("North", text);// 上部にラベルを追加
        add("Center", numberPanel);// 真ん中に数字のパネルを追加
        add("East", commandPanel);// 右にコマンドパネルを追加
        add("South", infoPanel); // 下に情報パネルを追加
        add("West", alphabetPanel);// 左にアルファベットパネルを追加
        pack();// 内側のコンポーネントに合わせてサイズを決める

        /*----------閉じる処理----------*/
        addWindowListener(new WindowAdapter() {// ウィンドウリスナーへ登録
            public void windowClosing(WindowEvent e) {// xボタンが押されたとき
                save();// 履歴保存
                System.exit(0);// 終了する
            }
        });

        /*----------キー入力関連----------*/
        addKeyListener(this);// KeyListenerに登録
        setFocusable(true);// フォーカス状態にする (requestFocusでは起動時にボタンにフォーカスが当てられてしまう)
    }

    /**
     * Clearボタンの動作
     */
    void clearPressed() {
        initBuffer();
        initOperator();
        showBuffer();
        append = false;
    }

    /**
     * 演算子(+, -, *, /)ボタンの動作
     *
     * @param op
     */
    void operatorPressed(String op) {
        calculate();// 現在の演算子に従い、計算する
        setOperator(op);// オペレータを変更
        append = false; // 入力中(文字連結)フラグfalse
    }

    /**
     * =ボタンの動作
     */
    void equalPressed() {
        calculate();
        append = false;
        initOperator();
    }

    /**
     * .ボタンの動作
     */
    void pointPressed() {
        if (!append) {// 小数から入力が始まるとき
            buffer = "0.";
        } else if (!buffer.contains(".")) {// まだ小数点を含んでいないとき
            buffer = buffer + "."; // バッファの文字列に小数点追加
        }
        append = true;// 入力中とする
        showBuffer();// バッファを表示
    }

    /**
     * 削除ボタンの動作
     */
    void deletePressed() {
        if (append) {
            int length = buffer.length();// バッファの長さ取得
            if (length <= 1) {// 1以下なら
                buffer = "0";// 0にする
            } else {
                buffer = buffer.substring(0, length - 1);// 1文字削除
            }
            showBuffer();// バッファ再表示
        }
    }

    /**
     * 符号反転
     */
    void negatePressed() {
        if (!buffer.equals("NaN")) {
            switch (buffer.charAt(0)) {// 先頭の文字が
                case '+':// プラスのとき
                    buffer = "-" + buffer.substring(1);
                    break;
                case '-':// マイナスのとき
                    buffer = "+" + buffer.substring(1);
                    break;
                default:// その他
                    buffer = "-" + buffer;
            }
            showBuffer();// 再表示
        }
    }

    /**
     * 平方根演算
     */
    void rootPressed() {
        if (decimalFlag) {// 10進数の場合
            buffer = Double.toString(Math.sqrt(Double.parseDouble(buffer)));// 文字列をdoubleにし、平方根を求め、文字列に戻す
        } else {// 16進数の場合
            buffer = Hex.doubleToHex(Math.sqrt(Hex.hexToDouble(buffer)));// 16進数文字列をdoubleにし、平方根を求め、16進数文字列に戻す
        }
        append = false;// 入力中でない
        showBuffer();// バッファ再表示
        addHistory(buffer);// 履歴に追加
    }

    /**
     * 数字かa-fを入力したときの動作 (関係ない文字は無視される)
     *
     * @param ch 入力文字
     */
    void numPressed(char ch) {
        if (('0' <= ch && ch <= '9')// 数字のときか
                || (!decimalFlag && 'a' <= ch && ch <= 'f')) {// 16進数モードでアルファベットa-fのとき
            if (append) {// 入力中のとき
                buffer = buffer + ch;// バッファの文字列に押された数字を連結
            } else {
                buffer = ch + "";// バッファの文字列を押された数字にする
            }
            append = true;// 入力中とする
            showBuffer();// バッファを表示
        }
    }

    /**
     * ボタンのイベントに従い、処理をする
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();// アクションを起こしたオブジェクトを求める
        if (source == clear) {// クリアの場合
            clearPressed();
        } else if (source == plus) { // プラスの場合
            operatorPressed("plus");
        } else if (source == minus) { // マイナスの場合
            operatorPressed("minus");
        } else if (source == star) { // スター(かけ算)の場合
            operatorPressed("star");
        } else if (source == slash) { // スラッシュ(割り算)の場合
            operatorPressed("slash");
        } else if (source == equal) { // イコールの場合
            equalPressed();
        } else if (source == point) { // 小数点のとき
            pointPressed();
        } else if (source == delete) { // 1文字削除のとき
            deletePressed();
        } else if (source == negate) {// プラスマイナス反転のとき
            negatePressed();
        } else if (source == root) {// ルートのとき
            rootPressed();
        } else { // 0-9, a-f の場合
            numPressed(event.getActionCommand().charAt(0));
        }
        requestFocus();// フォーカス要求
    }

    /**
     * バッファの内容を表示させる
     */
    void showBuffer() {
        buffer = Hex.trim(buffer);// 先頭の無駄な0を削除
        text.setText(buffer);// textの文字列をbufferの文字列に変更
    }

    /**
     * 表示する文字列(バッファ)を0に初期化
     */
    void initBuffer() {
        buffer = "0";
    }

    /**
     * 現在の演算子をnoneに初期化
     */
    void initOperator() {
        operator = "none";
        opLabel.setText("op:" + hash.get(operator));// 現在のオペレータを表示
    }

    /**
     * 現在の演算子を別の演算子に変更
     */
    void setOperator(String operator) {
        this.operator = operator;
        opLabel.setText("op:" + hash.get(operator));// 現在のオペレータを表示
    }

    /**
     * 現在の演算子に従い、計算する
     */
    void calculate() {
        if (decimalFlag) {// 10進数モードの場合
            if (operator.equals("plus")) {
                result = result + Double.parseDouble(buffer);// バッファの内容を加える
                buffer = Double.toString(result);// 計算結果を文字列に変換
                showBuffer();// バッファの再表示
            } else if (operator.equals("minus")) {
                result = result - Double.parseDouble(buffer);// バッファの内容を引く
                buffer = Double.toString(result);// 計算結果を文字列に変換
                showBuffer();// バッファの再表示
            } else if (operator.equals("star")) {
                result = result * Double.parseDouble(buffer);// バッファの内容を掛ける
                buffer = Double.toString(result);// 計算結果を文字列に変換
                showBuffer();// バッファの再表示
            } else if (operator.equals("slash")) {
                result = result / Double.parseDouble(buffer);// バッファの内容で割る
                buffer = Double.toString(result);// 計算結果を文字列に変換
                showBuffer();// バッファの再表示
            } else {
                result = Double.parseDouble(buffer);// バッファの内容を数値に変換
            }
        } else// 16進数モードの場合
        {
            if (operator.equals("plus")) {
                result = result + Hex.hexToDouble(buffer);// バッファの内容を加える
                buffer = Hex.doubleToHex(result);// 計算結果を文字列に変換
                showBuffer();// バッファの再表示
            } else if (operator.equals("minus")) {
                result = result - Hex.hexToDouble(buffer);// バッファの内容を引く
                buffer = Hex.doubleToHex(result);// 計算結果を文字列に変換
                showBuffer();// バッファの再表示
            } else if (operator.equals("star")) {
                result = result * Hex.hexToDouble(buffer);// バッファの内容を掛ける
                buffer = Hex.doubleToHex(result);// 計算結果を文字列に変換
                showBuffer();// バッファの再表示
            } else if (operator.equals("slash")) {
                result = result / Hex.hexToDouble(buffer);// バッファの内容で割る
                buffer = Hex.doubleToHex(result);// 計算結果を文字列に変換
                showBuffer();// バッファの再表示
            } else {
                result = Hex.hexToDouble(buffer);// バッファの内容を数値に変換
            }
        }
        addHistory(buffer);// 履歴に追加
    }

    /**
     * 文字列を履歴に追加
     *
     * @param ans
     */
    void addHistory(String ans) {
        // -----文字列を末尾ではなく先頭に追加する-----
        ArrayList<String> list = new ArrayList<>();
        while (choice.getItemCount() > 1) {// "History"以外を削除するまで
            list.add(choice.getItem(1));// リストに追加
            choice.remove(1);// 削除
        }
        if (decimalFlag) {// 10進数モードの場合
            choice.add("(10)" + ans);// 先頭に(10)を付けて追加
        } else {// 16進数モードの場合
            choice.add("(16)" + ans);// 先頭に(16)を付けて追加
        }
        for (String str : list) {
            choice.add(str);// リストからチョイスに追加(復元)
            if (choice.getItemCount() > HISTORY_MAX) {
                break;
            }
        }
    }

    /**
     * 履歴保存
     */
    void save() {
        int itemNum = choice.getItemCount();// 現在の履歴数取得
        try {
            PrintWriter out = new PrintWriter(new FileWriter(FILE_NAME));// ファイルを開く
            for (int i = 1; i < itemNum; ++i) {
                out.println(choice.getItem(i));// 履歴を保存
            }
            out.close();// ファイルを閉じる
        } catch (IOException e) {
            System.err.println("saveエラー");
        }
    }

    /**
     * 履歴読み込み
     */
    void load() {
        try {
            BufferedReader in = new BufferedReader(new FileReader(FILE_NAME));// ファイルを開く
            for (String line = in.readLine(); line != null; line = in.readLine()) {// 1行ずつ読み込む
                choice.add(line);// チョイスに追加
            }
            in.close();// ファイルを閉じる
        } catch (FileNotFoundException e) {
            System.err.println("ファイルが存在しません");
        } catch (IOException e) {
            System.err.println("loadエラー");
        }
    }

    /**
     * ラジオボタンの状態が変化したときの動作
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        if (decimalFlag != decimalMode.isSelected()) {// 現在の状態と変化があれば
            decimalFlag = !decimalFlag;// モードを逆転
            alphabetPanel.setVisible(!decimalFlag);// アルファベットパネルをモードに従い、表示・非表示させる
            pack();// 画面サイズの調整
            if (decimalFlag) {// 10進数モードへ変化した場合
                buffer = Double.toString(Hex.hexToDouble(buffer));// バッファをdoubleにしてから、10進数文字列へ変換
            } else {// 16進数モードへ変化した場合
                buffer = Hex.doubleToHex(Double.parseDouble(buffer));// バッファをdoubleにしてから、16進数文字列へ変換
            }
            showBuffer();// バッファの再表示
        }
        requestFocus();// フォーカスの要求
    }

    /**
     * キーが押されたときの動作
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();// キーに関連付けられた値を取得
        if (code == KeyEvent.VK_ESCAPE) {// クリアの場合
            clearPressed();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    /**
     * 文字キーが押されたときの動作
     */
    @Override
    public void keyTyped(KeyEvent e) {
        char ch = e.getKeyChar();// 入力された文字を取得
        if (ch == '+') { // プラスの場合
            operatorPressed("plus");
        } else if (ch == '-') { // マイナスの場合
            operatorPressed("minus");
        } else if (ch == '*') { // スター(かけ算)の場合
            operatorPressed("star");
        } else if (ch == '/') { // スラッシュ(割り算)の場合
            operatorPressed("slash");
        } else if (ch == '=' || ch == '\n') { // イコールの場合
            equalPressed();
        } else if (ch == '.') { // 小数点のとき
            pointPressed();
        } else if (ch == '\b') { // 1文字削除のとき
            deletePressed();
        } else {// その他のとき
            numPressed(ch);
        }
    }

    /**
     * 履歴(チョイス)が選択されたときの動作
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        String str = choice.getSelectedItem();// 選択されたアイテムの文字列を取得
        if (!"History".equals(str)) {// Historyでないとき
            String mode = str.substring(0, 4);// 何進数で記録されたものかを取得
            if (mode.equals("(10)") == decimalFlag) {// 現在の進数と同じなら
                buffer = str.substring(4);// そのまま表示
            } else if (decimalFlag) {// モードが違い、現在10進数モードなら
                buffer = Double.toString(Hex.hexToDouble(str.substring(4)));// 16進数文字列をdoubleに変換し、文字列に変換
            } else {// モードが違い、現在16進数モードなら
                buffer = Hex.doubleToHex(Double.parseDouble(str.substring(4)));// 10進数文字列をdoubleに変換し、16進数文字列に変換
            }
            showBuffer();// バッファの再表示
        }
        requestFocus();// フォーカス要求
    }
}
