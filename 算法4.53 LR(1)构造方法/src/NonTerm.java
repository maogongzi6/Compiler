import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class NonTerm extends InnerNode {
    public NonTerm(InnerNode inner,LRTable lr) {
        for (TreeNode node :inner.getChildren())
            addChildLast(node);                                             //这里加到最后面，否则会反序
        setOriginal(inner.original);
        lrTbale = lr;
    }
    int nextLineCount() {return lrTbale.code.size(); }
    LRTable lrTbale;
    //NonTerm() {}
    static HashMap<String,ArrayList<Integer>> funcBackPatch = new HashMap<>();
    static HashMap<String,Integer> funcSave = new HashMap<>();
}

class BackPatch extends NonTerm{

    public BackPatch(InnerNode inner,LRTable lr) {
        super(inner,lr);
    }

    void backPatch(ArrayList<Msil> code,HashSet<Integer> list,Integer target) {
        for (Integer line: list)
            code.get(line).result = new ConstantInt(target);
    }

    HashSet<Integer> makeList(Integer line) {
        return new HashSet<>(){{add(line);}};
    }

    HashSet<Integer> merge(HashSet<Integer> setA,HashSet<Integer> setB) {
        return new HashSet<>() {{addAll(setA); addAll(setB); }};
    }

    HashSet<Integer> nextList = new HashSet<>();
    HashSet<Integer> trueList = new HashSet<>();
    HashSet<Integer> falseList = new HashSet<>();
}

class VirtualType extends NonTerm {
    public VirtualType(InnerNode inner, LRTable lr) {
        super(inner, lr);
    }

    Type type;
}

class Exp extends NonTerm {
    public Exp(InnerNode inner, LRTable lr) {
        super(inner, lr);
    }

    Unit addr;
}