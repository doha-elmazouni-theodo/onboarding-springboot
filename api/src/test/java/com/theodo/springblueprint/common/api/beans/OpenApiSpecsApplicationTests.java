package com.theodo.springblueprint.common.api.beans;

import com.jayway.jsonpath.JsonPath;
import com.theodo.springblueprint.testhelpers.baseclasses.BaseApplicationTestsWithoutDb;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.bind.annotation.*;

@Import(OpenApiSpecsApplicationTests.TestController.class)
class OpenApiSpecsApplicationTests extends BaseApplicationTestsWithoutDb {

    private static final String API_DOCS = "/v3/api-docs";

    @Test
    void openapi_docs_should_show_immutable_collection_as_array_in_request() {
        // Act
        RestTestClient.ResponseSpec responseSpec = buildSessionRestTestClient().get().uri(API_DOCS).exchange();

        responseSpec
            .expectStatus()
            .isOk()
            .expectBody()
            .jsonPath("$.components.schemas.TestRequest")
            .isEqualTo(
                parseJson(
                    """
                    {
                       "type": "object",
                       "properties": {
                         "stringList": {
                           "type": "array",
                           "items": {
                             "type": "string"
                           }
                         },
                         "testRecordList": {
                           "type": "array",
                           "items": {
                             "type": "object",
                             "properties": {
                               "name": {
                                 "type": "string"
                               },
                               "age": {
                                 "type": "integer",
                                 "format": "int32"
                               }
                             }
                           }
                         },
                         "stringSet": {
                           "type": "array",
                           "items": {
                             "type": "string"
                           }
                         },
                         "testRecordSet": {
                           "type": "array",
                           "items": {
                             "type": "object",
                             "properties": {
                               "name": {
                                 "type": "string"
                               },
                               "age": {
                                 "type": "integer",
                                 "format": "int32"
                               }
                             }
                           }
                         }
                       }
                     }
                    """
                )
            );
    }

    @Test
    void openapi_docs_should_show_immutable_collection_as_array_in_response() {
        // Act
        RestTestClient.ResponseSpec responseSpec = buildSessionRestTestClient().get().uri(API_DOCS).exchange();

        responseSpec
            .expectStatus()
            .isOk()
            .expectBody()
            .jsonPath("$.components.schemas.TestResponse")
            .isEqualTo(
                parseJson(
                    """
                    {
                      "type": "object",
                      "properties": {
                        "stringList": {
                          "type": "array",
                          "items": {
                            "type": "string"
                          }
                        },
                        "testRecordSet": {
                          "type": "array",
                          "items": {
                            "type": "object",
                            "properties": {
                              "name": {
                                "type": "string"
                              },
                              "age": {
                                "type": "integer",
                                "format": "int32"
                              }
                            }
                          }
                        }
                      }
                    }
                    """
                )
            );
    }

    @Test
    void openapi_docs_should_render_nested_immutable_collection_properly() {
        // Act
        RestTestClient.ResponseSpec responseSpec = buildSessionRestTestClient().get().uri(API_DOCS).exchange();

        responseSpec
            .expectStatus()
            .isOk()
            .expectBody()
            .jsonPath("$.components.schemas.NestedImmutableList")
            .isEqualTo(
                parseJson(
                    """
                    {
                      "type": "object",
                      "properties": {
                        "data": {
                          "type": "array",
                          "items": {
                            "type": "array",
                            "items": {
                              "type": "string"
                            }
                          }
                        }
                      }
                    }
                    """
                )
            );
    }

    private LinkedHashMap<String, Object> parseJson(String json) {
        return JsonPath.parse(json).read("$");
    }

    @TestComponent
    @RestController
    @RequestMapping(TestController.API_CONTROLLER)
    static class TestController {

        private static final String API_CONTROLLER = "/__test__";
        private static final String API_IMMUTABLE_ENDPOINT = "/__immutable_collections__";
        private static final String API_NESTED_ENDPOINT = "/__nested_immutable_collections__";

        //

        @PostMapping(API_IMMUTABLE_ENDPOINT)
        TestResponse testImmutableCollections(@RequestBody @Valid TestRequest request) {
            return new TestResponse(request.stringList(), request.testRecordSet());
        }

        @PostMapping(API_NESTED_ENDPOINT)
        String testNestedCollections(@RequestBody @Valid NestedImmutableList request) {
            return "OK";
        }

        //

        record TestRequest(
            ImmutableList<String> stringList,
            ImmutableList<TestRecord> testRecordList,
            ImmutableSet<String> stringSet,
            ImmutableSet<TestRecord> testRecordSet
        ) {
        }

        record TestResponse(ImmutableList<String> stringList, ImmutableSet<TestRecord> testRecordSet) {
        }

        record NestedImmutableList(ImmutableList<ImmutableList<String>> data) {
        }

        record TestRecord(String name, int age) {
        }
    }
}
