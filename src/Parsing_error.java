import Symbol_table.FuncTable;
import Symbol_table.IntergerTable;
import Symbol_table.Symbols.ArraySymbol;
import Symbol_table.Symbols.FuncSymbol;
import Symbol_table.Symbols.NorSymbol;
import Symbol_table.Symbols.VarSymbol;
import Word.FormatWord;
import Word.Word;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List; // Changed from ArrayList to List for interface usage

public class Parsing_error { // Consider renaming to SyntaxErrorAnalyzer or similar

    private final List<Word> tokenStream; // Renamed from 'words'
    private int currentTokenIndex = 0; // Renamed from 'index'

    private FuncTable functionSymbolTable = new FuncTable(); // Renamed from 'functable'
    private IntergerTable variableSymbolTable = new IntergerTable(); // Renamed from 'inttable'

    // Consider a dedicated ErrorInfo class instead of int[][]
    private static class ErrorInfo {
        final int lineNumber;
        final int errorCode;

        ErrorInfo(int line, int code) {
            this.lineNumber = line;
            this.errorCode = code;
        }
    }

    private final List<ErrorInfo> detectedErrors = new ArrayList<>(); // Replaces errorarray and errorcount

    private int loopNestingLevel = 0; // Renamed from 'CircleLevel'
    private int currentFunctionReturnType = 0; // 0 for void, 1 for int. Renamed from 'functype'
    private boolean withinConditionalConstruct = false; // Renamed from 'inCond'
    private boolean isLastStatementReturn = false; // Renamed from 'isLast'
    // private boolean hasvoid = false; // This variable was not used, consider
    // removing if still unused.
    private ArrayList<Integer> dimensionTracker = new ArrayList<>(); // Renamed from 'maxarray'
    private int functionDepth = 0; // Renamed from 'intofunc'

    public Parsing_error(ArrayList<Word> tokenSequence) { // Parameter name changed
        this.tokenStream = tokenSequence;
        this.dimensionTracker.add(0); // Initialize dimension tracker
        performSyntaxAnalysis(); // Renamed from 'analyse'

        if (!detectedErrors.isEmpty()) {
            System.out.println("Syntax errors detected in your code:\nBelow are the identified issues:");
            reportErrors(); // Renamed from 'errorout'
            System.exit(1); // Exit if errors are present
        }
    }

    // Renamed from analyse()
    public void performSyntaxAnalysis() {
        parseCompilationUnit(); // Renamed from CompUnit()
    }

    // Renamed from getWord()
    private Word consumeToken() {
        if (currentTokenIndex < tokenStream.size()) {
            return tokenStream.get(currentTokenIndex++);
        }
        // Return a special 'EndOfFile' token or handle error, instead of a generic new
        // Word()
        return new Word(); // Placeholder, ideally an EOF token or specific error handling
    }

    // Renamed from showWord()
    private Word peekNextToken() {
        if (currentTokenIndex < tokenStream.size()) {
            return tokenStream.get(currentTokenIndex);
        }
        return new Word(); // Placeholder, EOF token or error handling
    }

    // Renamed from showWord(int num)
    private Word peekTokenAt(int lookaheadIndex) {
        if (lookaheadIndex < tokenStream.size()) {
            return tokenStream.get(lookaheadIndex);
        }
        return new Word(); // Placeholder, EOF token or error handling
    }

    // Renamed from NextIsExp(Word w)
    // This method's logic might need adjustment based on how Word objects are
    // structured
    // Assuming Word has methods like isIdentifier(), isIntegerConstant(), etc.
    private boolean isNextTokenExpressionStart(Word token) {
        String content = token.getContent();
        int symbolNumber = token.getSymnumber(); // Assuming getSymnumber() maps to token types

        // Example of more direct checks if Word class supports them:
        // if (token.isOperator("(") || token.isOperator("+") || token.isOperator("-")
        // || token.isOperator("!")) {
        // return true;
        // }
        // if (token.isIdentifier() || token.isIntegerConstant()) {
        // return true;
        // }

        if ("(".equals(content) || "+".equals(content) || "-".equals(content) || "!".equals(content)) {
            return true;
        }
        // Assuming symbolNumber 1 is IDENFR and 2 is INTCON based on original comments
        return symbolNumber == 1 || symbolNumber == 2;
    }

