import {
  SourceFile,
  Node,
  SyntaxKind,
  VariableDeclaration,
  FunctionDeclaration,
  ClassDeclaration,
  MethodDeclaration,
  PropertyDeclaration,
  GetAccessorDeclaration,
  SetAccessorDeclaration,
  ConstructorDeclaration,
  Block,
  IfStatement,
  WhileStatement,
  ForStatement,
  ReturnStatement,
  ExpressionStatement,
  BinaryExpression,
  CallExpression,
  Identifier,
  NumericLiteral,
  StringLiteral,
  UnaryExpression,
  PostfixUnaryExpression,
  PropertyAccessExpression,
  ArrayLiteralExpression,
  ObjectLiteralExpression,
  NewExpression,
} from 'ts-morph';
import { BytecodeBuilder, BytecodeModule, Instruction } from './instructions.js';
import { ScopeStack, Variable } from './scoping.js';

/**
 * Transformation context - determines how expressions are treated
 */
type TransformContext = 'statement' | 'expression';

/**
 * Transforms TypeScript AST to custom bytecode
 * Uses explicit scope management and context-aware expression handling
 */
export class BytecodeTransformer {
  private builder = new BytecodeBuilder();
  private scopes = new ScopeStack();
  private loopDepth = 0;
  private breakTargets: number[] = [];      // Stack of break jump targets
  private continueTargets: number[] = [];   // Stack of continue jump targets

  transform(sourceFile: SourceFile): BytecodeModule {
    // First pass: collect and pre-register all class declarations
    const classes = sourceFile.getClasses();
    for (const cls of classes) {
      const name = cls.getName() || '';
      const superclass = cls.getExtends()?.getExpression().getText() || undefined;
      this.scopes.declare(name, 'class');  // Register class name in scope
      this.builder.startClass(name, superclass);
      this.builder.endClass();  // Placeholder - will be replaced in transformClassDeclaration
    }

    // Second pass: collect and pre-register all function declarations
    const functions = sourceFile.getFunctions();
    for (const func of functions) {
      const name = func.getName() || '';
      const params = func.getParameters().map((p) => p.getName());
      this.scopes.declare(name, 'function');
      // Pre-register function in builder so it can be called before it's defined
      this.builder.preFunctionDeclaration(name, params);
    }

    // Third pass: transform each statement
    const statements = sourceFile.getStatements();
    for (const stmt of statements) {
      this.transformStatement(stmt);
    }

    return this.builder.build();
  }

  private transformStatement(node: Node): void {
    if (Node.isVariableStatement(node)) {
      this.transformVariableStatement(node);
    } else if (Node.isClassDeclaration(node)) {
      this.transformClassDeclaration(node);
    } else if (Node.isFunctionDeclaration(node)) {
      this.transformFunctionDeclaration(node);
    } else if (Node.isIfStatement(node)) {
      this.transformIfStatement(node);
    } else if (Node.isWhileStatement(node)) {
      this.transformWhileStatement(node);
    } else if (Node.isDoStatement(node)) {
      this.transformDoWhileStatement(node);
    } else if (Node.isForStatement(node)) {
      this.transformForStatement(node);
    } else if (Node.isReturnStatement(node)) {
      this.transformReturnStatement(node);
    } else if (Node.isBreakStatement(node)) {
      this.transformBreakStatement(node);
    } else if (Node.isContinueStatement(node)) {
      this.transformContinueStatement(node);
    } else if (Node.isExpressionStatement(node)) {
      const expr = node.getExpression();
      this.transformExpression(expr, 'statement');
      // Pop result from stack if not used (in statement context)
      this.builder.emit('POP');
    } else if (Node.isBlock(node)) {
      // Don't create a new scope for blocks in statements - caller handles it
      const statements = node.getStatements();
      for (const stmt of statements) {
        this.transformStatement(stmt);
      }
    }
  }

