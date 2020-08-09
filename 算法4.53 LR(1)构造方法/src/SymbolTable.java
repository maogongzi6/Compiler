import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class SymbolTable {
    SymbolTable() {outer = null; nowOffset = 0; }
    SymbolTable(SymbolTable sb) {outer = sb; nowOffset = sb.nowOffset; }
    boolean contains(SymbolRecord s) {
        if (records.containsKey(s.identify.id)) return true;
        SymbolTable now = outer;
        while (now!=null) {
            if (now.records.containsKey(s.identify.id)) return true;
            now = now.outer;
        }
        return false;
        //return records.containsKey(s.identify.id);
    }
    boolean add(SymbolRecord s) {
        s.offset = nowOffset;
        nowOffset += s.type.width;
        if (records.containsKey(s.identify.id))
            return false;
        records.put(s.identify.id,s);
        return true;
    }
    SymbolRecord get(String s) {
        SymbolRecord result;
        if ((result=records.get(s))!=null) return result;
        SymbolTable now = outer;
        while (now!=null) {
            if ((result=now.records.get(s))!=null) return result;
            now = now.outer;
        }
        return null;
    }

    @Override
    public String toString() {
        return "SymbolTable{" +
                "nowOffset=" + nowOffset +
                ", records=" + records +
                '}';
    }

    int nowOffset;
    SymbolTable outer;
    HashMap<String,SymbolRecord> records = new HashMap<>();
}
