package AST;

import Midcode.midCode;
import Symbol_table.IntergerTable;
import Symbol_table.Symbols.FuncSymbol;

import java.util.ArrayList;

/**
 * 函数定义节点 - 编译器中的"功能车间"
 * 
 * 函数就像工厂里的一个专门车间，有特定的输入(参数)、处理流程(函数体)
 * 和输出(返回值)。每个函数都是一个独立的功能单元，可以被其他地方调用。
 * 
 * 函数的组成要素：
 * 1. 返回类型：void(无返回值) 或 int(返回整数)
 * 2. 函数名：函数的唯一标识符
 * 3. 参数列表：函数的输入接口
 * 4. 函数体：具体的实现逻辑
 * 
 * 特殊的main函数：
 * - 程序的入口点，像工厂的总开关
 * - 不需要显式调用，程序启动时自动执行
 * - 必须是程序中唯一的main函数
 */
public class Func extends Node {
    // 函数返回类型 - 0:void(无返回值) 1:int(返回整数)
    int returnType;
    // 函数名标识符
    Id functionName;
    // 形式参数列表 - 函数的输入接口
    ArrayList<Fparam> formalParameters;
    // 函数体代码块 - 函数的具体实现
    Block functionBody;
    // 是否为主函数标记
    Boolean isMainFunction = false;

    /**
     * 构造一个普通函数定义节点
     * 
     * @param returnType       返回类型(0:void, 1:int)
     * @param functionName     函数名
     * @param formalParameters 形式参数列表
     * @param functionBody     函数体
     */
    public Func(int returnType, Id functionName, ArrayList<Fparam> formalParameters, Block functionBody) {
        this.returnType = returnType;
        this.functionName = functionName;
        this.formalParameters = formalParameters;
        this.functionBody = functionBody;
    }

    /**
     * 构造一个函数定义节点(可指定是否为main函数)
     * 
     * @param returnType       返回类型
     * @param functionName     函数名
     * @param formalParameters 形式参数列表
     * @param functionBody     函数体
     * @param isMainFunction   是否为main函数
     */
    public Func(int returnType, Id functionName, ArrayList<Fparam> formalParameters, Block functionBody,
            Boolean isMainFunction) {
        this.returnType = returnType;
        this.functionName = functionName;
        this.formalParameters = formalParameters;
        this.functionBody = functionBody;
        this.isMainFunction = isMainFunction;
    }

    /**
     * 生成中间代码 - 建造"功能车间"
     * 
     * 这个方法就像建设一个专门的生产车间：
     * 1. 注册函数到符号表(在工厂目录中登记车间)
     * 2. 创建函数作用域(设立车间的独立工作环境)
     * 3. 设置函数入口标签(安装车间门牌)
     * 4. 处理参数(安装输入传送带)
     * 5. 编译函数体(建设具体的生产线)
     */
    @Override
    public void gen() {
        // 将函数注册到符号表(除了main函数，它是特殊的入口点)
        if (!isMainFunction) {
            funcTable.add(functionName.getcontent(), new FuncSymbol(returnType));
        }

        String typeString = returnType == 0 ? "void" : "int";
        // 创建新的作用域 - 为函数建造独立的工作环境
        inttable = new IntergerTable(inttable);

        // 获取函数块标识符并设置入口标签
        int blockIdentifier = Block.getCount();
        emit(new midCode(midCode.operation.LABEL, String.valueOf(blockIdentifier), "start"));

        if (isMainFunction) {
            // 处理主函数 - 程序的总开关
            emit(new midCode(midCode.operation.MAIN, "main"));
        } else {
            // 处理普通函数 - 专门的功能车间
            emit(new midCode(midCode.operation.FUNC, functionName.getcontent(), typeString));
            // 处理形式参数 - 安装输入接口
            for (Fparam parameter : formalParameters) {
                parameter.gen();
            }
        }

        // 编译函数体 - 建设具体的生产线
        functionBody.gen(blockIdentifier);
    }
}
