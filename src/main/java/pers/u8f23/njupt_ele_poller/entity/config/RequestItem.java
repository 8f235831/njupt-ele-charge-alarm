package pers.u8f23.njupt_ele_poller.entity.config;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pers.u8f23.njupt_ele_poller.HttpUtils;
import pers.u8f23.njupt_ele_poller.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author 8f23
 * @create 2023/10/13-23:28
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RequestItem
{
	private String name;
	private Map<String, String> requestBody;
	private Set<String> receivers;
	/**
	 * 初始为 {@code null}。
	 */
	private transient String responseMsg;

	public Completable asRequest()
	{
		return HttpUtils.buildService(Service.class)
			.queryEleRoomInfo(this.requestBody)
			.subscribeOn(Schedulers.trampoline())
			.observeOn(Schedulers.trampoline())
			.map(res -> Objects.requireNonNull(res.body()).getMsg())
			.doOnSuccess(this::setResponseMsg)
			.ignoreElement()
			.onErrorComplete();
	}
}
