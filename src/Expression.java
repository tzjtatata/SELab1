import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Xiangxi and Yuanze on 2016/9/18.
 */

class Expression {
  private boolean compiledMark;
  private ArrayList<Term> resultTerms;
  private HashMap<String, Integer> variableList;
  private int variableNumber;
  private ArrayList<NumericTerm> compileResults;
  private ArrayList<String> variableIndexToName;
  public static final double numericError = 1e-6;

  public Expression() {
    variableList = new HashMap<>();
    compileResults = new ArrayList<>();
    compiledMark = false;
  }

  public void compile(String expression) throws ExpressionCompileException {
    String innerString;
    innerString = blankStrip(expression);
    innerString = completeMultiplication(innerString);
    try {
      innerString = replacePowerNotion(innerString);
      reduceBracket(innerString);
      generateVariableList();
      transformIntoNumeric();
      mergeResults();
    } catch (ExpressionCompileException e) {
      compiledMark = false;
      throw e;
    }
    compiledMark = true;
  }

  public String toString() {
    return toString(compileResults);
  }

  private String toString(ArrayList<NumericTerm> polynomial) { //将numericTerm形式存的多项式转化为字符串输出
    if (polynomial.isEmpty()) {   // 若多项式不包含任何项，则输出"0"
      return "0";
    }
    String result = transFromNumericTermToString(polynomial.get(0));
    for (int i = 1; i < polynomial.size(); i++) {
      String termString = transFromNumericTermToString(polynomial.get(i));
      if (!termString.startsWith("-")) {      // 若项前没有负号，则需要添加正号
        result += "+";
      }
      result = result + termString;
    }
    return result;
  }
  
  private String blankStrip(String expression) {
    return expression.replaceAll("\\s+", "");         // 先去除所有的空格
  }
  
  private String completeMultiplication(String expression) {
    String innerString = expression.replaceAll("([\\)|\\d])([\\(|a-zA-Z])", "$1*$2");
    return innerString.replaceAll("([a-zA-Z])(\\()", "$1*$2");
  }
  
  private String replacePowerNotion(String expression) throws ExpressionCompileException {
    PowerNotationReplacer powerNotationReplacer = new PowerNotationReplacer(expression);
    return powerNotationReplacer.getResult();
  }
  
  private void reduceBracket(String expression) throws ExpressionCompileException {
    BracketReducer bracketReducer = new BracketReducer(expression);
    resultTerms = bracketReducer.resultTerms;
  }
  
  private void generateVariableList() {
    variableIndexToName = new ArrayList<>();
    for (Term term : resultTerms) {                                       // 对每一项
      for (String fragments : term.content.split("\\*")) {              // 用乘号分隔开
        if (fragments.matches("[a-zA-Z]+") && !variableIndexToName.contains(fragments)) {
          variableIndexToName.add(fragments); // 如果是未出现过的纯字母字串，则加入到变量名列表
        }
      }
    }
    for (int i = 0; i < variableIndexToName.size(); i++) {
      variableList.put(variableIndexToName.get(i), i);                // 添加字串至词典
    }
    variableNumber = variableIndexToName.size();
  }
  
  private void transformIntoNumeric() throws ExpressionCompileException {
    for (Term term : resultTerms) {
      ArrayList<Integer> powers = new ArrayList<>();
      for (int i = 0; i < variableNumber; i++) {
        powers.add(0);                                               // 幂计数初始化
      }
      double coefficient = 1.0d;        // 系数初始化，对于负项处理为-1
      if (term.sign == '\0') {
        coefficient *= -1;
      }
      for (String fragments : term.content.split("\\*")) {    // 对于乘号分隔开的每一部分
        if (fragments.matches("[a-zA-Z]+")) {
          int index = variableList.get(fragments);
          powers.set(index, powers.get(index) + 1);
        } else {
          try {
            coefficient *= Double.parseDouble(fragments);
          } catch (Exception eeE) {
            throw new ExpressionCompileException("Can not resolve this expression");
          }
        }
      }
      if (Math.abs(coefficient) > numericError) {
        compileResults.add(new NumericTerm(coefficient, powers));
      }
    }
  }

  private void mergeResults() {
    mergeResults(compileResults);
  }

  private void mergeResults(ArrayList<NumericTerm> inputCompileResults) {
    boolean modified;
    do {
      modified = false;
      inputCompileResults = sortByHash(inputCompileResults);
      for (int i = 0; i < inputCompileResults.size() - 1; i++) {
        NumericTerm formerTerm = inputCompileResults.get(i);
        NumericTerm latterTerm = inputCompileResults.get(i + 1);
        if (formerTerm.powers.equals(latterTerm.powers)) {  // 若有相邻项幂指数相同, 选择合并
          double coefficientSum = formerTerm.coefficient + latterTerm.coefficient;
          if (Math.abs(coefficientSum) < numericError) { // 若系数和为0，则删去两项，否则用一项代替原来的两项
            inputCompileResults.remove(i + 1);
            inputCompileResults.remove(i);
          } else {
            inputCompileResults.set(i, new NumericTerm(coefficientSum, latterTerm.powers));
            inputCompileResults.remove(i + 1);
          }
          modified = true;                        // 记录此次的合并
          break;                       // 数组序列结构已被破坏，应进行下一轮排序和循环
        }
      }
    } while (modified);
  }

