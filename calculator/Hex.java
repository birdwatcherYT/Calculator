package calculator;

/**
 * 16�i����ϊ�����N���X
 */
public class Hex {

    private static final String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e",
        "f"};// 16�i��0-9,a-f
    public static final double EPS = 1.0e-50;// ���e�덷
    public static final int MAX = 50;// �����_�ő包��
    public static final double EXP_VIEW_BOUND_LARGE = 1e12;// �w���\���ɂ��鋫�E(�傫����)
    public static final double EXP_VIEW_BOUND_SMALL = 1e-6;// �w���\���ɂ��鋫�E(��������)

    /**
     * �w���\���̏����𖞂������ǂ���
     *
     * @param dbl double�^����
     * @return ��������true
     */
    public static boolean isExpView(double dbl) {
        double abs = Math.abs(dbl);// ��Βl
        return (EXP_VIEW_BOUND_LARGE < abs || (0 < abs && abs < EXP_VIEW_BOUND_SMALL) || Double.isNaN(abs));
    }

    /**
     * double�^����16�i���������
     *
     * @param dbl double�^
     * @return 16�i��������
     */
    public static String doubleToHex(double dbl) {
        if (isExpView(dbl)) {// �w���\���Ώۂ̂Ƃ�
            return Double.toHexString(dbl).replace("0x", "").replace('p', 'P');
        }
        String hexStr = "", sign = (dbl < 0) ? "-" : "";// 16�i��������, ����
        double abs = Math.abs(dbl);// ��Βl

        long integer = (long) abs;// ��������
        double decimal = abs - integer;// ��������

        do {// �������������߂�
            hexStr = hex[(int) (integer % 16)] + hexStr; // 16�i���֕ϊ�
            integer /= 16;
        } while (integer != 0);

        if (Math.abs(decimal) > EPS) {// ��������������Ȃ�
            hexStr += ".";
        }

        for (int i = 0; Math.abs(decimal) > EPS && i < MAX; ++i) {// ���e�덷�ȉ��ɂȂ�܂�orMAX���ɂȂ�܂�
            double temp = decimal * 16;
            hexStr += hex[(int) temp];// 16�i���֕ϊ�
            decimal = temp - (int) temp;// �������������߂�
        }
        return sign + hexStr;// ������t���ĕԂ�
    }

    /**
     * 16�i�������񂩂�double��
     *
     * @param hexString
     * @return
     */
    public static double hexToDouble(String hexString) {
        if (hexString.equals("NaN")) {// NaN�̎�
            return Double.NaN;
        } else if (hexString.equals("Infinity")) {// ���̖�����̂Ƃ�
            return Double.POSITIVE_INFINITY;
        } else if (hexString.equals("-Infinity")) {// ���̖�����̂Ƃ�
            return Double.NEGATIVE_INFINITY;
        }

        String str = hexString.toLowerCase().replace("0x", "");// 16�i����������i�[
        String intStr, dcmlStr;// ���������A���������̕�����
        int index;
        int pow = 0, sign = +1;// �w�������A�����̋L�^

        if (str.charAt(0) == '+') {// ���̎�
            str = str.substring(1);// ��������
        } else if (str.charAt(0) == '-') {// ���̎�
            sign = -1;// ���ɂ���
            str = str.substring(1);// ��������
        }

        if ((index = str.indexOf('p')) >= 0) { // �w��������Ƃ�
            pow = Integer.parseInt(str.substring(index + 1));// �w�������̐������擾
            str = str.substring(0, index);// �w����������
        }

        if ((index = str.indexOf('.')) >= 0) {// �����_������Ƃ�
            intStr = str.substring(0, index);// ��������
            dcmlStr = str.substring(index + 1);// ��������
        } else {// �����_�������Ƃ�
            intStr = str;// ���ׂĐ���
            dcmlStr = "";// ���������Ȃ�
        }

        double integer = 0; // ��������
        for (char ch : intStr.toCharArray()) {// �������������߂�
            if ('0' <= ch && ch <= '9') {// �����̏ꍇ
                integer = integer * 16 + (ch - '0');
            } else if ('a' <= ch && ch <= 'f') {// a-f�̏ꍇ
                integer = integer * 16 + (ch - 'a' + 10);
            }
        }

        double decimal = 0; // ��������
        for (int i = dcmlStr.length() - 1; i >= 0; --i) {// 16�i��������̌�납�珬�����������߂�
            char ch = dcmlStr.charAt(i);// i�Ԗڂ̕����擾
            if ('0' <= ch && ch <= '9') {// �����̎�
                decimal = decimal / 16 + (ch - '0');
            } else if ('a' <= ch && ch <= 'f') {// a-f�̎�
                decimal = decimal / 16 + (ch - 'a' + 10);
            }
        }
        decimal /= 16;

        return sign * (decimal + integer) * Math.pow(2, pow);// �����\����Ԃ�
    }

    /**
     * �擪��0���폜����(�����������Ă�OK�A���i���ł�OK)
     *
     * @param numStr N�i��������
     * @return �擪��0���폜����������
     */
    public static String trim(String numStr) {
        String str = numStr, sign = "";

        if (str.charAt(0) == '+') {// ���̎�
            str = str.substring(1);// ��������
        } else if (str.charAt(0) == '-') {// ���̎�
            sign = "-";// ���ɂ���
            str = str.substring(1);// ��������
        }

        int i, length = str.length();// �����擾
        for (i = 0; i < length && str.charAt(i) == '0'; ++i) {// 0�������ԃ��[�v
        }

        if (i == length) {// 0�����Ȃ��Ƃ�
            str = "0";
        } else if (str.charAt(i) == '.') {// �����_�܂ŗ����Ƃ��A��������0
            str = sign + "0" + str.substring(i);
        } else {
            str = sign + str.substring(i);
        }
        return str;
    }
}
