package org.example.utils;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PomUtil {

    public static Document mergePoms(String targetPomFile, String sourcePomFile) throws IOException, JDOMException {
        SAXBuilder saxBuilder = new SAXBuilder();
        Document finalPom = saxBuilder.build(new File(targetPomFile));

        Element targetRoot = finalPom.getRootElement();
        Element sourceRoot = saxBuilder.build(new File(sourcePomFile)).getRootElement();

        // Merge dependencies
        mergeDependencies(targetRoot, sourceRoot);

        // Merge plugins
        mergePlugins(targetRoot, sourceRoot);

        // Merge properties
        mergeProperties(targetRoot, sourceRoot);

        return finalPom;
    }

    public static void mergeDependencies(Element targetRoot, Element sourceRoot) {
        Element targetDependencies = targetRoot.getChild("dependencies", targetRoot.getNamespace());
        Element sourceDependencies = sourceRoot.getChild("dependencies", sourceRoot.getNamespace());

        if (sourceDependencies != null) {
            if (targetDependencies == null) {
                targetDependencies = new Element("dependencies");
                targetRoot.addContent(targetDependencies);
            }

            List<Element> sourceDependencyList = sourceDependencies.getChildren("dependency", sourceRoot.getNamespace());
            for (Element sourceDependency : sourceDependencyList) {
                targetDependencies.addContent(sourceDependency.clone());
            }
        }
    }

    public static void mergePlugins(Element targetRoot, Element sourceRoot) {
        Element targetBuild = targetRoot.getChild("build", targetRoot.getNamespace());
        Element sourceBuild = sourceRoot.getChild("build", sourceRoot.getNamespace());

        if (sourceBuild != null) {
            if (targetBuild == null) {
                targetBuild = new Element("build");
                targetRoot.addContent(targetBuild);
            }

            Element targetPlugins = targetBuild.getChild("plugins", targetRoot.getNamespace());
            Element sourcePlugins = sourceBuild.getChild("plugins", sourceRoot.getNamespace());

            if (sourcePlugins != null) {
                if (targetPlugins == null) {
                    targetPlugins = new Element("plugins");
                    targetBuild.addContent(targetPlugins);
                }

                List<Element> sourcePluginList = sourcePlugins.getChildren("plugin", sourceRoot.getNamespace());
                for (Element sourcePlugin : sourcePluginList) {
                    targetPlugins.addContent(sourcePlugin.clone());
                }
            }
        }
    }

    public static void mergeProperties(Element targetRoot, Element sourceRoot) {
        Element targetProperties = targetRoot.getChild("properties", targetRoot.getNamespace());
        Element sourceProperties = sourceRoot.getChild("properties", sourceRoot.getNamespace());

        if (sourceProperties != null) {
            if (targetProperties == null) {
                targetProperties = new Element("properties");
                targetRoot.addContent(targetProperties);
            }

            List<Element> sourcePropertyList = sourceProperties.getChildren();
            for (Element sourceProperty : sourcePropertyList) {
                targetProperties.addContent(sourceProperty.clone());
            }
        }
    }

    private static void mergeElement(Map<String, Element> mergedSections, Element projectElement, String sectionName) {
        Element section = projectElement.getChild(sectionName);
        if (section != null) {
            if (!mergedSections.containsKey(sectionName)) {
                mergedSections.put(sectionName, new Element(sectionName));
            }
            Element mergedSection = mergedSections.get(sectionName);

            for (Element child : section.getChildren()) {
                mergedSection.addContent(child.clone());
            }
        }

    }

    public static void saveDocument(Document document, Path filePath) throws IOException {
        XMLOutputter xmlOutputter = new XMLOutputter();
        xmlOutputter.setFormat(Format.getPrettyFormat());
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            xmlOutputter.output(document, writer);
        }
    }

}
