import java.util.*;

public class NfaMachine<T,P> {
    NfaMachine(ArrayList<T> nodes, ArrayList<P> paths, T s, HashSet<T> ac, P ips) {
        doubleWayWithNodeNId = new DoubleWayMap<>();
        doubleWayWithPathNId = new DoubleWayMap<>();
        //nodeIdToName = new HashMap<>();
        //pathIdToName = new HashMap<>();
        for (int i = 0;i<nodes.size();++i)
            //nodeIdToName.put(i,nodes.get(i));
            doubleWayWithNodeNId.put(nodes.get(i),i);
        for (int i = 0;i<paths.size();++i)
            //pathIdToName.put(i,paths.get(i));
            doubleWayWithPathNId.put(paths.get(i),i);
        HashSet<Integer> acceptId = new HashSet<>();
        for (T a: ac)
            acceptId.add(getNodeId(a));
        nfa = new NFA(nodes.size(),getNodeId(s),acceptId,getPathId(ips));
    }

    NfaMachine() {
        doubleWayWithNodeNId = new DoubleWayMap<>();
        doubleWayWithPathNId = new DoubleWayMap<>();
        //nodeIdToName = new HashMap<>();
        //pathIdToName = new HashMap<>();
        nfa = new NFA(doubleWayWithNodeNId.size(),null,new HashSet<>(),null);
    }

    void setIpslon(P ips) {nfa.setIpslon(getPathId(ips)); }
    boolean addNode(T node) {
        if (doubleWayWithNodeNId.get(node)!=null) {
            int size = doubleWayWithNodeNId.size();
            doubleWayWithNodeNId.put(node,size-1);
            //nodeIdToName.put(size-1,node);
            nfa.addNode();
            return true;
        }
        return false;
    }
    void setStart(T start) {nfa.setStart(getNodeId(start));}
    T getStart() {return getNode(getStartId()); }
    Integer getStartId() {return nfa.start; }
    void addAccept(T accept) {nfa.addAccept(getNodeId(accept));}

    NFA get() {return nfa; }

    DfaMachine<HashSet<T>,P> convertToDfa() {return nfa.convertToDfa(); }
    //void print() {nfa.print();}


    @Override
    public String toString() {
        return nfa.toString();
    }

    boolean addPath(T begin, T end, P path) {
        return nfa.addPath(getNodeId(begin),getNodeId(end),getPathId(path));
    }

    HashSet<T> runNfa(P[] input) {
        Integer[] inputId = new Integer[input.length];
        for (int pos = 0;pos<input.length;++pos)
            inputId[pos] = getPathId(input[pos]);
        HashSet<T> result = new HashSet<>();
        for (Integer integer: nfa.run(inputId))
            result.add(getNode(integer));
        return result;
    }

    /*boolean runNfaForResult(P[] input) {
        HashSet<T> result = runNfa(input);

        return !result.isEmpty() && nfa.accept.contains(getNodeId(result));
    }*/

    private T getNode(int index) {return doubleWayWithNodeNId.traverse().get(index);}
    private P getPath(int index) {return doubleWayWithPathNId.traverse().get(index);}
    private Integer getNodeId(T node) {return doubleWayWithNodeNId.get(node);}
    private Integer getPathId(P path) {return doubleWayWithPathNId.get(path);}

    //private HashMap<T,Integer> doubleWayWithNodeNId;//这个数组使用下标将nodeName与编号保存起来
    private DoubleWayMap<P,Integer> doubleWayWithPathNId;
    private DoubleWayMap<T,Integer> doubleWayWithNodeNId;
    //private HashMap<Integer,T> nodeIdToName;
    //private HashMap<Integer,P> pathIdToName;
    private NFA nfa;

    class NFA implements FA {
        private NFA(Integer nodeNum,Integer s, HashSet<Integer> ac, Integer ips) {
            start = s;
            accept = ac;
            ipslon = ips;
            for (int i = 0; i < nodeNum; ++i) {
                mapWithPathSet.add(new HashMap<>());
                mapWithTargetSet.add(new HashMap<>());
            }
        }

        public HashSet<Integer> findTarget(int begin, Integer path) {
            return mapWithTargetSet.get(begin).get(path);
        }

        public HashSet<Integer> findPath(int begin, Integer target) {
            return mapWithPathSet.get(begin).get(target);
        }

