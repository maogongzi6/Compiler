import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;

public interface Word {
    WordType wordType();
    SpecialWord specialType();
    String toDetailString();
}

class Terminal implements Word {
    static class Spread extends Terminal {
        private Spread() {
            super(Lex.spread);
        }

        @Override
        public SpecialWord specialType() {
            return SpecialWord.SPREAD;
        }
    }
    static class Dot extends Terminal {
        Dot() {
            super(Lex.dot);
        }

        @Override
        public SpecialWord specialType() {
            return SpecialWord.DOT;
        }
    }
    static class HidenIpslon extends Terminal {
        private HidenIpslon() {
            super(Lex.ipslon);
        }

        @Override
        public SpecialWord specialType() {
            return SpecialWord.IPSLON;
        }
    }

    static class End extends Terminal {
        private End() {super(Lex.end);}

        @Override
        public SpecialWord specialType() {
            return SpecialWord.END;
        }
    }

    Terminal(Lex lex) {term = lex; }

    @Override
    public WordType wordType() {
        return WordType.TERMINAL;
    }

    @Override
    public SpecialWord specialType() {
        return SpecialWord.NORMAL;
    }

    @Override
    public String toDetailString() {
        return toString();
    }

    @Override
    public String toString() {
        return term.toString()+" ";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Terminal terminal = (Terminal) o;
        return term.equals(terminal.term);
    }

    @Override
    public int hashCode() {
        return Objects.hash(term);
    }

    Lex term;
    static final Spread spread = new Spread();
    static final HidenIpslon hidenIpslon = new HidenIpslon();
    static final End end = new End();
    static final Dot dot = new Dot();
}

class Nonterminal implements Word {

    static class Ipslon extends Nonterminal {                                   //这里有一个大胆但是精彩的设计，解决了ipslon的问题，Ipslon是一个Nonterminal可以生成一个Terminal的hidenIpslon
        private Ipslon() {                                                      //Ipslon使用在表达式里，hidenIpslon使用在first，follow，calculateTable中
            super("IPSLON");
            add(new Sentence(this,new ArrayList<>(){{add(Terminal.hidenIpslon);}}));
        }

        @Override
        public SpecialWord specialType() {
            return SpecialWord.IPSLON;
        }

        //public void print() {/*System.out.println("IPSLON ");*/}
    }

    Nonterminal(String s) {
        sentences = new LinkedList<>();
        name = s;
    }

    boolean remove(ArrayList<Word> w) {return sentences.remove(new Sentence(this,w));}//這裏需要修改Sentence的equals
    boolean remove(Sentence sen) {return sentences.remove(sen);}

    boolean add(Sentence sen) {
        if (sen.productionLength()==0) {              //这里太重要了，要判断是不是把字符全部删完了，如果删完了且转换中还没有ipslon就加入ipslon，如果删完了且存在ipslon，就什么都不添加
            sen.add(Nonterminal.ipslon);
            if (sentences.contains(sen))//這裏需要修改Sentence的equals
                return false;
        }
        while (sen.productionLength()!=1&&sen.remove(Nonterminal.ipslon)) continue;            //删除非空串中的ipslong
        return sentences.add(sen);
    }

    boolean add(ArrayList<Word> prod) {return add(new Sentence(this,prod)); }

    @Override
    public WordType wordType() {
        return WordType.NONTERMINAL;
    }

    @Override
    public SpecialWord specialType() {
        return SpecialWord.NORMAL;
    }

    public String toDetailString() {
       StringBuilder result = new StringBuilder();
       result.append(this);
       result.append(": \n");
       for (Sentence sentence: sentences) {
           for (Word word: sentence.getProduction())
               result.append(word);
           result.append("\n");
       }
       return result.toString();
   }

    @Override
    public String toString() {
        return name+" ";
    }

    static final Ipslon ipslon = new Ipslon();
    LinkedList<Sentence> sentences;
    String name; //这个是暂时的
}

class Code implements Word {
    Code(String s) {funcName = s; }

    @Override
    public WordType wordType() {
        return WordType.ACTION;
    }

    @Override
    public SpecialWord specialType() {
        return SpecialWord.NORMAL;
    }

    @Override
    public String toDetailString() {
        return null;
    }
    String funcName;
}

//这个Lex是临时的

class Lex {
    Lex(String s) {name = s; }

    @Override
    public String toString() {
        return name;
    }

    LexType lexType() {return LexType.NORMAL;}

    public boolean equals(Object obj) {
        try {
            getClass().asSubclass(obj.getClass());
        }catch (ClassCastException e) {
            try {
                obj.getClass().asSubclass(getClass());
            }catch (ClassCastException e_2) {return false;}
            return ((Lex)obj).name.equals(name);
        }

        return ((Lex)obj).name.equals(name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    String name;
    int line = -1;

    static final Lex ipslon = new Lex("ipslon");
    static final Lex end = new Lex("$");
    static final Lex spread = new Lex("#");
    static final Lex dot = new Lex("·");
}

class Key extends Lex {
    Key(String s,int l) {
        super(s);
        line = l;
    }
    @Override
    LexType lexType() {return LexType.NORMAL;}

    @Override
    public String toString() {
        return name+"   "+line;
    }
}

class Identify extends Lex {

    Identify(String s,String i,int l) {
        super(s);
        line = l;
        id = i;
    }

    @Override
    LexType lexType() {return LexType.IDENTIFY;}

    @Override
    public String toString() {
        return name+"   "+line;
    }

    String id;
}

class IntData extends Lex {

    IntData(String s, String t, int l, int c) {
        super(s);
        line = l;
        content = c;
        type = t;
    }
    @Override
    LexType lexType() {return LexType.INT;}

    @Override
    public String toString() {
        return name+"   "+line+"  "+content;
    }

    Integer content;
    String type;
}

class CharData extends Lex {

    CharData(String s, String t, int l, char c) {
        super(s);
        line = l;
        content = c;
        type = t;
    }
    @Override
    LexType lexType() {return LexType.CHAR;}

    @Override
    public String toString() {
        return name+"   "+line+"  "+content;
    }

    Character content;
    String type;
}

class StringData extends Lex {

    StringData(String s, String t, int l, String c) {
        super(s);
        line = l;
        content = c;
        type = t;
    }
    @Override
    LexType lexType() {return LexType.STRING;}

    @Override
    public String toString() {
        return name+"   "+line+"  "+content;
    }

    String content;
    String type;
}

class Delimiter extends Lex {
    Delimiter(String s,int l) {
        super(s);
        line = l;
    }
    @Override
    LexType lexType() {return LexType.DELIMITER;}

    @Override
    public String toString() {
        return name+"   "+line;
    }
}

class TablePointer extends Lex {

    TablePointer(String s,int l,Table t) {
        super(s);
        line = l;
        inner = t;
    }

    @Override
    LexType lexType() {return LexType.POINTER;}

    @Override
    public String toString() {
        return name+"   "+line;
    }

    Table inner;
}