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
                contentBuilder.append(currentLine).append("\n"); 
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
            return; 
        }
        try (PrintStream customOut = new PrintStream(outputFilePath)) {
            System.setOut(customOut);

            Sym lexicalAnalyzer = new Sym(sourceCode);

            Parsing_error syntaxValidator = new Parsing_error(lexicalAnalyzer.getWords());

            Parsing_mid astBuilder = new Parsing_mid(lexicalAnalyzer.getWords());
            astBuilder.CompUnit();

            astBuilder.analyse();

            Optimize codeOptimizer = new Optimize(astBuilder.getMidCodes());

            Mips mipsCodeGenerator = new Mips(codeOptimizer.getNewmidCodes(), astBuilder.getStrings());

            System.err.println("编译成功完成，输出已写入: " + outputFilePath);

        } catch (FileNotFoundException e) {
            System.err.println("创建输出文件时出错: " + outputFilePath);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("编译过程中发生未预料的错误:");
            ;
            e.printStackTrace();
        }
    }
}
