package AST;

import Midcode.midCode;
import Word.Word;

import java.util.ArrayList;

/**
 * 函数调用表达式节点 - 编译器中的"外卖订餐员"
 * 
 * 函数调用就像在餐厅点外卖：你提供订单(参数)，餐厅处理后
 * 给你送来食物(返回值)。这个过程需要严格的协议来确保：
 * 1. 订单信息正确传达(参数传递)
 * 2. 餐厅能够理解并处理(函数存在且签名匹配)
 * 3. 食物能够正确送达(返回值处理)
 * 
 * 函数调用的执行流程：
 * 1. 准备参数(计算实际参数的值)
 * 2. 参数入栈(按照调用约定传递参数)
 * 3. 跳转到函数(转移控制权)
 * 4. 接收返回值(如果函数有返回值)
 * 
 * 需要区分void函数(不返回值)和int函数(返回整数值)的处理。
 */
public class FuncR extends Expr {
    // 实际参数列表 - 调用时传递的"订单清单"
    ArrayList<Expr> actualParameters;
    // 处理后的参数列表 - 准备好的"标准化订单"
    ArrayList<Expr> processedParameters = new ArrayList<>();

    /**
     * 构造一个函数调用表达式节点
     * 
     * @param functionToken    函数名词法符号
     * @param actualParameters 实际参数列表
     */
    public FuncR(Word functionToken, ArrayList<Expr> actualParameters) {
        super(functionToken);
        this.actualParameters = actualParameters;
    }

    /**
     * 化简表达式 - 执行"点餐和取餐"的完整流程
     * 
     * 这个方法就像外卖订餐的完整过程：
     * 1. 确认订单内容(计算参数值)
     * 2. 提交订单(参数入栈)
     * 3. 等待制作(函数调用)
     * 4. 接收外卖(获取返回值)
     */
    @Override
    public Expr reduce() {
        // 第一步：准备订单 - 计算所有实际参数的值
        for (Expr parameter : actualParameters) {
            parameter.canculculate();
        }

        // 第二步：标准化订单 - 获取参数的最终形式
        for (Expr parameter : actualParameters) {
            processedParameters.add(parameter.reduce());
        }

        // 第三步：提交订单 - 将参数按顺序压入栈中
        for (Expr processedParameter : processedParameters) {
            if (processedParameter instanceof Array) {
                // 数组参数需要特殊处理 - 传递数组的基地址和索引信息
                Array arrayParam = (Array) processedParameter;
                emit(new midCode(midCode.operation.PUSH,
                        processedParameter.op.getContent(),
                        arrayParam.temporaryStorage.toString(),
                        arrayParam.secondDimensionInfo));
            } else {
                // 普通参数直接传递值
                emit(new midCode(midCode.operation.PUSH, processedParameter.toString()));
            }
        }

        // 第四步：发起调用 - 跳转到目标函数
        emit(new midCode(midCode.operation.CALL, op.getContent()));

        // 第五步：处理返回值 - 根据函数类型决定是否接收返回值
        int functionReturnType = funcTable.get(op.getContent()).getReturntype();
        if (functionReturnType == 0) {
            // void函数：没有返回值，直接返回调用表达式本身
            return this;
        } else {
            // int函数：有返回值，创建临时变量接收
            Temp returnValueHolder = new Temp(op);
            emit(new midCode(midCode.operation.RETVALUE, returnValueHolder.toString()));
            return returnValueHolder;
        }
    }

    /**
     * 生成中间代码 - 简化的调用接口
     */
    @Override
    public void gen() {
        reduce();
    }

    /**
     * 转换为字符串表示
     */
    @Override
    public String toString() {
        return "RET";
    }
}
