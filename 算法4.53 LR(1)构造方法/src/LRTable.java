import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class LRTable {
    class ActionOrGoto {
        @Override
        public boolean equals(Object obj) {
            if (obj==null||obj.getClass()!=getClass()) return false;
            if (obj==this) return true;
            if (stateId==null||((ActionOrGoto)obj).stateId==null)                                       //这里的三个关键字都可为空，给判断增加难度，这是设计上的折衷，为了在初始化时更加方便
                if (!(stateId==null&&((ActionOrGoto)obj).stateId==null)) return false;                  //思路是，如果双方的同个属性只有一个为空，返回不等，如果都不为空或者都为空进行下面的检测
            if (sentence==null||((ActionOrGoto)obj).sentence==null)
                if (!(sentence==null&&((ActionOrGoto)obj).sentence==null)) return false;
            return (stateId==null||stateId.equals(((ActionOrGoto)obj).stateId))&&(sentence==null||sentence.equals(((ActionOrGoto)obj).sentence));
        }

        @Override
        public int hashCode() {
            int prime = 31,result = 1;
            result = prime*result+stateId.hashCode();
            return prime*result+sentence.hashCode();
        }

        boolean func(LinkedList<Integer> stateStack) {throw new RuntimeException(); } //这个函数不该被调用，ActionOrGoto意义上是虚基类
        boolean func(LinkedList<Integer> stateStack,LinkedList<TreeNode> nodeStack,Terminal node) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {throw new RuntimeException();}
        Integer stateId;
        Sentence sentence;
    }

    class MoveTo extends ActionOrGoto {
        MoveTo(Integer i,Sentence sen) {stateId = i;sentence = sen;}

        @Override
        boolean func(LinkedList<Integer> stateStack) {
            stateStack.push(stateId);
            return true;
        }

        @Override
        boolean func(LinkedList<Integer> stateStack, LinkedList<TreeNode> nodeStack, Terminal node) {
            boolean result = func(stateStack);
            nodeStack.push(new Leaf(node));
            //Leaf leaf = new Leaf(node);

            /*Class[] classes = new Class[1];
            classes[0] = node.getClass();
            Term obj = null;
            try {
                System.out.println(Class.forName(node.term.name.replace("[", "").replace("]", "")).getConstructor(classes).newInstance(node));
                obj = (Term) Class.forName(node.term.name.replace("[", "").replace("]", "")).getConstructor(classes).newInstance(node);
            } catch (Exception e) {throw new RuntimeException(); }

            nodeStack.push(obj);*/
            return result;
        }

        @Override
        public String toString() {
            return "moveTo: "+stateId;
        }
    }

    class Infer extends ActionOrGoto {
        Infer(Sentence sen) {
            sentence = sen;
        }

        @Override
        boolean func(LinkedList<Integer> stateStack) {
            for (int count = 0;count<sentence.productionLength();++count)
                stateStack.pop();
            Integer nowState = stateStack.getFirst();
            ActionOrGoto goTo = gotoMap.get(nowState).get(sentence.original);
            System.out.println(this);
            if (goTo==null) return false;
            goTo.func(stateStack);
            return true;
        }

        @Override
        boolean func(LinkedList<Integer> stateStack, LinkedList<TreeNode> nodeStack, Terminal node) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
            InnerNode newNode = new InnerNode();
            for (int count = sentence.productionWithCode.size()-1;count>=0;--count) {
                if (sentence.productionWithCode.get(count).specialType()!=SpecialWord.IPSLON) {
                            //！！！！！！！！！这里发现规约IPSLON时会错误的多弹栈一次，因为IPSLON.productionwithcode的长度是1（里面有一个不应被算入的IPSLON，而IPSLON在移入时并没有做压栈动作），所以会弹一次栈造成一个意料之外的弹栈，在这特殊判断改正，
                            // 这个IPSLON的设计真的时最大的败笔，造成了太多麻烦，但是发现的太晚已经无法修改
                    if (sentence.productionWithCode.get(count).wordType() != WordType.ACTION) {
                        stateStack.pop();
                        newNode.addChild(nodeStack.pop());
                    } else newNode.addChild(new ActionNode((Code) sentence.productionWithCode.get(count)));
                }
            }
            //这里构建相关的对象
            Class[] classes = new Class[2];
            classes[0] = newNode.getClass();
            classes[1] = LRTable.this.getClass();
            newNode.setOriginal(sentence.original);
            NonTerm obj = null;
            //try {
                obj = (NonTerm) Class.forName(newNode.original.name).getConstructor(classes).newInstance(newNode,LRTable.this);
            //} catch (Exception e) {throw new RuntimeException(); }

            Integer nowState = stateStack.getFirst();
            ActionOrGoto goTo = gotoMap.get(nowState).get(sentence.original);
            nodeStack.push(obj);
            //nodeStack.push(newNode);
            System.out.println(this);
            if (goTo==null)
                return false;
            goTo.func(stateStack);
            return true;
        }

        @Override
        public String toString() {
            return "infer: "+" "+sentence;
        }
    }

    class Accept extends ActionOrGoto {
        Accept() {}

        @Override
        boolean func(LinkedList<Integer> stateStack) {
            return true;
        }

        @Override
        boolean func(LinkedList<Integer> stateStack, LinkedList<TreeNode> nodeStack, Terminal node) {
            return true;
        }

        @Override
        public String toString() {
            return "accept";
        }
    }

    class Goto extends ActionOrGoto {
        Goto(Integer i) {stateId = i;}

        @Override
        boolean func(LinkedList<Integer> stateStack) {
            stateStack.push(stateId);
            return true;
        }

        @Override
        boolean func(LinkedList<Integer> stateStack, LinkedList<TreeNode> nodeStack, Terminal node) {
            return func(stateStack);
        }

        @Override
        public String toString() {
            return "goto: "+stateId;
        }
    }

    LRTable(LR_1 lrG) {
        grammar = lrG;
        for (int count = 0; count< grammar.dfaMaker.getNodeNum(); ++count) {
            actionMap.add(new HashMap<>());
            gotoMap.add(new HashMap<>());
        }
    }

    void buildTable() {
        for (int pos = 0;pos<actionMap.size();++pos) {
            System.out.println(pos);
            LR_1.SetOfItems item = grammar.dfaMaker.getNode(pos);
            for (Relation_1 relation : item.relationSet) {
                if (relation.productionLength()==relation.dotPos||relation.getProduction().get(0).specialType()==SpecialWord.IPSLON) {
                    if (grammar.dfaMaker.getAccept().contains(grammar.dfaMaker.move(item,Terminal.end)))
                        buildActionMap(pos,Terminal.end,new Accept());
                    else
                        for (Terminal term: grammar.grammar.getFollow(relation.original))
                            buildActionMap(pos,term,new Infer(relation));
                        //for (Terminal term : grammar.LRGrammar.getFollow(entry.getKey()))
                        //buildActionMap(pos,relation.next,new Infer(relation));
                }
                else {
                    Word nowWord = relation.wordAtDot();
                    if (nowWord.wordType()==WordType.TERMINAL) {
                        Integer nextState = grammar.dfaMaker.moveById(pos, grammar.dfaMaker.getPathId(nowWord));
                        buildActionMap(pos,(Terminal) nowWord,new MoveTo(nextState,relation));
                    }
                    else {
                        Integer nextState = grammar.dfaMaker.moveById(pos, grammar.dfaMaker.getPathId(nowWord));
                        buildGotoMap(pos,(Nonterminal) nowWord,new Goto(nextState));
                    }
                }
            }
        }
    }

    void buildActionMap(Integer itemPos, Terminal term, ActionOrGoto action) {
        if (actionMap.get(itemPos).get(term)!=null&&actionMap.get(itemPos).get(term).stateId!=null&&!(actionMap.get(itemPos).get(term).stateId.equals(action.stateId))) {
            System.out.print(itemPos);System.out.print(term);
            System.out.print(action); throw new RuntimeException();}
        actionMap.get(itemPos).put(term,action);
    }

    void buildGotoMap(Integer itemPos,Nonterminal nonTerm,ActionOrGoto goTo) {
        if (gotoMap.get(itemPos).get(nonTerm)!=null&&!gotoMap.get(itemPos).get(nonTerm).equals(goTo)) throw new RuntimeException();
        gotoMap.get(itemPos).put(nonTerm,goTo);
    }

    boolean runLR(ArrayList<Terminal> input) {
        input.add(Terminal.end);
        LinkedList<Integer> stateStack = new LinkedList<>() {{push(grammar.dfaMaker.getStartId());}};
        Integer nowState;
        int nowPos = 0;
        while (true) {
            nowState = stateStack.getFirst();
            ActionOrGoto action = actionMap.get(nowState).get(input.get(nowPos));
            if (action==null|| !action.func(stateStack))
            return false;
            else if (action.getClass().equals(Accept.class))
                return true;
            if (action.getClass().equals(MoveTo.class)) ++nowPos;
        }
    }

    boolean runLrWithCode(ArrayList<Terminal> input) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        tree = new Tree(new Root());
        input.add(Terminal.end);
        LinkedList<Integer> stateStack = new LinkedList<>() {{push(grammar.dfaMaker.getStartId());}};
        LinkedList<TreeNode> nodeStack = new LinkedList<>();
        Integer nowState;
        int nowPos = 0;
        while (true) {
            nowState = stateStack.getFirst();
            ActionOrGoto action = actionMap.get(nowState).get(input.get(nowPos));
            if (action==null|| !action.func(stateStack,nodeStack,input.get(nowPos)))
                return false;
            else if (action.getClass().equals(Accept.class)) {
                tree.root.addChild(nodeStack.pop());
                return true;
            }
            if (action.getClass().equals(MoveTo.class)) ++nowPos;
        }
    }

    void runTheTree() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException {
        LinkedList<TreeNode> stack = new LinkedList<>(){{push(tree.root);}};
        while (!stack.isEmpty()) {
            TreeNode now = stack.pop();
            if (now.treeType()==TreeType.FUNCTION) {
                //这里调用函数
                Class c = Class.forName(now.getMother().original.name);
                Method m = c.getMethod(((ActionNode)now).function.funcName);
                m.invoke(now.getMother());
                //System.out.println(now.getMother().original.name+"    "+((ActionNode)now).function.funcName+"\n");
            }
            else if (now.treeType()==TreeType.INNER)
                for (ListIterator<TreeNode> iterator = now.getChildren().listIterator(now.getChildren().size()); iterator.hasPrevious();) stack.push(iterator.previous());      //这里修改了这个瑕疵，使用iterator反向遍历，因为LinkedList是双向的，所以均摊时间是常数
            //for (int pos = now.getChildren().size()-1;pos>=0;--pos) stack.push(now.getChildren().get(pos));                 //这里一个小瑕疵，需要反向遍历子节点，但是
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("actionMap: \n");
        for (int pos = 0;pos<actionMap.size();++pos) {
            if (actionMap.get(pos).size()!=0) {
                builder.append(pos).append(":  ");
                builder.append(actionMap.get(pos)).append("\n");
            }
        }
        builder.append("gotoMap: \n");
        for (int pos = 0;pos<gotoMap.size();++pos) {
            if (gotoMap.get(pos).size()!=0) {
                builder.append(pos).append(":  ");
                builder.append(gotoMap.get(pos)).append("\n");
            }
        }
        return builder.toString();
    }

    SymbolTable table = new SymbolTable();
    ArrayList<Msil> code = new ArrayList<>();
    Tree tree;
    ActionOrGoto actionOrGoto = new ActionOrGoto();
    LR_1 grammar;
    ArrayList<HashMap<Terminal, ActionOrGoto>> actionMap = new ArrayList<>();
    ArrayList<HashMap<Nonterminal, ActionOrGoto>> gotoMap = new ArrayList<>();
}

