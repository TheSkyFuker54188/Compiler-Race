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
import java.util.List;

public class Parsing_error {

    private final List<Word> tokenStream;
    private int currentTokenIndex = 0;

    private FuncTable functionSymbolTable = new FuncTable();
    private IntergerTable variableSymbolTable = new IntergerTable();

    private static class ErrorInfo {
        final int lineNumber;
        final int errorCode;

        ErrorInfo(int line, int code) {
            this.lineNumber = line;
            this.errorCode = code;
        }
    }

    private final List<ErrorInfo> detectedErrors = new ArrayList<>();

    private int loopNestingLevel = 0;
    private int currentFunctionReturnType = 0;
    private boolean withinConditionalConstruct = false;
    private boolean isLastStatementReturn = false;
    private ArrayList<Integer> dimensionTracker = new ArrayList<>();
    private int functionDepth = 0;

    public Parsing_error(ArrayList<Word> tokenSequence) {
        this.tokenStream = tokenSequence;
        this.dimensionTracker.add(0);
        performSyntaxAnalysis();

        if (!detectedErrors.isEmpty()) {
            System.out.println("Syntax errors detected in your code:\nBelow are the identified issues:");
            reportErrors();
            System.exit(1);
        }
    }

    public void performSyntaxAnalysis() {
        parseCompilationUnit();
    }

    private Word consumeToken() {
        if (currentTokenIndex < tokenStream.size()) {
            return tokenStream.get(currentTokenIndex++);
        }
        return new Word();
    }

    private Word peekNextToken() {
        if (currentTokenIndex < tokenStream.size()) {
            return tokenStream.get(currentTokenIndex);
        }
        return new Word();
    }

    private Word peekTokenAt(int lookaheadIndex) {
        if (lookaheadIndex < tokenStream.size()) {
            return tokenStream.get(lookaheadIndex);
        }
        return new Word();
    }

    private boolean isNextTokenExpressionStart(Word token) {
        String content = token.getContent();
        int symbolNumber = token.getSymnumber();

        if ("(".equals(content) || "+".equals(content) || "-".equals(content) || "!".equals(content)) {
            return true;
        }
        return symbolNumber == 1 || symbolNumber == 2;
    }

    private void indicateSyntaxErrorPosition() {
        System.out.print("---------------SYNTAX ERROR DETECTED-------------");
    }

    private void recordError(int errorCode, int lineNumber) {
        detectedErrors.add(new ErrorInfo(lineNumber, errorCode));
    }

    private void reportErrors() {
        detectedErrors.sort(Comparator.comparingInt(e -> e.lineNumber));
        for (ErrorInfo error : detectedErrors) {
            System.out.println(error.lineNumber + " " + (char) ('a' + error.errorCode));
        }
    }

    private void parseCompilationUnit() {
        while (currentTokenIndex + 2 < tokenStream.size() &&
                !peekTokenAt(currentTokenIndex + 2).getContent().equals("(")) {
            parseDeclaration();
        }

        while (currentTokenIndex + 1 < tokenStream.size() &&
                !peekTokenAt(currentTokenIndex + 1).getContent().equals("main")) {
            parseFunctionDefinition();
        }
        parseMainFunctionDefinition();
    }

