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
 * Java�ɂ��ȒP�Ȍv�Z�@
 *
 */
public class Calculator {

    static CalculatorFrame cf;

    public static void main(String args[]) {
        cf = new CalculatorFrame();// CalculatorFrame�I�u�W�F�N�g�̍쐬
        cf.setVisible(true);// �E�B���h�E�̕\��
    }
}

/**
 * ��ʂɊւ���N���X
 *
 */
class CalculatorFrame extends Frame implements ActionListener, ChangeListener, KeyListener, ItemListener {

    private static final HashMap<String, String> hash = new HashMap<String, String>() {// ���Z�q��\�����邽�߂̕ϊ��p
        {
            put("none", " ");
            put("plus", "+");
            put("minus", "-");
            put("star", "*");
            put("slash", "/");
        }
    };
    private static final String FILE_NAME = "history.txt";// ������ۑ�����t�@�C����
    private static final int HISTORY_MAX = 50;// ������ۑ�����ő吔
    private static final Dimension BUTTON_DIM = new Dimension(45, 45);// �{�^���̑傫���w��p
    private static final Font BUTTON_FONT = new Font("Dialog", Font.PLAIN, 22);// �{�^���̃t�H���g�p
    private static final Font TEXT_FONT = new Font("Dialog", Font.PLAIN, 15);// ���x���̃t�H���g�p

    Label text;// �v�Z���ʂ���͒l�̏o�͂����邽�߂̃��x��
    Button button[];// ����0-9�p�̃{�^���̔z��
    Panel numberPanel;// �����̃{�^����z�u����p�l��
    Button clear, plus, minus, equal, point, star, slash, delete, negate, root;// �e��{�^��
    Panel commandPanel;// ���Z�L���Ȃǂ�z�u����p�l��
    String buffer;// �\�����镶����
    double result;// ���Z���ʂ�ۑ�
    String operator;// ���݂̉��Z�q��ۑ�
    boolean append;// �������͒����ǂ���(�������������邩�ǂ���)
    boolean decimalFlag;// 10�i�����[�h���ǂ���
    Button[] alphabet;// a-f�̃{�^��
    Panel alphabetPanel;// a-f�̃{�^����z�u����p�l��
    JRadioButton decimalMode, hexMode;// 10�i��,16�i�����[�h�ύX�p�̃��W�I�{�^��
    ButtonGroup radioGroup;// ���W�I�{�^���̃O���[�v(ON,OFF��r���I�ɂ��邽��)
    Panel modePanel;// 10,16�i�����[�h�I���p�l��
    Panel infoPanel;// ����z�u����p�l��
    Choice choice;// ������ۑ�����
    Label opLabel;// ���݂̃I�y���[�^��\�������郉�x��

