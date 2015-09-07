/*
 * Copyright 2014-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.restdocs.restassured;

import java.util.Arrays;
import java.util.List;

import org.springframework.restdocs.RestDocumentation;
import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.snippet.RestDocumentationContextPlaceholderResolver;
import org.springframework.restdocs.snippet.StandardWriterResolver;
import org.springframework.restdocs.snippet.WriterResolver;
import org.springframework.restdocs.templates.StandardTemplateResourceResolver;
import org.springframework.restdocs.templates.TemplateEngine;
import org.springframework.restdocs.templates.mustache.MustacheTemplateEngine;

import com.jayway.restassured.filter.Filter;
import com.jayway.restassured.filter.FilterContext;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.FilterableRequestSpecification;
import com.jayway.restassured.specification.FilterableResponseSpecification;

/**
 * A {@link Filter} that can be used to configure the documentation.
 *
 * @author Andy Wilkinson
 */
public class DocumentationConfigurationFilter implements Filter {

	private final TemplateEngineConfigurer templateEngineConfigurer = new TemplateEngineConfigurer();

	private final WriterResolverConfigurer writerResolverConfigurer = new WriterResolverConfigurer();

	private final SnippetConfigurer snippetConfigurer = new SnippetConfigurer(this);

	private final List<AbstractConfigurer> configurers = Arrays.asList(
			this.templateEngineConfigurer, this.writerResolverConfigurer,
			this.snippetConfigurer);

	private final RestDocumentation restDocumentation;

	DocumentationConfigurationFilter(RestDocumentation restDocumentation) {
		this.restDocumentation = restDocumentation;
	}

	/**
	 * Returns a {@link SnippetConfigurer} that can be used to configure the snippets that
	 * will be generated.
	 *
	 * @return the snippet configurer
	 */
	public SnippetConfigurer snippets() {
		return this.snippetConfigurer;
	}

	/**
	 * Configures the {@link TemplateEngine} that will be used for snippet rendering.
	 *
	 * @param templateEngine the template engine to use
	 * @return {@code this}
	 */
	public DocumentationConfigurationFilter templateEngine(TemplateEngine templateEngine) {
		this.templateEngineConfigurer.setTemplateEngine(templateEngine);
		return this;
	}

	/**
	 * Configures the {@link WriterResolver} that will be used to resolve a writer for a
	 * snippet.
	 *
	 * @param writerResolver The writer resolver to use
	 * @return {@code this}
	 */
	public DocumentationConfigurationFilter writerResolver(WriterResolver writerResolver) {
		this.writerResolverConfigurer.setWriterResolver(writerResolver);
		return this;
	}

	@Override
	public Response filter(FilterableRequestSpecification requestSpec,
			FilterableResponseSpecification responseSpec, FilterContext context) {
		context.setValue(RestDocumentationContext.class.getName(),
				this.restDocumentation.beforeOperation());
		for (AbstractConfigurer configurer : this.configurers) {
			configurer.apply(context);
		}
		return context.next(requestSpec, responseSpec);
	}

	private static class TemplateEngineConfigurer extends AbstractConfigurer {

		private TemplateEngine templateEngine = new MustacheTemplateEngine(
				new StandardTemplateResourceResolver());

		@Override
		void apply(FilterContext context) {
			context.setValue(TemplateEngine.class.getName(), this.templateEngine);
		}

		void setTemplateEngine(TemplateEngine templateEngine) {
			this.templateEngine = templateEngine;
		}

	}

	private static class WriterResolverConfigurer extends AbstractConfigurer {

		private WriterResolver writerResolver;

		@Override
		void apply(FilterContext context) {
			WriterResolver resolverToUse = this.writerResolver;
			if (resolverToUse == null) {
				resolverToUse = new StandardWriterResolver(
						new RestDocumentationContextPlaceholderResolver(
								(RestDocumentationContext) context
										.getValue(RestDocumentationContext.class
												.getName())));
			}
			context.setValue(WriterResolver.class.getName(), resolverToUse);
		}

		void setWriterResolver(WriterResolver writerResolver) {
			this.writerResolver = writerResolver;
		}

	}

}
