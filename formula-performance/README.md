# Formula Performance Testing

Performance testing for Formula using JMH (Java Microbenchmark Harness), the industry-standard benchmarking tool for JVM applications.

## Quick Start

### Running Benchmarks

```bash
# Run all benchmarks
./gradlew :formula-performance:jmh

# Run specific benchmark class
./gradlew :formula-performance:jmh -Pjmh=TransitionBenchmark

# Run specific benchmark method
./gradlew :formula-performance:jmh -Pjmh=TransitionBenchmark.transitions

# Run with custom JMH parameters
./gradlew :formula-performance:jmh \
  -Pjmh.warmup=10 \
  -Pjmh.iterations=10 \
  -Pjmh.fork=3
```

Benchmarks take approximately 5-10 minutes to run with full statistical analysis.

### Writing Benchmarks

Create benchmarks in `src/jmh/kotlin/`:

```kotlin
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
open class MyBenchmark {

    private lateinit var formula: MyFormula
    private lateinit var observer: TestFormulaObserver<Unit, *, *>

    @Setup(Level.Trial)
    fun setup() {
        formula = MyFormula()
        observer = formula.test()
        observer.input(Unit)
    }

    @Benchmark
    fun measurePerformance() {
        observer.output { onAction() }
    }

    @TearDown(Level.Trial)
    fun tearDown() {
        observer.dispose()
    }
}
```

### Key JMH Annotations

- `@BenchmarkMode(Mode.AverageTime)` - Measure average time per operation
- `@OutputTimeUnit(TimeUnit.MICROSECONDS)` - Report results in microseconds
- `@Warmup(iterations = 5)` - JIT warmup iterations
- `@Measurement(iterations = 5)` - Actual measurement iterations
- `@Fork(1)` - Run in separate JVM process for isolation
- `@State(Scope.Thread)` - Thread-local state
- `@Setup/@TearDown` - Lifecycle hooks
- `@OperationsPerInvocation(N)` - For batched operations

**Important**: Benchmark classes must be `open` in Kotlin (JMH generates subclasses).

## Benchmark Coverage

### ✅ Implemented Benchmarks

**TransitionBenchmark** - Formula transition performance at various depths (100 transitions)
- Tests transition performance from baseline (depth=0) through nested hierarchies (1, 2, 5, 10, 25 levels)
- Depth 0: Baseline performance with no child formulas
- Depth 1-25: Measures overhead of `context.child()` calls and state propagation through nesting
- Uses `@Param` to test different nesting depths
- Critical for validating transition queue optimizations and child formula overhead
- Run: `./gradlew :formula-performance:jmh -Pjmh=TransitionBenchmark`

**TransitionQueueBenchmark** - Inline state transition queue processing overhead
- Measures framework overhead for processing multiple inline state transitions
- Tests with 1, 100, and 10,000 transitions to validate queue scaling behavior
- Uses custom Action that emits events synchronously on start to exercise transitionQueue
- Validates that `FormulaManagerImpl.transitionQueue` adds negligible overhead
- Pure framework benchmark (trivial evaluation) - isolates queue processing cost
- Run: `./gradlew :formula-performance:jmh -Pjmh=TransitionQueueBenchmark`

**CallbackOverheadBenchmark** - State transition performance with many callback declarations
- Measures whether declaring many callbacks impacts state change performance
- Tests with 10 and 50 callbacks to understand callback declaration overhead
- Simulates real-world applications with many UI interaction callbacks
- Validates that callback management adds minimal overhead during evaluation
- Run: `./gradlew :formula-performance:jmh -Pjmh=CallbackOverheadBenchmark`

