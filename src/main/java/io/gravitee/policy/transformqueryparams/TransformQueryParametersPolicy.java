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

import io.gravitee.gateway.api.ExecutionContext;
import io.gravitee.gateway.api.Request;
import io.gravitee.gateway.api.Response;
import io.gravitee.policy.api.PolicyChain;
import io.gravitee.policy.api.annotations.OnRequest;
import io.gravitee.policy.transformqueryparams.configuration.TransformQueryParametersPolicyConfiguration;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class TransformQueryParametersPolicy {

    private static final String WHITESPACE = " ";
    private static final String ENCODED_WHITESPACE = "%20";

    /**
     * Transform headers configuration
     */
    private final TransformQueryParametersPolicyConfiguration transformQueryParametersPolicyConfiguration;

    public TransformQueryParametersPolicy(final TransformQueryParametersPolicyConfiguration transformQueryParametersPolicyConfiguration) {
        this.transformQueryParametersPolicyConfiguration = transformQueryParametersPolicyConfiguration;
    }

    @OnRequest
    public void onRequest(Request request, Response response, ExecutionContext executionContext, PolicyChain policyChain) {
        // Clear all parameters
        if (transformQueryParametersPolicyConfiguration.isClearAll()) {
            request.parameters().clear();
        }

        // Add or update query parameters
        if (transformQueryParametersPolicyConfiguration.getAddQueryParameters() != null) {
            transformQueryParametersPolicyConfiguration.getAddQueryParameters().forEach(
                    queryParameter -> {
                        if (queryParameter.getName() != null && ! queryParameter.getName().trim().isEmpty()) {
                            try {
                                String extValue = (queryParameter.getValue() != null) ?
                                        executionContext.getTemplateEngine().convert(queryParameter.getValue()) : null;
                                //encode whitespace
                                String name;
                                if (queryParameter.getName().contains(WHITESPACE)) {
                                    name = queryParameter.getName().replaceAll(WHITESPACE, ENCODED_WHITESPACE);
                                } else {
                                    name = queryParameter.getName();
                                }

                                if (extValue.contains(WHITESPACE)) {
                                    extValue = extValue.replaceAll(WHITESPACE, ENCODED_WHITESPACE);
                                }
                                List<String> values = new LinkedList<>();
                                values.add(extValue);
                                request.parameters().put(name, values);
                            } catch (Exception ex) {
                                // Do nothing
                            }
                        }
                    });
        }

        // Remove query parameters
        if (! transformQueryParametersPolicyConfiguration.isClearAll() &&
                transformQueryParametersPolicyConfiguration.getRemoveQueryParameters() != null) {
            transformQueryParametersPolicyConfiguration.getRemoveQueryParameters()
                    .forEach(queryParameterName -> {
                        if (queryParameterName != null && ! queryParameterName.trim().isEmpty()) {
                            request.parameters().remove(queryParameterName);
                        }
                    });
        }

        // Apply next policy in chain
        policyChain.doNext(request, response);
    }
}
