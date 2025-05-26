package AST;

import Midcode.midCode;
import Symbol_table.IntergerTable;

import java.util.ArrayList;

/**
 * 代码块节点 - 编译器中的"作业车间"
 * 
 * 代码块就像工厂里的一个独立作业车间，有明确的边界和独立的工作环境。
 * 车间内的工具(局部变量)只能在车间内使用，车间外看不到也用不了。
 * 这种设计确保了不同车间之间不会相互干扰。
 * 
 * 代码块的特点：
 * 1. 作用域隔离：{}内定义的变量只在块内有效
 * 2. 嵌套结构：可以在一个车间内建立子车间
 * 3. 资源管理：车间结束时自动清理局部资源
 * 
 * 两种生成模式：
 * - 普通代码块：有独立的开始和结束标签
 * - 函数体代码块：复用函数的入口标签，在结束时添加默认返回
 */
public class Block extends Stmt {
    // 全局计数器 - 为每个代码块分配唯一的车间号
    static int globalBlockCounter = 0;
    // 代码块项目列表 - 车间内的具体工作内容
    ArrayList<BlockItem> blockItems;
    // 当前代码块编号 - 这个车间的门牌号
    int blockNumber;

    /**
     * 构造一个代码块节点
     * 
     * @param blockItems 代码块中的语句和声明列表
     */
    public Block(ArrayList<BlockItem> blockItems) {
        this.blockItems = blockItems;
    }

    /**
     * 获取下一个代码块编号
     * 
     * @return 新的代码块编号
     */
    public static int getCount() {
        return ++globalBlockCounter;
    }

    /**
     * 生成中间代码 - 建立独立的"作业车间"
     * 
     * 这个方法就像建立一个完整的独立车间：
     * 1. 建立新的工作环境(创建作用域)
     * 2. 安装车间门牌(设置开始标签)
     * 3. 执行车间作业(编译内部语句)
     * 4. 设置车间出口(设置结束标签)
     * 5. 清理工作环境(恢复外层作用域)
     */
    @Override
    public void gen() {
        // 建立新的工作环境 - 创建块级作用域
        inttable = new IntergerTable(inttable);

        // 分配车间号并设置入口
        blockNumber = ++globalBlockCounter;
        emit(new midCode(midCode.operation.LABEL, String.valueOf(blockNumber), "start"));

        // 运行车间内的所有工作项目
        for (BlockItem item : blockItems) {
            item.gen();
        }

        // 设置车间出口
        emit(new midCode(midCode.operation.LABEL, String.valueOf(blockNumber), "end"));

        // 清理工作环境 - 恢复到外层作用域
        inttable = inttable.getOut();
    }

    /**
     * 生成函数体代码 - 特殊的"主车间"模式
     * 
     * 函数体代码块不需要独立的入口标签(复用函数入口)，
     * 但需要在结束时添加默认的返回语句。
     * 
     * @param functionBlockNumber 函数的代码块编号
     */
    public void gen(int functionBlockNumber) {
        blockNumber = functionBlockNumber;

        // 运行函数体内的所有工作项目
        for (BlockItem item : blockItems) {
            item.gen();
        }

        // 函数结束时添加默认返回(防止函数意外"掉出"结尾)
        emit(new midCode(midCode.operation.RET, null));

        // 设置函数出口标签
        emit(new midCode(midCode.operation.LABEL, String.valueOf(blockNumber), "end"));

        // 清理函数作用域
        inttable = inttable.getOut();
    }
}
