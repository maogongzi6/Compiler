import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

public class RealNon {
}
class Main extends NonTerm {
    public Main(InnerNode inner, LRTable lr) {
        super(inner,lr);

    }
}

class FunctionList extends NonTerm {
    public FunctionList(InnerNode inner,LRTable lr) {
        super(inner,lr);
    }
}

class Function extends NonTerm {
    public Function(InnerNode inner,LRTable lr) {
        super(inner,lr);
    }
    public void func_1() {
        LinkedList<TreeNode> children = getChildren();
        Leaf l = (Leaf)children.get(1);
        ParameterList pL = (ParameterList)children.get(4);
        //if (!((Identify)l.term.term).id.equals("main"))
            for (Integer msil: funcBackPatch.get(((Identify)l.term.term).id))
                lrTbale.code.get(msil).result = new ConstantInt(nextLineCount());
            funcSave.put(((Identify)l.term.term).id,nextLineCount());
        funcOffset = new ConstantInt(lrTbale.table.nowOffset);
        lrTbale.code.add(new Msil("reset",null,null,funcOffset));
        //pL.funcOffset = funcOffset;
        saveTable = lrTbale.table;
        lrTbale.table = new SymbolTable();
    }
    /*public void func_2() {
        LinkedList<TreeNode> children = getChildren();
        ParameterList pL = (ParameterList) children.get(4);
        for (int pos = 0;pos<pL.typeList.size();++pos) {
            SymbolRecord record = lrTbale.table.get(pL.paramList.get(pos).id);
            if (record.type.getType()==TypeEnum.ARRAY) {
                lrTbale.code.get(lrTbale.code.size()-pos-1).result = record;
                ((ConstantInt)lrTbale.code.get(lrTbale.code.size()-pos-1).param_1).value -= funcOffset.value;
            }
            else lrTbale.code.get(lrTbale.code.size()-pos-2).result = record;
        }
    }*/
    public void func_3() {
        lrTbale.table = saveTable;
    }
    ConstantInt funcOffset;
    SymbolTable saveTable;
}

class ParameterList extends NonTerm {
    public ParameterList(InnerNode inner,LRTable lr) {
        super(inner,lr);
    }
    public void func_1() {
        LinkedList<TreeNode> children = getChildren();
        FuncParameter fP = (FuncParameter)children.get(0);
        lrTbale.code.add(new Msil("pop",null/*new ConstantInt(lrTbale.table.nowOffset)*/,null,lrTbale.table.get(fP.id.id)));
        //paramList = pL.paramList;
        //paramList.add(fP.id);
        //typeList.add(fP.type);
        //paramWidth = fP.type.width+pL.paramWidth;
    }
    /*public void func_2() {
        LinkedList<TreeNode> children = getChildren();
        FuncParameter fP = (FuncParameter)children.get(0);
        lrTbale.code.add(new Msil("pop",null/*new ConstantInt(lrTbale.table.nowOffset)*//*,null,lrTbale.table.get(fP.id.id)));
        //paramList.add(fP.id);
        //typeList.add(fP.type);
        //paramWidth = fP.type.width;
    }*/

    ArrayList<Type> typeList = new ArrayList<>();
    ArrayList<Identify> paramList = new ArrayList<>();
    Integer paramWidth;
}

class FuncParameter extends VirtualType {
    public FuncParameter(InnerNode inner, LRTable lr) {
        super(inner, lr);
    }
    public void func_1() {
        LinkedList<TreeNode> children = getChildren();
        ((ArrayDeclare)(children.get(2))).saveType = ((VirtualType)children.get(0)).type;
    }
    public void func_2() {
        LinkedList<TreeNode> children = getChildren();
        type = new Stretch(((VirtualType) children.get(2)).type);
        Leaf l = (Leaf) children.get(3);
        id = (Identify)l.term.term;
        lrTbale.table.add(new SymbolRecord(id,type));
    }
    public void func_3() {
        LinkedList<TreeNode> children = getChildren();
        type = ((VirtualType) children.get(0)).type;
        Leaf l = (Leaf) children.get(1);
        id = (Identify)l.term.term;
        lrTbale.table.add(new SymbolRecord(id,type));
    }
    Identify id;
}

class ArrayDeclare extends VirtualType {
    public ArrayDeclare(InnerNode inner, LRTable lr) {
        super(inner, lr);
    }
    public void func_1() {
        type = saveType;
    }
    public void func_2() {
        LinkedList<TreeNode> children = getChildren();
        ArrayDeclare c_1 = (ArrayDeclare) children.get(1);
        Leaf l = (Leaf)children.get(3);
        type = new ArrayType(((IntData)l.term.term).content,c_1.type);
    }
    public void func_3() {
        ((ArrayDeclare)getChildren().get(1)).saveType = saveType;
    }