    // This method seems to be for debugging, can be kept or removed
    private void indicateSyntaxErrorPosition() {
        System.out.print("---------------SYNTAX ERROR DETECTED-------------");
    }

    // Renamed from errordeal(int type, int line)
    private void recordError(int errorCode, int lineNumber) {
        detectedErrors.add(new ErrorInfo(lineNumber, errorCode));
    }

    // Renamed from errorout()
    private void reportErrors() {
        // Sort errors by line number before printing
        detectedErrors.sort(Comparator.comparingInt(e -> e.lineNumber));

        for (ErrorInfo error : detectedErrors) {
            // The original code used (char) ('a' + error.errorCode). Ensure this mapping is
            // still desired.
            System.out.println(error.lineNumber + " " + (char) ('a' + error.errorCode));
        }
    }

    // Renamed from CompUnit()
    private void parseCompilationUnit() {
        // Original logic: while (!showWord(index + 2).getContent().equals("(")) Decl();
        // This peeks three tokens ahead. Let's make it clearer.
        while (currentTokenIndex + 2 < tokenStream.size() &&
                !peekTokenAt(currentTokenIndex + 2).getContent().equals("(")) {
            parseDeclaration(); // Renamed from Decl()
        }

        // Original logic: while (!showWord(index + 1).getContent().equals("main"))
        // FuncDef();
        while (currentTokenIndex + 1 < tokenStream.size() &&
                !peekTokenAt(currentTokenIndex + 1).getContent().equals("main")) {
            parseFunctionDefinition(); // Renamed from FuncDef()
        }
        parseMainFunctionDefinition(); // Renamed from MainFuncDef()
    }

    private void parseDeclaration() {
        if (peekNextToken().getContent().equals("const")) {
            parseConstantDeclaration();
        } else {
            parseVariableDeclaration();
        }
        // System.out.print("<Decl>\n");
    }

    private void parseConstantDeclaration() {
        NorSymbol sym = new NorSymbol();
        if (consumeToken().getContent().equals("const")) {
            parseBaseType();
            sym = parseConstantDefinition();
            while (peekNextToken().getContent().equals(",")) {
                consumeToken();
                sym = parseConstantDefinition();
            }
            if (peekNextToken().getContent().equals(";")) {
                consumeToken();
            } else {
                recordError(8, sym.getLine()); // i类错误
            }
        } else {
            indicateSyntaxErrorPosition();
        }
        // System.out.print("<ConstDecl>\n");
    }

    private void parseBaseType() {
        if (!consumeToken().getContent().equals("int")) {
            indicateSyntaxErrorPosition();
        }
        // System.out.print("<BType>\n");
    }

    private NorSymbol parseConstantDefinition() { // 暂时不考虑多维数组每一维的个数
        Word w = consumeToken();
        String name = w.getContent();
        NorSymbol sym = new NorSymbol();
        int line = w.getLine();
        if (w.getSymnumber() == 1) {
            int count = 0;
            while (peekNextToken().getContent().equals("[")) {
                count++;
                Word w1 = consumeToken();
                parseConstantExpression();
                if (peekNextToken().getContent().equals("]")) { // 使用[作为行号标记
                    consumeToken();
                } else {
                    recordError(10, w1.getLine()); // 错误处理k型缺少】
                }
            }
            if (!consumeToken().getContent().equals("=")) {
                indicateSyntaxErrorPosition();
            }
            parseConstantInitializationValue();

            if (variableSymbolTable.contains(name)) { // 错误处理b型名字重定义
                recordError(1, line);
            } else {
                if (count == 0) {
                    sym = new VarSymbol(name, true);
                } else {
                    sym = new ArraySymbol(name, true, count);
                }
                sym.setLine(line);
                variableSymbolTable.add(name, sym);
            }

        } else {
            indicateSyntaxErrorPosition();
        }

        return sym;
        // System.out.print("<ConstDef>\n");
    }

    private void parseConstantInitializationValue() {
        if (peekNextToken().getContent().equals("{")) {
            consumeToken();
            if (peekNextToken().getContent().equals("}")) {
                consumeToken();
            } else {
                parseConstantInitializationValue();
                while (peekNextToken().getContent().equals(",")) {
                    consumeToken();
                    parseConstantInitializationValue();
                }
                if (!consumeToken().getContent().equals("}")) {
                    indicateSyntaxErrorPosition();
                }
            }
        } else {
            parseConstantExpression();
        }
        // System.out.print("<ConstInitVal>\n");
    }

