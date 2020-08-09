import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class SLRTable {
    class ActionOrGoto {
        boolean func(int nowPos,LinkedList<Integer> stateStack) {throw new RuntimeException(); } //这个函数不该被调用，ActionOrGoto意义上是虚基类

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

        Integer stateId;
        Sentence sentence;
        //Word word;
    }

    class MoveTo extends ActionOrGoto {
        MoveTo(Integer i) {stateId = i;}

        @Override
        boolean func(int nowPos,LinkedList<Integer> stateStack) {
            stateStack.push(stateId);
            ++nowPos;
            return true;
        }

        @Override
        public String toString() {
            return "moveTo: "+stateId;
        }
    }

    class Infer extends ActionOrGoto {
        Infer(Sentence s) {
            sentence = s;
        }

        @Override
        boolean func(int nowPos,LinkedList<Integer> stateStack) {
            //int size = grammar.lrGrammar.getById(sentenceId).size();
            for (int count = 0;count<sentence.productionLength();++count)
                stateStack.pop();
            Integer nowState = stateStack.getFirst();
            ActionOrGoto goTo = gotoMap.get(nowState).get(sentence.original);
            if (goTo==null) return false;
            goTo.func(nowPos,stateStack);
            return true;
        }

        @Override
        public String toString() {
            return "infer: "+sentence.original+" "+sentence.production;
        }
    }

    class Accept extends ActionOrGoto {
        Accept() {}

        @Override
        boolean func(int nowPos,LinkedList<Integer> stateStack) {
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
        boolean func(int nowPos,LinkedList<Integer> stateStack) {
            stateStack.push(stateId);
            return true;
        }

        @Override
        public String toString() {
            return "goto: "+stateId;
        }
    }

    SLRTable(LRGrammar lrG) {
        grammar = lrG;
        for (int count = 0; count< grammar.dfaMaker.getNodeNum(); ++count) {
            actionMap.add(new HashMap<>());
            gotoMap.add(new HashMap<>());
        }
    }

    void buildTable() {                                                                 //这个函数要重新设计
        for (int pos = 0;pos<actionMap.size();++pos) {
            LRGrammar.SetOfItems item = grammar.dfaMaker.getNode(pos);
            for (Relation_0 relation : item.relationSet) {
                if (relation.productionLength()==relation.dotPos) {
                    if (grammar.dfaMaker.getAccept().contains(grammar.dfaMaker.move(item,Terminal.end)))
                        buildActionMap(pos,Terminal.end,new Accept());
                    else
                        for (Terminal term : grammar.lrGrammar.getFollow(relation.original))
                            //if (actionMap.get(pos).get(term)!=null) throw new RuntimeException();
                            //actionMap.get(pos).put((Terminal) nowWord,new Action(pair.idOfSentence,entry.getKey()));
                            buildActionMap(pos, term, new Infer(relation));
                }
                else {
                    Word nowWord = relation.wordAtDot();
                    if (nowWord.wordType()==WordType.TERMINAL) {
                        Integer nextState = grammar.dfaMaker.moveById(pos, grammar.dfaMaker.getPathId(nowWord));
                        //if (actionMap.get(pos).get((Terminal) nowWord)!=null) throw new RuntimeException();
                        //actionMap.get(pos).put((Terminal) nowWord,new Action(nextState));
                        buildActionMap(pos,(Terminal) nowWord,new MoveTo(nextState));
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
        if (actionMap.get(itemPos).get(term)!=null&&!actionMap.get(itemPos).get(term).equals(action)) throw new RuntimeException();
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
            if (action==null|| !action.func(nowPos, stateStack))
                return false;
            else if (action.getClass().equals(Accept.class))
                return true;
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

    LRGrammar grammar;
    ArrayList<HashMap<Terminal, ActionOrGoto>> actionMap = new ArrayList<>();
    ArrayList<HashMap<Nonterminal, ActionOrGoto>> gotoMap = new ArrayList<>();
}

