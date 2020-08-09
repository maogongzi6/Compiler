import java.util.*;

public class Grammar {
    Grammar(HashSet<String> nonTerms,HashSet<String> terms,ArrayList<String> begins,ArrayList<String> ends) {
        nameToTerminal = new LinkedHashMap<>();
        nameToNonterminal = new LinkedHashMap<>();
        for (String term: terms)
            nameToTerminal.put(term,new Terminal(new Lex(term)));
        for (String nonTerm: nonTerms)
            nameToNonterminal.put(nonTerm,new Nonterminal(nonTerm));
        for (int i = 0;i<begins.size();++i) {
            Nonterminal begin = nameToNonterminal.get(begins.get(i));
            ArrayList<Word> result = new ArrayList<>();
            String end = ends.get(i);
            for (int charAt = 0;charAt<end.length();++charAt) {
                char letter = end.charAt(charAt);
                Word word = null;
                if (nameToNonterminal.get(Character.toString(letter))!=null)
                    word = nameToNonterminal.get(Character.toString(letter));
                else if (nameToTerminal.get(Character.toString(letter))!=null)
                    word = nameToTerminal.get(Character.toString(letter));
                else word = Nonterminal.ipslon;
                result.add(word);
            }
            begin.add(result);
        }
        begin = nameToNonterminal.get(begins.get(0));
        terminals = new ArrayList<>(nameToTerminal.values());
        derivations = new ArrayList<>(nameToNonterminal.values());
        //end = new Terminal(new Lex("$"));
        terminals.add(Terminal.end);
        terminals.add(Terminal.spread);
        terminals.add(Terminal.hidenIpslon);
        derivations.add(Nonterminal.ipslon);
    }

    Grammar(HashSet<String> nonTerms, HashSet<String> terms,ArrayList<String> begins,ArrayList<ArrayList<String>> ends,int t) {
        nameToTerminal = new LinkedHashMap<>();
        nameToNonterminal = new LinkedHashMap<>();
        for (String term: terms)
            nameToTerminal.put(term,new Terminal(new Lex(term)));
        for (String nonTerm: nonTerms)
            nameToNonterminal.put(nonTerm,new Nonterminal(nonTerm));
        nameToTerminal.put(Terminal.hidenIpslon.term.name,Terminal.hidenIpslon);
        nameToNonterminal.replace(Nonterminal.ipslon.name,Nonterminal.ipslon);
        for (int i = 0;i<begins.size();++i) {
            Nonterminal begin = nameToNonterminal.get(begins.get(i));
            ArrayList<Word> result = new ArrayList<>();
            ArrayList<String> end = ends.get(i);
            for (int charAt = 0;charAt<end.size();++charAt) {
                String letter = end.get(charAt);
                Word word = null;
                if (nameToNonterminal.get(letter)!=null)
                    word = nameToNonterminal.get(letter);
                else if (nameToTerminal.get(letter)!=null)
                    word = nameToTerminal.get(letter);
                else if (!letter.equals(Nonterminal.ipslon.name)) word = new Code(letter);
                else word = Nonterminal.ipslon;
                result.add(word);
            }
            begin.add(result);
        }
        begin = nameToNonterminal.get(begins.get(0));
        terminals = new ArrayList<>(nameToTerminal.values());
        derivations = new ArrayList<>(nameToNonterminal.values());
        //end = new Terminal(new Lex("$"));
        terminals.add(Terminal.end);
        terminals.add(Terminal.spread);
        terminals.add(Terminal.hidenIpslon);
        derivations.add(Nonterminal.ipslon);
    }


    boolean setStart(Nonterminal newStart) {
        if (derivations.contains(newStart)) {
            begin = newStart;
            return true;
        }
        return false;
    }

    ArrayList<Nonterminal> getNonTerms() {return derivations; }
    ArrayList<Terminal> getTerms() {return terminals; }

    Nonterminal getStart() {return begin; }

    boolean addNonterminal(Nonterminal nt) {return derivations.add(nt);}            //添加一个非终结符