    private void parseVariableDeclaration() {
        NorSymbol sym = new NorSymbol();
        parseBaseType();
        sym = parseVariableDefinition();
        while (peekNextToken().getContent().equals(",")) {
            consumeToken();
            sym = parseVariableDefinition();
        }
        if (peekNextToken().getContent().equals(";")) {
            consumeToken();
        } else {
            recordError(8, sym.getLine()); // i类错误类型
        }
        // System.out.print("<VarDecl>\n");
    }

    private NorSymbol parseVariableDefinition() {
        NorSymbol sym = new NorSymbol();
        Word w = consumeToken();
        String name = w.getContent();
        int line = w.getLine();
        if (w.getSymnumber() == 1) {
            int count = 0;
            while (peekNextToken().getContent().equals("[")) {
                count++;
                Word w1 = consumeToken();
                parseConstantExpression();
                if (peekNextToken().getContent().equals("]")) { // 使用[作为行号标记
                    consumeToken();
                } else {
                    recordError(10, w1.getLine()); // 错误处理k型
                }
            }
            if (peekNextToken().getContent().equals("=")) {
                consumeToken();
                parseInitializationValue();
            }

            if (variableSymbolTable.contains(name)) { // 错误处理b型
                recordError(1, line);
            } else {
                if (count == 0) {
                    sym = new VarSymbol(name, false);
                } else {
                    sym = new ArraySymbol(name, false, count);
                }
                sym.setLine(line);
                variableSymbolTable.add(name, sym);
            }

        } else {
            indicateSyntaxErrorPosition();
        }
        return sym;
        // System.out.print("<VarDef>\n");
    }

    private void parseInitializationValue() {
        if (peekNextToken().getContent().equals("{")) {
            consumeToken();
            if (peekNextToken().getContent().equals("}")) {
                consumeToken();
            } else {
                parseInitializationValue();
                while (peekNextToken().getContent().equals(",")) {
                    consumeToken();
                    parseInitializationValue();
                }
                if (!consumeToken().getContent().equals("}")) {
                    indicateSyntaxErrorPosition();
                }
            }
        } else {
            parseExpression();
        }
        // System.out.print("<InitVal>\n");
    }

    private void parseFunctionDefinition() {

        IntergerTable newtable = new IntergerTable(); // 进入函数创建一个新的作用域
        newtable.setOut(variableSymbolTable);
        variableSymbolTable = newtable;

        int type = parseFunctionType();
        currentFunctionReturnType = type;
        Word w = consumeToken();
        String name = w.getContent();
        int line = w.getLine();
        ArrayList<NorSymbol> list = new ArrayList<>();
        if (w.getSymnumber() != 1) {
            indicateSyntaxErrorPosition();
        }
        Word w1 = consumeToken(); // "("

        if (peekNextToken().getContent().equals(")")) {
            consumeToken();
        } else if (peekNextToken().getContent().equals("{")) {
            recordError(9, w1.getLine()); // j类错误缺少）
        } else {
            list = parseFunctionParameters();
            if (peekNextToken().getContent().equals(")")) {
                consumeToken();
            } else {
                recordError(9, w1.getLine()); // j类错误缺少）
            }
        }

        if (functionSymbolTable.contains(name)) { // 错误处理b型未定义
            recordError(1, line);
        } else {
            FuncSymbol sym = new FuncSymbol(name, list, type); // 函数定义中增加声明
            functionSymbolTable.add(name, sym);
        }

        functionDepth = 1;
        Word w2 = parseBlock();
        if (type == 1 && !isLastStatementReturn)
            recordError(6, w2.getLine()); // g类错误缺少最后return语句

        // System.out.print("<FuncDef>\n");
    }

    private void parseMainFunctionDefinition() {// 在预判的时候就会确定符合要求，不进行每个的特殊处理

        consumeToken();
        consumeToken();
        Word w = consumeToken();
        if (peekNextToken().getContent().equals(")")) {
            consumeToken();
        } else {
            recordError(9, w.getLine());
        }
        currentFunctionReturnType = 1;
        Word w1 = parseBlock();
        if (!isLastStatementReturn)
            recordError(6, w1.getLine()); // g类错误
        // System.out.print("<MainFuncDef>\n");
    }