    Type saveType;
}

class Parameter extends VirtualType {
    public Parameter(InnerNode inner,LRTable lr) {
        super(inner,lr);
    }
    public void func_1() {
        LinkedList<TreeNode> children = getChildren();
        ((ArrayProperty)(children.get(2))).saveType = ((VirtualType)children.get(0)).type;
    }
    public void func_2() {
        LinkedList<TreeNode> children = getChildren();
        type = ((VirtualType)children.get(2)).type;
        id = (Identify) ((Leaf)children.get(3)).term.term;
        //System.out.println(type.width+"**********");
    }

    Identify id;
}

class ArrayProperty extends VirtualType {
    public ArrayProperty(InnerNode inner,LRTable lr) {
        super(inner,lr);
    }
    public void func_1() {
        LinkedList<TreeNode> children = getChildren();
        ((ArrayProperty)(children.get(4))).saveType = saveType;
    }
    public void func_2() {
        LinkedList<TreeNode> children = getChildren();
        ArrayProperty c_1 = (ArrayProperty) children.get(4);
        Leaf l = (Leaf) children.get(1);
        IntData data = (IntData)l.term.term;
        //if (num.term.term.name.equals("data")&&((Data)(num.term.term)).type.equals("Integer"))
        //if (data.content)
        type = new ArrayType(data.content,c_1.type);
        //else throw new RuntimeException();
    }
    public void func_3() {
        type = saveType;
    }

    Type saveType;
}

class ParamType extends VirtualType {
    public ParamType(InnerNode inner,LRTable lr) {
        super(inner,lr);
    }
    public void func_1() {
        LinkedList<TreeNode> children = getChildren();
        Leaf leaf = (Leaf) children.get(0);
        switch (leaf.term.term.name) {
            case "int": type = Type.integer; break;
            case "char": type = Type.character; break;
            default:throw new RuntimeException();
        }
    }
}

class ReturnType extends VirtualType {
    public ReturnType(InnerNode inner,LRTable lr) {
        super(inner,lr);
    }

}

class FunctionBody extends NonTerm {
    public FunctionBody(InnerNode inner,LRTable lr) {
        super(inner,lr);
    }
}

class SentencesList extends BackPatch {
    public SentencesList(InnerNode inner,LRTable lr) {
        super(inner,lr);
    }
    public void func_1() {
        LinkedList<TreeNode> children = getChildren();
        SentencesList l_1 = (SentencesList) children.get(0);
        Sentences s = (Sentences) children.get(2);
        
        M m = (M) children.get(1);
        backPatch(lrTbale.code,l_1.nextList,m.instr);
        nextList = s.nextList;
    }
    public void func_2() {
        LinkedList<TreeNode> children = getChildren();

        //SentencesList l = (SentencesList)mother;
        Sentences s = (Sentences) children.get(0);
        nextList = s.nextList;
    }
}

class Sentences extends BackPatch {
    public Sentences(InnerNode inner,LRTable lr) {
        super(inner,lr);
    }
    public void func_1() {
        nextList = ((BackPatch) getChildren().get(0)).nextList;
    }
    public void func_2() {
        target = new Temp(Type.stretch);
        lrTbale.code.add(new Msil("load",new Temp(Type.integer),null,target));
        lrTbale.code.add(new Msil("reset",null,null,new ConstantInt(-lrTbale.table.nowOffset)));
    }
    public void func_3() {
        lrTbale.code.add(new Msil("goto",null,null,target));
        lrTbale.code.add(new Msil("ret",null,null,null));

    }
    Temp target;
}

