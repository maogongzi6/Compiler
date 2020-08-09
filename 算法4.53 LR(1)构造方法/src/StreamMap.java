import java.util.*;

public class StreamMap {
    StreamMap( ArrayList<Msil> c) {code = c; map = makeMap();}
    ArrayList<ArrayList<Integer>> makeMap() {
        //HashMap<Integer,Integer> lineToBlock = new HashMap<>();
        int blockCount = 0;
        for (Msil line: code) {
            switch (line.func) {
                case "<":
                case "<=":
                case ">":
                case ">=":
                case "==":
                case "!=":
                    block.add(((ConstantInt)line.result).value);
                    break;
                case "goto":
                    if (line.result==null||line.result.getClass().equals(Temp.class)) {
                        break;
                    }
                    block.add(((ConstantInt)line.result).value);
            }
        }
        block.add(code.size());
        block = new TreeSet<>(block);
        ArrayList<Integer> temp = new ArrayList<>(block);
        System.out.println(block);
        /*for (Map.Entry<Integer,Integer> entry: lineToBlock.entrySet())
            blockToLine.put(entry.getValue(),entry.getKey());
        System.out.println();*/
        for (int i = 0;i<block.size();++i) map.add(new ArrayList<>());
        for (Msil line: code) {
            switch (line.func) {
                case "<":
                case "<=":
                case ">":
                case ">=":
                case "==":
                case "!=":
                    int target = ((ConstantInt)line.result).value;
                    map.get(temp.indexOf(block.floor(line.lineCount))).add(target);
                    line.result = new ConstantInt(temp.indexOf(target));
                    String func = code.get(target-1).func;
                    if (!(func.equals("<")||func.equals("<=")||func.equals(">")||func.equals(">=")||func.equals("==")||func.equals("!=")||func.equals("goto"))) map.get(temp.indexOf(block.floor(target-1))).add(target);
                    break;
                case "goto":
                    if (line.result==null||line.result.getClass().equals(Temp.class)) {
                        break;
                    }
                    target = ((ConstantInt)line.result).value;
                    line.result = new ConstantInt(temp.indexOf(target));
                    map.get(temp.indexOf(block.floor(line.lineCount))).add(target);
                    func = code.get(target-1).func;
                    if (!(func.equals("<")||func.equals("<=")||func.equals(">")||func.equals(">=")||func.equals("==")||func.equals("!=")||func.equals("goto"))) map.get(temp.indexOf(block.floor(target-1))).add(target);

            }
        }
        System.out.println(code);
        return map;
    }
    void completeTable() {
        Iterator<Integer> iterator = block.iterator();
        for (int i = 0;i<block.size();++i) {
            tableList.add(new ActiveTable());
            Integer begin = iterator.next(),end = block.higher(begin);
            ActiveTable nowTable = tableList.get(i);
            if (end==null) break;
            for (int pos = end-1;pos>=begin;--pos) {
                Unit result = code.get(pos).result,param_1 = code.get(pos).param_1,param_2 = code.get(pos).param_2;
                if (result!=null&& !nowTable.table.containsKey(result)) {
                    if (result.getClass().equals(Temp.class)) nowTable.table.put(result, new ActiveState(false, null));
                    else nowTable.table.put(result, new ActiveState(true, null));
                }
                if (param_1!=null&& !nowTable.table.containsKey(param_1)) {
                    if (param_1.getClass().equals(Temp.class)) nowTable.table.put(param_1, new ActiveState(false, null));
                    else nowTable.table.put(param_1, new ActiveState(true, null));
                }
                if (param_2!=null&& !nowTable.table.containsKey(param_2)) {
                    if (param_2.getClass().equals(Temp.class)) nowTable.table.put(param_2, new ActiveState(false, null));
                    else nowTable.table.put(param_2, new ActiveState(true, null));
                }
                switch (code.get(pos).func) {
                    case "+":
                    case "-":
                    case "*":
                    case "/":
                        nowTable.table.get(result).ifActive = false;
                        nowTable.table.get(result).line = null;
                        nowTable.table.get(param_1).ifActive = nowTable.table.get(param_2).ifActive = true;
                        nowTable.table.get(param_1).line = nowTable.table.get(param_2).line = code.get(pos);
                        break;
                    case "assign":
                        nowTable.table.get(result).ifActive = false;
                        nowTable.table.get(result).line = null;
                        nowTable.table.get(param_1).ifActive = true;
                        nowTable.table.get(param_1).line = code.get(pos);
                        break;
                    case "assign[]":
                    case "<" :
                    case "<=" :
                    case ">" :
                    case ">=" :
                    case "==" :
                    case "!=" :
                        nowTable.table.get(param_1).ifActive = nowTable.table.get(param_2).ifActive = nowTable.table.get(result).ifActive = true;
                        nowTable.table.get(param_1).line = nowTable.table.get(param_2).line = nowTable.table.get(result).line = code.get(pos);
                        break;
                    case "load":
                        nowTable.table.get(param_1).ifActive = true;
                        nowTable.table.get(param_1).line = code.get(pos);
                        nowTable.table.get(result).ifActive = false;
                        nowTable.table.get(result).line = null;
                        break;
                    case "goto" :
                        nowTable.table.get(result).ifActive = true;
                        nowTable.table.get(result).line = code.get(pos);
                        break;
                }
            }
            HashSet<Unit> set = new HashSet<>();
            for (Unit unit :nowTable.table.keySet()) {
                if (unit.getClass().equals(ConstantInt.class)||unit.getClass().equals(ConstantChar.class)) set.add(unit);
            }
            for (Unit unit: set) nowTable.table.remove(unit);               //这里把常数去掉，常数应该是不需要记录的
        }
        for (int i = 0;i<block.size();++i) {System.out.println(tableList.get(i));}
    }
    ArrayList<Msil> code;
    ArrayList<ActiveTable> tableList = new ArrayList<>();
    TreeSet<Integer> block = new TreeSet<>() {{add(0);}};
    ArrayList<ArrayList<Integer>> map = new ArrayList<>();
    HashMap<Integer,Integer> blockToLine = new HashMap<>();
}
