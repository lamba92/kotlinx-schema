package kotlinx.schema.generator.json.serialization

import io.kotest.assertions.json.shouldEqualJson
import kotlinx.serialization.SerialInfo
import kotlinx.serialization.Serializable
import kotlin.test.Test

class SerializationFunctionCallingJsonSchemaGeneratorTest {
    private val generator =
        SerializationClassJsonSchemaGenerator(
            introspectorConfig =
                SerializationClassSchemaIntrospector.Config(
                    descriptionExtractor = { annotations ->
                        annotations.filterIsInstance<CustomDescription>().firstOrNull()?.value
                    },
                ),
        )

    //region Test fixtures

    @Serializable
    @CustomDescription("Greets a person")
    data class GreetParams(
        @property:CustomDescription("Person's name")
        val name: String,
        @property:CustomDescription("Person's age")
        val age: Int?,
        val intVal: Int,
        val longVal: Long,
        val floatVal: Float? = null,
        val doubleVal: Double = 2.0,
    )

    @Serializable
    @CustomDescription("Process items with metadata")
    data class ProcessItemsParams(
        @property:CustomDescription("Items to process")
        val items: List<String>,
        @property:CustomDescription("Metadata map")
        val metadata: Map<String, Int>,
    )

    @Serializable
    enum class LogLevel { DEBUG, INFO, WARN, ERROR }

    @Serializable
    @CustomDescription("Log a message")
    data class LogParams(
        @property:CustomDescription("Message to log")
        val message: String,
        @property:CustomDescription("Log level")
        val level: LogLevel = LogLevel.INFO,
    )

    @Serializable
    @CustomDescription("Postal address")
    data class Address(
        @property:CustomDescription("Street name")
        val street: String,
        @property:CustomDescription("City name")
        val city: String,
    )

    @Serializable
    @CustomDescription("Update user address")
    data class UpdateAddressParams(
        @property:CustomDescription("User identifier")
        val userId: String,
        @property:CustomDescription("New address")
        val address: Address,
    )

    //endregion

    //region Test cases

    @Test
    fun `generates schema for function parameters with primitives and descriptions`() {
        val schema = generator.generateSchemaString(GreetParams.serializer().descriptor)

        schema shouldEqualJson
            // language=JSON
            $$"""
            {
              "$schema": "https://json-schema.org/draft/2020-12/schema",
              "$id": "kotlinx.schema.generator.json.serialization.SerializationFunctionCallingJsonSchemaGeneratorTest.GreetParams",
              "description": "Greets a person",
              "type": "object",
              "properties": {
                "name":     { "type": "string",            "description": "Person's name" },
                "age":      { "type": ["integer", "null"], "description": "Person's age"  },
                "intVal":   { "type": "integer" },
                "longVal":  { "type": "integer" },
                "floatVal": { "type": ["number", "null"] },
                "doubleVal": { "type": "number" }
              },
              "required": ["name", "age", "intVal", "longVal"],
              "additionalProperties": false
            }
            """.trimIndent()
    }

    @Test
    fun `generates schema for function parameters with collections and descriptions`() {
        val schema = generator.generateSchemaString(ProcessItemsParams.serializer().descriptor)

        schema shouldEqualJson
            // language=JSON
            $$"""
            {
              "$schema": "https://json-schema.org/draft/2020-12/schema",
              "$id": "kotlinx.schema.generator.json.serialization.SerializationFunctionCallingJsonSchemaGeneratorTest.ProcessItemsParams",
              "description": "Process items with metadata",
              "type": "object",
              "properties": {
                "items": {
                  "type": "array",
                  "description": "Items to process",
                  "items": { "type": "string" }
                },
                "metadata": {
                  "type": "object",
                  "description": "Metadata map",
                  "additionalProperties": { "type": "integer" }
                }
              },
              "required": ["items", "metadata"],
              "additionalProperties": false
            }
            """.trimIndent()
    }

    @Test
    fun `generates schema for function parameters with enum and descriptions`() {
        val schema = generator.generateSchemaString(LogParams.serializer().descriptor)

        schema shouldEqualJson
            // language=JSON
            $$"""
            {
              "$schema": "https://json-schema.org/draft/2020-12/schema",
              "$id": "kotlinx.schema.generator.json.serialization.SerializationFunctionCallingJsonSchemaGeneratorTest.LogParams",
              "description": "Log a message",
              "type": "object",
              "properties": {
                "message": { "type": "string", "description": "Message to log" },
                "level":   { "$ref": "#/$defs/kotlinx.schema.generator.json.serialization.SerializationFunctionCallingJsonSchemaGeneratorTest.LogLevel", "description": "Log level" }
              },
              "required": ["message"],
              "additionalProperties": false,
              "$defs": {
                "kotlinx.schema.generator.json.serialization.SerializationFunctionCallingJsonSchemaGeneratorTest.LogLevel": {
                  "type": "string",
                  "enum": ["DEBUG", "INFO", "WARN", "ERROR"]
                }
              }
            }
            """.trimIndent()
    }

    @Test
    fun `generates schema for function parameters with nested serializable and descriptions`() {
        val schema = generator.generateSchemaString(UpdateAddressParams.serializer().descriptor)

        schema shouldEqualJson
            // language=JSON
            $$"""
            {
              "$schema": "https://json-schema.org/draft/2020-12/schema",
              "$id": "kotlinx.schema.generator.json.serialization.SerializationFunctionCallingJsonSchemaGeneratorTest.UpdateAddressParams",
              "description": "Update user address",
              "type": "object",
              "properties": {
                "userId":  { "type": "string", "description": "User identifier" },
                "address": { "$ref": "#/$defs/kotlinx.schema.generator.json.serialization.SerializationFunctionCallingJsonSchemaGeneratorTest.Address", "description": "New address" }
              },
              "required": ["userId", "address"],
              "additionalProperties": false,
              "$defs": {
                "kotlinx.schema.generator.json.serialization.SerializationFunctionCallingJsonSchemaGeneratorTest.Address": {
                  "type": "object",
                  "description": "Postal address",
                  "properties": {
                    "street": { "type": "string", "description": "Street name" },
                    "city":   { "type": "string", "description": "City name"   }
                  },
                  "required": ["street", "city"],
                  "additionalProperties": false
                }
              }
            }
            """.trimIndent()
    }

    //endregion
}
