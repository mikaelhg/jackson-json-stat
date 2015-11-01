import com.google.common.base.CaseFormat
import com.google.common.base.Converter
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

import java.nio.file.Paths;

public class LanguageFeature {

    public String id, name, className;

    public List<String> types, parents, children;

    public boolean isOptional, isReservedWord, isFreeWord, isExternalWord;

    public static Map<String, LanguageFeature> parse(final Document doc) {
        Converter<String, String> converter = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.UPPER_CAMEL)
        return doc.select("section[id]").collect { e ->
            LanguageFeature ret = new LanguageFeature();

            ret.id = e.attr("id")
            ret.name = e.select("h3").first().childNode(0).toString()
            ret.className = "JsonStat" + converter.convert(ret.name.replaceAll(" ", ""))

            Elements badges = e.select("span.badge")

            ret.types = badges
                    .findAll { it.classNames().size() == 1 }
                    .collect { it.childNode(0).toString() }
                    .toList()

            ret.isOptional = badges.any {
                it.classNames().contains("label-info") &&
                        !it.getElementsContainingText("optional").isEmpty()
            }

            ret.isReservedWord = badges.any {
                it.classNames().contains("label-warning") &&
                        !it.getElementsContainingText("reserved word").isEmpty()
            }

            ret.isFreeWord = badges.any {
                it.classNames().contains("label-success") &&
                        !it.getElementsContainingText("free word").isEmpty()
            }

            ret.isExternalWord = badges.any {
                it.classNames().contains("label-success") &&
                        !it.getElementsContainingText("external word").isEmpty()
            }

            ret.parents = e.select("table.table-doc th:contains(Parents) ~ td > a")
                    .collect { it.attr("href").substring(1) }
                    .toList()

            ret.children = e.select("table.table-doc th:contains(Children) ~ td > a")
                    .collect { it.attr("href").substring(1) }
                    .toList()

            return ret
        }.collectEntries { [(it.id): it] }
    }

}

String basedir;
if (Paths.get("./src").toFile().exists()) {
    basedir = "."
} else {
    basedir = "../../.."
}

generatedSourcePackage = "io.mikael.jsonstat"

generatedSourceBase = "${basedir}/target/generated-sources/jsonstat/${generatedSourcePackage.replaceAll("\\.", "/")}"

targetDir = new File("${generatedSourceBase}")

if (!targetDir.isDirectory()) {
    targetDir.mkdirs()
}

format = new File("${basedir}/src/generator/resources/format.html")
Document doc = Jsoup.parse(format, "UTF-8", "http://json-stat.org/format/");

Map<String, LanguageFeature> features = LanguageFeature.parse(doc)

features.values().forEach {
    it.parents.forEach({ p ->
        if (!features[p].children.contains(it.id)) {
            println("parent ${features[p].id}.children doesn't know about ${it.id}")
        }
    })
    it.children.forEach({ c ->
        if (!features[c].parents.contains(it.id)) {
            println("child ${features[c].id}.parents doesn't know about ${it.id}")
        }
    })
}

features.values().forEach {

    sourceFile = new File("${generatedSourceBase}/${it.className}.java")

    if (!sourceFile.isFile()) {
        sourceFile.createNewFile()
    }

    sourceFile.text = """
package ${generatedSourcePackage};

public class ${it.className} {

    // <${it.id}> <${it.name}>

    // optional: ${it.isOptional}

    // types: ${it.types}

    // children: ${it.children}

    // parents: ${it.parents}

}
"""[1 .. -1]

}
