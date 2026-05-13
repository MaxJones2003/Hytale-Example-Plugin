import {
  SourceFile,
  Node,
  SyntaxKind,
  VariableDeclaration,
  FunctionDeclaration,
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
} from 'ts-morph';
import { BytecodeBuilder, BytecodeModule, Instruction } from './instructions.js';

/**
 * Transforms TypeScript AST to custom bytecode
 */
export class BytecodeTransformer {
  private builder = new BytecodeBuilder();
  private symbolTable = new Map<string, 'var' | 'param' | 'function'>();
  private jumpTargets: Map<string, number> = new Map();

  transform(sourceFile: SourceFile): BytecodeModule {
    // First pass: collect and pre-register all function declarations
    const functions = sourceFile.getFunctions();
    for (const func of functions) {
      const name = func.getName() || '';
      const params = func.getParameters().map((p) => p.getName());
      this.symbolTable.set(name, 'function');
      // Pre-register function in builder so it can be called before it's defined
      this.builder.preFunctionDeclaration(name, params);
    }

    // Second pass: transform each statement
    const statements = sourceFile.getStatements();
    for (const stmt of statements) {
      this.transformStatement(stmt);
    }

    return this.builder.build();
  }

  private transformStatement(node: Node): void {
    if (Node.isVariableStatement(node)) {
      this.transformVariableStatement(node);
    } else if (Node.isFunctionDeclaration(node)) {
      this.transformFunctionDeclaration(node);
    } else if (Node.isIfStatement(node)) {
      this.transformIfStatement(node);
    } else if (Node.isWhileStatement(node)) {
      this.transformWhileStatement(node);
    } else if (Node.isForStatement(node)) {
      this.transformForStatement(node);
    } else if (Node.isReturnStatement(node)) {
      this.transformReturnStatement(node);
    } else if (Node.isExpressionStatement(node)) {
      const expr = node.getExpression();
      this.transformExpression(expr);
      // Pop result from stack if not used
      this.builder.emit('POP');
    } else if (Node.isBlock(node)) {
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
      this.symbolTable.set(name, 'var');

      if (decl.getInitializer) {
        const initializer = decl.getInitializer();
        if (initializer) {
          this.transformExpression(initializer);
          this.builder.emitStoreVar(name);
        }
      }
    }
  }

