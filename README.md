# COS226-Assignment2

This project implements and tests two concurrent queue types:

- `BoundedQueue`: a bounded blocking queue using locks and conditions
- `LockFreeQueue`: a lock-free queue using atomic references

Both implement the `ConcurrentQueue<T>` interface and are exercised by the `Main` program.

## Files

- `ConcurrentQueue.java` — shared queue interface
- `BoundedQueue.java` — blocking bounded queue implementation
- `LockFreeQueue.java` — lock-free queue implementation
- `Main.java` — producer/consumer test harness
- `makefile` — build/run shortcuts

## Build and Run

```bash
make build
make run
```