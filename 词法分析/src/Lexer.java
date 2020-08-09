
import java.io.File;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.*;

public class Lexer {
    private String[] key = {"int", "void", "char", "string","bool", "struct", "enum", "return","main", "if", "else", "while","for","true","false"};
    Overall overall = new Overall();//标识符
    /*private List<String> character = new ArrayList<>();//字符
    private List<String> string = new ArrayList<>();//字符串
    private List<Integer> number = new ArrayList<>();//数字
    private List<String> delimiter = new ArrayList<>();//界符*/

    ArrayList<Lex> lexResult = new ArrayList<>();

    public static void main(String[] args) {
        String pathIn = "in.txt";
        String pathOut = "out.txt";
        Lexer lexer = new Lexer();
        lexer.lexicalAnalyse(pathIn);
        System.out.println(lexer.lexResult);
    }
    private int nextChar(String str, int j){
        int t = str.charAt(j);
        if(t == 32) return 0;   //空格
        else if((t>=65 && t<=90)||(t>=97 && t<=122) || t==95) return 1; //字母或下划线
        else if(t>=48 && t<=57) return 2; //数字
        else if(t==34) return 3; //双引号
        else if(t==39) return 4; //单引号
        else if((t>=33 && t<=47)||(t>=58 && t<=64)||(t>=91 && t<=96)||(t>=123 && t<=126)) return 5; //界符
        else if (t=='\n') return 6;
        else return -1;
    }
    private int isKeyword(String w){
        int m, key = 0;
        for(m=0; m< this.key.length; m++) {
            if(w.equals(this.key[m])){
                key=1;
                break;
            }
        }
        if(key == 1){
            return m;
        }else
            return -1;
    }
    private String deleteUseless(String str){
        while(str.contains("/*")){
            if(str.contains("*/")) str=str.substring(0,str.indexOf("/*")).concat(str.substring(str.indexOf("*/")+2));
            else str=str.substring(str.indexOf("/*"));
        }
        str = str.replaceAll(" {2,}", " ");//删去多余的空格
        str = str.replaceAll("//[\\u4e00-\\u9fa5]*" ,  " ");
        if(str.charAt(0)==' ') str=str.substring(1);//删去首部空格
        if(str.charAt(str.length()-1)==' ') str=str.substring(0,str.length()-1);//删去尾部空格
        return str;
    }
    void lexicalAnalyse(String pathIn) {
        List<List<String>> tokenTable = new ArrayList<>();
        try{
            File filename = new File(pathIn);
            InputStreamReader reader = new InputStreamReader(new FileInputStream(filename)); // 建立一个输入流对象reader
            BufferedReader br = new BufferedReader(reader);// 建立一个对象，它把文件内容转成计算机能读懂的语言
            String line;
            StringBuilder strOut= new StringBuilder();
            line = br.readLine();//去换行符
            StringBuilder builder = new StringBuilder();
            while (line != null) {
                builder.append("\n").append(line);
                line=br.readLine();
            }
            br.close();
            int i=0;
            int j;//记录单词开始
            String word;

            String strIn = deleteUseless(builder.toString());

            System.out.println(strIn);

            int state;
            int lineCount = 0;
            Table now = overall;
            while(i < strIn.length()){
                state = nextChar(strIn, i);
                switch(state){
                    case 0://空格
                        i++;
                        break;

                    case 6://换行符
                        ++lineCount;
                        ++i;
                        break;

                    case 1://字母（单词）
                        j=i;
                        while((nextChar(strIn, i) == 1 //字母或下划线
                                || nextChar(strIn, i) == 2)//数字
                                &&i<=strIn.length()-1){
                            i++;
                        }
                        if(j==i) i++;
                        word = strIn.substring(j,i);
                        if(isKeyword(word) >= 0){
                            //strOut.append(" {k,").append(isKeyword(word)).append("}");
                            lexResult.add(new Key(word,lineCount));
                        }
                        else{
                            if(!now.table.containsKey(word)){
                                //indexOf返回该字符串第一次出现的索引
                                Identify iden = new Identify("identity",word,lineCount);
                                now.table.put(word,iden);
                                lexResult.add(iden);
                                //strOut.append(" {I,").append(identify.indexOf(word)).append("}");
                            }
                            else{
                                lexResult.add(now.table.get(word));
                                //identify.add(word);
                                //strOut.append(" {I,").append(identify.indexOf(word)).append("}");
                            }
                        }
                        break;

                    case 2://数字
                        j=i;
                        while(nextChar(strIn, i) == 2 && i<strIn.length()-1){
                            i++;
                        }
                        if(j==i) i++;
                        word=strIn.substring(j,i);
                        lexResult.add(new IntData("integer","Integer",lineCount, Integer.parseInt(word)));
                        /*if (number.contains(Integer.parseInt(word))){
                            strOut = new StringBuilder(strOut.toString().concat(" {c,").concat(String.valueOf(number.indexOf(Integer.parseInt(word)))).concat("}"));
                        }
                        else{
                            number.add(Integer.parseInt(word));
                            strOut = new StringBuilder(strOut.toString().concat(" {c,").concat(String.valueOf(number.indexOf(Integer.parseInt(word)))).concat("}"));
                        }*/
                        break;

                    case 3://字符串
                        j=i;
                        i++;
                        while(nextChar(strIn, i) != 3&&i<strIn.length()-1) i++;
                        word=strIn.substring(j,i+1);
                        if(nextChar(strIn, i) != 3) System.out.println("双引号不成对错误");
                        lexResult.add(new StringData("characterString","String",lineCount,word));
                        /*if(character.contains(word)){
                            strOut.append(" {C,").append(string.indexOf(word)).append("}");
                        }
                        else{
                            string.add(word);
                            strOut.append(" {C,").append(string.indexOf(word)).append("}");
                        }*/
                        i++;
                        break;

                    case 4://字符
                        j=i;
                        i++;
                        while(nextChar(strIn, i) != 4 && i<strIn.length()-1 ) i++;
                        word = strIn.substring(j,i+1);
                        if(nextChar(strIn, i) != 4) System.out.println("单引号不成对错误");
                        lexResult.add(new CharData("character","Character",lineCount, word.charAt(0)));
                        /*if(character.contains(word)){
                            strOut.append(" {C,").append(character.indexOf(word)).append("}");
                        }
                        else{
                            character.add(word);
                            strOut.append(" {C,").append(character.indexOf(word)).append("}");
                        }*/
                        i++;
                        break;

                    case 5://符号
                        word=strIn.substring(i,i+1);
                        if(i+1<strIn.length()){
                            if(nextChar(strIn, i) == 5)
                            {
                                if((strIn.charAt(i)=='&'&&strIn.charAt(i+1)=='&')
                                        ||(strIn.charAt(i)=='|'&&strIn.charAt(i+1)=='|')
                                        ||(strIn.charAt(i)=='>'&&strIn.charAt(i+1)=='=')
                                        ||(strIn.charAt(i)=='<'&&strIn.charAt(i+1)=='=')
                                        ||(strIn.charAt(i)=='='&&strIn.charAt(i+1)=='=')
                                        ||(strIn.charAt(i)=='!'&&strIn.charAt(i+1)=='='))
                                {
                                    word=strIn.substring(i,i+2);
                                    ++i;                //这里多自增一个，因为读了两个
                                }
                            }
                        }
                        ++i;
                        lexResult.add(new Delimiter(word,lineCount));
                        if (word.equals("{")) {
                            now = new Table(now);
                            lexResult.add(new TablePointer("pointer",lineCount,now));
                        }
                        if (word.equals("}")) {System.out.println("\n"+now.table+"\n");now = now.outer;}
                        /*if(delimiter.contains(word)){
                            strOut.append(" {p,").append(delimiter.indexOf(word)).append("}");
                        }
                        else{
                            delimiter.add(word);
                            strOut.append(" {p,").append(delimiter.indexOf(word)).append("}");
                        }*/
                        break;
                    case -1://
                        System.out.println("state异常");
                        System.out.println(strIn.charAt(i));
                        System.out.println((int)strIn.charAt(i));
                        break;
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        //return tokenTable;
    }
}

class Table {
    Table(Table t) {outer = t; }

    HashMap<String,Identify> table = new HashMap<>();

    public Table getOuter() {
        return outer;
    }

    Table outer;
}

class Overall extends Table {
    Overall() {
        super(null);
    }

    @Override
    public Table getOuter() {
        throw new RuntimeException();
    }
}