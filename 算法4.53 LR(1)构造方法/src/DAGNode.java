import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

class DAGNode {
    static ArrayList<Msil> makeDAG(StreamMap map) {
        Iterator<Integer> iterator = map.block.iterator();
        Msil.count = 0;
        ArrayList<Msil> newCodeList = new ArrayList<>();
        for (int i = 0;i<map.tableList.size();++i) {
            ArrayList<Msil> newCode = new ArrayList<>();
            Integer begin = iterator.next(),end = map.block.higher(begin);
            ArrayList<DAGNode> leafSet = new ArrayList<>();
            ActiveTable nowTable = map.tableList.get(i);
            if (end==null) break;
            for (Unit unit: nowTable.table.keySet())
                leafSet.add(new DAGLeaf(unit));
            for (int pos = end-1;pos>=begin;--pos) {
                Msil code = map.code.get(pos);
                DAGInner inner = add(code.func,code.param_1,code.param_2,code.result);
                if (inner!=null)
                lineToNode.put(code.lineCount,inner);
            }
            System.out.println(leafSet);
            HashSet<DAGInner> set = new HashSet<>();
            for (int pos = begin;pos<end;++pos) {
                DAGInner inner = lineToNode.get(pos);
                if (inner!=null&&!set.contains(inner)) {
                    if (inner.param_1==null&&inner.param_2==null) {
                        if (inner.assignSet.isEmpty())
                            newCode.add(new Msil(inner.func, null, null, null));
                        else newCode.add(new Msil(inner.func, null, null, inner.assignSet.get(0)));
                    }
                    else if (inner.param_1==null) {
                        if (inner.assignSet.isEmpty())
                            newCode.add(new Msil(inner.func, null, inner.param_2.assignSet.get(0), null));
                        else
                            newCode.add(new Msil(inner.func, null, inner.param_2.assignSet.get(0), inner.assignSet.get(0)));
                    }
                    else if (inner.param_2==null) {
                        if (inner.assignSet.isEmpty())
                            newCode.add(new Msil(inner.func, inner.param_1.assignSet.get(0), null,null));
                        else
                            newCode.add(new Msil(inner.func, inner.param_1.assignSet.get(0), null, inner.assignSet.get(0)));
                    }
                    else{
                        if (inner.assignSet.isEmpty())
                            newCode.add(new Msil(inner.func, inner.param_1.assignSet.get(0), inner.param_2.assignSet.get(0),null));
                        else
                            newCode.add(new Msil(inner.func, inner.param_1.assignSet.get(0), inner.param_2.assignSet.get(0), inner.assignSet.get(0)));
                    }
                    set.add(inner);
                }
            }
            /*while (!leafSet.isEmpty()) {
                for (int pos_1 = 0; pos_1 < leafSet.size(); ++pos_1) {
                    DAGNode node_1 = leafSet.get(pos_1);
                    for (int pos_2 = 0; pos_2 < node_1.mamaSet.size(); ++pos_2) {
                        DAGInner node_2 = node_1.mamaSet.get(pos_2);
                        if (leafSet.contains(node_2.param_1) && leafSet.contains(node_2.param_2)) {
                            leafSet.add(node_2);
                            newCode.add(new Msil(node_2.func, node_2.param_1.assignSet.get(0), node_2.param_2.assignSet.get(0), node_2.assignSet.get(0)));
                            node_2.param_1.mamaSet.remove(node_2);
                            node_2.param_2.mamaSet.remove(node_2);
                        }
                    }
                    if (node_1.mamaSet.isEmpty()) leafSet.remove(node_1);
                }
            }*/
            newCodeList.addAll(newCode);
            blockToLine.put(i,newCode.get(0).lineCount);
        }
        for (int line = 0;line<newCodeList.size();++line) {
            switch (newCodeList.get(line).func) {
                case "<":
                case "<=":
                case ">":
                case ">=":
                case "==":
                case "!=":
                case "goto":
                    if (!newCodeList.get(line).result.getClass().equals(Temp.class)) {
                        newCodeList.get(line).result = new ConstantInt(blockToLine.get(((ConstantInt)newCodeList.get(line).result).value));
                    }
            }
        }
        //System.out.println(newCodeList);
        //System.out.println(blockToLine);
        return newCodeList;
    }
    DAGNode() {}
    static DAGInner add(String func,Unit p_1,Unit p_2,Unit as) {
        switch (func) {
            case "+":
            case "-":
            case "*":
            case "/":
            DAGNode t_1 = lastPlace.get(p_1);
            DAGNode t_2 = lastPlace.get(p_2);
            if (t_1==null||t_2==null) {
                DAGInner inner = new DAGInner();
                if (as!=null)
                    inner.assignSet.add(as);
                inner.func = func;
                inner.param_1 = new DAGLeaf(p_1);
                inner.param_2 = new DAGLeaf(p_2);
                return inner;
            }
            boolean mark = false;
                for (DAGInner mom : t_1.mamaSet) {
                    if (mom != null && mom.func.equals(func) && mom.param_1.equals(lastPlace.get(p_1)) && mom.param_2.equals(lastPlace.get(p_2))) {
                        lastPlace.replace(as, mom);
                        mom.assignSet.add(as);
                        mark = true;
                        return mom;
                    }
                }
                if (!mark) {
                    DAGInner inner = new DAGInner();
                    inner.func = func;
                    inner.param_1 = lastPlace.get(p_1);
                    inner.param_2 = lastPlace.get(p_2);
                    inner.assignSet.add(as);
                    lastPlace.replace(as, inner);
                    if (inner.param_1!=null&&inner.param_2!=null) {
                        inner.param_1.mamaSet.add(inner);
                        inner.param_2.mamaSet.add(inner);
                    }
                    return inner;
                }

            break;
            default:
                if (p_1==null) {
                    if (p_2==null) {
                        DAGInner inner = new DAGInner();
                        if (as!=null)
                            inner.assignSet.add(as);
                        inner.func = func;
                        return inner;
                    }
                    //t_2 = lastPlace.get(p_2);
                    DAGInner inner = new DAGInner();
                    inner.func = func;
                    inner.param_2 = new DAGLeaf(p_2);
                    inner.param_2.mamaSet.add(inner);
                    if (as!=null)
                        inner.assignSet.add(as);
                    return inner;
                } else if (p_2==null) {
                    //t_1 = lastPlace.get(p_1);
                    DAGInner inner = new DAGInner();
                    inner.func = func;
                    inner.param_1 = new DAGLeaf(p_1);
                    inner.param_1.mamaSet.add(inner);
                    if (as!=null)
                        inner.assignSet.add(as);
                    return inner;
                }
                else {
                    //t_1 = lastPlace.get(p_1);
                    //t_2 = lastPlace.get(p_2);
                    DAGInner inner = new DAGInner();
                    inner.func = func;
                    inner.param_1 = new DAGLeaf(p_1);
                    inner.param_1.mamaSet.add(inner);
                    inner.param_2 = new DAGLeaf(p_2);
                    inner.param_2.mamaSet.add(inner);
                    if (as!=null)
                        inner.assignSet.add(as);

                    return inner;
                }
        }
        return null;
    }

    ArrayList<Unit> assignSet = new ArrayList<>();
    static HashMap<Unit,DAGNode> lastPlace = new HashMap<>();
    ArrayList<DAGInner> mamaSet = new ArrayList<>();
    static HashMap<Integer,DAGInner> lineToNode = new HashMap<>();
    static HashMap<Integer,Integer> blockToLine = new HashMap<>();
}

class DAGInner extends DAGNode {
    void add(Unit as) {
        assignSet.add(as);

        //还有一些其它操作
    }
    String func;
    DAGNode param_1;
    DAGNode param_2;
}

class DAGLeaf extends DAGNode {
    DAGLeaf(Unit l) {
        leaf = l;
        lastPlace.put(leaf,this);
        assignSet.add(leaf);
    }
    Unit leaf;
}