  private transformVariableStatement(node: Node): void {
    const varStmt = node as any;
    const declarations = varStmt.getDeclarations ? varStmt.getDeclarations() : [];

    for (const decl of declarations) {
      const name = decl.getName ? decl.getName() : '';
      
      // Declare in current scope
      this.scopes.declare(name, 'var');

      if (decl.getInitializer) {
        const initializer = decl.getInitializer();
        if (initializer) {
          // Transform initializer as expression
          this.transformExpression(initializer, 'expression');
          // Emit DECLARE to pop value and store it
          this.builder.emitDeclare(name);
        }
      }
    }
  }

  private transformFunctionDeclaration(node: Node): void {
    const func = node as FunctionDeclaration;
    const name = func.getName() || 'anonymous';
    const params = func.getParameters().map((p) => p.getName());

    this.builder.startFunction(name, params);

    // Push new scope for function body
    this.scopes.pushScope();

    // Add parameters to scope
    for (const param of params) {
      this.scopes.declare(param, 'param');
    }

    const body = func.getBody();
    if (body) {
      if (Node.isBlock(body)) {
        const statements = body.getStatements();
        for (const stmt of statements) {
          this.transformStatement(stmt);
        }
      }
    }

    this.builder.emitReturn();
    
    // Pop function scope
    this.scopes.popScope();
    
    this.builder.endFunction(name, params);
  }

  private transformClassDeclaration(node: Node): void {
    const classDec = node as ClassDeclaration;
    const className = classDec.getName() || 'AnonymousClass';
    const superclass = classDec.getExtends()?.getExpression().getText() || undefined;

    // Start the class
    this.builder.startClass(className, superclass);

    // Create new scope for class members
    this.scopes.pushScope();

    // Process properties
    try {
      const properties = classDec.getProperties();
      for (const prop of properties) {
        const propName = prop.getName() || '';
        const modifiers = prop.getModifiers();
        const visibility = modifiers.some(m => m.getText() === 'private') ? 'private' : 'public';
        const isReadonly = modifiers.some(m => m.getText() === 'readonly');

        // Declare property in scope
        this.scopes.declare(propName, 'var');

        // Add property to class
        this.builder.declareProperty(propName, visibility, false, false);
      }
    } catch (e) {
      // Skip properties that can't be processed
    }

    // Process constructor
    const constructor = classDec.getConstructors()[0];
    if (constructor) {
      this.transformConstructor(constructor, className);
    }

    // Process methods
    try {
      const methods = classDec.getMethods();
      for (const method of methods) {
        const methodName = method.getName() || '';
        const modifiers = method.getModifiers();
        const visibility = modifiers.some(m => m.getText() === 'private') ? 'private' : 'public';
        const isStatic = modifiers.some(m => m.getText() === 'static');

        this.transformMethod(method, methodName, visibility, isStatic);
      }
    } catch (e) {
      // Skip methods that can't be processed
    }

    // Process getters
    try {
      const getters = classDec.getGetAccessors?.() || [];
      for (const getter of getters) {
        const getterName = getter.getName() || '';
        const modifiers = getter.getModifiers();
        const visibility = modifiers.some(m => m.getText() === 'private') ? 'private' : 'public';
        const isStatic = modifiers.some(m => m.getText() === 'static');

        this.transformGetter(getter, getterName, visibility, isStatic);
      }
    } catch (e) {
      // Getters not supported in this version of ts-morph, skip silently
    }

    // Process setters
    try {
      const setters = classDec.getSetAccessors?.() || [];
      for (const setter of setters) {
        const setterName = setter.getName() || '';
        const modifiers = setter.getModifiers();
        const visibility = modifiers.some(m => m.getText() === 'private') ? 'private' : 'public';
        const isStatic = modifiers.some(m => m.getText() === 'static');

        this.transformSetter(setter, setterName, visibility, isStatic);
      }
    } catch (e) {
      // Setters not supported in this version of ts-morph, skip silently
    }

    // Close class scope
    this.scopes.popScope();

    // End class definition
    this.builder.endClass();
  }

