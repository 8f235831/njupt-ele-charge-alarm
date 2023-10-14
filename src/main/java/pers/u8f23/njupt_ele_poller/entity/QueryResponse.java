package pers.u8f23.njupt_ele_poller.entity;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author 8f23
 * @create 2023/10/13-23:15
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QueryResponse
{
	@SerializedName ("errmsg")
	private String msg;
}
