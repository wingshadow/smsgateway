## smsgateway 介绍：
> 给予cmpp3.0协议开发出 短信网关模拟器
## 配置文件说明
> 文件路径：src/main/resources/smsgateway/properties
> - gatewayPort  监听端口
> - gatewayVersion 网关版本
> - clientConfig 客户端配置
>   - JSON说明
>     - spIp 客户端IP
>     - spId 网关分配的SPID 相当于登录账号<>
>     - sharedSecret  SPID对应的密码
>     - spCode  网关分配的SPCODE,即用户接受到的短信显示的主叫号码
>     - serviceId  企业代码
