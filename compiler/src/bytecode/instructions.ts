/**
 * Bytecode instruction types and definitions
 */

export type InstructionOp =
  // Stack operations
  | 'PUSH_CONST'
  | 'POP'
  | 'DUP'
  | 'SWAP'

  // Variables (original)
  | 'LOAD_VAR'
  | 'STORE_VAR'
  | 'LOAD_ARG'
  | 'LOAD_LOCAL'

  // Variables (new explicit versions)
  | 'DECLARE'        // Declare variable: pops value, stores it, yields nothing
  | 'ASSIGN'         // Assign to variable: pops value, stores it, yields the value
  | 'POSTINC'        // Postfix increment (i++): yields old value, updates variable
  | 'POSTDEC'        // Postfix decrement (i--): yields old value, updates variable
  | 'PREINC'         // Prefix increment (++i): yields new value, updates variable
  | 'PREDEC'         // Prefix decrement (--i): yields new value, updates variable

  // Scope management
  | 'SCOPE_PUSH'     // Push new scope
  | 'SCOPE_POP'      // Pop scope (clean up locals)

  // Control flow - loops
  | 'BREAK'          // Break from loop
  | 'CONTINUE'       // Continue to next iteration

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
  | 'OBJECT_SET'

  // Classes
  | 'CLASS_DEF'      // Define class metadata
  | 'NEW'            // Create new instance
  | 'THIS_LOAD'      // Load implicit this reference
  | 'PROP_GET'       // Get instance property
  | 'PROP_SET'       // Set instance property
  | 'METHOD_CALL'    // Call instance method with this binding
  | 'STATIC_CALL'    // Call static method
  | 'SUPER_CALL';    // Call parent class method

/**
 * Stack semantics for each instruction
 * Defines how many values are consumed and produced
 */
export const InstructionSemantics: {[key in InstructionOp]: {consumes: number, yields: boolean, description: string}} = {
  // Stack operations
  'PUSH_CONST': {consumes: 0, yields: true, description: 'Push constant onto stack'},
  'POP': {consumes: 1, yields: false, description: 'Pop value from stack'},
  'DUP': {consumes: 1, yields: true, description: 'Duplicate top of stack (consumes and yields same value)'},
  'SWAP': {consumes: 2, yields: true, description: 'Swap top two stack values'},

  // Variables
  'LOAD_VAR': {consumes: 0, yields: true, description: 'Load variable value onto stack'},
  'STORE_VAR': {consumes: 1, yields: false, description: 'Pop value and store to variable'},
  'LOAD_ARG': {consumes: 0, yields: true, description: 'Load function argument onto stack'},
  'LOAD_LOCAL': {consumes: 0, yields: true, description: 'Load local variable onto stack'},

  // New explicit variable operations
  'DECLARE': {consumes: 1, yields: false, description: 'Declare variable and initialize (pops value)'},
  'ASSIGN': {consumes: 1, yields: true, description: 'Assign to variable (pops value, yields it)'},
  'POSTINC': {consumes: 0, yields: true, description: 'Postfix increment (yields old value)'},
  'POSTDEC': {consumes: 0, yields: true, description: 'Postfix decrement (yields old value)'},
  'PREINC': {consumes: 0, yields: true, description: 'Prefix increment (yields new value)'},
  'PREDEC': {consumes: 0, yields: true, description: 'Prefix decrement (yields new value)'},

  // Scope management
  'SCOPE_PUSH': {consumes: 0, yields: false, description: 'Push new scope'},
  'SCOPE_POP': {consumes: 0, yields: false, description: 'Pop scope'},

  // Control flow - loops
  'BREAK': {consumes: 0, yields: false, description: 'Break from loop'},
  'CONTINUE': {consumes: 0, yields: false, description: 'Continue to next iteration'},

  // Arithmetic
  'ADD': {consumes: 2, yields: true, description: 'Pop two values, push sum'},
  'SUB': {consumes: 2, yields: true, description: 'Pop two values, push difference'},
  'MUL': {consumes: 2, yields: true, description: 'Pop two values, push product'},
  'DIV': {consumes: 2, yields: true, description: 'Pop two values, push quotient'},
  'MOD': {consumes: 2, yields: true, description: 'Pop two values, push modulo'},

  // Comparison
  'EQ': {consumes: 2, yields: true, description: 'Pop two values, push equality result'},
  'NE': {consumes: 2, yields: true, description: 'Pop two values, push inequality result'},
  'LT': {consumes: 2, yields: true, description: 'Pop two values, push less-than result'},
  'LE': {consumes: 2, yields: true, description: 'Pop two values, push less-equal result'},
  'GT': {consumes: 2, yields: true, description: 'Pop two values, push greater-than result'},
  'GE': {consumes: 2, yields: true, description: 'Pop two values, push greater-equal result'},

  // Logic
  'AND': {consumes: 2, yields: true, description: 'Pop two values, push logical AND result'},
  'OR': {consumes: 2, yields: true, description: 'Pop two values, push logical OR result'},
  'NOT': {consumes: 1, yields: true, description: 'Pop value, push logical NOT result'},

  // Control flow
  'JMP': {consumes: 0, yields: false, description: 'Jump to target'},
  'JMP_IF': {consumes: 1, yields: false, description: 'Pop condition, jump if true'},
  'JMP_NOT': {consumes: 1, yields: false, description: 'Pop condition, jump if false'},

  // Function calls
  'CALL': {consumes: -1, yields: true, description: 'Call function (consumes args, yields result)'},
  'RETURN': {consumes: 1, yields: false, description: 'Return from function'},

  // Java interop
  'NATIVE_CALL': {consumes: -1, yields: true, description: 'Call native Java function'},
  'CHECKPOINT': {consumes: 0, yields: false, description: 'Pause execution at checkpoint'},

  // Arrays
  'ARRAY_NEW': {consumes: 0, yields: true, description: 'Create new array'},
  'ARRAY_GET': {consumes: 2, yields: true, description: 'Pop index and array, push element'},
  'ARRAY_SET': {consumes: 3, yields: false, description: 'Pop array, index, value; set element'},

  // Objects
  'OBJECT_NEW': {consumes: 0, yields: true, description: 'Create new object'},
  'OBJECT_GET': {consumes: 1, yields: true, description: 'Pop object, push property value'},
  'OBJECT_SET': {consumes: 2, yields: false, description: 'Pop object and value, set property'},

  // Classes
  'CLASS_DEF': {consumes: 0, yields: false, description: 'Define class metadata'},
  'NEW': {consumes: -1, yields: true, description: 'Create new instance (consumes args, yields instance)'},
  'THIS_LOAD': {consumes: 0, yields: true, description: 'Load implicit this reference'},
  'PROP_GET': {consumes: 1, yields: true, description: 'Pop object, push property value'},
  'PROP_SET': {consumes: 2, yields: false, description: 'Pop object and value, set property'},
  'METHOD_CALL': {consumes: -1, yields: true, description: 'Call instance method with this binding'},
  'STATIC_CALL': {consumes: -1, yields: true, description: 'Call static method'},
  'SUPER_CALL': {consumes: -1, yields: true, description: 'Call parent class method'},
};

