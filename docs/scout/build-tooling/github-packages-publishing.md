# GitHub Packages publish retries live in the plugin, not this repo

Why a snapshot `publish` run can fail with 409, and how to confirm a publish actually landed.

- Destination is the GitHub Packages Maven registry
  (`https://maven.pkg.github.com/fluidsonic/raptor`, Gradle repository name `github`),
  configured by the `io.fluidsonic.gradle` plugin, not declared in this file; auth is via
  `GITHUB_PACKAGES_AUTH_TOKEN`.
- GitHub Packages PUT is non-idempotent: a repeated PUT of an existing path answers 409
  Conflict, identical bytes or not, and GitHub sometimes stores the file yet still fails the
  response. Gradle's Maven publisher retries each PUT on transient network failures (socket
  errors, closed connection, HTTP 5xx; 3 attempts), which can turn a lost-but-stored response
  into a 409 that fails the `publish` task.
- A failed run can leave a partial snapshot build recorded as latest in GitHub's
  server-generated `maven-metadata.xml`; it is superseded once a later publish succeeds. Each
  `publish` run re-reads remote metadata and gets a fresh snapshot build number, so simply
  re-running `publish` (with `--continue`, so one artifact's failure doesn't abort the rest) is
  the safe recovery.
- With the `io.fluidsonic.gradle` plugin at 4.2.0, a failed SNAPSHOT publication to GitHub
  Packages is republished automatically, up to three attempts, each with a fresh build number;
  release publications and other repositories are unchanged. Root `build.gradle.kts` needs no
  publish-retry wrapper or `githubPublishParallelism` property for this — the retry lives in the
  plugin, not this repo.
- To confirm a snapshot publish landed completely, fetch each artifact's
  `<artifact>/<version>/maven-metadata.xml` and count `<snapshotVersion>` entries for the
  latest build. The log line "Cannot upload checksum … doesn't support SHA-256. This will not
  fail the build." is benign. Querying package status via GitHub's REST API needs a
  `read:packages`-scoped token — the default `gh` CLI login typically lacks it.