**ActionCountBenchmark** - State transition performance with varying numbers of active actions
- Measures how the number of running actions affects state change performance
- Tests with 1, 25, and 100 active actions (never-ending flows that don't emit)
- Triggers 1000 state changes while actions remain active
- Critical for understanding action management overhead during state updates
- Run: `./gradlew :formula-performance:jmh -Pjmh=ActionCountBenchmark`

**ChildrenCountBenchmark** - State transition performance with varying numbers of child formulas
- Measures how the number of active child formulas affects parent state change performance
- Tests with 1, 25, and 100 child formulas to understand composition overhead
- Triggers 1000 parent state changes while children remain stable
- Critical for understanding the cost of composing many child formulas
- Run: `./gradlew :formula-performance:jmh -Pjmh=ChildrenCountBenchmark`

**CallbackInitializationBenchmark** - Callback initialization and removal performance
- Measures the cost of creating brand new callbacks (not redeclaring existing ones)
- Tests with 10 and 50 callbacks to understand initialization overhead
- Each iteration creates callbacks with different keys, forcing initialization from scratch
- Measures combined cost of removing old callbacks and initializing new ones (steady-state)
- Initial formula setup excluded via warmup
- Run: `./gradlew :formula-performance:jmh -Pjmh=CallbackInitializationBenchmark`

**ActionInitializationBenchmark** - Action initialization and cancellation performance
- Measures the cost of creating brand new actions (not redeclaring existing ones)
- Tests with 1, 25, and 100 actions to understand initialization overhead
- Each iteration creates actions with different keys, forcing new subscriptions from scratch
- Measures combined cost of cancelling old actions and starting new ones (steady-state)
- Initial formula setup excluded via warmup
- Run: `./gradlew :formula-performance:jmh -Pjmh=ActionInitializationBenchmark`

**ChildrenInitializationBenchmark** - Child formula initialization and removal performance
- Measures the cost of creating brand new child formulas (not reusing existing ones)
- Tests with 1, 25, and 100 children to understand initialization overhead
- Each iteration creates children with different keys, forcing initialState() calls
- Measures combined cost of removing old children and initializing new ones (steady-state)
- Initial formula setup excluded via warmup
- Run: `./gradlew :formula-performance:jmh -Pjmh=ChildrenInitializationBenchmark`

**GlobalEffectQueueBenchmark** - Effect queueing and execution performance
- Measures the cost of queueing and executing transition effects through globalEffectQueue
- Tests with 1, 10, and 100 effects per transition to understand queueing overhead
- Each benchmark triggers 100 transitions to measure average per-transition cost
- Uses TransitionContext.andThen API to chain multiple effects in a single transition
- Critical for validating that globalEffectQueue (LinkedList) adds minimal overhead
- Run: `./gradlew :formula-performance:jmh -Pjmh=GlobalEffectQueueBenchmark`

### ⏳ Planned Benchmarks

#### Action Performance
- Actions with frequent state transitions

#### Callback Performance
- Nested callback invocation patterns
- Deep callback call stacks

#### Input Change Performance
- Rapid input changes
- Input propagation to children

## Example Output

```
Benchmark                        (depth)  Mode  Cnt   Score   Error  Units
TransitionBenchmark.transitions        0  avgt    5  34.649 ±17.587  us/op
TransitionBenchmark.transitions       10  avgt    5  35.253 ±15.513  us/op
TransitionBenchmark.transitions       20  avgt    5  35.750 ±16.558  us/op
```

The `Score ± Error` shows the average time with 99.9% confidence interval, indicating measurement precision.

## CI Integration

Benchmarks run automatically on every PR:

```yaml
- name: Run Formula Performance Benchmarks
  run: ./gradlew :formula-performance:jmh
```

**Note**: Full benchmarks take 5-10 minutes. We run them in CI to:
- Catch major performance regressions
- Track performance trends over time
- Validate optimization PRs

## Testing Optimizations

### Measuring Improvements

When optimizing Formula internals (e.g., LinkedList → ArrayDeque):

```bash
# 1. Baseline on master branch
git checkout master
./gradlew :formula-performance:jmh -Pjmh=TransitionBenchmark

# Output:
# TransitionBenchmark.transitions  avgt  25  10.234 ± 0.156  us/op

# 2. Test optimization
git checkout feature/arraydeque
./gradlew :formula-performance:jmh -Pjmh=TransitionBenchmark

# Output:
# TransitionBenchmark.transitions  avgt  25   7.123 ± 0.089  us/op
#
# Improvement: 30.4% faster (3.1μs saved)
# Confidence: ±0.089μs (high precision)
```

### Testing Multiple Scenarios with @Param

Use `@Param` to test different configurations in a single benchmark:

```kotlin
@State(Scope.Thread)
open class MyBenchmark {

    @Param("1", "5", "10")
    var depth: Int = 1

    private lateinit var observer: TestFormulaObserver<*, *, *>

    @Setup
    fun setup() {
        observer = createObserver(depth)
    }

    @Benchmark
    fun measure() {
        // JMH runs this for each depth value
    }

    @TearDown
    fun tearDown() {
        observer.dispose()
    }
}
```

JMH automatically runs all variants and compares results:

```
Benchmark            (depth)  Mode  Cnt   Score    Error  Units
MyBenchmark.measure        1  avgt   25  10.234 ± 0.156  us/op
MyBenchmark.measure        5  avgt   25  52.123 ± 0.456  us/op
MyBenchmark.measure       10  avgt   25 104.567 ± 0.891  us/op
```

## Why JMH?

JMH provides:
- ✅ **Statistical rigor** - Confidence intervals, error bars, multiple forks
- ✅ **JIT handling** - Proper warmup ensures JIT compilation happens first
- ✅ **Dead code elimination prevention** - Blackhole prevents compiler optimizations
- ✅ **Isolation** - Separate JVM forks eliminate cross-test interference
- ✅ **Industry standard** - Used by RxJava, Kotlin Coroutines, Netty, etc.
- ✅ **Reproducible** - Consistent results across runs and machines

## Tips for Accurate Benchmarks

### 1. Use `@State(Scope.Thread)`
```kotlin
@State(Scope.Thread)  // State is thread-local
open class MyBenchmark {
    private lateinit var formula: MyFormula
}
```

### 2. Initialize in `@Setup`, not `@Benchmark`
```kotlin
@Setup(Level.Trial)
fun setup() {
    formula = MyFormula()  // Setup once before all iterations
}

@Benchmark
fun measure() {
    // Only measure the operation itself
}
```

### 3. Use `@OperationsPerInvocation` for Batched Operations
```kotlin
@Benchmark
@OperationsPerInvocation(10)  // 10 ops per invocation
fun batchedOps() {
    repeat(10) { doWork() }  // Report time per single op
}
```

### 4. Dispose Resources in `@TearDown`
```kotlin
@TearDown(Level.Trial)
fun tearDown() {
    observer.dispose()  // Clean up after benchmarks
}
```

### 5. Make Classes `open`
```kotlin
@State(Scope.Thread)
open class MyBenchmark {  // Must be open for JMH
    // ...
}
```

## Troubleshooting

### Benchmarks taking too long?
```bash
# Reduce iterations for faster (less precise) results
./gradlew :formula-performance:jmh \
  -Pjmh.warmup=2 \
  -Pjmh.iterations=2
```

### Need more precision?
```bash
# Increase forks and iterations
./gradlew :formula-performance:jmh \
  -Pjmh.fork=3 \
  -Pjmh.iterations=10
```

### Run specific method?
```bash
# Use regex to match specific benchmark method
./gradlew :formula-performance:jmh -Pjmh=TransitionBenchmark.transitions
```

## Additional Resources

- [JMH Documentation](https://github.com/openjdk/jmh)
- [JMH Samples](https://github.com/openjdk/jmh/tree/master/jmh-samples/src/main/java/org/openjdk/jmh/samples)
- [Avoiding Microbenchmark Pitfalls](https://www.oracle.com/technical-resources/articles/java/architect-benchmarking.html)