    void deleteLeftRecursion() {                                                    //删除左递归
        HashMap<Nonterminal,Integer> nameToOrder = new HashMap<>();
        for (int i = 0;i<derivations.size();++i)                                    //按照一个顺序保存非终结符
            nameToOrder.put(derivations.get(i),i);
        for (int i = 0;i<derivations.size();++i) {                                  //按照算法操作，这里的for不能改成for(:)，因为在循环中添加或删除了容器中的元素
            Nonterminal target = derivations.get(i);
            for (int pos = 0; pos<target.sentences.size(); ++pos) {
                Sentence sentence = target.sentences.get(pos);
                if (!(sentence.wordAt(0).wordType()==WordType.TERMINAL|| sentence.wordAt(0).specialType()==SpecialWord.IPSLON)&&nameToOrder.get((Nonterminal) sentence.wordAt(0))<nameToOrder.get(target)) {
                    target.sentences.remove(sentence);
                    for (Sentence productSentence : ((Nonterminal) sentence.wordAt(0)).sentences) {
                        Sentence newSentence = new Sentence(productSentence);
                        newSentence.addAll(sentence);
                        newSentence.remove(newSentence.productionLength()- sentence.productionLength());
                        target.add(newSentence);
                    }
                }
            }
            deleteDirectLeftRecursion(target,nameToOrder);
        }
    }

    void deleteDirectLeftRecursion(Nonterminal target,HashMap<Nonterminal,Integer> nameToOrder) {           //删除直接左递归
        ArrayList<Sentence> leftRecursion = new ArrayList<>(), others = new ArrayList<>();
        for (Sentence sentence : target.sentences) {                                                   //判断是不是左递归
            if (sentence.wordAt(0).equals(target))
                leftRecursion.add(sentence);
            else others.add(sentence);
        }
        if (leftRecursion.size()!=0) {
            Nonterminal newWord = new Nonterminal(target.name+"'");                                     //生成的新非终结符加上" ' "
            addNonterminal(newWord);
            nameToOrder.put(newWord,nameToOrder.size()-1);
            target.sentences = new LinkedList<>();
            for (Sentence recursSentence : leftRecursion) {
                recursSentence.remove(0);
                recursSentence.add(newWord);
                newWord.add(recursSentence);
            }
            for (Sentence otherSentence : others) {
                otherSentence.add(newWord);
                target.add(otherSentence);
            }
            newWord.add(new ArrayList<>(){{add(Nonterminal.ipslon);}});    //最后加上ipslon
        }
    }

    void leftFactor() {
        for (int i = 0;i<derivations.size();++i) {                                          //先找到一个非终结符的所有转换中首字母相同那些转换，再在这些转换中提取尽可能多的公共首字母，之后转换
            Nonterminal nonTerm = derivations.get(i);                                       //转换得到的新终结符的转换也要加入到队列中，在之后进行检测
            HashMap<Word,ArrayList<Sentence>> firstToSentence = new HashMap<>();     //建立映射，从首字母到产生式
            for (Sentence sentence :nonTerm.sentences) {
                if (!firstToSentence.containsKey(sentence.wordAt(0)))
                    firstToSentence.put(sentence.wordAt(0),new ArrayList<>());
                firstToSentence.get(sentence.wordAt(0)).add(sentence);
            }
            for (Map.Entry<Word,ArrayList<Sentence>> entry: firstToSentence.entrySet()) {
                ArrayList<Sentence> sameLeft = entry.getValue();
                if (sameLeft.size()!=1) {
                    int sameLetterCount = 0;
                    Word nowWord = sameLeft.get(0).wordAt(sameLetterCount);
                    for (;sameLeft.get(0).productionLength() != sameLetterCount;++sameLetterCount)
                        for (Sentence sentence : sameLeft)
                            if (sameLetterCount== sentence.productionLength() || sentence.wordAt(sameLetterCount) != nowWord) break;        //计算最长相同前缀长度
                    Nonterminal newTerm = new Nonterminal(nonTerm.name+"^");                                            //产生的新终结符加"^"
                    addNonterminal(newTerm);
                    ArrayList<Word> replace = new ArrayList<>(sameLeft.get(0).subSentence(0,sameLetterCount));                  //开始根据前缀构造新的字符串
                    replace.add(newTerm);
                    nonTerm.add(replace);
                    for (int count = 0;count<sameLeft.size();++count) {
                        Sentence sentence = sameLeft.get(count);
                        Sentence copy = new Sentence(sentence);
                        copy.subSentence(0, sameLetterCount).clear();
                        newTerm.add(copy);
                        nonTerm.remove(sentence);
                    }
                }
            }
        }
    }