    /**
     * �R���X�g���N�^
     */
    CalculatorFrame() {
        /*----------�������֘A----------*/
        setTitle("Calculator");// �^�C�g���̐ݒ�
        opLabel = new Label("");// ���݂̃I�y���[�^�\���p���x��
        initBuffer();// �\�����镶����̏�����
        initOperator();// ���݂̉��Z�q��������
        append = false;// ���͒��łȂ�
        decimalFlag = true;// 10�i�����[�h�ɂ���
        text = new Label(buffer, Label.RIGHT);// ������\�����邽�߂̃��x�����쐬(�E��)
        text.setBackground(Color.white);// ���x���̔w�i�𔒂ɂ���
        text.setFont(TEXT_FONT);
        showBuffer();// �o�b�t�@�̕\��
        result = Double.parseDouble(buffer);// ���Z���ʂ�������

        /*----------�`���C�X�֘A----------*/
        choice = new Choice();// ����ۑ��p�`���C�X�̍쐬
        choice.add("History");// "History"��ǉ�
        choice.addItemListener(this);// ItemListener�ɓo�^
        load();// �������[�h

        /*----------���W�I�{�^���֘A----------*/
        radioGroup = new ButtonGroup();// ���W�I�{�^���O���[�v�̍쐬
        decimalMode = new JRadioButton("10�i�����[�h", decimalFlag);// 10�i�����[�h�̃��W�I�{�^��
        hexMode = new JRadioButton("16�i�����[�h");// 16�i�����[�h�̃��W�I�{�^��
        radioGroup.add(decimalMode);// �{�^���O���[�v��10�i���[�h�ǉ�
        radioGroup.add(hexMode);// �{�^���O���[�v��16�i���[�h�ǉ�
        decimalMode.addChangeListener(this);// 10�i�����[�h��ChangeListener�ɒǉ�
        hexMode.addChangeListener(this);// 16�i�����[�h��ChangeListener�ɒǉ�

        modePanel = new Panel();// ���[�h�ύX�p�p�l�����쐬
        modePanel.setLayout(new GridLayout(1, 2));// 1 x 2 �̃��C�A�E�g
        modePanel.add(decimalMode);// 10�i�����[�h�̃��W�I�{�^���ǉ�
        modePanel.add(hexMode);// 16�i�����[�h�̃��W�I�{�^���ǉ�

        /*----------�R�}���h�{�^���֘A----------*/
        clear = new Button("C");// Clear�{�^���̍쐬
        plus = new Button("+");// +�{�^���̍쐬
        minus = new Button("-");// -�{�^���̍쐬
        equal = new Button("=");// =�{�^���̍쐬
        point = new Button(".");// �����_�{�^���̍쐬
        star = new Button("*");// *�{�^���̍쐬
        slash = new Button("/");// /�{�^���̍쐬
        delete = new Button("��");// 1�����폜�{�^���̍쐬
        negate = new Button("�}");// �v���X�}�C�i�X���]
        root = new Button("��");// ���[�g�{�^���쐬

        clear.addActionListener(this);// �A�N�V�������X�i�[�ɓo�^
        plus.addActionListener(this);
        minus.addActionListener(this);
        equal.addActionListener(this);
        point.addActionListener(this);
        star.addActionListener(this);
        slash.addActionListener(this);
        delete.addActionListener(this);
        negate.addActionListener(this);
        root.addActionListener(this);

        commandPanel = new Panel(); // �R�}���h�������p�l���̍쐬
        commandPanel.setLayout(new GridLayout(4, 1));// 4 x 1�̊i�q��ɔz�u����悤�ɂ���
        commandPanel.add(clear);// Clear�{�^���̒ǉ�
        commandPanel.add(equal);// =�{�^���̒ǉ�
        commandPanel.add(plus);// +�{�^���̒ǉ�
        commandPanel.add(minus);// -�{�^���̒ǉ�
        commandPanel.add(star);// *�{�^���̒ǉ�
        commandPanel.add(slash);// /�{�^���̒ǉ�
        commandPanel.add(negate);// �}�{�^���̒ǉ�
        commandPanel.add(root);// ��{�^���̒ǉ�

        clear.setPreferredSize(BUTTON_DIM);// GridLayout�ɒu���v�f1�̃T�C�Y��ύX����΁A�����p�l����̑��̃T�C�Y�������T�C�Y�ɂȂ�

        clear.setFont(BUTTON_FONT);// �t�H���g�̐ݒ�
        plus.setFont(BUTTON_FONT);
        minus.setFont(BUTTON_FONT);
        equal.setFont(BUTTON_FONT);
        point.setFont(BUTTON_FONT);
        star.setFont(BUTTON_FONT);
        slash.setFont(BUTTON_FONT);
        delete.setFont(BUTTON_FONT);
        negate.setFont(BUTTON_FONT);
        root.setFont(BUTTON_FONT);

        /*----------�����{�^���֘A----------*/
        numberPanel = new Panel();// ��������ׂ�p�l���̍쐬
        numberPanel.setLayout(new GridLayout(4, 3));// 4 x 3 �̊i�q��ɔz�u����悤�ɂ���
        button = new Button[10];// ����0-9�̃{�^��
        for (int i = 0; i < 10; i++) {
            button[i] = new Button((new Integer(i)).toString());// ����i�̃{�^���̃I�u�W�F�N�g���쐬
            button[i].addActionListener(this);// �A�N�V�������X�i�[�ɓo�^
            button[i].setPreferredSize(BUTTON_DIM);// �T�C�Y�w��
            button[i].setFont(BUTTON_FONT);// �t�H���g�w��
        }
        for (int i = 1; i < 10; ++i) {
            numberPanel.add(button[i]);// �p�l����1-9�̃{�^����ǉ�
        }
        numberPanel.add(button[0]); // �p�l����0�{�^���̒ǉ�
        numberPanel.add(point);// �����_�{�^���̒ǉ�
        numberPanel.add(delete);// 1�����폜�{�^���̒ǉ�

        /*----------�A���t�@�x�b�g�{�^���֘A----------*/
        alphabetPanel = new Panel();// a-f�̃A���t�@�x�b�g��z�u����p�l��
        alphabetPanel.setLayout(new GridLayout(3, 2));// 3 x 2 �̊i�q��ɔz�u
        alphabetPanel.setVisible(!decimalFlag);
        alphabet = new Button[6];// �A���t�@�x�b�ga-f�̃{�^��
        for (int i = 0; i < 6; i++) {
            alphabet[i] = new Button((char) ('a' + i) + "");// �A���t�@�x�b�g�̃{�^���̍쐬
            alphabet[i].addActionListener(this);// �A�N�V�������X�i�[�ɓo�^
            alphabet[i].setPreferredSize(BUTTON_DIM);// �T�C�Y�w��
            alphabet[i].setFont(BUTTON_FONT);// �t�H���g�w��
            alphabetPanel.add(alphabet[i]);// �p�l��a-f�̃{�^����ǉ�
        }

        /*----------���p�l���֘A----------*/
        infoPanel = new Panel();// ���p�l���쐬
        infoPanel.setLayout(new BorderLayout());
        infoPanel.add("North", modePanel);// ��Ƀ��[�h�p�l���ǉ�
        infoPanel.add("Center", choice);// ���S�Ƀ`���C�X�ǉ�
        infoPanel.add("East", opLabel);// �E�ɃI�y���[�^�̏�Ԓǉ�

        /*----------�S�̂̕\���֘A----------*/
        setLayout(new BorderLayout());// ���C�A�E�g�̎w��
        add("North", text);// �㕔�Ƀ��x����ǉ�
        add("Center", numberPanel);// �^�񒆂ɐ����̃p�l����ǉ�
        add("East", commandPanel);// �E�ɃR�}���h�p�l����ǉ�
        add("South", infoPanel); // ���ɏ��p�l����ǉ�
        add("West", alphabetPanel);// ���ɃA���t�@�x�b�g�p�l����ǉ�
        pack();// �����̃R���|�[�l���g�ɍ��킹�ăT�C�Y�����߂�

        /*----------���鏈��----------*/
        addWindowListener(new WindowAdapter() {// �E�B���h�E���X�i�[�֓o�^
            public void windowClosing(WindowEvent e) {// x�{�^���������ꂽ�Ƃ�
                save();// ����ۑ�
                System.exit(0);// �I������
            }
        });

        /*----------�L�[���͊֘A----------*/
        addKeyListener(this);// KeyListener�ɓo�^
        setFocusable(true);// �t�H�[�J�X��Ԃɂ��� (requestFocus�ł͋N�����Ƀ{�^���Ƀt�H�[�J�X�����Ă��Ă��܂�)
    }

