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

import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
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

    @Before
    public void init() {
        initMocks(this);

        policy = new TransformQueryParametersPolicy(configuration);
        when(executionContext.getTemplateEngine()).thenReturn(templateEngine);
        when(templateEngine.convert(any(String.class))).thenAnswer(returnsFirstArg());
    }

    @Test
    public void shouldAddSimpleParam() {
        List<HttpQueryParameter> addQueryParameters = new ArrayList<>() {
            {
                add(HttpQueryParameter.builder().name("foo").value("bar").build());
            }
        };
        List<String> removeQueryParameters = new ArrayList<>();
        MultiValueMap<String, String> requestQueryParams = new LinkedMultiValueMap<>();
        MultiValueMap<String, String> expectedQueryParams = new LinkedMultiValueMap<>() {
            {
                add("foo", "bar");
            }
        };

        shouldTest(requestQueryParams, addQueryParameters, removeQueryParameters, expectedQueryParams, false);
    }

    @Test
    public void shouldAddSimpleParamAndKeepExistingParams() {
        List<HttpQueryParameter> addQueryParameters = new ArrayList<>() {
            {
                add(HttpQueryParameter.builder().name("foo").value("bar").build());
            }
        };
        List<String> removeQueryParameters = new ArrayList<>();
        MultiValueMap<String, String> requestQueryParams = new LinkedMultiValueMap<>() {
            {
                add("existing", "value");
            }
        };
        MultiValueMap<String, String> expectedQueryParams = new LinkedMultiValueMap<>() {
            {
                add("existing", "value");
                add("foo", "bar");
            }
        };

        shouldTest(requestQueryParams, addQueryParameters, removeQueryParameters, expectedQueryParams, false);
    }

    @Test
    public void shouldAddEncodedParam() {
        List<HttpQueryParameter> addQueryParameters = new ArrayList<>() {
            {
                add(HttpQueryParameter.builder().name("foo%20name").value("bar%20name").build());
            }
        };
        List<String> removeQueryParameters = new ArrayList<>();
        MultiValueMap<String, String> requestQueryParams = new LinkedMultiValueMap<>();
        MultiValueMap<String, String> expectedQueryParams = new LinkedMultiValueMap<>() {
            {
                add("foo%20name", "bar%20name");
            }
        };

        shouldTest(requestQueryParams, addQueryParameters, removeQueryParameters, expectedQueryParams, false);
    }

    @Test
    public void shouldAddUnencodedParam() {
        List<HttpQueryParameter> addQueryParameters = new ArrayList<>() {
            {
                add(HttpQueryParameter.builder().name("foo&name").value("bar'name&=3").build());
            }
        };
        List<String> removeQueryParameters = new ArrayList<>();
        MultiValueMap<String, String> requestQueryParams = new LinkedMultiValueMap<>();
        MultiValueMap<String, String> expectedQueryParams = new LinkedMultiValueMap<>() {
            {
                add("foo&name", "bar'name&=3");
            }
        };

        shouldTest(requestQueryParams, addQueryParameters, removeQueryParameters, expectedQueryParams, false);
    }

    @Test
    public void shouldEncodeWhitespaceParam() {
        List<HttpQueryParameter> addQueryParameters = new ArrayList<>() {
            {
                add(HttpQueryParameter.builder().name("foo name").value("bar name").build());
            }
        };
        List<String> removeQueryParameters = new ArrayList<>();
        MultiValueMap<String, String> requestQueryParams = new LinkedMultiValueMap<>();
        MultiValueMap<String, String> expectedQueryParams = new LinkedMultiValueMap<>() {
            {
                add("foo%20name", "bar%20name");
            }
        };

        shouldTest(requestQueryParams, addQueryParameters, removeQueryParameters, expectedQueryParams, false);
    }

    @Test
    public void shouldClearAll() {
        List<HttpQueryParameter> addQueryParameters = new ArrayList<>();
        List<String> removeQueryParameters = new ArrayList<>();
        MultiValueMap<String, String> requestQueryParams = new LinkedMultiValueMap<>() {
            {
                add("foo% 20name", "bar%20name");
            }
        };
        MultiValueMap<String, String> expectedQueryParams = new LinkedMultiValueMap<>();

        shouldTest(requestQueryParams, addQueryParameters, removeQueryParameters, expectedQueryParams, true);
    }

    @Test
    public void shouldOverride() {
        List<HttpQueryParameter> addQueryParameters = new ArrayList<>() {
            {
                add(HttpQueryParameter.builder().name("foo").value("newbar").build());
            }
        };
        List<String> removeQueryParameters = new ArrayList<>();
        MultiValueMap<String, String> requestQueryParams = new LinkedMultiValueMap<>() {
            {
                add("foo", "bar");
            }
        };
        MultiValueMap<String, String> expectedQueryParams = new LinkedMultiValueMap<>() {
            {
                add("foo", "newbar");
            }
        };

        shouldTest(requestQueryParams, addQueryParameters, removeQueryParameters, expectedQueryParams, false);
    }

    @Test
    public void shouldRemoveAllAndAdd() {
        List<HttpQueryParameter> addQueryParameters = new ArrayList<>() {
            {
                add(HttpQueryParameter.builder().name("foo").value("bar").build());
            }
        };
        List<String> removeQueryParameters = new ArrayList<>();
        MultiValueMap<String, String> requestQueryParams = new LinkedMultiValueMap<>() {
            {
                add("old", "value");
            }
        };
        MultiValueMap<String, String> expectedQueryParams = new LinkedMultiValueMap<>() {
            {
                add("foo", "bar");
            }
        };

        shouldTest(requestQueryParams, addQueryParameters, removeQueryParameters, expectedQueryParams, true);
    }

    @Test
    public void shouldRemoveParam() {
        List<HttpQueryParameter> addQueryParameters = new ArrayList<>();
        List<String> removeQueryParameters = new ArrayList<>() {
            {
                add("foo");
            }
        };
        MultiValueMap<String, String> requestQueryParams = new LinkedMultiValueMap<>() {
            {
                add("foo", "bar");
                add("old", "value");
            }
        };
        MultiValueMap<String, String> expectedQueryParams = new LinkedMultiValueMap<>() {
            {
                add("old", "value");
            }
        };

        shouldTest(requestQueryParams, addQueryParameters, removeQueryParameters, expectedQueryParams, false);
    }

    @Test
    public void shouldVerifyOrderAddThenRemoveParam() {
        List<HttpQueryParameter> addQueryParameters = new ArrayList<>() {
            {
                add(HttpQueryParameter.builder().name("foo").value("bar").build());
            }
        };
        List<String> removeQueryParameters = new ArrayList<>() {
            {
                add("foo");
            }
        };
        MultiValueMap<String, String> requestQueryParams = new LinkedMultiValueMap<>() {
            {
                add("existing", "value");
            }
        };
        MultiValueMap<String, String> expectedQueryParams = new LinkedMultiValueMap<>() {
            {
                add("existing", "value");
            }
        };

        shouldTest(requestQueryParams, addQueryParameters, removeQueryParameters, expectedQueryParams, false);
    }

    @Test
    public void shouldAddParamDoubleTime() {
        List<HttpQueryParameter> addQueryParameters = new ArrayList<>() {
            {
                add(HttpQueryParameter.builder().name("foo").value("bar").build());
                add(HttpQueryParameter.builder().name("foo").value("bar2").build());
            }
        };
        List<String> removeQueryParameters = new ArrayList<>();
        MultiValueMap<String, String> requestQueryParams = new LinkedMultiValueMap<>();
        MultiValueMap<String, String> expectedQueryParams = new LinkedMultiValueMap<>() {
            {
                add("foo", "bar2");
            }
        };

        shouldTest(requestQueryParams, addQueryParameters, removeQueryParameters, expectedQueryParams, false);
    }

    private void shouldTest(
        MultiValueMap<String, String> requestQueryParams,
        List<HttpQueryParameter> addQueryParameters,
        List<String> removeQueryParameters,
        MultiValueMap<String, String> expectedQueryParams,
        boolean clearAll
    ) {
        when(configuration.isClearAll()).thenReturn(clearAll);
        when(configuration.getAddQueryParameters()).thenReturn(addQueryParameters);
        when(configuration.getRemoveQueryParameters()).thenReturn(removeQueryParameters);
        when(request.parameters()).thenReturn(requestQueryParams);

        policy.onRequest(request, response, executionContext, policyChain);

        assertEquals(expectedQueryParams, request.parameters());
    }
}