  private ArrayList<NumericTerm> sortByHash(ArrayList<NumericTerm> list) {
    boolean flag;
    do {
      flag = false;
      for (int i = 0; i < list.size() - 1; i++) {
        if (CompareNumericTermByPowersHash.getHashCode(list.get(i).powers) 
            <
              CompareNumericTermByPowersHash.getHashCode(list.get(i + 1).powers)) {
          Object temp = list.get(i);
          list.set(i, list.get(i + 1));
          list.set(i + 1, (NumericTerm)temp);
          flag = true;
        }
      }
    } while (flag);
    return list;
  }

  private String transFromNumericTermToString(NumericTerm term) {
    String result;
    if (Math.abs(term.coefficient - Math.round(term.coefficient)) < numericError) {
      result = Long.toString(Math.round(term.coefficient)); // 当系数（近似）为整数时，以整数形式打印
    } else {
      result = Double.toString(term.coefficient);   // 否则以小数形式打印
    }
    for (int i = 0; i < variableNumber; i++) {
      int power = term.powers.get(i);              // 该位置的对应指数
      if (power > 0) {
        result += "*" + variableIndexToName.get(i);      // 指数非零则添加变量名
      }
      if (power > 1) {
        result += "^" + Integer.toString(power);       // 指数大于1则添加幂次
      }
    }
    if (result.startsWith("1*")) {
      return result.substring(2);          // 若为1*a形式，则省略前面的1
    }
    return result;
  }

  public boolean isCompiled() {
    return compiledMark;
  }
  
  public boolean hasVariable(String variable) {
    return variableIndexToName.contains(variable);    // 即检测该变量是否存在于变量列表中
  }
  
  public String derivate(String variable) {
    int variableIndex = variableList.get(variable);                     // 待求导变量在变量列表中的标号
    ArrayList<NumericTerm> derivedResult = new ArrayList<>();// 求导结果多项式的NumericTerm形式表达
    ArrayList<Integer> tempPowers = new ArrayList<>();           // 各变量幂指数暂存列表

    for (NumericTerm term : compileResults) {
      int variablePower = term.powers.get(variableIndex); // 对原多项式每一项中待求导变量的幂次
      if (variablePower > 0 ) {   // 当幂次为正，可以求得导数项，存入result中
        tempPowers.addAll(term.powers);
        tempPowers.set(variableIndex, variablePower - 1);
        derivedResult.add(new NumericTerm(term.coefficient * variablePower, tempPowers));
        tempPowers.clear();
      }
    }
    return toString(derivedResult);
  }
  
  public String simplify(String assignments) {
    ArrayList<String> variableArray = new ArrayList<>();
    ArrayList<Double> valueArray = new ArrayList<>();
    String resu;
    boolean isVariable = true;
    for (String fragments : assignments.split("[=| ]")) {                // 用等号与空格进行分割
      if (fragments.equals("")) {      // 若截取出的是空串，则不应进行任何处理
        continue;
      }
      if (isVariable) {                                               // 若此处应为变量名
        if (variableArray.contains(fragments)) {
          return "There're multiple variables " + fragments + "."; // 检测变量重复赋值错误
        }
        variableArray.add(fragments);
        isVariable = false;
      } else {                                                        // 若此处应为数值
        try {
          valueArray.add(Double.parseDouble(fragments));
        } catch (NumberFormatException eeE) {                         // 检测实数格式错误
          return "The substring " + fragments + " cannot be parsed into a real number.";
        }
        isVariable = true;
      }
    }
    if (variableArray.size() != valueArray.size()) { // 检测变量个数与数值个数不匹配的错误
      resu = "The number of variables and values don't match. ";
    }
    for (String variable : variableArray) {                              // 检测变量不包含在原表达式的错误
      if (!variableIndexToName.contains(variable)) {
        resu = "No such variable in the former expression.";
      }
    }
    ArrayList<NumericTerm> simplifiedResult = new ArrayList<>();// 化简结果多项式的NumericTerm形式表达
    for (NumericTerm originalTerm : compileResults) {                    // 对于原多项式中的每一项
      ArrayList<Integer> tempPowers = new ArrayList<>(originalTerm.powers);
      // 各变量幂指数暂存列表
      double tempCoefficient = originalTerm.coefficient;              // 系数暂存
      for (int i = 0; i < variableArray.size(); i++) {                // 对于赋值列表中的每一组赋值
        int variableIndex = variableList.get(variableArray.get(i)); // 取得变量标号
        int variablePower = tempPowers.get(variableIndex);          // 取得变量幂次
        double variableValue = valueArray.get(i);                   // 取得变量被赋值
        tempCoefficient *= Math.pow(variableValue, variablePower);  // 计算新系数
        tempPowers.set(variableIndex, 0);                           // 消除原变量
      }
      if (Math.abs(tempCoefficient) >= numericError) {                 // 系数为零的项自动消除
        simplifiedResult.add(new NumericTerm(tempCoefficient, tempPowers));
      }
    }
    mergeResults(simplifiedResult);
    resu = toString(simplifiedResult);
    return resu;
  }
}