    /**
     * Clear�{�^���̓���
     */
    void clearPressed() {
        initBuffer();
        initOperator();
        showBuffer();
        append = false;
    }

    /**
     * ���Z�q(+, -, *, /)�{�^���̓���
     *
     * @param op
     */
    void operatorPressed(String op) {
        calculate();// ���݂̉��Z�q�ɏ]���A�v�Z����
        setOperator(op);// �I�y���[�^��ύX
        append = false; // ���͒�(�����A��)�t���Ofalse
    }

    /**
     * =�{�^���̓���
     */
    void equalPressed() {
        calculate();
        append = false;
        initOperator();
    }

    /**
     * .�{�^���̓���
     */
    void pointPressed() {
        if (!append) {// ����������͂��n�܂�Ƃ�
            buffer = "0.";
        } else if (!buffer.contains(".")) {// �܂������_���܂�ł��Ȃ��Ƃ�
            buffer = buffer + "."; // �o�b�t�@�̕�����ɏ����_�ǉ�
        }
        append = true;// ���͒��Ƃ���
        showBuffer();// �o�b�t�@��\��
    }

    /**
     * �폜�{�^���̓���
     */
    void deletePressed() {
        if (append) {
            int length = buffer.length();// �o�b�t�@�̒����擾
            if (length <= 1) {// 1�ȉ��Ȃ�
                buffer = "0";// 0�ɂ���
            } else {
                buffer = buffer.substring(0, length - 1);// 1�����폜
            }
            showBuffer();// �o�b�t�@�ĕ\��
        }
    }

