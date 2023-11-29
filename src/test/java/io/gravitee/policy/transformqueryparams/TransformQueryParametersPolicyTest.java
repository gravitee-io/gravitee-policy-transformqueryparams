/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.policy.transformqueryparams;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.MockitoAnnotations.initMocks;

import io.gravitee.common.util.LinkedMultiValueMap;
import io.gravitee.common.util.MultiValueMap;
import io.gravitee.el.TemplateEngine;
import io.gravitee.gateway.api.ExecutionContext;
import io.gravitee.gateway.api.Request;
import io.gravitee.gateway.api.Response;
import io.gravitee.policy.api.PolicyChain;
import io.gravitee.policy.transformqueryparams.configuration.HttpQueryParameter;
import io.gravitee.policy.transformqueryparams.configuration.TransformQueryParametersPolicyConfiguration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com)
 * @author GraviteeSource Team
 */
@ExtendWith(MockitoExtension.class)
public class TransformQueryParametersPolicyTest {

    private TransformQueryParametersPolicy policy;

    @Mock
    private TransformQueryParametersPolicyConfiguration configuration;

    @Mock
    private ExecutionContext executionContext;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private Request request;

    @Mock
    private Response response;

    @Mock
    protected PolicyChain policyChain;

    @BeforeEach
    public void init() {
        initMocks(this);

        policy = new TransformQueryParametersPolicy(configuration);
        lenient().when(executionContext.getTemplateEngine()).thenReturn(templateEngine);
        lenient().when(templateEngine.convert(any(String.class))).thenAnswer(returnsFirstArg());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("useCases")
    void shouldTest(
        String name,
        List<HttpQueryParameter> addQueryParameters,
        List<String> removeQueryParameters,
        MultiValueMap<String, String> requestQueryParams,
        MultiValueMap<String, String> expectedQueryParams,
        boolean clearAll
    ) {
        lenient().when(configuration.isClearAll()).thenReturn(clearAll);
        lenient().when(configuration.getAddQueryParameters()).thenReturn(addQueryParameters);
        lenient().when(configuration.getRemoveQueryParameters()).thenReturn(removeQueryParameters);
        lenient().when(request.parameters()).thenReturn(requestQueryParams);

        policy.onRequest(request, response, executionContext, policyChain);

        assertEquals(expectedQueryParams, request.parameters());
    }

    private static Stream<Arguments> useCases() throws IOException {
        return Stream.of(
            // name, addQueryParameters, removeQueryParameters, requestQueryParams, expectedQueryParams, clearAll
            Arguments.of(
                "add simple param",
                List.of(HttpQueryParameter.builder().name("foo").value("bar").build()),
                List.of(),
                new LinkedMultiValueMap<>(),
                new LinkedMultiValueMap<>(Map.of("foo", List.of("bar"))),
                false
            ),
            Arguments.of(
                "add simple param and keep existing params",
                List.of(HttpQueryParameter.builder().name("foo").value("bar").build()),
                List.of(),
                new LinkedMultiValueMap<>(Map.of("existing", List.of("value"))),
                new LinkedMultiValueMap<>(Map.of("existing", List.of("value"), "foo", List.of("bar"))),
                false
            ),
            Arguments.of(
                "add encoded param",
                List.of(HttpQueryParameter.builder().name("foo%20name").value("bar%20name").build()),
                List.of(),
                new LinkedMultiValueMap<>(),
                new LinkedMultiValueMap<>(Map.of("foo%20name", List.of("bar%20name"))),
                false
            ),
            Arguments.of(
                "add unencoded param",
                List.of(HttpQueryParameter.builder().name("foo&name").value("bar'name&=3").build()),
                List.of(),
                new LinkedMultiValueMap<>(),
                new LinkedMultiValueMap<>(Map.of("foo&name", List.of("bar'name&=3"))),
                false
            ),
            Arguments.of(
                "add whitespace param",
                List.of(HttpQueryParameter.builder().name("foo name").value("bar name").build()),
                List.of(),
                new LinkedMultiValueMap<>(),
                new LinkedMultiValueMap<>(Map.of("foo%20name", List.of("bar%20name"))),
                false
            ),
            Arguments.of(
                "clear all",
                List.of(),
                List.of(),
                new LinkedMultiValueMap<>(Map.of("foo", List.of("bar"))),
                new LinkedMultiValueMap<>(),
                true
            ),
            Arguments.of(
                "remove param",
                List.of(),
                List.of("foo"),
                new LinkedMultiValueMap<>(Map.of("foo", List.of("bar"), "old", List.of("value"))),
                new LinkedMultiValueMap<>(Map.of("old", List.of("value"))),
                false
            ),
            Arguments.of(
                "override param",
                List.of(HttpQueryParameter.builder().name("foo").value("newbar").build()),
                List.of(),
                new LinkedMultiValueMap<>(Map.of("foo", List.of("bar"))),
                new LinkedMultiValueMap<>(Map.of("foo", List.of("newbar"))),
                false
            ),
            Arguments.of(
                "remove all and add param",
                List.of(HttpQueryParameter.builder().name("foo").value("bar").build()),
                List.of(),
                new LinkedMultiValueMap<>(Map.of("old", List.of("value"))),
                new LinkedMultiValueMap<>(Map.of("foo", List.of("bar"))),
                true
            ),
            Arguments.of(
                "verify order add then remove param",
                List.of(HttpQueryParameter.builder().name("foo").value("bar").build()),
                List.of("foo"),
                new LinkedMultiValueMap<>(Map.of("existing", List.of("value"))),
                new LinkedMultiValueMap<>(Map.of("existing", List.of("value"))),
                false
            ),
            Arguments.of(
                "add param double time",
                List.of(
                    HttpQueryParameter.builder().name("foo").value("bar").build(),
                    HttpQueryParameter.builder().name("foo").value("bar2").build()
                ),
                List.of(),
                new LinkedMultiValueMap<>(),
                new LinkedMultiValueMap<>(Map.of("foo", List.of("bar2"))),
                false
            ),
            Arguments.of(
                "append param to an array",
                List.of(
                    HttpQueryParameter.builder().name("foo").value("bar").build(),
                    HttpQueryParameter.builder().name("foo").value("bar2").appendToExistingArray(true).build()
                ),
                List.of(),
                new LinkedMultiValueMap<>(),
                new LinkedMultiValueMap<>(Map.of("foo", List.of("bar", "bar2"))),
                false
            ),
            Arguments.of(
                "append param to an existing array",
                List.of(HttpQueryParameter.builder().name("foo").value("bar2").appendToExistingArray(true).build()),
                List.of(),
                new LinkedMultiValueMap<>(Map.of("foo", new ArrayList<>(List.of("bar")))),
                new LinkedMultiValueMap<>(Map.of("foo", List.of("bar", "bar2"))),
                false
            ),
            Arguments.of(
                "append multiple values into an array",
                List.of(
                    HttpQueryParameter.builder().name("foo").value("bar").appendToExistingArray(true).build(),
                    HttpQueryParameter.builder().name("foo").value("bar2").appendToExistingArray(true).build(),
                    HttpQueryParameter.builder().name("foo").value("bar3").appendToExistingArray(true).build()
                ),
                List.of(),
                new LinkedMultiValueMap<>(),
                new LinkedMultiValueMap<>(Map.of("foo", List.of("bar", "bar2", "bar3"))),
                false
            ),
            Arguments.of(
                "replace an existing param with an array",
                List.of(
                    HttpQueryParameter.builder().name("foo").value("bar").appendToExistingArray(false).build(),
                    HttpQueryParameter.builder().name("foo").value("bar2").appendToExistingArray(true).build(),
                    HttpQueryParameter.builder().name("foo").value("bar3").appendToExistingArray(true).build()
                ),
                List.of(),
                new LinkedMultiValueMap<>(Map.of("foo", List.of("oldvalue"))),
                new LinkedMultiValueMap<>(Map.of("foo", List.of("bar", "bar2", "bar3"))),
                false
            )
        );
    }
}
