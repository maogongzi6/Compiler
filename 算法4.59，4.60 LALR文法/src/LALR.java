import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LALR {
    class RelationWithItemId {
        RelationWithItemId(Relation_0 r,int i) {relation = r;id = i;}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RelationWithItemId that = (RelationWithItemId) o;
            return id == that.id &&
                    relation.equals(that.relation);
        }

        @Override
        public int hashCode() {
            return Objects.hash(relation, id);
        }

        @Override
        public String toString() {
            return "RelationWithItemId{" +
                    "relation=" + relation +
                    ", id=" + id +
                    '}';
        }

        Relation_0 relation;
        int id;
    }

    LALR(LRGrammar lrg) {
        lrGrammar = lrg; lr_1 = new LR_1(lrg.lrGrammar);
        lrGrammar.items();                                                                                      //书中的第一步，构建LR_0在这里，因为接下来要用到dfa属性
        for (int count = 0;count<lrg.dfaMaker.getNodeNum();++count)
            for (Relation_0 relation: lrg.dfaMaker.getNode(count).originalRelation)
                lookForwardMap.put(new RelationWithItemId(relation,count),new HashSet<>());
    }

    void calculateLALR() {
                                                                                        //第一步在初始化时完成
        for (int count = 0;count<lrGrammar.dfaMaker.getNodeNum();++count) {
            LRGrammar.SetOfItems item_0 = lrGrammar.dfaMaker.getNode(count);
            determineTheOrigin(item_0);                                                 //对每个item操作
        }
        for (Relation_0 relation: lrGrammar.dfaMaker.getStart().originalRelation) {
            lookForwardMap.get(new RelationWithItemId(relation,lrGrammar.dfaMaker.getStartId())).add(Terminal.end);
        }
        boolean ifChanged;
        do {                                                                                    //这里是传播符号
            ifChanged = false;
            for (Map.Entry<RelationWithItemId,HashSet<Terminal>> entry: lookForwardMap.entrySet()) {
                if (translationMap.get(entry.getKey().relation)!=null) {
                    for (RelationWithItemId relation: translationMap.get(entry.getKey().relation)) {
                        ifChanged = lookForwardMap.get(relation).addAll(lookForwardMap.get(entry.getKey())) || ifChanged;
                    }
                }
            }
        } while (ifChanged);
    }

    void determineTheOrigin(LRGrammar.SetOfItems item_0) {
        for (Relation_0 relation_0 : item_0.originalRelation) {
            LR_1.SetOfItems item_1 = lr_1.new SetOfItems(relation_0, relation_0.dotPos,Terminal.spread);//这里用文中的#（就是这里的spread）构造LR_1 item对其求closure
            for (Relation_1 relation_1: item_1.relationSet) {
                if (relation_1.dotPos!=relation_1.productionLength()) {                                //如果·不在句末
                    //LRGrammar.SetOfItems i = item_0.getItemFromLR_1(item_1);
                    LR_1.SetOfItems newItem = lr_1.new SetOfItems(relation_1,relation_1.dotPos+1,relation_1.next);
                    //这里我们无法使用lr1的move，因为lr1在本算法中没有构建dfa（本算法目的就是为了避免lr1构造dfa所造成的大量空间浪费），因此我们使用relation_1,relation_1.dotPos+1,relation_1.next这三个参数新建一个newItem，模拟move操作
                    for (Relation_1 relation :newItem.originalSet) {
                        Relation_0 newRelation = getRelation_0(relation);              //这里转化为Relation_0为了和下面的调用一致，而且这里使用Relation_0就足够了
                        if (relation_1.wordAtDot()==null) {
                            continue;                                                       //!!!!!!这里我觉得如果是推出IPSLON就应该直接结束传播？，意义上应该是这样的，结果也正确
                        }
                        if (relation.next != Terminal.spread) {//如果next不是#，判定targetId中的next是自发生成，完善自发生成表
                            lookForwardMap.get(new RelationWithItemId(newRelation,lrGrammar.dfaMaker.getNodeId(lrGrammar.dfaMaker.move(item_0,relation_1.wordAtDot())))).add(relation_1.next);
                            //这里我们必须知道newRelation对应的stateId，所以我们对item_0使用关于relation_1.wordAtDot()的move，得到下一个状态也就是newRelation对应的状态的id，然后我们生成一个暂时的RelationWithItemId，用于查询
                        }
                        else {                                                                                          //传播的到，填写传播表
                            if (!translationMap.containsKey(relation_0))
                                translationMap.put(relation_0, new HashSet<>());
                            translationMap.get(relation_0).add(new RelationWithItemId(newRelation,lrGrammar.dfaMaker.getNodeId(lrGrammar.dfaMaker.move(item_0,relation_1.wordAtDot()))));//与上一长句相同
                        }
                    }
                }
            }
        }
    }

    public Relation_0 getRelation_0(Relation_1 relation_1) {                                 //使用一个lr_1 item 构建一个 lr_0 item
        return new Relation_0(relation_1,relation_1.dotPos);
    }
    public HashSet<Relation_0> getRelationSet_0(HashSet<Relation_1> relationSet_1) {
        HashSet<Relation_0> relationSet_0 = new HashSet<>();
        for (Relation_1 relation_1: relationSet_1)
            relationSet_0.add(getRelation_0(relation_1));
        return relationSet_0;
    }

    public void buildDfa() {
        HashMap<Integer, LR_1.SetOfItems> idToItemSet = new HashMap<>();
        lalr = new LR_1(lrGrammar.lrGrammar);
        for (HashMap.Entry<RelationWithItemId,HashSet<Terminal>> entry: lookForwardMap.entrySet()) {
            RelationWithItemId key = entry.getKey();
            HashSet<Terminal> value = entry.getValue();
            if (!idToItemSet.containsKey(key.id)) {
                LR_1.SetOfItems item = lalr.new SetOfItems();
                if (key.relation.original.equals(lrGrammar.newStart)&&key.relation.dotPos==0)
                    begin = item;
                idToItemSet.put(key.id, item);
            }
            for (Terminal next: value)
                idToItemSet.get(key.id).addOriginal(key.relation,key.relation.dotPos,next);
        }
        LR_1.SetOfItems acceptItem = lalr.new AcceptItem();
        HashSet<LR_1.SetOfItems> nodes = new HashSet<>(idToItemSet.values()){{add(acceptItem);}};
        HashSet<Word> paths = new HashSet<>(lalr.grammar.getNonTerms()){{addAll(lalr.grammar.getTerms());}};
        HashSet<LR_1.SetOfItems> acc = new HashSet<>(){{add(acceptItem);}};
        DfaMachine<LR_1.SetOfItems,Word> dfa = new DfaMachine<>(nodes,paths,begin,acc);
        HashMap<HashSet<Relation_0>, LR_1.SetOfItems> originalToItem = new HashMap<>();
        for (LR_1.SetOfItems value: idToItemSet.values()) {
            HashSet<Relation_0> relationSet_0 = getRelationSet_0(value.originalSet);
            originalToItem.put(relationSet_0, value);
        }
        for (LR_1.SetOfItems item: idToItemSet.values()) {
            HashMap<Word,HashSet<Relation_1>> wordToEnd = moveOn(item);
            for (Map.Entry<Word,HashSet<Relation_1>> entry: wordToEnd.entrySet()) {
                LR_1.SetOfItems end = originalToItem.get(getRelationSet_0(entry.getValue()));
                dfa.addPath(item,end,entry.getKey());
            }
        }
        dfa.addPath(dfa.runDfa(new Word[]{lrGrammar.oldStart}),acceptItem,Terminal.end);
        lalr.dfaMaker = dfa;
    }

    HashMap<Word,HashSet<Relation_1>> moveOn(LR_1.SetOfItems begin) {
        HashMap<Word,HashSet<Relation_1>> wordToEnd = new HashMap<>();
        for (Relation_1 relation_1 :begin.relationSet) {
            if (relation_1.dotPos!=relation_1.productionLength()) {
                if (!wordToEnd.containsKey(relation_1.wordAtDot()))
                    wordToEnd.put(relation_1.wordAtDot(),new HashSet<>());
                wordToEnd.get(relation_1.wordAtDot()).add(new Relation_1(relation_1,relation_1.dotPos+1,relation_1.next));
            }
        }
        return wordToEnd;
    }

    void makeTable() {lalrTable = new LRTable(lalr); lalrTable.buildTable();}

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(lrGrammar).append("\n");
        builder.append("*********************lookForward:  \n");
        for (Map.Entry<RelationWithItemId,HashSet<Terminal>> entry: lookForwardMap.entrySet()) {
            builder.append(entry.getKey()).append(": ");
            builder.append(entry.getValue()).append("\n");
        }
        builder.append("spread:  \n");
        for (Map.Entry<Relation_0,HashSet<RelationWithItemId>> entry : translationMap.entrySet()) {
            builder.append(entry.getKey()).append(": ");
            for (RelationWithItemId i : entry.getValue())
                builder.append(i).append("\n");
        }
        builder.append("\n").append(lalr.dfaMaker);
        builder.append("\n").append(lalrTable);
        return builder.toString();
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        //ArrayList<String> nonTerms = new ArrayList<>(List.of("S","C")), terms = new ArrayList<>(List.of("c","d")),
               // begins = new ArrayList<>(List.of("S","C","C")),ends = new ArrayList<>(List.of("CC","cC","d"));
        GUI_exercise g = new GUI_exercise();
        g.setframe();
        g.setComponent();
        g.listener();
        HashSet<String> nonTerms = new HashSet<>(),terms = new HashSet<>();
        ArrayList<String> begins = new ArrayList<>();
        ArrayList<ArrayList<String>> end = new ArrayList<>();
        loadGrammar(nonTerms,terms,begins,end);
        Grammar grammar = new Grammar(nonTerms,terms,begins,end,1);
        LRGrammar lrGrammar = new LRGrammar(grammar);
        LALR lalr = new LALR(lrGrammar);
        //System.out.print(lalr.lrGrammar);
        lalr.calculateLALR();
        lalr.buildDfa();
        lalr.makeTable();
        Lexer lexer = new Lexer();
        lexer.lexicalAnalyse("in2.txt");
        System.out.println(lalr.lalrTable.runLrWithCode(lalr.lalr.translate_2(lexer.lexResult)));
        System.out.println(lalr.lalrTable.tree);
        lalr.lalrTable.runTheTree();
        StreamMap map = new StreamMap(lalr.lalrTable.code);
        map.completeTable();
        ArrayList<Msil> newCode = DAGNode.makeDAG(map);
        ArrayList<String> target = TargetCode.makeTargetCode(newCode);
        FileOutputStream out=new FileOutputStream(new File("result.txt"));
        for (String s: target) {
            s = s+"\n";
            out.write(s.getBytes());
        }

    }

    static void loadGrammar(HashSet<String> nonTerms, HashSet<String> terms, ArrayList<String> begin, ArrayList<ArrayList<String>> end) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("my.txt"));
        String str;
        while ((str = br.readLine())!=null) {
            str = str.replace(" ","");
            if (str.equals("")) continue;
            String [] sentences = str.split("[~$]");
            nonTerms.add(sentences[0].replace("<","").replace(">",""));
            for (int pos = 1; pos< sentences.length; ++pos) {
                ArrayList<String> product = new ArrayList<>();
                begin.add(sentences[0].replace("<","").replace(">",""));
                Matcher m = Pattern.compile("(<.+?>)|(\\[.+?])|(\\{.+?})").matcher(sentences[pos]);
                while (m.find()) {
                    String s = m.group();
                    if (s.charAt(0)=='<')
                        nonTerms.add(s=s.substring(1,s.length()-1));
                    else if (s.charAt(0)=='[') {
                        if (s.equals("[\\]")) {s="]";terms.add(s);}                                                                           //"\"是转义符，代表实际值为"]"
                        else terms.add(s=s.substring(1,s.length()-1));
                    }
                    else s=s.substring(1,s.length()-1);
                    product.add(s);
                }
                end.add(product);
            }
        }
    }



    ArrayList<String> loadFile(String input) {
        ArrayList<String> output = new ArrayList<>(Arrays.asList(input.split("[ \\n]")));
        for (int i = 0;i<output.size();++i) output.set(i,"["+output.get(i)+"]");
        return output;
    }

    LR_1.SetOfItems begin;
    LRGrammar lrGrammar;
    LR_1 lr_1;
    LR_1 lalr;
    LRTable lalrTable;
    HashMap<RelationWithItemId,HashSet<Terminal>> lookForwardMap = new HashMap<>();     //这里必须使用新类，其中包含了Relation和Relation所在的stateId，否则会漏掉一些情况
    HashMap<Relation_0,HashSet<RelationWithItemId>> translationMap = new HashMap<>();
}
   // ArrayList<String> nonTerms = new ArrayList<>(List.of("S","L","R")), terms = new ArrayList<>(List.of("i","*","=")),
        //    begins = new ArrayList<>(List.of("S","S","L","L","R")),ends = new ArrayList<>(List.of("L=R","R","*R","i","L"));
   //ArrayList<String> nonTerms = new ArrayList<>(List.of("S","C")), terms = new ArrayList<>(List.of("c","d")),
   //        begins = new ArrayList<>(List.of("S","C","C")),ends = new ArrayList<>(List.of("CC","cC","d"));