    /**
     * �������]
     */
    void negatePressed() {
        if (!buffer.equals("NaN")) {
            switch (buffer.charAt(0)) {// �擪�̕�����
                case '+':// �v���X�̂Ƃ�
                    buffer = "-" + buffer.substring(1);
                    break;
                case '-':// �}�C�i�X�̂Ƃ�
                    buffer = "+" + buffer.substring(1);
                    break;
                default:// ���̑�
                    buffer = "-" + buffer;
            }
            showBuffer();// �ĕ\��
        }
    }

    /**
     * ���������Z
     */
    void rootPressed() {
        if (decimalFlag) {// 10�i���̏ꍇ
            buffer = Double.toString(Math.sqrt(Double.parseDouble(buffer)));// �������double�ɂ��A�����������߁A������ɖ߂�
        } else {// 16�i���̏ꍇ
            buffer = Hex.doubleToHex(Math.sqrt(Hex.hexToDouble(buffer)));// 16�i���������double�ɂ��A�����������߁A16�i��������ɖ߂�
        }
        append = false;// ���͒��łȂ�
        showBuffer();// �o�b�t�@�ĕ\��
        addHistory(buffer);// �����ɒǉ�
    }

    /**
     * ������a-f����͂����Ƃ��̓��� (�֌W�Ȃ������͖��������)
     *
     * @param ch ���͕���
     */
    void numPressed(char ch) {
        if (('0' <= ch && ch <= '9')// �����̂Ƃ���
                || (!decimalFlag && 'a' <= ch && ch <= 'f')) {// 16�i�����[�h�ŃA���t�@�x�b�ga-f�̂Ƃ�
            if (append) {// ���͒��̂Ƃ�
                buffer = buffer + ch;// �o�b�t�@�̕�����ɉ����ꂽ������A��
            } else {
                buffer = ch + "";// �o�b�t�@�̕�����������ꂽ�����ɂ���
            }
            append = true;// ���͒��Ƃ���
            showBuffer();// �o�b�t�@��\��
        }
    }

    /**
     * �{�^���̃C�x���g�ɏ]���A����������
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();// �A�N�V�������N�������I�u�W�F�N�g�����߂�
        if (source == clear) {// �N���A�̏ꍇ
            clearPressed();
        } else if (source == plus) { // �v���X�̏ꍇ
            operatorPressed("plus");
        } else if (source == minus) { // �}�C�i�X�̏ꍇ
            operatorPressed("minus");
        } else if (source == star) { // �X�^�[(�����Z)�̏ꍇ
            operatorPressed("star");
        } else if (source == slash) { // �X���b�V��(����Z)�̏ꍇ
            operatorPressed("slash");
        } else if (source == equal) { // �C�R�[���̏ꍇ
            equalPressed();
        } else if (source == point) { // �����_�̂Ƃ�
            pointPressed();
        } else if (source == delete) { // 1�����폜�̂Ƃ�
            deletePressed();
        } else if (source == negate) {// �v���X�}�C�i�X���]�̂Ƃ�
            negatePressed();
        } else if (source == root) {// ���[�g�̂Ƃ�
            rootPressed();
        } else { // 0-9, a-f �̏ꍇ
            numPressed(event.getActionCommand().charAt(0));
        }
        requestFocus();// �t�H�[�J�X�v��
    }

    /**
     * �o�b�t�@�̓��e��\��������
     */
    void showBuffer() {
        buffer = Hex.trim(buffer);// �擪�̖��ʂ�0���폜
        text.setText(buffer);// text�̕������buffer�̕�����ɕύX
    }

    /**
     * �\�����镶����(�o�b�t�@)��0�ɏ�����
     */
    void initBuffer() {
        buffer = "0";
    }

    /**
     * ���݂̉��Z�q��none�ɏ�����
     */
    void initOperator() {
        operator = "none";
        opLabel.setText("op:" + hash.get(operator));// ���݂̃I�y���[�^��\��
    }

    /**
     * ���݂̉��Z�q��ʂ̉��Z�q�ɕύX
     */
    void setOperator(String operator) {
        this.operator = operator;
        opLabel.setText("op:" + hash.get(operator));// ���݂̃I�y���[�^��\��
    }

