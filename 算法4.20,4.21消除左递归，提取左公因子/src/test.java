import java.util.LinkedList;

public class test {
    public static void main(String[] args) {
        A a1 = new A(),a2 = new A();
        AA aa = new AA(),aa2 = new AA();
        B b = new B();
        System.out.println(a1.equals(a2));
        System.out.println(aa.equals(a1));
        System.out.println(a1.equals(aa));
        System.out.println(aa.equals(aa2));
        System.out.println(b.equals(aa2));

    }
}

class B {}

class A {
    @Override
    public boolean equals(Object obj) {
        //System.out.println(obj.getClass().asSubclass(getClass())||);
        try {
            getClass().asSubclass(obj.getClass());
        }catch (ClassCastException e) {
            try {
                obj.getClass().asSubclass(getClass());
            }catch (ClassCastException e_2) {return false;}
            return true;
        }

        return true;
    }

    public static void main(String[] args) {
        LinkedList<Integer> list = new LinkedList<>();
        list.addFirst(1);
        list.addFirst(2);
        list.addFirst(3);
    System.out.println(list);
    }
}

class AA extends A {
    /*@Override
    public boolean equals(Object obj) {
        System.out.println("A");
        return obj.getClass().equals(getClass());
    }*/
}