class Return extends Exp {
    public Return(InnerNode inner, LRTable lr) {
        super(inner, lr);
    }
    public void func_1() {
        lrTbale.code.add(new Msil("push",null,new ConstantInt(0),null));
    }
    public void func_2() {
        lrTbale.code.add(new Msil("push",null,new ConstantInt(4),null));
        Leaf l = (Leaf) getChildren().get(1);
        String identify = ((Identify)(l.term.term)).id;
        if (lrTbale.table.get(identify).type.getType()==TypeEnum.STRETCH||lrTbale.table.get(identify).type.getType()==TypeEnum.ARRAY)
            addr = new Temp(Type.stretch);
        else if (lrTbale.table.get(identify).type.getType()==TypeEnum.INT)
            addr = new Temp(Type.integer);
        else if (lrTbale.table.get(identify).type.getType()==TypeEnum.CHAR)
            addr = new Temp(Type.character);
        lrTbale.code.add(new Msil("load",new ConstantInt(lrTbale.table.get(identify).offset+lrTbale.table.nowOffset),null,addr));
        //if (((Temp)addr).type.getType()==TypeEnum.STRETCH)
        lrTbale.code.add(new Msil("push",null,addr,null));
    }
    public void func_3() {
        lrTbale.code.add(new Msil("push", null, new ConstantInt(4), null));
        Leaf l = (Leaf) getChildren().get(1);
        Integer num = ((IntData) (l.term.term)).content;
        lrTbale.code.add(new Msil("push",null,new ConstantInt(num),null));
    }
    public void func_4() {
        lrTbale.code.add(new Msil("push", null, new ConstantInt(1), null));
        Leaf l = (Leaf) getChildren().get(1);
        Character num = ((CharData) (l.term.term)).content;
        lrTbale.code.add(new Msil("push",null,new ConstantChar(num),null));
    }

}

class Definition extends BackPatch {
    public Definition(InnerNode inner,LRTable lr) {
        super(inner,lr);
    }
    public void func_1() {
        Parameter p = (Parameter) getChildren().get(0);
        if (!lrTbale.table.add(new SymbolRecord(p.id,p.type))) throw new RuntimeException();
    }
}

class Assignment extends BackPatch {
    public Assignment(InnerNode inner,LRTable lr) {
        super(inner,lr);
    }
    public void func_1() {
        LinkedList<TreeNode> children = getChildren();
        Leaf l = (Leaf) children.get(0);
        FirstExp e = (FirstExp) children.get(2);
        lrTbale.code.add(new Msil("assign",e.addr,null,lrTbale.table.get(((Identify)l.term.term).id)));
    }
    public void func_2() {
        LinkedList<TreeNode> children = getChildren();
        ArrayAssign l = (ArrayAssign) children.get(0);
        FirstExp e = (FirstExp) children.get(2);
        lrTbale.code.add(new Msil("assign[]",l.array,l.addr,e.addr));
    }
}

class ArrayAssign extends NonTerm {
    public ArrayAssign(InnerNode inner, LRTable lr) {
        super(inner, lr);
    }
    public void func_1() {
        LinkedList<TreeNode> children = getChildren();
        Leaf l = (Leaf) children.get(0);
        FirstExp e = (FirstExp) children.get(2);
        array = lrTbale.table.get(((Identify)l.term.term).id);
        if (type==null) type = array.type;
        else if (array.type.getType()==TypeEnum.STRETCH)
            type = ((Stretch)array.type).target;
        else type = ((ArrayType)type).dataType;
        addr = new Temp(Type.integer);
        lrTbale.code.add(new Msil("*",e.addr,new ConstantInt(type.width),addr));
    }
    public void func_2() {
        LinkedList<TreeNode> children = getChildren();
        ArrayAssign l_1 = (ArrayAssign) children.get(0);
        FirstExp e = (FirstExp) children.get(2);
        array = l_1.array;
        type = ((ArrayType)l_1.type).dataType;
        Temp t = new Temp(Type.integer);
        addr = new Temp(Type.integer);
        lrTbale.code.add(new Msil("*",e.addr,new ConstantInt(type.width),t));
        lrTbale.code.add(new Msil("+",l_1.addr,t,addr));
    }

    Unit addr;
    Type type;
    SymbolRecord array;
}

class FirstExp extends Exp {
    public FirstExp(InnerNode inner, LRTable lr) {
        super(inner, lr);
    }
    public void func_1() {
        LinkedList<TreeNode> children = getChildren();
        Leaf l = (Leaf) children.get(1);
        FirstExp e = (FirstExp) children.get(0);
        SecondExp t = (SecondExp) children.get(2);
        addr = new Temp(Type.integer);
        lrTbale.code.add(new Msil(l.term.term.name,e.addr,t.addr,addr));
    }
    public void func_2() {
        SecondExp t = (SecondExp) getChildren().get(0);
        addr = t.addr;
    }
}

