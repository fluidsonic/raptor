# entities / entities-core (deprecated) — ID encoding and resolution

`modules/entities` and `modules/entities-core` are both deprecated and slated for deletion
(build files carry `// TODO Deprecated module. Delete.`). Do not build new work on them.
Captured here because the ID encoding differences are easy to get wrong if you must touch
existing code.

- **An entity ID serializes differently in BSON vs GraphQL.** BSON writes a sub-document
  `{type: <discriminator>, id: <value>}` (`RaptorTypedEntityId.bsonDefinition`); GraphQL
  serializes the same ID as a single string `"<discriminator>:<value>"` and parses with
  `substringBefore(":")` (`RaptorEntityId.graphDefinition`). There is no shared constant tying
  the two formats together. Anchors: `entities/sources-jvm/bson/RaptorTypedEntityId.bson.kt`,
  `entities/sources-jvm/graphql/RaptorEntityId.graphql.kt`.
- **BSON ID storage is heterogeneous.** `RaptorEntityId.Definition.bsonDefinition()` checks
  `ObjectId.isValid(stringValue)`: a valid 24-hex ObjectId string is written as a native BSON
  `ObjectId`, otherwise as a BSON string. So within one collection the same logical ID field can
  have two BSON types depending on the value — a Mongo query for a string won't match an
  ObjectId-typed field. The default ID factory (`RaptorEntityBsonIdFactory`) mints
  `org.bson.types.ObjectId().toHexString()`, so the default identity scheme is ObjectId hex even
  though `RaptorEntityId` is a storage-agnostic string abstraction.
- **entities-core ID wire format** (`RaptorEntityId.Typed`): encoding prepends
  `"discriminator:"`; decode splits on the **first** colon (values may contain further colons);
  discriminator mismatch → null; empty value → null. `Typed.equals` compares on
  `this::class == other::class && value == other.value` (discriminator ignored); `hashCode` is
  `value.hashCode()` only. Declare a descriptor with the infix `::MyId by "discriminator"`.
- **Resolvers are wired by KType into DI, not stored as instances.** `resolver<Id, Resolver>()`
  only records a `KClass<Id> → KType(Resolver)` mapping; at completion a single
  `RaptorAnyEntityResolver` is provided in DI and later `di.get(resolverType)` per id-class. The
  concrete `Resolver` must be separately provided in DI or resolution fails at runtime, not
  assembly time. `RaptorEntityRepository.query(ids)` restores input order and throws
  `ServerFailure.ofUser(code = "not found")` if any id is missing (unlike the lenient
  `queryOrSkip`).

Part of the entity graph wiring is commented out (`RaptorEntitiesPlugin.complete` graph block;
`RaptorEntityIdDefinition.kt`, `RaptorEntityId.jvm.kt` files) — the live path uses a different
nested `RaptorEntityId.Definition` type.
</content>
