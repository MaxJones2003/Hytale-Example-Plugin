package com.getfriedpig.golem.bytecode;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Virtual machine for executing Golem bytecode
 * Stack-based execution model with support for checkpoints and pausing
 */
public class BytecodeVM {
    private List<Instruction> instructions;
    private List<FunctionDef> functions;
    private ExecutionState state;
    private int maxInstructionsPerFrame = 10000; // Prevent infinite loops
    // Use shared registry instance - all VMs use the same one
    private NativeFunctionRegistry nativeFunctionRegistry = NativeFunctionRegistry.getInstance();

    /**
     * Function definition from bytecode
     */
    public static class FunctionDef {
        public int id;
        public String name;
        public List<String> params;
        public List<Instruction> instructions;

        public FunctionDef(int id, String name, List<String> params, List<Instruction> instructions) {
            this.id = id;
            this.name = name;
            this.params = params;
            this.instructions = instructions;
        }
    }

    /**
     * Load bytecode from JSON
     */
    public static BytecodeVM load(JsonObject bytecodeJson) throws Exception {
        BytecodeVM vm = new BytecodeVM();
        
        // If the JSON has a nested "bytecode" field, extract it first
        // This handles the wrapper structure from TypeScript.compile()
        if (bytecodeJson.has("bytecode") && !bytecodeJson.has("instructions")) {
            bytecodeJson = bytecodeJson.get("bytecode").getAsJsonObject();
        }
        
        // Load functions
        vm.functions = new ArrayList<>();
        if (bytecodeJson.has("functions")) {
            JsonArray functionsArray = bytecodeJson.getAsJsonArray("functions");
            for (int i = 0; i < functionsArray.size(); i++) {
                JsonObject funcJson = functionsArray.get(i).getAsJsonObject();
                FunctionDef func = BytecodeVM.loadFunction(funcJson);
                vm.functions.add(func);
            }
        }

        // Load main instructions
        vm.instructions = new ArrayList<>();
        if (bytecodeJson.has("instructions")) {
            JsonArray instrArray = bytecodeJson.getAsJsonArray("instructions");
            for (int i = 0; i < instrArray.size(); i++) {
                JsonObject instrJson = instrArray.get(i).getAsJsonObject();
                Instruction instr = Instruction.fromJson(instrJson);
                vm.instructions.add(instr);
            }
        }

        vm.state = new ExecutionState();
        return vm;
    }

    private static FunctionDef loadFunction(JsonObject funcJson) {
        int id = funcJson.get("id").getAsInt();
        String name = funcJson.get("name").getAsString();
        
        List<String> params = new ArrayList<>();
        JsonArray paramsArray = funcJson.getAsJsonArray("params");
        for (int i = 0; i < paramsArray.size(); i++) {
            params.add(paramsArray.get(i).getAsString());
        }

        List<Instruction> instructions = new ArrayList<>();
        JsonArray instrArray = funcJson.getAsJsonArray("instructions");
        for (int i = 0; i < instrArray.size(); i++) {
            JsonObject instrJson = instrArray.get(i).getAsJsonObject();
            Instruction instr = Instruction.fromJson(instrJson);
            instructions.add(instr);
        }

        return new FunctionDef(id, name, params, instructions);
    }

    /**
     * Execute the bytecode
     * Runs up to maxInstructionsPerFrame instructions or until CHECKPOINT
     */
    public VMResult execute() {
        // Set the execution context so native functions can access it
        ExecutionContext.setCurrentState(state);
        
        try {
            int instructionCount = 0;

            while (state.pc < instructions.size() && instructionCount < maxInstructionsPerFrame) {
                Instruction instr = instructions.get(state.pc);

                executeInstruction(instr);

                state.pc++;
                instructionCount++;

                // Check if paused at checkpoint
                if (state.paused) {
                    return VMResult.paused(state.snapshot(), state.pausedAtCheckpoint);
                }
            }

            // Finished
            if (state.pc >= instructions.size()) {
                Object result = state.stack.isEmpty() ? null : state.stack.get(state.stack.size() - 1);
                return VMResult.completed(result);
            }

            // Hit instruction limit (shouldn't happen in normal execution)
            return VMResult.error("Instruction limit exceeded");

        } catch (Exception e) {
            return VMResult.error(e.getMessage());
        } finally {
            // Always clear the execution context when done
            ExecutionContext.clear();
        }
    }

