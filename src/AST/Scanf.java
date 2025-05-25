package AST;

import Midcode.midCode;

/**
 * 输入语句节点 - 编译器中的"信息接收站"
 * 
 * Scanf语句就像程序的信息接收站，负责从外界获取数据并存储到程序中。
 * 它是与用户交互的重要桥梁，就像邮局的收件窗口，
 * 接收外界传来的"包裹"(数据)并放到指定的"收件箱"(变量)中。
 * 
 * 支持的输入目标：
 * 1. 普通变量：scanf("%d", &x); (直接收到变量仓库)
 * 2. 数组元素：scanf("%d", &arr[i]); (需要找到正确的仓库位置)
 * 
 * 实现时的特殊考虑：
 * - 普通变量：可以直接接收数据
 * - 数组元素：需要先接收到临时存储，再转移到目标位置
 */
public class Scanf extends Stmt {
    // 输入目标 - 接收数据的"收件地址"
    Lval inputTarget;

    /**
     * 构造一个输入语句节点
     * 
     * @param inputTarget 接收输入数据的左值表达式
     */
    public Scanf(Lval inputTarget) {
        this.inputTarget = inputTarget;
    }

    /**
     * 生成中间代码 - 安排"收件流程"
     * 
     * 这个方法就像邮局分拣员，根据收件地址的类型
     * 选择不同的投递方式：
     * - 普通地址：直接投递到门口信箱
     * - 复杂地址：先收到代收点，再转送到具体位置
     */
    @Override
    public void gen() {
        if (inputTarget instanceof Id) {
            // 普通变量输入 - 直接投递模式
            emit(new midCode(midCode.operation.SCAN, inputTarget.reduce().toString()));
        } else {
            // 数组元素输入 - 中转投递模式
            // 先收到临时仓库
            Temp temporaryStorage = new Temp(null);
            emit(new midCode(midCode.operation.SCAN, temporaryStorage.toString()));
            // 再从临时仓库转移到目标位置
            emit(new midCode(midCode.operation.PUTARRAY,
                    inputTarget.getcontent(),
                    inputTarget.toString(),
                    temporaryStorage.toString()));
        }
    }
}