  private transformConstructor(constructor: ConstructorDeclaration, className: string): void {
    const params = constructor.getParameters().map((p) => p.getName());

    // Start constructor method (special method with name "constructor")
    this.builder.startMethod('constructor', 'public', false, params, false, false);

    // Push new scope for constructor body
    this.scopes.pushScope();

    // Add parameters to scope
    for (const param of params) {
      this.scopes.declare(param, 'param');
    }

    // Add 'this' to scope
    this.scopes.declare('this', 'var');

    // Transform constructor body
    const body = constructor.getBody();
    if (body && Node.isBlock(body)) {
      const statements = body.getStatements();
      for (const stmt of statements) {
        this.transformStatement(stmt);
      }
    }

    // Implicit return for constructor
    this.builder.emitReturn();

    // Pop constructor scope
    this.scopes.popScope();

    // End constructor method
    this.builder.endMethod();
  }

  private transformMethod(method: MethodDeclaration, methodName: string, visibility: 'public' | 'private', isStatic: boolean): void {
    const params = method.getParameters().map((p) => p.getName());

    // Start method
    this.builder.startMethod(methodName, visibility, isStatic, params, false, false);

    // Push new scope for method body
    this.scopes.pushScope();

    // Add parameters to scope
    for (const param of params) {
      this.scopes.declare(param, 'param');
    }

    // Add 'this' to scope (unless static)
    if (!isStatic) {
      this.scopes.declare('this', 'var');
    }

    // Transform method body
    const body = method.getBody();
    if (body && Node.isBlock(body)) {
      const statements = body.getStatements();
      for (const stmt of statements) {
        this.transformStatement(stmt);
      }
    }

    // Implicit return for method
    this.builder.emitReturn();

    // Pop method scope
    this.scopes.popScope();

    // End method
    this.builder.endMethod();
  }

  private transformGetter(getter: GetAccessorDeclaration, getterName: string, visibility: 'public' | 'private', isStatic: boolean): void {
    // Getters are treated as methods with no parameters
    const params: string[] = [];

    // Start getter method
    this.builder.startMethod(getterName, visibility, isStatic, params, true, false);

    // Push new scope for getter body
    this.scopes.pushScope();

    // Add 'this' to scope (unless static)
    if (!isStatic) {
      this.scopes.declare('this', 'var');
    }

    // Transform getter body
    const body = getter.getBody();
    if (body && Node.isBlock(body)) {
      const statements = body.getStatements();
      for (const stmt of statements) {
        this.transformStatement(stmt);
      }
    }

    // Implicit return for getter
    this.builder.emitReturn();

    // Pop getter scope
    this.scopes.popScope();

    // End getter method
    this.builder.endMethod();
  }

  private transformSetter(setter: SetAccessorDeclaration, setterName: string, visibility: 'public' | 'private', isStatic: boolean): void {
    // Setters take one parameter (the value)
    const params = setter.getParameters().map((p) => p.getName());

    // Start setter method
    this.builder.startMethod(setterName, visibility, isStatic, params, false, true);

    // Push new scope for setter body
    this.scopes.pushScope();

    // Add parameters to scope
    for (const param of params) {
      this.scopes.declare(param, 'param');
    }

    // Add 'this' to scope (unless static)
    if (!isStatic) {
      this.scopes.declare('this', 'var');
    }

    // Transform setter body
    const body = setter.getBody();
    if (body && Node.isBlock(body)) {
      const statements = body.getStatements();
      for (const stmt of statements) {
        this.transformStatement(stmt);
      }
    }

    // Implicit return for setter (return nothing/undefined)
    this.builder.emitPushConst(undefined);
    this.builder.emitReturn();

    // Pop setter scope
    this.scopes.popScope();

    // End setter method
    this.builder.endMethod();
  }

