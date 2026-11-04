package com.calculino.app.domain.engineering

import java.io.StringReader
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import org.xml.sax.InputSource

sealed interface FormatResult {
    data class Success(val output: String) : FormatResult
    data class Failure(val message: String) : FormatResult
}

object JsonXmlFormatter {
    fun formatJson(source: String): FormatResult = try {
        FormatResult.Success(JsonPrettyPrinter(source).format())
    } catch (exception: FormatException) {
        FormatResult.Failure(exception.message ?: "JSON inválido")
    }

    fun formatXml(source: String): FormatResult = try {
        val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            runCatching {
                setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            }
            runCatching {
                setFeature("http://xml.org/sax/features/external-general-entities", false)
            }
            runCatching {
                setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            }
            runCatching {
                setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            }
        }
        val document = factory.newDocumentBuilder().parse(InputSource(StringReader(source)))
        val root = document.documentElement ?: throw FormatException("XML sin elemento raíz")
        val transformer = TransformerFactory.newInstance().newTransformer().apply {
            setOutputProperty(OutputKeys.INDENT, "yes")
            setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
            setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
        }
        val writer = StringWriter()
        transformer.transform(DOMSource(root), StreamResult(writer))
        FormatResult.Success(writer.toString().trim())
    } catch (exception: Exception) {
        FormatResult.Failure(exception.message ?: "XML inválido")
    }
}

private class JsonPrettyPrinter(
    private val input: String,
) {
    private var index = 0
    private val output = StringBuilder()

    fun format(): String {
        skipWhitespace()
        if (index >= input.length) throw FormatException("JSON vacío")
        parseValue(0)
        skipWhitespace()
        if (index != input.length) {
            throw FormatException("Contenido inesperado en la posición ${index + 1}")
        }
        return output.toString()
    }

    private fun parseValue(indent: Int) {
        skipWhitespace()
        if (index >= input.length) throw FormatException("Valor incompleto")
        when (val character = input[index]) {
            '{' -> parseObject(indent)
            '[' -> parseArray(indent)
            '"' -> output.append(readString())
            't' -> appendLiteral("true")
            'f' -> appendLiteral("false")
            'n' -> appendLiteral("null")
            '-', in '0'..'9' -> output.append(readNumber())
            else -> throw FormatException(
                "Carácter inesperado '$character' en la posición ${index + 1}",
            )
        }
    }

    private fun parseObject(indent: Int) {
        output.append('{')
        index++
        skipWhitespace()
        if (peek('}')) {
            index++
            output.append('}')
            return
        }
        output.append('\n')
        while (true) {
            skipWhitespace()
            if (!peek('"')) throw FormatException("Se esperaba una clave entre comillas")
            val key = readString()
            skipWhitespace()
            if (!peek(':')) throw FormatException("Se esperaba ':' después de la clave")
            index++
            appendIndent(indent + INDENT_SIZE)
            output.append(key).append(": ")
            parseValue(indent + INDENT_SIZE)
            skipWhitespace()
            if (peek(',')) {
                index++
                output.append(",\n")
            } else if (peek('}')) {
                index++
                output.append('\n')
                appendIndent(indent)
                output.append('}')
                return
            } else {
                throw FormatException("Se esperaba ',' o '}' en la posición ${index + 1}")
            }
        }
    }

    private fun parseArray(indent: Int) {
        output.append('[')
        index++
        skipWhitespace()
        if (peek(']')) {
            index++
            output.append(']')
            return
        }
        output.append('\n')
        while (true) {
            appendIndent(indent + INDENT_SIZE)
            parseValue(indent + INDENT_SIZE)
            skipWhitespace()
            if (peek(',')) {
                index++
                output.append(",\n")
            } else if (peek(']')) {
                index++
                output.append('\n')
                appendIndent(indent)
                output.append(']')
                return
            } else {
                throw FormatException("Se esperaba ',' o ']' en la posición ${index + 1}")
            }
        }
    }

    private fun readString(): String {
        val start = index
        index++
        while (index < input.length) {
            when (val character = input[index]) {
                '"' -> {
                    index++
                    return input.substring(start, index)
                }
                '\\' -> {
                    index++
                    if (index >= input.length) throw FormatException("Secuencia de escape incompleta")
                    when (input[index]) {
                        '"', '\\', '/', 'b', 'f', 'n', 'r', 't' -> index++
                        'u' -> {
                            val end = index + 5
                            if (end > input.length) throw FormatException("Código unicode incompleto")
                            val hex = input.substring(index + 1, end)
                            if (hex.length != 4 || hex.any { it !in HEX_DIGITS }) {
                                throw FormatException("Código unicode inválido")
                            }
                            index = end
                        }
                        else -> throw FormatException("Secuencia de escape inválida")
                    }
                }
                in '\u0000'..'\u001F' -> throw FormatException("Salto o control sin escape en la cadena")
                else -> index++
            }
        }
        throw FormatException("Cadena sin cerrar")
    }

    private fun readNumber(): String {
        val match = NUMBER_PATTERN.find(input, index)
        if (match == null || match.range.first != index) {
            throw FormatException("Número inválido en la posición ${index + 1}")
        }
        val end = match.range.last + 1
        if (end < input.length) {
            val next = input[end]
            if (!next.isWhitespace() && next != ',' && next != ']' && next != '}') {
                throw FormatException("Número inválido en la posición ${index + 1}")
            }
        }
        index = end
        return match.value
    }

    private fun appendLiteral(literal: String) {
        if (!input.startsWith(literal, index)) {
            throw FormatException("Valor inválido en la posición ${index + 1}")
        }
        output.append(literal)
        index += literal.length
    }

    private fun appendIndent(level: Int) {
        repeat(level) { output.append(' ') }
    }

    private fun skipWhitespace() {
        while (index < input.length && input[index].isWhitespace()) index++
    }

    private fun peek(character: Char): Boolean = index < input.length && input[index] == character

    private companion object {
        const val INDENT_SIZE = 2
        val HEX_DIGITS = (('0'..'9') + ('a'..'f') + ('A'..'F')).toSet()
        val NUMBER_PATTERN = Regex("-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?")
    }
}

private class FormatException(
    override val message: String,
) : RuntimeException(message)
