import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class t {
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Class.forName("X").getConstructor(new Class[0]).newInstance();
        Class.forName("Y").getConstructor(new Class[0]).newInstance();

        Matcher m = Pattern.compile("(<.+?>)|(\\[.+?])|(\\{.+?})").matcher("<ArrayProperty>~[[]<Num>[\\]]<ArrayProperty>$<IPSLON>");
        while (m.find()) {
            String s = m.group();
            System.out.println(1+s);
        }
    }
}

class X{
    public X(){}
}
class Y{
    public Y(){}

}