    private int parseFunctionType() {
        String s = consumeToken().getContent();
        if (s.equals("void"))
            return 0;
        else if (s.equals("int"))
            return 1;
        else
            return -1;
        // System.out.print("<FuncType>\n");
    }

    private ArrayList<NorSymbol> parseFunctionParameters() {
        ArrayList<NorSymbol> list = new ArrayList<>();
        list.add(parseFunctionParameter());
        while (peekNextToken().getContent().equals(",")) {
            consumeToken();
            list.add(parseFunctionParameter());
        }
        return list;
        // System.out.print("<FuncFParams>\n");
    }

    private NorSymbol parseFunctionParameter() {
        parseBaseType();
        Word w = consumeToken();
        String name = w.getContent();
        int line = w.getLine();
        int count = 0;
        if (w.getSymnumber() != 1)
            indicateSyntaxErrorPosition();
        if (peekNextToken().getContent().equals("[")) {
            count++;
            Word w1 = consumeToken();

            if (peekNextToken().getContent().equals("]")) {
                consumeToken();
            } else {
                recordError(10, w1.getLine()); // k型错误
            }

            while (peekNextToken().getContent().equals("[")) {
                count++;
                w1 = consumeToken();
                parseConstantExpression();
                if (peekNextToken().getContent().equals("]")) {
                    consumeToken();
                } else {
                    recordError(10, w1.getLine()); // k型错误
                }
            }
        }
        if (variableSymbolTable.contains(name)) { // 错误处理b型
            recordError(1, line);
        }
        if (count == 0) {
            NorSymbol sym = new VarSymbol(name, false);
            variableSymbolTable.add(name, sym);
            return sym;
        } else {
            NorSymbol sym = new ArraySymbol(name, false, count);
            variableSymbolTable.add(name, sym);
            return sym;
        }
        // System.out.print("<FuncFParam>\n");
    }

    private Word parseBlock() {
        consumeToken();
        Word w;
        if (peekNextToken().getContent().equals("}")) {
            isLastStatementReturn = false;
            w = consumeToken();
            if (functionDepth == 1) {
                functionDepth = 0;
                variableSymbolTable = variableSymbolTable.getOut();
            }
        } else {
            if (functionDepth == 1) {
                functionDepth = 0;
            } else {
                IntergerTable newtable = new IntergerTable(); // 进入一个新的作用域时，创建新的符号表
                newtable.setOut(variableSymbolTable);
                variableSymbolTable = newtable;
            }
            isLastStatementReturn = false;
            parseBlockItem();
            while (!peekNextToken().getContent().equals("}")) {
                isLastStatementReturn = false;
                parseBlockItem();
            }
            w = consumeToken();
            variableSymbolTable = variableSymbolTable.getOut();
        }
        return w;
        // System.out.print("<Block>\n");
    }

    private void parseBlockItem() {
        if (peekNextToken().getContent().equals("int") || peekNextToken().getContent().equals("const")) {
            parseDeclaration();
        } else {
            parseStatement();
        }
        // System.out.print("<BlockItem>\n");
    }

