import java.util.ArrayList;
import java.util.HashMap;

public class LALRTable {
    LALRTable(LALR lalr) {grammar = lalr; }


    LALR grammar;
    ArrayList<HashMap<Terminal, LRTable.ActionOrGoto>> actionMap = new ArrayList<>();
    ArrayList<HashMap<Nonterminal, LRTable.ActionOrGoto>> gotoMap = new ArrayList<>();

}