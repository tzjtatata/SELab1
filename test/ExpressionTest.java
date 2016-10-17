import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Created by Xiangxi and Yuanze on 2016/9/19.
 */
@RunWith(Parameterized.class)
public class ExpressionTest {
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"1.0*a^2+2.0*a*b+1.0*b^2", "(a+b)^2"},
                {"1.0*a+1.0*b", "a+b"},
                {"1.0*a+3.0*b", "a+3b"},
                {"1.0*a*b*c+2.0*b*c*e+2.0*b*e*d","a*b*c+2b(c+d)e"},
                {"1.0*b*a+1.0*b^2*d+1.0*b^2*a*c+1.0*a*c+1.0*b*d*c+1.0*b*a*c^2","(b+c)(a+b(d+c*a))"},
                {"-1.0*a*b+1.0*a^2*c*d-1.0*a*b*c*d","-a*(b+c*d(-a+b))"},
                {"-1.0*a^2-6.0*a*b-9.0*b^2","-(a+3b)^2"}
        });
    }

    private String fExpcted;
    private String fInput;

    public ExpressionTest(String expcted, String input) {
        fExpcted = expcted;
        fInput = input;
    }

    @org.junit.Test
    public void TestCompile() throws Exception{
        Expression expression = new Expression();
        try {
            expression.compile(fInput);
            assertEquals(fExpcted,expression.toString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}