    /**
     * Resume execution from a saved state
     */
    public VMResult resume(ExecutionState savedState) {
        this.state = savedState;
        state.paused = false;
        state.pausedAtCheckpoint = null;
        return execute();
    }

    /**
     * Execute a single instruction
     */
    private void executeInstruction(Instruction instr) throws Exception {
        switch (instr.op) {
            // Stack operations
            case "PUSH_CONST":
                executePushConst(instr);
                break;
            case "POP":
                state.popStack();
                break;
            case "DUP":
                state.pushStack(state.peekStack());
                break;
            case "SWAP":
                executeSwap();
                break;

            // Variables
            case "LOAD_VAR":
                executeLoadVar(instr);
                break;
            case "STORE_VAR":
                executeStoreVar(instr);
                break;

            // New explicit variable operations
            case "DECLARE":
                executeDeclare(instr);
                break;
            case "ASSIGN":
                executeAssign(instr);
                break;
            case "POSTINC":
                executePostInc(instr);
                break;
            case "POSTDEC":
                executePostDec(instr);
                break;
            case "PREINC":
                executePreInc(instr);
                break;
            case "PREDEC":
                executePreDec(instr);
                break;

            // Scope management
            case "SCOPE_PUSH":
                // TODO: Implement scope push when scope management is added
                break;
            case "SCOPE_POP":
                // TODO: Implement scope pop when scope management is added
                break;

            // Control flow - loops
            case "BREAK":
                executeBreak();
                break;
            case "CONTINUE":
                executeContinue();
                break;

            // Arithmetic
            case "ADD":
                executeBinOp("+");
                break;
            case "SUB":
                executeBinOp("-");
                break;
            case "MUL":
                executeBinOp("*");
                break;
            case "DIV":
                executeBinOp("/");
                break;
            case "MOD":
                executeBinOp("%");
                break;

            // Comparison
            case "EQ":
                executeBinOp("==");
                break;
            case "NE":
                executeBinOp("!=");
                break;
            case "LT":
                executeBinOp("<");
                break;
            case "LE":
                executeBinOp("<=");
                break;
            case "GT":
                executeBinOp(">");
                break;
            case "GE":
                executeBinOp(">=");
                break;

            // Logic
            case "AND":
                executeBinOp("&&");
                break;
            case "OR":
                executeBinOp("||");
                break;
            case "NOT":
                executeNot();
                break;

            // Control flow
            case "JMP":
                state.pc = instr.target - 1; // -1 because loop will increment
                break;
            case "JMP_IF":
                if (isTruthy(state.popStack())) {
                    state.pc = instr.target - 1;
                }
                break;
            case "JMP_NOT":
                if (!isTruthy(state.popStack())) {
                    state.pc = instr.target - 1;
                }
                break;

            // Function calls
            case "CALL":
                executeCall(instr);
                break;
            case "RETURN":
                executeReturn();
                break;

            // Java interop
            case "NATIVE_CALL":
                executeNativeCall(instr);
                break;
            case "CHECKPOINT":
                executeCheckpoint(instr);
                break;

            // Objects & Arrays
            case "OBJECT_GET":
                executeObjectGet(instr);
                break;
            case "OBJECT_SET":
                executeObjectSet(instr);
                break;
            case "ARRAY_GET":
                executeArrayGet();
                break;
            case "ARRAY_SET":
                executeArraySet();
                break;
            case "ARRAY_NEW":
                executeArrayNew(instr);
                break;

            // Classes
            case "CLASS_DEF":
                executeClassDef(instr);
                break;
            case "NEW":
                executeNew(instr);
                break;
            case "THIS_LOAD":
                executeThisLoad();
                break;
            case "PROP_GET":
                executePropGet(instr);
                break;
            case "PROP_SET":
                executePropSet(instr);
                break;
            case "METHOD_CALL":
                executeMethodCall(instr);
                break;
            case "STATIC_CALL":
                executeStaticCall(instr);
                break;
            case "SUPER_CALL":
                executeSuperCall(instr);
                break;

            default:
                throw new Exception("Unknown opcode: " + instr.op);
        }
    }

