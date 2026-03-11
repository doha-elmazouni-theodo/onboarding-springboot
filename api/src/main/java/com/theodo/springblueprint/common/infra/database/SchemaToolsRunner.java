package com.theodo.springblueprint.common.infra.database;

import java.sql.Statement;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.relational.SchemaManager;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("schema-tools")
@RequiredArgsConstructor
class SchemaToolsRunner implements ApplicationRunner {

    private final SessionFactory sessionFactory;
    private final ConfigurableApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments args) {
        if (args.getSourceArgs().length == 0) {
            return;
        }
        SchemaManager schemaManager = sessionFactory.getSchemaManager();
        String operation = getRequiredOptionValue(args, "schema-tools.operation");
        switch (operation) {
            case "export" -> {
                String schema = getRequiredOptionValue(args, "schema-tools.export_schema");
                dropSchemaIfExists(schema);
                schemaManager.forSchema(schema).exportMappedObjects(true);
            }
            case "validate_default_schema" -> schemaManager.validateMappedObjects();
            default -> {
            }
        }
        applicationContext.close();
    }

    private static String getRequiredOptionValue(ApplicationArguments args, String optionName) {
        List<String> values = args.getOptionValues(optionName);
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Missing required option: " + optionName);
        }
        return values.getFirst();
    }

    private void dropSchemaIfExists(String schema) {
        try (Session session = sessionFactory.openSession()) {
            session.doWork(connection -> {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("DROP SCHEMA IF EXISTS \"" + schema + "\" CASCADE");
                }
            });
        }
    }
}