class SecondExp extends Exp {
    public SecondExp(InnerNode inner, LRTable lr) {
        super(inner, lr);
    }
    public void func_1() {
        LinkedList<TreeNode> children = getChildren();
        Leaf l = (Leaf) children.get(1);
        SecondExp t = (SecondExp) children.get(0);
        ThirdExp f = (ThirdExp) children.get(2);
        addr = new Temp(Type.integer);
        lrTbale.code.add(new Msil(l.term.term.name,t.addr,f.addr,addr));
    }
    public void func_2() {
        ThirdExp f = (ThirdExp) getChildren().get(0);
        addr = f.addr;
    }
}

class ThirdExp extends Exp {
    public ThirdExp(InnerNode inner, LRTable lr) {
        super(inner, lr);
    }
    public void func_6() {
        ArrayAssign l = (ArrayAssign) getChildren().get(0);
        addr = new Temp(Type.integer);
        lrTbale.code.add(new Msil("assign[]",l.array,l.addr,addr));
    }
    public void func_5() {
        Leaf l = (Leaf) getChildren().get(0);
        addr = lrTbale.table.get(((Identify)l.term.term).id);
    }
    public void func_1() {
        LinkedList<TreeNode> children = getChildren();
        ThirdExp f = (ThirdExp) children.get(1);
        addr = new Temp(Type.integer);
        lrTbale.code.add(new Msil("N",f.addr,null,addr));
    }
    public void func_2() {
        LinkedList<TreeNode> children = getChildren();
        FirstExp f = (FirstExp) children.get(1);
        addr = f.addr;
    }
    public void func_3() {
        Leaf l = (Leaf) getChildren().get(0);
        addr = new ConstantInt(((IntData)l.term.term).content);
    }
    public void func_4() {
        Leaf l = (Leaf) getChildren().get(0);
        addr = new ConstantChar(((CharData)l.term.term).content);
    }
}

class For extends BackPatch {
    public For(InnerNode inner,LRTable lr) {super(inner,lr);}
    public void func_1() {}

}

class While extends BackPatch {
    public While(InnerNode inner,LRTable lr) {
        super(inner,lr);
    }
    public void func_1() {
        ComparationList b = (ComparationList) children.get(3);
        M m_1 = (M) children.get(1),m_2 = (M)children.get(5);
        Brace s = (Brace) children.get(6);
        backPatch(lrTbale.code,s.nextList,m_1.instr);
        backPatch(lrTbale.code,b.trueList,m_2.instr);
        nextList = b.falseList;
        lrTbale.code.add(new Msil("goto",null,null,new ConstantInt(m_1.instr)));
    }

}

class If extends BackPatch {
    public If(InnerNode inner,LRTable lr) {
        super(inner,lr);
    }
    public void func_1() {
        nextList = ((BackPatch) getChildren().get(0)).nextList;
    }

}

class If_1 extends BackPatch {
    public If_1(InnerNode inner,LRTable lr) {
        super(inner,lr);
    }
    public void func_1() {
        LinkedList<TreeNode> children = getChildren();
        ComparationList b = (ComparationList) children.get(2);
        M m = (M) children.get(4);
        Brace s_1 = (Brace) children.get(5);
        backPatch(lrTbale.code,b.trueList,m.instr);
        nextList = merge(b.falseList,s_1.nextList);
    }

}

class If_2 extends BackPatch {
    public If_2(InnerNode inner,LRTable lr) {
        super(inner,lr);
    }
    public void func_1() {
        LinkedList<TreeNode> children = getChildren();
        ComparationList b = (ComparationList) children.get(2);
        M m_1 = (M) children.get(4),m_2 = (M)children.get(8);
        Brace s_1 = (Brace) children.get(5),s_2 = (Brace) children.get(9);
        backPatch(lrTbale.code,b.trueList,m_1.instr);
        backPatch(lrTbale.code,b.falseList,m_2.instr);
        nextList = merge(s_1.nextList,s_2.nextList);
        nextList = merge(nextList,saveList);

    }
    public void func_2() {
        saveList = makeList(nextLineCount());
        lrTbale.code.add(new Msil("goto",null,null,new ConstantInt()));
    }
    HashSet<Integer> saveList;
}