    private void executePushConst(Instruction instr) {
        if (instr.value.isJsonPrimitive()) {
            if (instr.value.getAsJsonPrimitive().isNumber()) {
                state.pushStack(instr.value.getAsDouble());
            } else if (instr.value.getAsJsonPrimitive().isString()) {
                state.pushStack(instr.value.getAsString());
            } else if (instr.value.getAsJsonPrimitive().isBoolean()) {
                state.pushStack(instr.value.getAsBoolean());
            }
        } else if (instr.value.isJsonNull()) {
            state.pushStack(null);
        }
    }

    private void executeLoadVar(Instruction instr) {
        Object value = state.getLocal(instr.name);
        state.pushStack(value);
    }

    private void executeStoreVar(Instruction instr) {
        Object value = state.popStack();
        state.setLocal(instr.name, value);
    }

    private void executeDeclare(Instruction instr) {
        // Declare variable: pop value and store it, don't push anything
        Object value = state.popStack();
        state.setLocal(instr.name, value);
    }

    private void executeAssign(Instruction instr) {
        // Assign: pop value, store it, push the value back
        Object value = state.popStack();
        state.setLocal(instr.name, value);
        state.pushStack(value);
    }

    private void executePostInc(Instruction instr) throws Exception {
        // Postfix increment: yields old value
        Object current = state.getLocal(instr.name);
        double currentVal = toDouble(current);
        double newVal = currentVal + 1;
        state.setLocal(instr.name, newVal);
        state.pushStack(currentVal);  // Push old value
    }

    private void executePostDec(Instruction instr) throws Exception {
        // Postfix decrement: yields old value
        Object current = state.getLocal(instr.name);
        double currentVal = toDouble(current);
        double newVal = currentVal - 1;
        state.setLocal(instr.name, newVal);
        state.pushStack(currentVal);  // Push old value
    }

    private void executePreInc(Instruction instr) throws Exception {
        // Prefix increment: yields new value
        Object current = state.getLocal(instr.name);
        double currentVal = toDouble(current);
        double newVal = currentVal + 1;
        state.setLocal(instr.name, newVal);
        state.pushStack(newVal);  // Push new value
    }

    private void executePreDec(Instruction instr) throws Exception {
        // Prefix decrement: yields new value
        Object current = state.getLocal(instr.name);
        double currentVal = toDouble(current);
        double newVal = currentVal - 1;
        state.setLocal(instr.name, newVal);
        state.pushStack(newVal);  // Push new value
    }

    private void executeSwap() throws Exception {
        // Swap top two stack values
        if (state.stackSize() < 2) {
            throw new Exception("Stack underflow for SWAP");
        }
        Object top = state.popStack();
        Object second = state.popStack();
        state.pushStack(top);
        state.pushStack(second);
    }

    private void executeBreak() throws Exception {
        // TODO: Implement break with loop tracking
        // For now, mark as unimplemented
        throw new Exception("Break statement not yet implemented");
    }

    private void executeContinue() throws Exception {
        // TODO: Implement continue with loop tracking
        // For now, mark as unimplemented
        throw new Exception("Continue statement not yet implemented");
    }

    private void executeBinOp(String op) throws Exception {
        Object right = state.popStack();
        Object left = state.popStack();

        Object result = applyBinOp(op, left, right);
        state.pushStack(result);
    }