    private void parseDeclaration() {
        if (peekNextToken().getContent().equals("const")) {
            parseConstantDeclaration();
        } else {
            parseVariableDeclaration();
        }
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
                recordError(8, sym.getLine()); 
            }
        } else {
            indicateSyntaxErrorPosition();
        }
    }

    private void parseBaseType() {
        if (!consumeToken().getContent().equals("int")) {
            indicateSyntaxErrorPosition();
        }
    }

    private NorSymbol parseConstantDefinition() {
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
                if (peekNextToken().getContent().equals("]")) { 
                    consumeToken();
                } else {
                    recordError(10, w1.getLine()); 
                }
            }
            if (!consumeToken().getContent().equals("=")) {
                indicateSyntaxErrorPosition();
            }
            parseConstantInitializationValue();

            if (variableSymbolTable.contains(name)) { 
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
            recordError(8, sym.getLine()); 
        }
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
                if (peekNextToken().getContent().equals("]")) { 
                    consumeToken();
                } else {
                    recordError(10, w1.getLine());
                }
            }
            if (peekNextToken().getContent().equals("=")) {
                consumeToken();
                parseInitializationValue();
            }

            if (variableSymbolTable.contains(name)) {
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
    }

    private void parseFunctionDefinition() {

        IntergerTable newtable = new IntergerTable();
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
        Word w1 = consumeToken();

        if (peekNextToken().getContent().equals(")")) {
            consumeToken();
        } else if (peekNextToken().getContent().equals("{")) {
            recordError(9, w1.getLine());
        } else {
            list = parseFunctionParameters();
            if (peekNextToken().getContent().equals(")")) {
                consumeToken();
            } else {
                recordError(9, w1.getLine());
            }
        }

        if (functionSymbolTable.contains(name)) {
            recordError(1, line);
        } else {
            FuncSymbol sym = new FuncSymbol(name, list, type);
            functionSymbolTable.add(name, sym);
        }

        functionDepth = 1;
        Word w2 = parseBlock();
        if (type == 1 && !isLastStatementReturn)
            recordError(6, w2.getLine());
    }

    private void parseMainFunctionDefinition() {

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
            recordError(6, w1.getLine());
    }

    private int parseFunctionType() {
        String s = consumeToken().getContent();
        if (s.equals("void"))
            return 0;
        else if (s.equals("int"))
            return 1;
        else
            return -1;
    }

    private ArrayList<NorSymbol> parseFunctionParameters() {
        ArrayList<NorSymbol> list = new ArrayList<>();
        list.add(parseFunctionParameter());
        while (peekNextToken().getContent().equals(",")) {
            consumeToken();
            list.add(parseFunctionParameter());
        }
        return list;
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
                recordError(10, w1.getLine());
            }

            while (peekNextToken().getContent().equals("[")) {
                count++;
                w1 = consumeToken();
                parseConstantExpression();
                if (peekNextToken().getContent().equals("]")) {
                    consumeToken();
                } else {
                    recordError(10, w1.getLine());
                }
            }
        }
        if (variableSymbolTable.contains(name)) {
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
                IntergerTable newtable = new IntergerTable();
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
    }

    private void parseBlockItem() {
        if (peekNextToken().getContent().equals("int") || peekNextToken().getContent().equals("const")) {
            parseDeclaration();
        } else {
            parseStatement();
        }
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
                recordError(9, line);
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
                recordError(9, line);
            }
            loopNestingLevel++;
            parseStatement();
            loopNestingLevel--;
            withinConditionalConstruct = false;
        } else if (peekNextToken().getContent().equals("break") || peekNextToken().getContent().equals("continue")) {
            Word w = consumeToken();

            if (loopNestingLevel == 0)
                recordError(12, w.getLine());

            if (peekNextToken().getContent().equals(";")) {
                consumeToken();
            } else {
                recordError(8, w.getLine());
            }

        } else if (peekNextToken().getContent().equals("return")) {
            if (withinConditionalConstruct == false)
                isLastStatementReturn = true;
            Word w = consumeToken();
            int line = w.getLine();
            if (isNextTokenExpressionStart(peekNextToken())) {
                parseExpression();
                if (currentFunctionReturnType == 0)
                    recordError(5, line);
                if (peekNextToken().getContent().equals(";")) {
                    consumeToken();
                } else {
                    recordError(8, line);
                }
            } else {
                if (peekNextToken().getContent().equals(";")) {
                    consumeToken();
                } else {
                    recordError(8, line);
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
                recordError(0, count2);

            while (peekNextToken().getContent().equals(",")) {
                symnum++;
                consumeToken();
                parseExpression();
            }

            if (peekNextToken().getContent().equals(")")) {
                consumeToken();
            } else {
                recordError(9, count2);
            }
            if (peekNextToken().getContent().equals(";")) {
                consumeToken();
            } else {
                recordError(8, count2);
            }

            if (num != symnum)
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
                if (sym.isConst()) {
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
                        recordError(9, line);
                    }
                    if (peekNextToken().getContent().equals(";")) {
                        consumeToken();
                    } else {
                        recordError(8, line);
                    }
                } else {
                    parseExpression();
                    if (peekNextToken().getContent().equals(";")) {
                        consumeToken();
                    } else {
                        recordError(8, line);
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
                recordError(8, w.getLine());
            }
        }
    }

    private Word parseExpression() {
        Word w = peekNextToken();
        parseAdditiveExpression();
        return w;
    }

    private void parseCondition() {
        parseLogicalOrExpression();
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
        while (table != null) {
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
                recordError(10, w1.getLine());
            }
        }
        dimensionTracker.set(0, Math.max(dimensionTracker.get(0), arrayLevel));
        sym.setLine(line);
        return sym;
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
    }

    private void parseNumber() {
        if (consumeToken().getSymnumber() != 2) {
            indicateSyntaxErrorPosition();
        }
    }

    private void parseUnaryExpression() {
        String s = peekNextToken().getContent();
        if (peekNextToken().getSymnumber() == 1 && peekTokenAt(currentTokenIndex + 1).getContent().equals("(")) {

            Word w = consumeToken();
            String name = w.getContent();
            int line = w.getLine();
            int flag = 0;
            if (!functionSymbolTable.contains(name)) {
                recordError(2, line);
                consumeToken();

                if (isNextTokenExpressionStart(peekNextToken())) {
                    dimensionTracker.add(0, 0);
                    parseFunctionRParams();
                    dimensionTracker.remove(0);
                    if (peekNextToken().getContent().equals(")")) {
                        consumeToken();
                    } else {
                        recordError(9, line);
                    }
                } else {

                    if (peekNextToken().getContent().equals(")")) {
                        consumeToken();
                    } else {
                        recordError(9, line);
                    }
                }

            } else {
                FuncSymbol sym = functionSymbolTable.get(name);
                if (sym.getReturntype() == 0)
                    dimensionTracker.set(0, 100);
                ArrayList<NorSymbol> Fparas = sym.getParams();
                ArrayList<Integer> Rparas = new ArrayList<>();
                consumeToken();
                if (isNextTokenExpressionStart(peekNextToken())) {
                    dimensionTracker.add(0, 0);
                    Rparas = parseFunctionRParams();
                    dimensionTracker.remove(0);
                    if (Rparas.size() != Fparas.size()) {
                        recordError(3, line);
                    } else {
                        for (int i = 0; i < Rparas.size(); i++) {
                            if (Rparas.get(i) != Fparas.get(i).getLevel()) {
                                recordError(4, line);
                                break;
                            }
                        }
                    }

                    if (peekNextToken().getContent().equals(")")) {
                        consumeToken();
                    } else {
                        recordError(9, line);
                    }
                } else {

                    if (Rparas.size() != Fparas.size()) {
                        recordError(3, line);
                    }

                    if (peekNextToken().getContent().equals(")")) {
                        consumeToken();
                    } else {
                        recordError(9, line);
                    }
                }
            }
        } else if (s.equals("+") || s.equals("-") || s.equals("!")) {
            parseUnaryOperator();
            parseUnaryExpression();
        } else {
            parsePrimaryExpression();
        }
    }

    private void parseUnaryOperator() {
        String s = consumeToken().getContent();
        if (s.equals("+") || s.equals("-") || s.equals("!")) {

        } else {
            indicateSyntaxErrorPosition();
        }
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
    }

    private void parseMultiplicativeExpression() {
        parseUnaryExpression();
        while (true) {
            String s = peekNextToken().getContent();
            if (s.equals("*") || s.equals("/") || s.equals("%")) {
                consumeToken();
                parseUnaryExpression();
            } else {
                break;
            }
        }
    }

    private void parseAdditiveExpression() {
        parseMultiplicativeExpression();
        while (peekNextToken().getContent().equals("+") || peekNextToken().getContent().equals("-")) {
            consumeToken();
            parseMultiplicativeExpression();
        }
    }

    private void parseRelationalExpression() {
        parseAdditiveExpression();
        while (true) {
            String s = peekNextToken().getContent();
            if (s.equals("<") || s.equals(">") || s.equals("<=") || s.equals(">=")) {
                consumeToken();
                parseAdditiveExpression();
            } else {
                break;
            }
        }
    }

    private void parseEqualityExpression() {
        parseRelationalExpression();
        while (peekNextToken().getContent().equals("==") || peekNextToken().getContent().equals("!=")) {
            consumeToken();
            parseRelationalExpression();
        }
    }

    private void parseLogicalAndExpression() {
        parseEqualityExpression();
        while (peekNextToken().getContent().equals("&&")) {
            consumeToken();
            parseEqualityExpression();
        }
    }

    private void parseLogicalOrExpression() {
        parseLogicalAndExpression();
        while (peekNextToken().getContent().equals("||")) {
            consumeToken();
            parseLogicalAndExpression();
        }
    }

    private void parseConstantExpression() {
        parseAdditiveExpression();
    }

}