    private void parseStatement() {
        if (peekNextToken().getContent().equals("if")) {
            withinConditionalConstruct = true;
            Word w = consumeToken();
            int line = w.getLine();
            if (!consumeToken().getContent().equals("("))
                indicateSyntaxErrorPosition();
            parseCondition();
            if (peekNextToken().getContent().equals(")")) {
                consumeToken();
            } else {
                recordError(9, line); // j kind error
            }
            parseStatement();
            if (peekNextToken().getContent().equals("else")) {
                consumeToken();
                parseStatement();
            }
            withinConditionalConstruct = false;
        } else if (peekNextToken().getContent().equals("{")) {
            withinConditionalConstruct = true;
            parseBlock();
            withinConditionalConstruct = false;
        } else if (peekNextToken().getContent().equals("while")) {
            withinConditionalConstruct = true;
            Word w = consumeToken();
            int line = w.getLine();
            if (!consumeToken().getContent().equals("("))
                indicateSyntaxErrorPosition();
            parseCondition();
            if (peekNextToken().getContent().equals(")")) {
                consumeToken();
            } else {
                recordError(9, line); // j kind error
            }
            loopNestingLevel++; // 循环层次增加
            parseStatement();
            loopNestingLevel--;
            withinConditionalConstruct = false;
        } else if (peekNextToken().getContent().equals("break") || peekNextToken().getContent().equals("continue")) {
            Word w = consumeToken();

            if (loopNestingLevel == 0) // m型错误
                recordError(12, w.getLine());

            if (peekNextToken().getContent().equals(";")) {
                consumeToken();
            } else {
                recordError(8, w.getLine()); // i类错误
            }

        } else if (peekNextToken().getContent().equals("return")) {
            if (withinConditionalConstruct == false)
                isLastStatementReturn = true;
            Word w = consumeToken();
            int line = w.getLine();
            // if(showWord().getContent().equals(";")){
            // getWord();
            // }else{
            // Exp();
            // if(!getWord().getContent().equals(";"))
            // error();
            // }
            if (isNextTokenExpressionStart(peekNextToken())) {
                parseExpression();
                if (currentFunctionReturnType == 0)
                    recordError(5, line); // f型错误
                if (peekNextToken().getContent().equals(";")) {
                    consumeToken();
                } else {
                    recordError(8, line); // i kind error
                }
            } else {
                if (peekNextToken().getContent().equals(";")) {
                    consumeToken();
                } else {
                    recordError(8, line); // i kind error
                }
            }
        } else if (peekNextToken().getContent().equals("printf")) {
            int symnum = 0;
            Word w1 = consumeToken();
            int count1 = w1.getLine();

            if (!consumeToken().getContent().equals("("))
                indicateSyntaxErrorPosition();

            FormatWord w2 = (FormatWord) consumeToken();
            int count2 = w2.getLine();
            boolean correct = w2.isCorrect();
            int num = w2.getNum();

            if (!correct)
                recordError(0, count2); // a类错误

            while (peekNextToken().getContent().equals(",")) {
                symnum++;
                consumeToken();
                parseExpression();
            }

            if (peekNextToken().getContent().equals(")")) {
                consumeToken();
            } else {
                recordError(9, count2); // j kind error
            }
            if (peekNextToken().getContent().equals(";")) {
                consumeToken();
            } else {
                recordError(8, count2); // i kind error
            }
            // if(!getWord().getContent().equals(")"))
            // error();
            // if(!getWord().getContent().equals(";"))
            // error();

            if (num != symnum) // l类错误
                recordError(11, count1);

        } else if (peekNextToken().getContent().equals(";")) {
            consumeToken();
        } else if (peekNextToken().getSymnumber() == 1) {
            int flag1 = 0;
            if (peekTokenAt(currentTokenIndex + 1).getContent().equals("=")) {
                flag1 = 1;
            } else if (peekTokenAt(currentTokenIndex + 1).getContent().equals("(")) {
                flag1 = 2;
            } else if (peekTokenAt(currentTokenIndex + 1).getContent().equals("[")) {
                // int k=index+1; //有可能会出问题就是
                // while (!showWord(k).getContent().equals(";")){
                // if(showWord(k).getContent().equals("=")){
                // break;
                // }
                // k++;
                // }
                // if(showWord(k).getContent().equals("="))
                // flag1=1;
                // else
                // flag1=2;
                int k = currentTokenIndex + 1;
                while (peekTokenAt(k).getContent().equals("[")) {
                    k++;
                    int level = 1;
                    while (level > 0) {
                        if (peekTokenAt(k).getContent().equals("["))
                            level++;
                        else if (peekTokenAt(k).getContent().equals("]"))
                            level--;
                        else if (peekTokenAt(k).getContent().equals("=")) {
                            flag1 = 1;
                            break;
                        } else if (peekTokenAt(k).getContent().equals(";")) {
                            flag1 = 2;
                            break;
                        }
                        k++;
                    }
                    if (flag1 > 0)
                        break;
                }
                if (flag1 == 0) {
                    if (peekTokenAt(k).getContent().equals("="))
                        flag1 = 1;
                    else
                        flag1 = 2;
                }
            } else {
                flag1 = 2;
            }

            if (flag1 == 1) {

                NorSymbol sym = parseLValue();
                if (sym.isConst()) { // h型错误
                    recordError(7, sym.getLine());
                }

                Word w = consumeToken();
                int line = w.getLine();

                if (peekNextToken().getContent().equals("getint")) {
                    consumeToken();
                    if (!consumeToken().getContent().equals("("))
                        indicateSyntaxErrorPosition();
                    if (peekNextToken().getContent().equals(")")) {
                        consumeToken();
                    } else {
                        recordError(9, line); // j kind error
                    }
                    if (peekNextToken().getContent().equals(";")) {
                        consumeToken();
                    } else {
                        recordError(8, line); // i kind error
                    }
                } else {
                    parseExpression();
                    if (peekNextToken().getContent().equals(";")) {
                        consumeToken();
                    } else {
                        recordError(8, line); // i kind error
                    }
                }
            } else {
                Word w = parseExpression();
                if (peekNextToken().getContent().equals(";"))
                    consumeToken();
                else
                    recordError(8, w.getLine());
            }
        } else {
            Word w = parseExpression();
            if (peekNextToken().getContent().equals(";")) {
                consumeToken();
            } else {
                recordError(8, w.getLine()); // i kind error缺;
            }
        }
        // System.out.print("<Stmt>\n");
    }

