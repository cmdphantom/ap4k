/**
 * Copyright 2018 The original authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
**/

package io.ap4k.component.processor;

import io.ap4k.Session;
import io.ap4k.component.adapt.CompositeConfigAdapter;
import io.ap4k.component.annotation.CompositeApplication;
import io.ap4k.component.config.CompositeConfig;
import io.ap4k.component.config.CompositeConfigBuilder;
import io.ap4k.component.handler.ComponentHandler;
import io.ap4k.component.handler.ComponentServiceCatalogHandler;
import io.ap4k.kubernetes.config.ConfigurationSupplier;
import io.ap4k.processor.AbstractAnnotationProcessor;
import io.ap4k.doc.Description;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@Description("Generate component custom resources.")
@SupportedAnnotationTypes("io.ap4k.component.annotation.CompositeApplication")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class CompositeAnnotationProcessor extends AbstractAnnotationProcessor<CompositeConfig> {

  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    Session session = Session.getSession();
    if (roundEnv.processingOver()) {
      session.onClose(this::write);
      return true;
    }
    for (TypeElement typeElement : annotations) {
      for (Element mainClass : roundEnv.getElementsAnnotatedWith(typeElement)) {
        session.configurators().add(config(mainClass));
        session.handlers().add(new ComponentHandler(session.resources()));
        session.handlers().add(new ComponentServiceCatalogHandler(session.resources()));
      }
    }
    return false;
  }

  public ConfigurationSupplier<CompositeConfig> config(Element mainClass) {
    CompositeApplication compositeApplication = mainClass.getAnnotation(CompositeApplication.class);
    return compositeApplication != null
      ? new ConfigurationSupplier<>(CompositeConfigAdapter.newBuilder(compositeApplication))
      : new ConfigurationSupplier<>(new CompositeConfigBuilder());
  }
}
