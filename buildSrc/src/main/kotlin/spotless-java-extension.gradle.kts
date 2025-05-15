/*
 * Copyright 2025-present MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.diffplug.spotless.FormatterFunc
import com.diffplug.spotless.FormatterStep
import java.io.Serializable

plugins {
    id("com.diffplug.spotless")
}

spotless {
    java {
        importOrder()
        removeUnusedImports()
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()

        palantirJavaFormat("2.58.0").formatJavadoc(true)

        formatAnnotations()

        // need to add license header manually to package-info.java and module-info.java
        // due to the bug: https://github.com/diffplug/spotless/issues/532
        licenseHeaderFile(rootProject.file("spotless.license.java")) // contains '$YEAR' placeholder

        targetExclude(rootProject.file("build/generated/sources/buildConfig/main/com/mongodb/hibernate/internal/BuildConfig.java"))

        addStep(
            FormatterStep.create(
                "multilineWithParameterFormatter",
                MultilineWithParameterFormatter(),
                { formatter -> FormatterFunc { formatter.format(it) } }))
        addStep(
            FormatterStep.create(
                "multilineIndentFormatter",
                MultilineIndentFormatter(),
                { formatter -> FormatterFunc { formatter.format(it) } }))
    }
}

class MultilineIndentFormatter : Serializable {
    fun format(content: String): String {
        val tripleQuote = "\"\"\""
        val lines = content.lines()
        val result = StringBuilder()
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            if (!line.trimEnd().endsWith(tripleQuote)) {
                result.append(line)
                if (i + 1 < lines.size) result.append("\n")
                i++
                continue
            }
            val baseIndent = line.indexOf(tripleQuote)
            result.append(line).append("\n")
            i++
            val multilineStringLines = mutableListOf<String>()
            while (i < lines.size) {
                val multilineStringLine = lines[i++]
                multilineStringLines.add(multilineStringLine)
                if (multilineStringLine.contains(tripleQuote)) break
            }
            val minIndent =
                multilineStringLines
                    .filter { it.isNotBlank() }
                    .minOfOrNull { l -> l.indexOfFirst { ch -> !ch.isWhitespace() }.takeIf { it >= 0 } ?: line.length }
                    ?: 0
            multilineStringLines.forEach { blockLine ->
                result.append(" ".repeat(baseIndent)).append(blockLine.drop(minIndent)).append("\n")
            }
        }
        return result.toString()
    }
}

class MultilineWithParameterFormatter : Serializable {
    fun format(content: String): String {
        val lines = content.lines()
        val result = StringBuilder()
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            if (line.trimEnd().endsWith("\"\"\"") && i + 1 < lines.size && lines[i + 1].trimStart().startsWith(".formatted(")) {
                result.append(line.trimEnd() + lines[i + 1].trimStart())
                i++
            } else {
                result.append(line)
            }
            i++
            if (i < lines.size) {
                result.append("\n")
            }
        }
        return result.toString()
    }
}