  private transformIfStatement(node: Node): void {
    const ifStmt = node as IfStatement;
    const condition = ifStmt.getExpression();

    // Evaluate condition
    this.transformExpression(condition, 'expression');

    // Get current instruction count to calculate jump target
    const jmpNotAddr = this.builder.getInstructionCount();
    this.builder.emitJmpNot(0); // Placeholder, will be patched

    // Transform then branch
    const thenStmt = ifStmt.getThenStatement();
    this.transformStatement(thenStmt);

    const elseStmt = ifStmt.getElseStatement();
    if (elseStmt) {
      const jmpAddr = this.builder.getInstructionCount();
      const instructions = this.builder.getCurrentInstructions();
      instructions[jmpNotAddr].target = jmpAddr + 1; // Patch the JMP_NOT to skip else

      this.builder.emitJmp(0); // Placeholder for jump over else
      const jmpElseAddr = this.builder.getInstructionCount() - 1;

      // Transform else branch
      this.transformStatement(elseStmt);

      // Patch the else jump
      instructions[jmpElseAddr].target = this.builder.getInstructionCount();
    } else {
      const instructions = this.builder.getCurrentInstructions();
      instructions[jmpNotAddr].target = this.builder.getInstructionCount();
    }
  }

  private transformWhileStatement(node: Node): void {
    const whileStmt = node as WhileStatement;

    const loopStart = this.builder.getInstructionCount();

    // Evaluate condition
    const condition = whileStmt.getExpression();
    this.transformExpression(condition, 'expression');

    // Jump if condition is false
    const jmpNotAddr = this.builder.getInstructionCount();
    this.builder.emitJmpNot(0); // Placeholder

    // Transform body
    const body = whileStmt.getStatement();
    this.transformStatement(body);

    // Jump back to start
    this.builder.emitJmp(loopStart);

    // Patch the JMP_NOT
    const instructions = this.builder.getCurrentInstructions();
    instructions[jmpNotAddr].target = this.builder.getInstructionCount();
  }

  private transformDoWhileStatement(node: Node): void {
    const doStmt = node as any;

    const loopStart = this.builder.getInstructionCount();

    // Transform body
    const body = doStmt.getStatement();
    this.transformStatement(body);

    // Evaluate condition
    const condition = doStmt.getExpression();
    this.transformExpression(condition, 'expression');

    // Jump back if true
    this.builder.emitJmpIf(loopStart);
  }

  private transformBreakStatement(node: Node): void {
    if (this.breakTargets.length === 0) {
      throw new Error('Break statement outside of loop');
    }
    // Emit BREAK instruction with the target loop end
    this.builder.emitBreak();
  }

  private transformContinueStatement(node: Node): void {
    if (this.continueTargets.length === 0) {
      throw new Error('Continue statement outside of loop');
    }
    // Emit CONTINUE instruction
    this.builder.emitContinue();
  }

  private transformForStatement(node: Node): void {
    const forStmt = node as ForStatement;

    // Push new scope for for loop (so loop variable doesn't leak out)
    this.scopes.pushScope();

    // Get the initializer
    const initializer = forStmt.getInitializer();
    
    // Handle initializer - could be either an expression or variable declaration list
    if (initializer) {
      // Check the node kind to determine if it's a variable declaration list
      const kind = initializer.getKind?.();
      const isVariableDeclarationList = kind === SyntaxKind.VariableDeclarationList;
      
      if (isVariableDeclarationList) {
        // Handle variable declaration list - cast and get declarations
        const declList = initializer as any;
        const declarations = declList.getDeclarations?.() || [];
        
        for (const decl of declarations) {
          const name = decl.getName?.() || '';
          if (name) {
            // Declare in current scope
            this.scopes.declare(name, 'var');

            const declInit = decl.getInitializer?.();
            if (declInit) {
              this.transformExpression(declInit, 'expression');
              this.builder.emitDeclare(name);
            } else {
              // If no initializer, push null and declare
              this.builder.emitPushConst(null);
              this.builder.emitDeclare(name);
            }
          }
        }
      } else {
        // Handle as expression (assignment like i = 0)
        this.transformExpression(initializer, 'expression');
        this.builder.emit('POP');
      }
    }

    const loopStart = this.builder.getInstructionCount();

    // Evaluate condition
    const condition = forStmt.getCondition();
    if (condition) {
      this.transformExpression(condition, 'expression');
      const jmpNotAddr = this.builder.getInstructionCount();
      this.builder.emitJmpNot(0); // Placeholder

      // Transform body
      const body = forStmt.getStatement();
      this.transformStatement(body);

      // Transform incrementor
      const incrementor = forStmt.getIncrementor();
      if (incrementor) {
        this.transformExpression(incrementor, 'expression');
        this.builder.emit('POP');
      }

      // Jump back
      this.builder.emitJmp(loopStart);

      // Patch JMP_NOT
      const instructions = this.builder.getCurrentInstructions();
      instructions[jmpNotAddr].target = this.builder.getInstructionCount();
    }

    // Pop scope
    this.scopes.popScope();
  }

