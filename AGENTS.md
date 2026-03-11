# AGENTS.md
Style: telegraph; noun phrases ok; drop filler/grammar; min tokens, except for documentation which must be concise but clear and non-ambiguous.

You must read @AGENTS-GLOBAL.md for stack-agnostic instructions.

## Maven commands
- When running `./mvnw`, add these arguments right after `./mvnw`: `-Dorg.slf4j.simpleLogger.defaultLogLevel=warn -Dorg.slf4j.simpleLogger.showDateTime=false --batch-mode -DtestReporter.disable=true spotless:apply`
- If a command runs tests, request escalated permissions up front to allow Docker/Testcontainers
- Shell command timeout: default 20min for `./mvnw` (tests/builds); no retry unless still timing out.

## Java Instructions
- Prefer `Optional.orElseThrow` when unwrapping present `Optional` instead of `Optional.get`
- Prefer text blocks for multi‑line literals over `String.join`.
- When Checker Framework or NullAway is used in the project (check dependencies), skip defensive checks (eg. requireNonNull) on parameters unless null is explicitly allowed with `@Nullable`.
- For stream→immutable list, use `Immutable.list.fromStream(...)` over `ofAll(stream.toList())`
- Argument types: smallest-surface interface required; Iterable -> (List|Map|Set)Iterable -> (Mutable|Immutable)(List|Map|Set) as needed
- Return types: non-private -> most precise type (immutable by default, mutable only when required); private -> smallest interface required by callers
- Immutable default for non-private method signatures; avoid Mutable unless required
- Avoid private static helper factories that only wrap constructors; construct `new Violation(...)` inline instead.
- Use `Immutable.list.empty()` for empty immutable lists.
- Prefer `Immutable.list.of(...)` for single-item immutable lists over `Mutable.list.of(...).toImmutable()`.
- Rely on `instanceof` null behavior; avoid explicit null guard when it already yields false (e.g., `null instanceof X`).
- When uncertain about standard library behavior, verify against JDK source or docs before asserting.
- Require explicit access modifier for all class members; only private/protected/public allowed; package scope only allowed on types; exceptions: interface methods omit explicit public; interface fields omit explicit public static final; enum constructors omit explicit private; JUnit test classes and @Test methods must be package‑private (no modifier).
- Utility classes: do not add parameterless ctor; default-ctor coverage via `private static final String DEFAULT_CTOR_COVERAGE_CLASS_NAME = new ClassName().getClass().getName();`
- Prefer record for data holder with factory method when appropriate.

## Java Tests Instructions
- For Spring JPA slice + nested/contract base tests, prefer `@ParentNestedDataJpaTest` over `@DataJpaTest`
- For `@AssertQueryCount`, ensure every test method has matching `@Expected`
- Use AssertJ for all assertions; within assertion chains avoid `castNonNull` and assert non‑null with AssertJ—outside assertions, `castNonNull` is allowed when needed.
- Prefer `anySatisfy` over `anyMatch` for log/message assertions to keep AssertJ failure output detailed and allow multiple checks in one block.
- Use method references in AssertJ extracting calls, not string property names.
- Prefer fluent AssertJ wherever possible; for multi‑property object assertions, use `returns(...)/doesNotReturn(...)` chains; keep specialized fluent exception assertions.
- AssertJ: avoid `returns(true/false, ...)` for predicates; prefer `anySatisfy`/`allSatisfy`/`matches`/`satisfiesExactlyInAnyOrder`.
- AssertJ: for collection content, use `hasSize`/`containsExactly`/`containsExactlyInAnyOrder`/`singleElement` vs size/boolean equality.
- AssertJ: use `withinPercentage` for tolerance checks vs manual percent arithmetic.
- AssertJ: prefer `singleElement().satisfies(...)` for custom single-element assertions vs `getOnly()`.
- AssertJ: Prefer single AssertJ chain for ordered composite assertions (`extracting` + `containsExactly` + `tuple`) over multiple asserts
- Exception assertions: assign `assertThatThrownBy`/`assertThatCode` to local, blank line, then chain assertions.
- Always use `var` when an explicit type would wrap the SUT call across lines. Never leave a wrapped SUT call with explicit type; change it to `var` immediately. Otherwise use explicit type.
- WebTestClient: treat `exchange()` as Act, store response, assert after blank line.
- When tests target multiple methods from the same class, use nested test classes per method.
- Move nested helper classes into the nested test class when they are only used there.
