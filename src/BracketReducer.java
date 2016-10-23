import java.util.ArrayList;
/**
 * Created by Xiangxi and Yuanze on 2016/9/18.
 */

class BracketReducer {
  /**
  * Description.
  * @author me
  */
  public BracketReducer(String expression) throws ExpressionCompileException {
    resultTerms = breakAtPlusAndSub(expression);
  }
  
  public ArrayList<Term> resultTerms;
  
  public String toString() {
    String result = "";
    int len = resultTerms.size();
    for (int i = 0;i < len;i++) {
      if (i == 0 && resultTerms.get(i).sign == 1) {
        result += resultTerms.get(i).content;
      } else {
        if (resultTerms.get(i).sign == 1) { 
          result += "+";
        } else {
          result += "-";
        }
        result += resultTerms.get(i).content;
      }
    }
    return result;
  }

  private ArrayList<Term> breakAtPlusAndSub(String exp) throws ExpressionCompileException {   
    // 对一个由加减连接各个项的式子进行处理
    ArrayList<Term> rel = new ArrayList<>();
    ArrayList<Integer> notation = divideAtLowLevel(exp);
    if (exp.charAt(0) == '-') {
      return breakAtPlusAndSub("0" + exp);
    }
    if (divideWithCharacters(exp,"+-*").size() == 1 && exp.charAt(0) == '(') {
      //如果整个表达式由一个括号包络
      return breakAtPlusAndSub(exp.substring(1, exp.length() - 1));
    }
    int len = notation.size();
    for (int i = 0;i < len;i++) {
      int head = notation.get(i);
      int tail;
      if (i == len - 1) {
        tail = exp.length();
      } else {
        tail = notation.get(i + 1) - 1;
      }
      String substr = exp.substring(head,tail);
      ArrayList<Term> temp = new ArrayList<>();
      if (substr.contains("(")) {
        temp = breakAtMultiplification(substr);
      } else {
        temp.add(new Term(substr,'\1'));
      }
      if (i != 0 && exp.charAt(notation.get(i) - 1) == '-') {
        for (Term aaTemp : temp) {
          aaTemp.sign = 1 - aaTemp.sign;
        }
      }
      rel.addAll(temp);
    }
    return rel;
  }
  
  private ArrayList<Term> breakAtMultiplification(String exp) throws ExpressionCompileException {
    ArrayList<Integer> ssSplits = divideAtHighLevel(exp);
    int len = ssSplits.size();
    if (divideWithCharacters(exp,"+-*").size() == 1 && exp.charAt(0) == '(') {
      // 如果整个表达式由一个括号包络
      return breakAtMultiplification(exp.substring(1, exp.length() - 1));  // 就剥去括号再进行函数操作
    }
    ArrayList<Term> aaA = new ArrayList<>();
    ArrayList<Term> bbB;
    aaA.add(new Term("1",'\1'));
    String subStr;
    for (int i = 0;i < len;i++) {
      int head = ssSplits.get(i);
      int tail;
      if (i == len - 1) {
        tail = exp.length();
      } else {
        tail = ssSplits.get(i + 1) - 1;
      }
      subStr = exp.substring(head,tail);
      bbB = breakAtPlusAndSub(subStr);
      aaA = multiplyTerm(aaA, bbB);
    }
    return aaA;

  }
  
  private ArrayList<Term> multiplyTerm(ArrayList<Term> aaA, ArrayList<Term> bbB) {
    ArrayList<Term> rel = new ArrayList<>();
    Term temp;
    for (Term anA : aaA) {
      for (Term a1B : bbB) {
    	temp = new Term();
        temp.content = anA.content + "*" + a1B.content;
        if (anA.sign == a1B.sign) {
          temp.sign = '\1';
        } else {
          temp.sign = '\0';
        }
        rel.add(temp);
      }
    }
    return rel;
  }
  
  private ArrayList<Integer> divideAtLowLevel(String inputString) 
      throws ExpressionCompileException {
    return divideWithCharacters(inputString, "+-");
  }
  
  private ArrayList<Integer> divideAtHighLevel(String inputString) 
      throws ExpressionCompileException {
    return divideWithCharacters(inputString, "*");
  }
  
  private ArrayList<Integer> divideWithCharacters(String inputString, String characters) 
      throws ExpressionCompileException {
    int depth = 0;                                          // 括号嵌套深度
    int index = 0;
    ArrayList<Integer> result = new ArrayList<>();
    result.add(0);
    while (index < inputString.length()) {
      if (characters.contains(inputString.substring(index, index + 1)) && depth == 0) {
        if (index != 0) {
          result.add(index + 1);
        }
      }
      if (inputString.charAt(index) == '(') {
        depth++;
      }
      if (inputString.charAt(index) == ')') {
        depth--;
      }
      if (depth < 0) {
        throw new ExpressionCompileException("Brackets not match.");
      }
      index++;
    }
    if (depth != 0) {
      throw new ExpressionCompileException("Brackets not match.");
    }
    return result;
  }
}
