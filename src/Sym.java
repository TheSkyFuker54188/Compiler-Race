import Word.FormatWord;
import Word.Word;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 词法解析引擎 - 编译器前端的核心字符流处理器
 * 
 * 该类采用有限状态机模式实现源码的词法分析功能
 * 负责将原始字符序列转换为结构化的词法单元流
 * 集成预处理器、关键字识别器和符号分解器等子系统
 */
public class Sym {
    /* 源码字符串缓存 - 保存待分析的完整源代码文本 */
    private final String sourceCodeBuffer;
    
    /* 行号跟踪器 - 维护当前处理位置的行数信息 */
    private int currentLineNumber = 1;
    
    /* 源码字符数组 - 将源码转换为字符序列以便逐字符处理 */
    private char[] rawCharacterArray;
    
    /* 预处理字符数组 - 经过注释去除和格式化的字符序列 */
    private char[] sanitizedCharArray;
    
    /* 预处理字符串 - 清理后的源码字符串表示 */
    private String cleanedSourceText;
    
    /* 词法单元容器 - 存储解析生成的词法符号序列 */
    private ArrayList<Word> lexicalTokenList = new ArrayList<>();
    
    /* 保留字映射表 - 关键字到类型编码的快速查找结构 */
    private HashMap<String, Integer> reservedKeywordMap;
    
    /* 操作符映射表 - 单字符操作符到类型编码的映射关系 */
    private HashMap<Character, Integer> operatorSymbolMap;

    /**
     * 词法分析器构造函数 - 初始化并执行完整的词法解析流程
     * 
     * @param sourceCode 待分析的源代码字符串
     */
    public Sym(String sourceCode) {
        initializeTokenMappings();
        sourceCodeBuffer = sourceCode;
        rawCharacterArray = sourceCode.toCharArray();
        performPreprocessing(rawCharacterArray);
        executeTokenization();
    }

    /**
     * 获取解析生成的词法单元序列
     * 
     * @return 词法符号列表
     */
    public ArrayList<Word> getWords() {
        return lexicalTokenList;
    }

    /**
     * 源码预处理器 - 执行注释过滤和格式标准化操作
     * 采用双缓冲区机制处理字符流，确保内存安全和处理效率
     * 
     * @param characterStream 待处理的原始字符数组
     */
    private void performPreprocessing(char[] characterStream) {
        char[] processingBuffer = new char[characterStream.length + 1];
        int bufferIndex = 0;
        
        for (int cursor = 0; cursor < characterStream.length; cursor++) {
            // 单行注释消除逻辑
            if (cursor + 1 < characterStream.length && 
                characterStream[cursor] == '/' && characterStream[cursor + 1] == '/') {
                while (cursor < characterStream.length && characterStream[cursor] != '\n') {
                    cursor++;
                }
                if (cursor < characterStream.length) {
                    cursor--; // 回退以便外层循环正确处理换行符
                }
            }
            // 多行注释消除逻辑
            else if (cursor + 1 < characterStream.length && 
                     characterStream[cursor] == '/' && characterStream[cursor + 1] == '*') {
                cursor += 2;
                while (cursor + 1 < characterStream.length && 
                       !(characterStream[cursor] == '*' && characterStream[cursor + 1] == '/')) {
                    if (characterStream[cursor] == '\n') {
                        processingBuffer[bufferIndex++] = '\n';
                    }
                    cursor++;
                }
                if (cursor + 1 < characterStream.length) {
                    cursor++; // 跳过 '*/'
                }
                processingBuffer[bufferIndex++] = ' ';
                continue;
            }
            // 字符串字面量保护机制
            else if (characterStream[cursor] == '"') {
                processingBuffer[bufferIndex++] = characterStream[cursor++];
                while (cursor < characterStream.length && characterStream[cursor] != '"') {
                    processingBuffer[bufferIndex++] = characterStream[cursor++];
                }
                if (cursor < characterStream.length) {
                    processingBuffer[bufferIndex++] = characterStream[cursor]; // 添加结束引号
                }
            }
            // 标准字符过滤和保留
            else if (characterStream[cursor] != '\r') {
                processingBuffer[bufferIndex++] = characterStream[cursor];
            }
        }

        // 添加流结束标记
        processingBuffer[bufferIndex++] = '$';

        // 构建最终的清理后字符数组
        char[] finalizedArray = new char[bufferIndex];
        System.arraycopy(processingBuffer, 0, finalizedArray, 0, bufferIndex);
        this.sanitizedCharArray = finalizedArray;
        this.cleanedSourceText = new String(sanitizedCharArray);
    }