    private void executeNot() {
        Object value = state.popStack();
        state.pushStack(!isTruthy(value));
    }

    private void executeCall(Instruction instr) throws Exception {
        // Get arguments from stack (they're in reverse order)
        List<Object> args = new ArrayList<>();
        if (instr.args != null) {
            for (int i = 0; i < instr.args; i++) {
                args.add(0, state.popStack());
            }
        }
        
        // Get the function to call
        if (instr.function == null || instr.function >= functions.size()) {
            throw new Exception("Invalid function reference: " + instr.function);
        }
        
        FunctionDef func = functions.get(instr.function);

        
        // Save current state
        int savedPC = state.pc;
        Map<String, Object> savedLocals = new HashMap<>(state.locals);
        List<Instruction> savedInstructions = instructions;
        
        // Create a new call frame
        ExecutionState.CallFrame frame = new ExecutionState.CallFrame(instr.function, savedPC);
        frame.locals = savedLocals;
        state.callStack.add(frame);
        
        // Set up for function execution
        state.locals.clear();
        instructions = func.instructions;
        state.pc = 0;
        
        // Bind parameters to locals
        for (int i = 0; i < func.params.size(); i++) {
            String param = func.params.get(i);
            Object arg = i < args.size() ? args.get(i) : null;
            state.locals.put(param, arg);
        }
        
        // Execute function instructions with proper PC handling
        Object result = null;
        try {
            while (state.pc < instructions.size()) {
                Instruction funcInstr = instructions.get(state.pc);
                
                if (funcInstr.op.equals("RETURN")) {
                    result = state.stack.isEmpty() ? null : state.popStack();
                    break;
                }
                
                executeInstruction(funcInstr);
                state.pc++;
            }
        } finally {
            // Restore previous state
            if (!state.callStack.isEmpty()) {
                ExecutionState.CallFrame prevFrame = state.callStack.remove(state.callStack.size() - 1);
                state.locals = prevFrame.locals;
            }
            // Restore previous instruction list and PC
            instructions = savedInstructions;
            state.pc = savedPC;
        }
        
        // Push result
        state.pushStack(result);
    }

    private void executeReturn() {
        // Return is handled in executeCall
    }

    private void executeNativeCall(Instruction instr) throws Exception {
        List<Object> args = new ArrayList<>();
        if (instr.args != null) {
            for (int i = 0; i < instr.args; i++) {
                args.add(0, state.popStack()); // Reverse order
            }
        }

        Object result = callNativeFunction(instr.namespace, instr.className, instr.method, args);
        state.pushStack(result);
    }

    private void executeCheckpoint(Instruction instr) {
        state.paused = true;
        state.pausedAtCheckpoint = instr.id;
    }

    private void executeObjectGet(Instruction instr) throws Exception {
        // TODO: Implement object property access
    }

    private void executeObjectSet(Instruction instr) throws Exception {
        // TODO: Implement object property assignment
    }

    private void executeArrayGet() throws Exception {
        Object indexObj = state.popStack();
        Object arrayObj = state.popStack();
        
        if (!(arrayObj instanceof List)) {
            throw new RuntimeException("Cannot get array element: target is not an array");
        }
        if (!(indexObj instanceof Number)) {
            throw new RuntimeException("Array index must be a number");
        }
        
        List<Object> array = (List<Object>) arrayObj;
        int index = ((Number) indexObj).intValue();
        
        if (index < 0 || index >= array.size()) {
            throw new RuntimeException("Array index out of bounds: " + index);
        }
        
        state.pushStack(array.get(index));
    }

