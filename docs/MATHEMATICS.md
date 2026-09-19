# Engineering mathematics

## Numerical library

`Vector` and `Matrix` are immutable finite real-value types. They implement vector addition/scaling/dot product, matrix addition/scaling/multiplication, matrix-vector products, transpose, determinant, pivoted `Ax=b` solution, and inverse through repeated solves. Dimension mismatches, ragged data, NaN/infinity, non-square operations, and pivots at or below `1e-12` fail with explicit diagnostics such as `MATRIX SINGULAR`.

`NumericalIntegrators` provides guarded Forward Euler and classical RK4 state steps. A derivative must return the same dimension as its state, and a step must be within `(0, 1]` seconds. `StateSpaceSystem` implements:

`x_dot = A x + B u`

`y = C x + D u`

with either integrator. `TransferFunction` accepts proper SISO numerator/denominator coefficients in descending powers of `s`, normalizes them, and converts them to controllable canonical state-space form.

## Mathematics Workstation

Place an **Engineering Mathematics Workstation**. Write one calculation in a Book and Quill, then use the book on the workstation. Parsing and calculation happen on the logical server; the grammar cannot execute Java, commands, files, or reflection. Empty-hand use repeats the stored expression/result, and the Engineering Inspector shows them. Both survive save/reload.

Scalar expressions support parentheses, `+ - * / ^`, `pi`, `e`, `sin`, `cos`, `tan`, `sqrt`, `exp`, and `log`:

```text
sin(pi / 2) + sqrt(9)
```

Vectors use comma-separated values and matrix rows use semicolons. Workstation matrices are intentionally capped at 8x8 and numerical integration at 10,000 steps:

```text
dot 1,2,3 | 4,5,6
vadd 1,2 | 3,4
det 1,2;3,4
inv 1,2;3,4
transpose 1,2,3;4,5,6
mul 1,2;3,4 | 1,2;3,4
solve 0,2;1,3 | 4,7
integrate 0 pi 100 : sin(x)
differentiate 0 0.0001 : sin(x)
```

The book is the first bounded in-game matrix editor. A richer grid GUI can be layered over the same pure engine later without moving calculation authority to the client.