    /**
     * 词汇映射表初始化器 - 构建关键字和操作符的识别字典
     * 采用哈希表数据结构实现O(1)时间复杂度的符号类型查找
     */
    private void initializeTokenMappings() {
        reservedKeywordMap = new HashMap<>();
        reservedKeywordMap.put("main", 4);
        reservedKeywordMap.put("const", 5);
        reservedKeywordMap.put("int", 6);
        reservedKeywordMap.put("break", 7);
        reservedKeywordMap.put("continue", 8);
        reservedKeywordMap.put("if", 9);
        reservedKeywordMap.put("else", 10);
        reservedKeywordMap.put("while", 14);
        reservedKeywordMap.put("getint", 15);
        reservedKeywordMap.put("printf", 16);
        reservedKeywordMap.put("return", 17);
        reservedKeywordMap.put("void", 20);
        
        operatorSymbolMap = new HashMap<>();
        operatorSymbolMap.put('+', 18);
        operatorSymbolMap.put('-', 19);
        operatorSymbolMap.put('*', 21);
        operatorSymbolMap.put('/', 22);
        operatorSymbolMap.put('%', 23);
        operatorSymbolMap.put(';', 31);
        operatorSymbolMap.put(',', 32);
        operatorSymbolMap.put('(', 33);
        operatorSymbolMap.put(')', 34);
        operatorSymbolMap.put('[', 35);
        operatorSymbolMap.put(']', 36);
        operatorSymbolMap.put('{', 37);
        operatorSymbolMap.put('}', 38);
    }

