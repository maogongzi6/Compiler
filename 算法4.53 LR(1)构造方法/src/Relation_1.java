import java.util.ArrayList;

public class Relation_1 extends Sentence{
    Relation_1(Sentence s, int dP, Terminal n) {
        super(s);
        //if (production.get(0).specialType()==SpecialWord.IPSLON) dotPos = 0;
        dotPos = dP;
        next = n;
    }

    Relation_1(Sentence s, Terminal n) {
        super(s);
        dotPos = 0;
        next = n;
    }

    Word wordAtDot() {                          //！！！！！！！！！！！！！！！！这里要特殊考虑IPSLON，属于设计上的一个大问题
        if (productionLength()==dotPos||production.get(0).specialType()==SpecialWord.IPSLON) return null;
        return wordAt(dotPos);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime*result+super.hashCode();
        result = prime*result+next.hashCode();
        return prime*result+Integer.hashCode(dotPos);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj==null||obj.getClass()!=getClass()) return false;
        if (obj==this) return true;
        return production.equals(((Relation_1) obj).production)&&dotPos==((Relation_1) obj).dotPos&&next.equals(((Relation_1) obj).next);
    }

    @Override
    public String toString() {
        ArrayList<Word> copy = new ArrayList<>(production);
        copy.add(dotPos,Terminal.dot);
        return copy.toString().replaceAll(",","")+" , "+next;
    }

    int dotPos;
    Terminal next;
}