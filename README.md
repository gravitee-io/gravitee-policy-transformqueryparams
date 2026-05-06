
<!-- GENERATED CODE - DO NOT ALTER THIS OR THE FOLLOWING LINES -->
# Transform Query Parameters

[![Gravitee.io](https://img.shields.io/static/v1?label=Available%20at&message=Gravitee.io&color=1EC9D2)](https://download.gravitee.io/#graviteeio-apim/plugins/policies/gravitee-policy-transform-queryparams/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/gravitee-io/gravitee-policy-transform-queryparams/blob/master/LICENSE.txt)
[![Releases](https://img.shields.io/badge/semantic--release-conventional%20commits-e10079?logo=semantic-release)](https://github.com/gravitee-io/gravitee-policy-transform-queryparams/releases)
[![CircleCI](https://circleci.com/gh/gravitee-io/gravitee-policy-transform-queryparams.svg?style=svg)](https://circleci.com/gh/gravitee-io/gravitee-policy-transform-queryparams)

## Overview
You can use the `transform-queryparams` policy to override incoming HTTP request query parameters.

You can override HTTP query parameters by:

* Clearing all existing query parameters
* Adding to or updating the list of query parameters
* Removing query parameters individually

You can also append a value to an existing query parameter.

Query parameter values of the incoming request are accessible via the `{#request.params['query_parameter_name']}` construct.




## Phases
The `transform-queryparams` policy can be applied to the following API types and flow phases.

### Compatible API types

* `PROXY`
* `MESSAGE`

### Supported flow phases:

* Request

## Compatibility matrix
Strikethrough text indicates that a version is deprecated.

| Plugin version| APIM| Java version |
| --- | --- | ---  |
|2.x|4.8.x to latest|21 |
|1.7.x|4.0.x to 4.7.x|17 |
|~~1.6.x~~|~~3.x~~|~~-~~ |


## Configuration options


#### 
| Name <br>`json name`  | Type <br>`constraint`  | Mandatory  | Default  | Description  |
|:----------------------|:-----------------------|:----------:|:---------|:-------------|
| Add or replace query parameter<br>`addQueryParameters`| array|  | | <br/>See "Add or replace query parameter" section.|
| Clear all query parameters<br>`clearAll`| boolean|  | | Please be aware that by clearing all query parameters, you mustn't be able to use them in expression language.|
| Remove query parameters<br>`removeQueryParameters`| array (string)|  | | |


#### Add or replace query parameter (Array)
| Name <br>`json name`  | Type <br>`constraint`  | Mandatory  | Default  | Description  |
|:----------------------|:-----------------------|:----------:|:---------|:-------------|
| Append the value to existing queryParam as an array (i.e. ?key=v1&key=v2&key=v3) ?<br>`appendToExistingArray`| boolean|  | | |
| Name<br>`name`| string| ✅| | |
| Value. (Supports EL)<br>`value`| string| ✅| | |




## Examples

*Add, update and remove query parameters*
```json
{
  "api": {
    "definitionVersion": "V4",
    "type": "PROXY",
    "name": "Transform Query Parameters example API",
    "flows": [
      {
        "name": "Common Flow",
        "enabled": true,
        "selectors": [
          {
            "type": "HTTP",
            "path": "/",
            "pathOperator": "STARTS_WITH"
          }
        ],
        "request": [
          {
            "name": "Transform Query Parameters",
            "enabled": true,
            "policy": "transform-queryparams",
            "configuration":
              {
                  "addQueryParameters": [
                      { "name": "added", "value": "addedValue" },
                      { "name": "toUpdate", "value": "updatedValue" }
                  ],
                  "removeQueryParameters": ["toRemove"]
              }
          }
        ]
      }
    ]
  }
}

```
*Clear all and add a new query parameter*
```json
{
  "api": {
    "definitionVersion": "V4",
    "type": "PROXY",
    "name": "Transform Query Parameters example API",
    "flows": [
      {
        "name": "Common Flow",
        "enabled": true,
        "selectors": [
          {
            "type": "HTTP",
            "path": "/",
            "pathOperator": "STARTS_WITH"
          }
        ],
        "request": [
          {
            "name": "Transform Query Parameters",
            "enabled": true,
            "policy": "transform-queryparams",
            "configuration":
              {
                  "clearAll": true,
                  "addQueryParameters": [{ "name": "added", "value": "addedValue" }]
              }
          }
        ]
      }
    ]
  }
}

```
*Append values to an existing query parameter*
```json
{
  "api": {
    "definitionVersion": "V4",
    "type": "PROXY",
    "name": "Transform Query Parameters example API",
    "flows": [
      {
        "name": "Common Flow",
        "enabled": true,
        "selectors": [
          {
            "type": "HTTP",
            "path": "/",
            "pathOperator": "STARTS_WITH"
          }
        ],
        "request": [
          {
            "name": "Transform Query Parameters",
            "enabled": true,
            "policy": "transform-queryparams",
            "configuration":
              {
                  "addQueryParameters": [
                      { "name": "appended", "value": "value1", "appendToExistingArray": true },
                      { "name": "appended", "value": "value2", "appendToExistingArray": true }
                  ]
              }
          }
        ]
      }
    ]
  }
}

```


## Changelog

### [1.9.0](https://github.com/gravitee-io/gravitee-policy-transformqueryparams/compare/1.8.0...1.9.0) (2023-12-19)


##### Features

* enable policy on REQUEST phase for message APIs ([5d080cd](https://github.com/gravitee-io/gravitee-policy-transformqueryparams/commit/5d080cd570df79b3373f10d017c485886718f219)), closes [gravitee-io/issues#9430](https://github.com/gravitee-io/issues/issues/9430)

### [1.8.0](https://github.com/gravitee-io/gravitee-policy-transformqueryparams/compare/1.7.1...1.8.0) (2023-12-01)


##### Features

* add an option to handle array of values in a query parameter ([253127b](https://github.com/gravitee-io/gravitee-policy-transformqueryparams/commit/253127bc1a071413ac124a11237707972f9ed557))

#### [1.7.1](https://github.com/gravitee-io/gravitee-policy-transformqueryparams/compare/1.7.0...1.7.1) (2023-07-20)


##### Bug Fixes

* update policy description ([91bc7bd](https://github.com/gravitee-io/gravitee-policy-transformqueryparams/commit/91bc7bd375a9a53bd13c11591717e0a2be694cce))

### [1.7.0](https://github.com/gravitee-io/gravitee-policy-transformqueryparams/compare/1.6.0...1.7.0) (2023-07-05)


##### Features

* addition of the execution phase ([9061fa3](https://github.com/gravitee-io/gravitee-policy-transformqueryparams/commit/9061fa36f18948a03fa57abce95b509576703264))

