package com.tgirard12.graphqlkotlindsl

import com.tgirard12.graphqlkotlindsl.graphqljava.*
import com.tgirard12.graphqlkotlindsl.models.Right
import com.tgirard12.graphqlkotlindsl.models.SimpleTypes
import com.tgirard12.graphqlkotlindsl.models.User
import graphql.ExecutionInput
import graphql.GraphQLError
import graphql.execution.AsyncExecutionStrategy
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.util.*
import kotlin.test.assertEquals

object ExecutionSpek : Spek({
    describe("Execution") {
        val schema = schemaDsl {
            scalar<Double>()
            scalar<UUID>()
            type<User> { }
            type<SimpleTypes> { }
            enum<Right> { }
            query<User> {
                arg<UUID> { name = "id" }
                returnTypeNullable = true
            }
            query<Unit> {
                name = "users"
                returnType = "[User]"
            }
            query<Unit>("typeByNames") {
                returnType = "[SimpleTypes]"
                arg<String>("name") {
                    nullable = true
                }
                arg<Int> {
                    name = "count"
                }
            }
            mutation<User> {
                name = "updateUser"
                arg<String>("name") {
                    nullable = true
                }
                arg<String>("email") { }
            }
        }
        val graphql = schema.graphQL({
            scalarUUID()
            scalarDouble()
            queryType {
                asyncDataFetcher("user") { e ->
                    e.arguments["id"]?.let { id ->
                        Stubs.users.firstOrNull {
                            id == it.id
                        }
                    }
                }
                staticDataFetcher("users") { Stubs.users }
                asyncDataFetcher("typeByNames") { e ->
                    e.arguments["count"]?.let { count ->
                        if (count is Int) Stubs.simpleTypes.take(count)
                        else null
                    }
                }
            }
            mutationType {
                asyncDataFetcher("updateUser") { e ->
                    User(id = UUID.fromString("773b29ba-6b2b-49fe-8cb1-36134689c458"),
                            name = e.arguments["name"] as String? ?: "",
                            email = e.arguments["email"] as String,
                            deleteField = 2)
                }
            }
            type<SimpleTypes> {
                asyncDataFetcher("user") { Stubs.users[0] }
            }
        }, { queryExecutionStrategy(AsyncExecutionStrategy()) })
        it("should execute query: users") {
            val execution = graphql.executeAsync(
                    ExecutionInput.newExecutionInput()
                            .query("""query users {
                                         |   users {
                                         |       id
                                         |       name
                                         |       email
                                         |   }
                                         |}""".trimMargin())
                            .operationName("users")
                            .build())
                    .get()
            assertEquals(listOf<GraphQLError>(), execution.errors)

            val usersRes = execution.getData<Map<String, List<Map<String, Any>>>>()["users"]!!
            assertEquals(2, usersRes.size)
            usersRes[0].let {
                assertEquals(it["id"], "b6214ea0-fc5a-493c-91ea-939e17b2e95f")
                assertEquals(it["name"], "John")
                assertEquals(it["email"], "john@mail.com")
            }
            usersRes[1].let {
                assertEquals(it["id"], "c682a4c5-e66b-4dbf-a077-d97579c308dc")
                assertEquals(it["name"], "Doe")
                assertEquals(it["email"], "doe@mail.com")
            }
        }
        it("should execute query: user by id") {
            val execution = graphql.executeAsync(
                    ExecutionInput.newExecutionInput()
                            .query("""query user {
                                         |   user(id: "b6214ea0-fc5a-493c-91ea-939e17b2e95f") {
                                         |       id
                                         |       name
                                         |       email
                                         |   }
                                         |}""".trimMargin())
                            .operationName("user")
                            .build())
                    .get()
            assertEquals(listOf<GraphQLError>(), execution.errors)

            val user = execution.getData<Map<String, Map<String, Any>>>().getValue("user")
            assertEquals(3, user.size)
            assertEquals(user["id"], "b6214ea0-fc5a-493c-91ea-939e17b2e95f")
            assertEquals(user["name"], "John")
            assertEquals(user["email"], "john@mail.com")
        }
        it("should execute query: query simpleType") {
            val execution = graphql.executeAsync(
                    ExecutionInput.newExecutionInput()
                            .query("""query typeByNames {
                                         |   typeByNames(count: 2) {
                                         |       int
                                         |       intNull
                                         |       long
                                         |       longNull
                                         |       float
                                         |       floatNull
                                         |       double
                                         |       doubleNull
                                         |       string
                                         |       stringNull
                                         |       uuid
                                         |       uuidNull
                                         |       user {
                                         |          id
                                         |          name
                                         |          email
                                         |       }
                                         |   }
                                         |}""".trimMargin())
                            .operationName("typeByNames")
                            .build())
                    .get()
            assertEquals(listOf<GraphQLError>(), execution.errors)

            val types = execution.getData<Map<String, List<Map<String, Any>>>>()["typeByNames"]!!
            assertEquals(types.size, 2)
            types[0].let {
                assertEquals(it["int"], 1)
                assertEquals(it["intNull"], 2)
                assertEquals(it["long"], 3L)
                assertEquals(it["longNull"], 4L)
                assertEquals(it["float"], 5.1)
                assertEquals(it["floatNull"], 5.2)
                assertEquals(it["double"], 6.1)
                assertEquals(it["doubleNull"], 6.2)
                assertEquals(it["string"], "val")
                assertEquals(it["stringNull"], "null val")
                assertEquals(it["uuid"], "dac5310f-484b-4f81-9756-bce0349ceaa5")
                assertEquals(it["uuidNull"], "acb53d26-3cba-4177-ba54-88232b5066c5")
                (it["user"] as Map<*, *>).let {
                    assertEquals(it["id"], "b6214ea0-fc5a-493c-91ea-939e17b2e95f")
                    assertEquals(it["name"], "John")
                    assertEquals(it["email"], "john@mail.com")
                }
            }
        }
        it("should execute mutation: updateUser") {
            val mutation = graphql.executeAsync(
                    ExecutionInput.newExecutionInput()
                            .query("""mutation updateUser {
                            |   updateUser(name: "john doe", email: "john.doe@mail.com") {
                            |       id
                            |       name
                            |       email
                            |   }
                            |}""".trimMargin())
                            .operationName("updateUser")
                            .build())
                    .get()
                    .getData<Map<String, Map<String, Any>>>()["updateUser"]!!
            mutation.let {
                assertEquals(it["id"], "773b29ba-6b2b-49fe-8cb1-36134689c458")
                assertEquals(it["name"], "john doe")
                assertEquals(it["email"], "john.doe@mail.com")
            }
        }
    }
    describe("Readme Schema") {
        it("should generate schema and execute query") {
            val expectedSchema = """
                schema {
                    query: QueryType
                    mutation: MutationType
                }
                
                type QueryType {
                    # User By Id
                    user(id: UUID!): User!
                    # All Users
                    users: [User]!
                }
                
                type MutationType {
                    # Update a user
                    updateUser(count: Int, name: String!): User!
                }
                
                scalar Double
                scalar UUID
                
                # An enum
                enum Right {
                    read
                    write
                    execute
                }
                
                # An User
                type User {
                    # User Email
                    email: String!
                    id: UUID!
                    name: String!
                
                    otherName: String!
                    # User Right
                    right: Right
                }
            """
            val actualSchema = schemaDsl {
                scalar<Double> { GqlJavaScalars.double }
                scalar<UUID> { GqlJavaScalars.uuid }
                type<User>(typeDescription = "An User") {
                    desc("email", "User Email")
                    addField<String>(name = "otherName") {
                        asyncDataFetcher<String>("otherName") {
                            "MyOtherName"
                        }
                    }
                    addField<Right> {
                        description = "User Right"
                        nullable = true
                        asyncDataFetcher<Right>("right") {
                            Right.execute
                        }
                    }
                    dropField("deleteField")
                }
                enum<Right>(enumDescription = "An enum") { }
                query<User>(queryDescription = "User By Id") {
                    arg<UUID> { name = "id" }
                    asyncDataFetcher { env ->
                        Stubs.users.firstOrNull { it.id == env.arguments["id"] }
                    }
                }
                query<Unit> {
                    name = "users"
                    description = "All Users"
                    returnType = "[User]"
                    staticDataFetcher {
                        Stubs.users
                    }
                }
                mutation<User>(mutationDescription = "Update a user") {
                    name = "updateUser"
                    arg<Int>("count") {
                        nullable = true
                    }
                    arg<String> { name = "name" }
                    asyncDataFetcher { env ->
                        User(UUID.randomUUID(), env.arguments["name"] as String, "email@gql.io", 5)
                    }
                }
            }
            assertEquals(expectedSchema.trimIndent(), actualSchema.schemaString().trimIndent())

            val graphQL = actualSchema.graphQL {
                queryExecutionStrategy(AsyncExecutionStrategy())
            }
            val queryRes = graphQL.execute("""
                    |query user {
                    |   user(id: "b6214ea0-fc5a-493c-91ea-939e17b2e95f") {
                    |       id
                    |       email
                    |       name
                    |       otherName
                    |       right
                    |   }
                    |}""".trimMargin())
            assertEquals(listOf<GraphQLError>(), queryRes.errors)
            assertEquals(hashMapOf(
                    "user" to hashMapOf(
                            "id" to "b6214ea0-fc5a-493c-91ea-939e17b2e95f",
                            "email" to "john@mail.com",
                            "name" to "John",
                            "right" to Right.execute.name,
                            "otherName" to "MyOtherName"
                    )
            ), queryRes.getData<Map<String, Map<String, Any>>>())
        }
    }
})