  private transformReturnStatement(node: Node): void {
    const retStmt = node as ReturnStatement;
    const expression = retStmt.getExpression();

    if (expression) {
      this.transformExpression(expression, 'expression');
    } else {
      this.builder.emitPushConst(null);
    }

    this.builder.emitReturn();
  }

  private transformExpression(node: Node, context: TransformContext = 'expression'): void {
    if (Node.isBinaryExpression(node)) {
      this.transformBinaryExpression(node as BinaryExpression, context);
    } else if (Node.isCallExpression(node)) {
      this.transformCallExpression(node as CallExpression);
    } else if (Node.isNewExpression(node)) {
      this.transformNewExpression(node as NewExpression);
    } else if (Node.isConditionalExpression(node)) {
      this.transformConditionalExpression(node as any);
    } else if (Node.isIdentifier(node)) {
      const id = node as Identifier;
      const name = id.getText();

      // Handle 'this' specially
      if (name === 'this') {
        this.builder.emitThisLoad();
        return;
      }
      
      // Check if variable is defined
      if (!this.scopes.isDefined(name)) {
        throw new Error(`Undefined variable: ${name}`);
      }
      
      this.builder.emitLoadVar(name);
    } else if (Node.isNumericLiteral(node)) {
      const lit = node as NumericLiteral;
      this.builder.emitPushConst(parseFloat(lit.getText()));
    } else if (Node.isStringLiteral(node)) {
      const lit = node as StringLiteral;
      this.builder.emitPushConst(lit.getLiteralValue());
    } else if (Node.isTemplateExpression(node)) {
      this.transformTemplateExpression(node as any);
    } else if (Node.isPropertyAccessExpression(node)) {
      const prop = node as PropertyAccessExpression;
      this.transformExpression(prop.getExpression(), context);
      const propName = prop.getName();
      this.builder.emitPropGet(propName);
    } else if (Node.isPostfixUnaryExpression(node)) {
      // Handle postfix operators (i++, i--)
      this.transformPostfixUnaryExpression(node as PostfixUnaryExpression);
    } else if (Node.isUnaryExpression(node)) {
      // Handle prefix operators (++i, --i, !, -)
      this.transformUnaryExpression(node as UnaryExpression);
    } else if (Node.isArrayLiteralExpression(node)) {
      const arrLit = node as ArrayLiteralExpression;
      const elements = arrLit.getElements();
      this.builder.emitArrayNew(elements.length);
      
      for (let i = 0; i < elements.length; i++) {
        this.transformExpression(elements[i], 'expression');
        // Store element at index i
        this.builder.emitPushConst(i);
        this.builder.emitArraySet();
      }
    }
  }