class Comparation extends BackPatch {
    public Comparation(InnerNode inner,LRTable lr) {
        super(inner,lr);
    }
    public void func_1() {
        LinkedList<TreeNode> children = getChildren();
        trueList = makeList(nextLineCount());
        falseList = makeList(nextLineCount()+1);
        Leaf e_1 = (Leaf) children.get(0),e_2 = (Leaf) children.get(2),c = (Leaf) children.get(1).getChildren().get(0);
        lrTbale.code.add(new Msil(c.term.term.name,lrTbale.table.get(((Identify)e_1.term.term).id),lrTbale.table.get(((Identify)e_2.term.term).id),new ConstantInt()));
        lrTbale.code.add(new Msil("goto",null,null,new ConstantInt()));
    }
    public void func_2() {
        Comparation b = (Comparation) getChildren().get(1);
        trueList = b.falseList;
        falseList = b.trueList;
    }
    public void func_3() {
        ComparationList b = (ComparationList) getChildren().get(1);
        trueList = b.trueList;
        falseList = b.falseList;
    }
    public void func_4() {
        trueList = makeList(nextLineCount());
        lrTbale.code.add(new Msil("goto",null,null,new ConstantInt()));
    }
    public void func_5() {
        falseList = makeList(nextLineCount());
        lrTbale.code.add(new Msil("goto",null,null,new ConstantInt()));
    }
}

class ComparationList extends BackPatch {
    public ComparationList(InnerNode inner,LRTable lr) {
        super(inner,lr);
    }
    public void func_1() {
        LinkedList<TreeNode> children = getChildren();
        ComparationList b_1 = (ComparationList) children.get(0);
        Comparation b_2 = (Comparation) children.get(3);
        M m = (M) children.get(2);
        backPatch(lrTbale.code,b_1.trueList,m.instr);
        falseList = merge(b_1.falseList,b_2.falseList);
        trueList = b_2.trueList;
    }
    public void func_2() {
        LinkedList<TreeNode> children = getChildren();
        ComparationList b_1 = (ComparationList) children.get(0);
        Comparation b_2 = (Comparation) children.get(3);
        M m = (M) children.get(2);
        backPatch(lrTbale.code,b_1.falseList,m.instr);
        trueList = merge(b_1.trueList,b_2.trueList);
        falseList = b_2.falseList;
    }
    public void func_3() {
        Comparation c = (Comparation) getChildren().get(0);
        trueList = c.trueList;
        falseList = c.falseList;
        nextList = c.nextList;
    }
}


class Compare extends NonTerm {
    public Compare(InnerNode inner,LRTable lr) {
        super(inner,lr);
    }
}

class Calculation extends NonTerm {
    public Calculation(InnerNode inner,LRTable lr) {
        super(inner,lr);
    }
}

class Calculate extends NonTerm {
    public Calculate(InnerNode inner,LRTable lr) {
        super(inner,lr);
    }
}

class Brace extends BackPatch {
    public Brace(InnerNode inner,LRTable lr) {
        super(inner,lr);
    }
    public void func_2() {
        width = lrTbale.table.nowOffset;
        lrTbale.table = lrTbale.table.outer;
        System.out.println(lrTbale.table+"------");
        nextList = ((SentencesList) getChildren().get(3)).nextList;
    }

    public void func_1() {
        lrTbale.table = new SymbolTable(lrTbale.table);
        System.out.println(lrTbale.table);
    }
    int width;
}

class M extends NonTerm {
    public M(InnerNode inner,LRTable lr) {
        super(inner,lr);
    }
    public void func_1() {
        instr = nextLineCount();
        System.out.println(instr);
    }
    Integer instr;
}

/*class N extends BackPatch {
    public N(InnerNode inner,ArrayList<Msil> c) {
        super(inner,c);
    }
}
*/

class FunctionBrace extends NonTerm {
    public FunctionBrace(InnerNode inner,LRTable lr) {
        super(inner,lr);
    }
}

/*class ConstantData extends NonTerm {

    public ConstantData(InnerNode inner, LRTable lr) {
        super(inner, lr);
    }
    public void func_1() {
        LinkedList<TreeNode> children = getChildren();
        Leaf l = (Leaf) children.get(0);
        if (l.term.term.name.equals("int"))
            constant = new ConstantInt(((IntData)l.term.term).content);
    }
    Unit constant;
}*/