    private void executeArraySet() throws Exception {
        Object indexObj = state.popStack();  // Pop index first (it's on top)
        Object value = state.popStack();     // Pop value second
        Object arrayObj = state.popStack();  // Pop array last
        
        if (!(arrayObj instanceof List)) {
            throw new RuntimeException("Cannot set array element: target is not an array");
        }
        if (!(indexObj instanceof Number)) {
            throw new RuntimeException("Array index must be a number");
        }
        
        List<Object> array = (List<Object>) arrayObj;
        int index = ((Number) indexObj).intValue();
        
        if (index < 0 || index >= array.size()) {
            throw new RuntimeException("Array index out of bounds: " + index);
        }
        
        array.set(index, value);
        state.pushStack(array);
    }

    private void executeArrayNew(Instruction instr) {
        List<Object> array = new ArrayList<>();
        if (instr.size != null) {
            for (int i = 0; i < instr.size; i++) {
                array.add(null);
            }
        }
        state.pushStack(array);
    }

    private Object applyBinOp(String op, Object left, Object right) throws Exception {
        // Handle string concatenation for + operator
        if ("+".equals(op) && (left instanceof String || right instanceof String)) {
            return String.valueOf(left) + String.valueOf(right);
        }
        
        double l = toDouble(left);
        double r = toDouble(right);

        switch (op) {
            case "+":
                return l + r;
            case "-":
                return l - r;
            case "*":
                return l * r;
            case "/":
                if (r == 0) throw new Exception("Division by zero");
                return l / r;
            case "%":
                return l % r;
            case "==":
                return left == null ? right == null : left.equals(right);
            case "!=":
                return left == null ? right != null : !left.equals(right);
            case "<":
                return l < r;
            case "<=":
                return l <= r;
            case ">":
                return l > r;
            case ">=":
                return l >= r;
            case "&&":
                return isTruthy(left) && isTruthy(right);
            case "||":
                return isTruthy(left) || isTruthy(right);
            default:
                throw new Exception("Unknown binary operator: " + op);
        }
    }

    private double toDouble(Object value) throws Exception {
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        throw new Exception("Cannot convert " + value + " to number");
    }

