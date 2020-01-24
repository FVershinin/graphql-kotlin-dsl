package com.tgirard12.graphqlkotlindsl

import com.tgirard12.graphqlkotlindsl.graphqljava.GqlJavaExtensions
import com.tgirard12.graphqlkotlindsl.graphqljava.GqlJavaScalars
import graphql.Scalars
import graphql.schema.CoercingSerializeException
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.util.*
import kotlin.reflect.jvm.jvmName
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

object ScalarSpek: Spek({
    describe("uuid") {
        val uuid = GqlJavaScalars.uuid
        it("should parse uuid") {
            val expectedUUID: UUID = UUID.randomUUID()
            val actualValue = uuid.coercing.parseValue(expectedUUID.toString())
            assertEquals(expectedUUID, actualValue)
        }
        it("should fail if parsable value is null") {
            assertFailsWith(CoercingSerializeException::class, "parseValue expected type UUID but was NULL") {
                uuid.coercing.parseValue(null)
            }
        }
        it("should serialize UUID") {
            val expectedUUID: UUID = UUID.randomUUID()
            val actualValue = uuid.coercing.serialize(expectedUUID)
            assertEquals(expectedUUID.toString(), actualValue.toString())
        }
        it("should fail if serialize value is null") {
            assertFailsWith(CoercingSerializeException::class, "serialize expected type UUID but was NULL") {
                uuid.coercing.parseValue(null)
            }
        }
    }
    describe("Scalar Type DSL") {
        it("should return a custom scalar type with description") {
            val coercing  = Scalars.GraphQLBoolean.coercing
            val scalarType = GqlJavaExtensions.scalarTypeDsl<Boolean>(coercing) {
                description = "description"
            }
            assertEquals("Boolean", scalarType.name)
            assertEquals("description", scalarType.description)
            assertEquals(coercing, scalarType.coercing)
        }
        it("should return a custom scalar type with custom name") {
            val coercing  = Scalars.GraphQLBoolean.coercing
            val scalarType = GqlJavaExtensions.scalarTypeDsl<Boolean>(coercing) {
                name = "customName"
            }
            assertEquals("customName", scalarType.name)
            assertEquals(coercing, scalarType.coercing)
        }
    }
})