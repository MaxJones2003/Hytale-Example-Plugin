import { Project, ScriptTarget, ModuleKind, Diagnostic } from 'ts-morph';
import { BytecodeTransformer } from './bytecode/transformer.js';

/**
 * Golem Script Compiler
 * Compiles TypeScript to custom bytecode for the Golem VM
 */

export class GolemCompiler {
  private project: Project;
  private globalsContent = `
declare namespace Logger {
  function debug(message: any): void;
  function warn(message: any): void;
}

declare namespace Entity {
  function getSelf(): string;
  function log(message: any): void;
  function forward(): void;
  function backward(): void;
  function turnLeft(): void;
  function turnRight(): void;
}

declare namespace Math {
  function random(): number;
  function abs(x: number): number;
  function sqrt(x: number): number;
  function floor(x: number): number;
  function ceil(x: number): number;
}
`;

  constructor() {
    this.project = new Project({
      compilerOptions: {
        target: ScriptTarget.ES2020,
        module: ModuleKind.ES2020,
        skipLibCheck: true,
        noEmit: true,
      },
      skipAddingFilesFromTsConfig: true,
    });

    // Add global type definitions as a virtual file
    this.project.createSourceFile('globals.d.ts', this.globalsContent);
  }

  compile(sourceCode: string, fileName: string = 'script.ts'): string {
    // Parse the TypeScript source
    const sourceFile = this.project.createSourceFile(fileName, sourceCode, {
      overwrite: true,
    });

    // TODO: Validate against restricted TS subset
    // - No async/await
    // - No generators
    // - No classes
    // - No prototypes
    // - No dynamic imports
    // - No closures initially
    // - No arbitrary object mutation

    // Type checking
    const diagnostics = this.project.getPreEmitDiagnostics();
    if (diagnostics.length > 0) {
      const errors = diagnostics.map((d: Diagnostic) => d.getMessageText()).join('\n');
      throw new Error(`Compilation errors:\n${errors}`);
    }

    // Transform AST to bytecode
    const transformer = new BytecodeTransformer();
    const bytecode = transformer.transform(sourceFile);

    return JSON.stringify({
      fileName,
      source: sourceCode,
      bytecode,
    }, null, 2);
  }

  private printASTStructure(sourceFile: any, indent: number = 0): void {
    const statements = sourceFile.getStatements();
    
    for (const stmt of statements) {
      this.printNode(stmt, indent);
    }
  }

  private printNode(node: any, indent: number = 0): void {
    const prefix = '  '.repeat(indent);
    const kind = node.getKind?.();
    const kindName = kind ? this.getSyntaxKindName(kind) : 'Unknown';
    
    // Get additional info based on node type
    let info = '';
    if (node.getName?.()) info = ` name="${node.getName()}"`;
    else if (node.getText?.()) {
      const text = node.getText();
      if (text.length > 50) info = ` text="${text.substring(0, 50)}..."`;
      else if (text.length > 0) info = ` text="${text}"`;
    }

    console.log(`${prefix}${kindName}${info}`);

    // Print children for certain node types
    const children = node.getChildren?.() || [];
    const maxChildren = 10; // Limit to avoid spam
    
    for (let i = 0; i < Math.min(children.length, maxChildren); i++) {
      const child = children[i];
      // Skip syntax tokens, only show meaningful nodes
      if (!this.isSyntaxToken(child.getKind?.()) && child.getKind?.() !== undefined) {
        this.printNode(child, indent + 1);
      }
    }
  }

  private isSyntaxToken(kind: number): boolean {
    // Skip whitespace, semicolons, braces, etc
    return kind > 70 && kind < 160;
  }

  private getSyntaxKindName(kind: number): string {
    const names: {[key: number]: string} = {
      261: 'SourceFile',
      241: 'VariableStatement',
      254: 'FunctionDeclaration',
      238: 'Block',
      232: 'ExpressionStatement',
      250: 'IfStatement',
      251: 'WhileStatement',
      249: 'ForStatement',
      235: 'ReturnStatement',
      215: 'BinaryExpression',
      216: 'PrefixUnaryExpression',
      217: 'PostfixUnaryExpression',
      196: 'CallExpression',
      205: 'Identifier',
      8: 'NumericLiteral',
      10: 'StringLiteral',
      79: 'Identifier',
      192: 'PropertyAccessExpression',
      199: 'ArrayLiteralExpression',
      200: 'ObjectLiteralExpression',
    };
    return names[kind] || `SyntaxKind(${kind})`;
  }
}

// CLI entry point
async function runCLI() {
  const compiler = new GolemCompiler();
  
  // Read from stdin
  let source = '';
  
  for await (const chunk of process.stdin) {
    source += chunk;
  }

  try {
    const bytecode = compiler.compile(source);
    
    // Output JSON for Java to parse
    console.log(JSON.stringify({
      success: true,
      bytecode: JSON.parse(bytecode),
    }));
    
    process.exit(0);
  } catch (error) {
    // Output error JSON
    console.log(JSON.stringify({
      success: false,
      error: error instanceof Error ? error.message : String(error),
    }));
    
    process.exit(1);
  }
}

runCLI();