  private transformUnaryExpression(node: UnaryExpression): void {
    const text = node.getText();
    // Get operand using getChildren() - typically the last child is the operand
    const children = node.getChildren();
    const operand = children.length > 1 ? children[children.length - 1] : children[0];
    const varName = operand.getText();

    // Check if it's ++/-- operator
    if (text.startsWith('++') || text.startsWith('--')) {
      const isIncrement = text.startsWith('++');
      
      // Check if variable is defined
      if (!this.scopes.isDefined(varName)) {
        throw new Error(`Undefined variable: ${varName}`);
      }

      // Load current value
      this.builder.emitLoadVar(varName);
      // Push 1
      this.builder.emitPushConst(1);
      // Add or subtract
      if (isIncrement) {
        this.builder.emitBinOp('ADD');
      } else {
        this.builder.emitBinOp('SUB');
      }
      // DUP the new value for prefix (yields new value)
      this.builder.emit('DUP');
      // Assign back to variable (pops value, yields it)
      this.builder.emitAssign(varName);
    } else if (text.startsWith('!')) {
      // Logical NOT
      this.transformExpression(operand, 'expression');
      // NOT isn't implemented yet, skip for now
    } else if (text.startsWith('-')) {
      // Negation
      this.transformExpression(operand, 'expression');
      this.builder.emitPushConst(-1);
      this.builder.emitBinOp('MUL');
    } else if (text.startsWith('+')) {
      // Unary plus (just evaluate)
      this.transformExpression(operand, 'expression');
    }
  }

  private transformPostfixUnaryExpression(node: PostfixUnaryExpression): void {
    const text = node.getText();
    const operand = node.getOperand();
    const varName = operand.getText();

    // Check if it's ++/-- operator
    if (text.endsWith('++') || text.endsWith('--')) {
      const isIncrement = text.endsWith('++');
      
      // Check if variable is defined
      if (!this.scopes.isDefined(varName)) {
        throw new Error(`Undefined variable: ${varName}`);
      }

      // Load current value
      this.builder.emitLoadVar(varName);
      // DUP for postfix (preserve old value)
      this.builder.emit('DUP');
      // Push 1
      this.builder.emitPushConst(1);
      // Add or subtract
      if (isIncrement) {
        this.builder.emitBinOp('ADD');
      } else {
        this.builder.emitBinOp('SUB');
      }
      // Assign back to variable (pops value, yields it)
      this.builder.emitAssign(varName);
      // For postfix, we now have [old_value, new_value] on stack
      // We want to return old value, so swap and pop
      this.builder.emit('SWAP');
      this.builder.emit('POP');
    }
  }

  private transformBinaryExpression(node: BinaryExpression, context: TransformContext = 'expression'): void {
    const left = node.getLeft();
    const right = node.getRight();
    const operator = node.getOperatorToken().getKind();

    // Special handling for assignment - don't transform left side as expression
    if (operator === SyntaxKind.EqualsToken) {
      // Check if variable is defined
      if (Node.isIdentifier(left)) {
        const name = (left as Identifier).getText();
        if (!this.scopes.isDefined(name)) {
          throw new Error(`Undefined variable: ${name}`);
        }
      }

      // Transform right side to get value
      this.transformExpression(right, 'expression');

      // Store to left side variable
      if (Node.isIdentifier(left)) {
        const name = (left as Identifier).getText();
        // Use ASSIGN instead of STORE_VAR (pops value, yields it)
        this.builder.emitAssign(name);
      }
      return;
    }

    // For all other operators, transform both sides normally
    this.transformExpression(left, 'expression');
    this.transformExpression(right, 'expression');

    switch (operator) {
      case SyntaxKind.PlusToken:
        this.builder.emitBinOp('ADD');
        break;
      case SyntaxKind.MinusToken:
        this.builder.emitBinOp('SUB');
        break;
      case SyntaxKind.AsteriskToken:
        this.builder.emitBinOp('MUL');
        break;
      case SyntaxKind.SlashToken:
        this.builder.emitBinOp('DIV');
        break;
      case SyntaxKind.PercentToken:
        this.builder.emitBinOp('MOD');
        break;
      case SyntaxKind.EqualsEqualsToken:
      case SyntaxKind.EqualsEqualsEqualsToken:
        this.builder.emitBinOp('EQ');
        break;
      case SyntaxKind.ExclamationEqualsToken:
      case SyntaxKind.ExclamationEqualsEqualsToken:
        this.builder.emitBinOp('NE');
        break;
      case SyntaxKind.LessThanToken:
        this.builder.emitBinOp('LT');
        break;
      case SyntaxKind.LessThanEqualsToken:
        this.builder.emitBinOp('LE');
        break;
      case SyntaxKind.GreaterThanToken:
        this.builder.emitBinOp('GT');
        break;
      case SyntaxKind.GreaterThanEqualsToken:
        this.builder.emitBinOp('GE');
        break;
      case SyntaxKind.AmpersandAmpersandToken:
        this.builder.emitBinOp('AND');
        break;
      case SyntaxKind.BarBarToken:
        this.builder.emitBinOp('OR');
        break;
    }
  }