class Func extends Exp {
    public Func(InnerNode inner, LRTable lr) {
        super(inner, lr);
    }
    public void func_1() {
        lrTbale.code.add(new Msil("func",null,null,null));
        lrTbale.code.add(new Msil("push",/*new ConstantInt(lrTbale.table.nowOffset)*/null,new ConstantInt(),null));               //调用前的入栈，这是函数返回时goto的目标位置，需要回填
        saveLineCount = nextLineCount();
    }
    public void func_2() {
        Leaf l = (Leaf) getChildren().get(0);
        String id = ((Identify)l.term.term).id;
        InputList list = (InputList) children.get(3);
        ((ConstantInt)lrTbale.code.get(nextLineCount()-list.count-1).param_2).value = saveLineCount+list.count+1;
        if (!funcSave.containsKey(id)) {
            lrTbale.code.add(new Msil("goto", null, null, new ConstantInt()));                                                                 //前往调用函数，现在目标未知，找到函数名后回填
            if (!funcBackPatch.containsKey(id)) funcBackPatch.put(id, new ArrayList<>());
            funcBackPatch.get(id).add(nextLineCount() - 1);                                                                               //这里减一是因为需要的是当前行而不是下一行
        }
        else lrTbale.code.add(new Msil("goto", null, null, new ConstantInt(funcSave.get(id))));

        ConstantInt width = new ConstantInt();
        lrTbale.code.add(new Msil("pop",null/*new ConstantInt(lrTbale.table.nowOffset)*/,null,width));                                          //调用后的出栈
        addr = new Temp(new Type(width.value));
        lrTbale.code.add(new Msil("pop",null/*new ConstantInt(lrTbale.table.nowOffset)*/,null,addr));                                          //调用后的出栈
    }
    int saveLineCount;
}

class InputList extends NonTerm {
    public InputList(InnerNode inner, LRTable lr) {
        super(inner, lr);
    }
    public void func_1() {
        Leaf l = (Leaf) getChildren().get(2);
        Integer num = ((IntData)l.term.term).content;
        InputList list = (InputList) children.get(0);
        lrTbale.code.add(new Msil("push",null/*new ConstantInt(lrTbale.table.nowOffset)*/,new ConstantInt(num),null));               //调用前的入栈
        count = list.count+1;

        //lrTbale.code.add(new Msil("assign",new ConstantInt(num),null,null));               //参数准备好赋值，但是付给谁还不知道，等待回填
    }
    public void func_2() {
        Leaf l = (Leaf) getChildren().get(2);
        InputList list = (InputList) children.get(0);

        Character letter = ((CharData)l.term.term).content;
        lrTbale.code.add(new Msil("push",null/*new ConstantInt(lrTbale.table.nowOffset)*/,new ConstantChar(letter),null));               //调用前的入栈
        count = list.count+1;

        // lrTbale.code.add(new Msil("assign",new ConstantChar(letter),null,null));
    }
    public void func_3() {
        Leaf l = (Leaf) getChildren().get(2);
        Identify identify = (Identify)l.term.term;
        InputList list = (InputList) children.get(0);

        SymbolRecord record = lrTbale.table.get(identify.id);
        if (record.type.getType()==TypeEnum.ARRAY) {
            lrTbale.code.add(new Msil("push",null/*new ConstantInt(lrTbale.table.nowOffset)*/,new StretchRecord(record),null));               //调用前的入栈
            //lrTbale.code.add(new Msil("assign", new StretchRecord(record), null, null));
        }
        else {
            lrTbale.code.add(new Msil("push",null/*new ConstantInt(lrTbale.table.nowOffset)*/,record,null));               //调用前的入栈
            //lrTbale.code.add(new Msil("assign", record, null, null));
        }
        count = list.count+1;

    }
    public void func_4() {
        Leaf l = (Leaf) getChildren().get(0);
        Integer num = ((IntData)l.term.term).content;
        lrTbale.code.add(new Msil("push",null/*new ConstantInt(lrTbale.table.nowOffset)*/,new ConstantInt(num),null));               //调用前的入栈
        ++count;

    }
    public void func_5() {
        Leaf l = (Leaf) getChildren().get(0);
        Character letter = ((CharData)l.term.term).content;
        lrTbale.code.add(new Msil("push",null/*new ConstantInt(lrTbale.table.nowOffset)*/,new ConstantChar(letter),null));               //调用前的入栈
        ++count;

    }
    public void func_6() {
        Leaf l = (Leaf) getChildren().get(0);
        Identify identify = (Identify)l.term.term;
        SymbolRecord record = lrTbale.table.get(identify.id);
        if (record.type.getType()==TypeEnum.ARRAY) {
            lrTbale.code.add(new Msil("push",null/*new ConstantInt(lrTbale.table.nowOffset)*/,new StretchRecord(record),null));               //调用前的入栈
            //lrTbale.code.add(new Msil("assign", new StretchRecord(record), null, null));
        }
        else {
            lrTbale.code.add(new Msil("push",null/*new ConstantInt(lrTbale.table.nowOffset)*/,record,null));               //调用前的入栈
            //lrTbale.code.add(new Msil("assign", record, null, null));
        }
        ++count;
    }
    int count = 0;

}

