package org.highmed.dsf.bpe.start;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsPing.PROFILE_HIGHMED_TASK_PING_PROCESS_URI_AND_LATEST_VERSION;
import static org.highmed.dsf.bpe.ConstantsPing.PROFILE_HIGHMED_TASK_START_PING;
import static org.highmed.dsf.bpe.ConstantsPing.PROFILE_HIGHMED_TASK_START_PING_MESSAGE_NAME;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_TTP;
import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.TTP_FHIR_BASE_URL;

import java.util.Date;

import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskStatus;

public class Ping3MedicFromTtpExampleStarter
{
	// Environment variable "DSF_CLIENT_CERTIFICATE_PATH" or args[0]: the path to the client-certificate
	// highmed-dsf/dsf-tools/dsf-tools-test-data-generator/cert/Webbrowser_Test_User/Webbrowser_Test_User_certificate.p12
	// Environment variable "DSF_CLIENT_CERTIFICATE_PASSWORD" or args[1]: the password of the client-certificate
	// password
	public static void main(String[] args) throws Exception
	{
		Task task = createStartResource();
		ExampleStarter.forServer(args, TTP_FHIR_BASE_URL).startWith(task);
	}

	private static Task createStartResource()
	{
		Task task = new Task();
		task.getMeta().addProfile(PROFILE_HIGHMED_TASK_START_PING);
		task.setInstantiatesUri(PROFILE_HIGHMED_TASK_PING_PROCESS_URI_AND_LATEST_VERSION);
		task.setStatus(TaskStatus.REQUESTED);
		task.setIntent(TaskIntent.ORDER);
		task.setAuthoredOn(new Date());
		task.getRequester().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER)
				.setValue(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_TTP);
		task.getRestriction().addRecipient().setType(ResourceType.Organization.name()).getIdentifier()
				.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER)
				.setValue(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_VALUE_TTP);

		task.addInput().setValue(new StringType(PROFILE_HIGHMED_TASK_START_PING_MESSAGE_NAME)).getType().addCoding()
				.setSystem(CODESYSTEM_HIGHMED_BPMN).setCode(CODESYSTEM_HIGHMED_BPMN_VALUE_MESSAGE_NAME);

		return task;
	}
}
