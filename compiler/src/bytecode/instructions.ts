/**
 * Bytecode instruction types and definitions
 */

export type InstructionOp =
  // Stack operations
  | 'PUSH_CONST'
  | 'POP'
  | 'DUP'

  // Variables
  | 'LOAD_VAR'
  | 'STORE_VAR'
  | 'LOAD_ARG'
  | 'LOAD_LOCAL'

  // Arithmetic
  | 'ADD'
  | 'SUB'
  | 'MUL'
  | 'DIV'
  | 'MOD'

  // Comparison
  | 'EQ'
  | 'NE'
  | 'LT'
  | 'LE'
  | 'GT'
  | 'GE'

  // Logic
  | 'AND'
  | 'OR'
  | 'NOT'

  // Control flow
  | 'JMP'
  | 'JMP_IF'
  | 'JMP_NOT'

  // Function calls
  | 'CALL'
  | 'RETURN'

  // Java interop
  | 'NATIVE_CALL'
  | 'CHECKPOINT'

  // Arrays
  | 'ARRAY_NEW'
  | 'ARRAY_GET'
  | 'ARRAY_SET'

  // Objects
  | 'OBJECT_NEW'
  | 'OBJECT_GET'
  | 'OBJECT_SET';

export interface Instruction {
  op: InstructionOp;
  value?: unknown;           // For PUSH_CONST
  name?: string;             // For LOAD_VAR, STORE_VAR
  index?: number;            // For LOAD_ARG, LOAD_LOCAL
  target?: number;           // For JMP, JMP_IF, JMP_NOT
  function?: number;         // For CALL
  args?: number;             // For CALL, NATIVE_CALL
  method?: string;           // For NATIVE_CALL
  namespace?: string;        // For NATIVE_CALL (e.g., "Entity", "Player", "Math")
  className?: string;        // For NATIVE_CALL (e.g., "EntityUtils", "PlayerManager")
  property?: string;         // For OBJECT_GET, OBJECT_SET
  id?: string;               // For CHECKPOINT
  size?: number;             // For ARRAY_NEW
}

export interface FunctionDef {
  id: number;
  name: string;
  params: string[];
  instructions: Instruction[];
}

export interface BytecodeModule {
  functions: FunctionDef[];
  instructions: Instruction[];
}

export class BytecodeBuilder {
  private functions: Map<string, FunctionDef> = new Map();
  private currentFunctionId = 0;
  private currentInstructions: Instruction[] = [];

  /**
   * Pre-register a function declaration so it can be called before definition is transformed
   * This creates a placeholder that will be updated when endFunction is called
   */
  preFunctionDeclaration(name: string, params: string[]): void {
    if (!this.functions.has(name)) {
      const funcId = this.currentFunctionId++;
      this.functions.set(name, {
        id: funcId,
        name,
        params,
        instructions: [], // Empty for now, will be filled in by endFunction
      });
    }
  }

  startFunction(name: string, params: string[]): void {
    this.currentInstructions = [];
  }

  endFunction(name: string, params: string[]): void {
    let func = this.functions.get(name);
    if (!func) {
      // If not pre-registered, create it now
      const funcId = this.currentFunctionId++;
      func = {
        id: funcId,
        name,
        params,
        instructions: [...this.currentInstructions],
      };
      this.functions.set(name, func);
    } else {
      // Update the pre-registered function with actual instructions
      func.instructions = [...this.currentInstructions];
    }
    this.currentInstructions = [];
  }

  emit(op: InstructionOp, data?: Partial<Instruction>): void {
    this.currentInstructions.push({
      op,
      ...data,
    });
  }

  emitPushConst(value: unknown): void {
    this.emit('PUSH_CONST', { value });
  }

  emitLoadVar(name: string): void {
    this.emit('LOAD_VAR', { name });
  }

  emitStoreVar(name: string): void {
    this.emit('STORE_VAR', { name });
  }

  emitLoadArg(index: number): void {
    this.emit('LOAD_ARG', { index });
  }

  emitBinOp(op: 'ADD' | 'SUB' | 'MUL' | 'DIV' | 'MOD' | 'EQ' | 'NE' | 'LT' | 'LE' | 'GT' | 'GE' | 'AND' | 'OR'): void {
    this.emit(op);
  }

  emitJmp(target: number): void {
    this.emit('JMP', { target });
  }

  emitJmpIf(target: number): void {
    this.emit('JMP_IF', { target });
  }

  emitJmpNot(target: number): void {
    this.emit('JMP_NOT', { target });
  }

  emitCall(functionName: string, args: number): void {
    const func = this.functions.get(functionName);
    if (!func) {
      throw new Error(`Function ${functionName} not found`);
    }
    this.emit('CALL', { function: func.id, args });
  }

  emitNativeCall(method: string, args: number): void {
    this.emit('NATIVE_CALL', { method, args });
  }

  emitNativeCallWithNamespace(namespace: string, method: string, args: number): void {
    this.emit('NATIVE_CALL', { namespace, method, args });
  }

  emitNativeCallWithClass(className: string, method: string, args: number): void {
    this.emit('NATIVE_CALL', { className, method, args });
  }

  emitNativeCallFull(namespace: string, className: string, method: string, args: number): void {
    this.emit('NATIVE_CALL', { namespace, className, method, args });
  }

  emitCheckpoint(id: string): void {
    this.emit('CHECKPOINT', { id });
  }

  emitReturn(): void {
    this.emit('RETURN');
  }

  emitObjectGet(property: string): void {
    this.emit('OBJECT_GET', { property });
  }

  emitObjectSet(property: string): void {
    this.emit('OBJECT_SET', { property });
  }

  emitArrayNew(size: number): void {
    this.emit('ARRAY_NEW', { size });
  }

  emitArrayGet(): void {
    this.emit('ARRAY_GET');
  }

  emitArraySet(): void {
    this.emit('ARRAY_SET');
  }

  getCurrentInstructions(): Instruction[] {
    return this.currentInstructions;
  }

  getInstructionCount(): number {
    return this.currentInstructions.length;
  }

  build(): BytecodeModule {
    return {
      functions: Array.from(this.functions.values()),
      instructions: this.currentInstructions,
    };
  }
}