        void setIpslon(Integer ips) {ipslon = ips; }
        void setStart(Integer sta) {start = sta; }

        void addAccept(Integer ac) {accept.add(ac);}
        void addNode() {
            mapWithTargetSet.add(new HashMap<>());
            mapWithPathSet.add(new HashMap<>());
        }

        public boolean addPath(Integer beginId, Integer endId, Integer path) {
            boolean isSucceed = true;
            if (!mapWithPathSet.get(beginId).containsKey(endId))
                mapWithPathSet.get(beginId).put(endId, new HashSet<>());
            isSucceed = mapWithPathSet.get(beginId).get(endId).add(path);
            if (!mapWithTargetSet.get(beginId).containsKey(path))
                mapWithTargetSet.get(beginId).put(path, new HashSet<>());
            isSucceed &= mapWithTargetSet.get(beginId).get(path).add(endId);
            return isSucceed;
        }

        DfaMachine<HashSet<T>,P> convertToDfa(){
            //有一点，treeSet中对象的相应属性值改变后不会更新顺序，可以把原有的值重新插入一变
            class Pair implements Comparable<Pair> {                        //使用Pair保存状态节点，定义compareTo使得所有未标记节点都在treeSet最上方
                private Pair(HashSet<Integer> nsI, boolean iM) {
                    nodesId = nsI;
                    isMarked = iM;
                }

                @Override
                public int compareTo(Pair pair) {
                    if (pair==this) return 0;
                    if (pair==null) throw new RuntimeException();
                    if (equals(pair)) return 0;
                    if (pair.isMarked==isMarked) {
                        //把所有未标记的放在最前面
                        if (pair.nodesId.size()!= nodesId.size()) return nodesId.size()-pair.nodesId.size();
                        Object[] thisSorted = (new TreeSet<Integer>(nodesId)).toArray(), pairSorted = (new TreeSet<Integer>(pair.nodesId)).toArray();
                        for (int i = 0;i<thisSorted.length;++i) {
                            if (thisSorted[i]!=pairSorted[i]) return (Integer) thisSorted[i]-(Integer) pairSorted[i];
                        }
                        return 0;
                    }
                    else {
                        if (isMarked) return 1;
                        else return -1;
                    }
                }

                @Override
                public boolean equals(Object obj) {
                    if (obj==this) return true;
                    if (obj!=null&&obj.getClass()==Pair.class) {
                        Pair pair = (Pair)obj;
                        if (pair.nodesId.size()== nodesId.size()) {
                            for (Integer integer: nodesId) {
                                if (!pair.nodesId.contains(integer)) {
                                    return false;
                                }
                            }
                            return true;
                        }
                    }
                    return false;
                }

                private HashSet<Integer> nodesId = null;
                private boolean isMarked;
            }
            class Path {                                                                //使用Path保存待生成的DFA路径的begin,end,path
                private Path(HashSet<Integer> b, HashSet<Integer> e, Integer p) {
                    begin = b;
                    end = e;
                    path = p;
                }
                private HashSet<Integer> begin;
                private HashSet<Integer> end;
                private Integer path;
            }
            TreeSet<Pair> dfaStates = new TreeSet<>();
            HashSet<Path> dfaPathSet = new HashSet<>();
            HashSet<HashSet<T>> dfaAccept = new HashSet<>();
            HashSet<Integer> startIdSet = ipslonClosure(start);
            HashSet<T> dfaStart = null;
            dfaStates.add(new Pair(startIdSet,false));
            while (!dfaStates.first().isMarked) {
                HashSet<Integer> nowSet = dfaStates.first().nodesId;
                dfaStates.remove(dfaStates.first());
                dfaStates.add(new Pair(nowSet,true));
                for (Integer action = 0; action< doubleWayWithPathNId.size(); ++action) {
                    if (action.equals(ipslon)) continue;
                    HashSet<Integer> targetSet = ipslonClosure(move(nowSet,action));
                    if (!targetSet.isEmpty()) {
                        Pair moveToPair = new Pair(targetSet, true);
                        if (!dfaStates.contains(moveToPair)) {
                            moveToPair.isMarked = false;
                            dfaStates.add(moveToPair);
                        }

                        dfaPathSet.add(new Path(nowSet, targetSet, action));                            //这里我们只能暂时保存path，因为我们必须等DFA节点构建完参能插入路径，由于编号与实际对象转换的问题，这里无法优化
                    }                                                                                   //实际上就算是使用实际对象，这个时间消耗也无法避免，要比较两个容器是否相等只能比较其中的元素，时间消耗基本相同
                }
            }
            ArrayList<HashSet<T>> dfaStatesList = new ArrayList<>();

            HashMap<HashSet<Integer>,HashSet<T>> idToName = new HashMap<>();
            for (Pair pair: dfaStates) {
                HashSet<T> nodes = new HashSet<>();
                for (Integer id: pair.nodesId)
                    nodes.add(getNode(id));
                dfaStatesList.add(nodes);
                if (pair.nodesId.contains(start))
                    dfaStart = nodes;
                HashSet<Integer> tempId = new HashSet<>(pair.nodesId);
                tempId.retainAll(accept);
                if (!tempId.isEmpty())
                    dfaAccept.add(nodes);
                idToName.put(pair.nodesId,nodes);
            }
            ArrayList<P> dfaPath = new ArrayList<>(doubleWayWithPathNId.keySet());
            dfaPath.remove(getPath(ipslon));
            DfaMachine<HashSet<T>,P> buildDFA = new DfaMachine<>(dfaStatesList,dfaPath,dfaStart,dfaAccept);
            for (Path path: dfaPathSet)
                if (!path.path.equals(ipslon))
                    buildDFA.addPath(idToName.get(path.begin),idToName.get(path.end),getPath(path.path));
            return buildDFA;
        }
        private HashSet<Integer> ipslonClosure(int begin) {
            return ipslonClosure(new HashSet<>() {{
                add(begin);
            }});
        }

