package AST;

import Symbol_table.IntergerTable;
import Symbol_table.Symbols.ArraySymbol;
import Symbol_table.Symbols.VarSymbol;
import Word.Word;

/**
 * 标识符节点类 - 代表程序中的变量名和常量名
 * 
 * 当我们在代码中写下一个变量名时，比如 "count" 或 "maxValue"，
 * 编译器就会创建一个Id节点来表示它。这个节点负责：
 * 1. 在符号表中查找这个名字对应的值
 * 2. 判断它是否是可以在编译期计算的常量
 * 3. 如果是常量，就替换成具体的数值
 */
public class Id extends Lval {

    /**
     * 创建一个标识符节点
     * 
     * @param identifierToken 包含标识符名称的词法单元
     */
    public Id(Word identifierToken) {
        super(identifierToken);
    }

    /**
     * 计算标识符的实际数值
     * 这个方法会在符号表中层层查找，直到找到这个标识符的定义
     * 就像在字典里查单词一样，从当前作用域开始，一层层往外找
     */
    @Override
    public int calculate() {
        // 从当前符号表开始查找
        IntergerTable currentScope = inttable;
        VarSymbol foundSymbol = null;

        // 在符号表的作用域链中查找标识符
        while (currentScope != null) {
            if (currentScope.contains(op.getContent())) {
                foundSymbol = (VarSymbol) currentScope.get(op.getContent());
                break;
            }
            currentScope = currentScope.getOut();
        }

        return foundSymbol.getValue();
    }

    /**
     * 尝试将标识符化简为常量
     * 如果这个标识符指向一个编译期已知的常量，就把它替换成Constant节点
     * 这样可以优化代码，减少运行时的查表操作
     */
    @Override
    public Expr reduce() {
        if (isvalue) {
            return new Constant(new Word(String.valueOf(value)));
        }
        return this;
    }

    /**
     * 检查这个标识符是否可以在编译期计算出值
     * 只有常量才能在编译期计算，普通变量需要等到运行时才知道值
     */
    @Override
    public boolean canculculate() {
        IntergerTable currentScope = inttable;
        VarSymbol foundSymbol = null;

        // 在符号表链中搜索标识符定义
        while (currentScope != null) {
            if (currentScope.contains(op.getContent())) {
                // 数组不能直接计算值，需要下标访问
                if (currentScope.get(op.getContent()) instanceof ArraySymbol)
                    return false;
                foundSymbol = (VarSymbol) currentScope.get(op.getContent());
                break;
            }
            currentScope = currentScope.getOut();
        }

        // 如果找到了常量定义，就可以预先计算出值
        if (foundSymbol != null && foundSymbol.isConst()) {
            isvalue = true;
            value = foundSymbol.getValue();
            return true;
        }
        return false;
    }
}
