package org.abrahamalarcon.datastream.service;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.StaticDataFetcher;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.abrahamalarcon.datastream.dom.response.DatastoreResponse;

import java.io.IOException;

//@Service
public class Query
{
    //@Autowired
    TypeDefinitionRegistry typeDefinitionRegistry;

    public String apply(String subscription, String inMessage) throws IOException {
        final RuntimeWiring wiring = RuntimeWiring.newRuntimeWiring()
                .type("Query", builder -> builder.dataFetcher("result",
                        new StaticDataFetcher(new DatastoreResponse())))
                //.type("DatastoreResponse", builder -> builder.dataFetcher("", ))

                .build();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema schema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, wiring);
        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionInput executionInput = ExecutionInput.newExecutionInput().query(subscription).build();
        final ExecutionResult result = graphQL.execute(executionInput);
        return result.getData().toString();
    }

    /*
    RuntimeWiring buildDynamicRuntimeWiring() {
        WiringFactory dynamicWiringFactory = new WiringFactory() {
            @Override
            public boolean providesTypeResolver(TypeDefinitionRegistry registry, InterfaceTypeDefinition definition) {
                return getDirective(definition,"specialMarker") != null;
            }

            @Override
            public boolean providesTypeResolver(TypeDefinitionRegistry registry, UnionTypeDefinition definition) {
                return getDirective(definition,"specialMarker") != null;
            }

            @Override
            public TypeResolver getTypeResolver(TypeDefinitionRegistry registry, InterfaceTypeDefinition definition) {
                Directive directive  = getDirective(definition,"specialMarker");
                return createTypeResolver(definition,directive);
            }

            @Override
            public TypeResolver getTypeResolver(TypeDefinitionRegistry registry, UnionTypeDefinition definition) {
                Directive directive  = getDirective(definition,"specialMarker");
                return createTypeResolver(definition,directive);
            }

            @Override
            public boolean providesDataFetcher(TypeDefinitionRegistry registry, FieldDefinition definition) {
                return getDirective(definition,"dataFetcher") != null;
            }

            @Override
            public DataFetcher getDataFetcher(TypeDefinitionRegistry registry, FieldDefinition definition) {
                Directive directive = getDirective(definition, "dataFetcher");
                return createDataFetcher(definition,directive);
            }
        };
        return RuntimeWiring.newRuntimeWiring()
                .wiringFactory(dynamicWiringFactory).build();
    }
    */
}