    /**
     * ���݂̉��Z�q�ɏ]���A�v�Z����
     */
    void calculate() {
        if (decimalFlag) {// 10�i�����[�h�̏ꍇ
            if (operator.equals("plus")) {
                result = result + Double.parseDouble(buffer);// �o�b�t�@�̓��e��������
                buffer = Double.toString(result);// �v�Z���ʂ𕶎���ɕϊ�
                showBuffer();// �o�b�t�@�̍ĕ\��
            } else if (operator.equals("minus")) {
                result = result - Double.parseDouble(buffer);// �o�b�t�@�̓��e������
                buffer = Double.toString(result);// �v�Z���ʂ𕶎���ɕϊ�
                showBuffer();// �o�b�t�@�̍ĕ\��
            } else if (operator.equals("star")) {
                result = result * Double.parseDouble(buffer);// �o�b�t�@�̓��e���|����
                buffer = Double.toString(result);// �v�Z���ʂ𕶎���ɕϊ�
                showBuffer();// �o�b�t�@�̍ĕ\��
            } else if (operator.equals("slash")) {
                result = result / Double.parseDouble(buffer);// �o�b�t�@�̓��e�Ŋ���
                buffer = Double.toString(result);// �v�Z���ʂ𕶎���ɕϊ�
                showBuffer();// �o�b�t�@�̍ĕ\��
            } else {
                result = Double.parseDouble(buffer);// �o�b�t�@�̓��e�𐔒l�ɕϊ�
            }
        } else// 16�i�����[�h�̏ꍇ
        {
            if (operator.equals("plus")) {
                result = result + Hex.hexToDouble(buffer);// �o�b�t�@�̓��e��������
                buffer = Hex.doubleToHex(result);// �v�Z���ʂ𕶎���ɕϊ�
                showBuffer();// �o�b�t�@�̍ĕ\��
            } else if (operator.equals("minus")) {
                result = result - Hex.hexToDouble(buffer);// �o�b�t�@�̓��e������
                buffer = Hex.doubleToHex(result);// �v�Z���ʂ𕶎���ɕϊ�
                showBuffer();// �o�b�t�@�̍ĕ\��
            } else if (operator.equals("star")) {
                result = result * Hex.hexToDouble(buffer);// �o�b�t�@�̓��e���|����
                buffer = Hex.doubleToHex(result);// �v�Z���ʂ𕶎���ɕϊ�
                showBuffer();// �o�b�t�@�̍ĕ\��
            } else if (operator.equals("slash")) {
                result = result / Hex.hexToDouble(buffer);// �o�b�t�@�̓��e�Ŋ���
                buffer = Hex.doubleToHex(result);// �v�Z���ʂ𕶎���ɕϊ�
                showBuffer();// �o�b�t�@�̍ĕ\��
            } else {
                result = Hex.hexToDouble(buffer);// �o�b�t�@�̓��e�𐔒l�ɕϊ�
            }
        }
        addHistory(buffer);// �����ɒǉ�
    }

    /**
     * ������𗚗��ɒǉ�
     *
     * @param ans
     */
    void addHistory(String ans) {
        // -----������𖖔��ł͂Ȃ��擪�ɒǉ�����-----
        ArrayList<String> list = new ArrayList<>();
        while (choice.getItemCount() > 1) {// "History"�ȊO���폜����܂�
            list.add(choice.getItem(1));// ���X�g�ɒǉ�
            choice.remove(1);// �폜
        }
        if (decimalFlag) {// 10�i�����[�h�̏ꍇ
            choice.add("(10)" + ans);// �擪��(10)��t���Ēǉ�
        } else {// 16�i�����[�h�̏ꍇ
            choice.add("(16)" + ans);// �擪��(16)��t���Ēǉ�
        }
        for (String str : list) {
            choice.add(str);// ���X�g����`���C�X�ɒǉ�(����)
            if (choice.getItemCount() > HISTORY_MAX) {
                break;
            }
        }
    }

    /**
     * ����ۑ�
     */
    void save() {
        int itemNum = choice.getItemCount();// ���݂̗��𐔎擾
        try {
            PrintWriter out = new PrintWriter(new FileWriter(FILE_NAME));// �t�@�C�����J��
            for (int i = 1; i < itemNum; ++i) {
                out.println(choice.getItem(i));// ������ۑ�
            }
            out.close();// �t�@�C�������
        } catch (IOException e) {
            System.err.println("save�G���[");
        }
    }

