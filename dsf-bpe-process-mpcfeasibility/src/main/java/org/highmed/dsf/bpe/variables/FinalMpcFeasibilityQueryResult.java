package org.highmed.dsf.bpe.variables;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FinalMpcFeasibilityQueryResult
{
	private final String cohortId;
	private final int participatingMedics;
	private final int cohortSize;

	@JsonCreator
	public FinalMpcFeasibilityQueryResult(@JsonProperty("cohortId") String cohortId,
			@JsonProperty("participatingMedics") int participatingMedics, @JsonProperty("cohortSize") int cohortSize)
	{
		this.cohortId = cohortId;
		this.participatingMedics = participatingMedics;
		this.cohortSize = cohortSize;
	}

	public String getCohortId()
	{
		return cohortId;
	}

	public int getParticipatingMedics()
	{
		return participatingMedics;
	}

	public int getCohortSize()
	{
		return cohortSize;
	}
}