    /**
     * 词法单元提取引擎 - 执行字符流到词法符号的转换过程
     * 采用有限状态机算法识别标识符、数字、字符串和操作符等不同类型的词汇单元
     */
    private void executeTokenization() {
        int streamPointer = 0;
        int segmentEnd = 0;
        
        while (streamPointer < sanitizedCharArray.length && sanitizedCharArray[streamPointer] != '$') {
            // 跳过空白字符和制表符，同时维护行号计数
            while (streamPointer < sanitizedCharArray.length && 
                   (sanitizedCharArray[streamPointer] == ' ' || 
                    sanitizedCharArray[streamPointer] == '\t' || 
                    sanitizedCharArray[streamPointer] == '\n')) {
                if (sanitizedCharArray[streamPointer] == '\n')
                    currentLineNumber++;
                streamPointer++;
            }
            
            if (streamPointer >= sanitizedCharArray.length || sanitizedCharArray[streamPointer] == '$') break;
            
            // 标识符和关键字识别逻辑
            if (Character.isUpperCase(sanitizedCharArray[streamPointer]) || 
                Character.isLowerCase(sanitizedCharArray[streamPointer]) || 
                sanitizedCharArray[streamPointer] == '_') {
                segmentEnd = streamPointer;
                while (segmentEnd < sanitizedCharArray.length && 
                       (Character.isLetterOrDigit(sanitizedCharArray[segmentEnd]) || 
                        sanitizedCharArray[segmentEnd] == '_')) {
                    segmentEnd++;
                }
                String identifierText = cleanedSourceText.substring(streamPointer, segmentEnd);
                lexicalTokenList.add(new Word(reservedKeywordMap.getOrDefault(identifierText, 1), 
                                            identifierText, currentLineNumber));
                streamPointer = segmentEnd;
            } 
            // 数字字面量识别逻辑
            else if (Character.isDigit(sanitizedCharArray[streamPointer])) {
                segmentEnd = streamPointer;
                while (segmentEnd < sanitizedCharArray.length && 
                       Character.isDigit(sanitizedCharArray[segmentEnd])) {
                    segmentEnd++;
                }
                lexicalTokenList.add(new Word(2, cleanedSourceText.substring(streamPointer, segmentEnd), 
                                            currentLineNumber));
                streamPointer = segmentEnd;
            } 
            // 字符串字面量识别逻辑
            else if (sanitizedCharArray[streamPointer] == '"') {
                int formatCount = 0;
                boolean validFormat = true;
                segmentEnd = streamPointer + 1;
                while (segmentEnd < sanitizedCharArray.length && sanitizedCharArray[segmentEnd] != '"') {
                    if (sanitizedCharArray[segmentEnd] == '\\') {
                        if (segmentEnd + 1 < sanitizedCharArray.length && 
                            sanitizedCharArray[segmentEnd + 1] != 'n') {
                            validFormat = false;
                        }
                        segmentEnd += 2;
                    } else if (sanitizedCharArray[segmentEnd] == '%') {
                        if (segmentEnd + 1 < sanitizedCharArray.length && 
                            (sanitizedCharArray[segmentEnd + 1] == 'd' || 
                             sanitizedCharArray[segmentEnd + 1] == 'c')) {
                            formatCount++;
                        } else {
                            validFormat = false;
                        }
                        segmentEnd += 2;
                    } else {
                        segmentEnd++;
                    }
                }
                lexicalTokenList.add(new FormatWord(3, cleanedSourceText.substring(streamPointer, segmentEnd), 
                                                  currentLineNumber, formatCount, validFormat));
                streamPointer = segmentEnd + 1;
            } 
            // 逻辑AND操作符识别
            else if (sanitizedCharArray[streamPointer] == '&') {
                if (streamPointer + 1 < sanitizedCharArray.length && 
                    sanitizedCharArray[streamPointer + 1] == '&') {
                    lexicalTokenList.add(new Word(12, "&&", currentLineNumber));
                    streamPointer += 2;
                } else {
                    System.out.println("&wrong");
                    break;
                }
            }
            // 逻辑NOT和不等操作符识别
            else if (sanitizedCharArray[streamPointer] == '!') {
                if (streamPointer + 1 < sanitizedCharArray.length && 
                    sanitizedCharArray[streamPointer + 1] == '=') {
                    lexicalTokenList.add(new Word(29, "!=", currentLineNumber));
                    streamPointer += 2;
                } else {
                    lexicalTokenList.add(new Word(11, "!", currentLineNumber));
                    streamPointer++;
                }
            }
            // 逻辑OR操作符识别
            else if (sanitizedCharArray[streamPointer] == '|') {
                if (streamPointer + 1 < sanitizedCharArray.length && 
                    sanitizedCharArray[streamPointer + 1] == '|') {
                    lexicalTokenList.add(new Word(13, "||", currentLineNumber));
                    streamPointer += 2;
                } else {
                    System.out.println("|wrong");
                    break;
                }
            }
            // 小于和小于等于操作符识别
            else if (sanitizedCharArray[streamPointer] == '<') {
                if (streamPointer + 1 < sanitizedCharArray.length && 
                    sanitizedCharArray[streamPointer + 1] == '=') {
                    lexicalTokenList.add(new Word(25, "<=", currentLineNumber));
                    streamPointer += 2;
                } else {
                    lexicalTokenList.add(new Word(24, "<", currentLineNumber));
                    streamPointer++;
                }
            }
            // 大于和大于等于操作符识别
            else if (sanitizedCharArray[streamPointer] == '>') {
                if (streamPointer + 1 < sanitizedCharArray.length && 
                    sanitizedCharArray[streamPointer + 1] == '=') {
                    lexicalTokenList.add(new Word(27, ">=", currentLineNumber));
                    streamPointer += 2;
                } else {
                    lexicalTokenList.add(new Word(26, ">", currentLineNumber));
                    streamPointer++;
                }
            }
            // 等于和赋值操作符识别
            else if (sanitizedCharArray[streamPointer] == '=') {
                if (streamPointer + 1 < sanitizedCharArray.length && 
                    sanitizedCharArray[streamPointer + 1] == '=') {
                    lexicalTokenList.add(new Word(28, "==", currentLineNumber));
                    streamPointer += 2;
                } else {
                    lexicalTokenList.add(new Word(30, "=", currentLineNumber));
                    streamPointer++;
                }
            }
            // 其他单字符操作符处理
            else if (operatorSymbolMap.containsKey(sanitizedCharArray[streamPointer])) {
                lexicalTokenList.add(new Word(operatorSymbolMap.get(sanitizedCharArray[streamPointer]), 
                                            String.valueOf(sanitizedCharArray[streamPointer]), 
                                            currentLineNumber));
                streamPointer++;
            }
            // 未识别字符处理
            else {
                System.out.println("Unrecognized character: " + sanitizedCharArray[streamPointer]);
                streamPointer++;
            }
        }
    }
}
