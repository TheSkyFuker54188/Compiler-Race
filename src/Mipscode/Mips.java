package Mipscode;

import Midcode.midCode;
import Optim.Register;
import Symbol_table.FuncTable;
import Symbol_table.IntergerTable;
import Symbol_table.Symbols.ArraySymbol;
import Symbol_table.Symbols.NorSymbol;
import Symbol_table.Symbols.VarSymbol;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Mips {

    ArrayList<midCode> pushStack = new ArrayList<>(); 
    HashMap<String, Integer> funcLenMap = new HashMap<>(); 
    HashMap<String, String> strLabelMap = new HashMap<>(); 
    private ArrayList<midCode> irList;
    private LinkedList<String> strList;
    private ArrayList<Mipscode> mipsList = new ArrayList<>();
    private IntergerTable curIntTable = new IntergerTable(); 
    private boolean inFunc = false; 
    private boolean inMain = false;
    private int funcPtr = 0;
    private int divLabelIdx = 0; 
    private Register regAlloc;

    public Mips(ArrayList<midCode> midCodes, LinkedList<String> strings) {
        this.irList = midCodes;
        this.strList = strings;
        regAlloc = new Register();
        getFuncLenMap();
        generateMips();
        printMips();
    }

    void getFuncLenMap() {
        String funcName = null;
        int idx = 0;
        int cnt = 0;
        for (idx = 0; idx < irList.size(); idx++) {
            if (irList.get(idx).op == midCode.operation.FUNC || irList.get(idx).op == midCode.operation.MAIN) {
                break;
            }
        }
        while (idx < irList.size()) {
            midCode md = irList.get(idx);
            if (md.op == midCode.operation.FUNC || md.op == midCode.operation.MAIN) {
                if (funcName != null) {
                    funcLenMap.put(funcName, cnt);
                }
                funcName = md.z;
                cnt = 0;
            }
            if (md.op == midCode.operation.ARRAY) {
                int k = Integer.parseInt(md.x);
                if (md.y != null) {
                    int l = Integer.parseInt(md.y);
                    k *= l;
                }
                cnt += k;
            }
            cnt += 2;
            idx++;
        }
        funcLenMap.put(funcName, cnt);
    }

    int getOffset(String name) { 
        IntergerTable table = curIntTable;
        while (table != null) {
            if (table.contains(name)) {
                return table.get(name).getOffset();
            }
            table = table.getOut();
        }
        return -1;
    }

    boolean checkname(String name) {
        return name.length() >= 2 && name.charAt(1) == '&';
    }

    boolean ispointer(String name) {
        IntergerTable table = curIntTable;
        while (table != null) {
            if (table.contains(name)) {
                return table.get(name).isIspointer();
            }
            table = table.getOut();
        }
        return false;
    }

    boolean isGlobal(String name) { 
        IntergerTable table = curIntTable;
        while (table != null) {
            if (table.contains(name)) {
                return table.getOut() == null;
            }
            table = table.getOut();
        }
        return false;
    }

    void addNormalVar(String name) { 
        if (curIntTable.contains(name)) {
            return;
        } else {
            curIntTable.add(name, new VarSymbol(name, funcPtr));
            funcPtr += 1;
        }
    }

    void addNormalVar(String name, boolean isPtr) {
        if (curIntTable.contains(name)) {
            return;
        } else {
            curIntTable.add(name, new ArraySymbol(name, funcPtr, true));
            funcPtr += 1;
        }
    }

    void addNormalVar(String name, int len) {
        if (curIntTable.getOut() == null) {
            curIntTable.add(name, new ArraySymbol(name, funcPtr));
            funcPtr += len;
        } else {
            funcPtr += len - 1;
            curIntTable.add(name, new ArraySymbol(name, funcPtr));
            funcPtr += 1;
        }
    }

    String getValue(String name, String regName, boolean allowTable) { 
        if (checkname(name)) {
            String addr = regAlloc.findtemp(name);
            return addr;
        }
        if (Character.isDigit(name.charAt(0)) || name.charAt(0) == '-') {
            mipsList.add(new Mipscode(Mipscode.operation.li, regName, "", "", Integer.parseInt(name)));
        } else {
            if (allowTable) {
                addNormalVar(name);
            }
            boolean global = isGlobal(name);
            int offset = getOffset(name);
            if (global) {
                mipsList.add(new Mipscode(Mipscode.operation.lw, regName, "$gp", "", 4 * offset));
            } else {
                mipsList.add(new Mipscode(Mipscode.operation.lw, regName, "$fp", "", -4 * offset));
            }
        }
        return regName;
    }

    String getAddr(String name, String regName) {
        IntergerTable table = curIntTable;
        if (Character.isDigit(name.charAt(0)) || name.charAt(0) == '-') {
            mipsList.add(new Mipscode(Mipscode.operation.li, regName, "", "", Integer.parseInt(name)));
            return regName;
        }
        while (table != null) {
            if (table.contains(name)) {
                NorSymbol sym = table.get(name);
                if (sym instanceof ArraySymbol) {
                    if (ispointer(name)) {
                        getValue(name, regName, false);
                    } else {
                        if (isGlobal(name)) {
                            mipsList.add(
                                    new Mipscode(Mipscode.operation.addi, regName, "$gp", "", 4 * sym.getOffset()));
                        } else {
                            mipsList.add(
                                    new Mipscode(Mipscode.operation.addi, regName, "$fp", "", -4 * sym.getOffset()));
                        }
                        return regName;
                    }
                } else {
                    String addr = getValue(name, regName, false);
                    return addr;
                }
                break;
            }
            table = table.getOut();
        }
        String addr = getValue(name, regName, false);
        return addr;
    }

    void saveValue(String name, String regName, boolean allowTable) { 
        if (allowTable) {
            addNormalVar(name);
        }
        boolean global = isGlobal(name);
        int offset = getOffset(name);
        if (global) {
            mipsList.add(new Mipscode(Mipscode.operation.sw, regName, "$gp", "", 4 * offset));
        } else {
            mipsList.add(new Mipscode(Mipscode.operation.sw, regName, "$fp", "", -4 * offset));
        }
    }

    boolean isTempVar(String s) {
        if (s.length() < 2)
            return false;
        return s.charAt(1) == '&';
    }

    int getTwoExp(int k) {
        int a = 0;
        while (k % 2 == 0) {
            a++;
            k /= 2;
        }
        return a;
    }

    boolean isPowerOfTwo(int k) {
        return (k & (k - 1)) == 0;
    }

    void modHandler(midCode mc) {
        divHandler(mc);
        String addr = getValue(mc.y, "$t1", false);
        mipsList.add(new Mipscode(Mipscode.operation.mult, addr, "$t2", ""));
        mipsList.add(new Mipscode(Mipscode.operation.mflo, "$t2"));
        mipsList.add(new Mipscode(Mipscode.operation.sub, "$t2", "$t0", "$t2"));
        if (checkname(mc.z)) {
            mipsList.add(new Mipscode(Mipscode.operation.moveop, regAlloc.gettemp(mc.z), "$t2"));
        } else {
            saveValue(mc.z, "$t2", isTempVar(mc.z));
        }
    }

    int leftCheck(int m) {
        while (m % 2 == 0) {
            m /= 2;
        }
        return m;
    }

    void divHandler(midCode mc) {
        String addr = getValue(mc.x, "$t0", false);
        mipsList.add(new Mipscode(Mipscode.operation.moveop, "$t0", addr));
        if (Character.isDigit(mc.y.charAt(0))) {
            int divnum = Integer.parseInt(mc.y);
            int k = getTwoExp(divnum);
            int left = leftCheck(divnum);
            if (isPowerOfTwo(divnum)) {
                divLabelIdx++;
                mipsList.add(new Mipscode(Mipscode.operation.bgez, "$t0", "divjump" + divLabelIdx));
                mipsList.add(new Mipscode(Mipscode.operation.addi, "$t0", "$t0", "", divnum - 1));
                mipsList.add(new Mipscode(Mipscode.operation.label, "divjump" + divLabelIdx));
                mipsList.add(new Mipscode(Mipscode.operation.sra, "$t2", "$t0", "", k));
                return;
            } else if (left == 625) {
                if (k > 0) {
                    mipsList.add(new Mipscode(Mipscode.operation.sra, "$t2", "$t0", "", k));
                }
                mipsList.add(new Mipscode(Mipscode.operation.li, "$t1", "", "", 0x68DB8BAD));
                if (k > 0)
                    mipsList.add(new Mipscode(Mipscode.operation.mult, "$t2", "$t1", ""));
                else
                    mipsList.add(new Mipscode(Mipscode.operation.mult, "$t0", "$t1", ""));
                mipsList.add(new Mipscode(Mipscode.operation.mfhi, "$t2"));
                mipsList.add(new Mipscode(Mipscode.operation.sra, "$t2", "$t2", "", 8));
                divLabelIdx++;
                mipsList.add(new Mipscode(Mipscode.operation.bgez, "$t2", "divjump" + divLabelIdx));
                mipsList.add(new Mipscode(Mipscode.operation.addi, "$t2", "$t2", "", 1));
                mipsList.add(new Mipscode(Mipscode.operation.label, "divjump" + divLabelIdx));
                return;
            } else if (left == 125) {
                if (k > 0) {
                    mipsList.add(new Mipscode(Mipscode.operation.sra, "$t2", "$t0", "", k));
                }
                mipsList.add(new Mipscode(Mipscode.operation.li, "$t1", "", "", 0x10624DD3));
                if (k > 0)
                    mipsList.add(new Mipscode(Mipscode.operation.mult, "$t2", "$t1", ""));
                else
                    mipsList.add(new Mipscode(Mipscode.operation.mult, "$t0", "$t1", ""));
                mipsList.add(new Mipscode(Mipscode.operation.mfhi, "$t2"));
                mipsList.add(new Mipscode(Mipscode.operation.sra, "$t2", "$t2", "", 3));
                divLabelIdx++;
                mipsList.add(new Mipscode(Mipscode.operation.bgez, "$t2", "divjump" + divLabelIdx));
                mipsList.add(new Mipscode(Mipscode.operation.addi, "$t2", "$t2", "", 1));
                mipsList.add(new Mipscode(Mipscode.operation.label, "divjump" + divLabelIdx));
                return;
            } else if (left == 25) {
                if (k > 0) {
                    mipsList.add(new Mipscode(Mipscode.operation.sra, "$t2", "$t0", "", k));
                }
                mipsList.add(new Mipscode(Mipscode.operation.li, "$t1", "", "", 0x51EB851F));
                if (k > 0)
                    mipsList.add(new Mipscode(Mipscode.operation.mult, "$t2", "$t1", ""));
                else
                    mipsList.add(new Mipscode(Mipscode.operation.mult, "$t0", "$t1", ""));
                mipsList.add(new Mipscode(Mipscode.operation.mfhi, "$t2"));
                mipsList.add(new Mipscode(Mipscode.operation.sra, "$t2", "$t2", "", 3));
                divLabelIdx++;
                mipsList.add(new Mipscode(Mipscode.operation.bgez, "$t2", "divjump" + divLabelIdx));
                mipsList.add(new Mipscode(Mipscode.operation.addi, "$t2", "$t2", "", 1));
                mipsList.add(new Mipscode(Mipscode.operation.label, "divjump" + divLabelIdx));
                return;
            } else if (left == 11) {
                if (k > 0) {
                    mipsList.add(new Mipscode(Mipscode.operation.sra, "$t2", "$t0", "", k));
                }
                mipsList.add(new Mipscode(Mipscode.operation.li, "$t1", "", "", 0x2E8BA2E9));
                if (k > 0)
                    mipsList.add(new Mipscode(Mipscode.operation.mult, "$t2", "$t1", ""));
                else
                    mipsList.add(new Mipscode(Mipscode.operation.mult, "$t0", "$t1", ""));
                mipsList.add(new Mipscode(Mipscode.operation.mfhi, "$t2"));
                mipsList.add(new Mipscode(Mipscode.operation.sra, "$t2", "$t2", "", 1));
                divLabelIdx++;
                mipsList.add(new Mipscode(Mipscode.operation.bgez, "$t2", "divjump" + divLabelIdx));
                mipsList.add(new Mipscode(Mipscode.operation.addi, "$t2", "$t2", "", 1));
                mipsList.add(new Mipscode(Mipscode.operation.label, "divjump" + divLabelIdx));
                return;
            } else if (left == 9) {
                if (k > 0) {
                    mipsList.add(new Mipscode(Mipscode.operation.sra, "$t2", "$t0", "", k));
                }
                mipsList.add(new Mipscode(Mipscode.operation.li, "$t1", "", "", 0x38E38E39));
                if (k > 0)
                    mipsList.add(new Mipscode(Mipscode.operation.mult, "$t2", "$t1", ""));
                else
                    mipsList.add(new Mipscode(Mipscode.operation.mult, "$t0", "$t1", ""));
                mipsList.add(new Mipscode(Mipscode.operation.mfhi, "$t2"));
                mipsList.add(new Mipscode(Mipscode.operation.sra, "$t2", "$t2", "", 1));
                divLabelIdx++;
                mipsList.add(new Mipscode(Mipscode.operation.bgez, "$t2", "divjump" + divLabelIdx));
                mipsList.add(new Mipscode(Mipscode.operation.addi, "$t2", "$t2", "", 1));
                mipsList.add(new Mipscode(Mipscode.operation.label, "divjump" + divLabelIdx));
                return;
            } else if (left == 7) {
                if (k > 0) {
                    mipsList.add(new Mipscode(Mipscode.operation.sra, "$t2", "$t0", "", k));
                }
                mipsList.add(new Mipscode(Mipscode.operation.li, "$t1", "", "", 0x92492493));
                if (k > 0)
                    mipsList.add(new Mipscode(Mipscode.operation.multu, "$t2", "$t1", ""));
                else
                    mipsList.add(new Mipscode(Mipscode.operation.multu, "$t0", "$t1", ""));
                mipsList.add(new Mipscode(Mipscode.operation.mfhi, "$t2"));
                mipsList.add(new Mipscode(Mipscode.operation.sra, "$t2", "$t2", "", 2));
                divLabelIdx++;
                mipsList.add(new Mipscode(Mipscode.operation.bgez, "$t2", "divjump" + divLabelIdx));
                mipsList.add(new Mipscode(Mipscode.operation.addi, "$t2", "$t2", "", 1));
                mipsList.add(new Mipscode(Mipscode.operation.label, "divjump" + divLabelIdx));
                return;
            } else if (5 == left) {
                if (k > 0) {
                    mipsList.add(new Mipscode(Mipscode.operation.sra, "$t2", "$t0", "", k));
                }
                mipsList.add(new Mipscode(Mipscode.operation.li, "$t1", "", "", 0x66666667));
                if (k > 0)
                    mipsList.add(new Mipscode(Mipscode.operation.mult, "$t2", "$t1", ""));
                else
                    mipsList.add(new Mipscode(Mipscode.operation.mult, "$t0", "$t1", ""));
                mipsList.add(new Mipscode(Mipscode.operation.mfhi, "$t2"));
                mipsList.add(new Mipscode(Mipscode.operation.sra, "$t2", "$t2", "", 1));
                divLabelIdx++;
                mipsList.add(new Mipscode(Mipscode.operation.bgez, "$t2", "divjump" + divLabelIdx));
                mipsList.add(new Mipscode(Mipscode.operation.addi, "$t2", "$t2", "", 1));
                mipsList.add(new Mipscode(Mipscode.operation.label, "divjump" + divLabelIdx));
                return;
            } else if (3 == left) {
                if (k > 0) {
                    mipsList.add(new Mipscode(Mipscode.operation.sra, "$t2", "$t0", "", k));
                }
                mipsList.add(new Mipscode(Mipscode.operation.li, "$t1", "", "", 0x55555556));
                if (k > 0)
                    mipsList.add(new Mipscode(Mipscode.operation.mult, "$t2", "$t1", ""));
                else
                    mipsList.add(new Mipscode(Mipscode.operation.mult, "$t0", "$t1", ""));
                mipsList.add(new Mipscode(Mipscode.operation.mfhi, "$t2"));
                divLabelIdx++;
                mipsList.add(new Mipscode(Mipscode.operation.bgez, "$t2", "divjump" + divLabelIdx));
                mipsList.add(new Mipscode(Mipscode.operation.addi, "$t2", "$t2", "", 1));
                mipsList.add(new Mipscode(Mipscode.operation.label, "divjump" + divLabelIdx));
                return;
            }
        }

        String addr2 = getValue(mc.y, "$t1", false);
        mipsList.add(new Mipscode(Mipscode.operation.divop, "$t0", addr2, ""));
        mipsList.add(new Mipscode(Mipscode.operation.mflo, "$t2"));
    }

    public void generateMips() { 
        mipsList.add(new Mipscode(Mipscode.operation.dataSeg, ""));
        for (int i = 0; i < strList.size(); i++) {
            mipsList.add(new Mipscode(Mipscode.operation.asciizSeg, "s_" + i, strList.get(i)));
            strLabelMap.put(strList.get(i), "s_" + i);
        }
        mipsList.add(new Mipscode(Mipscode.operation.textSeg, ""));
        for (int i = 0; i < irList.size(); i++) {
            midCode mc = irList.get(i);
            if (mc.op.equals(midCode.operation.PLUSOP)) {
                String addr1 = getValue(mc.x, "$t0", false);
                String addr2 = getValue(mc.y, "$t1", false);
                if (checkname(mc.z)) {
                    mipsList.add(new Mipscode(Mipscode.operation.add, regAlloc.gettemp(mc.z), addr1, addr2));
                } else {
                    mipsList.add(new Mipscode(Mipscode.operation.add, "$t2", addr1, addr2));
                    saveValue(mc.z, "$t2", isTempVar(mc.z));
                }
            } else if (mc.op.equals(midCode.operation.MINUOP)) {
                String addr1 = getValue(mc.x, "$t0", false);
                String addr2 = getValue(mc.y, "$t1", false);
                if (checkname(mc.z)) {
                    mipsList.add(new Mipscode(Mipscode.operation.sub, regAlloc.gettemp(mc.z), addr1, addr2));
                } else {
                    mipsList.add(new Mipscode(Mipscode.operation.sub, "$t2", addr1, addr2));
                    saveValue(mc.z, "$t2", isTempVar(mc.z));
                }
            } else if (mc.op.equals(midCode.operation.MULTOP)) {
                String addr1 = getValue(mc.x, "$t0", false);
                String addr2 = getValue(mc.y, "$t1", false);
                if (checkname(mc.z)) {
                    mipsList.add(new Mipscode(Mipscode.operation.mult, addr1, addr2, ""));
                    mipsList.add(new Mipscode(Mipscode.operation.mflo, regAlloc.gettemp(mc.z)));
                } else {
                    mipsList.add(new Mipscode(Mipscode.operation.mult, "$t0", "$t1", ""));
                    mipsList.add(new Mipscode(Mipscode.operation.mflo, "$t2"));
                    saveValue(mc.z, "$t2", isTempVar(mc.z));
                }
            } else if (mc.op.equals(midCode.operation.DIVOP)) {
                divHandler(mc);
                if (checkname(mc.z)) {
                    mipsList.add(new Mipscode(Mipscode.operation.moveop, regAlloc.gettemp(mc.z), "$t2"));
                } else {
                    saveValue(mc.z, "$t2", isTempVar(mc.z));
                }
            } else if (mc.op.equals(midCode.operation.MODOP)) {
                modHandler(mc);
            } else if (mc.op.equals(midCode.operation.ASSIGNOP)) {
                String addr = getValue(mc.x, "$t0", false);
                if (checkname(mc.z)) {
                    mipsList.add(new Mipscode(Mipscode.operation.moveop, regAlloc.gettemp(mc.z), addr));
                } else {
                    saveValue(mc.z, addr, isTempVar(mc.z));
                }
            } else if (mc.op.equals(midCode.operation.PUSH)) {
                pushStack.add(mc);
            } else if (mc.op.equals(midCode.operation.CALL)) {
                for (int j = 0; j < pushStack.size(); j++) {
                    midCode mcs = pushStack.get(j);
                    if (mcs.x != null) {
                        getAddr(mcs.z, "$t0");
                        String addr = getValue(mcs.x, "$t1", false);
                        mipsList.add(new Mipscode(Mipscode.operation.li, "$t2", "", "", Integer.parseInt(mcs.y) * 4));
                        mipsList.add(new Mipscode(Mipscode.operation.mult, "$t2", addr, ""));
                        mipsList.add(new Mipscode(Mipscode.operation.mflo, "$t2"));
                        mipsList.add(new Mipscode(Mipscode.operation.add, "$t0", "$t0", "$t2"));
                    } else {
                        getAddr(mcs.z, "$t0");
                        String addr = getAddr(mcs.z, "$t0");
                        mipsList.add(new Mipscode(Mipscode.operation.sw, addr, "$sp", "", -4 * j));
                        ;
                    }
                }
                pushStack.clear();
                ArrayList<String> lists = regAlloc.getReverlists();
                int len = lists.size();
                mipsList.add(new Mipscode(Mipscode.operation.addi, "$sp", "$sp", "",
                        -4 * funcLenMap.get(mc.z) - 8 - 4 * len));
                mipsList.add(new Mipscode(Mipscode.operation.sw, "$ra", "$sp", "", 4));
                mipsList.add(new Mipscode(Mipscode.operation.sw, "$fp", "$sp", "", 8));
                for (int k = 0; k < len; k++) {
                    mipsList.add(new Mipscode(Mipscode.operation.sw, lists.get(k), "$sp", "", 12 + 4 * k));
                }
                mipsList.add(new Mipscode(Mipscode.operation.addi, "$fp", "$sp", "",
                        4 * funcLenMap.get(mc.z) + 8 + 4 * len));
                mipsList.add(new Mipscode(Mipscode.operation.jal, mc.z));
                for (int k = len - 1; k >= 0; k--) {
                    mipsList.add(new Mipscode(Mipscode.operation.lw, lists.get(k), "$sp", "", 12 + 4 * k));
                }
                mipsList.add(new Mipscode(Mipscode.operation.lw, "$fp", "$sp", "", 8));
                mipsList.add(new Mipscode(Mipscode.operation.lw, "$ra", "$sp", "", 4));
                mipsList.add(new Mipscode(Mipscode.operation.addi, "$sp", "$sp", "",
                        4 * funcLenMap.get(mc.z) + 8 + 4 * len));
            } else if (mc.op.equals(midCode.operation.RET)) {
                if (inMain) {
                    mipsList.add(new Mipscode(Mipscode.operation.li, "$v0", "", "", 10));
                    mipsList.add(new Mipscode(Mipscode.operation.syscall, ""));
                } else {
                    if (mc.z != null) {              
                        if (checkname(mc.z)) {
                            mipsList.add(new Mipscode(Mipscode.operation.moveop, "$v0", regAlloc.findtemp(mc.z)));
                        } else {
                            getValue(mc.z, "$v0", false);
                        }
                    }
                    mipsList.add(new Mipscode(Mipscode.operation.jr, "$ra"));
                }
            } else if (mc.op.equals(midCode.operation.RETVALUE)) {
                if (checkname(mc.z)) {
                    mipsList.add(new Mipscode(Mipscode.operation.moveop, regAlloc.gettemp(mc.z), "$v0"));
                } else {
                    saveValue(mc.z, "$v0", isTempVar(mc.z));
                }
            } else if (mc.op.equals(midCode.operation.PRINT)) {
                if (mc.x.equals("string")) {
                    String addr = strLabelMap.get(mc.z);
                    mipsList.add(new Mipscode(Mipscode.operation.la, "$a0", addr));
                    mipsList.add(new Mipscode(Mipscode.operation.li, "$v0", "", "", 4));
                    mipsList.add(new Mipscode(Mipscode.operation.syscall, "", "", ""));
                } else {
                    if (checkname(mc.z)) {
                        mipsList.add(new Mipscode(Mipscode.operation.moveop, "$a0", regAlloc.findtemp(mc.z)));
                    } else {
                        getValue(mc.z, "$a0", false);
                    }
                    mipsList.add(new Mipscode(Mipscode.operation.li, "$v0", "", "", 1));
                    mipsList.add(new Mipscode(Mipscode.operation.syscall, null));

                }
            } else if (mc.op.equals(midCode.operation.SCAN)) {
                mipsList.add(new Mipscode(Mipscode.operation.li, "$v0", "", "", 5));
                mipsList.add(new Mipscode(Mipscode.operation.syscall, null));
                if (checkname(mc.z)) {
                    mipsList.add(new Mipscode(Mipscode.operation.moveop, regAlloc.gettemp(mc.z), "$v0"));
                } else {
                    saveValue(mc.z, "$v0", isTempVar(mc.z));
                }
            } else if (mc.op.equals(midCode.operation.LABEL)) {
                if (mc.x.equals("start")) {
                    curIntTable = new IntergerTable(curIntTable);
                } else if (mc.x.equals("end")) {
                    funcPtr -= curIntTable.getContentlength();
                    curIntTable = curIntTable.getOut();
                    regAlloc.reset();
                }
            } else if (mc.op.equals(midCode.operation.FUNC)) {
                if (!inFunc) {
                    mipsList.add(new Mipscode(Mipscode.operation.j, "main")); 
                    inFunc = true;
                }
                mipsList.add(new Mipscode(Mipscode.operation.label, mc.z));
                funcPtr = 0;
            } else if (mc.op.equals(midCode.operation.PARAM)) {
                if (mc.x.equals("0"))
                    addNormalVar(mc.z);
                else {
                    addNormalVar(mc.z, true); 
                }
            } else if (mc.op.equals(midCode.operation.GETARRAY)) {
                String addr1 = getValue(mc.y, "$t0", false);
                mipsList.add(new Mipscode(Mipscode.operation.sll, "$t0", addr1, "", 2));
                if (ispointer(mc.x)) {
                    getValue(mc.x, "$t1", false);
                    mipsList.add(new Mipscode(Mipscode.operation.add, "$t1", "$t1", "$t0"));
                    mipsList.add(new Mipscode(Mipscode.operation.lw, "$t2", "$t1", "", 0));
                } else {
                    if (isGlobal(mc.x)) {
                        mipsList.add(new Mipscode(Mipscode.operation.add, "$t1", "$t0", "$gp"));
                        mipsList.add(new Mipscode(Mipscode.operation.lw, "$t2", "$t1", "", 4 * getOffset(mc.x)));
                    } else {
                        mipsList.add(new Mipscode(Mipscode.operation.addu, "$t1", "$t0", "$fp"));
                        mipsList.add(new Mipscode(Mipscode.operation.lw, "$t2", "$t1", "", -4 * getOffset(mc.x)));
                    }
                }
                if (checkname(mc.z)) {
                    mipsList.add(new Mipscode(Mipscode.operation.moveop, regAlloc.gettemp(mc.z), "$t2"));
                } else {
                    saveValue(mc.z, "$t2", isTempVar(mc.z));
                }
            } else if (mc.op.equals(midCode.operation.PUTARRAY)) {
                String addr1 = getValue(mc.y, "$t0", false);
                String addr2 = getValue(mc.x, "$t1", false);
                mipsList.add(new Mipscode(Mipscode.operation.sll, "$t1", addr2, "", 2));
                if (ispointer(mc.z)) { 
                    getValue(mc.z, "$t2", false);
                    mipsList.add(new Mipscode(Mipscode.operation.add, "$t2", "$t2", "$t1"));
                    mipsList.add(new Mipscode(Mipscode.operation.sw, addr1, "$t2", "", 0));
                } else {
                    if (isGlobal(mc.z)) {
                        mipsList.add(new Mipscode(Mipscode.operation.add, "$t1", "$t1", "$gp"));
                        mipsList.add(new Mipscode(Mipscode.operation.sw, addr1, "$t1", "", 4 * getOffset(mc.z)));
                    } else {
                        mipsList.add(new Mipscode(Mipscode.operation.addu, "$t1", "$t1", "$fp"));
                        mipsList.add(new Mipscode(Mipscode.operation.sw, addr1, "$t1", "", -4 * getOffset(mc.z))); // 数组还有点小问题，记得考虑一下
                    }
                }
            } else if (mc.op.equals(midCode.operation.CONST)) {
                String addr = getValue(mc.x, "$t0", false);
                saveValue(mc.z, addr, true);
            } else if (mc.op.equals(midCode.operation.EXIT)) {
            } else if (mc.op.equals(midCode.operation.VAR)) {
                if (mc.x != null) {
                    String addr = getValue(mc.x, "$t0", false);
                    saveValue(mc.z, addr, true);
                } else {
                    addNormalVar(mc.z);
                }
            } else if (mc.op.equals(midCode.operation.ARRAY)) {
                int k;
                if (mc.y == null) {
                    k = Integer.parseInt(mc.x);
                } else {
                    int l = Integer.parseInt(mc.x);
                    k = Integer.parseInt(mc.y);
                    k *= l;
                }
                addNormalVar(mc.z, k);
            } else if (mc.op.equals(midCode.operation.MAIN)) {
                if (!inFunc) {
                    mipsList.add(new Mipscode(Mipscode.operation.j, "main")); 
                    inFunc = true;
                }
                inMain = true;
                mipsList.add(new Mipscode(Mipscode.operation.label, mc.z));
                funcPtr = 0;
                int len = funcLenMap.get("main");
                mipsList.add(new Mipscode(Mipscode.operation.moveop, "$fp", "$sp"));
                mipsList.add(new Mipscode(Mipscode.operation.addi, "$sp", "$sp", "", -4 * len - 8));
            } else if (mc.op.equals(midCode.operation.LSSOP)) { // <
                String addr1 = getValue(mc.x, "$t0", false);
                String addr2 = getValue(mc.y, "$t1", false);
                if (checkname(mc.z)) {
                    mipsList.add(new Mipscode(Mipscode.operation.slt, regAlloc.gettemp(mc.z), addr1, addr2));
                } else {
                    mipsList.add(new Mipscode(Mipscode.operation.slt, "$t2", addr1, addr2));
                    saveValue(mc.z, "$t2", true);
                }
            } else if (mc.op.equals(midCode.operation.LEQOP)) { // <=
                String addr1 = getValue(mc.x, "$t0", false);
                String addr2 = getValue(mc.y, "$t1", false);
                if (checkname(mc.z)) {
                    mipsList.add(new Mipscode(Mipscode.operation.sle, regAlloc.gettemp(mc.z), addr1, addr2));
                } else {
                    mipsList.add(new Mipscode(Mipscode.operation.sle, "$t2", addr1, addr2));
                    saveValue(mc.z, "$t2", true);
                }
            } else if (mc.op.equals(midCode.operation.GREOP)) { // >
                String addr1 = getValue(mc.x, "$t0", false);
                String addr2 = getValue(mc.y, "$t1", false);
                if (checkname(mc.z)) {
                    mipsList.add(new Mipscode(Mipscode.operation.sgt, regAlloc.gettemp(mc.z), addr1, addr2));
                } else {
                    mipsList.add(new Mipscode(Mipscode.operation.sgt, "$t2", addr1, addr2));
                    saveValue(mc.z, "$t2", true);
                }
            } else if (mc.op.equals(midCode.operation.GEQOP)) { // >=
                String addr1 = getValue(mc.x, "$t0", false);
                String addr2 = getValue(mc.y, "$t1", false);
                if (checkname(mc.z)) {
                    mipsList.add(new Mipscode(Mipscode.operation.sge, regAlloc.gettemp(mc.z), addr1, addr2));
                } else {
                    mipsList.add(new Mipscode(Mipscode.operation.sge, "$t2", addr1, addr2));
                    saveValue(mc.z, "$t2", true);
                }
            } else if (mc.op.equals(midCode.operation.EQLOP)) { // ==
                String addr1 = getValue(mc.x, "$t0", false);
                String addr2 = getValue(mc.y, "$t1", false);
                if (checkname(mc.z)) {
                    mipsList.add(new Mipscode(Mipscode.operation.seq, regAlloc.gettemp(mc.z), addr1, addr2));
                } else {
                    mipsList.add(new Mipscode(Mipscode.operation.seq, "$t2", addr1, addr2));
                    saveValue(mc.z, "$t2", true);
                }
            } else if (mc.op.equals(midCode.operation.NEQOP)) {
                String addr1 = getValue(mc.x, "$t0", false);
                String addr2 = getValue(mc.y, "$t1", false);
                if (checkname(mc.z)) {
                    mipsList.add(new Mipscode(Mipscode.operation.sne, regAlloc.gettemp(mc.z), addr1, addr2));
                } else {
                    mipsList.add(new Mipscode(Mipscode.operation.sne, "$t2", addr1, addr2));
                    saveValue(mc.z, "$t2", true);
                } // !=
            } else if (mc.op.equals(midCode.operation.BZ)) {
                String addr = getValue(mc.x, "$t0", false);
                mipsList.add(new Mipscode(Mipscode.operation.li, "$t1", "", "", 0));
                mipsList.add(new Mipscode(Mipscode.operation.beq, mc.z, addr, "$t1"));
            } else if (mc.op.equals(midCode.operation.GOTO)) {
                mipsList.add(new Mipscode(Mipscode.operation.j, mc.z, "", ""));
            } else if (mc.op.equals(midCode.operation.Jump)) {
                mipsList.add(new Mipscode(Mipscode.operation.label, mc.z));
            } else if (mc.op.equals(midCode.operation.DEBUG)) {
                continue;
            } else if (mc.op.equals(midCode.operation.SLL)) {
                String addr = getValue(mc.x, "$t0", false);
                mipsList.add(new Mipscode(Mipscode.operation.sll, "$t0", addr, "", Integer.parseInt(mc.y)));
                if (checkname(mc.z)) {
                    mipsList.add(new Mipscode(Mipscode.operation.moveop, regAlloc.gettemp(mc.z), "$t0"));
                } else {
                    saveValue(mc.z, "$t0", true);
                }
            } else if (mc.op.equals(midCode.operation.SRA)) {
                String addr = getValue(mc.x, "$t0", false);
                mipsList.add(new Mipscode(Mipscode.operation.sra, "$t0", addr, "", Integer.parseInt(mc.y)));
                if (checkname(mc.z)) {
                    mipsList.add(new Mipscode(Mipscode.operation.moveop, regAlloc.gettemp(mc.z), "$t0"));
                } else {
                    saveValue(mc.z, "$t0", true);
                }
            } else {
                System.out.print("what happened!!!!!!!!");
            }
        }
    }

    public void printMips() {
        String outputpath = "mips.txt";
        PrintStream out = null;
        try {
            out = new PrintStream(outputpath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.setOut(out);
        for (int i = 0; i < mipsList.size(); i++) {
            Mipscode mc = mipsList.get(i);
            switch (mc.op) {
                case add:
                    System.out.println("add " + mc.z + "," + mc.x + "," + mc.y);
                    break;
                case addu:
                    System.out.println("addu " + mc.z + "," + mc.x + "," + mc.y);
                    break;
                case sle:
                    System.out.println("sle " + mc.z + "," + mc.x + "," + mc.y);
                    break;
                case sgt:
                    System.out.println("sgt " + mc.z + "," + mc.x + "," + mc.y);
                    break;
                case sge:
                    System.out.println("sge " + mc.z + "," + mc.x + "," + mc.y);
                    break;
                case slt:
                    System.out.println("slt " + mc.z + "," + mc.x + "," + mc.y);
                    break;
                case sne:
                    System.out.println("sne " + mc.z + "," + mc.x + "," + mc.y);
                    break;
                case seq:
                    System.out.println("seq " + mc.z + "," + mc.x + "," + mc.y);
                    break;
                case beq:
                    System.out.println("beq " + mc.x + "," + mc.y + "," + mc.z);
                    break;
                case sub:
                    System.out.println("sub " + mc.z + "," + mc.x + "," + mc.y);
                    break;
                case mult:
                    System.out.println("mult " + mc.z + "," + mc.x);
                    break;
                case multu:
                    System.out.println("multu " + mc.z + "," + mc.x);
                    break;
                case divop:
                    System.out.println("div " + mc.z + "," + mc.x);
                    break;
                case addi:
                    System.out.println("addi " + mc.z + "," + mc.x + "," + mc.imme);
                    break;
                case mflo:
                    System.out.println("mflo " + mc.z);
                    break;
                case mfhi:
                    System.out.println("mfhi " + mc.z);
                    break;
                case j:
                    System.out.println("j " + mc.z);
                    break;
                case jal:
                    System.out.println("jal " + mc.z);
                    break;
                case jr:
                    System.out.println("jr " + mc.z);
                    break;
                case lw:
                    System.out.println("lw " + mc.z + "," + mc.imme + "(" + mc.x + ")");
                    break;
                case sw:
                    System.out.println("sw " + mc.z + "," + mc.imme + "(" + mc.x + ")");
                    break;
                case syscall:
                    System.out.println("syscall");
                    break;
                case li:
                    System.out.println("li " + mc.z + "," + mc.imme);
                    break;
                case la:
                    System.out.println("la " + mc.z + "," + mc.x);
                    break;
                case moveop:
                    System.out.println("move " + mc.z + "," + mc.x);
                    break;
                case dataSeg:
                    System.out.println(".data");
                    break;
                case textSeg:
                    System.out.println("\n.text");
                    break;
                case asciizSeg:
                    System.out.println(mc.z + ": .asciiz \"" + mc.x + "\"");
                    break;
                case label:
                    System.out.println("\n" + mc.z + ":");
                    break;
                case sll:
                    System.out.println("sll " + mc.z + "," + mc.x + "," + mc.imme);
                    break;
                case sra:
                    System.out.println("sra " + mc.z + "," + mc.x + "," + mc.imme);
                    break;
                case bgez:
                    System.out.println("bgez " + mc.z + "," + mc.x);
                    break;
                default:
                    System.out.println("-------------------wrong-------------------");
                    break;
            }
        }
    }
}