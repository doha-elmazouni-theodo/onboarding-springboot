# Nullability and Checker Framework

## 1. Why We Use Nullability Checking

Nullability bugs are frequent and expensive. The blueprint enables Checker Framework nullness analysis at compile time to catch those issues before runtime.

This is an explicit quality gate: nullness warnings fail the build.

## 2. Default Nullability Model

The default is non-null.

That means:
- parameters are treated as non-null unless annotated otherwise
- return values are treated as non-null unless annotated otherwise
- fields are treated as non-null unless annotated otherwise
- generic type arguments are treated as non-null unless annotated otherwise

To allow null:
- annotate with `@Nullable`

## 3. Practical Rules

### ✅ Do

- annotate nullable parameters and returns explicitly
- initialize non-null fields in constructors
- use `@EnsuresNonNull` / `@EnsuresNonNullIf` when a validator method establishes non-null state
- annotate generic type parameters when nullable values are expected (for example `List<@Nullable T>`)

### ❌ Do Not

- keep null checks on non-null parameters unless the parameter is explicitly nullable
- assign nullable values to non-null fields
- rely on implicit assumptions that the checker cannot infer

## 4. Common Error Patterns and Fixes

### 4.1 `nullness:return`

```java
// ❌ method can return null but return type is implicitly non-null
public static String toStringOrNull(@Nullable Object valueObject) {
    return valueObject != null ? valueObject.toString() : null;
}
```

```diff
// ✅ mark return type as nullable
- public static String toStringOrNull(@Nullable Object valueObject) {
+ public static @Nullable String toStringOrNull(@Nullable Object valueObject) {
    return valueObject != null ? valueObject.toString() : null;
}
```

### 4.2 `nullness:nulltest.redundant`

```java
public static Language fromString(String language) {
    // ❌ redundant null check: parameter is implicitly non-null
    if (language == null) {
        return null;
    }
    ...
}
```

```diff
// ✅ fix 1: remove null check when null input is not allowed
public static Language fromString(String language) {
-   if (language == null) {
-       return null;
-   }
    ...
}
```

```diff
// ✅ fix 2: mark parameter nullable when null input is allowed
- public static Language fromString(String language) {
+ public static Language fromString(@Nullable String language) {
    if (language == null) {
        return null;
    }
    ...
}
```

### 4.3 `nullness:initialization.fields.uninitialized`

```java
public class CustomersDbEntity {
    Collection<NewCardRequestDbEntity> newCardRequests;

    // ❌ constructor does not initialize implicitly non-null field
    public CustomersDbEntity(Customer customer) {
        ...
    }
}
```

```diff
public class CustomersDbEntity {
    Collection<NewCardRequestDbEntity> newCardRequests;

    public CustomersDbEntity(Customer customer) {
+       this.newCardRequests = new ArrayList<>();
    }
}
```

### 4.4 `nullness:assignment` / `nullness:argument`

```java
public class CardDbEntity {
    @Column(name = "opposition_date")
    private LocalDateTime oppositionDate;

    public CardDbEntity(@NotNull Card card) {
        // ❌ assigning nullable value to non-null field
        this.oppositionDate = card.oppositionDate();
    }
}
```

```diff
public class CardDbEntity {
    @Column(name = "opposition_date")
+   @Nullable
    private LocalDateTime oppositionDate;
}
```

```java
ArrayList<String> list = new ArrayList<>();
// ❌ list element type is non-null by default
list.add(customer.vehicleBrandId());
```

```diff
- ArrayList<String> list = new ArrayList<>();
+ ArrayList<@Nullable String> list = new ArrayList<>();
list.add(customer.vehicleBrandId());
```

### 4.5 `nullness:dereference.of.nullable`

```java
if (customer.card() != null && customer.card().isOpposed()) {
    cardReactivationDeadline = customer
        .card()
        .oppositionDate()
        // ❌ oppositionDate is nullable
        .plusDays(reactivateCardDeadlineParameter.getReactivateCardDeadline().days());
}
```

```diff
// ✅ refine contract: if isOpposed() returns true, oppositionDate() is non-null
+ @EnsuresNonNullIf(expression = "oppositionDate()", result = true)
public Boolean isOpposed() {
    ...
}
```

```java
return new GetCurrentCustomerResponse(
    ...
    // ❌ checker still sees customer() as nullable on this line
    current.customer().name(),
    ...
);
```

```diff
return new GetCurrentCustomerResponse(
    ...
    // ✅ last resort when reasoning is sound
-   current.customer().name(),
+   castNonNull(current.customer()).name(),
    ...
);
```

### 4.6 `nullness:dereference.of.nullable` and lambdas

```java
if (request.oldCard() != null) {
    var oldCard = cardRepository
        .findById(request.oldCard().number().value())
        .orElseThrow(() -> new CardNotFoundException(
            // ❌ deferred lambda can observe a nullable value
            request.oldCard().number()));
}
```

```diff
if (request.oldCard() != null) {
+   final Card oldCardFromRequest = request.oldCard();
    var oldCard = cardRepository
        .findById(request.oldCard().number().value())
        .orElseThrow(() -> new CardNotFoundException(
-           request.oldCard().number()));
+           oldCardFromRequest.number()));
}
```

### 4.7 Wildcards and Type Inference Edge Cases

```java
// ❌ wildcard can hide nullable element assumptions
public void process(Stream<?> data) {
    data.forEach(item -> System.out.println(item.toString()));
}
```

```diff
// ✅ use explicit non-null bound
- public void process(Stream<?> data) {
+ public void process(Stream<? extends @NotNull Object> data) {
    data.forEach(item -> System.out.println(item.toString()));
}
```

```java
public <T> ImmutableList<T> getEvents(Class<T> eventType) {
    return Immutable.list.fromStream(
        eventQueue.stream()
            .filter(eventType::isInstance)
            .map(eventType::cast)
    );
}
```

```diff
public <T> ImmutableList<T> getEvents(Class<T> eventType) {
    return Immutable.list.fromStream(
        eventQueue.stream()
            .filter(eventType::isInstance)
-           .map(eventType::cast)
+           .map(e -> eventType.cast(e))
    );
}
```

## 5. Checker Framework Stubs (`api/src/main/stubs`)

Checker Framework needs nullability contracts for third-party APIs. Many libraries do not provide full annotations.

Stubs provide those contracts.

How it works:
- Maven compiler passes `-Astubs=${basedir}/src/main/stubs`
- Checker uses `.astub` files to understand nullability of external APIs

Why this matters:
- reduces false positives and false negatives
- makes nullness checking predictable across dependency upgrades

Current stub examples include contracts for:
- selected `java.*` types used in reflective/framework-heavy paths
- selected Liquibase classes used by migration tooling

## 6. When the Checker Crashes

Rare parser/inference crashes can happen on specific syntax forms.

Typical workarounds:
- replace method references with explicit lambdas
- break long stream pipelines into intermediate locals
- extract expressions into named methods

If needed, open an issue with a minimal reproducible example.

## 7. Local Validation Commands

```bash
cd api
./mvnw spotless:apply verify
```

Disable nullness checker only when explicitly needed during local debugging:

```bash
./mvnw spotless:apply verify -P '!nullness-checker'
```

Use this opt-out sparingly and re-enable before final validation.

## Navigation

- ⬅️ Previous: [5 - Build Toolchain and Quality Gates](05-build-toolchain-and-quality-gates.md)
- ➡️ Next: [7 - Data Persistence and Migrations](07-data-persistence-and-migrations.md)
