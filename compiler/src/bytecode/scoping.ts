/**
 * Scope management for TypeScript compiler
 * Handles nested scopes (global, function, block, loop)
 */

export interface Variable {
  name: string;
  scopeDepth: number;
  type: 'var' | 'let' | 'const' | 'param' | 'function' | 'class';
}

export class Scope {
  private variables: Map<string, Variable> = new Map();
  private depth: number;
  private parent: Scope | null;

  constructor(depth: number, parent: Scope | null = null) {
    this.depth = depth;
    this.parent = parent;
  }

  /**
   * Declare a new variable in this scope
   */
  declare(name: string, type: Variable['type']): void {
    if (this.variables.has(name)) {
      throw new Error(`Variable '${name}' already declared in current scope`);
    }
    this.variables.set(name, { name, scopeDepth: this.depth, type });
  }

  /**
   * Lookup a variable in this scope or parent scopes
   * Returns the variable if found, null otherwise
   */
  lookup(name: string): Variable | null {
    if (this.variables.has(name)) {
      return this.variables.get(name)!;
    }
    if (this.parent) {
      return this.parent.lookup(name);
    }
    return null;
  }

  /**
   * Check if a variable exists in this scope (not parent scopes)
   */
  has(name: string): boolean {
    return this.variables.has(name);
  }

  /**
   * Check if a variable is defined in this scope or any parent scope
   */
  isDefined(name: string): boolean {
    return this.lookup(name) !== null;
  }

  /**
   * Get all variables at this scope level (not including parent scopes)
   */
  getLocalVariables(): Variable[] {
    return Array.from(this.variables.values());
  }

  /**
   * Get depth of this scope
   */
  getDepth(): number {
    return this.depth;
  }

  /**
   * Get parent scope
   */
  getParent(): Scope | null {
    return this.parent;
  }
}

export class ScopeStack {
  private scopes: Scope[] = [];

  constructor() {
    // Start with global scope at depth 0
    this.scopes.push(new Scope(0));
  }

  /**
   * Push a new scope (entering a block, function, loop, etc.)
   */
  pushScope(): Scope {
    const currentScope = this.scopes[this.scopes.length - 1];
    const newScope = new Scope(currentScope.getDepth() + 1, currentScope);
    this.scopes.push(newScope);
    return newScope;
  }

  /**
   * Pop the current scope (exiting a block, function, loop, etc.)
   */
  popScope(): Scope {
    if (this.scopes.length <= 1) {
      throw new Error('Cannot pop global scope');
    }
    return this.scopes.pop()!;
  }

  /**
   * Get the current scope
   */
  currentScope(): Scope {
    return this.scopes[this.scopes.length - 1];
  }

  /**
   * Declare a variable in the current scope
   */
  declare(name: string, type: Variable['type']): void {
    this.currentScope().declare(name, type);
  }

  /**
   * Look up a variable starting from current scope and walking up the stack
   */
  lookup(name: string): Variable | null {
    return this.currentScope().lookup(name);
  }

  /**
   * Check if a variable is defined
   */
  isDefined(name: string): boolean {
    return this.currentScope().isDefined(name);
  }

  /**
   * Get the scope depth
   */
  getDepth(): number {
    return this.scopes.length - 1;
  }

  /**
   * Get all scopes (for debugging)
   */
  getAllScopes(): Scope[] {
    return [...this.scopes];
  }
}