  private transformFunctionDeclaration(node: Node): void {
    const func = node as FunctionDeclaration;
    const name = func.getName() || 'anonymous';
    const params = func.getParameters().map((p) => p.getName());

    this.builder.startFunction(name, params);

    // Add parameters to symbol table
    for (const param of params) {
      this.symbolTable.set(param, 'param');
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
    this.builder.endFunction(name, params);
  }

  private transformIfStatement(node: Node): void {
    const ifStmt = node as IfStatement;
    const condition = ifStmt.getExpression();

    // Evaluate condition
    this.transformExpression(condition);

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
    this.transformExpression(condition);

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

  private transformForStatement(node: Node): void {
    const forStmt = node as ForStatement;

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
            this.symbolTable.set(name, 'var');

            const declInit = decl.getInitializer?.();
            if (declInit) {
              this.transformExpression(declInit);
              this.builder.emitStoreVar(name);
            } else {
              // If no initializer, push null and store
              this.builder.emitPushConst(null);
              this.builder.emitStoreVar(name);
            }
          }
        }
      } else {
        // Handle as expression (assignment like i = 0)
        this.transformExpression(initializer);
        this.builder.emit('POP');
      }
    }

    const loopStart = this.builder.getInstructionCount();

    // Evaluate condition
    const condition = forStmt.getCondition();
    if (condition) {
      this.transformExpression(condition);
      const jmpNotAddr = this.builder.getInstructionCount();
      this.builder.emitJmpNot(0); // Placeholder

      // Transform body
      const body = forStmt.getStatement();
      this.transformStatement(body);

      // Transform incrementor
      const incrementor = forStmt.getIncrementor();
      if (incrementor) {
        this.transformExpression(incrementor);
        this.builder.emit('POP');
      }

      // Jump back
      this.builder.emitJmp(loopStart);

      // Patch JMP_NOT
      const instructions = this.builder.getCurrentInstructions();
      instructions[jmpNotAddr].target = this.builder.getInstructionCount();
    }
  }

  private transformReturnStatement(node: Node): void {
    const retStmt = node as ReturnStatement;
    const expression = retStmt.getExpression();

    if (expression) {
      this.transformExpression(expression);
    } else {
      this.builder.emitPushConst(null);
    }

    this.builder.emitReturn();
  }

  private transformExpression(node: Node): void {
    if (Node.isBinaryExpression(node)) {
      this.transformBinaryExpression(node as BinaryExpression);
    } else if (Node.isCallExpression(node)) {
      this.transformCallExpression(node as CallExpression);
    } else if (Node.isIdentifier(node)) {
      const id = node as Identifier;
      const name = id.getText();
      this.builder.emitLoadVar(name);
    } else if (Node.isNumericLiteral(node)) {
      const lit = node as NumericLiteral;
      this.builder.emitPushConst(parseFloat(lit.getText()));
    } else if (Node.isStringLiteral(node)) {
      const lit = node as StringLiteral;
      this.builder.emitPushConst(lit.getLiteralValue());
    } else if (Node.isPropertyAccessExpression(node)) {
      const prop = node as PropertyAccessExpression;
      this.transformExpression(prop.getExpression());
      const propName = prop.getName();
      this.builder.emitObjectGet(propName);
    } else if (Node.isUnaryExpression(node)) {
      const unary = node as UnaryExpression;
      const text = node.getText();
      const kind = node.getKind();

      // Handle prefix/postfix increment/decrement (++, --)
      if (text.includes('++') || text.includes('--')) {
        const isIncrement = text.includes('++');
        const isPrefix = text.startsWith('++') || text.startsWith('--');
        
        // Get the variable name
        const operand = unary.getOperand();
        const varName = operand.getText();

        // Load current value
        this.builder.emitLoadVar(varName);

        // Duplicate top of stack for postfix to preserve old value
        if (!isPrefix) {
          this.builder.emit('DUP');
        }

        // Push 1 and add/subtract
        this.builder.emitPushConst(1);
        if (isIncrement) {
          this.builder.emitBinOp('ADD');
        } else {
          this.builder.emitBinOp('SUB');
        }

        // For prefix, duplicate the new value to return it
        if (isPrefix) {
          this.builder.emit('DUP');
        }

        // Store back to variable (pops the new value)
        this.builder.emitStoreVar(varName);

        // Stack state after STORE_VAR:
        // - Prefix: [new_value] (we DUP'd before storing)
        // - Postfix: [old_value] (it was at bottom, new_value was popped by STORE_VAR)
      } else {
        // Handle other unary expressions (!, -, etc)
        const children = unary.getChildAtIndex(1) || node.getChildren()[1];
        if (children) {
          this.transformExpression(children);
        }

        // Check if it's a logical not or negation
        if (text.startsWith('!')) {
          this.builder.emit('NOT');
        } else if (text.startsWith('-')) {
          this.builder.emitPushConst(-1);
          this.builder.emitBinOp('MUL');
        }
      }
    } else if (Node.isKind(node, SyntaxKind.PostfixUnaryExpression)) {
      // Handle postfix increment/decrement (i++, i--)
      const postfix = node as PostfixUnaryExpression;
      const text = node.getText();
      const isIncrement = text.includes('++');
      
      // Get the variable name from the operand
      const operand = postfix.getOperand();
      const varName = operand.getText();

      // Load current value
      this.builder.emitLoadVar(varName);

      // Duplicate to preserve old value (which will be the expression result for postfix)
      this.builder.emit('DUP');

      // Push 1 and add/subtract
      this.builder.emitPushConst(1);
      if (isIncrement) {
        this.builder.emitBinOp('ADD');
      } else {
        this.builder.emitBinOp('SUB');
      }

      // Store new value back to variable (pops new value)
      this.builder.emitStoreVar(varName);

      // Old value remains on stack for postfix semantics
    } else if (Node.isArrayLiteralExpression(node)) {
      const arr = node as ArrayLiteralExpression;
      const elements = arr.getElements();
      this.builder.emitArrayNew(elements.length);

      for (let i = 0; i < elements.length; i++) {
        this.transformExpression(elements[i]);
        this.builder.emitPushConst(i);
        this.builder.emitArraySet();
      }
    }
  }

  private transformBinaryExpression(node: BinaryExpression): void {
    const left = node.getLeft();
    const right = node.getRight();
    const operator = node.getOperatorToken().getKind();

    // Special handling for assignment - don't transform left side as expression
    if (operator === SyntaxKind.EqualsToken) {
      // Transform right side to get value
      this.transformExpression(right);

      // Store to left side variable
      if (Node.isIdentifier(left)) {
        const name = (left as Identifier).getText();
        this.builder.emitStoreVar(name);
        // Push the assigned value back on stack for use in expressions
        this.builder.emitLoadVar(name);
      }
      return;
    }

    // For all other operators, transform both sides normally
    this.transformExpression(left);
    this.transformExpression(right);

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
      this.transformExpression(arg);
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
      // Handle namespace.method() calls like Logger.debug() or Math.sqrt()
      const propAccess = callee as PropertyAccessExpression;
      const namespace = propAccess.getExpression().getText();
      const method = propAccess.getName();
      
      this.builder.emitNativeCallWithNamespace(namespace, method, args.length);
    }
  }

  private isNativeFunction(name: string): boolean {
    return ['print', 'console'].includes(name.toLowerCase());
  }
}
