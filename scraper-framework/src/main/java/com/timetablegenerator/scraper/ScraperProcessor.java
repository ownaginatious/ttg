package com.timetablegenerator.scraper;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import com.timetablegenerator.model.School;
import com.timetablegenerator.model.Term;
import com.timetablegenerator.model.TermClassifier;
import com.timetablegenerator.scraper.annotation.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

@SuppressWarnings("unused")
@AutoService(Processor.class)
public class ScraperProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Filer filer;
    private Messager messenger;

    private Map<String, SchoolConfig> nameToSchoolConfigMap = new HashMap<>();
    private Map<String, String> nameToScraperMap = new HashMap<>();

    private TypeMirror scraperTypeMirror;
    private boolean alreadyGenerated = false;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        filer = processingEnv.getFiler();
        messenger = processingEnv.getMessager();

        this.scraperTypeMirror = processingEnv.getElementUtils()
                .getTypeElement(Scraper.class.getCanonicalName()).asType();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedAnnotations = new HashSet<>();
        supportedAnnotations.add(SchoolConfig.class.getCanonicalName());
        return supportedAnnotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        // This annotation processor should only do one round of processing.
        if (alreadyGenerated)
            return true;

        alreadyGenerated = true;

        for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(SchoolConfig.class)) {

            // Check to ensure the annotated element is a class.
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                error(annotatedElement, "Only classes can be annotated with @%s. %s is not valid.",
                        SchoolConfig.class.getSimpleName(), annotatedElement.getSimpleName());
                return false;
            }

            SchoolConfig config = annotatedElement.getAnnotation(SchoolConfig.class);

            String id = config.id();

            if (id.isEmpty()) {
                error(annotatedElement, "An empty id in @%s on %s is not allowed.",
                        SchoolConfig.class.getSimpleName(),
                        annotatedElement.getSimpleName());
                return false;
            }

            if (nameToSchoolConfigMap.put(id, config) != null) {
                error(annotatedElement, "The id \"%s\" is used in more than one @%s annotation.",
                        id, SchoolConfig.class.getSimpleName());
                return false;
            }

            String qualifiedName = ((TypeElement) annotatedElement).getQualifiedName().toString();
            nameToScraperMap.put(id, qualifiedName);

            if (config.legacy().length > 1) {
                error(annotatedElement, "Only a single @%s sub-annotation is allowed in @%s.",
                        LegacyConfig.class.getSimpleName(), annotatedElement.getSimpleName());
                return false;
            }

            if (!viableScraper((TypeElement) annotatedElement)) {
                return false;
            }

            messenger.printMessage(Diagnostic.Kind.NOTE,
                     String.format("Discovered scraper for school \"%s\" [%s] @ (%s)",
                                   config.name(), id, qualifiedName));
        }

        try {
            generateFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    private boolean viableScraper(TypeElement classElement) {

        // Ensure the scraper is public.
        if (!classElement.getModifiers().contains(Modifier.PUBLIC)) {
            error(classElement, "@%s annotated class %s must be public.",
                    SchoolConfig.class.getSimpleName(),
                    classElement.getQualifiedName().toString());
            return false;
        }

        // Ensure it's not an abstract class
        if (classElement.getModifiers().contains(Modifier.ABSTRACT)) {
            error(classElement, "@%s annotated class %s must not be abstract.",
                    SchoolConfig.class.getSimpleName(),
                    classElement.getQualifiedName().toString());
            return false;
        }

        TypeMirror superClassMirror = classElement.asType();
        boolean inheritsFromScraper = false;

        // Ensure it extends from the Scraper abstract class somewhere in its hierarchy.
        while (!inheritsFromScraper && superClassMirror.getKind() != TypeKind.NONE) {
            inheritsFromScraper = superClassMirror.equals(this.scraperTypeMirror);
            superClassMirror = ((TypeElement) typeUtils.asElement(superClassMirror)).getSuperclass();
        }

        if (!inheritsFromScraper) {
            error(classElement, "@%s annotated class %s must extend the %s abstract class.",
                    SchoolConfig.class.getSimpleName(),
                    classElement.getSimpleName(),
                    Scraper.class.getSimpleName()
            );
        }

        // Check if the class exposes an public default constructor.
        for (Element enclosed : classElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructorElement = (ExecutableElement) enclosed;
                if (constructorElement.getParameters().size() == 0 && constructorElement.getModifiers()
                        .contains(Modifier.PUBLIC)) {
                    return true;
                }
            }
        }

        // No empty public constructor found.
        error(classElement, "@%s annotated class %s must have an empty public constructor.",
                SchoolConfig.class.getSimpleName(),
                classElement.getQualifiedName().toString());

        return false;
    }

    private void generateFactory() throws IOException {

        TypeSpec scraperFactory = TypeSpec.classBuilder("ScraperFactoryImpl")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(ScraperFactory.class)
                .addField(
                        FieldSpec.builder(
                                ParameterizedTypeName.get(Map.class, String.class, School.class),
                                "schoolMap", Modifier.PRIVATE, Modifier.FINAL)
                                .initializer("new $T<>()", HashMap.class).build()
                )
                .addField(
                        FieldSpec.builder(
                                ParameterizedTypeName.get(Map.class, String.class, LegacySchool.class),
                                "legacyMap", Modifier.PRIVATE, Modifier.FINAL)
                                .initializer("new $T<>()", HashMap.class).build()
                )
                .addMethod(
                        this.generateInitializedConfigs()
                )
                .addMethod(
                        MethodSpec.methodBuilder("getScraper")
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(String.class, "schoolId")
                                .addAnnotation(Override.class)
                                .returns(ParameterizedTypeName.get(Optional.class, Scraper.class))
                                .addCode(this.generateInstantiations())
                                .addStatement("return scraper == null ? $T.empty() :"
                                                + "$T.of(scraper)",
                                        Optional.class, Optional.class)
                                .build()
                )
                .addMethod(
                        MethodSpec.methodBuilder("getLegacyConfig")
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(String.class, "schoolId")
                                .addAnnotation(Override.class)
                                .returns(ParameterizedTypeName.get(Optional.class, LegacySchool.class))
                                .addStatement("return this.legacyMap.containsKey(schoolId) ? "
                                                + "$T.of(this.legacyMap.get(schoolId)) : $T.empty()",
                                        Optional.class, Optional.class)
                                .build()
                )
                .build();

        JavaFile javaFile = JavaFile.builder(
                ScraperFactory.class.getPackage().toString().split(" ")[1],
                scraperFactory).build();

        OutputStreamWriter osw = new OutputStreamWriter(
                filer.createSourceFile(ScraperFactory.class.getCanonicalName() + "Impl").openOutputStream());

        javaFile.writeTo(osw);
        osw.close();
    }

    private CodeBlock generateInstantiations() {

        CodeBlock.Builder cbb = CodeBlock.builder()
                .addStatement("$T scraper = $L", Scraper.class, null);

        boolean first = true;

        for (Map.Entry<String, SchoolConfig> c : this.nameToSchoolConfigMap.entrySet()) {
            cbb.beginControlFlow("$Lif ($S.equals(schoolId))", first ? "" : "else ", c.getValue().id())
                    .addStatement("scraper = new $L()",
                            this.nameToScraperMap.get(c.getKey()))
                    .endControlFlow();
            first = false;
        }

        return cbb.beginControlFlow("if (scraper != null)")
                .addStatement("scraper.init(this.schoolMap.get(schoolId))")
                .endControlFlow().build();
    }

    private MethodSpec generateInitializedConfigs() {

        List<CodeBlock> schoolConfigs = new ArrayList<>();

        for (Map.Entry<String, SchoolConfig> c : this.nameToSchoolConfigMap.entrySet()) {

            SchoolConfig v = c.getValue();

            CodeBlock.Builder cbb = CodeBlock.builder()
                    .addStatement("sb = $T.builder($S, $S)",
                            School.class,
                            v.name(), v.id()
                    )
                    .addStatement("sb.usesDepartmentPrefixes($L)", v.useDepartmentPrefixes());

            for (SectionMapping sm : v.sections()) {
                cbb.addStatement("sb.withSection($S, $S)", sm.name(), sm.code());
            }

            cbb.addStatement("this.schoolMap.put($S, sb.build())", v.id());

            for (LegacyConfig lc : v.legacy()) {
                cbb.addStatement("ls = $T.builder(new $T($T.$L, $L))",
                        LegacySchool.class, Term.class, TermClassifier.class,
                        lc.term(), lc.year());
                for (LegacyMapping lm : lc.mapping()) {
                    cbb.addStatement("ls.withMapping($S, $T.$L)",
                            lm.from(), LegacyMapping.LegacyType.class, lm.to());
                }
                cbb.addStatement("this.legacyMap.put($S, ls.build())", v.id());
            }

            schoolConfigs.add(cbb.build());
        }

        MethodSpec.Builder ms = MethodSpec.constructorBuilder();
        //FIXME: Remove the following line after McMaster summer legacy support is dropped.
        ms.addModifiers(Modifier.PUBLIC);
        ms.addStatement("$T sb = $L", School.Builder.class, null);
        ms.addStatement("$T ls = $L", LegacySchool.Builder.class, null);
        schoolConfigs.forEach(ms::addCode);

        return ms.build();
    }

    private void error(Element e, String msg, Object... args) {
        messenger.printMessage(Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }
}
