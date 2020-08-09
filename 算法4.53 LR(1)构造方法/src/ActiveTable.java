import java.util.HashMap;

public class ActiveTable {
    ActiveTable() {}

    @Override
    public String toString() {
        return "ActiveTable{" +
                "table='\n" + table +
                "}";
    }

    HashMap<Unit,ActiveState> table = new HashMap<>();

}

class ActiveState {
    ActiveState(boolean i,Msil n) {ifActive = i; line = n; }

    @Override
    public String toString() {
                return "ifActive=" + ifActive +
                ", line=" + (line==null?"null": line.lineCount) +
                "}\n";
    }

    boolean ifActive;
    Msil line;
}