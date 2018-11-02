package calculator;

/**
 * 16進数を変換するクラス
 */
public class Hex {

    private static final String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e",
        "f"};// 16進数0-9,a-f
    public static final double EPS = 1.0e-50;// 許容誤差
    public static final int MAX = 50;// 小数点最大桁数
    public static final double EXP_VIEW_BOUND_LARGE = 1e12;// 指数表示にする境界(大きい方)
    public static final double EXP_VIEW_BOUND_SMALL = 1e-6;// 指数表示にする境界(小さい方)

    /**
     * 指数表示の条件を満たすかどうか
     *
     * @param dbl double型実数
     * @return 満たす時true
     */
    public static boolean isExpView(double dbl) {
        double abs = Math.abs(dbl);// 絶対値
        return (EXP_VIEW_BOUND_LARGE < abs || (0 < abs && abs < EXP_VIEW_BOUND_SMALL) || Double.isNaN(abs));
    }

    /**
     * double型から16進数文字列へ
     *
     * @param dbl double型
     * @return 16進数文字列
     */
    public static String doubleToHex(double dbl) {
        if (isExpView(dbl)) {// 指数表示対象のとき
            return Double.toHexString(dbl).replace("0x", "").replace('p', 'P');
        }
        String hexStr = "", sign = (dbl < 0) ? "-" : "";// 16進数文字列, 符号
        double abs = Math.abs(dbl);// 絶対値

        long integer = (long) abs;// 整数部分
        double decimal = abs - integer;// 小数部分

        do {// 整数部分を求める
            hexStr = hex[(int) (integer % 16)] + hexStr; // 16進数へ変換
            integer /= 16;
        } while (integer != 0);

        if (Math.abs(decimal) > EPS) {// 小数部分があるなら
            hexStr += ".";
        }

        for (int i = 0; Math.abs(decimal) > EPS && i < MAX; ++i) {// 許容誤差以下になるまでorMAX桁になるまで
            double temp = decimal * 16;
            hexStr += hex[(int) temp];// 16進数へ変換
            decimal = temp - (int) temp;// 小数部分を求める
        }
        return sign + hexStr;// 符号を付けて返す
    }

    /**
     * 16進数文字列からdoubleへ
     *
     * @param hexString
     * @return
     */
    public static double hexToDouble(String hexString) {
        if (hexString.equals("NaN")) {// NaNの時
            return Double.NaN;
        } else if (hexString.equals("Infinity")) {// 正の無限大のとき
            return Double.POSITIVE_INFINITY;
        } else if (hexString.equals("-Infinity")) {// 負の無限大のとき
            return Double.NEGATIVE_INFINITY;
        }

        String str = hexString.toLowerCase().replace("0x", "");// 16進数文字列を格納
        String intStr, dcmlStr;// 整数部分、小数部分の文字列
        int index;
        int pow = 0, sign = +1;// 指数部分、符号の記録

        if (str.charAt(0) == '+') {// 正の時
            str = str.substring(1);// 符号除去
        } else if (str.charAt(0) == '-') {// 負の時
            sign = -1;// 負にする
            str = str.substring(1);// 符号除去
        }

        if ((index = str.indexOf('p')) >= 0) { // 指数があるとき
            pow = Integer.parseInt(str.substring(index + 1));// 指数部分の数字を取得
            str = str.substring(0, index);// 指数部分除去
        }

        if ((index = str.indexOf('.')) >= 0) {// 小数点があるとき
            intStr = str.substring(0, index);// 整数部分
            dcmlStr = str.substring(index + 1);// 小数部分
        } else {// 小数点が無いとき
            intStr = str;// すべて整数
            dcmlStr = "";// 小数部分なし
        }

        double integer = 0; // 整数部分
        for (char ch : intStr.toCharArray()) {// 整数部分を求める
            if ('0' <= ch && ch <= '9') {// 数字の場合
                integer = integer * 16 + (ch - '0');
            } else if ('a' <= ch && ch <= 'f') {// a-fの場合
                integer = integer * 16 + (ch - 'a' + 10);
            }
        }

        double decimal = 0; // 小数部分
        for (int i = dcmlStr.length() - 1; i >= 0; --i) {// 16進数文字列の後ろから小数部分を求める
            char ch = dcmlStr.charAt(i);// i番目の文字取得
            if ('0' <= ch && ch <= '9') {// 数字の時
                decimal = decimal / 16 + (ch - '0');
            } else if ('a' <= ch && ch <= 'f') {// a-fの時
                decimal = decimal / 16 + (ch - 'a' + 10);
            }
        }
        decimal /= 16;

        return sign * (decimal + integer) * Math.pow(2, pow);// 実数表現を返す
    }

    /**
     * 先頭の0を削除する(符号があってもOK、何進数でもOK)
     *
     * @param numStr N進数文字列
     * @return 先頭の0を削除した文字列
     */
    public static String trim(String numStr) {
        String str = numStr, sign = "";

        if (str.charAt(0) == '+') {// 正の時
            str = str.substring(1);// 符号除去
        } else if (str.charAt(0) == '-') {// 負の時
            sign = "-";// 負にする
            str = str.substring(1);// 符号除去
        }

        int i, length = str.length();// 長さ取得
        for (i = 0; i < length && str.charAt(i) == '0'; ++i) {// 0が続く間ループ
        }

        if (i == length) {// 0しかないとき
            str = "0";
        } else if (str.charAt(i) == '.') {// 小数点まで来たとき、整数部は0
            str = sign + "0" + str.substring(i);
        } else {
            str = sign + str.substring(i);
        }
        return str;
    }
}
