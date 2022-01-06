package org.highmed.dsf.bpe.start;

import static org.highmed.dsf.bpe.start.ConstantsExampleStarters.MEDIC_1_DOCKER_FHIR_BASE_URL;

public class RequestMpcFeasibilityFromMedicsViaMedic1DockerExampleStarter
		extends AbstractRequestMpcFeasibilityFromMedicsViaMedic1ExampleStarter
{
	// Environment variable "DSF_CLIENT_CERTIFICATE_PATH" or args[0]: the path to the client-certificate
	// highmed-dsf/dsf-tools/dsf-tools-test-data-generator/cert/Webbrowser_Test_User/Webbrowser_Test_User_certificate.p12
	// Environment variable "DSF_CLIENT_CERTIFICATE_PASSWORD" or args[1]: the password of the client-certificate
	// password
	public static void main(String[] args) throws Exception
	{
		new RequestMpcFeasibilityFromMedicsViaMedic1DockerExampleStarter().main(args, MEDIC_1_DOCKER_FHIR_BASE_URL);
	}
}
