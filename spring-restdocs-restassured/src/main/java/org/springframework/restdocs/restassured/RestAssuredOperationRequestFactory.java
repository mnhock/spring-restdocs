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

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.operation.OperationRequestFactory;
import org.springframework.restdocs.operation.OperationRequestPart;
import org.springframework.restdocs.operation.Parameters;

import com.jayway.restassured.filter.FilterContext;
import com.jayway.restassured.response.Header;
import com.jayway.restassured.specification.FilterableRequestSpecification;

/**
 * A factory for creating an {@link OperationRequest} derived from a REST Assured
 * {@link FilterableRequestSpecification}.
 *
 * @author Andy Wilkinson
 */
class RestAssuredOperationRequestFactory {

	OperationRequest createOperationRequest(FilterableRequestSpecification requestSpec,
			FilterContext context) {
		return new OperationRequestFactory().create(URI.create(requestSpec.getURI()),
				HttpMethod.valueOf(requestSpec.getMethod().name()),
				extractContent(requestSpec), extractHeaders(requestSpec),
				extractParameters(requestSpec), extractParts(requestSpec));
	}

	private byte[] extractContent(FilterableRequestSpecification requestSpec) {
		return requestSpec.getBody() == null ? new byte[0] : ((String) requestSpec
				.getBody()).getBytes();
	}

	private HttpHeaders extractHeaders(FilterableRequestSpecification requestSpec) {
		HttpHeaders httpHeaders = new HttpHeaders();
		for (Header header : requestSpec.getHeaders()) {
			httpHeaders.add(header.getName(), header.getValue());
		}
		return httpHeaders;
	}

	private Parameters extractParameters(FilterableRequestSpecification requestSpec) {
		Parameters parameters = new Parameters();
		Map<String, ?> requestParams = requestSpec.getRequestParams();
		for (Entry<String, ?> entry : requestParams.entrySet()) {
			parameters.add(entry.getKey(), entry.getValue().toString());
		}
		return parameters;
	}

	private Collection<OperationRequestPart> extractParts(
			FilterableRequestSpecification requestSpec) {
		return Collections.emptyList();
	}
}
