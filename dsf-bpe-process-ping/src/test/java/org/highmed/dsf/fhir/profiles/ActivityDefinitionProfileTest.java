package org.highmed.dsf.fhir.profiles;

import static org.highmed.dsf.bpe.PingProcessPluginDefinition.VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.highmed.dsf.fhir.authorization.process.ProcessAuthorizationHelper;
import org.highmed.dsf.fhir.authorization.process.ProcessAuthorizationHelperImpl;
import org.highmed.dsf.fhir.validation.ResourceValidator;
import org.highmed.dsf.fhir.validation.ResourceValidatorImpl;
import org.highmed.dsf.fhir.validation.ValidationSupportRule;
import org.hl7.fhir.r4.model.ActivityDefinition;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.ValidationResult;

public class ActivityDefinitionProfileTest
{
	private static final Logger logger = LoggerFactory.getLogger(ActivityDefinitionProfileTest.class);

	@ClassRule
	public static final ValidationSupportRule validationRule = new ValidationSupportRule(VERSION,
			Arrays.asList("highmed-activity-definition-0.5.0.xml", "highmed-extension-process-authorization-0.5.0.xml",
					"highmed-extension-process-authorization-consortium-role-0.5.0.xml",
					"highmed-extension-process-authorization-organization-0.5.0.xml",
					"highmed-coding-process-authorization-local-all-0.5.0.xml",
					"highmed-coding-process-authorization-local-consortium-role-0.5.0.xml",
					"highmed-coding-process-authorization-local-organization-0.5.0.xml",
					"highmed-coding-process-authorization-remote-all-0.5.0.xml",
					"highmed-coding-process-authorization-remote-consortium-role-0.5.0.xml",
					"highmed-coding-process-authorization-remote-organization-0.5.0.xml"),
			Arrays.asList("highmed-read-access-tag-0.5.0.xml", "highmed-process-authorization-0.5.0.xml"),
			Arrays.asList("highmed-read-access-tag-0.5.0.xml", "highmed-process-authorization-recipient-0.5.0.xml",
					"highmed-process-authorization-requester-0.5.0.xml"));

	private final ResourceValidator resourceValidator = new ResourceValidatorImpl(validationRule.getFhirContext(),
			validationRule.getValidationSupport());

	private final ProcessAuthorizationHelper processAuthorizationHelper = new ProcessAuthorizationHelperImpl();

	@Test
	public void testPingValid() throws Exception
	{
		try (InputStream in = Files
				.newInputStream(Paths.get("src/main/resources/fhir/ActivityDefinition/highmed-ping.xml")))
		{
			ActivityDefinition ad = validationRule.getFhirContext().newXmlParser()
					.parseResource(ActivityDefinition.class, in);

			ValidationResult result = resourceValidator.validate(ad);
			ValidationSupportRule.logValidationMessages(logger, result);

			assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
					|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());

			assertTrue(
					processAuthorizationHelper.isValid(ad, taskProfile -> true, orgIdentifier -> true, role -> true));
		}
	}

	@Test
	public void testPongValid() throws Exception
	{
		try (InputStream in = Files
				.newInputStream(Paths.get("src/main/resources/fhir/ActivityDefinition/highmed-pong.xml")))
		{
			ActivityDefinition ad = validationRule.getFhirContext().newXmlParser()
					.parseResource(ActivityDefinition.class, in);

			ValidationResult result = resourceValidator.validate(ad);
			ValidationSupportRule.logValidationMessages(logger, result);

			assertEquals(0, result.getMessages().stream().filter(m -> ResultSeverityEnum.ERROR.equals(m.getSeverity())
					|| ResultSeverityEnum.FATAL.equals(m.getSeverity())).count());

			assertTrue(
					processAuthorizationHelper.isValid(ad, taskProfile -> true, orgIdentifier -> true, role -> true));
		}
	}
}