export interface Instruction {
  op: InstructionOp;
  value?: unknown;           // For PUSH_CONST
  name?: string;             // For LOAD_VAR, STORE_VAR, DECLARE, ASSIGN, POSTINC, etc.
  index?: number;            // For LOAD_ARG, LOAD_LOCAL
  target?: number;           // For JMP, JMP_IF, JMP_NOT
  function?: number;         // For CALL
  args?: number;             // For CALL, NATIVE_CALL
  method?: string;           // For NATIVE_CALL
  namespace?: string;        // For NATIVE_CALL (e.g., "Entity", "Player", "Math")
  className?: string;        // For NATIVE_CALL (e.g., "EntityUtils", "PlayerManager")
  property?: string;         // For OBJECT_GET, OBJECT_SET, PROP_GET, PROP_SET
  id?: string;               // For CHECKPOINT
  size?: number;             // For ARRAY_NEW
  classRef?: number;         // For NEW, THIS_LOAD (class id)
  methodRef?: number;        // For METHOD_CALL, STATIC_CALL, SUPER_CALL (method id)
  visibility?: 'public' | 'private';  // For properties and methods
  isStatic?: boolean;        // For METHOD_CALL, STATIC_CALL
  isGetter?: boolean;        // For properties and method declarations
  isSetter?: boolean;        // For properties and method declarations
}

export interface FunctionDef {
  id: number;
  name: string;
  params: string[];
  instructions: Instruction[];
}

export interface ClassProperty {
  name: string;
  visibility: 'public' | 'private';
  isGetter: boolean;
  isSetter: boolean;
  initializer?: Instruction[];  // Instructions to initialize property
}

export interface MethodDef {
  id: number;
  name: string;
  visibility: 'public' | 'private';
  isStatic: boolean;
  params: string[];
  instructions: Instruction[];
  isGetter?: boolean;
  isSetter?: boolean;
}

export interface ClassDef {
  id: number;
  name: string;
  superclass?: string;  // Name of parent class
  properties: ClassProperty[];
  methods: MethodDef[];
  staticMethods: MethodDef[];
}

export interface BytecodeModule {
  functions: FunctionDef[];
  classes: ClassDef[];
  instructions: Instruction[];
}

