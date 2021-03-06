import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DfaMachine<T,P> {
    DfaMachine(ArrayList<T> nodes, ArrayList<P> paths, T s, HashSet<T> ac) {
        doubleWayWithNodeNId = new DoubleWayMap<>();
        doubleWayWithPathNId = new DoubleWayMap<>();
        //nodeIdToName = new HashMap<>();
        //pathIdToName = new HashMap<>();
        for (int i = 0;i<nodes.size();++i)
            //nodeIdToName.put(i,nodes.get(i));
            doubleWayWithNodeNId.put(nodes.get(i),i);
        for (int i = 0;i<paths.size();++i)
            // pathIdToName.put(i,paths.get(i));
            doubleWayWithPathNId.put(paths.get(i),i);
        HashSet<Integer> acceptId = new HashSet<>();
        for (T a: ac)
            acceptId.add(getNodeId(a));
        dfa = new DFA(nodes.size(),getNodeId(s),acceptId);
    }

    DfaMachine(HashSet<T> nodes, HashSet<P> paths, T s, HashSet<T> ac) {
        doubleWayWithNodeNId = new DoubleWayMap<>();
        doubleWayWithPathNId = new DoubleWayMap<>();
        //nodeIdToName = new HashMap<>();
        //pathIdToName = new HashMap<>();
        int nodeCount = 0;
        for (T node : nodes) {
            //nodeIdToName.put(nodeCount,node);
            doubleWayWithNodeNId.put(node, nodeCount);
            ++nodeCount;
        }
        int pathCount = 0;
        for (P path : paths) {
            //pathIdToName.put(pathCount,path);
            doubleWayWithPathNId.put(path, pathCount);
            ++pathCount;
        }
        HashSet<Integer> acceptId = new HashSet<>();
        for (T a: ac)
            acceptId.add(getNodeId(a));
        dfa = new DfaMachine.DFA(nodes.size(),getNodeId(s),acceptId);
    }


    T runDfa(P[] input) {
        Integer[] inputId = new Integer[input.length];
        for (int pos = 0;pos<input.length;++pos)
            inputId[pos] = getPathId(input[pos]);
        return getNode(dfa.run(inputId));
    }

    boolean runDfaForResult(P[] input) {
        T result = runDfa(input);
        return result != null && dfa.accept.contains(getNodeId(result));
    }

    T move(T begin,P path) {
        Integer endId = moveById(getNodeId(begin),getPathId(path));
        if (endId!=null) return getNode(endId);
        else return null;
    }

    Integer moveById(Integer begin,Integer path) {
        return dfa.move(begin,path);
    }

    DfaMachine simplifyStates() {return dfa.simplifyDfa();}

    boolean addPath(T begin, T end, P path) {return dfa.addPath(getNodeId(begin),getNodeId(end),getPathId(path));}
    boolean addNode(T node) {
        if (doubleWayWithNodeNId.get(node)!=null) {
            int size = doubleWayWithNodeNId.size();
            doubleWayWithNodeNId.put(node,size-1);
            //nodeIdToName.put(size-1,node);
            dfa.addNode();
            return true;
        }
        return false;
    }
    void setStart(T start) {dfa.setStart(getNodeId(start));}
    T getStart() {return getNode(getStartId()); }
    Integer getStartId() {return dfa.start; }
    void addAccept(T accept) {dfa.addAccept(getNodeId(accept));}

    DfaMachine.DFA get() {return dfa; }
    T getNode(int index) {return doubleWayWithNodeNId.traverse().get(index);}
    P getPath(int index) {return doubleWayWithPathNId.traverse().get(index);}
    Integer getNodeId(T node) {return doubleWayWithNodeNId.get(node);}
    Integer getPathId(P path) {return doubleWayWithPathNId.get(path);}
    int getNodeNum() {return doubleWayWithNodeNId.size(); }
    HashSet<T> getAccept() {
        HashSet<T> accept = new HashSet<>();
        for (Object id : dfa.accept)
            accept.add(getNode((Integer) id));
        return accept;
    }

    //void print() {dfa.print();}


    @Override
    public String toString() {
        return dfa.toString();
    }

    private DoubleWayMap<T,Integer> doubleWayWithNodeNId;//这个数组使用下标将nodeName与编号保存起来
    private DoubleWayMap<P,Integer> doubleWayWithPathNId;
    //private HashMap<Integer,T> nodeIdToName;
    //private HashMap<Integer,P> pathIdToName;
    private DfaMachine.DFA dfa;

    class DFA implements FA {

        private DFA(Integer nodeNum,Integer s, HashSet<Integer> ac) {
            start = s;
            accept = ac;
            for (int i = 0; i < nodeNum; ++i) {
                mapWithPathSet.add(new HashMap<>());
                mapWithTarget.add(new HashMap<>());
            }
        }

        @Override
        public HashSet<Integer> findTarget(int begin, Integer path) {
            return new HashSet<Integer>() {{add(mapWithTarget.get(begin).get(path));}};
        }

        private Integer findTheTarget(int begin, Integer path) {
            return mapWithTarget.get(begin).get(path);
        }

        @Override
        public HashSet<Integer> findPath(int begin, Integer target) {
            return mapWithPathSet.get(begin).get(target);
        }

        DfaMachine<T,P> simplifyDfa() {                     //这个化简可能不太对要着重看一下
            ArrayList<Integer> newStates = new ArrayList<>();
            ArrayList<Integer> acceptSet = new ArrayList<>(),notAcceptSet = new ArrayList<>();
            int newStateNum = 2,oldStateNum = 2;                            //这个newStateNum是新生成的节点划分的编号
            for (int i = 0; i< doubleWayWithNodeNId.size(); ++i) {
                if (accept.contains(i)) {
                    newStates.add(0);
                    acceptSet.add(i);
                }
                else {
                    newStates.add(1);
                    notAcceptSet.add(i);
                }
            }
            ArrayList<ArrayList<Integer>> newStatesSet = new ArrayList<>();     //使用节点划分编号保存每个划分
            newStatesSet.add(acceptSet);
            newStatesSet.add(notAcceptSet);
            while (true) {
                for (int pos = 0;pos<newStatesSet.size();++pos) {
                    HashMap<ArrayList<Integer>,ArrayList<Integer>> newStatesMap = new HashMap<>();
                    for (Integer nodeId: newStatesSet.get(pos)) {
                        ArrayList<Integer> allTargets = new ArrayList<>();
                        for (int path = 0; path< doubleWayWithPathNId.size(); ++path) {
                            //Integer i = newStates.get(findTheTarget(nodeId, path));
                            Integer targetId = findTheTarget(nodeId, path);
                            if (targetId!=null)
                                allTargets.add(newStates.get(findTheTarget(nodeId, path)));
                        }
                        if (!newStatesMap.containsKey(allTargets)) {
                            newStatesMap.put(allTargets, new ArrayList<>());
                            ++newStateNum;
                        }
                        newStatesMap.get(allTargets).add(nodeId);               //这个是原来dfa
                    }
                    if (newStatesMap.size()!=1) {
                        newStatesSet.remove(pos);
                        --pos;
                        for (Map.Entry<ArrayList<Integer>, ArrayList<Integer>> entry : newStatesMap.entrySet()) {
                            for (Integer node : entry.getValue())
                                newStates.set(node, oldStateNum);
                            ++oldStateNum;
                            newStatesSet.add(entry.getValue());
                        }
                    }
                }
                if (newStateNum==newStatesSet.size()) break;
                newStateNum = 0;
            }
            HashMap<Integer,Integer> stateConvert = new HashMap<>();
            ArrayList<T> statesName = new ArrayList<>();
            HashSet<T> acceptName = new HashSet<>();
            T startName = null;
            for (ArrayList<Integer> set: newStatesSet) {
                for (Integer id : set) {
                    T node = getNode(id);
                    stateConvert.put(id, set.get(0));
                    if (!statesName.contains(getNode(stateConvert.get(id)))) {
                        statesName.add(node);
                        if (accept.contains(id)) acceptName.add(node);
                        if (start.equals(id)) startName = node;
                    }
                }
            }
            DfaMachine<T,P> simpleMachine = new DfaMachine<T,P>(statesName,new ArrayList<>(doubleWayWithPathNId.keySet()),startName,acceptName);
            for (int nodeId = 0; nodeId< doubleWayWithNodeNId.size(); ++nodeId) {
                if (nodeId==stateConvert.get(nodeId)) {
                    for (int pathId = 0; pathId< doubleWayWithPathNId.size(); ++pathId) {
                        Integer targetId = findTheTarget(nodeId,pathId);
                        if (targetId!=null)
                            simpleMachine.addPath(getNode(nodeId), getNode(stateConvert.get(findTheTarget(nodeId, pathId))), getPath(pathId));
                    }
                }
            }
            return simpleMachine;
        }

        public void replace(Integer beginId, Integer endId, Integer path) {
            if (!mapWithPathSet.get(beginId).containsKey(endId))
                mapWithPathSet.get(beginId).put(endId, new HashSet<>());
            mapWithPathSet.get(beginId).get(endId).add(path);
            if (!mapWithTarget.get(beginId).containsKey(path))
                mapWithTarget.get(beginId).put(path,endId);
            else mapWithTarget.get(beginId).replace(path,endId);
        }

        @Override
        public boolean addPath(Integer beginId, Integer endId, Integer path) {
            if (!mapWithPathSet.get(beginId).containsKey(endId))
                mapWithPathSet.get(beginId).put(endId, new HashSet<>());
            mapWithPathSet.get(beginId).get(endId).add(path);
            if (!mapWithTarget.get(beginId).containsKey(path))
                mapWithTarget.get(beginId).put(path,endId);
            else if (!mapWithTarget.get(beginId).get(path).equals(endId)) throw new RuntimeException();
            return true;
        }

        void setStart(Integer sta) {start = sta; }
        void addAccept(Integer ac) {accept.add(ac);}
        void addNode() {
            mapWithTarget.add(new HashMap<>());
            mapWithPathSet.add(new HashMap<>());
        }


        Integer run(Integer []input) {
            Integer now = start;
            for (Integer integer : input) {
                now = move(now, integer);
                if (now==null) return null;
            }
            return now;
        }

        private Integer move(Integer begin, Integer path) {
            return mapWithTarget.get(begin).get(path);
        }

       /* void print() {
            for (int i = 0; i < nodeNameToId.size(); ++i) {
                System.out.println(getNode(i) + " :");
                for (Map.Entry<Integer, Integer> entry : mapWithTarget.get(i).entrySet()) {
                    System.out.println(getPath(entry.getKey()) + ":  ");
                    System.out.println(getNode(entry.getValue())+", ");
                }
                System.out.println();
            }
            System.out.println(accept);
        }*/

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < doubleWayWithNodeNId.size(); ++i) {
                result.append(getNode(i)).append(" :\n");
                for (Map.Entry<Integer, Integer> entry : mapWithTarget.get(i).entrySet()) {
                    result.append(getPath(entry.getKey())).append(":  ");
                    result.append(getNode(entry.getValue())).append("\n");
                }
                result.append("\n");
            }
            result.append(getAccept());
            return result.toString();
        }

        Integer start;
        HashSet<Integer> accept;
        ArrayList<HashMap<Integer,Integer>> mapWithTarget = new ArrayList<>();
        ArrayList<HashMap<Integer, HashSet<Integer>>> mapWithPathSet = new ArrayList<>();
        //private int startId;
    }
}
