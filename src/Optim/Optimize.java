package Optim;

import Midcode.midCode;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class Optimize {
    private List<midCode> sourceInstructionSet;
    private List<midCode> refinedInstructionSet;
    private Map<String, String> branchTargetMap;

    public Optimize(ArrayList<midCode> inputInstructions) {
        this.sourceInstructionSet = new ArrayList<>(inputInstructions);
        this.refinedInstructionSet = new ArrayList<>();
        this.branchTargetMap = new HashMap<>();

        executeArithmeticOptimization();
        executeBranchOptimization();
        exportOptimizedResult();
    }

    private void exportOptimizedResult() {
        String exportFilePath = "optimize.txt";
        try (PrintStream outputStream = new PrintStream(exportFilePath)) {
            System.setOut(outputStream);
            refinedInstructionSet.forEach(inst -> System.out.println(inst.toString()));
        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
        }
    }

    private void executeBranchOptimization() {
        initializeBranchMappings();
        optimizeBranchChains();
        updateBranchTargets();
    }

    private void initializeBranchMappings() {
        for (int position = 0; position < refinedInstructionSet.size(); position++) {
            midCode currentInstruction = refinedInstructionSet.get(position);
            if (isJumpInstruction(currentInstruction)) {
                midCode nextInstruction = getNextInstructionSafely(position);
                String targetLabel = determineTargetLabel(currentInstruction, nextInstruction);
                branchTargetMap.put(currentInstruction.z, targetLabel);
            }
        }
    }

    private boolean isJumpInstruction(midCode instruction) {
        return instruction.op.equals(midCode.operation.Jump);
    }

    private midCode getNextInstructionSafely(int currentPosition) {
        return currentPosition + 1 < refinedInstructionSet.size() ? refinedInstructionSet.get(currentPosition + 1)
                : null;
    }

    private String determineTargetLabel(midCode jumpInst, midCode nextInst) {
        return (nextInst != null && nextInst.op.equals(midCode.operation.GOTO)) ? nextInst.z : jumpInst.z;
    }

    private void optimizeBranchChains() {
        boolean hasChanges;
        do {
            hasChanges = false;
            for (Map.Entry<String, String> entry : branchTargetMap.entrySet()) {
                String indirectTarget = branchTargetMap.get(entry.getValue());
                if (indirectTarget != null && !entry.getValue().equals(indirectTarget)) {
                    entry.setValue(indirectTarget);
                    hasChanges = true;
                }
            }
        } while (hasChanges);
    }

    private void updateBranchTargets() {
        refinedInstructionSet.forEach(instruction -> {
            if (isBranchInstruction(instruction)) {
                String optimizedTarget = branchTargetMap.get(instruction.z);
                if (optimizedTarget != null) {
                    instruction.z = optimizedTarget;
                }
            }
        });
    }

    private boolean isBranchInstruction(midCode instruction) {
        return instruction.op.equals(midCode.operation.BZ) ||
                instruction.op.equals(midCode.operation.GOTO);
    }

    private void executeArithmeticOptimization() {
        for (int index = 0; index < sourceInstructionSet.size(); index++) {
            midCode currentInstruction = sourceInstructionSet.get(index);

            if (processMultiplicationOptimization(currentInstruction) ||
                    processDivisionOptimization(currentInstruction) ||
                    processRedundantBranchOptimization(currentInstruction, index)) {
                continue;
            }

            refinedInstructionSet.add(currentInstruction);
        }
    }

    private boolean processMultiplicationOptimization(midCode instruction) {
        if (!instruction.op.equals(midCode.operation.MULTOP)) {
            return false;
        }

        ArithmeticOperand leftOperand = analyzeOperand(instruction.x);
        ArithmeticOperand rightOperand = analyzeOperand(instruction.y);

        if (leftOperand.isNumeric()) {
            return optimizeMultiplicationByConstant(instruction, leftOperand.getValue(),
                    instruction.y, instruction.z);
        } else if (rightOperand.isNumeric()) {
            return optimizeMultiplicationByConstant(instruction, rightOperand.getValue(),
                    instruction.x, instruction.z);
        }

        return false;
    }

    private boolean optimizeMultiplicationByConstant(midCode originalInst, int constant,
            String operand, String result) {
        int shiftAmount = calculateShiftEquivalent(constant);

        switch (shiftAmount) {
            case 0:
                refinedInstructionSet.add(createAssignmentInstruction(result, "0"));
                return true;
            case -1:
                if (!result.equals(operand)) {
                    refinedInstructionSet.add(createAssignmentInstruction(result, operand));
                }
                return true;
            default:
                if (shiftAmount > 0) {
                    refinedInstructionSet.add(createShiftInstruction(result, operand, shiftAmount));
                    return true;
                }
        }

        return false;
    }

    private boolean processDivisionOptimization(midCode instruction) {
        if (!instruction.op.equals(midCode.operation.DIVOP)) {
            return false;
        }

        ArithmeticOperand divisor = analyzeOperand(instruction.y);
        if (divisor.isNumeric() && calculateShiftEquivalent(divisor.getValue()) == -1) {
            refinedInstructionSet.add(createAssignmentInstruction(instruction.z, instruction.x));
            return true;
        }

        return false;
    }

    private boolean processRedundantBranchOptimization(midCode instruction, int currentIndex) {
        if (!isBranchInstruction(instruction)) {
            return false;
        }

        midCode subsequentInstruction = getSubsequentInstructionSafely(currentIndex);
        return subsequentInstruction != null &&
                subsequentInstruction.op.equals(midCode.operation.Jump) &&
                instruction.z.equals(subsequentInstruction.z);
    }

    private midCode getSubsequentInstructionSafely(int currentIndex) {
        return currentIndex + 1 < sourceInstructionSet.size() ? sourceInstructionSet.get(currentIndex + 1) : null;
    }

    private ArithmeticOperand analyzeOperand(String operand) {
        try {
            return new ArithmeticOperand(Integer.parseInt(operand));
        } catch (NumberFormatException e) {
            return new ArithmeticOperand();
        }
    }

    private int calculateShiftEquivalent(int value) {
        if (value == 0)
            return 0;
        if (value == 1)
            return -1;
        if (isPowerOfTwo(value)) {
            return computeLog2(value);
        }
        return -2;
    }

    private boolean isPowerOfTwo(int number) {
        return number > 0 && (number & (number - 1)) == 0;
    }

    private int computeLog2(int powerOfTwo) {
        int exponent = 0;
        int temp = powerOfTwo >> 1;
        while (temp > 0) {
            temp >>= 1;
            exponent++;
        }
        return exponent;
    }

    private midCode createAssignmentInstruction(String destination, String source) {
        return new midCode(midCode.operation.ASSIGNOP, destination, source);
    }

    private midCode createShiftInstruction(String destination, String source, int shiftAmount) {
        return new midCode(midCode.operation.SLL, destination, source, String.valueOf(shiftAmount));
    }

    public ArrayList<midCode> getNewmidCodes() {
        return new ArrayList<>(refinedInstructionSet);
    }

    private static class ArithmeticOperand {
        private final boolean isConstant;
        private final int constantValue;

        public ArithmeticOperand() {
            this.isConstant = false;
            this.constantValue = 0;
        }

        public ArithmeticOperand(int value) {
            this.isConstant = true;
            this.constantValue = value;
        }

        public boolean isNumeric() {
            return isConstant;
        }

        public int getValue() {
            return constantValue;
        }
    }
}
