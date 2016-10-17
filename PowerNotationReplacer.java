/**
 * Created by Xiangxi and Yuanze on 2016/9/18.
 */
public class PowerNotationReplacer {
    public PowerNotationReplacer(String expression) {
        this.expression = expression;
        result = expression;
    }

    private String expression;
    public String result = "xx";

    public String getResult() throws ExpressionCompileException{
        while (expression.contains("^")) {                                          // 当表达式中仍存在幂符号，进行处理
            int notionIndex = expression.indexOf("^");                              // 找到第一个幂符号的位置
            String stringAfterNotion = expression.substring(notionIndex+1);         // 幂符号后的子串
            if (!stringAfterNotion.matches("^[\\d]+.*"))                            // 如果其后的子串不是以数字开头，则表达式不合法
                throw new ExpressionCompileException("A single natural number expected behind the power notion.");
            String powerString = stringAfterNotion.split("\\D")[0];
            int power = Integer.parseInt(powerString);                              // 获取这个幂符号后的幂次
            if (power < 1)                                                          // 如果其后的子串数字不是正整数，则表达式不合法
                throw new ExpressionCompileException("A single natural number expected behind the power notion.");
            int startIndex;                                                         // 幂符号支配的部分的起始位置
            try {
                startIndex = findRepeatPosition(expression.substring(0, notionIndex));
            } catch (ExpressionCompileException e) {
                throw e;
            }
            String formerFragment = expression.substring(0, startIndex);            // 前部子串
            String middleFragment = expression.substring(startIndex, notionIndex);  // 中部子串，即需要重复的部分
            String latterFragment = expression.substring(notionIndex+1+powerString.length());
                                                                                    // 幂符号位置右移一位再移除幂数字占位，得到后部子串
            result = formerFragment;
            while (power != 1) {
                result += middleFragment + "*";
                power--;
            }
            result += middleFragment;
            result += latterFragment;
            expression = result;
        }
        return result;
    }
                                                                                    // 找到子字符串中应当被幂符号重复的部分
    private int findRepeatPosition(String inputString) throws ExpressionCompileException {
        try{
            if (inputString.endsWith(")"))                                          // 若幂次前是括号
                return findLastMatchBracket(inputString);
            if (inputString.substring(inputString.length()-1).matches("[a-zA-Z]"))       // 若幂次前是变量
                return findLastMatchAlphabet(inputString);
            throw new ExpressionCompileException("Variables expected before power notion.");
        } catch(ExpressionCompileException e) {
            throw e;
        }
    }

    private int findLastMatchBracket(String inputString) throws ExpressionCompileException {
        int index = inputString.length()-1;                                         // 指标定位到最后一个括号处
        int depth = 0;                                                              // 括号嵌套深度此时为1
        do {
            if (index == -1)                                                        // 穷尽字符串而未能找到匹配位置，说明输入不合法
                throw new ExpressionCompileException("Brackets not match.");
            if (inputString.charAt(index) == ')')
                depth++;
            if (inputString.charAt(index) == '(')
                depth--;
            index--;                                                                // 指针左移
        } while(depth != 0 || inputString.charAt(index+1) != '(');
        return index+1;
    }

    private int findLastMatchAlphabet(String inputString) {
        int index = inputString.length()-1;
        while (Character.isLetter(inputString.charAt(index))) {                     // 找到第一个非字母字符的位置
            index--;
            if (index == -1)
                return 0;
        }
        return index+1;
    }
}