    private boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Double) return ((Double) value) != 0;
        if (value instanceof Integer) return ((Integer) value) != 0;
        return true;
    }

    private Object callNativeFunction(String namespace, String className, String method, List<Object> args) throws Exception {
        if (namespace == null) {
            namespace = "default";
        }

        return nativeFunctionRegistry.call(namespace, className, method, args);
    }

    public ExecutionState getState() {
        return state;
    }

    public void setState(ExecutionState newState) {
        this.state = newState;
    }

    public void setMaxInstructionsPerFrame(int max) {
        this.maxInstructionsPerFrame = max;
    }

    public NativeFunctionRegistry getNativeFunctionRegistry() {
        return nativeFunctionRegistry;
    }

    public void setNativeFunctionRegistry(NativeFunctionRegistry registry) {
        this.nativeFunctionRegistry = registry;
    }

    public BytecodeVM clone() {
        BytecodeVM cloneVM = new BytecodeVM();
        if (instructions == null) instructions = new ArrayList<>();
        cloneVM.instructions = new ArrayList<>(this.instructions);
        if (functions == null) functions = new ArrayList<>();
        cloneVM.functions = new ArrayList<>(this.functions);
        if (state == null) state = new ExecutionState();
        cloneVM.state = state.clone();
        
        return cloneVM;
    }

    // Class-related instruction handlers

    private void executeClassDef(Instruction instr) throws Exception {
        // CLASS_DEF instruction - just metadata, nothing to execute
        // The class metadata is loaded when bytecode is loaded
        // This is a placeholder for future class registration if needed
    }

    private void executeNew(Instruction instr) throws Exception {
        // NEW: Create new instance
        // Stack: [arg1, arg2, ...] -> [instance]
        // instruction.classRef contains the class ID
        // instruction.args contains the argument count

        if (instr.classRef == null) {
            throw new Exception("NEW instruction missing classRef");
        }

        int classId = instr.classRef;
        int argCount = instr.args != null ? instr.args : 0;

        // Pop arguments from stack
        List<Object> args = new ArrayList<>();
        for (int i = 0; i < argCount; i++) {
            args.add(0, state.popStack()); // Reverse order
        }

        // Create new instance
        ObjectInstance instance = new ObjectInstance(classId);

        // Call constructor if it exists (would need class metadata to do this properly)
        // For now, just push the instance
        state.pushStack(instance);
    }

    private void executeThisLoad() throws Exception {
        // THIS_LOAD: Push implicit this reference
        if (state.thisObject == null) {
            throw new Exception("'this' is not available in this context");
        }
        state.pushStack(state.thisObject);
    }

    private void executePropGet(Instruction instr) throws Exception {
        // PROP_GET: Get property from object
        // Stack: [object] -> [value]
        Object obj = state.popStack();

        if (!(obj instanceof ObjectInstance)) {
            throw new Exception("Cannot get property from non-object: " + obj);
        }

        ObjectInstance instance = (ObjectInstance) obj;
        String property = instr.property;

        if (property == null) {
            throw new Exception("PROP_GET instruction missing property name");
        }

        Object value = instance.getProperty(property);
        state.pushStack(value);
    }

    private void executePropSet(Instruction instr) throws Exception {
        // PROP_SET: Set property on object
        // Stack: [object, value] -> []
        Object value = state.popStack();
        Object obj = state.popStack();

        if (!(obj instanceof ObjectInstance)) {
            throw new Exception("Cannot set property on non-object: " + obj);
        }

        ObjectInstance instance = (ObjectInstance) obj;
        String property = instr.property;
        String visibility = instr.visibility;

        if (property == null) {
            throw new Exception("PROP_SET instruction missing property name");
        }

        instance.setProperty(property, value, visibility);
    }

    private void executeMethodCall(Instruction instr) throws Exception {
        // METHOD_CALL: Call instance method with this binding
        // Stack: [this, arg1, arg2, ...] -> [result]
        // This requires class and method metadata - placeholder implementation

        if (instr.args == null) {
            throw new Exception("METHOD_CALL instruction missing args count");
        }

        int argCount = instr.args;

        // Pop arguments
        List<Object> args = new ArrayList<>();
        for (int i = 0; i < argCount; i++) {
            args.add(0, state.popStack()); // Reverse order
        }

        // Pop this object
        Object thisObj = state.popStack();

        if (!(thisObj instanceof ObjectInstance)) {
            throw new Exception("Cannot call method on non-object: " + thisObj);
        }

        // For now, just push null result
        // Full implementation would look up method in class metadata and execute
        state.pushStack(null);
    }

    private void executeStaticCall(Instruction instr) throws Exception {
        // STATIC_CALL: Call static method
        // Stack: [arg1, arg2, ...] -> [result]
        // This requires class and method metadata - placeholder implementation

        if (instr.args == null) {
            throw new Exception("STATIC_CALL instruction missing args count");
        }

        int argCount = instr.args;

        // Pop arguments
        List<Object> args = new ArrayList<>();
        for (int i = 0; i < argCount; i++) {
            args.add(0, state.popStack()); // Reverse order
        }

        // For now, just push null result
        // Full implementation would look up static method in class metadata and execute
        state.pushStack(null);
    }

    private void executeSuperCall(Instruction instr) throws Exception {
        // SUPER_CALL: Call parent class method
        // Stack: [this, arg1, arg2, ...] -> [result]
        // This requires class hierarchy metadata - placeholder implementation

        if (instr.args == null) {
            throw new Exception("SUPER_CALL instruction missing args count");
        }

        int argCount = instr.args;

        // Pop arguments
        List<Object> args = new ArrayList<>();
        for (int i = 0; i < argCount; i++) {
            args.add(0, state.popStack()); // Reverse order
        }

        // Pop this object
        Object thisObj = state.popStack();

        if (!(thisObj instanceof ObjectInstance)) {
            throw new Exception("Cannot call super method on non-object: " + thisObj);
        }

        // For now, just push null result
        // Full implementation would look up method in parent class and execute
        state.pushStack(null);
    }
}
