import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LR_1 {
    class AcceptItem extends SetOfItems {
        AcceptItem() {}

        @Override
        public String toString() {
            return "AcceptItem ";
        }

        @Override
        boolean addOriginal(Sentence sentence, int pos, Terminal term) {throw new RuntimeException(); }
        @Override
        boolean add(Sentence sentence, int pos, Terminal term) {throw new RuntimeException(); }
        @Override
        HashMap<Word, SetOfItems> moveFront(Relation_1 relation, HashMap<Word,SetOfItems> wordToItems) {throw new RuntimeException(); }
        @Override
        SetOfItems formClosure(Relation_1 original) {throw new RuntimeException(); }
    }

    class SetOfItems {
        SetOfItems() {}

        SetOfItems(Sentence sentence,Terminal term) {
            addOriginal(sentence,0,term);
        }

        SetOfItems(Sentence sentence,int pos,Terminal term) {
            addOriginal(sentence,pos,term);
        }

        boolean addOriginal(Sentence sentence, int pos, Terminal term) {
            Relation_1 relation = new Relation_1(sentence,pos,term);
            originalSet.add(relation);
            if (relationSet.add(relation)) {
                formClosure(relation);
                return true;
            }
            return false;
        }

        boolean add(Sentence sentence, int pos, Terminal term) {
            return relationSet.add(new Relation_1(sentence,pos,term));
        }

        HashMap<Word,SetOfItems> moveFront(Relation_1 relation, HashMap<Word,SetOfItems> wordToItems) {
            if (relation.production.get(0).specialType()==SpecialWord.IPSLON) return wordToItems;                   //！！！！！！！！！！这里特殊考虑IPSLON，是设计上的大问题
            if (relation.dotPos != relation.productionLength()) {
                Word word = relation.wordAtDot();
                if (wordToItems.get(word)==null)
                    wordToItems.put(word, new SetOfItems(relation, relation.dotPos + 1,relation.next));
                else
                    wordToItems.get(word).addOriginal(relation,relation.dotPos+1,relation.next);
            }
            return wordToItems;
        }

        SetOfItems formClosure(Relation_1 original) {
            LinkedList<Relation_1> stack = new LinkedList<>(){{push(original); }};
            while (!stack.isEmpty()) {
                Relation_1 now = stack.pop();
                Word theWord = now.wordAtDot();
                if (!(theWord==null||theWord.wordType()==WordType.TERMINAL || theWord.specialType()==SpecialWord.IPSLON||theWord.wordType()==WordType.ACTION))
                    for (Sentence sentence: ((Nonterminal)theWord).sentences) {
                        ArrayList<Word> beta_a = new ArrayList<>(now.production.subList(now.dotPos+1,now.productionLength()));
                        beta_a.add(original.next);
                        for (Terminal next : grammar.getFirst(beta_a)) {
                            Relation_1 newRelation = new Relation_1(sentence,next);
                            if (add(newRelation,0,next))
                                stack.push(newRelation);
                        }
                    }
            }
            return this;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj==null||obj.getClass()!=getClass()) return false;
            if (obj==this) return true;
            return originalSet.equals(((SetOfItems)obj).originalSet);
        }

        @Override
        public int hashCode() {
            return originalSet.hashCode();
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            for (Relation_1 relation: relationSet)
                result.append("(").append(relation).append(") ,");
            return result.toString();
        }

        HashSet<Relation_1> relationSet = new HashSet<>();
        HashSet<Relation_1> originalSet = new HashSet<>();
    }

    LR_1(Grammar input) {                                   //这又两个版本的构造函数，区别在于是否扩张文法（如果扩张过就不必再扩张）
        acceptItem = new AcceptItem();
        grammar = input;
        //augmentGrammar();
        grammar.calculateFirst();
        grammar.calculateFollow();
    }

    LR_1(Grammar input,String newStart) {
        acceptItem = new AcceptItem();
        grammar = input;
        augmentGrammar(newStart);
        grammar.calculateFirst();
        grammar.calculateFollow();
    }

    Grammar augmentGrammar(String ns) {
        newStart = new Nonterminal(ns);
        grammar.addNonterminal(newStart);
        newStart.add(new ArrayList<>(){{add(grammar.getStart());}});
        oldStart = grammar.getStart();
        grammar.setStart(newStart);
        return grammar;
    }

    void items() {
        class Path {                                                                //使用Path保存待生成的DFA路径的begin,end,path
            private Path(SetOfItems b, SetOfItems e, Word p) {
                begin = b;
                end = e;
                path = p;
            }

            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime*result+((begin==null) ? 0 : begin.hashCode());
                result = prime*result+(((end==null)) ? 0 : end.hashCode());
                return prime*result+((path==null) ? 0 : path.hashCode());
            }

            @Override
            public boolean equals(Object obj) {
                if (obj==null||obj.getClass()!=getClass()) return false;
                if (obj==this) return true;
                return begin == ((Path) obj).begin && end == ((Path) obj).end && path == ((Path) obj).path;
            }

            private SetOfItems begin;
            private SetOfItems end;
            private Word path;
        }

        HashSet<Path> pathSet = new HashSet<>();
        SetOfItems begin = new SetOfItems(grammar.getStart().sentences.get(0),Terminal.end);
        HashSet<SetOfItems> closureSet = new HashSet<>() {{add(begin);}};
        LinkedList<SetOfItems> stack = new LinkedList<>() {{push(begin);}};
        while (!stack.isEmpty()) {
            SetOfItems now = stack.pop();
            HashMap<Word,SetOfItems> wordToItems = new HashMap<>();
            for (Relation_1 relation : now.relationSet)
                now.moveFront(relation,wordToItems);
            for (Map.Entry<Word,SetOfItems> entryOfWI : wordToItems.entrySet()) {
                Path path = new Path(now,entryOfWI.getValue(),entryOfWI.getKey());
                pathSet.add(path);
                if (closureSet.add(entryOfWI.getValue()))
                    stack.push(entryOfWI.getValue());
            }
        }
        closureSet.add(acceptItem);                                                                 //加入终止状态
        dfaMaker = new DfaMachine<>(closureSet,new HashSet<>(grammar.getNonTerms()) {{addAll(grammar.getTerms());}},begin,new HashSet<>() {{add(acceptItem);}});
        for (Path path :pathSet)
            dfaMaker.addPath(path.begin,path.end,path.path);
        SetOfItems result = dfaMaker.runDfa(new Word[] {oldStart});                 //这一步得到包含oldStart的state
        dfaMaker.addPath(result,acceptItem,Terminal.end);                           //加入到acceptItem的path
    }

    @Override
    public String toString() {
        return grammar.toString() + "\n\n" + dfaMaker.toString();
    }

    Nonterminal newStart;
    Nonterminal oldStart;
    Grammar grammar;
    DfaMachine<SetOfItems,Word> dfaMaker;
    private AcceptItem acceptItem;                           //这里没有办法像dot一样写为static，因为SetOfItems是inner class不能拥有自己内部的static class，
    // 只能退一步让每个LRGrammar都持有一个acceptItem，这样处理不是很好，但也说得过去

    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        //ArrayList<String> nonTerms = new ArrayList<>(List.of("S","C")), terms = new ArrayList<>(List.of("c","d")),
         //       begins = new ArrayList<>(List.of("S","C","C")),ends = new ArrayList<>(List.of("CC","cC","d"));
        GUI_exercise g = new GUI_exercise();
        g.setframe();
        g.setComponent();
        g.listener();
        HashSet<String> nonTerms = new HashSet<>(), terms = new HashSet<>();
        ArrayList<String> begins = new ArrayList<>();
        ArrayList<ArrayList<String>> ends = new ArrayList<>();
        loadGrammar(nonTerms,terms,begins,ends);
        Grammar grammar = new Grammar(nonTerms,terms,begins,ends,1);
        //grammar.print();
        LR_1 lrG = new LR_1(grammar,"S*");
        lrG.items();
        System.out.println(lrG);
        LRTable lrTable = new LRTable(lrG);
        lrTable.buildTable();
        Lexer lexer = new Lexer();
        lexer.lexicalAnalyse("in3.txt");
        //System.out.println(lrTable.runLR(lrG.translate(lrG.loadFile("D:\\myinput.txt"))));
        //System.out.println(lrTable.runLrWithCode(lrG.translate(lrG.loadFile("D:\\myinput.txt"))));
        //ArrayList<Terminal> list =  lrG.translate_2(lexer.lexResult);
        System.out.println(lrTable.runLrWithCode(lrG.translate_2(lexer.lexResult)));
        System.out.println(lrTable.tree);
        lrTable.runTheTree();
        System.out.println(lrTable.code);
        StreamMap map = new StreamMap(lrTable.code);
        map.completeTable();
        ArrayList<Msil> newCode = DAGNode.makeDAG(map);
        ArrayList<String> target = TargetCode.makeTargetCode(newCode);
        FileOutputStream out=new FileOutputStream(new File("result.txt"));
        for (String s: target) {
            s = s+"\n";
            out.write(s.getBytes());
        }
        //System.out.println(map.map);
        /*"include < ID > ;\n" +
                "include < ID > ;\n" +
                "void ID ( int ID ) {\n" +
                "if ( ID > ID ) {\n" +
                "while ( ID > ID ) do {\n" +
                "ID = digital * digital + digital ;\n" +
                "} ;\n" +
                "ID = digital ;\n" +
                "}\n" +
                "ID = digital ;\n" +
                "}"))));*/
        //"if ( ID > ID ) { ID = digital ; }"
        //new ArrayList<>(){{add("[if]");add("[(]");add("[ID]");add("[>]");add("[ID]");add("[)]");add("[{]");add("[ID]");add("[=]");add("[digital]");add("[;]");add("[}]"); }}))
        //System.out.print(lrTable);
    }

    ArrayList<Terminal> translate(ArrayList<String> input) {                                //这个是翻译成纯语法分析的输入
        ArrayList<Terminal> terms = new ArrayList<>();
        for (String word: input)
            terms.add(grammar.nameToTerminal.get(word));
        return terms;
    }

    ArrayList<Terminal> translate_2(ArrayList<Lex> input) {
        ArrayList<Terminal> terms = new ArrayList<>();
        for (Lex word: input) {
            terms.add(new Terminal(word));
        }
        return terms;
    }

    //ArrayList<Terminal> translate_2(ArrayList<String> input) {                      //这个翻译为包含语义分析所需动作的输入
    //}

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

    ArrayList<String> loadFile(String fileName) throws IOException {            //这个函数要改，如果要用的话
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        ArrayList<String> output = new ArrayList<>();
        String input;
        while ((input = br.readLine())!=null)
            output.addAll(Arrays.asList(input.split("[ \\n]")));
        for (int i = 0;i<output.size();++i) output.set(i,"["+output.get(i)+"]");
        return output;
    }

}

//ArrayList<String> nonTerms = new ArrayList<>(List.of("E","T","F")), terms = new ArrayList<>(List.of("+","*","(",")","i")),
//                begins = new ArrayList<>(List.of("E","E","T","T","F","F")),ends = new ArrayList<>(List.of("E+T","T","T*F","F","(E)","i"));

