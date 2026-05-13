# Golem Script Compiler

TypeScript compiler for the Golem Hytale mod scripting system. Converts a restricted subset of TypeScript into custom bytecode for execution in the Golem VM.

## Architecture

- **Input**: TypeScript source code (restricted subset)
- **Process**: Parse → Validate → Type Check → Transform to bytecode
- **Output**: Serialized bytecode format (to be consumed by Java VM)

## Supported TypeScript Features

✅ Supported:
- Variables
- Loops (for, while)
- Functions
- Arrays
- Simple objects

❌ Not supported:
- async/await
- Generators
- Classes
- Prototypes
- Dynamic imports
- Closures (initially)
- Arbitrary object mutation

## Installation

```bash
npm install
```

## Development

```bash
npm run dev   # Watch mode
npm run build # One-time build
```

## Compilation

```bash
npm run compile
```

## Project Structure

```
compiler/
├── src/
│   ├── index.ts           # Main compiler entry point
│   ├── parser/            # TS parsing utilities
│   ├── validator/         # Subset validation
│   ├── bytecode/          # Bytecode generation
│   └── types.ts           # Type definitions
├── dist/                  # Compiled output
├── tsconfig.json
├── package.json
└── README.md
```
