# PasswordHasher — self-describing colon-delimited hash format

The `security` module's `PasswordHasher` stores hashes in a custom 5-section format;
anything reading/migrating stored hashes must know the layout.

Format: `algorithmId:iterationCount:hashSize:base64(salt):base64(hash)`. Only
`sha512` → `PBKDF2WithHmacSHA512` is registered. Defaults: hash length 18 bytes,
iteration count 64000, salt length 24 bytes.

Verification re-derives using the iteration count and hash size **read back from the stored
string**, so hashes created with different parameters still verify — this enables parameter
migration over time. Comparison uses a constant-time `slowEquals`. The implementation is
adapted from crackstation.net (header comment).

None of this is visible from the public `createHash`/`verifyPassword` signatures. Anchors:
`modules/security/sources-jvm/PasswordHasher.kt` (`createHash`, `slowEquals`).