    void calculateFirst() {
        firstMap = new HashMap<>();
        for (Nonterminal expression : derivations)
            firstMap.put(expression,new HashSet<>());
        for (Terminal terminal: terminals)                                  //对于终结符来说，first就是自己
            firstMap.put(terminal,new HashSet<>(){{add(terminal);}});
        boolean ifChanged;
        do {                                                                //不断循环，直到没有新的first添加
            ifChanged = false;
            for (Nonterminal expression : derivations)
                ifChanged = calculateFirst(expression) || ifChanged;
        } while(ifChanged);
    }

    boolean calculateFirst(Nonterminal expression) {
        boolean ifChanged = false;
        for (Sentence sentence : expression.sentences) {
            for (int pos = 0; pos< sentence.productionLength(); ++pos) {
                Word word = sentence.wordAt(pos);
                if (word.wordType()==WordType.TERMINAL) {
                    ifChanged = firstMap.get(expression).add((Terminal) word) || ifChanged;           //更改ifChanged
                                    //!!!!这里非常重要，之前是吧ifChanged放在||前判断，虽然可以运行成功但是没有考虑到短路原理，其实只是凑巧成功，正确的做法是像现在这样后判断
                    break;                                                                          //如果是终结符就不继续操作
                }
                else {
                    HashSet<Terminal> copy = new HashSet<>(firstMap.get(word));
                    if (pos!= sentence.productionLength()-1)                                                       //如果不是表达式的最后一个字符，那么集合中不可能存在ipslon
                        copy.remove(Terminal.hidenIpslon);
                    ifChanged = firstMap.get(expression).addAll(copy) || ifChanged;
                    if (!firstMap.get(word).contains(Terminal.hidenIpslon))                         //如果这个非终结符的first集没有ipslon，也不继续操作
                        break;
                }
            }
        }
        return ifChanged;
    }

    HashSet<Terminal> getFirst(ArrayList<Word> sentence) {                                          //找出一个语句中的所有first，这个过程与calculateFirst的过程类似
        HashSet<Terminal> firstSet = new HashSet<>();
        for (int pos = 0;pos<sentence.size();++pos) {
            Word word = sentence.get(pos);
            HashSet<Terminal> firsts = new HashSet<>(getFirst(word));
            if (pos!=sentence.size()-1)
                firsts.remove(Terminal.hidenIpslon);
            firstSet.addAll(firsts);
            if (!getFirst(word).contains(Terminal.hidenIpslon))
                break;
        }
        return firstSet;
    }

    HashSet<Terminal> getFirst(Sentence sent) {return getFirst(sent.getProduction()); }

    HashSet<Terminal> getFirst(Word word) {return firstMap.get(word); }

    void calculateFollow() {
        followMap = new HashMap<>();
        for (Nonterminal expression : derivations)
            followMap.put(expression,new HashSet<>());
        followMap.get(begin).add(Terminal.end);                                  //followMap中要有终止字符
        boolean ifChanged;
        do {                                                            //不断循环，直到没有新的follow
            ifChanged = false;
            for (Nonterminal expression : derivations) {
                ifChanged = calculateFollow(expression) || ifChanged;
            }
        } while(ifChanged);
    }

