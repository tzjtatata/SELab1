/**
 * Created by Xiangxi and Yuanze on 2016/9/18.
 */
public class Term {                                 // 不包含括号的联乘式
    public String content;
    public int sign;                              // '\0'表示负,'\1'表示正
    Term(String content,char sign) {
        this.content = content;
        this.sign = sign;
    }
    Term() {
        this.content = "";
        this.sign = '\0';
    }
}
