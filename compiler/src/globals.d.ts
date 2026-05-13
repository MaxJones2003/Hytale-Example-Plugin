/**
 * Global type definitions for Golem native function namespaces
 * These types allow TypeScript to recognize the available native functions
 */

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
