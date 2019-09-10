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

import io.gravitee.common.util.LinkedMultiValueMap;
import io.gravitee.common.util.MultiValueMap;
import io.gravitee.el.TemplateEngine;
import io.gravitee.gateway.api.ExecutionContext;
import io.gravitee.gateway.api.Request;
import io.gravitee.gateway.api.Response;
import io.gravitee.policy.api.PolicyChain;
import io.gravitee.policy.transformqueryparams.configuration.HttpQueryParameter;
import io.gravitee.policy.transformqueryparams.configuration.TransformQueryParametersPolicyConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

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
        List<HttpQueryParameter> parameters = new ArrayList<>();
        HttpQueryParameter param = new HttpQueryParameter();
        param.setName("foo");
        param.setValue("bar");
        parameters.add(param);
        when(configuration.isClearAll()).thenReturn(false);
        when(configuration.getAddQueryParameters()).thenReturn(parameters);
        MultiValueMap requestParams = new LinkedMultiValueMap();
        when(request.parameters()).thenReturn(requestParams);

        policy.onRequest(request, response, executionContext, policyChain);

        assertNotNull(request.parameters().get("foo"));
        assertEquals(1, request.parameters().get("foo").size());
        assertEquals("bar", request.parameters().get("foo").get(0));
    }

    @Test
    public void shouldAddSimpleParamAndKeepExistingParams() {
        List<HttpQueryParameter> parameters = new ArrayList<>();
        HttpQueryParameter param = new HttpQueryParameter();
        param.setName("foo");
        param.setValue("bar");
        parameters.add(param);
        when(configuration.isClearAll()).thenReturn(false);
        when(configuration.getAddQueryParameters()).thenReturn(parameters);
        MultiValueMap requestParams = new LinkedMultiValueMap();
        requestParams.add("existing", "value");
        when(request.parameters()).thenReturn(requestParams);

        policy.onRequest(request, response, executionContext, policyChain);

        assertNotNull(request.parameters().get("foo"));
        assertEquals(1, request.parameters().get("foo").size());
        assertEquals("bar", request.parameters().get("foo").get(0));
        assertNotNull(request.parameters().get("existing"));
        assertEquals(1, request.parameters().get("existing").size());
        assertEquals("value", request.parameters().get("existing").get(0));
    }

    @Test
    public void shouldAddEncodedParam() {
        List<HttpQueryParameter> parameters = new ArrayList<>();
        HttpQueryParameter param = new HttpQueryParameter();
        param.setName("foo%20name");
        param.setValue("bar%20name");
        parameters.add(param);
        when(configuration.isClearAll()).thenReturn(false);
        when(configuration.getAddQueryParameters()).thenReturn(parameters);
        MultiValueMap requestParams = new LinkedMultiValueMap();
        when(request.parameters()).thenReturn(requestParams);

        policy.onRequest(request, response, executionContext, policyChain);

        assertNotNull(request.parameters().get("foo%20name"));
        assertEquals(1, request.parameters().get("foo%20name").size());
        assertEquals("bar%20name", request.parameters().get("foo%20name").get(0));
    }

    @Test
    public void shouldAddUnencodedParam() {
        List<HttpQueryParameter> parameters = new ArrayList<>();
        HttpQueryParameter param = new HttpQueryParameter();
        param.setName("foo&name");
        param.setValue("bar'name&=3");
        parameters.add(param);
        when(configuration.isClearAll()).thenReturn(false);
        when(configuration.getAddQueryParameters()).thenReturn(parameters);
        MultiValueMap requestParams = new LinkedMultiValueMap();
        when(request.parameters()).thenReturn(requestParams);

        policy.onRequest(request, response, executionContext, policyChain);

        assertNotNull(request.parameters().get("foo&name"));
        assertEquals(1, request.parameters().get("foo&name").size());
        assertEquals("bar'name&=3", request.parameters().get("foo&name").get(0));
    }

    @Test
    public void shouldEncodeWhitespaceParam() {
        List<HttpQueryParameter> parameters = new ArrayList<>();
        HttpQueryParameter param = new HttpQueryParameter();
        param.setName("foo name");
        param.setValue("bar name");
        parameters.add(param);
        when(configuration.isClearAll()).thenReturn(false);
        when(configuration.getAddQueryParameters()).thenReturn(parameters);
        MultiValueMap requestParams = new LinkedMultiValueMap();
        when(request.parameters()).thenReturn(requestParams);

        policy.onRequest(request, response, executionContext, policyChain);

        assertNotNull(request.parameters().get("foo%20name"));
        assertEquals(1, request.parameters().get("foo%20name").size());
        assertEquals("bar%20name", request.parameters().get("foo%20name").get(0));
    }

    @Test
    public void shouldClearAll() {
        when(configuration.isClearAll()).thenReturn(true);
        MultiValueMap requestParams = new LinkedMultiValueMap();
        requestParams.add("foo", "bar");
        when(request.parameters()).thenReturn(requestParams);

        assertFalse(request.parameters().isEmpty());

        policy.onRequest(request, response, executionContext, policyChain);

        assertTrue(request.parameters().isEmpty());
    }

    @Test
    public void shouldOverride() {
        when(configuration.isClearAll()).thenReturn(false);
        List<HttpQueryParameter> parameters = new ArrayList<>();
        HttpQueryParameter param = new HttpQueryParameter();
        param.setName("foo");
        param.setValue("newbar");
        parameters.add(param);
        when(configuration.getAddQueryParameters()).thenReturn(parameters);
        when(configuration.isClearAll()).thenReturn(false);
        MultiValueMap requestParams = new LinkedMultiValueMap();
        requestParams.add("foo", "bar");
        when(request.parameters()).thenReturn(requestParams);

        policy.onRequest(request, response, executionContext, policyChain);

        assertNotNull(request.parameters().get("foo"));
        assertEquals(1, request.parameters().get("foo").size());
        assertEquals("newbar", request.parameters().get("foo").get(0));
    }

    @Test
    public void shouldRemoveAllAndAdd() {
        when(configuration.isClearAll()).thenReturn(true);
        List<HttpQueryParameter> parameters = new ArrayList<>();
        HttpQueryParameter param = new HttpQueryParameter();
        param.setName("foo");
        param.setValue("bar");
        parameters.add(param);
        when(configuration.getAddQueryParameters()).thenReturn(parameters);
        MultiValueMap requestParams = new LinkedMultiValueMap();
        requestParams.add("old", "value");
        when(request.parameters()).thenReturn(requestParams);

        policy.onRequest(request, response, executionContext, policyChain);

        assertNotNull(request.parameters().get("foo"));
        assertEquals(1, request.parameters().get("foo").size());
        assertEquals("bar", request.parameters().get("foo").get(0));
        assertNull(request.parameters().get("old"));
    }

    @Test
    public void shouldRemoveParam() {
        when(configuration.isClearAll()).thenReturn(false);
        List<String> parameters = new ArrayList<>();
        String param = "foo";
        parameters.add(param);
        when(configuration.getRemoveQueryParameters()).thenReturn(parameters);
        MultiValueMap requestParams = new LinkedMultiValueMap();
        requestParams.add("existing", "value");
        when(request.parameters()).thenReturn(requestParams);

        policy.onRequest(request, response, executionContext, policyChain);

        assertNotNull(request.parameters().get("existing"));
        assertNull(request.parameters().get("foo"));
    }

    @Test
    public void shouldVerifyOrderAddAndRemoveParam() {
        when(configuration.isClearAll()).thenReturn(false);
        List<HttpQueryParameter> parameters = new ArrayList<>();
        HttpQueryParameter param = new HttpQueryParameter();
        param.setName("foo");
        param.setValue("bar");
        parameters.add(param);
        when(configuration.isClearAll()).thenReturn(false);
        when(configuration.getAddQueryParameters()).thenReturn(parameters);
        
        List<String> removeparameters = new ArrayList<>();
        String removeparam = "foo";
        removeparameters.add(removeparam);
        when(configuration.getRemoveQueryParameters()).thenReturn(removeparameters);
        MultiValueMap requestParams = new LinkedMultiValueMap();
        requestParams.add("existing", "value");
        when(request.parameters()).thenReturn(requestParams);

        policy.onRequest(request, response, executionContext, policyChain);

        assertNotNull(request.parameters().get("existing"));
        assertNull(request.parameters().get("foo"));
    }
}
