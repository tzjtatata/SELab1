import java.util.ArrayList;
/**
 * Created by Xiangxi and Yuanze on 2016/9/18.
 */
class BracketReducer {
    public BracketReducer(String expression) throws ExpressionCompileException {
        resultTerms = breakAtPlusAndSub(expression);
    }
    public ArrayList<Term> resultTerms;
    public String toString(){
        String result = "";
        int len = resultTerms.size();
        for (int i = 0;i<len;i++) {
            if (i == 0 && resultTerms.get(i).sign == 1) {
                result += resultTerms.get(i).content;
            }
            else {
                if (resultTerms.get(i).sign == 1) result += "+";
                else result += "-";
                result += resultTerms.get(i).content;
            }
        }
        return result;
    }

    private ArrayList<Term> breakAtPlusAndSub(String exp) throws ExpressionCompileException{   // 对一个由加减连接各个项的式子进行处理
        ArrayList<Term> rel = new ArrayList<>();
        ArrayList<Integer> notation = divideAtLowLevel(exp);
        if (exp.charAt(0) == '-')
            return breakAtPlusAndSub("0" + exp);
        int len = notation.size();
        if (divideWithCharacters(exp,"+-*").size() == 1 && exp.charAt(0) == '(')            // 如果整个表达式由一个括号包络
            return breakAtPlusAndSub(exp.substring(1, exp.length() - 1));                     // 就剥去括号再进行函数操作
        for (int i = 0;i<len;i++) {
            int head = notation.get(i);
            int tail;
            if (i == len -1) {
                tail = exp.length();
            }
            else {
                tail = notation.get(i + 1) -1;
            }
            String substr = exp.substring(head,tail);
            ArrayList<Term> temp = new ArrayList<>();
            if (substr.contains("(")) {
                temp = breakAtMultiplification(substr);
            }
            else {
                temp.add(new Term(substr,'\1'));
            }
            if (i != 0 && exp.charAt(notation.get(i) - 1) == '-') {
                for (Term aTemp : temp) {
                    aTemp.sign = 1 - aTemp.sign;
                }
            }
            rel.addAll(temp);
        }
        return rel;
    }
    private ArrayList<Term> breakAtMultiplification(String exp) throws ExpressionCompileException{
        ArrayList<Integer> Splits = divideAtHighLevel(exp);
        int len = Splits.size();
        if (divideWithCharacters(exp,"+-*").size() == 1 && exp.charAt(0) == '(')               // 如果整个表达式由一个括号包络
            return breakAtMultiplification(exp.substring(1, exp.length() - 1));                // 就剥去括号再进行函数操作
        ArrayList<Term> a = new ArrayList<>();
        ArrayList<Term> b;
        a.add(new Term("1",'\1'));
        String subStr;
        for (int i = 0;i<len;i++) {
            int head = Splits.get(i);
            int tail;
            if (i == len -1) {
                tail = exp.length();
            }
            else {
                tail = Splits.get(i + 1) -1;
            }
            subStr = exp.substring(head,tail);
            b = breakAtPlusAndSub(subStr);
            a = multiplyTerm(a, b);
        }
        return a;

    }
    private ArrayList<Term> multiplyTerm(ArrayList<Term> a, ArrayList<Term> b) {
        ArrayList<Term> rel = new ArrayList<>();
        Term temp;
        for (Term anA : a) {
            for (Term aB : b) {
                temp = new Term();
                temp.content = anA.content + "*" + aB.content;
                if (anA.sign != aB.sign) {
                    temp.sign = '\0';
                } else {
                    temp.sign = '\1';
                }
                rel.add(temp);
            }
        }
        return rel;
    }
    private ArrayList<Integer> divideAtLowLevel(String inputString) throws ExpressionCompileException {
        return divideWithCharacters(inputString, "+-");
    }
    private ArrayList<Integer> divideAtHighLevel(String inputString) throws ExpressionCompileException {
        return divideWithCharacters(inputString, "*");
    }
    private ArrayList<Integer> divideWithCharacters(String inputString, String characters) throws ExpressionCompileException {
        int depth = 0;                                          // 括号嵌套深度
        int index = 0;
        ArrayList<Integer> result = new ArrayList<>();
        result.add(0);
        while (index < inputString.length()) {
            if (characters.contains(inputString.substring(index, index + 1)) && depth == 0) {
                if (index != 0)
                    result.add(index+1);
            }
            if (inputString.charAt(index) == '(')
                depth++;
            if (inputString.charAt(index) == ')')
                depth--;
            if (depth < 0)
                throw new ExpressionCompileException("Brackets not match.");
            index++;
        }
        if (depth != 0)
            throw new ExpressionCompileException("Brackets not match.");
        return result;
    }
}