    boolean calculateFollow(Nonterminal expression) {
        boolean ifChanged = false;
        for (Sentence sentence : expression.sentences) {
            for (int pos = 0; pos< sentence.productionLength()-1; ++pos) {                                       //这是第一条规则
                if (sentence.wordAt(pos).wordType()==WordType.TERMINAL|| sentence.wordAt(pos).specialType()==SpecialWord.IPSLON)
                    continue;
                ifChanged = followMap.get((Nonterminal) sentence.wordAt(pos)).addAll(new ArrayList<>(getFirst(sentence.subSentence(pos + 1,sentence.productionLength()))) {{ remove(Terminal.hidenIpslon); }}) || ifChanged;
            }
            int count = 0;
            Word word;
            do {                                                                                //这是第二条规则
                ++count;
                word = sentence.wordAt(sentence.productionLength()-count);
                if (!(word.wordType()==WordType.TERMINAL||word.specialType()==SpecialWord.IPSLON))
                    ifChanged = followMap.get(word).addAll(followMap.get(expression)) || ifChanged;
            } while(firstMap.get(word).contains(Terminal.hidenIpslon)&&count< sentence.productionLength());
        }
        return ifChanged;
    }

    HashSet<Terminal> getFollow(Nonterminal nonTerm) {return followMap.get(nonTerm); }

   /* void makeIdToSentence() {
        idToSentence = new DoubleWayMap<>();
        for (Nonterminal nonTerm : derivations)
            for (Sentence sentence : nonTerm.sentences)
                idToSentence.put(idToSentence.size(),sentence);
    }*/

   // ArrayList<Word> getById(Integer i) {return idToSentence.get(i); }
 //   Integer getBySentence(ArrayList<Word> sentence) {return idToSentence.traverse().get(sentence); }

    void makeCalculateTable() {                                                 //这里用一个id编号到产生式的映射，可以节约资源
        calculateTable = new HashMap<>();
        //makeIdToSentence();
        //idToSentence.traverseRemove(Nonterminal.ipslon.sentences.get(0));      //这里去掉ipslon的式子
        for (Nonterminal nonTerm : derivations) {
            HashMap<Terminal,Sentence> termToSentence = new HashMap<>();
            calculateTable.put(nonTerm, termToSentence);
            for (Sentence sentence : nonTerm.sentences) {
                HashSet<Terminal> firstSet = getFirst(sentence);
                for (Terminal term : firstSet)                                  //第一条规则
                    termToSentence.put(term,sentence);
                if (firstSet.contains(Terminal.hidenIpslon))
                    for (Terminal term : getFollow(nonTerm))                    //第二条规则
                        termToSentence.put(term,sentence);
            }
            termToSentence.remove(Terminal.hidenIpslon);
        }
        calculateTable.remove(Nonterminal.ipslon);
    }

    Sentence takeUpTable(Nonterminal begin,Terminal lookForward) {
        Sentence sentence;
        if (calculateTable.get(begin)==null||(sentence = calculateTable.get(begin).get(lookForward))==null) return null;
        else return sentence;
    }

