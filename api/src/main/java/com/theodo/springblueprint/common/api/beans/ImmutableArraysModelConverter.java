package com.theodo.springblueprint.common.api.beans;

// Do no migrate Jackson packages to v3 because SpringDoc still uses v2 indirectly via swagger-core
// see: https://github.com/swagger-api/swagger-core/issues/4991
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.theodo.springblueprint.common.utils.collections.Immutable;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Iterator;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

//
// Source: https://github.com/springdoc/springdoc-openapi/blob/main/springdoc-openapi-tests/springdoc-openapi-javadoc-tests/src/test/java/test/org/springdoc/api/v31/app157/StringyConverter.java
// A helper class to allow OpenAPI to correctly interpret Immutable Collections from Eclipse Collections as arrays in the generated OpenAPI documentation.
//
@Component
@ConditionalOnProperty(name = "springdoc.api-docs.enabled", havingValue = "true")
public class ImmutableArraysModelConverter implements ModelConverter {

    private final ObjectMapper objectMapper;
    private static final ImmutableList<Class<?>> supportedClasses = Immutable.list.of(
        ImmutableList.class,
        ImmutableSet.class
    );

    public ImmutableArraysModelConverter() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Schema<?> resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        JavaType javaType = objectMapper.constructType(type.getType());

        for (Class<?> supportedClass : supportedClasses) {
            if (javaType.getRawClass().equals(supportedClass)) {
                // If the type is one of the supported classes, we can create an array schema
                JavaType elementType = getFirstElementType(javaType);
                return createArraySchema(context, elementType);
            }
        }

        return chain.next().resolve(type, context, chain);
    }

    private JavaType getFirstElementType(JavaType type) {
        TypeBindings bindings = type.getBindings();
        return bindings.getBoundType(0);
    }

    private ArraySchema createArraySchema(ModelConverterContext context, JavaType elementType) {
        Schema<?> itemSchema = context.resolve(new AnnotatedType(elementType));

        ArraySchema arraySchema = new ArraySchema();
        arraySchema.setItems(itemSchema);

        return arraySchema;
    }
}