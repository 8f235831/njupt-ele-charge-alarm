package pers.u8f23.njupt_ele_poller.entity.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author 8f23
 * @create 2023/10/13-23:16
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RootConfig
{
	private static AtomicReference<RootConfig> instance = new AtomicReference<>();

	private String jSessionId;
	private String formalLog;
	private String keepAliveLog;

	private String emailHost;
	private String transportType;
	private String fromEmail;
	private String authCode;

	private Set<RequestItem> requests;

	public static void initReference(RootConfig config)
	{
		instance.set(config);
	}

	public static RootConfig getReference()
	{
		return instance.get();
	}

	public Map<String, String> linkResults()
	{
		Map<String, Map<String, String>> bufferedResult = new HashMap<>();
		for (RequestItem request : requests)
		{
			String name = request.getName();
			String singleResult = request.getResponseMsg();
			for (String receiver : request.getReceivers())
			{
				bufferedResult.compute(receiver, (r, subMap) -> {
					subMap = subMap != null ? subMap : new HashMap<>();
					subMap.put(name, singleResult);
					return subMap;
				});
			}
		}
		Map<String, String> result = new HashMap<>();
		bufferedResult.forEach((receiver, results) -> {
			StringBuffer stringBuffer = new StringBuffer();
			results.forEach((name, itemResult) -> stringBuffer
				.append("<li>")
				.append(name)
				.append("：")
				.append(itemResult == null ? "查询失败" : itemResult)
				.append("</li>"));
			result.put(receiver, stringBuffer.toString());
		});
		return result;
	}
}
