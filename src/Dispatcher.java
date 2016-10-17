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
    public String outputString;                                         // 输出的字符串
    public boolean readyForNextLoop;
    private String inputString;
    private Expression expression;
    private CommandRecognizer commandRecognizer;
    private void testForNextLoop() {
        readyForNextLoop = commandRecognizer.inputType != InputType.End;
    }
    private void dispatch() {       // 任务分配器
        if (commandRecognizer.inputType == InputType.Expression) {
            try {
                expression = null;
                expression = new Expression();
                expression.compile(inputString);
                outputString = expression.toString();
            } catch (ExpressionCompileException e) {
                outputString = e.getMessage();
            }
            return;
        }
        if (commandRecognizer.inputType == InputType.Derivation) {
            if (expression.isCompiled()) {
                String variable = commandRecognizer.operand.replaceAll("\\s", "");                      // 对操作数，去空格后输入
                if (expression.hasVariable(variable)) {
                    outputString = expression.derivate(variable);
                }
                else
                    outputString = "No such variable in this expression.";
            }
            else
                outputString = "No valid Expression has been given.";
            return;
        }
        if (commandRecognizer.inputType == InputType.Simplification) {
            if (expression.isCompiled()) {
                outputString = expression.simplify(commandRecognizer.operand.replaceAll("^[\\s]*(\\w+)", "$1"));
                                                                                                        // 规格化赋值式,去除所有开头的空格
            }
            else
                outputString = "No such variable in this expression.";
            return;
        }
        if (commandRecognizer.inputType == InputType.End)
            outputString = "Bye";
        else                                                                                            // 输入不能被识别
            outputString = "No valid input recognised. ";
    }
}
