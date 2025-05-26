import AST.*;
import Midcode.midCode;
import Word.Word;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Parsing_mid {
    // 语法符号枚举，便于后续扩展
    private enum Symbol {
        A, IDENFR, INTCON, STRCON, MAINTK, CONSTTK, INTTK, BREAKTK, CONTINUETK, IFTK, ELSETK,
        NOT, AND, OR, WHILETK, GETINTTK, PRINTFTK, RETURNTK, PLUS, MINU, VOIDTK, MULT, DIV,
        MOD, LSS, LEQ, GRE, GEQ, EQL, NEQ, ASSIGN, SEMICN, COMMA, LPARENT, RPARENT, LBRACK,
        RBRACK, LBRACE, RBRACE
    }

    // 中间代码序列
    private ArrayList<midCode> midCodes = new ArrayList<>();
    // 字符串常量池
    private LinkedList<String> stringPool = new LinkedList<>();
    // AST根节点
    private Program astRoot;
    // 词法单元流
    private ArrayList<Word> tokenStream;
    // 标签与临时变量计数器
    private int labelCounter = 0;
    private int tempVarCounter = 0;
    private int curIndex = 0;

    public Parsing_mid(ArrayList<Word> tokens) {
        this.tokenStream = tokens;
    }

    public ArrayList<midCode> getMidCodes() {
        return midCodes;
    }

    public LinkedList<String> getStrings() {
        return stringPool;
    }

    // 主分析入口
    public void analyse() {
        // astRoot = parseCompUnit();
        astRoot.gen();
        midCodes = astRoot.getMidCodes();
        midCodes.add(new midCode(midCode.operation.EXIT, null));
        stringPool = astRoot.getStringss();
        printMidCode();
    }

    // 输出中间代码
    public void printMidCode() {
        midCodes.forEach(code -> System.out.println(code));
    }

    // 获取下一个词法单元
    private Word nextToken() {
        if (curIndex < tokenStream.size()) {
            return tokenStream.get(curIndex++);
        }
        return new Word();
    }

    // 查看当前词法单元
    private Word peekToken() {
        if (curIndex < tokenStream.size())
            return tokenStream.get(curIndex);
        return new Word();
    }

    // 查看指定位置的词法单元
    private Word peekToken(int pos) {
        if (pos < tokenStream.size())
            return tokenStream.get(pos);
        return new Word();
    } // 错误报告

    private void reportError() {
        System.out.print("---Syntax Error---");
    }

    public void CompUnit() {
        LinkedList<Decl> decls = new LinkedList<>();
        LinkedList<Func> funcs = new LinkedList<>();
        while (!peekToken(curIndex + 2).getContent().equals("("))
            decls.add(Decl());
        while (!peekToken(curIndex + 1).getContent().equals("main"))
            funcs.add(FuncDef());
        funcs.add(MainFuncDef());
        astRoot = new Program(decls, funcs);
    }

    private Decl Decl() {
        if (peekToken().getContent().equals("const")) {
            return ConstDecl();
        } else {
            return VarDecl();
        }
    }

    private Decl ConstDecl() {
        LinkedList<Def> defs = new LinkedList<>();
        if (nextToken().getContent().equals("const")) {
            BType();
            defs.add(ConstDef());
            while (peekToken().getContent().equals(",")) {
                nextToken();
                defs.add(ConstDef());
            }
            if (!nextToken().getContent().equals(";")) {
                reportError();
            }
        } else {
            reportError();
        }
        return new Decl(defs, true);
    }

    private void BType() {
        if (!nextToken().getContent().equals("int")) {
            reportError();
        }
    }

    private Def ConstDef() {
        Word w = nextToken();
        Lval lval = null;
        Expr expr1 = null;
        Expr expr2 = null;
        ArrayList<Expr> exprs = new ArrayList<>();
        if (w.getSymnumber() == 1) {
            int count = 0;
            while (peekToken().getContent().equals("[")) {
                count++;
                nextToken();
                if (count == 1)
                    expr1 = ConstExp();
                else if (count == 2)
                    expr2 = ConstExp();
                if (!nextToken().getContent().equals("]")) {
                    reportError();
                }
            }
            if (count == 0)
                lval = new Id(w);
            else if (count == 1)
                lval = new Array(w, expr1);
            else
                lval = new Array(w, expr1, expr2);
            if (!nextToken().getContent().equals("=")) {
                reportError();
            }
            ConstInitVal(exprs);
        } else {
            reportError();
        }
        return new ConstDef(lval, exprs);
    }

    private void ConstInitVal(ArrayList<Expr> exprs) {
        if (peekToken().getContent().equals("{")) {
            nextToken();
            if (peekToken().getContent().equals("}")) {
                nextToken();
            } else {
                ConstInitVal(exprs);
                while (peekToken().getContent().equals(",")) {
                    nextToken();
                    ConstInitVal(exprs);
                }
                if (!nextToken().getContent().equals("}")) {
                    reportError();
                }
            }
        } else {
            exprs.add(ConstExp());
        }
    }

    private Decl VarDecl() {
        LinkedList<Def> defs = new LinkedList<>();
        BType();
        defs.add(VarDef());
        while (peekToken().getContent().equals(",")) {
            nextToken();
            defs.add(VarDef());
        }
        if (!nextToken().getContent().equals(";")) {
            reportError();
        }
        return new Decl(defs, false);
    }

    private Def VarDef() {
        Word w = nextToken();
        int level = 0;
        Lval lval = null;
        Expr expr1 = null;
        Expr expr2 = null;
        ArrayList<ArrayList<Expr>> exprs = new ArrayList<>();
        if (w.getSymnumber() == 1) {
            int count = 0;
            while (peekToken().getContent().equals("[")) {
                count++;
                nextToken();
                if (count == 1)
                    expr1 = ConstExp();
                else if (count == 2)
                    expr2 = ConstExp();
                if (!nextToken().getContent().equals("]")) {
                    reportError();
                }
            }
            if (count == 0)
                lval = new Id(w);
            else if (count == 1)
                lval = new Array(w, expr1);
            else
                lval = new Array(w, expr1, expr2);
            if (peekToken().getContent().equals("=")) {
                nextToken();
                InitVal(exprs, level);
            }
        } else {
            reportError();
        }
        return new VarDef(lval, exprs);
    }

    private void InitVal(ArrayList<ArrayList<Expr>> exprs, int level) {
        if (peekToken().getContent().equals("{")) {
            if (level == 1)
                exprs.add(new ArrayList<>());
            nextToken();
            if (peekToken().getContent().equals("}")) {
                nextToken();
            } else {
                InitVal(exprs, level + 1);
                while (peekToken().getContent().equals(",")) {
                    nextToken();
                    InitVal(exprs, level + 1);
                }
                if (!nextToken().getContent().equals("}")) {
                    reportError();
                }
            }
        } else {
            if (exprs.size() == 0) {
                exprs.add(new ArrayList<>());
            }
            exprs.get(exprs.size() - 1).add(Exp());
        }
    }

    private Func FuncDef() {
        int functype = FuncType();
        Id id = new Id(nextToken());
        ArrayList<Fparam> paras = new ArrayList<>();
        Block block;
        if (!nextToken().getContent().equals("(")) {
            reportError();
        }
        if (peekToken().getContent().equals(")")) {
            nextToken();
            block = Block();
        } else {
            paras = FuncFParams();
            if (!nextToken().getContent().equals(")")) {
                reportError();
            }
            block = Block();
        }
        return new Func(functype, id, paras, block);
    }

    private Func MainFuncDef() {
        nextToken();
        nextToken();
        nextToken();
        nextToken();
        Block block = Block();
        return new Func(1, new Id(new Word("main")), new ArrayList<>(), block, true);
    }

    private int FuncType() {
        String s = nextToken().getContent();
        if (s.equals("void"))
            return 0;
        else if (s.equals("int"))
            return 1;
        else
            return -1;
    }

    private ArrayList<Fparam> FuncFParams() {
        ArrayList<Fparam> list = new ArrayList<>();
        list.add(FuncFParam());
        while (peekToken().getContent().equals(",")) {
            nextToken();
            list.add(FuncFParam());
        }
        return list;
    }

    private Fparam FuncFParam() {
        BType();
        int count = 0;
        Expr expr = null;
        Id id = new Id(nextToken());
        if (peekToken().getContent().equals("[")) {
            count++;
            nextToken();
            if (!nextToken().getContent().equals("]"))
                reportError();
            while (peekToken().getContent().equals("[")) {
                count++;
                nextToken();
                expr = ConstExp();
                if (!nextToken().getContent().equals("]"))
                    reportError();
            }
        }
        return new Fparam(id, count, expr);
    }

    private Block Block() {
        ArrayList<BlockItem> items = new ArrayList<>();
        if (!nextToken().getContent().equals("{")) {
            reportError();
        }
        if (peekToken().getContent().equals("}")) {
            nextToken();
        } else {
            BlockItem item = BlockItem();
            if (item != null) {
                items.add(item);
            }
            while (!peekToken().getContent().equals("}")) {
                item = BlockItem();
                if (item != null) {
                    items.add(item);
                }
            }
            nextToken();
        }
        return new Block(items);
    }

    private BlockItem BlockItem() {
        if (peekToken().getContent().equals("int") || peekToken().getContent().equals("const")) {
            return Decl();
        } else {
            return Stmt();
        }
    }

    private Stmt Stmt() {
        if (peekToken().getContent().equals("if")) {
            Stmt stmt2 = null;
            nextToken();
            if (!nextToken().getContent().equals("("))
                reportError();
            Or or = Cond();
            if (!nextToken().getContent().equals(")"))
                reportError();
            Stmt stmt1 = Stmt();
            if (peekToken().getContent().equals("else")) {
                nextToken();
                stmt2 = Stmt();
            }
            return new If(or, stmt1, stmt2);
        } else if (peekToken().getContent().equals("{")) {
            return Block();
        } else if (peekToken().getContent().equals("while")) {
            nextToken();
            if (!nextToken().getContent().equals("("))
                reportError();
            Or or = Cond();
            if (!nextToken().getContent().equals(")"))
                reportError();
            Stmt stmt = Stmt();
            return new While(or, stmt);
        } else if (peekToken().getContent().equals("break") || peekToken().getContent().equals("continue")) {
            int flag = 0;
            if (nextToken().getContent().equals("break")) {
                flag = 1;
            }
            if (!nextToken().getContent().equals(";")) {
                reportError();
            }
            if (flag == 1) {
                return new Break();
            } else {
                return new Continue();
            }
        } else if (peekToken().getContent().equals("return")) {
            Expr expr = null;
            nextToken();
            if (peekToken().getContent().equals(";")) {
                nextToken();
            } else {
                expr = Exp();
                if (!nextToken().getContent().equals(";"))
                    reportError();
            }
            return new Ret(expr);
        } else if (peekToken().getContent().equals("printf")) {
            Word format;
            ArrayList<Expr> exprs = new ArrayList<>();
            nextToken();
            if (!nextToken().getContent().equals("("))
                reportError();
            format = nextToken();
            while (peekToken().getContent().equals(",")) {
                nextToken();
                exprs.add(Exp());
            }
            if (!nextToken().getContent().equals(")"))
                reportError();
            if (!nextToken().getContent().equals(";"))
                reportError();
            return new Print(format, exprs);
        } else if (peekToken().getContent().equals(";")) {
            nextToken();
        } else if (peekToken().getSymnumber() == 1) {
            int flag1 = 0;
            if (peekToken(curIndex + 1).getContent().equals("=")) {
                flag1 = 1;
            } else if (peekToken(curIndex + 1).getContent().equals("(")) {
                flag1 = 2;
            } else if (peekToken(curIndex + 1).getContent().equals("[")) {
                int k = curIndex + 1;
                while (peekToken(k).getContent().equals("[")) {
                    k++;
                    int level = 1;
                    while (level > 0) {
                        if (peekToken(k).getContent().equals("["))
                            level++;
                        else if (peekToken(k).getContent().equals("]"))
                            level--;
                        k++;
                    }
                }
                if (peekToken(k).getContent().equals("="))
                    flag1 = 1;
                else
                    flag1 = 2;
            } else {
                flag1 = 2;
            }
            if (flag1 == 1) {
                Lval lval = (Lval) LVal();
                nextToken();
                if (peekToken().getContent().equals("getint")) {
                    nextToken();
                    if (!nextToken().getContent().equals("("))
                        reportError();
                    if (!nextToken().getContent().equals(")"))
                        reportError();
                    if (!nextToken().getContent().equals(";"))
                        reportError();
                    return new Scanf(lval);
                } else {
                    Expr expr = Exp();
                    if (!nextToken().getContent().equals(";"))
                        reportError();
                    return new Assign(lval, expr);
                }
            } else {
                Expr expr = Exp();
                if (!nextToken().getContent().equals(";"))
                    reportError();
                return expr;
            }
        } else {
            Expr expr = Exp();
            if (!nextToken().getContent().equals(";"))
                reportError();
            return expr;
        }
        return null;
    }

    private Expr Exp() {
        return AddExp();
    }

    private Or Cond() {
        return LOrExp();
    }

    private Expr LVal() {
        Word id = nextToken();
        int flag = 0;
        Expr exp1 = null;
        Expr exp2 = null;
        while (peekToken().getContent().equals("[")) {
            flag++;
            nextToken();
            if (flag == 1)
                exp1 = Exp();
            else if (flag == 2)
                exp2 = Exp();
            if (!nextToken().getContent().equals("]")) {
                reportError();
            }
        }
        if (flag == 0) {
            return new Id(id);
        } else if (flag == 1) {
            return new Array(id, exp1);
        } else {
            return new Array(id, exp1, exp2);
        }
    }

    private Expr PrimaryExp() {
        if (peekToken().getContent().equals("(")) {
            nextToken();
            Expr expr = Exp();
            if (!nextToken().getContent().equals(")")) {
                reportError();
            }
            return expr;
        } else if (peekToken().getSymnumber() == 1) {
            return LVal();
        } else if (peekToken().getSymnumber() == 2) {
            return Number();
        } else {
            reportError();
        }
        return null;
    }

    private Expr Number() {
        return new Constant(nextToken());
    }

    private Expr UnaryExp() {
        String s = peekToken().getContent();
        if (peekToken().getSymnumber() == 1 && peekToken(curIndex + 1).getContent().equals("(")) {
            Word w = nextToken();
            ArrayList<Expr> exprs = new ArrayList<>();
            nextToken();
            if (peekToken().getContent().equals(")")) {
                nextToken();
            } else {
                exprs = FuncRParams();
                if (!nextToken().getContent().equals(")"))
                    reportError();
            }
            return new FuncR(w, exprs);
        } else if (s.equals("+") || s.equals("-") || s.equals("!")) {
            Word w = UnaryOp();
            Expr expr = UnaryExp();
            return new Unary(w, expr);
        } else {
            return PrimaryExp();
        }
    }

    private Word UnaryOp() {
        return nextToken();
    }

    private ArrayList<Expr> FuncRParams() {
        ArrayList<Expr> exprs = new ArrayList<>();
        exprs.add(Exp());
        while (peekToken().getContent().equals(",")) {
            nextToken();
            exprs.add(Exp());
        }
        return exprs;
    }

    private Expr MulExp() {
        Expr expr1 = UnaryExp();
        while (true) {
            String s = peekToken().getContent();
            if (s.equals("*") || s.equals("/") || s.equals("%")) {
                Word w = nextToken();
                Expr expr2 = UnaryExp();
                expr1 = new Arith(w, expr1, expr2);
            } else {
                break;
            }
        }
        return expr1;
    }

    private Expr AddExp() {
        Expr expr1 = MulExp();
        while (peekToken().getContent().equals("+") || peekToken().getContent().equals("-")) {
            Word w = nextToken();
            Expr expr2 = MulExp();
            expr1 = new Arith(w, expr1, expr2);
        }
        return expr1;
    }

    private Expr RelExp() {
        Expr expr1 = AddExp();
        while (true) {
            String s = peekToken().getContent();
            if (s.equals("<") || s.equals(">") || s.equals("<=") || s.equals(">=")) {
                Word w = nextToken();
                Expr expr2 = AddExp();
                expr1 = new Logical(w, expr1, expr2);
            } else {
                break;
            }
        }
        return expr1;
    }

    private Expr EqExp() {
        Expr expr1 = RelExp();
        while (peekToken().getContent().equals("==") || peekToken().getContent().equals("!=")) {
            Word w = nextToken();
            Expr expr2 = RelExp();
            expr1 = new Logical(w, expr1, expr2);
        }
        return expr1;
    }

    private And LAndExp() {
        ArrayList<Expr> exprs = new ArrayList<>();
        exprs.add(EqExp());
        while (peekToken().getContent().equals("&&")) {
            nextToken();
            exprs.add(EqExp());
        }
        return new And(exprs);
    }

    private Or LOrExp() {
        ArrayList<And> ands = new ArrayList<>();
        ands.add(LAndExp());
        while (peekToken().getContent().equals("||")) {
            nextToken();
            ands.add(LAndExp());
        }
        return new Or(ands);
    }

    private Expr ConstExp() {
        return AddExp();
    }

}
