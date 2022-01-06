package org.highmed.dsf.bpe.variables;

import java.io.IOException;
import java.util.Objects;

import org.camunda.bpm.engine.impl.variable.serializer.PrimitiveValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.highmed.dsf.bpe.variables.FinalMpcFeasibilityQueryResultsValues.FinalMpcFeasibilityQueryResultsValue;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FinalMpcFeasibilityQueryResultsSerializer extends PrimitiveValueSerializer<FinalMpcFeasibilityQueryResultsValue>
		implements InitializingBean
{
	private final ObjectMapper objectMapper;

	public FinalMpcFeasibilityQueryResultsSerializer(ObjectMapper objectMapper)
	{
		super(FinalMpcFeasibilityQueryResultsValues.VALUE_TYPE);

		this.objectMapper = objectMapper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(objectMapper, "objectMapper");
	}

	@Override
	public void writeValue(FinalMpcFeasibilityQueryResultsValue value, ValueFields valueFields)
	{
		FinalMpcFeasibilityQueryResults results = value.getValue();
		try
		{
			if (results != null)
				valueFields.setByteArrayValue(objectMapper.writeValueAsBytes(results));
		}
		catch (JsonProcessingException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public FinalMpcFeasibilityQueryResultsValue convertToTypedValue(UntypedValueImpl untypedValue)
	{
		return FinalMpcFeasibilityQueryResultsValues.create((FinalMpcFeasibilityQueryResults) untypedValue.getValue());
	}

	@Override
	public FinalMpcFeasibilityQueryResultsValue readValue(ValueFields valueFields, boolean asTransientValue)
	{
		byte[] bytes = valueFields.getByteArrayValue();

		try
		{
			FinalMpcFeasibilityQueryResults results = (bytes == null || bytes.length <= 0) ? null
					: objectMapper.readValue(bytes, FinalMpcFeasibilityQueryResults.class);
			return FinalMpcFeasibilityQueryResultsValues.create(results);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