    private Word parseExpression() {
        Word w = peekNextToken();
        parseAdditiveExpression();
        return w;
        // System.out.print("<Exp>\n");
    }

    private void parseCondition() {
        parseLogicalOrExpression();
        // System.out.print("<Cond>\n");
    }

    private NorSymbol parseLValue() {
        NorSymbol sym = new NorSymbol();
        Word w = consumeToken();
        String name = w.getContent();
        int line = w.getLine();
        int flag = 0;
        if (w.getSymnumber() != 1)
            indicateSyntaxErrorPosition();

        IntergerTable table = variableSymbolTable;
        while (table != null) { // 未定义名字c类错误
            if (table.contains(name)) {
                flag = 1;
                sym = table.get(name);
                break;
            }
            table = table.getOut();
        }

        if (flag == 0)
            recordError(2, line);

        int arrayLevel = sym.getLevel();

        while (peekNextToken().getContent().equals("[")) {
            arrayLevel--;
            Word w1 = consumeToken();
            parseExpression();
            if (peekNextToken().getContent().equals("]")) {
                consumeToken();
            } else {
                recordError(10, w1.getLine()); // k型错误
            }
        }
        dimensionTracker.set(0, Math.max(dimensionTracker.get(0), arrayLevel));
        sym.setLine(line);
        return sym;
        // System.out.print("<LVal>\n");
    }

    private void parsePrimaryExpression() {
        if (peekNextToken().getContent().equals("(")) {
            Word w = consumeToken();
            parseExpression();
            if (peekNextToken().getContent().equals(")")) {
                consumeToken();
            } else {
                recordError(9, w.getLine());
            }
        } else if (peekNextToken().getSymnumber() == 1) {
            parseLValue();
        } else if (peekNextToken().getSymnumber() == 2) {
            parseNumber();
        } else {
            indicateSyntaxErrorPosition();
        }
        // System.out.print("<PrimaryExp>\n");
    }

    private void parseNumber() {
        if (consumeToken().getSymnumber() != 2) {
            indicateSyntaxErrorPosition();
        }
        // System.out.print("<Number>\n");
    }

