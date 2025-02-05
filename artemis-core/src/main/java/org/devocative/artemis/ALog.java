package org.devocative.artemis;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.sift.SiftingAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.sift.AbstractDiscriminator;
import ch.qos.logback.core.sift.Discriminator;
import ch.qos.logback.core.util.FileSize;

public class ALog {
	private static final String PATTERN = "%date %-5level - %msg%n";
	private static final LoggerContext lc = new LoggerContext();
	private static Logger log = null;

	public synchronized static void init(String name, boolean enableConsole) {
		Thread.currentThread().setName(name);

		if (log != null) {
			return;
		}

		lc.start();

		final Discriminator<ILoggingEvent> discriminator = new AbstractDiscriminator<ILoggingEvent>() {
			@Override
			public String getDiscriminatingValue(ILoggingEvent iLoggingEvent) {
				return Thread.currentThread().getName();
			}

			@Override
			public String getKey() {
				return null;
			}
		};
		discriminator.start();

		final SiftingAppender sa = new SiftingAppender();
		sa.setContext(lc);
		sa.setName("Artemis");
		sa.setDiscriminator(discriminator);
		sa.setAppenderFactory((context, discriminatingValue) -> {
			final PatternLayoutEncoder ple = new PatternLayoutEncoder();
			ple.setContext(context);
			ple.setPattern(PATTERN);
			ple.start();

			final RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
			appender.setContext(context);
			appender.setName(discriminatingValue);
			appender.setFile("logs/" + discriminatingValue + ".log");
			appender.setEncoder(ple);

			final SizeAndTimeBasedRollingPolicy<ILoggingEvent> policy = new SizeAndTimeBasedRollingPolicy<>();
			policy.setContext(context);
			policy.setParent(appender);
			policy.setMaxHistory(5);
			policy.setFileNamePattern("logs/" + discriminatingValue + "-%d{yyyy-MM-dd-HH}-%i.log");
			policy.setMaxFileSize(FileSize.valueOf("5mb"));
			policy.start();

			appender.setRollingPolicy(policy);
			appender.start();

			return appender;
		});
		sa.start();

		final Logger logger = lc.getLogger(ALog.class);
		logger.setLevel(Level.INFO);
		logger.setAdditive(false);
		logger.addAppender(sa);

		if (enableConsole) {
			final PatternLayoutEncoder ple = new PatternLayoutEncoder();
			ple.setContext(lc);
			ple.setPattern(PATTERN);
			ple.start();

			final ConsoleAppender<ILoggingEvent> ca = new ConsoleAppender<>();
			ca.setContext(lc);
			ca.setEncoder(ple);
			ca.start();

			logger.addAppender(ca);
		}

		log = logger;
	}

	public static void info(String s, Object... params) {
		log.info(s, params);
	}

	public static void warn(String s, Object... params) {
		log.warn(s, params);
	}

	public static void error(String s, Object... params) {
		log.error(s, params);
	}
}
