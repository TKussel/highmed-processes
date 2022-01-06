package org.highmed.dsf.bpe.spring.config;

import org.highmed.dsf.bpe.variables.MpcFeasibilityQueryResultSerializer;
import org.highmed.dsf.bpe.variables.MpcFeasibilityQueryResultsSerializer;
import org.highmed.dsf.bpe.variables.FinalMpcFeasibilityQueryResultSerializer;
import org.highmed.dsf.bpe.variables.FinalMpcFeasibilityQueryResultsSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class MpcFeasibilitySerializerConfig
{
	@Autowired
	private ObjectMapper objectMapper;

	@Bean
	public MpcFeasibilityQueryResultSerializer feasibilityQueryResultSerializer()
	{
		return new MpcFeasibilityQueryResultSerializer(objectMapper);
	}

	@Bean
	public MpcFeasibilityQueryResultsSerializer feasibilityQueryResultsSerializer()
	{
		return new MpcFeasibilityQueryResultsSerializer(objectMapper);
	}

	@Bean
	public FinalMpcFeasibilityQueryResultSerializer finalMpcFeasibilityQueryResultSerializer()
	{
		return new FinalMpcFeasibilityQueryResultSerializer(objectMapper);
	}

	@Bean
	public FinalMpcFeasibilityQueryResultsSerializer finalMpcFeasibilityQueryResultsSerializer()
	{
		return new FinalMpcFeasibilityQueryResultsSerializer(objectMapper);
	}
}
