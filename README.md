# archunit-layeredarchitecture-problem
ArchUnit, problem with layeredarchitecture

## What is this?
Description for a potential issue in ArchUnit with layeredArchitecture

## Layers
We have three layers A, B and C:
* A may access B
* B may access C

## Test to show the potential issue
The JUnit test `ArchUnit_LayeredArchitectureProblem` shows the error.

As soon as the last `whereLayer` statement in line 34 is commented in, the test fails because of
`Field <mycomponent.layerA.A.b> has type <mycomponent.layerB.B> in (A.java:0)`

But why does an allowed access from B->C bring up an error for an access from A->B (which is even explicitely allowed)?