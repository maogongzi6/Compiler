import java.util.*;

public class LRGrammar {
    /*static class Dot extends Terminal {
        private Dot(Lex lex) {
            super(lex);
        }
    }*/

    /*class Pair {
        Pair(Integer dFS,int dP) {
            idOfSentence = dFS;
            dotPos = dP;
        }

        Word wordAtDot() {
            if (lrGrammar.getById(idOfSentence).size()==dotPos) return null;
            return lrGrammar.getById(idOfSentence).get(dotPos);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime*result+idOfSentence.hashCode();
            return prime*result+Integer.hashCode(dotPos);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj==null||obj.getClass()!=getClass()) return false;
            if (obj==this) return true;
            return idOfSentence.equals(((Pair) obj).idOfSentence)&&dotPos==((Pair) obj).dotPos;
        }

        @Override
        public String toString() {
            ArrayList<Word> sentence = new ArrayList<>(lrGrammar.getById(idOfSentence));
            sentence.add(dotPos,dot);
            return sentence.toString().replaceAll(",","");
        }

        Integer idOfSentence;
        int dotPos;
    }*/

    static class AcceptItem extends SetOfItems {
        private AcceptItem() {}

        @Override
        public String toString() {
            return "AcceptItem ";
        }

        @Override
        boolean containSentence(Relation_0 sentence) {throw new RuntimeException(); }
        @Override
        boolean addOriginal(Sentence sentence, int pos) {throw new RuntimeException(); }
        @Override
        boolean add(Sentence sentence) {throw new RuntimeException(); }
        @Override
        HashMap<Word, SetOfItems> moveFront(Relation_0 relation, HashMap<Word, SetOfItems> wordToItems) {throw new RuntimeException(); }
        @Override
        SetOfItems formClosure(Relation_0 relation) {throw new RuntimeException(); }
    }

    static class SetOfItems {
        private SetOfItems() {}

        SetOfItems(Sentence sentence) {
            addOriginal(sentence,0);
        }

        SetOfItems(Sentence sentence,int pos) {
            addOriginal(sentence,pos);
        }

        boolean containSentence(Relation_0 relation) {
            return relationSet.contains(relation);
        }

        boolean addOriginal(Sentence sentence, int pos) {
            Relation_0 relation = new Relation_0(sentence,pos);
            originalRelation.add(relation);
            if (relationSet.add(relation)) {
                formClosure(relation);
                return true;
            }
            return false;
        }

        boolean add(Sentence sentence) {
            return relationSet.add(new Relation_0(sentence, 0));
        }

        HashMap<Word,SetOfItems> moveFront(Relation_0 relation, HashMap<Word,SetOfItems> wordToItems) {
            if (relation.dotPos != relation.productionLength()) {
                Word word = relation.wordAtDot();
                if (wordToItems.get(word)==null)
                    wordToItems.put(word, new SetOfItems(relation, relation.dotPos + 1));
                else
                    wordToItems.get(word).addOriginal(relation,relation.dotPos+1);
            }
            return wordToItems;
        }

        SetOfItems formClosure(Relation_0 original) {
            LinkedList<Relation_0> stack = new LinkedList<>(){{push(original); }};
            while (!stack.isEmpty()) {
                Relation_0 now = stack.pop();
                Word theWord = now.wordAtDot();
                if (!(theWord==null||theWord.wordType()==WordType.TERMINAL || theWord.specialType()==SpecialWord.IPSLON))
                    for (Sentence sentence: ((Nonterminal)theWord).sentences) {
                        Relation_0 newRelation = new Relation_0(sentence);
                        if (add(newRelation))
                            stack.push(newRelation);
                    }
            }
            return this;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj==null||obj.getClass()!=getClass()) return false;
            if (obj==this) return true;
            return originalRelation.equals(((SetOfItems)obj).originalRelation);
        }

        @Override
        public int hashCode() {
            return originalRelation.hashCode();
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            for (Relation_0 relation: relationSet)
                result.append("(").append(relation).append(") ,");
            return result.toString();
        }

