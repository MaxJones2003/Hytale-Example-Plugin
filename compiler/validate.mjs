#!/usr/bin/env node

/**
 * Validation script for Golem TypeScript Compiler
 * Tests key language features to ensure they compile correctly
 */

import { GolemCompiler } from './dist/index.js';

const compiler = new GolemCompiler();

const tests = [
  {
    name: 'For loop with i++',
    code: `
      let sum = 0;
      for (let i = 0; i < 5; i++) {
        sum = sum + i;
      }
    `,
  },
  {
    name: 'For loop with ++i',
    code: `
      let sum = 0;
      for (let i = 0; i < 5; ++i) {
        sum = sum + i;
      }
    `,
  },
  {
    name: 'While loop',
    code: `
      let i = 0;
      while (i < 5) {
        i = i + 1;
      }
    `,
  },
  {
    name: 'Do-while loop',
    code: `
      let i = 0;
      do {
        i = i + 1;
      } while (i < 5);
    `,
  },
  {
    name: 'Ternary operator',
    code: `
      let x = 5;
      let result = x > 3 ? 10 : 20;
    `,
  },
  {
    name: 'Prefix/postfix operators',
    code: `
      let i = 5;
      let a = ++i;
      let b = i--;
    `,
  },
  {
    name: 'Array operations',
    code: `
      let arr = [1, 2, 3];
      let x = arr[0];
      arr[1] = 10;
    `,
  },
  {
    name: 'Function declaration',
    code: `
      function add(a, b) {
        return a + b;
      }
      let result = add(3, 5);
    `,
  },
  {
    name: 'Complex expression',
    code: `
      let x = 5;
      let y = 3;
      let result = (x + y) * 2 - x / y;
    `,
  },
  {
    name: 'Variable scoping',
    code: `
      let x = 10;
      for (let i = 0; i < 3; i++) {
        let y = i;
        x = x + y;
      }
    `,
  },
  {
    name: 'Basic class',
    code: `
      class Animal {
        name: string;
        constructor(name: string) {
          this.name = name;
        }
      }
    `,
  },
  {
    name: 'Class with method',
    code: `
      class Player {
        health: number;
        constructor(health: number) {
          this.health = health;
        }
        takeDamage(amount: number) {
          this.health = this.health - amount;
        }
      }
    `,
  },
  {
    name: 'Class instantiation',
    code: `
      class Entity {
        x: number;
        y: number;
        constructor(x: number, y: number) {
          this.x = x;
          this.y = y;
        }
      }
      let e = new Entity(10, 20);
    `,
  },
  {
    name: 'Class with getter',
    code: `
      class Box {
        width: number;
        height: number;
        constructor(width: number, height: number) {
          this.width = width;
          this.height = height;
        }
        get area(): number {
          return this.width * this.height;
        }
      }
    `,
  },
  {
    name: 'Class with static method',
    code: `
      class MathHelper {
        static max(a: number, b: number): number {
          return a > b ? a : b;
        }
      }
    `,
  },
  {
    name: 'Class inheritance',
    code: `
      class Entity {
        name: string;
        constructor(name: string) {
          this.name = name;
        }
      }
      class Player extends Entity {
        health: number;
        constructor(name: string, health: number) {
          super(name);
          this.health = health;
        }
      }
    `,
  },
  {
    name: 'Private property',
    code: `
      class Secret {
        private value: number = 42;
        getValue(): number {
          return this.value;
        }
      }
    `,
  },
];

let passed = 0;
let failed = 0;

console.log('========================================');
console.log('Golem TypeScript Compiler Validation');
console.log('========================================\n');

for (const test of tests) {
  try {
    const result = compiler.compile(test.code);
    const parsed = JSON.parse(result);
    
    // Validate bytecode structure
    if (!parsed.bytecode || !parsed.bytecode.instructions) {
      console.log(`✗ ${test.name}`);
      console.log(`  Error: Invalid bytecode structure\n`);
      failed++;
      continue;
    }
    
    console.log(`✓ ${test.name}`);
    console.log(`  Instructions: ${parsed.bytecode.instructions.length}`);
    if (parsed.bytecode.functions && parsed.bytecode.functions.length > 0) {
      console.log(`  Functions: ${parsed.bytecode.functions.length}`);
    }
    console.log();
    passed++;
  } catch (error) {
    console.log(`✗ ${test.name}`);
    console.log(`  Error: ${error.message}\n`);
    failed++;
  }
}

console.log('========================================');
console.log(`Results: ${passed} passed, ${failed} failed`);
console.log('========================================');

process.exit(failed > 0 ? 1 : 0);
