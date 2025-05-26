package AST;

import Midcode.midCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 逻辑或表达式节点类 - 处理 || 运算符
 * 
 * 逻辑或也有短路求值特性：如果任何一个条件为true，
 * 整个表达式就是true，不需要计算后面的条件。
 * 
 * 就像找停车位：找到一个空位就可以停了，
 * 不用把整个停车场都看完。
 */
public class Or extends Node {
    /* 组成逻辑或的And表达式列表 */
    List<And> andConditions = new ArrayList<>();

    /* 成功跳转标签编号 */
    private int successJumpLabel;

    /**
     * 创建逻辑或表达式节点
     * 
     * @param andConditions 组成逻辑或的And表达式列表
     */
    public Or(ArrayList<And> andConditions) {
        this.andConditions = andConditions;
    }

    /**
     * 生成逻辑或的短路求值代码（普通版本）
     * 
     * 逻辑：任何一个And条件成功，就跳转到成功标签；
     * 所有And条件都失败，才跳转到失败标签
     * 
     * @param failureLabel 所有条件都为false时跳转的标签
     */
    public void gen(int failureLabel) {
        successJumpLabel = ++jumps;

        // 为每个And条件生成代码，成功就跳到成功标签
        for (And andCondition : andConditions) {
            andCondition.gen(successJumpLabel);
        }

        // 所有And条件都失败，跳转到失败标签
        emit(new midCode(midCode.operation.GOTO, "Jump" + failureLabel));

        // 设置成功标签位置
        emit(new midCode(midCode.operation.Jump, "Jump" + successJumpLabel));
    }

    /**
     * 生成逻辑或的短路求值代码（循环版本）
     * 
     * 这个版本专门用于while循环的条件判断，
     * 失败时不是跳到普通标签，而是跳到循环结束标签
     * 
     * @param loopId      循环的标识编号
     * @param isWhileLoop 标识这是while循环的标志
     */
    public void gen(int loopId, boolean isWhileLoop) {
        successJumpLabel = ++jumps;

        // 为每个And条件生成代码
        for (And andCondition : andConditions) {
            andCondition.gen(successJumpLabel);
        }

        // 所有条件都失败，跳出while循环
        emit(new midCode(midCode.operation.GOTO, "Loop" + String.valueOf(loopId) + "end"));

        // 设置成功标签位置（继续循环体运行）
        emit(new midCode(midCode.operation.Jump, "Jump" + successJumpLabel));
    }
}
