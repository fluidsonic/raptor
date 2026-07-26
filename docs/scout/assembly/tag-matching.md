# Component tag matching is deferred and OR-only

Behavior of `tagged(...) { }` component configurations.

`RaptorComponentTagRegistry` queues a `tagged{}` configuration as pending if none of its
`tagsToMatch` are present yet, then replays matching pending configurations whenever
`addTags()` later adds a matching tag. So a `tagged(X){...}` block applies whether tag `X`
was added *before or after* the configuration was declared. Matching is
`tagsToMatch.any(tags::contains)` — **OR** across the requested tags, never AND. Multiple
`// TODO Support multiple tags with OR combination` comments in
`modules/core/sources-jvm/assembly/RaptorTaggableComponent.kt`
confirm true multi-tag combinators are unbuilt. Anchors:
`modules/core/sources-jvm/assembly/RaptorComponentTagRegistry.kt` (`addConfiguration`,
`addTags`).

**Latent bug:** in `modules/core/sources-jvm/assembly/RaptorTaggableComponent.kt` the
`RaptorAssemblyQuery<Component>.tagged(vararg tags: Any, configure)` overload calls
`tagged(tags)(configure)` — passing the whole array as one `Any` argument instead of
`tagged(*tags)(configure)`. Because `tagged(vararg tags: Any)` accepts `Any`, the array is
wrapped as a single tag object, so `tagsToMatch` becomes the array instance rather than the
individual tags and the configuration never matches. The sibling
`RaptorComponentSet<Component>.tagged` overload correctly spreads with `*tags`.
