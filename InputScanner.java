import java.util.Scanner;

/**
 * Created by Xiangxi and Yuanze on 2016/9/18.
 */
public class InputScanner {
    InputScanner() {
        dispatcher = new Dispatcher();                      // 处理输入指令或表达式的对象
        String inputString;                                 // 接收输入字符串
        Scanner scanner = new Scanner(System.in);           // 建立输入流
        do {
            inputString = scanner.nextLine();
            dispatch(inputString);
        } while(nextLoop);                                  // 循环接收并处理每一行输入，直到dispatcher通知不再接收新输入为止
        scanner.close();
    }
    private boolean nextLoop;
    private Dispatcher dispatcher;
    private void dispatch(String inputString) {
        dispatcher.receiveInputString(inputString);
        nextLoop = dispatcher.readyForNextLoop;
        System.out.println(dispatcher.outputString);
    }
}
