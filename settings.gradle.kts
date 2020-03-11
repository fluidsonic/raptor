rootProject.name = "raptor"

include("bson", "core", "graphql", "ktor", "mongodb")

project(":bson").name = "raptor-bson"
project(":core").name = "raptor-core"
project(":graphql").name = "raptor-graphql"
project(":ktor").name = "raptor-ktor"
project(":mongodb").name = "raptor-mongodb"