    boolean grammarAnalysis(ArrayList<Terminal> input) {
        LinkedList<Word> stack = new LinkedList<>();
        stack.push(Terminal.end);
        stack.push(begin);
        Word topOfStack;
        Terminal nowRead;
        for (int nowPos = 0; (topOfStack = stack.pop()).specialType()!=SpecialWord.END;) {
            nowRead = input.get(nowPos);
            if (nowRead.equals(topOfStack)) ++nowPos;
            else if (topOfStack.wordType()==WordType.TERMINAL) return false;
            else {
                Sentence sentence = takeUpTable((Nonterminal) topOfStack,nowRead);
                if (sentence==null) return false;
                else if (sentence.productionLength()==1&&sentence.wordAt(0).specialType()==SpecialWord.IPSLON) continue;
                else {
                    for (int pos = sentence.productionLength()-1;pos>=0;--pos)
                        stack.push(sentence.wordAt(pos));
                }
            }
        }
        return true;
    }


    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Nonterminal derivation: derivations)
            result.append(derivation.toDetailString());
        if (firstMap!=null) {
            result.append("firstMap: \n");
            for (Map.Entry<Word, HashSet<Terminal>> entry : firstMap.entrySet()) {
                if (entry.getKey().specialType()==SpecialWord.IPSLON||entry.getKey().wordType()==WordType.TERMINAL) continue;
                result.append(entry.getKey()).append(" : ");
                for (Word word : entry.getValue())
                    result.append(word);
                result.append("\n");
            }
        }
        if (followMap!=null) {
            result.append("followMap: \n");
            for (Map.Entry<Nonterminal, HashSet<Terminal>> entry : followMap.entrySet()) {
                if (entry.getKey().specialType()==SpecialWord.IPSLON) continue;
                result.append(entry.getKey()).append(" : ");
                for (Word word : entry.getValue())
                    result.append(word);
                result.append("\n");
            }
        }
        if (calculateTable!=null) {
            result.append("calculateTable: \n");
            for (Map.Entry<Nonterminal,HashMap<Terminal,Sentence>> entry1 : calculateTable.entrySet()) {
                result.append(entry1.getKey()).append(": \n");
                for (Map.Entry<Terminal,Sentence> entry2 : entry1.getValue().entrySet())
                    result.append("[").append(entry2.getKey().toDetailString()).append(",").append(entry2.getValue()).append("] , ");
                result.append("\n");
            }
        }
        /*if (idToSentence!=null) {
            result.append("idToSentence: \n");
            for (Map.Entry<Integer,ArrayList<Word>> entry : idToSentence.map().entrySet()) {
                for (Word word : entry.getValue())
                    result.append(word);
                result.append(": ").append(entry.getKey()).append("\n");
            }
        }*/
        return result.toString();
    }

    private ArrayList<Nonterminal> derivations;
    private ArrayList<Terminal> terminals;
    private HashMap<Word,HashSet<Terminal>> firstMap = null;
    private HashMap<Nonterminal,HashSet<Terminal>> followMap = null;
    private HashMap<Nonterminal,HashMap<Terminal,Sentence>> calculateTable = null;
    LinkedHashMap<String,Terminal> nameToTerminal;
    LinkedHashMap<String,Nonterminal> nameToNonterminal;
    //private DoubleWayMap<Integer,ArrayList<Word>> idToSentence = null;
    private Nonterminal begin;
    //private Terminal end;

    public static void main(String[] args) {
       /* ArrayList<String> nonTerms = new ArrayList<>(List.of("E","T","F")), terms = new ArrayList<>(List.of("+","-","*","/","(",")","i")),
        begins = new ArrayList<>(List.of("E","E","E","T","T","T","F","F")),ends = new ArrayList<>(List.of("E+T","E-T","T","T*F","T/F","F","(E)","i"));
        Grammar grammar = new Grammar(nonTerms,terms,begins,ends);
        System.out.print(grammar);
        grammar.deleteLeftRecursion();
        //grammar.leftFactor();
        System.out.println("*-***-*-*-*-");
        grammar.calculateFirst();
        grammar.calculateFollow();
        grammar.makeCalculateTable();
        System.out.println(grammar);
        ArrayList<Terminal> input = new ArrayList<>();
        input.add(grammar.terminals.get(6));
        input.add(grammar.terminals.get(0));
        input.add(grammar.terminals.get(6));
        input.add(grammar.terminals.get(3));
        input.add(grammar.terminals.get(6));
        input.add(grammar.terminals.get(7));
        System.out.print(grammar.grammarAnalysis(input));*/
    }
}
//ArrayList<String> nonTerms = new ArrayList<>(List.of("A")), terms = new ArrayList<>(List.of("a","b","c","d")),
//begins = new ArrayList<>(List.of("A","A","A","A")),ends = new ArrayList<>(List.of("Ac","Aad","bd","i"));

//ArrayList<String> nonTerms = new ArrayList<>(List.of("S","A")), terms = new ArrayList<>(List.of("a","b","c","d")),
//begins = new ArrayList<>(List.of("S","S","A","A","A")),ends = new ArrayList<>(List.of("Aa","b","Ac","Sd","i"));

//ArrayList<String> nonTerms = new ArrayList<>(List.of("S","E")), terms = new ArrayList<>(List.of("i","e","t","a","b")),
//begins = new ArrayList<>(List.of("S","S","S","E")),ends = new ArrayList<>(List.of("iEtS","iEtSeS","a","b"));
