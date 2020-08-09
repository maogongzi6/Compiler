
import java.util.*;

public class NToD {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int nodeNum = scanner.nextInt(), pathNum = scanner.nextInt();
        HashSet<Character> accept = new HashSet<>(){{add("I".charAt(0));}};
        //NfaMachine<Integer,Character> nfa = new NfaMachine<Integer,Character>(new ArrayList<Integer>(List.of(0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19)),new ArrayList<>(List.of('1','0','-')),0,accept,'-');
        NfaMachine<Character,Character> nfa = new NfaMachine<Character,Character>(new ArrayList<Character>(List.of("A".charAt(0),"B".charAt(0),"C".charAt(0),"D".charAt(0),"E".charAt(0),"F".charAt(0),"G".charAt(0),"H".charAt(0),"I".charAt(0))),new ArrayList<Character>(List.of('1','0','-')),"A".charAt(0),accept,'-');
        for (int i = 0;i<pathNum;++i) {
            char begin = scanner.next().charAt(0), end = scanner.next().charAt(0);

            char path = scanner.next().charAt(0);
            nfa.addPath(begin,end,path);
        }
        System.out.println(nfa.convertToDfa().getNodeNum());
       /* DfaMachine<HashSet<Integer>,Character> dfa = nfa.convertToDfa();
        //nfa.print();
        //dfa.print();
        System.out.print(dfa);
        Character []input = {'b','b','a','b','b','b'};
        //System.out.println(dfa.runDfa(input));
        //System.out.println(dfa.runDfaForResult(input));
        //System.out.println(nfa.runNfa(input));
        DfaMachine sDfa = dfa.simplifyStates();
        //System.out.println(sDfa.runDfa(input));*/
    }
}



/*          //0~10十一个状态；a,b,-三个转换；0开始，十结束
11 13
0 1 -
0 7 -
1 2 -
1 4 -
2 3 a
3 6 -
4 5 b
5 6 -
6 1 -
6 7 -
7 8 a
8 9 b
9 10 b
 */