        HashSet<Relation_0> relationSet = new HashSet<>();
        HashSet<Relation_0> originalRelation = new HashSet<>();
    }

    LRGrammar(Grammar input) {
        acceptItem = new AcceptItem();
        lrGrammar = input;
        augmentGrammar();
        lrGrammar.calculateFirst();
        lrGrammar.calculateFollow();
    }

    Grammar augmentGrammar() {                                          //这是为了的到扩展后的文法
        newStart = new Nonterminal("S*");
        lrGrammar.addNonterminal(newStart);
        newStart.add(new ArrayList<>(){{add(lrGrammar.getStart());}});
        oldStart = lrGrammar.getStart();
        lrGrammar.setStart(newStart);
        //lrGrammar.makeIdToSentence();
        return lrGrammar;
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
                //return begin.equals(((Path) obj).begin) && end.equals(((Path) obj).end) && path.equals(((Path) obj).path);

                return begin == ((Path) obj).begin && end == ((Path) obj).end && path == ((Path) obj).path;
            }

            private SetOfItems begin;
            private SetOfItems end;
            private Word path;
        }

        HashSet<Path> pathSet = new HashSet<>();
        SetOfItems begin = new SetOfItems(lrGrammar.getStart().sentences.get(0));
        HashSet<SetOfItems> closureSet = new HashSet<>() {{add(begin);}};
        LinkedList<SetOfItems> stack = new LinkedList<>() {{push(begin);}};
        while (!stack.isEmpty()) {
            SetOfItems now = stack.pop();
            HashMap<Word,SetOfItems> wordToItems = new HashMap<>();
            for (Relation_0 relation : now.relationSet)
                now.moveFront(relation,wordToItems);
            for (Map.Entry<Word,SetOfItems> entryOfWI : wordToItems.entrySet()) {
                Path path = new Path(now,entryOfWI.getValue(),entryOfWI.getKey());
                pathSet.add(path);
                if (closureSet.add(entryOfWI.getValue()))
                    stack.push(entryOfWI.getValue());
            }
        }
        closureSet.add(acceptItem);                                                                 //加入终止状态
        dfaMaker = new DfaMachine<>(closureSet,new HashSet<>(lrGrammar.getNonTerms()) {{addAll(lrGrammar.getTerms());}},begin,new HashSet<>() {{add(acceptItem);}});
        for (Path path :pathSet)
            dfaMaker.addPath(path.begin,path.end,path.path);
        SetOfItems result = dfaMaker.runDfa(new Word[] {oldStart});
        dfaMaker.addPath(result,acceptItem,Terminal.end);
    }



    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(lrGrammar.toString()).append("\n\n").append(dfaMaker.toString());
        return result.toString();
    }

    Nonterminal newStart;
    Nonterminal oldStart;
    Grammar lrGrammar;
    DfaMachine<SetOfItems,Word> dfaMaker;
    private AcceptItem acceptItem;                           //这里没有办法像dot一样写为static，因为SetOfItems是inner class不能拥有自己内部的static class，
                                                                        // 只能退一步让每个LRGrammar都持有一个acceptItem，这样处理不是很好，但也说得过去

    public static void main(String[] args) {
        HashSet<String> nonTerms = new HashSet<>(List.of("E","T","F")), terms = new HashSet<>(List.of("+","*","(",")","i"));
        ArrayList<String> begins = new ArrayList<>(List.of("E","E","T","T","F","F")),ends = new ArrayList<>(List.of("E+T","T","T*F","F","(E)","i"));
        Grammar grammar = new Grammar(nonTerms,terms,begins,ends);
        System.out.print("asadada");
        LRGrammar lrG = new LRGrammar(grammar);
        lrG.items();
        System.out.println(lrG);
        SLRTable slrTable = new SLRTable(lrG);
        slrTable.buildTable();
        System.out.print(slrTable);
    }

}
//        ArrayList<String> nonTerms = new ArrayList<>(List.of("E","T","F")), terms = new ArrayList<>(List.of("+","-","*","/","(",")","i")),
//                begins = new ArrayList<>(List.of("E","E","E","T","T","T","F","F")),ends = new ArrayList<>(List.of("E+T","E-T","T","T*F","T/F","F","(E)","i"));