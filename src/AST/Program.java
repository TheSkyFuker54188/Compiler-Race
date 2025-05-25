package AST;

import java.util.LinkedList;

/**
 * 程序根节点 - 编译器中的"总指挥部"
 * 
 * Program是整个程序的最高级节点，就像一个公司的总指挥部，
 * 统筹管理整个程序的所有组成部分。它负责协调全局声明和函数定义，
 * 确保程序的各个部分能够有序地编译和执行。
 * 
 * 程序的基本结构：
 * 1. 全局声明部分：全局变量、常量的定义(像公司的基础设施)
 * 2. 函数定义部分：各种功能模块的实现(像公司的各个部门)
 * 
 * 编译顺序很重要：
 * - 先处理全局声明，建立全局符号表
 * - 再处理函数定义，逐个编译各个功能模块
 * 
 * 这个顺序就像建房子：先打地基(全局声明)，再盖楼层(函数定义)。
 */
public class Program extends Node {
    // 全局声明列表 - 程序的"基础设施"部分
    LinkedList<Decl> globalDeclarations = new LinkedList<>();
    // 函数定义列表 - 程序的"功能模块"部分
    LinkedList<Func> functionDefinitions = new LinkedList<>();

    /**
     * 构造一个程序根节点
     * 
     * @param globalDeclarations  全局声明列表
     * @param functionDefinitions 函数定义列表
     */
    public Program(LinkedList<Decl> globalDeclarations, LinkedList<Func> functionDefinitions) {
        this.globalDeclarations = globalDeclarations;
        this.functionDefinitions = functionDefinitions;
    }

    /**
     * 生成中间代码 - 启动整个程序的编译流程
     * 
     * 这个方法就像总指挥官发布作战命令：
     * 1. 先让后勤部门建立基础设施(编译全局声明)
     * 2. 再让各个作战部队执行任务(编译各个函数)
     * 
     * 严格按照这个顺序执行，确保编译过程的正确性。
     */
    public void gen() {
        // 第一阶段：编译全局声明 - 建立程序的基础环境
        for (Decl globalDeclaration : globalDeclarations) {
            globalDeclaration.gen();
        }

        // 第二阶段：编译函数定义 - 实现程序的具体功能
        for (Func functionDefinition : functionDefinitions) {
            functionDefinition.gen();
        }
    }
}
