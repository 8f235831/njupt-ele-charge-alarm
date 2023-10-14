package pers.u8f23.njupt_ele_poller;

import io.reactivex.rxjava3.core.Single;
import pers.u8f23.njupt_ele_poller.entity.QueryResponse;
import retrofit2.Response;
import retrofit2.http.*;

import java.util.Map;

/**
 * @author 8f23
 * @create 2023/10/13-23:34
 */
public interface Service
{
	@FormUrlEncoded
	@POST ("/wechat/basicQuery/queryElecRoomInfo.html")
	Single<Response<QueryResponse>> queryEleRoomInfo(
		@FieldMap Map<String, String> body
	);
}