export class BytecodeBuilder {
  private functions: Map<string, FunctionDef> = new Map();
  private classes: Map<string, ClassDef> = new Map();
  private currentFunctionId = 0;
  private currentClassId = 0;
  private currentMethodId = 0;
  private currentInstructions: Instruction[] = [];
  private currentClass: ClassDef | null = null;
  private currentMethod: MethodDef | null = null;

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

  // New explicit variable operation methods
  emitDeclare(name: string): void {
    this.emit('DECLARE', { name });
  }

  emitAssign(name: string): void {
    this.emit('ASSIGN', { name });
  }

  emitPostInc(name: string): void {
    this.emit('POSTINC', { name });
  }

  emitPostDec(name: string): void {
    this.emit('POSTDEC', { name });
  }

  emitPreInc(name: string): void {
    this.emit('PREINC', { name });
  }

  emitPreDec(name: string): void {
    this.emit('PREDEC', { name });
  }

  emitScopePush(): void {
    this.emit('SCOPE_PUSH');
  }

  emitScopePop(): void {
    this.emit('SCOPE_POP');
  }

  emitBreak(): void {
    this.emit('BREAK');
  }

  emitContinue(): void {
    this.emit('CONTINUE');
  }

  emitSwap(): void {
    this.emit('SWAP');
  }

  // Class-related methods
  startClass(name: string, superclass?: string): void {
    const classId = this.currentClassId++;
    this.currentClass = {
      id: classId,
      name,
      superclass,
      properties: [],
      methods: [],
      staticMethods: [],
    };
    this.classes.set(name, this.currentClass);
  }

  declareProperty(name: string, visibility: 'public' | 'private' = 'public', isGetter: boolean = false, isSetter: boolean = false): void {
    if (!this.currentClass) {
      throw new Error('Cannot declare property outside of class');
    }
    this.currentClass.properties.push({
      name,
      visibility,
      isGetter,
      isSetter,
    });
  }

  startMethod(name: string, visibility: 'public' | 'private' = 'public', isStatic: boolean = false, params: string[] = [], isGetter: boolean = false, isSetter: boolean = false): void {
    if (!this.currentClass) {
      throw new Error('Cannot declare method outside of class');
    }
    const methodId = this.currentMethodId++;
    this.currentMethod = {
      id: methodId,
      name,
      visibility,
      isStatic,
      params,
      instructions: [],
      isGetter,
      isSetter,
    };
    this.currentInstructions = [];
  }

  endMethod(): void {
    if (!this.currentMethod || !this.currentClass) {
      throw new Error('Cannot end method - not in method context');
    }

    this.currentMethod.instructions = [...this.currentInstructions];

    if (this.currentMethod.isStatic) {
      this.currentClass.staticMethods.push(this.currentMethod);
    } else {
      this.currentClass.methods.push(this.currentMethod);
    }

    this.currentMethod = null;
    this.currentInstructions = [];
  }

  endClass(): void {
    this.currentClass = null;
  }

  emitClassDef(name: string): void {
    const classDef = this.classes.get(name);
    if (!classDef) {
      throw new Error(`Class ${name} not found`);
    }
    this.emit('CLASS_DEF', { classRef: classDef.id, name });
  }

  emitNew(className: string, args: number): void {
    const classDef = this.classes.get(className);
    if (!classDef) {
      throw new Error(`Class ${className} not found`);
    }
    this.emit('NEW', { classRef: classDef.id, args });
  }

  emitThisLoad(): void {
    this.emit('THIS_LOAD');
  }

  emitPropGet(property: string): void {
    this.emit('PROP_GET', { property });
  }

  emitPropSet(property: string): void {
    this.emit('PROP_SET', { property });
  }

  emitMethodCall(className: string, methodName: string, args: number): void {
    const classDef = this.classes.get(className);
    if (!classDef) {
      throw new Error(`Class ${className} not found`);
    }
    const method = classDef.methods.find(m => m.name === methodName);
    if (!method) {
      throw new Error(`Method ${methodName} not found in class ${className}`);
    }
    this.emit('METHOD_CALL', { classRef: classDef.id, methodRef: method.id, args });
  }

  emitStaticCall(className: string, methodName: string, args: number): void {
    const classDef = this.classes.get(className);
    if (!classDef) {
      throw new Error(`Class ${className} not found`);
    }
    const method = classDef.staticMethods.find(m => m.name === methodName);
    if (!method) {
      throw new Error(`Static method ${methodName} not found in class ${className}`);
    }
    this.emit('STATIC_CALL', { classRef: classDef.id, methodRef: method.id, args });
  }

  emitSuperCall(superclassName: string, methodName: string, args: number): void {
    // This will be resolved at runtime based on class hierarchy
    this.emit('SUPER_CALL', { className: superclassName, method: methodName, args });
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
      classes: Array.from(this.classes.values()),
      instructions: this.currentInstructions,
    };
  }
}
