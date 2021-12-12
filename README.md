# archunit-layeredarchitecture-problem
ArchUnit, problem with layeredarchitecture

## What is this?
Description for an potential issue in ArchUnit with layeredArchitecture

## Layers
We have three layers A, B and C. A may access C, B may access A. 

## Test to show the potential issue
A JUnit test shows the error. As soon as the "whereLayer" statement in line 34 is commented in, the test fails because of
       Field <mycomponent.layerB.B.a> has type <mycomponent.layerA.A> in (B.java:0)

But why does an allowed dependency from A->C bring up an error for a dependency from B->A (which is even explicitely allowed)?