        private HashSet<Integer> ipslonClosure(HashSet<Integer> begins) {
            HashSet<Integer> result = new HashSet<>(begins);
            LinkedList<Integer> stack = new LinkedList<>(begins);
            while (!stack.isEmpty()) {
                int begin = stack.pop();
                HashSet<Integer> targetSet = mapWithTargetSet.get(begin).get(ipslon);
                if (targetSet!=null) {
                    targetSet.removeAll(result);
                    stack.addAll(targetSet);
                    result.addAll(targetSet);
                }
            }
            return result;
        }

        private HashSet<Integer> move(HashSet<Integer> begins, Integer path) {
            HashSet<Integer> result = new HashSet<>();
            for (Integer begin : begins) {
                HashSet<Integer> target = mapWithTargetSet.get(begin).get(path);
                if (target != null)
                    result.addAll(target);
            }
            return result;
        }

        HashSet<Integer> run(Integer[] input) {
            HashSet<Integer> now = ipslonClosure(start);
            for (Integer path: input) now = ipslonClosure(move(now,path));
            return now;
        }

        /*void print() {
            for (int i = 0; i < nodeNameToId.size(); ++i) {
                System.out.println(getNode(i) + " :");
                for (Map.Entry<Integer, HashSet<Integer>> entry : mapWithTargetSet.get(i).entrySet()) {
                    System.out.println(getPath(entry.getKey()) + ":  ");
                    for (Integer targetId: entry.getValue())
                        System.out.println(getNode(targetId)+", ");
                }
                System.out.println();
            }
            System.out.println("begin: " + getNode(start));
        }*/

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < doubleWayWithNodeNId.size(); ++i) {
                result.append(getNode(i)).append(" :\n");
                for (Map.Entry<Integer, HashSet<Integer>> entry : mapWithTargetSet.get(i).entrySet()) {
                    result.append(getPath(entry.getKey())).append(":  \n");
                    for (Integer targetId: entry.getValue())
                        result.append(getNode(targetId)).append(", \n");
                }
                result.append("\n");
            }
            result.append("begin: ").append(getNode(start)).append("\n");
            return result.toString();
        }

        Integer start;
        HashSet<Integer> accept;
        ArrayList<HashMap<Integer, HashSet<Integer>>> mapWithTargetSet = new ArrayList<>();
        ArrayList<HashMap<Integer, HashSet<Integer>>> mapWithPathSet = new ArrayList<>();
        Integer ipslon;
        //private int startId = 10;
    }
}
