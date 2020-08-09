import java.util.ArrayList;
import java.util.LinkedList;

public class Sentence {
    public Sentence(Nonterminal nT, ArrayList<Word> ws) {
        original = nT;
        productionWithCode = ws;
        production = new ArrayList<>();
        for (Word word: ws)
            if (word.wordType()!=WordType.ACTION) production.add(word);
    }

    public Sentence(Sentence sent) {
        original = sent.original;
        production = new ArrayList<>(sent.production);
        productionWithCode = new ArrayList<>(sent.productionWithCode);
    }

    public boolean equals(Object obj) {
        if (obj==null||obj.getClass()!=getClass()) return false;
        if (obj==this) return true;
        return ((Sentence) obj).production.equals(production) && ((Sentence) obj).original.equals(original);
    }

    @Override
    public int hashCode() {
        int prime = 31,result = 1;
        result = prime*result+production.hashCode();
        return prime*result+original.hashCode();
    }

    public int productionLength() {/*if (production.get(0).specialType()==SpecialWord.IPSLON)return 0;*/ return production.size();}
    //！！！！！！！！！！本来IPSLON->·而不是IPSLON->ipslon，因此原本来说对于IPSLON这个函数应该返回0而不是1，但是现在没法改了，如果改掉会出现很多错误，所以只有修改relation中的wordatdot和LR1中的movefront，这里看书不仔细造成的问题巨大
    public boolean add(Word w) {
        productionWithCode.add(w);
        if (w.wordType()!=WordType.ACTION) return production.add(w);
        return false;
    }
    public boolean addAll(Sentence sent) {
        productionWithCode.addAll(sent.getProductionWithCode());
        return production.addAll(sent.getProduction());
    }
    public ArrayList<Word> subSentence(int begin,int end) {return new ArrayList<>(production.subList(begin, end)); }
    public Word remove(int w) {return production.remove(w); }                               //注意！！！！！！！！！！！！这里的两个remove函数，不能正确的移除productionWithCode，但是这两个函数只在ll文法使用，所以不影响我们的程序
    public boolean remove(Word w) {return production.remove(w); }
    public ArrayList<Word> getProduction() {return production; }
    public ArrayList<Word> getProductionWithCode() {return productionWithCode; }
    public Word wordAt(int pos) {return production.get(pos); }

    @Override
    public String toString() {
        return original.toString()+" -> "+production.toString();
    }

    Nonterminal original;
    ArrayList<Word> production;
    ArrayList<Word> productionWithCode;
}
