public class Type {
    static class IntegerType extends Type {
        IntegerType(int w) {
            super(w);
        }

        @Override
        TypeEnum getType() {
            return TypeEnum.INT;
        }
    }
    static class CharType extends Type {
        CharType(int w) {
            super(w);
        }
        @Override
        TypeEnum getType() {
            return TypeEnum.CHAR;
        }
    }

    Type() {}

    Type(int w) {width = w;}

    TypeEnum getType() {return TypeEnum.NORM; }

    int width;

    static IntegerType integer = new IntegerType(4);
    static CharType character = new CharType(1);
    static Stretch stretch = new Stretch();
}

class ArrayType extends Type {

    ArrayType(int num,Type dT) {
        super(num *dT.width);
        length = num;
        dataType = dT;
    }
    @Override
    TypeEnum getType() {
        return TypeEnum.ARRAY;
    }
    int length;
    Type dataType;
}

class Pointer extends Type {
    Pointer(int w) {
        super(w);
    }
}

class Stretch extends Type {
    Stretch() {
        super(8);
    }
    Stretch(Type t) {
        super(8);
        target = t;
    }
    TypeEnum getType() {return TypeEnum.STRETCH; }
    Type target;
}