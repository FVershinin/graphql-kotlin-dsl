package com.tgirard12.graphqlkotlindsl

import com.tgirard12.graphqlkotlindsl.models.SimpleEnum
import io.kotlintest.KTestJUnitRunner
import io.kotlintest.specs.WordSpec
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class EnumDslTest : WordSpec() {

    init {
        "EnumDslTest schema" should {
            "print enum" {
                schemaDsl {
                    enum<SimpleEnum> { }
                } schemaEqual """
schema {
}

enum SimpleEnum {
    val1
    VAL_2
    enum
}
"""
            }
        }
    }
}