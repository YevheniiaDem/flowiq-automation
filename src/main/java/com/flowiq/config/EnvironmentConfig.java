package com.flowiq.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

@LoadPolicy(LoadType.MERGE)
@Sources({
        "classpath:environments/local.properties",
        "classpath:environments/${env}.properties",
        "system:properties",
        "system:env"
})
public interface EnvironmentConfig extends Config {

    @Key("env")
    @DefaultValue("local")
    String env();

    @Key("base.url")
    String baseUrl();

    @Key("api.url")
    String apiUrl();

    @Key("api.timeout")
    @DefaultValue("30000")
    int apiTimeout();

    @Key("ui.timeout")
    @DefaultValue("30000")
    int uiTimeout();

    @Key("browser")
    @DefaultValue("chromium")
    String browser();

    @Key("headless")
    @DefaultValue("true")
    boolean headless();

    @Key("slow.mo")
    @DefaultValue("0")
    int slowMo();

    @Key("test.user.email")
    String testUserEmail();

    @Key("test.user.password")
    String testUserPassword();

    @Key("db.url")
    String dbUrl();

    @Key("db.username")
    String dbUsername();

    @Key("db.password")
    String dbPassword();
}
