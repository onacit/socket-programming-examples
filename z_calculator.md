# CALCULATOR

## Message format

### Request message format

```text
Format
------

 0     7 8     15 16    23 24    31
+-------+--------+--------+--------+
|            Operator              |
|                                  |
|-------+--------+--------+--------|
|             Operand              |
|                                  |
|-------+--------+--------+--------|

 0     7 8     15 16    23 24    31
+-------+--------+--------+--------+
|            Operator              |
|                                  |
|-------+--------+--------+--------|
|            Operand1              |
|-------+--------+--------+--------|
|            Operand2              |
|-------+--------+--------+--------|


Fields
------

Operator is a 64-bit US-ASCII word.

Operand is a IEEE 754 double precision floating point value.
Operand1 and Operand2 are both IEEE 754 single precision floating point value.
```

### Response message format

```text
Format
------

 0     7 8     15 16    23 24    31
+-------+--------+--------+--------+
|            Operator              |
|                                  |
|-------+--------+--------+--------|
|             Operand              |
|                                  |
|-------+--------+--------+--------|
|             Result               |
|                                  |
|-------+--------+--------+--------|

 0     7 8     15 16    23 24    31
+-------+--------+--------+--------+
|            Operator              |
|                                  |
|-------+--------+--------+--------|
|            Operand1              |
|-------+--------+--------+--------|
|            Operand2              |
|-------+--------+--------+--------|
|             Result               |
|                                  |
|-------+--------+--------+--------|

Fields
------

Operator, Operand, Operand1, and Operand2 have same meaning described in [Request message format].

Result is a IEEE 754 double precision floating point value.
```

## Operators

| `Operator`            | `Result` should be           | notes                                               |
|-----------------------|------------------------------|-----------------------------------------------------|
| <code>IDENTITY</code> | $`Operanad`$                 |                                                     |
| <code>ADD</code>      | $`Operand1 + Operand2`$      |                                                     |
| <code>SUB</code>      | $`Operand1 - Operand2`$      |                                                     |
| <code>MUL</code>      | $`Operand1 \times Operand2`$ |                                                     |
| <code>DIV</code>      | $`Operand1 \div Operand2`$   | `Result` should be zero when the `Operand2` is zero |
| <code>MOD</code>      | $`Operand1 \bmod Operand2`$  | `Result` should be zero when the `Operand2` is zero |

### More operators

| `Operator`        | `Result` should be | notes |
|-------------------|--------------------|-------|
| <code>SQRT</code> | $`\sqrt{Operand}`$ |       |

