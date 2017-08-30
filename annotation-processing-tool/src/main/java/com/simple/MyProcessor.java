package com.simple;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Created by FRAMGIA\nguyen.thanh.tuan on 8/29/17.
 */
@AutoService(Processor.class)
public class MyProcessor extends AbstractProcessor {
    private static final String SUFFIX = "_Simple";
    private static final String CONST_PARAM_TARGET_NAME = "target";
    private static final String TARGET_STATEMENT_FORMAT =
            "target.%1s = (%2s) target.findViewById(%3s)";
    private static final String ONCLICK_STATEMENT_FORMAT =
            "target.findViewById(%1s).setOnClickListener(new android.view.View.OnClickListener() {\n"
                    + "            @Override\n"
                    + "            public void onClick(android.view.View view) {\n"
                    + "                target.%2s();\n"
                    + "            }\n"
                    + "        })";
    private Filer mFiler;
    private Types mTypes;
    private Elements mElements;
    private Map<String, List<Element>> mListFields = new LinkedHashMap<>();
    private Map<String, List<Element>> mListMethods = new LinkedHashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mTypes = processingEnvironment.getTypeUtils();
        mElements = processingEnvironment.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
            types.add(annotation.getCanonicalName());
        }
        return types;
    }

    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();
        annotations.add(BindView.class);
        annotations.add(OnClick.class);
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        // setOnClickListener
        for (Element element : roundEnvironment.getElementsAnnotatedWith(OnClick.class)) {
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();
            String key = typeElement.getQualifiedName().toString();
            if (mListMethods.get(key) == null) {
                mListMethods.put(key, new ArrayList<Element>());
            }
            mListMethods.get(key).add(element);
        }
        // findViewById
        for (Element element : roundEnvironment.getElementsAnnotatedWith(BindView.class)) {
            String ANDROID_VIEW_TYPE = "android.view.View";
            if (mTypes.isSubtype(element.asType(),
                    mElements.getTypeElement(ANDROID_VIEW_TYPE).asType())) {
                TypeElement typeElement = (TypeElement) element.getEnclosingElement();
                String key = typeElement.getQualifiedName().toString();
                if (mListFields.get(key) == null) {
                    mListFields.put(key, new ArrayList<Element>());
                }
                mListFields.get(key).add(element);
            }
        }
        if (mListFields.isEmpty()) {
            return true;
        }
        for (Map.Entry<String, List<Element>> entry : mListFields.entrySet()) {
            MethodSpec constructor =
                    createConstructor(mListMethods.get(entry.getKey()), entry.getValue());
            TypeSpec binder = createClass(getClassName(entry.getKey()), constructor);
            JavaFile javaFile =
                    JavaFile.builder(getPackage(entry.getValue().get(0)), binder).build();
            try {
                javaFile.writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private TypeSpec createClass(String className, MethodSpec constructor) {
        return TypeSpec.classBuilder(className + SUFFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(constructor)
                .build();
    }

    private String getClassName(String qualifier) {
        String DOT = ".";
        return qualifier.substring(qualifier.lastIndexOf(DOT) + 1);
    }

    private String getPackage(Element element) {
        return mElements.getPackageOf(element).toString();
    }

    private MethodSpec createConstructor(List<Element> methods, List<Element> elements) {
        Element firstElement = elements.get(0);
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(firstElement.getEnclosingElement().asType()),
                        CONST_PARAM_TARGET_NAME, Modifier.FINAL);
        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            builder.addStatement(
                    String.format(TARGET_STATEMENT_FORMAT, element.getSimpleName().toString(),
                            element.asType().toString(),
                            String.valueOf(element.getAnnotation(BindView.class).value())));
        }
        if (methods != null && methods.size() > 0) {
            for (int j = 0; j < methods.size(); j++) {
                builder.addStatement(String.format(ONCLICK_STATEMENT_FORMAT,
                        String.valueOf(methods.get(j).getAnnotation(OnClick.class).value()),
                        methods.get(j).getSimpleName().toString()));
            }
        }
        return builder.build();
    }
}