  private transformCallExpression(node: CallExpression): void {
    const callee = node.getExpression();
    const args = node.getArguments();

    // Push arguments onto stack
    for (const arg of args) {
      this.transformExpression(arg, 'expression');
    }

    // Handle different call types
    if (Node.isIdentifier(callee)) {
      const funcName = (callee as Identifier).getText();

      // Check if it's a native function (print, etc.)
      if (this.isNativeFunction(funcName)) {
        this.builder.emitNativeCall(funcName, args.length);
      } else {
        this.builder.emitCall(funcName, args.length);
      }
    } else if (Node.isPropertyAccessExpression(callee)) {
      // Handle object.method() or namespace.method() calls
      const propAccess = callee as PropertyAccessExpression;
      const object = propAccess.getExpression();
      const methodName = propAccess.getName();
      const objectText = object.getText();
      
      // Check if it's a namespace.method() call (Logger.debug, Math.sqrt, etc.)
      if (Node.isIdentifier(object)) {
        const objName = (object as Identifier).getText();
        // Treat as potential native namespace call
        this.builder.emitNativeCallWithNamespace(objName, methodName, args.length);
      } else {
        // It's an object.method() call - we need to emit the object first
        // and then call the method on it
        // For now, treat as native call with namespace=className
        // This will be handled properly in Phase 5 (VM execution)
        const className = objectText;
        this.builder.emitNativeCallWithClass(className, methodName, args.length);
      }
    }
  }

  private transformNewExpression(node: NewExpression): void {
    // Handle: new ClassName(arg1, arg2, ...)
    const expression = node.getExpression();
    const className = expression.getText();
    const args = node.getArguments();

    // Push arguments onto stack
    for (const arg of args) {
      this.transformExpression(arg, 'expression');
    }

    // Emit NEW instruction with class name and arg count
    this.builder.emitNew(className, args.length);
  }

  private transformConditionalExpression(node: any): void {
    // Ternary operator: condition ? trueExpr : falseExpr
    const condition = node.getCondition();
    const trueExpr = node.getWhenTrue();
    const falseExpr = node.getWhenFalse();

    // Evaluate condition
    this.transformExpression(condition, 'expression');

    // Jump to false branch if falsy
    const jmpToFalseAddr = this.builder.getInstructionCount();
    this.builder.emitJmpNot(0); // Placeholder

    // Evaluate true expression
    this.transformExpression(trueExpr, 'expression');

    // Jump to end
    const jmpToEndAddr = this.builder.getInstructionCount();
    this.builder.emitJmp(0); // Placeholder

    // Patch jump to false
    const instructions = this.builder.getCurrentInstructions();
    instructions[jmpToFalseAddr].target = this.builder.getInstructionCount();

    // Evaluate false expression
    this.transformExpression(falseExpr, 'expression');

    // Patch jump to end
    instructions[jmpToEndAddr].target = this.builder.getInstructionCount();
  }

  private transformTemplateExpression(node: any): void {
    // Template literal: `string ${expr} more string`
    const parts = node.getTemplateSpans?.() || [];
    let result = '';

    // Get the text and parse it for template parts
    const text = node.getText();
    
    // For now, simple implementation - just push the literal string
    // Full support would require parsing template spans
    this.builder.emitPushConst(text);
  }

  private isNativeFunction(name: string): boolean {
    return ['print', 'console'].includes(name.toLowerCase());
  }
}
