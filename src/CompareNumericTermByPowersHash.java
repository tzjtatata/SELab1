import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by Xiangxi and Yuanze on 2016/9/19.
 */
class CompareNumericTermByPowersHash implements Comparator {
  @Override
    public int compare(Object o1, Object o2) {
    NumericTerm term1 = (NumericTerm) o1;
    NumericTerm term2 = (NumericTerm) o2;
    if (getHashCode(term1.powers) > getHashCode(term2.powers)) {
      return 1;
    }
    return 0;
  }

  public static long getHashCode(ArrayList<Integer> list) {
    if (list.size() == 0) {
      return 0;
    }
    long result = list.get(0);
    for (int i = 1; i < list.size(); i++) {
      result = result * 33 + list.get(i);
    }
    return result;
  }
}
