import java.util.Objects;

public class Msil {
    Msil(String f,Unit p_1,Unit p_2,Unit r) {func = f; result = r;param_1 = p_1; param_2 = p_2;lineCount = count++;}

    @Override
    public String toString() {
        return "Msil{" + lineCount +
                "  func='" + func + '\'' +
                ", result=" + result +
                ", param_1=" + param_1 +
                ", param_2=" + param_2 +
                "}\n";
    }

    int lineCount;
    String func;
    Unit result;
    Unit param_1;
    Unit param_2;
    static int count = 0;
}

interface Unit {

}

class SymbolRecord implements Unit {
    SymbolRecord(Identify i,Type t) {
        identify = i;type = t;
    }

    SymbolRecord(Type t) {type = t; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SymbolRecord that = (SymbolRecord) o;
        return identify.id.equals(that.identify.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identify.id);
    }

    @Override
    public String toString() {
        return "SymbolRecord{" +
                "identify=" + identify.id +
                ", type=" + type.width +
                ", offset=" + offset +
                "}";
    }

    Identify identify;
    Type type;
    int offset;

}

/*class StackRecord extends SymbolRecord {
    StackRecord(Type t) {
        super(t);
    }
    StackRecord(Type t,int o) {
        super(t);
        offset = o;
    }
}*/

class ConstantInt implements Unit {
    ConstantInt() {type = Type.integer; }
    ConstantInt(int i) {
        type = Type.integer;
        value = i;
    }

    @Override
    public String toString() {
        return "ConstantInt{" +
                "value=" + value +
                '}';
    }

    Type type;
    int value;
}

class ConstantChar implements Unit {
    ConstantChar(char c) {
        type = Type.character;
        value = c;
    }

    @Override
    public String toString() {
        return "ConstantChar{" +
                "value=" + value +
                '}';
    }

    Type type;
    char value;
}

class Temp implements Unit {
    //Temp() {}
    Temp(Type t) {type  =t; num = count++;}

    @Override
    public String toString() {
        return "Temp{" +
                "type=" + type +
                ", num=" + num +
                '}';
    }

    Type type;
    int num;
    static int count = 0;
}

class StretchRecord implements Unit {
    StretchRecord(SymbolRecord record) {offset = record.offset; type = record.type; }
    Integer offset;
    Type type;
}