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

  private String toString(ArrayList<NumericTerm> polynomial) { //��numericTerm��ʽ��Ķ���ʽת��Ϊ�ַ������
    if (polynomial.isEmpty()) {   // ������ʽ�������κ�������"0"
      return "0";
    }
    String result = transFromNumericTermToString(polynomial.get(0));
    for (int i = 1; i < polynomial.size(); i++) {
      String termString = transFromNumericTermToString(polynomial.get(i));
      if (!termString.startsWith("-")) {      // ����ǰû�и��ţ�����Ҫ�������
        result += "+";
      }
      result = result + termString;
    }
    return result;
  }
  
  private String blankStrip(String expression) {
    return expression.replaceAll("\\s+", "");         // ��ȥ�����еĿո�
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
    for (Term term : resultTerms) {                                       // ��ÿһ��
      for (String fragments : term.content.split("\\*")) {              // �ó˺ŷָ���
        if (fragments.matches("[a-zA-Z]+") && !variableIndexToName.contains(fragments)) {
          variableIndexToName.add(fragments); // �����δ���ֹ��Ĵ���ĸ�ִ�������뵽�������б�
        }
      }
    }
    for (int i = 0; i < variableIndexToName.size(); i++) {
      variableList.put(variableIndexToName.get(i), i);                // ����ִ����ʵ�
    }
    variableNumber = variableIndexToName.size();
  }
  
  private void transformIntoNumeric() throws ExpressionCompileException {
    for (Term term : resultTerms) {
      ArrayList<Integer> powers = new ArrayList<>();
      for (int i = 0; i < variableNumber; i++) {
        powers.add(0);                                               // �ݼ�����ʼ��
      }
      double coefficient = 1.0d;        // ϵ����ʼ�������ڸ����Ϊ-1
      if (term.sign == '\0') {
        coefficient *= -1;
      }
      for (String fragments : term.content.split("\\*")) {    // ���ڳ˺ŷָ�����ÿһ����
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
        if (formerTerm.powers.equals(latterTerm.powers)) {  // ������������ָ����ͬ, ѡ��ϲ�
          double coefficientSum = formerTerm.coefficient + latterTerm.coefficient;
          if (Math.abs(coefficientSum) < numericError) { // ��ϵ����Ϊ0����ɾȥ���������һ�����ԭ��������
            inputCompileResults.remove(i + 1);
            inputCompileResults.remove(i);
          } else {
            inputCompileResults.set(i, new NumericTerm(coefficientSum, latterTerm.powers));
            inputCompileResults.remove(i + 1);
          }
          modified = true;                        // ��¼�˴εĺϲ�
          break;                       // �������нṹ�ѱ��ƻ���Ӧ������һ�������ѭ��
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
      result = Long.toString(Math.round(term.coefficient)); // ��ϵ�������ƣ�Ϊ����ʱ����������ʽ��ӡ
    } else {
      result = Double.toString(term.coefficient);   // ������С����ʽ��ӡ
    }
    for (int i = 0; i < variableNumber; i++) {
      int power = term.powers.get(i);              // ��λ�õĶ�Ӧָ��
      if (power > 0) {
        result += "*" + variableIndexToName.get(i);      // ָ����������ӱ�����
      }
      if (power > 1) {
        result += "^" + Integer.toString(power);       // ָ������1������ݴ�
      }
    }
    if (result.startsWith("1*")) {
      return result.substring(2);          // ��Ϊ1*a��ʽ����ʡ��ǰ���1
    }
    return result;
  }

  public boolean isCompiled() {
    return compiledMark;
  }
  
  public boolean hasVariable(String variable) {
    return variableIndexToName.contains(variable);    // �����ñ����Ƿ�����ڱ����б���
  }
  
  public String derivate(String variable) {
    int variableIndex = variableList.get(variable);                     // ���󵼱����ڱ����б��еı��
    ArrayList<NumericTerm> derivedResult = new ArrayList<>();// �󵼽������ʽ��NumericTerm��ʽ���
    ArrayList<Integer> tempPowers = new ArrayList<>();           // ��������ָ���ݴ��б�

    for (NumericTerm term : compileResults) {
      int variablePower = term.powers.get(variableIndex); // ��ԭ����ʽÿһ���д��󵼱������ݴ�
      if (variablePower > 0 ) {   // ���ݴ�Ϊ����������õ��������result��
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
    for (String fragments : assignments.split("[=| ]")) {                // �õȺ���ո���зָ�
      if (fragments.equals("")) {      // ����ȡ�����ǿմ�����Ӧ�����κδ���
        continue;
      }
      if (isVariable) {                                               // ���˴�ӦΪ������
        if (variableArray.contains(fragments)) {
          return "There're multiple variables " + fragments + "."; // �������ظ���ֵ����
        }
        variableArray.add(fragments);
        isVariable = false;
      } else {                                                        // ���˴�ӦΪ��ֵ
        try {
          valueArray.add(Double.parseDouble(fragments));
        } catch (NumberFormatException eeE) {                         // ���ʵ����ʽ����
          return "The substring " + fragments + " cannot be parsed into a real number.";
        }
        isVariable = true;
      }
    }
    if (variableArray.size() != valueArray.size()) { // ��������������ֵ������ƥ��Ĵ���
      resu = "The number of variables and values don't match. ";
    }
    for (String variable : variableArray) {                              // ��������������ԭ���ʽ�Ĵ���
      if (!variableIndexToName.contains(variable)) {
        resu = "No such variable in the former expression.";
      }
    }
    ArrayList<NumericTerm> simplifiedResult = new ArrayList<>();// ����������ʽ��NumericTerm��ʽ���
    for (NumericTerm originalTerm : compileResults) {                    // ����ԭ����ʽ�е�ÿһ��
      ArrayList<Integer> tempPowers = new ArrayList<>(originalTerm.powers);
      // ��������ָ���ݴ��б�
      double tempCoefficient = originalTerm.coefficient;              // ϵ���ݴ�
      for (int i = 0; i < variableArray.size(); i++) {                // ���ڸ�ֵ�б��е�ÿһ�鸳ֵ
        int variableIndex = variableList.get(variableArray.get(i)); // ȡ�ñ������
        int variablePower = tempPowers.get(variableIndex);          // ȡ�ñ����ݴ�
        double variableValue = valueArray.get(i);                   // ȡ�ñ�������ֵ
        tempCoefficient *= Math.pow(variableValue, variablePower);  // ������ϵ��
        tempPowers.set(variableIndex, 0);                           // ����ԭ����
      }
      if (Math.abs(tempCoefficient) >= numericError) {                 // ϵ��Ϊ������Զ�����
        simplifiedResult.add(new NumericTerm(tempCoefficient, tempPowers));
      }
    }
    mergeResults(simplifiedResult);
    resu = toString(simplifiedResult);
    return resu;
  }
}
