package net.engining.pg.props;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

/**
 * 工程通用配置
 * 
 * @author luxue
 *
 */
@ConfigurationProperties(prefix = "pg.common")
public class CommonProperties implements EnvironmentAware {

	private static final String UNKNOWN = "unknown";
	private String appname = UNKNOWN;
	
	private String appVersion = UNKNOWN;

	public String getAppname() {
		return appname;
	}

	public void setAppname(String appname) {
		this.appname = appname;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	@Override
	public void setEnvironment(Environment environment) {
		String springAppName = environment.getProperty("spring.application.name");
		String springAppVersion = environment.getProperty("info.version");
		if (org.springframework.util.StringUtils.hasText(springAppName)) {
			if (StringUtils.isBlank(this.appname) || UNKNOWN.equals(this.appname)) {
				setAppname(springAppName);
			}
		}

		if (org.springframework.util.StringUtils.hasText(springAppVersion)) {
			if (StringUtils.isBlank(this.appVersion) || UNKNOWN.equals(this.appVersion)) {
				setAppVersion(springAppVersion);
			}
		}
	}

}