    private void parseUnaryExpression() {
        String s = peekNextToken().getContent();
        if (peekNextToken().getSymnumber() == 1 && peekTokenAt(currentTokenIndex + 1).getContent().equals("(")) {

            Word w = consumeToken();
            String name = w.getContent();
            int line = w.getLine();
            int flag = 0;
            if (!functionSymbolTable.contains(name)) {
                recordError(2, line);// 错误处理c类
                consumeToken(); // (

                if (isNextTokenExpressionStart(peekNextToken())) {
                    dimensionTracker.add(0, 0);
                    parseFunctionRParams();
                    dimensionTracker.remove(0);
                    if (peekNextToken().getContent().equals(")")) {
                        consumeToken();
                    } else {
                        recordError(9, line); // j kind error
                    }
                } else {

                    if (peekNextToken().getContent().equals(")")) {
                        consumeToken();
                    } else {
                        recordError(9, line); // j kind error
                    }
                }

            } else {
                FuncSymbol sym = functionSymbolTable.get(name);
                if (sym.getReturntype() == 0)
                    dimensionTracker.set(0, 100); // for return type void maxarray=100

                ArrayList<NorSymbol> Fparas = sym.getParams();
                ArrayList<Integer> Rparas = new ArrayList<>();
                consumeToken(); // (
                // if (showWord().getContent().equals(")")) {
                // getWord();
                // }else{
                // FuncRParams();
                // if (!getWord().getContent().equals(")"))
                // error();
                // }
                if (isNextTokenExpressionStart(peekNextToken())) {
                    dimensionTracker.add(0, 0);
                    Rparas = parseFunctionRParams();
                    dimensionTracker.remove(0);
                    if (Rparas.size() != Fparas.size()) {
                        recordError(3, line); // d类型错误
                    } else {
                        for (int i = 0; i < Rparas.size(); i++) {
                            if (Rparas.get(i) != Fparas.get(i).getLevel()) {
                                recordError(4, line);
                                break; // e类型错误
                            }
                        }
                    }

                    if (peekNextToken().getContent().equals(")")) {
                        consumeToken();
                    } else {
                        recordError(9, line); // j kind error
                    }
                } else {

                    if (Rparas.size() != Fparas.size()) {
                        recordError(3, line);
                    }

                    if (peekNextToken().getContent().equals(")")) {
                        consumeToken();
                    } else {
                        recordError(9, line); // j kind error
                    }
                }
            }
        } else if (s.equals("+") || s.equals("-") || s.equals("!")) {
            parseUnaryOperator();
            parseUnaryExpression();
        } else {
            parsePrimaryExpression();
        }
        // System.out.print("<UnaryExp>\n");
    }

    private void parseUnaryOperator() {
        String s = consumeToken().getContent();
        if (s.equals("+") || s.equals("-") || s.equals("!")) {

        } else {
            indicateSyntaxErrorPosition();
        }
        // System.out.print("<UnaryOp>\n");
    }

    private ArrayList<Integer> parseFunctionRParams() {
        ArrayList<Integer> list = new ArrayList<>();
        dimensionTracker.set(0, 0);
        parseExpression();
        list.add(dimensionTracker.get(0));
        while (peekNextToken().getContent().equals(",")) {
            consumeToken();
            dimensionTracker.set(0, 0);
            parseExpression();
            list.add(dimensionTracker.get(0));
        }
        return list;
        // System.out.print("<FuncRParams>\n");
    }

    private void parseMultiplicativeExpression() {
        parseUnaryExpression();
        while (true) {
            String s = peekNextToken().getContent();
            if (s.equals("*") || s.equals("/") || s.equals("%")) {
                // System.out.print("<MulExp>\n");
                consumeToken();
                parseUnaryExpression();
            } else {
                break;
            }
        }
        // System.out.print("<MulExp>\n");
    }

    private void parseAdditiveExpression() {
        parseMultiplicativeExpression();
        while (peekNextToken().getContent().equals("+") || peekNextToken().getContent().equals("-")) {
            // System.out.print("<AddExp>\n");
            consumeToken();
            parseMultiplicativeExpression();
        }
        // System.out.print("<AddExp>\n");
    }

    private void parseRelationalExpression() {
        parseAdditiveExpression();
        while (true) {
            String s = peekNextToken().getContent();
            if (s.equals("<") || s.equals(">") || s.equals("<=") || s.equals(">=")) {
                // System.out.print("<RelExp>\n");
                consumeToken();
                parseAdditiveExpression();
            } else {
                break;
            }
        }
        // System.out.print("<RelExp>\n");
    }

    private void parseEqualityExpression() {
        parseRelationalExpression();
        while (peekNextToken().getContent().equals("==") || peekNextToken().getContent().equals("!=")) {
            // System.out.print("<EqExp>\n");
            consumeToken();
            parseRelationalExpression();
        }
        // System.out.print("<EqExp>\n");
    }

    private void parseLogicalAndExpression() {
        parseEqualityExpression();
        while (peekNextToken().getContent().equals("&&")) {
            // System.out.print("<LAndExp>\n");
            consumeToken();
            parseEqualityExpression();
        }
        // System.out.print("<LAndExp>\n");
    }

    private void parseLogicalOrExpression() {
        parseLogicalAndExpression();
        while (peekNextToken().getContent().equals("||")) {
            // System.out.print("<LOrExp>\n");
            consumeToken();
            parseLogicalAndExpression();
        }
        // System.out.print("<LOrExp>\n");
    }

    private void parseConstantExpression() {
        parseAdditiveExpression();
        // System.out.print("<ConstExp>\n");
    }

}