    /**
     * ����ǂݍ���
     */
    void load() {
        try {
            BufferedReader in = new BufferedReader(new FileReader(FILE_NAME));// �t�@�C�����J��
            for (String line = in.readLine(); line != null; line = in.readLine()) {// 1�s���ǂݍ���
                choice.add(line);// �`���C�X�ɒǉ�
            }
            in.close();// �t�@�C�������
        } catch (FileNotFoundException e) {
            System.err.println("�t�@�C�������݂��܂���");
        } catch (IOException e) {
            System.err.println("load�G���[");
        }
    }

    /**
     * ���W�I�{�^���̏�Ԃ��ω������Ƃ��̓���
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        if (decimalFlag != decimalMode.isSelected()) {// ���݂̏�Ԃƕω��������
            decimalFlag = !decimalFlag;// ���[�h���t�]
            alphabetPanel.setVisible(!decimalFlag);// �A���t�@�x�b�g�p�l�������[�h�ɏ]���A�\���E��\��������
            pack();// ��ʃT�C�Y�̒���
            if (decimalFlag) {// 10�i�����[�h�֕ω������ꍇ
                buffer = Double.toString(Hex.hexToDouble(buffer));// �o�b�t�@��double�ɂ��Ă���A10�i��������֕ϊ�
            } else {// 16�i�����[�h�֕ω������ꍇ
                buffer = Hex.doubleToHex(Double.parseDouble(buffer));// �o�b�t�@��double�ɂ��Ă���A16�i��������֕ϊ�
            }
            showBuffer();// �o�b�t�@�̍ĕ\��
        }
        requestFocus();// �t�H�[�J�X�̗v��
    }

    /**
     * �L�[�������ꂽ�Ƃ��̓���
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();// �L�[�Ɋ֘A�t����ꂽ�l���擾
        if (code == KeyEvent.VK_ESCAPE) {// �N���A�̏ꍇ
            clearPressed();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    /**
     * �����L�[�������ꂽ�Ƃ��̓���
     */
    @Override
    public void keyTyped(KeyEvent e) {
        char ch = e.getKeyChar();// ���͂��ꂽ�������擾
        if (ch == '+') { // �v���X�̏ꍇ
            operatorPressed("plus");
        } else if (ch == '-') { // �}�C�i�X�̏ꍇ
            operatorPressed("minus");
        } else if (ch == '*') { // �X�^�[(�����Z)�̏ꍇ
            operatorPressed("star");
        } else if (ch == '/') { // �X���b�V��(����Z)�̏ꍇ
            operatorPressed("slash");
        } else if (ch == '=' || ch == '\n') { // �C�R�[���̏ꍇ
            equalPressed();
        } else if (ch == '.') { // �����_�̂Ƃ�
            pointPressed();
        } else if (ch == '\b') { // 1�����폜�̂Ƃ�
            deletePressed();
        } else {// ���̑��̂Ƃ�
            numPressed(ch);
        }
    }

    /**
     * ����(�`���C�X)���I�����ꂽ�Ƃ��̓���
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        String str = choice.getSelectedItem();// �I�����ꂽ�A�C�e���̕�������擾
        if (!"History".equals(str)) {// History�łȂ��Ƃ�
            String mode = str.substring(0, 4);// ���i���ŋL�^���ꂽ���̂����擾
            if (mode.equals("(10)") == decimalFlag) {// ���݂̐i���Ɠ����Ȃ�
                buffer = str.substring(4);// ���̂܂ܕ\��
            } else if (decimalFlag) {// ���[�h���Ⴂ�A����10�i�����[�h�Ȃ�
                buffer = Double.toString(Hex.hexToDouble(str.substring(4)));// 16�i���������double�ɕϊ����A������ɕϊ�
            } else {// ���[�h���Ⴂ�A����16�i�����[�h�Ȃ�
                buffer = Hex.doubleToHex(Double.parseDouble(str.substring(4)));// 10�i���������double�ɕϊ����A16�i��������ɕϊ�
            }
            showBuffer();// �o�b�t�@�̍ĕ\��
        }
        requestFocus();// �t�H�[�J�X�v��
    }
}
