import Mipscode.Mips;
import Optim.Optimize;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Compiler {

    public static String readFileToString(String filePath) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();
        // 使用 BufferedReader 按行读取，并指定 UTF-8 编码
        try (InputStream fis = new FileInputStream(filePath);
                InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(isr)) {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                contentBuilder.append(currentLine).append("\n"); // 使用双引号表示字符串
            }
        }
        return contentBuilder.toString();
    }

    public static void main(String[] args) {

        final String sourceFilePath = "testfile.txt";
        final String outputFilePath = "output.txt";

        String sourceCode;
        try {
            sourceCode = Compiler.readFileToString(sourceFilePath);
        } catch (IOException e) {
            System.err.println("读取输入文件时出错: " + sourceFilePath);
            e.printStackTrace();
            return; // 文件读取失败，直接退出
        }

        try (PrintStream customOut = new PrintStream(outputFilePath)) {
            System.setOut(customOut);

            // 阶段 1: 词法分析
            Sym lexicalAnalyzer = new Sym(sourceCode);
            // 词法分析后获取单词列表
            // List<Word> words = lexicalAnalyzer.getWords(); // 假设 getWords() 返回 List<Word>

            // 阶段 2: 语法分析与错误检查
            // 此处假设 Parsing_error 构造函数或其方法处理错误并可能在严重错误时退出
            // 如果 Parsing_error 修改了单词列表或有其他副作用，需要注意其调用位置
            Parsing_error syntaxValidator = new Parsing_error(lexicalAnalyzer.getWords());
            // 可以在这里添加一个检查，如果 syntaxValidator 发现了严重错误，则提前终止
            // if (syntaxValidator.hasFatalErrors()) {
            // System.err.println("语法错误导致编译终止。");
            // return;
            // }

            // 阶段 3: AST 构建
            // 使用经过词法分析的单词列表进行AST构建
            Parsing_mid astBuilder = new Parsing_mid(lexicalAnalyzer.getWords());
            astBuilder.CompUnit(); // 执行AST构建的核心逻辑

            // 阶段 4: 中间代码生成
            // 基于构建的AST生成中间代码
            astBuilder.analyse(); // 此方法名可以考虑更改为更具描述性的，如 generateIntermediateCode()
            // List<MidCode> intermediateCodes = astBuilder.getMidCodes(); // 假设
            // getMidCodes() 返回 List<MidCode>

            // 阶段 5: 中间代码优化
            // 对生成的中间代码进行优化
            Optimize codeOptimizer = new Optimize(astBuilder.getMidCodes());
            // List<MidCode> optimizedCodes = codeOptimizer.getNewmidCodes(); // 假设
            // getNewmidCodes() 返回优化后的代码

            // 阶段 6: 目标代码生成 (MIPS)
            // 基于优化后的中间代码和字符串表生成MIPS目标代码
            Mips mipsCodeGenerator = new Mips(codeOptimizer.getNewmidCodes(), astBuilder.getStrings());
            // mipsCodeGenerator.generate(); // 假设有一个明确的生成方法，或者其构造函数已完成所有工作

            System.err.println("编译成功完成，输出已写入: " + outputFilePath);

        } catch (FileNotFoundException e) {
            System.err.println("创建输出文件时出错: " + outputFilePath);
            e.printStackTrace();
        } catch (Exception e) { // 捕获其他潜在的编译阶段异常
            System.err.println("编译过程中发生未预料的错误:");
            e.printStackTrace();
        }
    }
}
