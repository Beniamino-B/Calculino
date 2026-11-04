package com.calculino.app.domain.engineering

import org.junit.Assert.assertTrue
import org.junit.Test

class JsonXmlFormatterTest {
    @Test
    fun prettyPrintsNestedJson() {
        val result = JsonXmlFormatter.formatJson("{\"a\":[1,true,null],\"b\":{\"c\":\"x\"}}")
        assertTrue(result is FormatResult.Success)
        val output = (result as FormatResult.Success).output
        assertTrue(output.contains("\n  \"a\": ["))
        assertTrue(output.contains("\n    1,"))
        assertTrue(output.contains("\n    true,"))
        assertTrue(output.contains("\n    null\n  ]"))
        assertTrue(output.contains("\n  \"b\": {"))
    }

    @Test
    fun reportsInvalidJson() {
        val result = JsonXmlFormatter.formatJson("{\"a\":1,}")
        assertTrue(result is FormatResult.Failure)
        assertTrue((result as FormatResult.Failure).message.contains("clave"))
    }

    @Test
    fun validatesAndFormatsXml() {
        val result = JsonXmlFormatter.formatXml("<root><item id=\"1\">value</item></root>")
        assertTrue(result is FormatResult.Success)
        val output = (result as FormatResult.Success).output
        assertTrue(output.startsWith("<root>"))
        assertTrue(output.contains("<item id=\"1\">value</item>"))
    }

    @Test
    fun reportsInvalidXml() {
        val result = JsonXmlFormatter.formatXml("<root><item></root>")
        assertTrue(result is FormatResult.Failure)
    }
}
