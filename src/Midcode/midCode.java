/**
 * 中间代码表示类
 * 用于编译器中间代码生成阶段的四元组表示
 * @author CompilerTeam
 */
package Midcode;

public class midCode {
    // 四元组操作类型
    public operation op;
    // 目标操作数或结果存储位置
    public String z = null;
    // 第一个源操作数
    public String x = null;
    // 第二个源操作数
    public String y = null;

    /**
     * 完整四元组构造方法
     * 
     * @param op 操作类型
     * @param z  目标操作数
     * @param x  第一源操作数
     * @param y  第二源操作数
     */
    public midCode(operation op, String z, String x, String y) {
        this.op = op;
        this.z = z;
        this.x = x;
        this.y = y;
    }

    /**
     * 双操作数构造方法
     * 
     * @param op 操作类型
     * @param z  目标操作数
     */
    public midCode(operation op, String z) {
        this.op = op;
        this.z = z;
    }

    /**
     * 三操作数构造方法
     * 
     * @param op 操作类型
     * @param z  目标操作数
     * @param x  源操作数
     */
    public midCode(operation op, String z, String x) {
        this.op = op;
        this.z = z;
        this.x = x;
    }

    /**
     * 将中间代码四元组转换为字符串表示
     * 
     * @return 格式化后的中间代码字符串
     */
    @Override
    public String toString() {
        // 根据操作类型生成对应的中间代码表示
        switch (op) {
            case PLUSOP:
                return generateBinaryOperation("+");
            case MINUOP:
                return generateBinaryOperation("-");
            case MULTOP:
                return generateBinaryOperation("*");
            case DIVOP:
                return generateBinaryOperation("/");
            case MODOP:
                return generateBinaryOperation("%");
            case LSSOP:
                return generateBinaryOperation("<");
            case LEQOP:
                return generateBinaryOperation("<=");
            case GREOP:
                return generateBinaryOperation(">");
            case GEQOP:
                return generateBinaryOperation(">=");
            case EQLOP:
                return generateBinaryOperation("==");
            case NEQOP:
                return generateBinaryOperation("!=");
            case ASSIGNOP:
                return z + " = " + x;
            case GOTO:
                return "GOTO " + z;
            case BZ:
                return "if " + x + " == 0 then goto " + z;
            case BNZ:
                break;
            case Jump:
                return "        <JUMPDST " + z + ">";
            case PUSH:
                return generatePushOperation();
            case CALL:
                return "call " + z;
            case RET:
                return generateReturnOperation();
            case RETVALUE:
                return "retvalue " + z;
            case SCAN:
                return "scan " + z;
            case PRINT:
                return generatePrintOperation();
            case LABEL:
                return "    <LABEL " + z + " " + x + ">";
            case CONST:
                return "const int " + z + " = " + x;
            case ARRAY:
                return generateArrayDeclaration();
            case VAR:
                return generateVariableDeclaration();
            case FUNC:
                return x + " " + z + "( )";
            case PARAM:
                return generateParameterDeclaration();
            case MAIN:
                return "\nMAIN\n";
            case GETARRAY:
                return z + " = " + x + "[" + y + "]";
            case PUTARRAY:
                return z + "[" + x + "] = " + y;
            case EXIT:
                return "\n-----------------EXIT--------------\n";
            case SLL:
                return generateBinaryOperation("<<");
            case SRA:
                return generateBinaryOperation(">>");
            default:
                return null;
        }
        return null;
    }

    /**
     * 生成二元运算操作的字符串表示
     */
    private String generateBinaryOperation(String operator) {
        return z + " = " + x + " " + operator + " " + y;
    }

    /**
     * 生成PUSH操作的字符串表示
     */
    private String generatePushOperation() {
        if (x == null) {
            return "push " + z;
        } else {
            return "push " + z + "[" + x + "]" + "[" + y + "]";
        }
    }

    /**
     * 生成返回操作的字符串表示
     */
    private String generateReturnOperation() {
        if (z != null) {
            return "RET  " + z;
        } else {
            return "RET null";
        }
    }

    /**
     * 生成打印操作的字符串表示
     */
    private String generatePrintOperation() {
        if (x.equals("string")) {
            return "print \"" + z + "\"";
        } else {
            return "print " + z;
        }
    }

    /**
     * 生成数组声明的字符串表示
     */
    private String generateArrayDeclaration() {
        if (y == null) {
            return "array int " + z + "[" + x + "]";
        } else {
            return "array int " + z + "[" + x + "]" + "[" + y + "]";
        }
    }

    /**
     * 生成变量声明的字符串表示
     */
    private String generateVariableDeclaration() {
        if (x == null) {
            return "var int " + z;
        } else {
            return "var int " + z + " = " + x;
        }
    }

    /**
     * 生成参数声明的字符串表示
     */
    private String generateParameterDeclaration() {
        if (x.equals("0")) {
            return "para int " + z;
        } else if (x.equals("1")) {
            return "para int " + z + "[]";
        } else {
            return "para int " + z + "[][" + y + "]";
        }
    }

    /**
     * 中间代码操作类型枚举
     * 定义了编译器支持的所有中间代码操作
     */
    public enum operation {
        MAIN, // 程序主入口标识
        PLUSOP, // 加法运算
        MINUOP, // 减法运算
        MULTOP, // 乘法运算
        DIVOP, // 除法运算
        MODOP, // 取模运算
        LSSOP, // 小于比较
        LEQOP, // 小于等于比较
        GREOP, // 大于比较
        GEQOP, // 大于等于比较
        EQLOP, // 等于比较
        NEQOP, // 不等于比较
        ASSIGNOP, // 赋值操作
        GOTO, // 无条件跳转指令
        Jump, // 跳转目标标记
        BZ, // 条件为假时跳转
        BNZ, // 条件为真时跳转
        PUSH, // 函数调用参数压栈
        CALL, // 函数调用指令
        RET, // 函数返回指令
        RETVALUE, // 返回值处理指令
        SCAN, // 输入操作指令
        PRINT, // 输出操作指令
        LABEL, // 标号定义指令
        CONST, // 常量定义指令
        ARRAY, // 数组定义指令
        VAR, // 变量定义指令
        FUNC, // 函数定义指令
        PARAM, // 函数参数定义指令
        GETARRAY, // 数组元素读取指令
        PUTARRAY, // 数组元素赋值指令
        EXIT, // 程序退出标识
        DEBUG, // 调试信息指令
        SLL, // 位左移运算
        SRA, // 位右移运算
    }
}
