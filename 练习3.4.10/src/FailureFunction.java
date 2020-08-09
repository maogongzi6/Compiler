import java.util.*;

public class FailureFunction {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int nodeNum = scanner.nextInt(), edgeNum = scanner.nextInt();
        ArrayList<ArrayList<Integer>> map = new ArrayList<>();
        ArrayList<Node> nodes = new ArrayList<>();
        for (int i = 0;i<nodeNum;++i) {
            map.add(new ArrayList<>());
            nodes.add(new Node(i));
            for (int j = 0;j<nodeNum;++j)
                map.get(i).add(-1);
        }
        for (int i = 0;i<edgeNum;++i) {
            int firstId = scanner.nextInt(), secondId = scanner.nextInt();
            int letter = scanner.nextInt();
            map.get(firstId).set(secondId,letter);
            nodes.get(secondId).prev = firstId;
        }
        failure(nodes,map);
        for (Node node: nodes)
            System.out.println(node.id+"   "+node.fValue);
    }

    static void failure(ArrayList<Node> nodes, ArrayList<ArrayList<Integer>> map) {
        LinkedList<Integer> queue = new LinkedList<>();
        for (Integer firstLetter : map.get(0)) {
            if (firstLetter!=-1)
                queue.addLast(map.get(0).indexOf(firstLetter));
        }
        Integer nowId = null;
        while (!queue.isEmpty()) {
            nowId = queue.removeFirst();
            for (Integer nextLetter : map.get(nowId)) {
                if (nextLetter != -1) {
                    int nowFValue = nodes.get(nowId).fValue;
                    while (nowFValue >= 0 && (!map.get(nowFValue).contains(nextLetter))) {
                        nowFValue = nodes.get(nowFValue).fValue;
                    }
                    if (nowFValue == -1) {
                        if (map.get(0).contains(nextLetter)) {
                            nowFValue = map.get(0).indexOf(nextLetter);
                        }
                    }
                    else if (map.get(nowFValue).contains(nextLetter)) {
                        nowFValue = map.get(nowFValue).indexOf(nextLetter);
                    }
                    nodes.get(map.get(nowId).indexOf(nextLetter)).fValue = nowFValue;
                    queue.addLast(map.get(nowId).indexOf(nextLetter));
                }
            }
        }
    }
}

class Node {
    Node(int i) {id = i;}
    int id;
    int prev = -1;
    int fValue = -1;
}

/*
10
9
0 1 3
1 2 4
2 8 5
8 9 2
1 6 1
6 7 2
0 3 2
3 4 3
4 5 4
 */
/*
9
8
0 1 1
1 2 1
2 3 2
0 4 2
0 5 3
5 6 1
6 7 1
7 8 2
 */