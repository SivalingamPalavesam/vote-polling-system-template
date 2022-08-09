package com.System.config;

public class LiquibaseConfig 
{
	private static LiquibaseConfig instance = new LiquibaseConfig();

    public static LiquibaseConfig getInstance() {
        return instance;
    }

    private LiquibaseConfig() {
    }

    public boolean isEnabled() {
        String enabledProperty = System.getProperty("liquibase.ext.nochangeloglock.enabled");
        if (enabledProperty == null) {
            return true;
        }
        return Boolean.valueOf(enabledProperty);
    }
}
