package com.simple;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Created by FRAMGIA\nguyen.thanh.tuan on 8/29/17.
 */
@SupportedAnnotationTypes("com.simple.BindView")
@AutoService(Processor.class)
public class MyProcessor extends AbstractProcessor {
    private static final String SUFFIX = "_Simple";
    private static final String CONST_PARAM_TARGET_NAME = "target";
    private static final String TARGET_STATEMENT_FORMAT =
            "target.%1s = (%2s) target.findViewById(%3s)";
    private Filer mFiler;
    private Types mTypes;
    private Elements mElements;
    private Map<String, List<Element>> mListMap = new LinkedHashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mTypes = processingEnvironment.getTypeUtils();
        mElements = processingEnvironment.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(BindView.class)) {
            String ANDROID_VIEW_TYPE = "android.view.View";
            if (mTypes.isSubtype(element.asType(),
                    mElements.getTypeElement(ANDROID_VIEW_TYPE).asType())) {
                TypeElement typeElement = (TypeElement) element.getEnclosingElement();
                String key = typeElement.getQualifiedName().toString();
                if (mListMap.get(key) == null) {
                    mListMap.put(key, new ArrayList<Element>());
                }
                mListMap.get(key).add(element);
            }
        }
        if (mListMap.isEmpty()) {
            return true;
        }
        for (Map.Entry<String, List<Element>> entry : mListMap.entrySet()) {
            MethodSpec constructor = createConstructor(entry.getValue());
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

    private MethodSpec createConstructor(List<Element> elements) {
        Element firstElement = elements.get(0);
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(firstElement.getEnclosingElement().asType()),
                        CONST_PARAM_TARGET_NAME);
        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            builder.addStatement(
                    String.format(TARGET_STATEMENT_FORMAT, element.getSimpleName().toString(),
                            element.asType().toString(),
                            String.valueOf(element.getAnnotation(BindView.class).value())));
        }
        return builder.build();
    }
}
