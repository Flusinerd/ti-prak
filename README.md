# Test
## Testausdruck
15+2*(5-20)

## AST
```mermaid
graph TD;
    A[+]-->15;
    A[+]-->*;
    *-->2;
    *-->C;
    C[-]-->5;
    C[-]-->20;
```
