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
package io.gravitee.policy.transformqueryparams.configuration;

import io.gravitee.policy.api.PolicyConfiguration;
import java.util.ArrayList;
import java.util.List;

/**
 * @author David BRASSELY (brasseld at gmail.com)
 */
public class TransformQueryParametersPolicyConfiguration implements PolicyConfiguration {

    private List<String> removeQueryParameters = new ArrayList<>();

    private List<HttpQueryParameter> addQueryParameters = new ArrayList<>();

    private boolean clearAll = false;

    public List<HttpQueryParameter> getAddQueryParameters() {
        return addQueryParameters;
    }

    public void setAddQueryParameters(List<HttpQueryParameter> addQueryParameters) {
        this.addQueryParameters = addQueryParameters;
    }

    public List<String> getRemoveQueryParameters() {
        return removeQueryParameters;
    }

    public void setRemoveQueryParameters(List<String> removeQueryParameters) {
        this.removeQueryParameters = removeQueryParameters;
    }

    public boolean isClearAll() {
        return clearAll;
    }

    public void setClearAll(boolean clearAll) {
        this.clearAll = clearAll;
    }
}
