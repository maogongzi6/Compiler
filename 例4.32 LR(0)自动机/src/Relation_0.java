import java.util.ArrayList;

public class Relation_0 extends Sentence {
    public Relation_0(Sentence sen) {super(sen); dotPos = 0;}
    public Relation_0(Sentence sen, int dp) {super(sen); dotPos = dp;}
    Word wordAtDot() {
        if (productionLength()==dotPos) return null;
        return wordAt(dotPos);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime*result+super.hashCode();
        return prime*result+Integer.hashCode(dotPos);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj==null||obj.getClass()!=getClass()) return false;
        if (obj==this) return true;
        return production.equals(((Relation_0) obj).production)&&dotPos==((Relation_0) obj).dotPos;
    }

    @Override
    public String toString() {
        ArrayList<Word> copy = new ArrayList<>(production);
        copy.add(dotPos,Terminal.dot);
        return copy.toString().replaceAll(",","");
    }
    int dotPos;
}
