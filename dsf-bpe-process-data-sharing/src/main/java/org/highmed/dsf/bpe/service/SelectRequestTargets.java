package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGET;
import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGETS;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_BLOOM_FILTER_CONFIG;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY;
import static org.highmed.dsf.bpe.ConstantsDataSharing.EXTENSION_HIGHMED_PARTICIPATING_MEDIC;
import static org.highmed.dsf.bpe.ConstantsDataSharing.EXTENSION_HIGHMED_PARTICIPATING_TTP;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.crypto.KeyGenerator;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variable.BloomFilterConfig;
import org.highmed.dsf.bpe.variable.BloomFilterConfigValues;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Target;
import org.highmed.dsf.fhir.variables.TargetValues;
import org.highmed.dsf.fhir.variables.Targets;
import org.highmed.dsf.fhir.variables.TargetsValues;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.springframework.beans.factory.InitializingBean;

public class SelectRequestTargets extends AbstractServiceDelegate implements InitializingBean
{
	private final Random random = new Random();

	private final OrganizationProvider organizationProvider;
	private final KeyGenerator hmacSha2Generator;
	private final KeyGenerator hmacSha3Generator;

	public SelectRequestTargets(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			OrganizationProvider organizationProvider, BouncyCastleProvider bouncyCastleProvider)
	{
		super(clientProvider, taskHelper);

		this.organizationProvider = organizationProvider;

		try
		{
			Objects.requireNonNull(bouncyCastleProvider, "bouncyCastleProvider");

			hmacSha2Generator = KeyGenerator.getInstance("HmacSHA256", bouncyCastleProvider);
			hmacSha3Generator = KeyGenerator.getInstance("HmacSHA3-256", bouncyCastleProvider);
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();
		Objects.requireNonNull(organizationProvider, "organizationProvider");
	}

	@Override
	protected void doExecute(DelegateExecution execution)
	{
		ResearchStudy researchStudy = (ResearchStudy) execution.getVariable(BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY);

		execution.setVariable(BPMN_EXECUTION_VARIABLE_TARGETS, TargetsValues.create(getMedicTargets(researchStudy)));
		execution.setVariable(BPMN_EXECUTION_VARIABLE_TARGET, TargetValues.create(getTtpTarget(researchStudy)));

		Boolean needsRecordLinkage = (Boolean) execution.getVariable(BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE);

		if (Boolean.TRUE.equals(needsRecordLinkage))
		{
			execution.setVariable(BPMN_EXECUTION_VARIABLE_BLOOM_FILTER_CONFIG,
					BloomFilterConfigValues.create(createBloomFilterConfig()));
		}
	}

	private Targets getMedicTargets(ResearchStudy researchStudy)
	{
		List<Target> targets = researchStudy.getExtensionsByUrl(EXTENSION_HIGHMED_PARTICIPATING_MEDIC).stream()
				.filter(e -> e.getValue() instanceof Reference).map(e -> (Reference) e.getValue())
				.map(r -> Target.createBiDirectionalTarget(r.getIdentifier().getValue(), UUID.randomUUID().toString()))
				.collect(Collectors.toList());

		return new Targets(targets);
	}

	private Target getTtpTarget(ResearchStudy researchStudy)
	{
		return researchStudy.getExtensionsByUrl(EXTENSION_HIGHMED_PARTICIPATING_TTP).stream()
				.filter(e -> e.getValue() instanceof Reference).map(e -> (Reference) e.getValue())
				.map(r -> Target.createUniDirectionalTarget(r.getIdentifier().getValue())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException(
						"Participating TTP is not set in ResearchStudy with id='" + researchStudy.getId()
								+ "', this error should " + "have been caught by resource validation"));
	}

	private BloomFilterConfig createBloomFilterConfig()
	{
		return new BloomFilterConfig(random.nextLong(), hmacSha2Generator.generateKey(),
				hmacSha3Generator.generateKey());
	}
}
