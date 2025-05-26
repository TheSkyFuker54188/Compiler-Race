package Optim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class Register {
    private HashMap<String, String> variableToRegisterMapping;
    private Queue<String> availableRegisterPool;
    private Stack<String> allocatedRegisterStack;
    private static final String[] TEMPORARY_REGISTERS = {
            "$t3", "$t4", "$t5", "$t6", "$t7", "$t8", "$t9"
    };

    public Register() {
        initializeRegisterManager();
    }

    private void initializeRegisterManager() {
        this.variableToRegisterMapping = new HashMap<>();
        this.availableRegisterPool = new LinkedList<>(Arrays.asList(TEMPORARY_REGISTERS));
        this.allocatedRegisterStack = new Stack<>();
    }

    public String findtemp(String variableName) {
        String assignedRegister = variableToRegisterMapping.get(variableName);
        if (assignedRegister != null) {
            relocateRegisterToAvailable(assignedRegister);
        }
        return assignedRegister;
    }

    private void relocateRegisterToAvailable(String registerName) {
        if (allocatedRegisterStack.remove(registerName)) {
            availableRegisterPool.offer(registerName);
        }
    }

    public ArrayList<String> getReverlists() {
        return new ArrayList<>(allocatedRegisterStack);
    }

    public ArrayList<String> getLists() {
        return new ArrayList<>(availableRegisterPool);
    }

    public String gettemp(String variableName) {
        if (isRegisterPoolEmpty()) {
            displayResourceExhaustedMessage();
            return null;
        }

        String selectedRegister = retrieveNextAvailableRegister();
        establishVariableRegisterBinding(variableName, selectedRegister);
        return selectedRegister;
    }

    private boolean isRegisterPoolEmpty() {
        return availableRegisterPool.isEmpty();
    }

    private void displayResourceExhaustedMessage() {
        System.out.print("Register allocation failed - no available registers");
    }

    private String retrieveNextAvailableRegister() {
        String register = availableRegisterPool.poll();
        allocatedRegisterStack.push(register);
        return register;
    }

    private void establishVariableRegisterBinding(String variable, String register) {
        variableToRegisterMapping.put(variable, register);
    }

    public void reset() {
        reinitializeAllDataStructures();
    }

    private void reinitializeAllDataStructures() {
        variableToRegisterMapping.clear();
        availableRegisterPool.clear();
        allocatedRegisterStack.clear();
        availableRegisterPool.addAll(Arrays.asList(TEMPORARY_REGISTERS));
    }
}
