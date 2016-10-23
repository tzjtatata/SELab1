/**
 * Created by Xiangxi and Yuanze on 2016/9/18.
 */
class Dispatcher {
  Dispatcher() {
    commandRecognizer = new CommandRecognizer();
  }
  
  public void receiveInputString(String inputString) {
    this.inputString = inputString;
    commandRecognizer.recognise(inputString);
    testForNextLoop();
    dispatch();
  }
  
  public String outputString;     // ������ַ���
  public boolean readyForNextLoop;
  private String inputString;
  private Expression expression;
  private CommandRecognizer commandRecognizer;
  
  private void testForNextLoop() {
    readyForNextLoop = commandRecognizer.inputType != InputType.End;
  }
  
  private void dispatch() {
    if (commandRecognizer.inputType == InputType.Expression) {
      try {
        expression = null;
        expression = new Expression();
        expression.compile(inputString);
        outputString = expression.toString();
      } catch (ExpressionCompileException eeE) {
        outputString = eeE.getMessage();
      }
      return;
    }
    if (commandRecognizer.inputType == InputType.Derivation) {
      if (expression.isCompiled()) {
        String variable = commandRecognizer.operand.replaceAll("\\s", ""); // �Բ�������ȥ�ո������
        if (expression.hasVariable(variable)) {
          outputString = expression.derivate(variable);
        } else {
          outputString = "No such variable in this expression.";
        }
      } else {
        outputString = "No valid Expression has been given.";
      }
      return;
    }
    if (commandRecognizer.inputType == InputType.Simplification) {
      if (expression.isCompiled()) {
        outputString = 
        expression.simplify(commandRecognizer.operand.replaceAll("^[\\s]*(\\w+)", "$1"));
        // ��񻯸�ֵʽ,ȥ�����п�ͷ�Ŀո�
      } else {
        outputString = "No such variable in this expression.";
      }
      return;
    }
    if (commandRecognizer.inputType == InputType.End) {
      outputString = "Bye";
    } else { // ���벻�ܱ�ʶ��
      outputString = "No valid input recognised. ";
    }
  }
}
