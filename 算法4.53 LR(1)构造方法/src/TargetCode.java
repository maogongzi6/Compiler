import java.util.ArrayList;
import java.util.HashMap;

public class TargetCode {
    static ArrayList<String> makeTargetCode(ArrayList<Msil> code) {
        for (Msil now: code) {
            if (now.result!=null&& now.result.getClass().equals(Temp.class) && !tempToOffset.containsKey(now.result)) {
                tempToOffset.put((Temp)now.result,nowOffset);
                nowOffset+=((Temp)now.result).type.width;
            }
            if (now.param_1!=null&& now.param_1.getClass().equals(Temp.class) && !tempToOffset.containsKey(now.param_1)) {
                tempToOffset.put((Temp)now.param_1,nowOffset);
                nowOffset+=((Temp)now.param_1).type.width;
            }
            if (now.param_2!=null&& now.param_2.getClass().equals(Temp.class) && !tempToOffset.containsKey(now.param_2)) {
                tempToOffset.put((Temp)now.param_2,nowOffset);
                nowOffset+=((Temp)now.param_2).type.width;
            }
            switch (now.func) {
                case "reset": codeToTarget.put(codeCount++,targetCount); targetCount+=1; target.add("RESET "+((ConstantInt)now.result).value); break;
                case "pop": codeToTarget.put(codeCount++,targetCount); targetCount+=1; target.add("POP "+getRegister(now.result)); break;
                case "push": String registerName = getRegister(now.param_2);
                if (!now.param_2.getClass().equals(ConstantInt.class)) {
                    codeToTarget.put(codeCount++,targetCount); targetCount+=2;
                    if (now.param_2.getClass().equals(Temp.class))
                        target.add("LDR_T " + registerName+","+tempToOffset.get(now.param_2));
                    else if (now.param_2.getClass().equals(StretchRecord.class)) target.add("LDR " + registerName+","+((StretchRecord)now.param_2).offset);
                    else target.add("LDR " + registerName+","+((SymbolRecord)now.param_2).offset);
                    target.add("PUSH " + registerName);

                }
                else {
                    codeToTarget.put(codeCount++,targetCount); targetCount+=1;
                    target.add("PUSH " + ((ConstantInt) now.param_2).value);

                }
                break;
                case "load":if (now.param_1.getClass().equals(ConstantInt.class)) {
                    codeToTarget.put(codeCount++,targetCount); targetCount+=1;
                    target.add("STO " + tempToOffset.get(now.result) + "," + ((ConstantInt) now.param_1).value);

                }
                else {
                    codeToTarget.put(codeCount++,targetCount); targetCount+=1;
                    registerName = getRegister(now.param_1);
                    target.add("LDR_T "+registerName+","+tempToOffset.get(now.param_1));
                    target.add("STO_T "+tempToOffset.get(now.result)+","+registerName);

                }
                break;
                case "assign": codeToTarget.put(codeCount++,targetCount); targetCount+=2;
                    registerName = getRegister(now.param_1);
                if (now.param_1.getClass().equals(Temp.class)) {
                    target.add("LDR_T "+registerName+","+tempToOffset.get(now.param_1));
                }
                else {
                    target.add("LDR "+registerName+","+((SymbolRecord)now.param_1).offset);
                }
                target.add("STO "+((SymbolRecord)now.result).offset+","+registerName);

                break;
                case "assign[]": codeToTarget.put(codeCount++,targetCount); targetCount+=5;
                    registerName = getRegister(now.param_1);
                target.add("LDR "+registerName+","+((SymbolRecord)now.param_1).offset);
                target.add("ADD "+registerName+","+tempToOffset.get(now.param_2));
                String registerName_2 = getRegister(now.result);
                target.add("LDR_T "+registerName_2+","+tempToOffset.get(now.result));
                target.add("LDR "+registerName+","+registerName_2);

                break;
                case "*":
                case "/":
                case "-":
                case "+": codeToTarget.put(codeCount++,targetCount); targetCount+=3;
                    registerName = getRegister(now.param_1);
                if (now.param_1.getClass().equals(ConstantInt.class))
                    target.add("MOV "+registerName+","+((ConstantInt) now.param_1).value);
                else if (now.param_1.getClass().equals(Temp.class))
                    target.add("LDR_T "+registerName+","+tempToOffset.get(now.param_1));
                else target.add("LDR"+registerName+","+((SymbolRecord)now.param_1).offset);
                registerName_2 = getRegister(now.param_2);
                if (now.param_2.getClass().equals(ConstantInt.class))
                    target.add("MOV "+registerName_2+","+((ConstantInt) now.param_2).value);
                else if (now.param_2.getClass().equals(Temp.class))
                    target.add("LDR_T "+registerName_2+","+tempToOffset.get(now.param_2));
                else target.add("LDR "+registerName_2+","+((SymbolRecord)now.param_2).offset);
                target.add(now.func+" "+registerName+","+registerName_2);
                target.add("STO_T "+tempToOffset.get(now.result)+","+registerName);

                    break;
                case "<":
                case "<=":
                case ">":
                case ">=":
                case "==":
                case "!=":codeToTarget.put(codeCount++,targetCount); targetCount+=4;
                    registerName = getRegister(now.param_1);
                    if (now.param_1.getClass().equals(ConstantInt.class))
                        target.add("MOV "+registerName+","+((ConstantInt) now.param_1).value);
                    else if (now.param_1.getClass().equals(Temp.class))
                        target.add("LDR_T "+registerName+","+tempToOffset.get(now.param_1));
                    else target.add("LDR"+registerName+","+((SymbolRecord)now.param_1).offset);
                    registerName_2 = getRegister(now.param_2);
                    if (now.param_2.getClass().equals(ConstantInt.class))
                        target.add("MOV "+registerName_2+","+((ConstantInt) now.param_2).value);
                    else if (now.param_2.getClass().equals(Temp.class))
                        target.add("LDR_T "+registerName_2+","+tempToOffset.get(now.param_2));
                    else target.add("LDR "+registerName_2+","+((SymbolRecord)now.param_2).offset);
                    target.add(now.func+" "+registerName+","+registerName_2);
                    target.add("GOTO "+registerName);

                    break;
                case "goto":
                    if (now.result.getClass().equals(Temp.class)) {
                        codeToTarget.put(codeCount++,targetCount); targetCount+=2;
                        registerName = getRegister(now.result);
                        target.add("LDR_T "+registerName);
                        target.add("GOTO " + registerName);

                    }
                    else {
                        codeToTarget.put(codeCount++,targetCount); targetCount+=1;
                        registerName = getRegister(now.result);
                        target.add("GOTO " + registerName);
                    }
                    break;
                case "ret":
                    codeToTarget.put(codeCount++,targetCount); targetCount+=1;
                    target.add("RET");

                    break;
                case "func":
                    codeToTarget.put(codeCount++,targetCount); targetCount+=1;
                    target.add("FUNC");

                    break;
            }
        }
        /*for (int i = 0;i<code.size();++i) {
            if (code.get(i).func.equals(">") || code.get(i).func.equals(">=") || code.get(i).func.equals("<") || code.get(i).func.equals("<=") || code.get(i).func.equals("==") || code.get(i).func.equals("!=") || code.get(i).func.equals("goto")) {
                String t = target.get(codeToTarget.get(i));
                if (t.charAt(t.length() - 1) == '-')
                    target.set(codeToTarget.get(i),  t.replace("-", codeToTarget.get(i).toString()));
                //else t = "GOTO "+ codeToTarget.get(Integer.valueOf(t.substring(t.length() - 3, t.length())));
            }
        }*/

        /*for (String s: target){
            if (s.substring(0,4).equals("GOTO")) {
                if (s.charAt(s.length()-1)=='-')
                    s.replace("-",codeToTarget.get())
            }
        }*/
        /*for (String s: target)
            System.out.println(s);
        System.out.println(codeToTarget);*/
        return target;

    }
    static String getRegister(Unit toSave) {
        for (Register register: registers) {
            if (!register.isUsing) {
                register.isUsing = true;
                register.saving = toSave;
                return register.name;
            }
        }
        r = (r+1)%registers.size();
        if (registers.get(r).saving.getClass().equals(Temp.class)) {
            targetCount += 1;
            target.add("LDR_T " + registers.get(r).name + "," + tempToOffset.get((Temp) registers.get(r).saving));

        }
        return registers.get(r).name;
    }
    static ArrayList<Register> registers = new ArrayList<>(){{
        add(new Register("eax"));
        add(new Register("ebx"));
        add(new Register("ecx"));
        add(new Register("edx"));
    }};
    static int codeCount = 0;
    static int targetCount = 0;

    static HashMap<Integer,Integer> codeToTarget = new HashMap<>();
    static HashMap<Temp,Integer> tempToOffset = new HashMap<>();
    static int nowOffset = 0;
    static int r = 0;
    static ArrayList<String> target = new ArrayList<>();

}

class Register {
    Register(String n) {name = n;isUsing = false;}
    String name;
    boolean isUsing;
    Unit saving;
}