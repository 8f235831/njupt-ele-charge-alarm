# 南邮电费查询程序

自动查询指定房间的电费余额，并将发送至邮箱。服务依赖于财务处微信服务。

## 程序编译

源程序项目目录下执行下列指令：

```shell
gradlew build
```

编译结果将输出至`./build/distributions`目录下，解包后程序入口命令位于`bin`目录下。

## 程序运行

### 依赖

* Java 11+

下列说明将以基于Linux发行版的服务器为部署目标设备进行。

### 配置文件

文件为`JSON`类型，字段说明如下。其中，`jSessionId`与`requestBody`字段的内容将在后文说明。

```typescript
{
    // cookie会话标识。
    jSessionId: string;
    // 日志文件地址。
    formalLog: string;
    // 保活请求日志文件地址。
    keepAliveLog: string;
    // 邮件服务器地址。
    emailHost: string;
    // 邮件协议名，如"smtp".
    transportType: string;
    // 发送自邮箱。
    fromEmail: string;
    // 邮箱访问口令。
    authCode: string;
    // 需要请求的房间列表。
    requests:Array<{
        // 房间名，任意可识别文本即可。
        name: string;
        // POST请求body内容。
        requestBody: {
            aid: string;
            area: string;
            building: string;
            floor: string;
            room: string;
        },
        // 接收者邮箱。
        receivers: Array<string>
    }>
}
```

### 初始数据抓包

* 登录电脑端微信。
* 启动Wireshark或其它可用的流量抓包软件，监听`wxcard.njupt.edu.cn:80`方向的`HTTP`请求与响应。
* 打开财务处微信网页，查找对应房间的电费，在对应内容显示后结束流量监听。
* 找到往`/wechat/basicQuery/queryElecRoomInfo.html`发送的请求，并确定请求内容与希望的房间对应。记录对应请求中`Cookie`
  中`JSESSIONID`字段内容与`POST`请求的表单内容（即`body`内容）。
* `JSESSIONID`的内容填写至配置文件的`jSessionId`字段中，表单内容填写至配置文件的`requestBody`字段中。
* 如需查询多个房间的电费，需要重复抓包对应房间的请求表单数据并填写。

### 执行程序

程序执行时需要两个参数，第一个参数为配置文件路径，第二个参数为是否执行保活命令。

正式请求命令：

```shell
sh /njupt-ele-alarm/software/bin/njupt-ele-charge-alarm /njupt-ele-alarm/config.json
```

保活命令：

```shell
sh /njupt-ele-alarm/software/bin/njupt-ele-charge-alarm /njupt-ele-alarm/config.json keep-alive
```

## 程序配置

`JSESSIONID`有效期较短，长期未发起请求将失效（30分钟以上），需要频繁执行请求才可保活。推荐使用`crontab`
配置正式请求与保活请求。`crontab`推荐配置如下：

```text
0 19 * * * sh /njupt-ele-alarm/software/bin/njupt-ele-charge-alarm /njupt-ele-alarm/config.json
*/5 * * * * sh /njupt-ele-alarm/software/bin/njupt-ele-charge-alarm /njupt-ele-alarm/config.json keep-alive
```

配置前请手动执行指令，观察程序能否正常运行。

## 致谢

感谢下列第三方库的开发者们为我节省了造轮子的时间。

* [Project Lombok](https://projectlombok.org/)
* [SLF4J](https://www.slf4j.org/)
* [RxJava3](https://github.com/ReactiveX/RxJava)
* [Gson](https://github.com/google/gson)
* [Retrofit2](https://square.github.io/retrofit/)
* [OkHttp3](https://square.github.io/okhttp/)
* [Jakarta Mail](https://jakartaee.github.io/mail-api/)