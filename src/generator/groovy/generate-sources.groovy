import com.google.common.base.CaseFormat
import com.google.common.base.Converter
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements

import java.nio.file.Paths;

String basedir;
if (Paths.get("./src").toFile().exists()) {
    basedir = "."
} else {
    basedir = "../../.."
}

class LanguageFeature {

    String id, name, className;

    List<String> types, parents, children;

    boolean isOptional, isReservedWord, isFreeWord, isExternalWord;

    static List<LanguageFeature> parse(final Document doc) {
        Converter<String, String> converter = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.UPPER_CAMEL)
        return doc.select("section[id]").collect { e ->
            LanguageFeature ret = new LanguageFeature();

            ret.id = e.attr("id")
            ret.name = e.select("h3").first().childNode(0).toString()
            ret.className = converter.convert(ret.name.replaceAll(" ", ""))

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
        }
    }

}

generatedSourceBase = "${basedir}/target/generated-sources/jsonstat"

targetDir = new File("${generatedSourceBase}")

if (!targetDir.isDirectory()) {
    targetDir.mkdirs()
}


format = new File("${basedir}/src/generator/resources/format.html")
Document doc = Jsoup.parse(format, "UTF-8", "http://json-stat.org/format/");

List<LanguageFeature> features = LanguageFeature.parse(doc)

features.forEach {
    printf("id: <%s> <%s>%n", it.id, it.name);
    printf("class: <%s>%n", it.className);
    printf("types: %s%n", it.types);
    printf("optional: %s%n", it.isOptional);
    printf("reserved word: %s%n", it.isReservedWord);
    printf("external word: %s%n", it.isExternalWord);
    printf("free word: %s%n", it.isFreeWord);
    printf("parents: %s%n", it.parents.toString());
    printf("children: %s%n", it.children.toString());
    println()

    sourceFile = new File("${generatedSourceBase}/${it.className}.java")

    if (!sourceFile.isFile()) {
        sourceFile.createNewFile()
    }

    sourceFile.text = """
public class ${it.className} {

    // ${it.id} ${it.name}

    // types: ${it.types}

    // children: ${it.children}

    // parents: ${it.parents}

}
"""

}