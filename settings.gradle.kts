rootProject.name = "raptor"

include("bson", "graphql", "ktor", "mongodb")

project(":bson").name = "raptor-bson"
project(":graphql").name = "raptor-graphql"
project(":ktor").name = "raptor-ktor"
project(":mongodb").name = "raptor-mongodb"
