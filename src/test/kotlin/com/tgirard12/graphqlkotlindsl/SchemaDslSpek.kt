package com.tgirard12.graphqlkotlindsl

import Stubs
import com.tgirard12.graphqlkotlindsl.graphqljava.*
import com.tgirard12.graphqlkotlindsl.models.*
import graphql.Scalars
import graphql.schema.CoercingSerializeException
import graphql.schema.idl.RuntimeWiring
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

object SchemaDslSpek : Spek({
    describe("Scalar") {
        it("should generate double scalar") {
            val expectedSchema = """
                schema {
                }
                
                scalar Double
            """
            val actualSchema = schemaDsl {
                scalar<Double>()
            }
            assertEquals(expectedSchema.replaceIndent(), actualSchema.schemaString().trimIndent())
        }
        it("should generate scalars with descriptions") {
            val expectedSchema = """
                schema {
                }
                
                scalar Double
                scalar LocalDateTime
                # The ID
                scalar UUID
            """
            val actualSchema = schemaDsl {
                scalar<Double>()
                scalar<UUID>(scalarDescription = "The ID")
                scalar<LocalDateTime>()
            }
            assertEquals(expectedSchema.replaceIndent(), actualSchema.schemaString().trimIndent())
        }
        it("should add dataFetcher to a field") {
            val schema = schemaDsl {
                scalar<Double> {
                    GqlJavaScalars.double
                }
            }
            assertTrue(schema.scalars.any { it.name == "Double" })
        }
    }
    describe("Enum") {
        it("should generate enums") {
            val expectedSchema = """
                schema {
                }

                # An enum
                enum SimpleEnum {
                    val1
                    VAL_2
                    enum
                }
                
                # My Description
                enum SimpleEnum {
                    val1
                    VAL_2
                    enum
                }
            """
            val actualSchema = schemaDsl {
                enum<SimpleEnum> {
                    description = "An enum"
                }
                enum<SimpleEnum>(enumDescription = "My Description") {

                }
            }
            assertEquals(expectedSchema.replaceIndent(), actualSchema.schemaString().trimIndent())
        }
    }
    describe("Query") {
        it("should generate simple type") {
            val expectedSchema = """
                schema {
                    query: QueryType
                }

                type QueryType {
                    string: String!
                }
            """
            val actualSchema = schemaDsl {
                query<String> { }
            }
            assertEquals(expectedSchema.trimIndent(), actualSchema.schemaString().trimIndent())
        }
        it("should generate custom type") {
            val expectedSchema = """
                schema {
                    query: QueryType
                }
                
                type QueryType {
                    # Query on String
                    myString: String!
                    # One SimpleType
                    simpleTypes: SimpleTypes!
                    string: String!
                }
            """
            val actualSchema = schemaDsl {
                query<String> { }
                query<String>("myString", "Query on String") { }
                query<SimpleTypes> {
                    name = "simpleTypes"
                    description = "One SimpleType"
                }
            }
            assertEquals(expectedSchema.trimIndent(), actualSchema.schemaString().trimIndent())
        }
        it("should generate nullable type") {
            val expectedSchema = """
                schema {
                    query: QueryType
                }
                
                type QueryType {
                    myString: String
                }
            """
            val actualSchema = schemaDsl {
                query<String>("myString") {
                    returnTypeNullable = true
                }
            }
            assertEquals(expectedSchema.trimIndent(), actualSchema.schemaString().trimIndent())
        }
        it("should generate field with arguments") {
            val expectedSchema = """
                schema {
                    query: QueryType
                }
                
                type QueryType {
                    string(string: String, type: SimpleTypes!): String!
                }
            """
            val actualSchema = schemaDsl {
                query<String> {
                    arg<String> { nullable = true }
                    arg<SimpleTypes>("type") { }
                }
            }
            assertEquals(expectedSchema.trimIndent(), actualSchema.schemaString().trimIndent())
        }
        it("should generate custom query name") {
            val expectedSchema = """
                schema {
                    query: QueryType
                }
                
                type QueryType {
                    myQuery(string: String): String!
                    secondQuery(type: SimpleTypes!, count: Int): String!
                }
            """
            val actualSchema = schemaDsl {
                query<String>("myQuery") {
                    arg<String> { nullable = true }
                }
                query<String> {
                    name = "secondQuery"
                    arg<SimpleTypes>("type") { }
                    arg<Int> {
                        name = "count"
                        nullable = true
                    }
                }
            }
            assertEquals(expectedSchema.trimIndent(), actualSchema.schemaString().trimIndent())
        }
        it("should fail generate list") {
            assertFailsWith(IllegalArgumentException::class) {
                schemaDsl {
                    query<List<String>> { }
                }
            }
        }
        it("should generate generate list") {
            val expectedSchema = """
                schema {
                    query: QueryType
                }
                
                type QueryType {
                    myQuery: [String]!
                    myQuery2: [String]
                }
            """
            val actualSchema = schemaDsl {
                query<Unit>("myQuery") {
                    returnType = "[String]"
                }
                query<Unit>("myQuery2") {
                    returnType = "[String]"
                    returnTypeNullable = true
                }
            }
            assertEquals(expectedSchema.trimIndent(), actualSchema.schemaString().trimIndent())
        }
        it("should add dataFetcher to a field") {
            val schema = schemaDsl {
                query<LocalDateTime> {
                    asyncDataFetcher { LocalDateTime.now() }
                }
            }
            assertTrue(schema.queries.any { it.name == "localDateTime" })
        }
    }
    describe("Mutation") {
        it("generate a simple type") {
            val expectedSchema = """
                schema {
                    mutation: MutationType
                }
                
                type MutationType {
                    string: String!
                }
            """
            val actualSchema = schemaDsl {
                mutation<String> { }
            }
            assertEquals(expectedSchema.trimIndent(), actualSchema.schemaString().trimIndent())
        }
        it("generate a simple type with name and description") {
            val expectedSchema = """
                schema {
                    mutation: MutationType
                }
                
                type MutationType {
                    # String mutation
                    myString: String!
                    # Update a SimpleType
                    simpleTypes: SimpleTypes!
                    string: String!
                }
            """
            val actualSchema = schemaDsl {
                mutation<String> { }
                mutation<String>("myString", mutationDescription = "String mutation") { }
                mutation<SimpleTypes> {
                    name = "simpleTypes"
                    description = "Update a SimpleType"
                }
            }
            assertEquals(expectedSchema.trimIndent(), actualSchema.schemaString().trimIndent())
        }
        it("generate a nullable return type") {
            val expectedSchema = """
                schema {
                    mutation: MutationType
                }
                
                type MutationType {
                    myString: String
                }
            """
            val actualSchema = schemaDsl {
                mutation<String>("myString") {
                    returnTypeNullable = true
                }
            }
            assertEquals(expectedSchema.trimIndent(), actualSchema.schemaString().trimIndent())
        }
        it("should generate a field with one argument") {
            val expectedSchema = """
                schema {
                    mutation: MutationType
                }

                type MutationType {
                    string(string: String!): String!
                }
            """
            val actualSchema = schemaDsl {
                mutation<String> {
                    arg<String> { }
                }
            }
            assertEquals(expectedSchema.trimIndent(), actualSchema.schemaString().trimIndent())
        }
        it("should generate a field with arguments") {
            val expectedSchema = """
                schema {
                    mutation: MutationType
                }

                type MutationType {
                    string(string: String, type: SimpleTypes!): String!
                }
            """
            val actualSchema = schemaDsl {
                mutation<String> {
                    arg<String> { nullable = true }
                    arg<SimpleTypes>("type") { }
                }
            }
            assertEquals(expectedSchema.trimIndent(), actualSchema.schemaString().trimIndent())
        }
        it("generate a custom mutation field") {
            val expectedSchema = """
                schema {
                    mutation: MutationType
                }
                
                type MutationType {
                    myMutation(string: String): String!
                    secondMutation(type: SimpleTypes!, count: Int): String!
                }
            """
            val actualSchema = schemaDsl {
                mutation<String>("myMutation") {
                    arg<String> { nullable = true }
                }
                mutation<String> {
                    name = "secondMutation"
                    arg<SimpleTypes>("type") { }
                    arg<Int> {
                        name = "count"
                        nullable = true
                    }
                }
            }
            assertEquals(expectedSchema.trimIndent(), actualSchema.schemaString().trimIndent())
        }
        it("should generate list for return type") {
            val expectedSchema = """
                schema {
                    mutation: MutationType
                }
                
                type MutationType {
                    myMutation: [String]!
                    myMutation2: [String]
                }
            """
            val actualSchema = schemaDsl {
                mutation<Unit>("myMutation") {
                    returnType = "[String]"
                }
                mutation<Unit>("myMutation2") {
                    returnType = "[String]"
                    returnTypeNullable = true
                }
            }
            assertEquals(expectedSchema.trimIndent(), actualSchema.schemaString().trimIndent())
        }
        it("should fail if type is List<*>") {
            assertFailsWith(IllegalArgumentException::class) {
                schemaDsl {
                    mutation<List<String>> { }
                }
            }
        }
        it("should add dataFetcher") {
            val schema = schemaDsl {
                mutation<LocalDateTime> {
                    asyncDataFetcher { LocalDateTime.now() }
                }
            }
            val action = requireNotNull(schema.mutations.find { it.name == "localDateTime" })
            assertNotNull(action.dataFetcher)
        }
    }
    describe("Type") {
        it("should generate simple type") {
            val expectedSchema = """
                schema {
                }
                
                type SimpleTypes {
                    double: Double!
                    doubleNull: Double
                    float: Float!
                    floatNull: Float
                    int: Int!
                    intNull: Int
                    long: Long!
                    longNull: Long
                    string: String!
                    stringNull: String
                    user: User
                    uuid: UUID!
                    uuidNull: UUID
                }
            """
            val actualSchema = schemaDsl {
                type<SimpleTypes> { }
            }
            assertEquals(expectedSchema.trimIndent(), actualSchema.schemaString().trimIndent())
        }
        it("should generate list of simple type") {
            val expectedSchema = """
                schema {
                }

                type ListTypes {
                    ints: [Int]!
                    intsNull: [Int]
                }
            """
            val actualSchema = schemaDsl {
                type<ListTypes> { }
            }
            assertEquals(expectedSchema.trimIndent(), actualSchema.schemaString().trimIndent())
        }
        it("should generate list of simple type with description") {
            val expectedSchema = """
                schema {
                }
                
                # List Type
                type ListTypes {
                    ints: [Int]!
                    intsNull: [Int]
                }
            """
            val actualSchema = schemaDsl {
                type<ListTypes>(typeDescription = "List Type") {}
            }
            assertEquals(expectedSchema.trimIndent(), actualSchema.schemaString().trimIndent())
        }
        it("should fail if type has 2 names") {
            assertFailsWith(IllegalArgumentException::class, "Description 'Ints descr 2' on type 'ListTypes.ints' does not exist") {
                schemaDsl {
                    type<ListTypes> {
                        desc("ints", "Ints descr 1")
                        desc("ints", "Ints descr 2")
                    }
                }
            }
        }
        it("should fail if field name not exists") {
            assertFailsWith(IllegalArgumentException::class, "Type 'ListTypes.intNotExist' does not exist") {
                schemaDsl {
                    type<ListTypes> {
                        desc("intNotExist", "Ints Not Exist")
                    }
                }
            }
        }
        it("should generate field with description") {
            val expectedSchema = """
                schema {
                }
                
                type ListTypes {
                    # Ints description
                    ints: [Int]!
                    intsNull: [Int]
                }
            """
            val actualSchema = schemaDsl {
                type<ListTypes> {
                    desc("ints", "Ints description")
                }
            }
            assertEquals(expectedSchema.trimIndent(), actualSchema.schemaString().trimIndent())
        }
        it("should generate field with description") {
            val expectedSchema = """
                schema {
                }
                
                type ListTypes {
                    ints: [Int]!
                    intsNull: [Int]
                
                    # Long description
                    countLong: Long
                    int: Int!
                    # string decr
                    stringField: String!
                }
            """
            val actualSchema = schemaDsl {
                type<ListTypes> {
                    addField<Int> {}
                    addField<Long> {
                        name = "countLong"
                        description = "Long description"
                        nullable = true
                    }
                    addField<String>(name = "stringField", description = "string decr") { }
                }
            }
            assertEquals(expectedSchema.trimIndent(), actualSchema.schemaString().trimIndent())
        }
        it("should drop a field") {
            val expectedSchema = """
                schema {
                }

                type ListTypes {
                    ints: [Int]!
                }
            """
            val actualSchema = schemaDsl {
                type<ListTypes> {
                    dropField("intsNull")
                }
            }
            assertEquals(expectedSchema.trimIndent(), actualSchema.schemaString().trimIndent())
        }
        it("should fail if field name not exists") {
            assertFailsWith(IllegalArgumentException::class, "Type 'ListTypes.intNotExist' does not exist") {
                schemaDsl {
                    type<ListTypes> {
                        dropField("intsNotExist")
                    }
                }
            }
        }
        it("should fail if field name not exists") {
            val schema = schemaDsl {
                type<SimpleTypes> {
                    staticDataFetcher("user") {
                        Stubs.users[0]
                    }
                    asyncDataFetcher { LocalDateTime.now() }
                }
            }
            val type = requireNotNull(schema.types.find { it.name == "SimpleTypes" })
            type.dataFetcher.let {
                assertEquals(2, it.size)
                assertNotNull(it["user"])
                assertNotNull(it["localDateTime"])
            }
        }
    }
    describe("Complex") {
        it("should generate a complex schema") {
            val expectedSchema = """
                schema {
                    query: QueryType
                    mutation: MutationType
                }
                
                type QueryType {
                    # Number of element
                    count: Int!
                    double: Double!
                    float: Float
                    id: UUID!
                    long: Long!
                    # Current DateTime
                    now: LocalDateTime!
                    simpleEnum: [SimpleEnum]!
                    string: String!
                    type(id: UUID!): SimpleTypes!
                    types(count: Int!, name: String): [SimpleTypes]!
                    typesId: [UUID]!
                }
                
                type MutationType {
                    # Update count
                    count: Int!
                    double: Double!
                    float: Float
                    # Update UUID
                    id: UUID!
                    long: Long!
                    now: LocalDateTime!
                    simpleEnum: SimpleEnum!
                    # Update the SimpleType
                    simpleTypes(long: Long!, double: Double!, simpleEnum: SimpleEnum): SimpleTypes!
                    string: String!
                }
                
                scalar Double
                scalar LocalDateTime
                scalar UUID
                
                # An enum
                enum SimpleEnum {
                    val1
                    VAL_2
                    enum
                }
                
                # List Types
                type ListTypes {
                    # Ints description
                    ints: [Int]!
                    intsNull: [Int]
                
                    # Long description
                    countLong: Long
                    int: Int!
                    # string decr
                    stringField: String!
                }
                
                # Simple Types
                type SimpleTypes {
                    double: Double!
                    doubleNull: Double
                    float: Float!
                    floatNull: Float
                    int: Int!
                    intNull: Int
                    long: Long!
                    longNull: Long
                    string: String!
                    stringNull: String
                    user: User
                    uuid: UUID!
                    uuidNull: UUID
                }
                
                type User {
                    email: String!
                    id: UUID!
                    name: String!
                }
            """
            val actualSchema = schemaDsl {

                // Scalar
                scalar<Double>()
                scalar<UUID>()
                scalar<LocalDateTime>()

                // Types
                type<SimpleTypes>("Simple Types") { }
                type<ListTypes> {
                    desc("ints", "Ints description")
                    description = "List Types"

                    addField<Int> {}
                    addField<Long> {
                        name = "countLong"
                        description = "Long description"
                        nullable = true
                    }
                    addField<String>(name = "stringField", description = "string decr") { }
                }
                type<User> {
                    dropField("deleteField")
                }

                // Enum
                enum<SimpleEnum>(enumDescription = "An enum") { }

                // Simple query
                query<String> { }
                query<Int>(queryDescription = "Number of element") { name = "count" }
                query<Long> { }
                query<Float> { returnTypeNullable = true }
                query<Double> { }
                query<UUID> { name = "id" }
                query<LocalDateTime> {
                    name = "now"
                    description = "Current DateTime"
                }

                // complex queries
                query<Unit> {
                    name = "typesId"
                    returnType = "[UUID]"
                }
                query<Unit> {
                    name = "types"
                    returnType = "[SimpleTypes]"

                    arg<Int> { name = "count" }
                    arg<String> {
                        name = "name"
                        nullable = true
                    }
                }
                query<SimpleTypes> {
                    name = "type"

                    arg<UUID> { name = "id" }
                }
                query<SimpleEnum> { returnType = "[SimpleEnum]" }

                // Mutations
                mutation<String> { }
                mutation<Int>(mutationDescription = "Update count") { name = "count" }
                mutation<Long> { returnType = "Long" }
                mutation<Float> { returnTypeNullable = true }
                mutation<Double> { }
                mutation<UUID> {
                    name = "id"
                    description = "Update UUID"
                }
                mutation<LocalDateTime> { name = "now" }

                mutation<SimpleEnum> { }
                mutation<SimpleTypes>(mutationDescription = "Update the SimpleType") {
                    arg<Long> { }
                    arg<Double> { }
                    arg<SimpleEnum> { nullable = true }
                }
            }
            actualSchema.graphQLSchema(RuntimeWiring.newRuntimeWiring()
                    .scalar(GqlJavaScalars.uuid)
                    .scalar(GqlJavaExtensions.scalarTypeDsl<Double>(Scalars.GraphQLFloat.coercing) { })
                    .scalar(GqlJavaExtensions.scalarTypeDsl<LocalDateTime> {
                        serialize {
                            when (it) {
                                is String -> it.toString()
                                else -> throw CoercingSerializeException("serialize expected type 'LocalDateTime' " +
                                        "but was ${it?.javaClass?.simpleName ?: "NULL"}")
                            }
                        }
                        parseValue {
                            when (it) {
                                is String -> LocalDateTime.parse(it)
                                else -> throw CoercingSerializeException("parseValue expected type 'String' " +
                                        "but was ${it?.javaClass?.simpleName ?: "NULL"}")
                            }
                        }
                    })
                    .build())
            assertEquals(expectedSchema.trimIndent(), actualSchema.schemaString().trimIndent())
        }
        it("should generate empty schema") {
            val expectedSchema = """
                schema {
                }
            """
            val actualSchema = schemaDsl {}
            assertEquals(expectedSchema.trimIndent(), actualSchema.schemaString().trimIndent())
        }
    }
})