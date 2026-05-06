/*
 * Copyright © 2015 The Gravitee team (http://gravitee.io)
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
 */
package io.gravitee.policy.transformqueryparams.v3;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.havingExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import io.gravitee.apim.gateway.tests.sdk.AbstractPolicyTest;
import io.gravitee.apim.gateway.tests.sdk.annotations.DeployApi;
import io.gravitee.apim.gateway.tests.sdk.annotations.GatewayTest;
import io.gravitee.definition.model.ExecutionMode;
import io.gravitee.policy.transformqueryparams.TransformQueryParametersPolicy;
import io.gravitee.policy.transformqueryparams.configuration.TransformQueryParametersPolicyConfiguration;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.core.http.HttpMethod;
import io.vertx.rxjava3.core.http.HttpClient;
import io.vertx.rxjava3.core.http.HttpClientRequest;
import io.vertx.rxjava3.core.http.HttpClientResponse;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@GatewayTest(v2ExecutionMode = ExecutionMode.V3)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class TransformQueryParametersPolicyV3IntegrationTest
    extends AbstractPolicyTest<TransformQueryParametersPolicy, TransformQueryParametersPolicyConfiguration> {

    @Test
    @DeployApi("/apis/v2/clear-queryparams.json")
    void should_clear_all_query_parameters(HttpClient client) throws InterruptedException {
        wiremock.stubFor(get(urlPathEqualTo("/endpoint")).willReturn(ok()));

        final TestObserver<HttpClientResponse> obs = client
            .rxRequest(HttpMethod.GET, "/test?keep=value&drop=value")
            .flatMap(HttpClientRequest::rxSend)
            .test();

        awaitTerminalEvent(obs);
        obs
            .assertComplete()
            .assertValue(response -> {
                assertThat(response.statusCode()).isEqualTo(200);
                return true;
            })
            .assertNoErrors();

        wiremock.verify(
            getRequestedFor(urlPathEqualTo("/endpoint"))
                .withQueryParam("added", equalTo("addedValue"))
                .withoutQueryParam("keep")
                .withoutQueryParam("drop")
        );
    }

    @Test
    @DeployApi("/apis/v2/add-update-remove-queryparams.json")
    void should_add_update_and_remove_query_parameters(HttpClient client) throws InterruptedException {
        wiremock.stubFor(get(urlPathEqualTo("/endpoint")).willReturn(ok()));

        final TestObserver<HttpClientResponse> obs = client
            .rxRequest(HttpMethod.GET, "/test?toUpdate=originalValue&toRemove=byebye&untouched=stay")
            .flatMap(HttpClientRequest::rxSend)
            .test();

        awaitTerminalEvent(obs);
        obs
            .assertComplete()
            .assertValue(response -> {
                assertThat(response.statusCode()).isEqualTo(200);
                return true;
            })
            .assertNoErrors();

        wiremock.verify(
            getRequestedFor(urlPathEqualTo("/endpoint"))
                .withQueryParam("added", equalTo("addedValue"))
                .withQueryParam("toUpdate", equalTo("updatedValue"))
                .withQueryParam("untouched", equalTo("stay"))
                .withoutQueryParam("toRemove")
        );
    }

    @Test
    @DeployApi("/apis/v2/append-queryparams.json")
    void should_append_query_parameter(HttpClient client) throws InterruptedException {
        wiremock.stubFor(get(urlPathEqualTo("/endpoint")).willReturn(ok()));

        final TestObserver<HttpClientResponse> obs = client
            .rxRequest(HttpMethod.GET, "/test?appended=value0")
            .flatMap(HttpClientRequest::rxSend)
            .test();

        awaitTerminalEvent(obs);
        obs
            .assertComplete()
            .assertValue(response -> {
                assertThat(response.statusCode()).isEqualTo(200);
                return true;
            })
            .assertNoErrors();

        wiremock.verify(
            getRequestedFor(urlPathEqualTo("/endpoint")).withQueryParam(
                "appended",
                havingExactly(equalTo("value0"), equalTo("value1"), equalTo("value2"))
            )
        );
    }
}
